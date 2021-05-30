package com.firstlinecode.granite.xeps.ibr;

import com.firstlinecode.basalt.xeps.ibr.IqRegister;
import com.firstlinecode.granite.framework.core.auth.Account;

public interface IRegistrationStrategy {
	IqRegister getRegistrationForm();
	Account convertToAccount(IqRegister iqRegister) throws MalformedRegistrationInfoException;
}
