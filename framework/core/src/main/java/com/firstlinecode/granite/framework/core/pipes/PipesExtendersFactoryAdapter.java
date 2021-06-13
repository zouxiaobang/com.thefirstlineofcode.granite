package com.firstlinecode.granite.framework.core.pipes;

import com.firstlinecode.granite.framework.core.event.IEventListenerFactory;
import com.firstlinecode.granite.framework.core.pipes.parsing.IPipesPreprocessor;
import com.firstlinecode.granite.framework.core.pipes.parsing.IProtocolParserFactory;
import com.firstlinecode.granite.framework.core.pipes.processing.IXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipes.routing.IPipesPostprocessor;
import com.firstlinecode.granite.framework.core.pipes.routing.IProtocolTranslatorFactory;

public class PipesExtendersFactoryAdapter implements IPipesExtendersFactory {

	@Override
	public IProtocolParserFactory<?>[] getProtocolParserFactories() {
		return null;
	}

	@Override
	public IXepProcessorFactory<?, ?>[] getXepProcessorFactories() {
		return null;
	}

	@Override
	public IProtocolTranslatorFactory<?>[] getProtocolTranslatorFactories() {
		return null;
	}

	@Override
	public IPipesPreprocessor[] getPipesPreprocessors() {
		return null;
	}

	@Override
	public IPipesPostprocessor[] getPipesPostprocessors() {
		return null;
	}

	@Override
	public IEventListenerFactory<?>[] getEventListenerFactories() {
		return null;
	}

}
