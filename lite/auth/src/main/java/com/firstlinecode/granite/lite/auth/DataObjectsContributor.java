package com.firstlinecode.granite.lite.auth;

import com.firstlinecode.granite.framework.adf.mybatis.DataObjectMapping;
import com.firstlinecode.granite.framework.adf.mybatis.DataObjectsContrubutorAdapter;
import com.firstlinecode.granite.framework.core.auth.Account;

public class DataObjectsContributor extends DataObjectsContrubutorAdapter {

	@Override
	public DataObjectMapping<?>[] getDataObjectMappings() {
		return new DataObjectMapping[] {
				new DataObjectMapping<Account>(D_Account.class)
		};
	}

}
