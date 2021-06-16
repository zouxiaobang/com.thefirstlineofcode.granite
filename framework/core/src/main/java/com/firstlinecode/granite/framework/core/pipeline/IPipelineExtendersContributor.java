package com.firstlinecode.granite.framework.core.pipeline;

import org.pf4j.ExtensionPoint;

import com.firstlinecode.granite.framework.core.event.IEventListenerFactory;
import com.firstlinecode.granite.framework.core.pipeline.parsing.IPipelinePreprocessor;
import com.firstlinecode.granite.framework.core.pipeline.parsing.IProtocolParserFactory;
import com.firstlinecode.granite.framework.core.pipeline.processing.IXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.routing.IPipelinePostprocessor;
import com.firstlinecode.granite.framework.core.pipeline.routing.IProtocolTranslatorFactory;

public interface IPipelineExtendersContributor extends ExtensionPoint {
	IProtocolParserFactory<?>[] getProtocolParserFactories();
	IXepProcessorFactory<?, ?>[] getXepProcessorFactories();
	IProtocolTranslatorFactory<?>[] getProtocolTranslatorFactories();
	IPipelinePreprocessor[] getPipesPreprocessors();
	IPipelinePostprocessor[] getPipesPostprocessors();
	IEventListenerFactory<?>[] getEventListenerFactories();
}
