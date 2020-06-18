package com.example.android.popularmoviess1v02.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ImageConverter {

    public static String bitmapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        String bitmapInString = Base64.encodeToString(bytes, Base64.DEFAULT);

        return bitmapInString;
    }

    public static Bitmap stringToBitmap(String string){
        try{
            byte[] bytes = Base64.decode(string, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            return bitmap;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
