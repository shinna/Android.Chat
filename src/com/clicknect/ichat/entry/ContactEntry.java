package com.clicknect.ichat.entry;

import android.graphics.Bitmap;

public class ContactEntry {
	
	private String jid;
	private String nickname;
	private byte[] avatarByte;
	private Bitmap avatarBitmap;
	private boolean isOnline;
	
	public ContactEntry() {
		this.jid = null;
		this.nickname = null;
		this.avatarByte = null;
		this.avatarBitmap = null;
		this.isOnline = false;
	}
	
	public String getJid() {
		return jid;
	}
	
	public void setJid(String jid) {
		this.jid = jid;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public byte[] getAvatarByte() {
		return avatarByte;
	}
	
	public void setAvatarByte(byte[] avatarByte) {
		this.avatarByte = avatarByte;
	}
	
	public Bitmap getAvatarBitmap() {
		return avatarBitmap;
	}
	
	public void setAvatarBitmap(Bitmap avatarBitmap) {
		this.avatarBitmap = avatarBitmap;
	}
	
	public boolean isOnline() {
		return isOnline;
	}
	
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

}
