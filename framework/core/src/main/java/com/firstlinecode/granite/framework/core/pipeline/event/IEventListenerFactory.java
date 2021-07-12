package com.firstlinecode.granite.framework.core.pipeline.event;

public interface IEventListenerFactory<E extends IEvent> {
	Class<E> getType();
	IEventListener<E> createListener();
}
