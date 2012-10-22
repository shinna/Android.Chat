package com.clicknect.ichat.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.clicknect.android.ichat.R;
import com.clicknect.ichat.entry.ChatEntry;

public class ChatAdapter extends BaseAdapter {
	
	private Context context;
	private ArrayList<ChatEntry> data;
	private ChatEntryHolder holder;
	private String myJID;
	
	public ChatAdapter(Context context, ArrayList<ChatEntry> data, String myJID) {
		this.context = context;
		this.data = data;
		this.myJID = myJID;
	}

	public ArrayList<ChatEntry> getData() {
		return data;
	}
	
	public void setData(ArrayList<ChatEntry> data) {
		this.data = data;
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
//		if ( convertView == null ) {
			if ( data.get(position).getSenderJID().equalsIgnoreCase(myJID) ) {
				convertView = LayoutInflater.from(context).inflate(R.layout.layout_chat_compact_right, null);
			} else {
				convertView = LayoutInflater.from(context).inflate(R.layout.layout_chat_compact_left, null);
			}
			holder = new ChatEntryHolder();
			holder.avatarImage = (ImageView) convertView.findViewById(R.id.chat_compact_avatar_image);
			holder.attachImage = (ImageView) convertView.findViewById(R.id.chat_compact_attachment_image);
			holder.message = (TextView) convertView.findViewById(R.id.chat_compact_message);
			convertView.setTag(holder);
//		} else {
//			holder = (ChatEntryHolder) convertView.getTag();
//		}
		
		if ( data.get(position).getSenderAvatarBitmap() != null ) {
			holder.avatarImage.setImageBitmap(data.get(position).getSenderAvatarBitmap());
		}
		holder.message.setText(data.get(position).getMessage());
		if ( data.get(position).isAttachedFile() ) {			
			holder.attachImage.setVisibility(View.VISIBLE);
		} else {
			holder.attachImage.setVisibility(View.GONE);
		}
		
		return convertView;
	}
	
	private class ChatEntryHolder {
		
		public ImageView avatarImage, attachImage;
		public TextView message;
		
	}

}
