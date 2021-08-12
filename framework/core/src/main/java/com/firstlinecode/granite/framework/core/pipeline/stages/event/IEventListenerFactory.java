package com.firstlinecode.granite.framework.core.pipeline.stages.event;

public interface IEventListenerFactory<E extends IEvent> {
	Class<E> getType();
	IEventListener<E> createListener();
}
