package com.firstlinecode.granite.framework.core.pipeline.stages;

import org.pf4j.ExtensionPoint;

import com.firstlinecode.granite.framework.core.pipeline.stages.event.IEventListenerFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.parsing.IPipelinePreprocessor;
import com.firstlinecode.granite.framework.core.pipeline.stages.parsing.IProtocolParserFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IIqResultProcessor;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.routing.IPipelinePostprocessor;
import com.firstlinecode.granite.framework.core.pipeline.stages.routing.IProtocolTranslatorFactory;
import com.firstlinecode.granite.framework.core.session.ISessionListener;

public interface IPipelineExtendersContributor extends ExtensionPoint {
	IProtocolParserFactory<?>[] getProtocolParserFactories();
	IXepProcessorFactory<?, ?>[] getXepProcessorFactories();
	IProtocolTranslatorFactory<?>[] getProtocolTranslatorFactories();
	IPipelinePreprocessor[] getPipesPreprocessors();
	IPipelinePostprocessor[] getPipesPostprocessors();
	IEventListenerFactory<?>[] getEventListenerFactories();
	IIqResultProcessor[] getIqResultProcessors();
	ISessionListener[] getSessionListeners();
}
