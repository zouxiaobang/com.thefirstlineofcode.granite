package com.firstlinecode.granite.framework.core.pipeline.stages.routing;

import com.firstlinecode.basalt.oxm.convention.NamingConventionTranslatorFactory;
import com.firstlinecode.basalt.oxm.translating.ITranslator;
import com.firstlinecode.basalt.oxm.translating.ITranslatorFactory;

public class NamingConventionProtocolTranslatorFactory<T> implements IProtocolTranslatorFactory<T> {
	private Class<T> type;
	private ITranslatorFactory<T> translatorFactory;
	
	public NamingConventionProtocolTranslatorFactory(Class<T> type) {
		this.type = type;
		translatorFactory = new NamingConventionTranslatorFactory<T>(type);
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
