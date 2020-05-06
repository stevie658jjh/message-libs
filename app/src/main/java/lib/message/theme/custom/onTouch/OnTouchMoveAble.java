package lib.message.theme.custom.onTouch;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import lib.message.theme.utils.MyApplication;

public class OnTouchMoveAble implements View.OnTouchListener {
    private final GestureDetector gestureDetector;
    private View.OnClickListener onClickListener;

    public OnTouchMoveAble(View.OnClickListener onClickListener) {
        gestureDetector = new GestureDetector(MyApplication.getInstance(), new SingleTapConfirm());
        this.onClickListener = onClickListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            onClickListener.onClick(v);
            return false;
        }
        return true;
    }


}
