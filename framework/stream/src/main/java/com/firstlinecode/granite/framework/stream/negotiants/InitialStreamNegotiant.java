package com.firstlinecode.granite.framework.stream.negotiants;

import java.util.List;
import java.util.Locale;

import com.firstlinecode.basalt.protocol.Constants;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stream.Feature;
import com.firstlinecode.basalt.protocol.core.stream.Features;
import com.firstlinecode.basalt.protocol.core.stream.Stream;
import com.firstlinecode.basalt.protocol.core.stream.error.InvalidFrom;
import com.firstlinecode.basalt.protocol.core.stream.error.InvalidNamespace;
import com.firstlinecode.basalt.protocol.core.stream.error.NotAuthorized;
import com.firstlinecode.basalt.protocol.core.stream.error.ResourceConstraint;
import com.firstlinecode.basalt.protocol.core.stream.error.UnsupportedVersion;
import com.firstlinecode.basalt.protocol.core.stream.sasl.Mechanisms;
import com.firstlinecode.basalt.protocol.core.stream.tls.StartTls;
import com.firstlinecode.basalt.oxm.IOxmFactory;
import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.basalt.oxm.translators.core.stream.FeaturesTranslatorFactory;
import com.firstlinecode.basalt.oxm.translators.core.stream.sasl.MechanismsTranslatorFactory;
import com.firstlinecode.basalt.oxm.translators.core.stream.tls.StartTlsTranslatorFactory;
import com.firstlinecode.granite.framework.core.connection.IClientConnectionContext;
import com.firstlinecode.granite.framework.core.integration.IMessage;
import com.firstlinecode.granite.framework.stream.StreamConstants;

public class InitialStreamNegotiant extends AbstractNegotiant {
	protected static IOxmFactory oxmFactory = OxmService.createStreamOxmFactory();
	
	protected String domainName;
	protected List<Feature> features;
	
	static {
		oxmFactory.register(Features.class, new FeaturesTranslatorFactory());
		oxmFactory.register(StartTls.class, new StartTlsTranslatorFactory());
		oxmFactory.register(Mechanisms.class, new MechanismsTranslatorFactory());
	}
	
	public InitialStreamNegotiant(String domainName, List<Feature> features) {
		this.domainName = domainName;
		this.features = features;
	}

	@Override
	protected boolean doNegotiate(IClientConnectionContext context, IMessage message) {
		Object obj = oxmFactory.parse((String)message.getPayload(), true);
		
		if (obj instanceof Stream) {
			Stream initialStream = (Stream)obj;
			
			Stream openStream = new Stream();
			// (rfc3290 4.7.1)
			// should provide the server's authoritative hostname
			openStream.setFrom(JabberId.parse(domainName));
			
			openStream.setDefaultNamespace(getDefaultNamespace());
			openStream.setLang(initialStream.getLang());
			openStream.setVersion(Constants.SPECIFICATION_VERSION);
			
			// (rfc3290 4.7.1)
            // must response a opening stream
			context.write(oxmFactory.translate(openStream), true);
			
			if (!Constants.SPECIFICATION_VERSION.equals(initialStream.getVersion())) {
				throw new ProtocolException(new UnsupportedVersion(String.format("Unsupported specification version: %s.",
						Constants.SPECIFICATION_VERSION)));
			}
			
			if (initialStream.getTo() != null) {
				if (!initialStream.getTo().toString().equals(domainName)) {
					throw new ProtocolException(new InvalidFrom(String.format("'to' attribute of 'stream' must be '%s'.", domainName)));
				}
			}
			
			if (!getDefaultNamespace().equals(initialStream.getDefaultNamespace())) {
				throw new ProtocolException(new InvalidNamespace(String.format("Only '%s' namespace supported.", getDefaultNamespace())));
			}
			
			if (initialStream.getLang() != null) {
				boolean available = false;
				for (Locale locale : Locale.getAvailableLocales()) {
					if (locale.toString().equals(initialStream.getLang())) {
						available = true;
						break;
					}
				}
				
				if (!available) {
					throw new ProtocolException(new ResourceConstraint(String.format(
							"Unspported lang: %s.", initialStream.getLang())));
				}
				
				context.setAttribute(StreamConstants.KEY_STREAM_LANG, initialStream.getLang());
			}
			
			Features availableFeatures = getAvailableFeatures(context);
			
			context.write(oxmFactory.translate(availableFeatures));
			
			return true;
		} else {
			throw new ProtocolException(new NotAuthorized());
		}
	}

	protected Features getAvailableFeatures(IClientConnectionContext context) {
		Features availableFeatures = new Features();
		availableFeatures.setFeatures(features);
		
		return availableFeatures;
	}
	
	protected String getDefaultNamespace() {
		return Constants.C2S_DEFAULT_NAMESPACE;
	}

}
