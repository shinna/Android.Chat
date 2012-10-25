package com.clicknect.ichat.network;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;

import android.util.Log;

public class PingPacketListener implements PacketListener {

	private XMPPConnection connection;
	
	public PingPacketListener(XMPPConnection connection) {
		this.connection = connection;
	}
	
	public void processPacket(Packet packet) {
		if ( packet == null ) {
			Log.e(PingPacketListener.class.getSimpleName(), "Ping packet is NULL");
			return;
		}
		
		Pong pong = new Pong();
		String from = packet.getFrom();
		String to = packet.getTo();
		Log.i(PingPacketListener.class.getSimpleName(), "-----> Original from "+from+", to "+to);
		
		pong.setFrom(to);
		pong.setTo(from);
		Log.i(PingPacketListener.class.getSimpleName(), "-----> Sending XMPP Pong Packet: ");
		Log.i(PingPacketListener.class.getSimpleName(), pong.toXML());
		
		this.connection.sendPacket(pong);
	}

}
