package com.github.hiroyuki_komatsu.pixel;

import android.graphics.Color;

/**
 * Created by komatsu on 5/6/15.
 */
public class PixelData {
    // Pixel data
    private int mPixelSize = 24;
    private int[][] mPixel;

    // Palette data
    private int mPaletteSize = 5;
    private int[] mPalette;

    public PixelData() {
        // Initialize color palette.
        mPalette = new int[mPaletteSize];
        mPalette[0] = Color.WHITE;
        mPalette[1] = Color.BLACK;
        mPalette[2] = Color.RED;
        mPalette[3] = Color.GREEN;
        mPalette[4] = Color.BLUE;

        mPixel = new int[mPixelSize][mPixelSize];
    }

    public int pixelSize() {
        return mPixelSize;
    }

    public int getColor(int x, int y) {
        return mPalette[getPixel(x, y)];
    }

    public int getPixel(int x, int y) {
        int paletteId = mPixel[x][y];
        if (paletteId >= mPaletteSize) {
            return 0;
        }
        return paletteId;
    }

    public boolean setPixel(int x, int y, int paletteId) {
        if (x >= mPixelSize || y >= mPixelSize) {
            return false;
        }
        if (paletteId >= mPaletteSize) {
            return false;
        }
        mPixel[x][y] = paletteId;
        return true;
    }

}
