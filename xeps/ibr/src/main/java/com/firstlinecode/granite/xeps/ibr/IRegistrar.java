package com.firstlinecode.granite.xeps.ibr;

import com.firstlinecode.basalt.xeps.ibr.IqRegister;

public interface IRegistrar {
	IqRegister getRegistrationForm();
	void register(IqRegister iqRegister);
	void remove(String username);
}
