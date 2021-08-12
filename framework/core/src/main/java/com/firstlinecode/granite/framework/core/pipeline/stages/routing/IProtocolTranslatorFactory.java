package com.firstlinecode.granite.framework.core.pipeline.stages.routing;

import org.pf4j.ExtensionPoint;

import com.firstlinecode.basalt.oxm.translating.ITranslator;

public interface IProtocolTranslatorFactory<T> extends ExtensionPoint {
	Class<T> getType();
	ITranslator<T> createTranslator();
}
