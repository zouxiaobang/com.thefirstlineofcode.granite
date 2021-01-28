package com.firstlinecode.granite.framework.core.connection;

import com.firstlinecode.granite.framework.core.session.ISession;

public interface IConnectionContext extends ISession {
	void write(Object message);
	void close();
}
