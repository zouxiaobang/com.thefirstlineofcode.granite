package com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing;

import org.pf4j.ExtensionPoint;

import com.thefirstlineofcode.basalt.oxm.translating.ITranslator;

public interface IProtocolTranslatorFactory<T> extends ExtensionPoint {
	Class<T> getType();
	ITranslator<T> createTranslator();
}
