package com.firstlinecode.granite.framework.core.pipes;

import org.pf4j.ExtensionPoint;

import com.firstlinecode.granite.framework.core.event.IEventListenerFactory;
import com.firstlinecode.granite.framework.core.pipes.parsing.IPipesPreprocessor;
import com.firstlinecode.granite.framework.core.pipes.parsing.IProtocolParserFactory;
import com.firstlinecode.granite.framework.core.pipes.processing.IXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipes.routing.IPipesPostprocessor;
import com.firstlinecode.granite.framework.core.pipes.routing.IProtocolTranslatorFactory;

public interface IPipesExtendersContributor extends ExtensionPoint {
	IProtocolParserFactory<?>[] getProtocolParserFactories();
	IXepProcessorFactory<?, ?>[] getXepProcessorFactories();
	IProtocolTranslatorFactory<?>[] getProtocolTranslatorFactories();
	IPipesPreprocessor[] getPipesPreprocessors();
	IPipesPostprocessor[] getPipesPostprocessors();
	IEventListenerFactory<?>[] getEventListenerFactories();
}
