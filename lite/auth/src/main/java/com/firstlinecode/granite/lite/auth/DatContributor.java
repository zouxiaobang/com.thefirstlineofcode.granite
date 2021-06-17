package com.firstlinecode.granite.lite.auth;

import org.pf4j.Extension;

import com.firstlinecode.granite.framework.adf.mybatis.DataContrubutorAdapter;
import com.firstlinecode.granite.framework.adf.mybatis.DataObjectMapping;
import com.firstlinecode.granite.framework.core.auth.Account;

@Extension
public class DatContributor extends DataContrubutorAdapter {

	@Override
	public DataObjectMapping<?>[] getDataObjectMappings() {
		return new DataObjectMapping[] {
				new DataObjectMapping<Account>(D_Account.class)
		};
	}
	
	@Override
	protected String[] getMapperFileNames() {
		return new String[] {
			"AccountMapper.xml"
		};
	}

}
