package com.firstlinecode.granite.xeps.ibr;

import com.firstlinecode.basalt.protocol.core.ProtocolException;
import com.firstlinecode.basalt.protocol.core.stanza.error.Conflict;
import com.firstlinecode.basalt.protocol.core.stanza.error.NotAcceptable;
import com.firstlinecode.granite.framework.core.annotations.Component;
import com.firstlinecode.granite.framework.core.annotations.Dependency;
import com.firstlinecode.granite.framework.core.auth.Account;
import com.firstlinecode.granite.framework.core.auth.IAccountManager;
import com.firstlinecode.basalt.xeps.ibr.IqRegister;

@Component("default.registrar")
public class Registrar implements IRegistrar {
	
	@Dependency("account.manager")
	private IAccountManager accountManager;
	
	@Dependency("registration.strategy")
	private IRegistrationStrategy strategy;
	

	@Override
	public IqRegister getRegistrationForm() {
		return strategy.getRegistrationForm();
	}

	@Override
	public void register(IqRegister iqRegister) {
		Account account;
		try {
			account = strategy.convertToAccount(iqRegister);
		} catch (MalformedRegistrationInfoException e) {
			throw new ProtocolException(new NotAcceptable());
		}
		
		if (accountManager.exists(account.getName()))
			throw new ProtocolException(new Conflict());
		
		accountManager.add(account);
	}
	
	@Override
	public void remove(String username) {
		accountManager.remove(username);
	}
	
}
