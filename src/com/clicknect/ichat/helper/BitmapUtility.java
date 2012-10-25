package com.clicknect.ichat.helper;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
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
	
	// Convert Bitmap to Gray Scale Bitmap
	public static Bitmap convertBitmapToGrayScaleBitmap(Bitmap bmpOriginal) {        
	    int width, height;
	    height = bmpOriginal.getHeight();
	    width = bmpOriginal.getWidth();    

	    Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	    Canvas c = new Canvas(bmpGrayscale);
	    Paint paint = new Paint();
	    ColorMatrix cm = new ColorMatrix();
	    cm.setSaturation(0);
	    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
	    paint.setColorFilter(f);
	    c.drawBitmap(bmpOriginal, 0, 0, paint);
	    
	    return bmpGrayscale;
	}	
	
}