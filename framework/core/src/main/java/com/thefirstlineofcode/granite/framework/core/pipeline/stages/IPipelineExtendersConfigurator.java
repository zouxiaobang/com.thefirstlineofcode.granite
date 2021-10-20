package com.thefirstlineofcode.granite.framework.core.pipeline.stages;

import com.thefirstlineofcode.basalt.protocol.core.ProtocolChain;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEvent;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListener;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListenerFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.IPipesPreprocessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.IProtocolParserFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IIqResultProcessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.IPipesPostprocessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.IProtocolTranslatorFactory;
import com.thefirstlineofcode.granite.framework.core.session.ISessionListener;

public interface IPipelineExtendersConfigurator {
	IPipelineExtendersConfigurator registerNamingConventionParser(ProtocolChain protocolChain, Class<?> protocolObjectType);
	IPipelineExtendersConfigurator registerParserFactory(IProtocolParserFactory<?> parserFactory);
	
	IPipelineExtendersConfigurator registerSingletonXepProcessor(ProtocolChain protocolChain, IXepProcessor<?, ?> xepProcessor);
	IPipelineExtendersConfigurator registerXepProcessorFactory(IXepProcessorFactory<?, ?> xepProcessorFactory);
	
	IPipelineExtendersConfigurator registerNamingConventionTranslator(Class<?> protocolObjectType);
	IPipelineExtendersConfigurator registerTranslatorFactory(IProtocolTranslatorFactory<?> translatorFactory);

	IPipelineExtendersConfigurator registerPipesPreprocessors(IPipesPreprocessor preprocessor);
	IPipelineExtendersConfigurator registerPipesPostprocessors(IPipesPostprocessor postprocessor);
		
	<E extends IEvent> IPipelineExtendersConfigurator registerEventListener(Class<E> eventType, IEventListener<E> listener);
	IPipelineExtendersConfigurator registerEventListenerFactory(IEventListenerFactory<?> listenerFactory);
	
	IPipelineExtendersConfigurator registerIqResultProcessor(IIqResultProcessor iqResultProcessor);
	IPipelineExtendersConfigurator registerSessionListener(ISessionListener sessionListener);
}
