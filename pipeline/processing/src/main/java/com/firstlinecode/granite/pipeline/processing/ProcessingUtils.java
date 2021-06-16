package com.firstlinecode.granite.pipeline.processing;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Iq;

public final class ProcessingUtils {
	private ProcessingUtils() {}
	
	public static Iq createIqResult(JabberId to, String id) {
		Iq result = new Iq();
		result.setId(id);
		result.setType(Iq.Type.RESULT);
		result.setTo(to);
		
		return result;
	}
}
