package com.park.fceux.control;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.park.fceux.EmulatorController;
import com.park.fceux.GameActivity;

public class FunctionKeyView extends KeyView {
    public FunctionKeyView(Context context) {
        super(context);
    }

    public FunctionKeyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FunctionKeyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private Rect TARect, TBRect, ARect, BRect;
    private boolean TA, TB, A, B;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        TARect = new Rect(margin, margin, (size / 2) - margin, (size / 2) - margin);
        TBRect = new Rect((size / 2) + margin, margin, size - margin, (size / 2) - margin);
        ARect = new Rect(margin, (size / 2) + margin, (size / 2) - margin, size - margin);
        BRect = new Rect((size / 2) + margin, (size / 2) + margin, size - margin, size - margin);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (TARect.contains(x, y)) {
                    TA = true;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_A_TURBO, true);
                }
                if (TBRect.contains(x, y)) {
                    TB = true;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_B_TURBO, true);
                }
                if (ARect.contains(x, y)) {
                    A = true;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_A, true);
                }
                if (BRect.contains(x, y)) {
                    B = true;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_B, true);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (TARect.contains(x, y)) {
                    TA = false;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_A_TURBO, false);
                }
                if (TBRect.contains(x, y)) {
                    TB = false;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_B_TURBO, false);
                }
                if (ARect.contains(x, y)) {
                    A = false;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_A, false);
                }
                if (BRect.contains(x, y)) {
                    B = false;
                    GameActivity.runner.setKeyPressed(0, EmulatorController.KEY_B, false);
                }
                break;
        }
        invalidate();
        return true;
    }

    private static final int margin = 5;

    @Override
    protected void onDraw(Canvas canvas) {
        drawKey(canvas, TARect, TA, "turbo A");
        drawKey(canvas, TBRect, TB, "turbo B");
        drawKey(canvas, ARect, A, "A");
        drawKey(canvas, BRect, B, "B");
    }
}
