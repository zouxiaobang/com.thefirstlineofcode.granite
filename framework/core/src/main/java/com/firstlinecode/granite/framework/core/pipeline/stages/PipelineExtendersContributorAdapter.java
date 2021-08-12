package com.firstlinecode.granite.framework.core.pipeline.stages;

import com.firstlinecode.granite.framework.core.pipeline.stages.event.IEventListenerFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.parsing.IPipelinePreprocessor;
import com.firstlinecode.granite.framework.core.pipeline.stages.parsing.IProtocolParserFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IIqResultProcessor;
import com.firstlinecode.granite.framework.core.pipeline.stages.processing.IXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.stages.routing.IPipelinePostprocessor;
import com.firstlinecode.granite.framework.core.pipeline.stages.routing.IProtocolTranslatorFactory;
import com.firstlinecode.granite.framework.core.session.ISessionListener;

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
	
	@Override
	public ISessionListener[] getSessionListeners() {
		return null;
	}
}
