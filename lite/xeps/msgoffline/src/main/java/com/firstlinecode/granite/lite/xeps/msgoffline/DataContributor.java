package com.firstlinecode.granite.lite.xeps.msgoffline;

import org.pf4j.Extension;

import com.firstlinecode.granite.framework.adf.mybatis.DataContributorAdapter;

@Extension
public class DataContributor extends DataContributorAdapter {

	@Override
	public Class<?>[] getDataObjects() {
		return new Class<?>[] {
			D_OfflineMessage.class
		};
	}
	
	@Override
	protected String[] getMapperFileNames() {
		return new String[] {
				"MsgOfflineMapper.xml"
		};
	}
	
	@Override
	protected String[] getInitScriptFileNames() {
		return new String[] {
				"msgoffline.sql"
		};
	}

}
