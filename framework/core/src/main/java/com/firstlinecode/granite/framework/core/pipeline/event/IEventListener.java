package com.firstlinecode.granite.framework.core.pipeline.event;


public interface IEventListener<T extends IEvent> {
	void process(IEventContext context, T event);
}
