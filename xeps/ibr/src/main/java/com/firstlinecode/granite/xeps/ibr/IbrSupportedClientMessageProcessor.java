package com.firstlinecode.granite.xeps.ibr;

import java.util.List;

import com.firstlinecode.basalt.protocol.core.stream.Feature;
import com.firstlinecode.basalt.protocol.core.stream.Features;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.connection.IClientConnectionContext;
import com.firstlinecode.granite.framework.stream.IStreamNegotiant;
import com.firstlinecode.granite.framework.stream.negotiants.InitialStreamNegotiant;
import com.firstlinecode.granite.framework.stream.negotiants.ResourceBindingNegotiant;
import com.firstlinecode.granite.framework.stream.negotiants.SaslNegotiant;
import com.firstlinecode.granite.framework.stream.negotiants.SessionEstablishmentNegotiant;
import com.firstlinecode.granite.framework.stream.negotiants.TlsNegotiant;
import com.firstlinecode.granite.stream.standard.StandardClientMessageProcessor;
import com.firstlinecode.basalt.xeps.ibr.Register;
import com.firstlinecode.basalt.xeps.ibr.oxm.RegisterTranslatorFactory;

@Component("ibr.supported.client.message.processor")
public class IbrSupportedClientMessageProcessor extends StandardClientMessageProcessor {
	private static final int DEFAULT_REGISTRAR_TIMEOUT_CHECK_INTERVAL = 1000;
	private static final String CONFIGURATION_KEY_REGISTRAR_TIMEOUT_CHECK_INTERVAL = "registrar.timeout.check.interval";
	
	private int registrarTimeoutCheckInterval;
	
	@Dependency("registrar")
	private IRegistrar registrar;
	
	@Override
	protected IStreamNegotiant createNegotiant() {
		if (tlsRequired) {
			IStreamNegotiant initialStream = new InitialStreamNegotiant(hostName,
					getInitialStreamNegotiantAdvertisements());
			
			IStreamNegotiant tls = new IbrSupportedTlsNegotiant(hostName, tlsRequired,
					getTlsNegotiantAdvertisements());
			
			IStreamNegotiant ibrAfterTls = new IbrNegotiant(hostName,
					getTlsNegotiantAdvertisements(), registrar, registrarTimeoutCheckInterval);
			
			IStreamNegotiant sasl = new SaslNegotiant(hostName,
					saslSupportedMechanisms, saslAbortRetries, saslFailureRetries,
					getSaslNegotiantFeatures(), authenticator);
			
			IStreamNegotiant resourceBinding = new ResourceBindingNegotiant(
					hostName, sessionManager);
			IStreamNegotiant sessionEstablishment = new SessionEstablishmentNegotiant(
					router, sessionManager, eventMessageChannel, sessionListenerDelegate);
			
			resourceBinding.setNext(sessionEstablishment);
			sasl.setNext(resourceBinding);
			ibrAfterTls.setNext(sasl);
			tls.setNext(ibrAfterTls);
			initialStream.setNext(tls);
			
			return initialStream;
		} else {
			IStreamNegotiant initialStream = new IbrSupportedInitialStreamNegotiant(hostName,
					getInitialStreamNegotiantAdvertisements());
			
			IStreamNegotiant ibrBeforeTls = new IbrNegotiant(hostName,
					getInitialStreamNegotiantAdvertisements(), registrar, registrarTimeoutCheckInterval);
			
			IStreamNegotiant tls = new IbrSupportedTlsNegotiant(hostName, tlsRequired,
					getTlsNegotiantAdvertisements());
			
			IStreamNegotiant ibrAfterTls = new IbrNegotiant(hostName,
					getTlsNegotiantAdvertisements(), registrar, registrarTimeoutCheckInterval);
			
			IStreamNegotiant sasl = new SaslNegotiant(hostName,
					saslSupportedMechanisms, saslAbortRetries, saslFailureRetries,
					getSaslNegotiantFeatures(), authenticator);
			
			IStreamNegotiant resourceBinding = new ResourceBindingNegotiant(
					hostName, sessionManager);
			IStreamNegotiant sessionEstablishment = new SessionEstablishmentNegotiant(
					router, sessionManager, eventMessageChannel, sessionListenerDelegate);
			
			resourceBinding.setNext(sessionEstablishment);
			sasl.setNext(resourceBinding);
			ibrAfterTls.setNext(sasl);
			tls.setNext(ibrAfterTls);
			ibrBeforeTls.setNext(tls);
			initialStream.setNext(ibrBeforeTls);
			
			return initialStream;
		}
		
	}
	
	private static class IbrSupportedInitialStreamNegotiant extends InitialStreamNegotiant {
		static {
			oxmFactory.register(Register.class, new RegisterTranslatorFactory());
		}
		
		public IbrSupportedInitialStreamNegotiant(String domainName, List<Feature> features) {
			super(domainName, features);
			features.add(new Register());
		}
	}
	
	private static class IbrSupportedTlsNegotiant extends TlsNegotiant {
		static {
			oxmFactory.register(Register.class, new RegisterTranslatorFactory());
		}

		public IbrSupportedTlsNegotiant(String domainName, boolean tlsRequired, List<Feature> features) {
			super(domainName, tlsRequired, features);
		}
		
		@Override
		protected Features getAvailableFeatures(IClientConnectionContext context) {
			Features features = super.getAvailableFeatures(context);
			
			if (context.getAttribute(IbrNegotiant.KEY_IBR_REGISTERED) == null) {
				features.getFeatures().add(new Register());
			}
			
			return features;
		}
	}
	
	@Override
	public void setConfiguration(IConfiguration configuration) {
		super.setConfiguration(configuration);
		
		registrarTimeoutCheckInterval = configuration.getInteger(
				CONFIGURATION_KEY_REGISTRAR_TIMEOUT_CHECK_INTERVAL,
				DEFAULT_REGISTRAR_TIMEOUT_CHECK_INTERVAL
		);
	}
}
