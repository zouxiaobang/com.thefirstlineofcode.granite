package com.firstlinecode.granite.framework.core.pipeline.stages.stream;

import com.firstlinecode.granite.framework.core.connection.IConnectionManagerAware;
import com.firstlinecode.granite.framework.core.pipeline.IMessageReceiver;

public interface IDeliveryMessageReceiver extends IMessageReceiver, IConnectionManagerAware {}
