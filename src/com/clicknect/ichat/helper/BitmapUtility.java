package com.clicknect.ichat.helper;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapUtility {

	// Convert Byte Array to Bitmap
	public static Bitmap convertByteArrayToBitmap(byte[] byteArray) {
		if ( byteArray == null ) {
			return null;
		} else {
			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
			} catch ( Exception e ) {
				Log.e("- Bitmap Utility -", "Unable to convert byte array to bitmap: "+e.getMessage());
			}
			return bitmap;
		}
	}
	
	// Convert Bitmap to Byte Array
	public static byte[] convertBitmapToByteArray(Bitmap bitmap) {
		if ( bitmap == null ) {
			return null;
		} else {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			return stream.toByteArray();
		}
	}	
	
}