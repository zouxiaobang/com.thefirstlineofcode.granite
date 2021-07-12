package com.firstlinecode.granite.framework.core.pipeline.routing;

import com.firstlinecode.basalt.oxm.translating.ITranslator;
import com.firstlinecode.basalt.oxm.translating.ITranslatorFactory;

public class ProtocolTranslatorFactory<T> implements IProtocolTranslatorFactory<T> {
	private Class<T> type;
	private ITranslatorFactory<T> translatorFactory;
	
	public ProtocolTranslatorFactory(Class<T> type, ITranslatorFactory<T> translatorFactory) {
		this.type = type;
		this.translatorFactory = translatorFactory;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public ITranslator<T> createTranslator() {
		return translatorFactory.create();
	}

}
