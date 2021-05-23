package com.firstlinecode.granite.framework.core;

import com.firstlinecode.granite.framework.core.annotations.Component;

@Component("test.component")
public class ServiceImpl implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("I'm ServiceImpl. I'm running.");
	}
	
}
