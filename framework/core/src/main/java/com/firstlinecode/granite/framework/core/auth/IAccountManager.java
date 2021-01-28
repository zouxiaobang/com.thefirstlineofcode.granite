package com.firstlinecode.granite.framework.core.auth;

public interface IAccountManager {
	void add(Account account);
	void remove(String name);
	boolean exists(String name);
	Account get(String name);
}
