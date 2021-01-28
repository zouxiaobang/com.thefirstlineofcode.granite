package com.firstlinecode.granite.xeps.ibr;

import com.firstlinecode.granite.framework.core.auth.Account;
import com.firstlinecode.basalt.xeps.ibr.IqRegister;

public interface IRegistrationStrategy {
	IqRegister getRegistrationForm();
	Account convertToAccount(IqRegister iqRegister) throws MalformedRegistrationInfoException;
}
