package com.color.sms.messages.theme.custom;

import android.content.Context;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

public class AnimatableCardView extends CardView {
    private float xFraction = 0;
    private float yFraction = 0;

    private ViewTreeObserver.OnPreDrawListener preDrawListener = null;

    public AnimatableCardView(Context context) {
        super(context);
    }

    public AnimatableCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatableCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Note that fraction "0.0" is the starting point of the view. This includes margins.
    // If this view was placed in (200,300), moveTo="0.1" for xFraction will give you (220,300)
    public void setXFraction(float fraction) {
        this.xFraction = fraction;

        if (((ViewGroup) getParent()).getWidth() == 0) {
            if (preDrawListener == null) {
                preDrawListener = () -> {
                    getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
                    setXFraction(xFraction);
                    return true;
                };
                getViewTreeObserver().addOnPreDrawListener(preDrawListener);
            }
            return;
        }

        float translationX = Math.max(0, (((ViewGroup) getParent()).getWidth()) * fraction - (getWidth() * getScaleX() / 2));
        setTranslationX(translationX);
    }

    public float getXFraction() {
        return this.xFraction;
    }

    public void setYFraction(float fraction) {
        this.yFraction = fraction;

        if (((ViewGroup) getParent()).getHeight() == 0) {
            if (preDrawListener == null) {
                preDrawListener = () -> {
                    getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
                    setYFraction(yFraction);
                    return true;
                };
                getViewTreeObserver().addOnPreDrawListener(preDrawListener);
            }
            return;
        }

        float translationY = Math.max(0, (((ViewGroup) getParent()).getHeight()) * fraction - (getHeight() * getScaleY() / 2));
        setTranslationY(translationY);
    }

    public float getYFraction() {
        return this.yFraction;
    }
}