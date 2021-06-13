package com.firstlinecode.granite.framework.core.event;

public interface IEventListenerFactory<E extends IEvent> {
	Class<E> getType();
	IEventListener<E> createListener();
}
