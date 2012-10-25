package com.clicknect.ichat.network;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

public class PingProvider implements IQProvider {

	public IQ parseIQ(XmlPullParser arg0) throws Exception {
		return new Ping();
	}

}
