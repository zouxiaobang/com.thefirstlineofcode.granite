package com.firstlinecode.granite.im;

import org.pf4j.Extension;

import com.firstlinecode.basalt.oxm.annotation.AnnotatedParserFactory;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;
import com.firstlinecode.basalt.protocol.im.roster.Roster;
import com.firstlinecode.basalt.protocol.im.roster.RosterParser;
import com.firstlinecode.basalt.protocol.im.roster.RosterTranslatorFactory;
import com.firstlinecode.granite.framework.core.pipeline.PipelineExtendersContributorAdapter;
import com.firstlinecode.granite.framework.core.pipeline.event.EventListenerFactory;
import com.firstlinecode.granite.framework.core.pipeline.event.IEventListenerFactory;
import com.firstlinecode.granite.framework.core.pipeline.parsing.IProtocolParserFactory;
import com.firstlinecode.granite.framework.core.pipeline.parsing.ProtocolParserFactory;
import com.firstlinecode.granite.framework.core.pipeline.processing.IXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.processing.SingletonXepProcessorFactory;
import com.firstlinecode.granite.framework.core.pipeline.routing.IProtocolTranslatorFactory;
import com.firstlinecode.granite.framework.core.pipeline.routing.ProtocolTranslatorFactory;
import com.firstlinecode.granite.framework.im.ResourceAvailabledEvent;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersContributorAdapter {

	@Override
	public IProtocolParserFactory<?>[] getProtocolParserFactories() {
		return new IProtocolParserFactory<?>[] {
			new ProtocolParserFactory<>(
					ProtocolChain.first(Iq.PROTOCOL).next(Roster.PROTOCOL),
					new AnnotatedParserFactory<Roster>(RosterParser.class)
			)			
		};
	}
	
	@Override
	public IXepProcessorFactory<?, ?>[] getXepProcessorFactories() {
		return new IXepProcessorFactory<?, ?>[] {
			new SingletonXepProcessorFactory<>(
					ProtocolChain.first(Iq.PROTOCOL).next(Roster.PROTOCOL),
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

}
