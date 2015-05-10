package com.github.hiroyuki_komatsu.pixel;

import android.graphics.Bitmap;

import java.io.OutputStream;

/**
 * Created by komatsu on 5/10/15.
 */
public class PixelDataConverter {
    static Bitmap convertToBitmap(PixelData pixelData) {
        Bitmap bitmap = Bitmap.createBitmap(
                pixelData.pixelSize(), pixelData.pixelSize(), Bitmap.Config.ARGB_8888);
        for (int x = 0; x < pixelData.pixelSize(); ++x) {
            for (int y = 0; y < pixelData.pixelSize(); ++y) {
                bitmap.setPixel(x, y, pixelData.getColor(x, y));
            }
        }
        return bitmap;
    }

    static boolean compressToPng(PixelData pixelData, OutputStream outputStream) {
        Bitmap bitmap = PixelDataConverter.convertToBitmap(pixelData);
        return bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
    }
}


