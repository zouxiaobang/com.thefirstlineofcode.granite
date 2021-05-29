package com.firstlinecode.granite.framework.core.event;

import org.pf4j.ExtensionPoint;

public interface IEventListenerFactory<E extends IEvent> extends ExtensionPoint {
	Class<E> getType();
	IEventListener<E> createListener();
}
