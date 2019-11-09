package com.firstlinecode.granite.lite.leps.im.traceable;

import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.granite.framework.core.config.IConfiguration;
import com.firstlinecode.granite.framework.core.config.IConfigurationAware;
import com.firstlinecode.granite.framework.core.supports.data.IDataObjectFactory;
import com.firstlinecode.granite.framework.core.supports.data.IDataObjectFactoryAware;
import com.firstlinecode.granite.framework.supports.mybatis.DataObjectIterator;
import com.firstlinecode.granite.leps.im.traceable.ITraceableMessageStore;
import com.firstlinecode.granite.leps.im.traceable.TraceableMessage;

@Transactional
@Component
public class TraceableMessageStore implements ITraceableMessageStore, IDataObjectFactoryAware,
			IConfigurationAware {
	private static final String CONFIGURATION_KEY_TRACEABLE_MESSAGES_FETCH_SIZE = "traceable.messages.fetch.size";
	private static final int DEFAULT_FETCH_SIZE = 20;
	
	private IDataObjectFactory dataObjectFactory;
	private int traceableMessagesFetchSize = DEFAULT_FETCH_SIZE;
	
	@Autowired
	private SqlSession sqlSession;

	@Override
	public void save(JabberId jid, String messageId, String message) {
		TraceableMessage traceableMessage = dataObjectFactory.create(TraceableMessage.class);
		traceableMessage.setMessageId(messageId);
		traceableMessage.setMessage(message);
		traceableMessage.setJid(jid);
		
		getMapper().insert(traceableMessage);
	}

	@Override
	public TraceableMessage get(JabberId jid, String messageId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(JabberId jid, String messageId) {
		getMapper().deleteByJidAndMessageId(jid, messageId);
	}

	@Override
	public Iterator<TraceableMessage> iterator(final JabberId jid) {
		return new DataObjectIterator<TraceableMessage>(traceableMessagesFetchSize) {
			@Override
			protected List<TraceableMessage> doFetch(int offset, int limit) {
				return getMapper().selectByJid(jid, limit, offset);
			}
		};
	}

	@Override
	public boolean isEmpty(JabberId jid) {
		return getMapper().selectCountByJid(jid) == 0;
	}

	@Override
	public int getSize(JabberId jid) {
		return getMapper().selectCountByJid(jid);
	}
	
	private TraceableMessageMapper getMapper() {
		return sqlSession.getMapper(TraceableMessageMapper.class);
	}

	@Override
	public void setDataObjectFactory(IDataObjectFactory dataObjectFactory) {
		this.dataObjectFactory = dataObjectFactory;
	}

	@Override
	public void setConfiguration(IConfiguration configuration) {
		traceableMessagesFetchSize = configuration.getInteger(CONFIGURATION_KEY_TRACEABLE_MESSAGES_FETCH_SIZE,
				DEFAULT_FETCH_SIZE);
	}
}
