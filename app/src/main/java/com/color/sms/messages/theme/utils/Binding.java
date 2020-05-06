package com.color.sms.messages.theme.utils;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

public class Binding {
    @BindingAdapter("load_image")
    public static void loadImage(ImageView view, Object imageId) {
        if (imageId != null)
            new Glide4Engine().loadImage(view, imageId);
    }
}
