package com.example.trabajadera.PasarLista.Mapa;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ZoomLayout extends FrameLayout {

    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 4.0f;

    private float mScaleFactor = 1.0f;
    private float mPosX = 0;
    private float mPosY = 0;

    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = -1;

    private ScaleGestureDetector mScaleDetector;

    public ZoomLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ZoomLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Interceptamos si hay más de un dedo o si ya hay zoom aplicado
        if (ev.getPointerCount() > 1 || mScaleFactor > 1.0f) {
            getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);

                // --- SOLUCIÓN AL CRASH ---
                // Si el índice es -1, significa que el puntero no es válido en este momento.
                // Simplemente salimos del caso para evitar que la app se cierre.
                if (pointerIndex == -1) break;

                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                if (!mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    corregirLimites();
                    invalidate();
                }

                mLastTouchX = x;
                mLastTouchY = y;
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = -1;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = ev.getActionIndex();
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        // El orden es importante: primero movemos (translate) y luego ampliamos (scale)
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float previousScale = mScaleFactor;
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(MIN_ZOOM, Math.min(mScaleFactor, MAX_ZOOM));

            // Cálculo para que el zoom siga el punto focal de los dedos
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            mPosX -= (focusX - mPosX) * (mScaleFactor / previousScale - 1);
            mPosY -= (focusY - mPosY) * (mScaleFactor / previousScale - 1);

            corregirLimites();
            invalidate();
            return true;
        }
    }

    private void corregirLimites() {
        if (mScaleFactor == 1.0f) {
            mPosX = 0;
            mPosY = 0;
            return;
        }

        // Medidas del contenedor y del contenido escalado
        float width = getWidth();
        float height = getHeight();
        float scaledWidth = width * mScaleFactor;
        float scaledHeight = height * mScaleFactor;

        // Límites horizontales
        if (mPosX > 0) mPosX = 0;
        if (mPosX < width - scaledWidth) mPosX = width - scaledWidth;

        // Límites verticales
        if (mPosY > 0) mPosY = 0;
        if (mPosY < height - scaledHeight) mPosY = height - scaledHeight;
    }

    public void resetZoom() {
        mScaleFactor = 1.0f;
        mPosX = 0;
        mPosY = 0;
        invalidate();
    }
}