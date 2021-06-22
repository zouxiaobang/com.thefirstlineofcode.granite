package com.firstlinecode.granite.stream.standard.codec;

import java.io.UnsupportedEncodingException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.firstlinecode.basalt.oxm.binary.IBinaryXmppProtocolConverter;
import com.firstlinecode.basalt.protocol.Constants;

public class MessageEncoder extends ProtocolEncoderAdapter {
	private static final char CHAR_HEART_BEAT = ' ';
	private static final byte BYTE_HEART_BEAT = (byte)CHAR_HEART_BEAT;
	
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
		} else if (isHeartBeatChar(message)) {
			out.write(IoBuffer.wrap((byte[])message));
		} else if (isHeartBeatByte(message)) {
			out.write(IoBuffer.wrap(new byte[] {(byte)message}));
		} else {
			throw new RuntimeException(String.format("Unknown message type: %s.", message.getClass().getName()));
		}
		
		out.flush();
	}

	private boolean isHeartBeatByte(Object message) {
		if (bxmppProtocolConverter == null)
			return false;
		
		if (!(message instanceof Byte))
			return false;
		
		return  ((Byte)message).byteValue() == BYTE_HEART_BEAT;
	}

	private boolean isHeartBeatChar(Object message) {
		if (bxmppProtocolConverter != null)
			return false;
		
		if (!(message instanceof byte[]))
			return false;
		
		try {
			return isHeartBeatString(new String((byte[])message, Constants.DEFAULT_CHARSET));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(String.format("%s not supported.", Constants.DEFAULT_CHARSET), e);
		}
	}

	private boolean isHeartBeatString(String message) {
		for (char c : message.toCharArray()) {
			if (c != CHAR_HEART_BEAT)
				return false;
		}
		
		return true;
	}

}
