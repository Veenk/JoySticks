package com.vlad.joysticks;

import android.content.Context;
import android.content.res.Resources;

import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import androidx.annotation.Nullable;
import com.vlad.joysticks.NetWork.Client;
import com.vlad.joysticks.listeners.JoystickListener;

import java.io.IOException;

public class JoyStickHandler extends SurfaceView implements
        SurfaceHolder.Callback, View.OnTouchListener {

private Bitmap mJoystick;
private SurfaceHolder mHolder;
private Rect mKnobBounds;
int mKnobX, mKnobY;
private int mKnobSize;
private int mBackgroundSize;
private float mRadius;
private JoystickListener joystickListener;
private Client client;
private boolean mAutoCentering = true;

public JoyStickHandler(Context context, AttributeSet attrs) {
    super(context, attrs);
    initGraphics(attrs);
    init();
}

private void initGraphics(AttributeSet attrs) {
        Resources res = getContext().getResources();
        mJoystick = BitmapFactory
                .decodeResource(res, R.mipmap.nose);
}

private void initBounds(final Canvas pCanvas) {
        mBackgroundSize = pCanvas.getHeight();
        mKnobSize = Math.round(mBackgroundSize * 0.6f);

        mKnobBounds = new Rect();

        mRadius = mBackgroundSize * 0.5f;
        mKnobX = Math.round((mBackgroundSize - mKnobSize) * 0.5f);
        mKnobY = Math.round((mBackgroundSize - mKnobSize) * 0.5f);
}

private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);

        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        setOnTouchListener(this);
        setEnabled(true);
        setAutoCentering(true);
}

public void setAutoCentering(final boolean pAutoCentering) {
        mAutoCentering = pAutoCentering;
}

public boolean isAutoCentering() {
        return mAutoCentering;
}


@Override
public void surfaceChanged(final SurfaceHolder arg0, final int arg1,
final int arg2, final int arg3) {
}

@Override
public void surfaceCreated(final SurfaceHolder arg0) {
        drawKnob();
}

@Override
public void surfaceDestroyed(final SurfaceHolder arg0) {
}

public void doDraw(final Canvas pCanvas) {
        if (mKnobBounds == null) {
            initBounds(pCanvas);
        }
        mKnobBounds.set(mKnobX, mKnobY, mKnobX + mKnobSize, mKnobY + mKnobSize);
        pCanvas.drawBitmap(mJoystick, null, mKnobBounds, null);
}

public void setJoystickListener(final JoystickListener mjoystickListener) {
    joystickListener = mjoystickListener;
}

@Override
public boolean onTouch(final View arg0, final MotionEvent pEvent) {
        final float x = pEvent.getX();
        final float y = pEvent.getY();

        switch (pEvent.getAction()) {

            case MotionEvent.ACTION_UP:
            if (isAutoCentering()) {
                mKnobX = Math.round((mBackgroundSize - mKnobSize) * 0.5f);
                mKnobY = Math.round((mBackgroundSize - mKnobSize) * 0.5f);
                drawKnob();

            }
            break;
        default:

            if (checkBounds(x, y)) {
                mKnobX = Math.round(x - mKnobSize * 0.5f);
                mKnobY = Math.round(y - mKnobSize * 0.5f);
            } else {
                final double angle = Math.atan2(y - mRadius, x - mRadius);
                mKnobX = (int) (Math.round(mRadius
                    + (mRadius - mKnobSize * 0.5f) * Math.cos(angle)) - mKnobSize * 0.5f);
                mKnobY = (int) (Math.round(mRadius
                    + (mRadius - mKnobSize * 0.5f) * Math.sin(angle)) - mKnobSize * 0.5f);
            }
            drawKnob();

        }

        if (joystickListener != null) {
            joystickListener.onTouch(this,
                    (short)mKnobX,
                    (short)mKnobY);

        }

        return true;
}

private boolean checkBounds(final float pX, final float pY) {
        return Math.pow(mRadius - pX, 2) + Math.pow(mRadius - pY, 2) <= Math
        .pow(mRadius - mKnobSize * 0.5f, 2);
}

private void drawKnob(){
    Canvas canvas = null;
    try {
        canvas = mHolder.lockCanvas(null);
        synchronized (mHolder) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            doDraw(canvas);
        }
    }
    catch(Exception ignored){}
    finally {
        if (canvas != null) {
            mHolder.unlockCanvasAndPost(canvas);
        }
    }
}

}
