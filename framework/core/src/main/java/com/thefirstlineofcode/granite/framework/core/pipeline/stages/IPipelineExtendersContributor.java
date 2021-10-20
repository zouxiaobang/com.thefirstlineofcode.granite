package com.thefirstlineofcode.granite.framework.core.pipeline.stages;

import org.pf4j.ExtensionPoint;

import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListenerFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.IPipesPreprocessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.IProtocolParserFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IIqResultProcessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.IPipesPostprocessor;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.IProtocolTranslatorFactory;
import com.thefirstlineofcode.granite.framework.core.session.ISessionListener;

public interface IPipelineExtendersContributor extends ExtensionPoint {
	IProtocolParserFactory<?>[] getProtocolParserFactories();
	IXepProcessorFactory<?, ?>[] getXepProcessorFactories();
	IProtocolTranslatorFactory<?>[] getProtocolTranslatorFactories();
	IPipesPreprocessor[] getPipesPreprocessors();
	IPipesPostprocessor[] getPipesPostprocessors();
	IEventListenerFactory<?>[] getEventListenerFactories();
	IIqResultProcessor[] getIqResultProcessors();
	ISessionListener[] getSessionListeners();
}
