package com.clicknect.ichat.network;

import org.jivesoftware.smack.packet.IQ;

public class Pong extends IQ {

	public Pong() {
		this.setType(Type.ERROR);
	}
	
	@Override
	public String getChildElementXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<ping xmlns='urn:xmpp:ping' />");
		sb.append("<error type='cancel'>");
		sb.append("<service-unavailable xmlns='urn:ietf:params:xml:ns:xmpp-stanzas' />");
		sb.append("</error>");
		return sb.toString();
	}

}
