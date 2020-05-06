package com.color.sms.messages.theme.custom.onTouch;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.color.sms.messages.theme.utils.MyApplication;

public class DragViewListener implements View.OnTouchListener {
    private static final String TAG = "=>>";
    private final GestureDetector gestureDetector;
    private View.OnClickListener onClickListener;
    private LocationListener locationListener;

    public void setLocationListener(LocationListener locationListener) {
        this.locationListener = locationListener;
    }

    public DragViewListener(View.OnClickListener clickListener) {
        gestureDetector = new GestureDetector(MyApplication.getInstance(), new SingleTapConfirm());
        this.onClickListener = clickListener;
    }

    public DragViewListener() {
        gestureDetector = new GestureDetector(MyApplication.getInstance(), new SingleTapConfirm());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch: " + event.getAction());
        if (gestureDetector.onTouchEvent(event)) {
            // single tap
            if (onClickListener != null)
                onClickListener.onClick(v);
            return true;
        } else {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (locationListener != null) {
                    locationListener.onMoved(v.getX(), v.getY());
                }
                return false;
            }
            v.performClick();
            return true;
        }
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }

    public interface LocationListener {
        void onMoved(float x, float y);
    }
}