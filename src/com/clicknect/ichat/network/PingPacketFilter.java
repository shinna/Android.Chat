package com.clicknect.ichat.network;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

import android.util.Log;

public class PingPacketFilter implements PacketFilter {

	public boolean accept(Packet packet) {
		if ( packet instanceof Ping ) {
			Log.i(PingPacketFilter.class.getSimpleName(), "----------------------------------");
			Log.i(PingPacketFilter.class.getSimpleName(), "Ping request received.");
			return true;
		} else {
			return false;
		}
	}

}
