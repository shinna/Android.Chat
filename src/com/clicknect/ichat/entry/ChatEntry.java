package com.clicknect.ichat.entry;

import android.graphics.Bitmap;

public class ChatEntry {

	private String id;
	private String senderJID;
	private String receiverJID;
	private Bitmap senderAvatarBitmap;
	private String message;
	private boolean isAttachedFile;
	private String filePath;
	private long when;
	
	public ChatEntry() {
		this.id = null;
		this.senderJID = null;
		this.receiverJID = null;
		this.senderAvatarBitmap = null;
		this.message = null;
		this.isAttachedFile = false;
		this.filePath = null;
		this.when = 0;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSenderJID() {
		return senderJID;
	}

	public void setSenderJID(String senderJID) {
		this.senderJID = senderJID;
	}

	public String getReceiverJID() {
		return receiverJID;
	}

	public void setReceiverJID(String receiverJID) {
		this.receiverJID = receiverJID;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isAttachedFile() {
		return isAttachedFile;
	}

	public void setAttachedFile(boolean isAttachedFile) {
		this.isAttachedFile = isAttachedFile;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Bitmap getSenderAvatarBitmap() {
		return senderAvatarBitmap;
	}

	public void setSenderAvatarBitmap(Bitmap senderAvatarBitmap) {
		this.senderAvatarBitmap = senderAvatarBitmap;
	}

	public long getWhen() {
		return when;
	}

	public void setWhen(long when) {
		this.when = when;
	}
	
}
