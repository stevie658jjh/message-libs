package com.color.sms.messages.theme.custom.onTouch;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return true;
    }

}
