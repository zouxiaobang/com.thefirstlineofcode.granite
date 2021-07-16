package com.firstlinecode.granite.framework.core.pipeline;

import com.firstlinecode.granite.framework.core.pipeline.event.IEventListenerFactory;
import com.firstlinecode.granite.framework.core.pipeline.parsing.IPipelinePreprocessor;
import com.firstlinecode.granite.framework.core.pipeline.parsing.IProtocolParserFactory;
import com.firstlinecode.granite.framework.core.pipeline.processing.IIqResultProcessor;
import com.firstlinecode.granite.framework.core.pipeline.processing.IXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.routing.IPipelinePostprocessor;
import com.firstlinecode.granite.framework.core.pipeline.routing.IProtocolTranslatorFactory;

public class PipelineExtendersContributorAdapter implements IPipelineExtendersContributor {

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
	public IPipelinePreprocessor[] getPipesPreprocessors() {
		return null;
	}

	@Override
	public IPipelinePostprocessor[] getPipesPostprocessors() {
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
}
