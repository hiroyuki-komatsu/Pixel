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
    private int mDotSize = 1;  // To be updated with (canvas_size / mPixelSize).

    private PixelData mPixelData = null;

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

        mPixelData = new PixelData();
    }

    public void setPixelData(PixelData pixelData) {
        mPixelData = pixelData;
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        if (mPixelData == null) {
            return true;
        }
        int x = (int)event.getX() / mDotSize;
        int y = (int)event.getY() / mDotSize;

        mPixelData.setPixel(x, y, mPixelData.getPixel(x, y) + 1);

        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Reset canvas
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mPaintBackground);

        if (mPixelData == null) {
            return;
        }

        // Draw Pixel
        int x = 0;
        int y = 0;
        for (int i = 0; i < mPixelData.pixelSize(); ++i) {
            for (int j = 0; j < mPixelData.pixelSize(); ++j) {
                x = mDotSize * i;
                y = mDotSize * j;

                mPaint.setColor(mPixelData.getColor(i, j));
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
        if (mPixelData != null) {
            mDotSize = width / mPixelData.pixelSize();
        }
        setMeasuredDimension(width, height);
    }
}
