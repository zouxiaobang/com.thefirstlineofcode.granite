package com.firstlinecode.granite.framework.core.adf.injection;

import java.lang.reflect.Method;

import com.firstlinecode.granite.framework.core.utils.CommonsUtils;

public class MethodDependencyInjector extends AbstractDependencyInjector {
	private Method method;
	
	public MethodDependencyInjector(Method method, IDependencyFetcher fetcher) {
		super(fetcher);
		this.method = method;
	}

	@Override
	protected void doInject(Object object, Object dependency) {
		if (CommonsUtils.isSetterMethod(method)) {
			try {
				method.invoke(object, new Object[] {dependency});
			} catch (Exception e) {
				throw new RuntimeException("Can't inject dependency by method.", e);
			}
		}
		
	}
}
