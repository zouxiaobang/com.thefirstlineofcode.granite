package com.firstlinecode.granite.lite.auth;

import org.pf4j.Extension;

import com.firstlinecode.granite.framework.adf.mybatis.DataContributorAdapter;

@Extension
public class DataContributor extends DataContributorAdapter {

	@Override
	public Class<?>[] getDataObjects() {
		return new Class<?>[] {
			D_Account.class
		};
	}
	
	@Override
	protected String[] getMapperFileNames() {
		return new String[] {
				"AccountMapper.xml"
		};
	}
	
	@Override
	protected String[] getInitScriptFileNames() {
		return new String[] {
				"auth.sql"
		};
	}

}
