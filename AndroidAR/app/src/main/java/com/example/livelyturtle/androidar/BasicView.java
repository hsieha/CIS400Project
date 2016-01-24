package com.example.livelyturtle.androidar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * TODO: document your custom view class.
 */
public class BasicView extends View {

    private List<Rect> rectangles;
    private Paint paint;

    public BasicView(Context context) {
        super(context);

        int sideLength = 20 + new Random().nextInt(80);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        rectangles = new LinkedList<>();

        // create a rectangle that we'll draw later
        for (int x = 10; x < width; x += (sideLength*6/5)) {
            for (int y = 10; y < height; y += (sideLength * 6 / 5)) {
                rectangles.add(new Rect(x, y, x+sideLength, y+sideLength));
            }
        }

        // create the Paint and set its color
        paint = new Paint();
        paint.setColor(Color.CYAN);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLUE);
        for (Rect rectangle : rectangles) {
            canvas.drawRect(rectangle, paint);
        }
    }
}