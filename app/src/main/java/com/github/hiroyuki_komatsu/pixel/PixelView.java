package com.github.hiroyuki_komatsu.pixel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by komatsu on 5/6/15.
 */
public class PixelView extends View {

    private Paint mPaint;
    private Paint mPaintBackground;

    // Data for drawing
    private int mDotSize = 0;  // = canvas_size / mPixelSize;

    // Pixel data
    private int mPixelSize = 24;
    private int[][] mPixel;

    // Palette data
    private int mPaletteSize = 5;
    private int[] mPalette;

    public PixelView(Context context) {
        this(context, null);
    }

    public PixelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PixelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = new Paint();
        mPaintBackground = new Paint();
        mPaintBackground.setColor(Color.rgb(0xC8, 0xE6, 0xC9));

        // Initialize color palette.
        mPalette = new int[mPaletteSize];
        mPalette[0] = Color.WHITE;
        mPalette[1] = Color.BLACK;
        mPalette[2] = Color.RED;
        mPalette[3] = Color.GREEN;
        mPalette[4] = Color.BLUE;

        mPixel = new int[mPixelSize][mPixelSize];
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        int x = (int)event.getX() / mDotSize;
        int y = (int)event.getY() / mDotSize;

        if (x >= mPixelSize || y >= mPixelSize) {
            return true;
        }

        mPixel[x][y] = (mPixel[x][y] + 1) % mPaletteSize;

        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Reset canvas
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mPaintBackground);

        // Draw Pixel
        int x = 0;
        int y = 0;
        for (int i = 0; i < mPixelSize; ++i) {
            for (int j = 0; j < mPixelSize; ++j) {
                x = mDotSize * i;
                y = mDotSize * j;

                // Get color from palette.
                int paletteIndex = mPixel[i][j];
                if (paletteIndex >= mPaletteSize) {
                    paletteIndex = 0;
                }
                mPaint.setColor(mPalette[paletteIndex]);
                canvas.drawRect(x, y, x + mDotSize - 1, y + mDotSize - 1, mPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (width > height) {
            width = height;
        } else {
            height = width;
        }
        mDotSize = width / mPixelSize;
        setMeasuredDimension(width, height);
    }
}
