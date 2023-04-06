package com.park.fceux.control;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.park.fceux.EmulatorController;
import com.park.fceux.GameActivity;

public class DirectKeyView extends KeyView {
    public DirectKeyView(Context context) {
        super(context);
    }

    public DirectKeyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DirectKeyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int keyLength;
    private static final int keyWidth = 110;

    private Rect leftRect, rightRect, upRect, downRect;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        keyLength = (size - keyWidth) / 2;
        leftRect = new Rect(0, keyLength, keyLength, keyLength + keyWidth);
        rightRect = new Rect(keyLength + keyWidth, keyLength, size, keyLength + keyWidth);
        upRect = new Rect(keyLength, 0, keyLength + keyWidth, keyLength);
        downRect = new Rect(keyLength, keyLength + keyWidth, keyLength + keyWidth, size);
    }

    private boolean up, down, left, right;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (leftRect.contains(x, y)) {
                    left = true;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_LEFT, true);
                }
                if(rightRect.contains(x, y)) {
                    right = true;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_RIGHT, true);
                }
                if(upRect.contains(x, y)) {
                    up = true;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_UP, true);
                }
                if(downRect.contains(x, y)) {
                    down = true;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_DOWN, true);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (leftRect.contains(x, y)) {
                    left = false;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_LEFT, false);
                }
                if(rightRect.contains(x, y)) {
                    right = false;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_RIGHT, false);
                }
                if(upRect.contains(x, y)) {
                    up = false;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_UP, false);
                }
                if(downRect.contains(x, y)) {
                    down = false;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_DOWN, false);
                }
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawKey(canvas, leftRect, left);
        drawKey(canvas, rightRect, right);
        drawKey(canvas, upRect, up);
        drawKey(canvas, downRect, down);
    }
}
