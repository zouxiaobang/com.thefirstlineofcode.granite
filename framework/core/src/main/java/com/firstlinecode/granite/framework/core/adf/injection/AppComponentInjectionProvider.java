package com.firstlinecode.granite.framework.core.adf.injection;

import java.lang.annotation.Annotation;

import com.firstlinecode.granite.framework.core.adf.IApplicationComponentService;
import com.firstlinecode.granite.framework.core.annotations.Dependency;

public class AppComponentInjectionProvider implements IInjectionProvider {
	private IApplicationComponentService appComponentService;
	
	public AppComponentInjectionProvider(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
	}

	@Override
	public Class<? extends Annotation> getAnnotationType() {
		return Dependency.class;
	}

	@Override
	public IDependencyFetcher getFetcher(Object mark) {
		return new AppComponentFetcher(appComponentService, (String)mark);
	}

	@Override
	public Object getMark(Object source, Object dependencyAnnotation) {
		Dependency dependency = (Dependency)dependencyAnnotation;
		
		return dependency.value();
	}

}
