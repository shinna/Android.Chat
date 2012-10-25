package com.clicknect.ichat.network;

import org.jivesoftware.smack.packet.IQ;

public class Ping extends IQ {

	@Override
	public String getChildElementXML() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<ping xmlns='urn:xmpp:ping' />");
		return buffer.toString();
	}

}
