package com.thefirstlineofcode.granite.framework.core.pipeline.stages;

import com.thefirstlineofcode.basalt.protocol.core.ProtocolChain;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListenerFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.IPipelinePreprocessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.IProtocolParserFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.NamingConventionProtocolParserFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IIqResultProcessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.IPipelinePostprocessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.IProtocolTranslatorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.NamingConventionProtocolTranslatorFactory;
import com.thefirstlineofcode.granite.framework.core.session.ISessionListener;

public class PipelineExtendersContributorAdapter implements IPipelineExtendersContributor {

	@Override
	public IProtocolParserFactory<?>[] getProtocolParserFactories() {		
		IProtocolParserFactory<?>[] namingConventionParserFactories = getNamingConventionParserFactories();
		IProtocolParserFactory<?>[] customizedParserFactories = getCustomizedParserFactories();
		
		return merge(namingConventionParserFactories, customizedParserFactories);
	}

	protected IProtocolParserFactory<?>[] merge(IProtocolParserFactory<?>[] namingConventionParserFactories,
			IProtocolParserFactory<?>[] customizedParserFactories) {
		if (isEmpty(namingConventionParserFactories) && isEmpty(customizedParserFactories)) {
			return null;
		}
		
		if (isEmpty(customizedParserFactories))
			return namingConventionParserFactories;
		
		if (isEmpty(namingConventionParserFactories))
			return customizedParserFactories;
		
		int length = namingConventionParserFactories.length + customizedParserFactories.length;
		IProtocolParserFactory<?>[] allParserFactories = new IProtocolParserFactory<?>[length];
		
		for (int i = 0; i < namingConventionParserFactories.length; i++) {
			allParserFactories[i] = namingConventionParserFactories[i];
		}
		
		for (int i = 0; i < customizedParserFactories.length; i++) {
			allParserFactories[namingConventionParserFactories.length + i] = customizedParserFactories[i];
		}
		
		return allParserFactories;
	}
	
	protected IProtocolTranslatorFactory<?>[] merge(IProtocolTranslatorFactory<?>[] namingConventionTranslatorFactories,
			IProtocolTranslatorFactory<?>[] customizedTranslatorFactories) {
		if (isEmpty(namingConventionTranslatorFactories) && isEmpty(customizedTranslatorFactories)) {
			return null;
		}
		
		if (isEmpty(customizedTranslatorFactories))
			return namingConventionTranslatorFactories;
		
		if (isEmpty(namingConventionTranslatorFactories))
			return customizedTranslatorFactories;
		
		int length = namingConventionTranslatorFactories.length + customizedTranslatorFactories.length;
		IProtocolTranslatorFactory<?>[] allParserFactories = new IProtocolTranslatorFactory<?>[length];
		
		for (int i = 0; i < namingConventionTranslatorFactories.length; i++) {
			allParserFactories[i] = namingConventionTranslatorFactories[i];
		}
		
		for (int i = 0; i < customizedTranslatorFactories.length; i++) {
			allParserFactories[namingConventionTranslatorFactories.length + i] = customizedTranslatorFactories[i];
		}
		
		return allParserFactories;
	}

	private <T> boolean isEmpty(T[] factories) {
		return factories == null || factories.length == 0;
	}

	protected IProtocolParserFactory<?>[] getNamingConventionParserFactories() {
		NamingConventionParsableProtocolObject[] namingConventionParsableProtocolObjects =
				getNamingConventionParsableProtocolObjects();
		if (namingConventionParsableProtocolObjects == null || namingConventionParsableProtocolObjects.length == 0)
			return null;
		
		IProtocolParserFactory<?>[] namingConventionParserFactories = new IProtocolParserFactory<?>[namingConventionParsableProtocolObjects.length];
		for (int i = 0; i < namingConventionParsableProtocolObjects.length; i++) {
			namingConventionParserFactories[i] = new NamingConventionProtocolParserFactory<>(
					namingConventionParsableProtocolObjects[i].protocolChain, namingConventionParsableProtocolObjects[i].protocolObjectClass);
		}
		
		return namingConventionParserFactories;
	}
	
	protected IProtocolParserFactory<?>[] getCustomizedParserFactories() {
		return null;
	}

	protected NamingConventionParsableProtocolObject[] getNamingConventionParsableProtocolObjects() {
		return null;
	}

	@Override
	public IXepProcessorFactory<?, ?>[] getXepProcessorFactories() {
		return null;
	}

	@Override
	public IProtocolTranslatorFactory<?>[] getProtocolTranslatorFactories() {
		IProtocolTranslatorFactory<?>[] namingConventionTranslatorFactories = getNamingConventionTranslatorFactories();
		IProtocolTranslatorFactory<?>[] customizedTranslatorFactories = getCustomizedTranslatorFactories();
		
		return merge(namingConventionTranslatorFactories, customizedTranslatorFactories);
	}

	protected IProtocolTranslatorFactory<?>[] getCustomizedTranslatorFactories() {
		return null;
	}

	private IProtocolTranslatorFactory<?>[] getNamingConventionTranslatorFactories() {
		Class<?>[] namingConventionTranslatableProtocolObjects = getNamingConventionTranslatableProtocolObjects();
		if (namingConventionTranslatableProtocolObjects == null || namingConventionTranslatableProtocolObjects.length == 0)
			return null;
		
		IProtocolTranslatorFactory<?>[] namingConventionTranslatorFactories = new IProtocolTranslatorFactory<?>[namingConventionTranslatableProtocolObjects.length];
		for (int i = 0; i < namingConventionTranslatableProtocolObjects.length; i++) {
			namingConventionTranslatorFactories[i] = new NamingConventionProtocolTranslatorFactory<>(namingConventionTranslatableProtocolObjects[i]);
		}
		
		return namingConventionTranslatorFactories;
	}
	
	protected Class<?>[] getNamingConventionTranslatableProtocolObjects() {
		return null;
	}

	@Override
	public IPipelinePreprocessor[] getPipelinePreprocessors() {
		return null;
	}

	@Override
	public IPipelinePostprocessor[] getPipelinePostprocessors() {
		return null;
	}

	@Override
	public IEventListenerFactory<?>[] getEventListenerFactories() {
		return null;
	}

	@Override
	public IIqResultProcessor[] getIqResultProcessors() {
		return null;
	}
	
	@Override
	public ISessionListener[] getSessionListeners() {
		return null;
	}
	
	protected class NamingConventionParsableProtocolObject {
		public ProtocolChain protocolChain;
		public Class<?> protocolObjectClass;
		
		public NamingConventionParsableProtocolObject(ProtocolChain protocolChain, Class<?> protocolObjectClass) {
			this.protocolChain = protocolChain;
			this.protocolObjectClass = protocolObjectClass;
		}
	}
}
