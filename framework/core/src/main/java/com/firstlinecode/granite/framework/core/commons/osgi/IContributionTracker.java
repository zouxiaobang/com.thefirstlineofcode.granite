package com.firstlinecode.granite.framework.core.commons.osgi;

import org.osgi.framework.Bundle;

public interface IContributionTracker {
	void found(Bundle bundle, String contribution) throws Exception;
    void lost(Bundle bundle, String contribution) throws Exception;

}
