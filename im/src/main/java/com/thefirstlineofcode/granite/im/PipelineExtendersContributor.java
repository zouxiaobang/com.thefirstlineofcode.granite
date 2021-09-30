package com.thefirstlineofcode.granite.im;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.oxm.annotation.AnnotatedParserFactory;
import com.thefirstlineofcode.basalt.protocol.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.protocol.im.roster.Roster;
import com.thefirstlineofcode.basalt.protocol.im.roster.RosterParser;
import com.thefirstlineofcode.basalt.protocol.im.roster.RosterTranslatorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersContributorAdapter;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.EventListenerFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventListenerFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.IProtocolParserFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.ProtocolParserFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.SingletonXepProcessorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.IProtocolTranslatorFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.ProtocolTranslatorFactory;
import com.thefirstlineofcode.granite.framework.core.session.ISessionListener;
import com.thefirstlineofcode.granite.framework.im.ResourceAvailabledEvent;
import com.thefirstlineofcode.granite.framework.im.SessionListener;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {

	@Override
	public IProtocolParserFactory<?>[] getProtocolParserFactories() {
		return new IProtocolParserFactory<?>[] {
			new ProtocolParserFactory<>(
					new IqProtocolChain(Roster.PROTOCOL),
					new AnnotatedParserFactory<Roster>(RosterParser.class)
			)			
		};
	}
	
	@Override
	public IXepProcessorFactory<?, ?>[] getXepProcessorFactories() {
		return new IXepProcessorFactory<?, ?>[] {
			new SingletonXepProcessorFactory<>(
					new IqProtocolChain().next(Roster.PROTOCOL),
					new RosterProcessor()
			)
		};
	}

	@Override
	public IProtocolTranslatorFactory<?>[] getProtocolTranslatorFactories() {
		return new IProtocolTranslatorFactory<?>[] {
			new ProtocolTranslatorFactory<>(Roster.class, new RosterTranslatorFactory())
		};
	}
	
	@Override
	public IEventListenerFactory<?>[] getEventListenerFactories() {
		return new IEventListenerFactory<?>[] {
			new EventListenerFactory<>(ResourceAvailabledEvent.class, new ResourceAvailabledEventListener())
		};
	}
	
	@Override
	public ISessionListener[] getSessionListeners() {
		return new ISessionListener[] {
			new SessionListener()
		};
	}

}
