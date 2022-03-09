package com.thefirstlineofcode.granite.framework.core.pipeline.stages;

import java.util.ArrayList;
import java.util.List;

import com.thefirstlineofcode.basalt.protocol.core.ProtocolChain;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.EventListenerFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEvent;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListener;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListenerFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.IPipesPreprocessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.IProtocolParserFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.NamingConventionProtocolParserFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IIqResultProcessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.SingletonXepProcessorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.IPipesPostprocessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.IProtocolTranslatorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.NamingConventionProtocolTranslatorFactory;
import com.thefirstlineofcode.granite.framework.core.session.ISessionListener;

public abstract class PipelineExtendersConfigurator implements IPipelineExtendersContributor, IPipelineExtendersConfigurator {
	protected List<IProtocolParserFactory<?>> parserFactories;
	protected List<IXepProcessorFactory<?, ?>> xepProcessorFactories;
	protected List<IProtocolTranslatorFactory<?>> translatorFactories;
	
	protected List<IPipesPreprocessor> pipesPreprocessors;
	protected List<IPipesPostprocessor> pipesPostprocessors;
	
	protected List<IEventListenerFactory<?>> eventListenerFactories;
	
	protected List<IIqResultProcessor> iqResultProcessors;
	protected List<ISessionListener> sessionListeners;
	
	
	public PipelineExtendersConfigurator() {
		parserFactories = new ArrayList<>();
		xepProcessorFactories = new ArrayList<>();
		translatorFactories = new ArrayList<>();
		
		pipesPreprocessors = new ArrayList<>();
		pipesPostprocessors = new ArrayList<>();
		
		eventListenerFactories = new ArrayList<>();
		
		iqResultProcessors = new ArrayList<>();
		sessionListeners = new ArrayList<>();
		
		configure(this);
	}
	
	protected abstract void configure(IPipelineExtendersConfigurator configurator);

	@Override
	public IPipelineExtendersConfigurator registerNamingConventionParser(ProtocolChain protocolChain,
			Class<?> protocolObjectType) {
		return registerParserFactory(new NamingConventionProtocolParserFactory<>(protocolChain, protocolObjectType));
	}

	@Override
	public IPipelineExtendersConfigurator registerParserFactory(IProtocolParserFactory<?> parserFactory) {
		parserFactories.add(parserFactory);
		
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public IPipelineExtendersConfigurator registerSingletonXepProcessor(ProtocolChain protocolChain,
			IXepProcessor<?, ?> xepProcessor) {
		return registerXepProcessorFactory(new SingletonXepProcessorFactory(protocolChain, xepProcessor));
	}

	@Override
	public IPipelineExtendersConfigurator registerXepProcessorFactory(IXepProcessorFactory<?, ?> xepProcessorFactory) {
		xepProcessorFactories.add(xepProcessorFactory);
		
		return this;
	}

	@Override
	public IPipelineExtendersConfigurator registerNamingConventionTranslator(Class<?> protocolObjectType) {
		return registerTranslatorFactory(new NamingConventionProtocolTranslatorFactory<>(protocolObjectType));
	}

	@Override
	public IPipelineExtendersConfigurator registerTranslatorFactory(IProtocolTranslatorFactory<?> translatorFactory) {
		translatorFactories.add(translatorFactory);
		
		return this;
	}

	@Override
	public IPipelineExtendersConfigurator registerPipesPreprocessors(IPipesPreprocessor preprocessor) {
		pipesPreprocessors.add(preprocessor);
		
		return this;
	}

	@Override
	public IPipelineExtendersConfigurator registerPipesPostprocessors(IPipesPostprocessor postprocessor) {
		pipesPostprocessors.add(postprocessor);
		
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <E extends IEvent> IPipelineExtendersConfigurator registerEventListener(Class<E> type,
			IEventListener<E> listener) {
		eventListenerFactories.add(new EventListenerFactory(type, listener));
		
		return this;
	}
	
	@Override
	public IPipelineExtendersConfigurator registerEventListenerFactory(IEventListenerFactory<?> listenerFactory) {
		eventListenerFactories.add(listenerFactory);
		return this;
	}

	@Override
	public IPipelineExtendersConfigurator registerIqResultProcessor(IIqResultProcessor iqResultProcessor) {
		iqResultProcessors.add(iqResultProcessor);
		return this;
	}

	@Override
	public IPipelineExtendersConfigurator registerSessionListener(ISessionListener sessionListener) {
		sessionListeners.add(sessionListener);
		return this;
	}
	
	@Override
	public IProtocolParserFactory<?>[] getProtocolParserFactories() {
		if (parserFactories.size() == 0)
			return null;
		
		return parserFactories.toArray(new IProtocolParserFactory<?>[parserFactories.size()]);
	}

	@Override
	public IXepProcessorFactory<?, ?>[] getXepProcessorFactories() {
		if (xepProcessorFactories.size() == 0)
			return null;
		
		return xepProcessorFactories.toArray(new IXepProcessorFactory<?, ?>[xepProcessorFactories.size()]);
	}

	@Override
	public IProtocolTranslatorFactory<?>[] getProtocolTranslatorFactories() {
		if (translatorFactories.size() == 0)
			return null;
		
		return translatorFactories.toArray(new IProtocolTranslatorFactory<?>[translatorFactories.size()]);
	}

	@Override
	public IPipesPreprocessor[] getPipesPreprocessors() {
		if (pipesPreprocessors.size() == 0)
			return null;
		
		return pipesPreprocessors.toArray(new IPipesPreprocessor[pipesPreprocessors.size()]);
	}

	@Override
	public IPipesPostprocessor[] getPipesPostprocessors() {
		if (pipesPostprocessors.size() == 0)
			return null;
		
		return pipesPostprocessors.toArray(new IPipesPostprocessor[pipesPostprocessors.size()]);
	}

	@Override
	public IEventListenerFactory<?>[] getEventListenerFactories() {
		if (eventListenerFactories.size() == 0)
			return null;
		
		return eventListenerFactories.toArray(new IEventListenerFactory<?>[eventListenerFactories.size()]);
	}

	@Override
	public IIqResultProcessor[] getIqResultProcessors() {
		if (iqResultProcessors.size() == 0)
			return null;
		
		return iqResultProcessors.toArray(new IIqResultProcessor[iqResultProcessors.size()]);
	}

	@Override
	public ISessionListener[] getSessionListeners() {
		if (sessionListeners.size() == 0)
			return null;
		
		return sessionListeners.toArray(new ISessionListener[sessionListeners.size()]);
	}
}
