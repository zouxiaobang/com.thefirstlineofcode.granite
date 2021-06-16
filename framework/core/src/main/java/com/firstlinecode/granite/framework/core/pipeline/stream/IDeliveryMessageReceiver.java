package com.firstlinecode.granite.framework.core.pipeline.stream;

import com.firstlinecode.granite.framework.core.connection.IConnectionManagerAware;
import com.firstlinecode.granite.framework.core.pipeline.IMessageReceiver;

public interface IDeliveryMessageReceiver extends IMessageReceiver, IConnectionManagerAware {}
