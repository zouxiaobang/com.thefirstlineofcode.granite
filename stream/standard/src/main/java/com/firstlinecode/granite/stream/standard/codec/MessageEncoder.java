package com.firstlinecode.granite.stream.standard.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.firstlinecode.basalt.protocol.Constants;
import com.firstlinecode.basalt.oxm.binary.IBinaryXmppProtocolConverter;

public class MessageEncoder extends ProtocolEncoderAdapter {
	private IBinaryXmppProtocolConverter bxmppProtocolConverter;
	
	public MessageEncoder() {}
	
	public MessageEncoder(IBinaryXmppProtocolConverter bxmppProtocolConverter) {
		this.bxmppProtocolConverter = bxmppProtocolConverter;
	}
	
	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		if (message instanceof String) {
			if (bxmppProtocolConverter != null) {
				out.write(IoBuffer.wrap(bxmppProtocolConverter.toBinary((String)message)));
			} else {
				out.write(IoBuffer.wrap(((String)message).getBytes(Constants.DEFAULT_CHARSET)));
			}
			out.flush();
		}
	}

}
