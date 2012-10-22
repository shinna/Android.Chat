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
import com.clicknect.ichat.entry.ContactEntry;

public class ContactAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<ContactEntry> data;
	private ContactEntryHolder holder;
	
	public ContactAdapter(Context context, ArrayList<ContactEntry> data) {
		this.context = context;
		this.data = data;
	}
	
	public ArrayList<ContactEntry> getData() {
		return data;
	}

	public void setData(ArrayList<ContactEntry> data) {
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
		if ( convertView == null ) {
			convertView = LayoutInflater.from(context).inflate(R.layout.layout_contact_entry, null);
			holder = new ContactEntryHolder();
			holder.avatarImage = (ImageView) convertView.findViewById(R.id.contact_entry_avatar_image);
			holder.displayNameText = (TextView) convertView.findViewById(R.id.contact_entry_display_name_text);
			convertView.setTag(holder);
		} else {
			holder = (ContactEntryHolder) convertView.getTag();
		}
		
		if ( data.get(position).getAvatarBitmap() != null ) {
			holder.avatarImage.setImageBitmap(data.get(position).getAvatarBitmap());
		}
		holder.displayNameText.setText(data.get(position).getNickname());
		
		return convertView;
	}
	
	private class ContactEntryHolder {
		
		public ImageView avatarImage;
		public TextView displayNameText;
		
	}

}
