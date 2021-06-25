package com.firstlinecode.granite.framework.core.bxmpp;

import com.firstlinecode.basalt.oxm.binary.IBinaryXmppProtocolConverter;
import com.firstlinecode.basalt.oxm.preprocessing.IMessagePreprocessor;

public interface IBxmppService {
	IMessagePreprocessor getBinaryMessagePreprocessor();
	IBinaryXmppProtocolConverter getBxmppProtocolConverter();
}
