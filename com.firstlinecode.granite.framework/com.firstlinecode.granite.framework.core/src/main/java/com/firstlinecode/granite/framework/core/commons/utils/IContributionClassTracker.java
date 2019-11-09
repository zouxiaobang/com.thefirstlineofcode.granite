package com.firstlinecode.granite.framework.core.commons.utils;

import java.lang.annotation.Annotation;

public interface IContributionClassTracker<T extends Annotation> {
	void found(ContributionClass<T> contributionClass);
	void lost(ContributionClass<T> contributionClass);
}
