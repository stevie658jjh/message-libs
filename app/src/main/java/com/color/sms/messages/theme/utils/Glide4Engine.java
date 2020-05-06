package com.color.sms.messages.theme.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;

import com.color.sms.messages.theme.R;

@GlideModule
public class Glide4Engine extends AppGlideModule {

    private static Glide4Engine instance;

    public static Glide4Engine getInstance() {
        if (instance == null) {
            instance = new Glide4Engine();
        }
        return instance;
    }

    public Glide4Engine() {

    }

    public void loadImageOriginal(ImageView imageView, Object object, Object obj, LoadImageListener loadImageListener) {
        GlideApp.with(MyApplication.getInstance())
                .asBitmap()
                .load(object)
                .dontAnimate()
                .priority(Priority.HIGH)
                .addListener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        if (obj != null && obj != object) {
                            loadImageOriginal(imageView, obj, obj, loadImageListener);
                        }
                        if (loadImageListener != null)
                            loadImageListener.OnLoaded();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (loadImageListener != null) loadImageListener.OnLoaded();
                        return false;
                    }
                })
                .into(imageView);
    }

    public void loadImage(ImageView imageView, Object obj) {
        GlideApp.with(MyApplication.getInstance())
                .load(obj)
                .thumbnail(0.1f)
                .dontAnimate()
                .priority(Priority.HIGH)
                .into(imageView);
    }

    public void loadImageDefault(ImageView imageView) {
        GlideApp.with(MyApplication.getInstance())
                .load("")
                .placeholder(R.drawable.ic_person_black_24dp)
                .priority(Priority.HIGH)
                .into(imageView);
    }


    public void loadImageMessage(Context context, ImageView imageView, Object object) {
        int w = (Function.getScreenWidth() / 2) - ((Function.getScreenWidth() / 2) / 10);
        int h = Function.getScreenHeight() / 2 - ((Function.getScreenHeight() / 2) / 5);
        GlideApp.with(context)
                .asBitmap()
                .load(object)
                .thumbnail(0.1f)
                .override(w, h)
                .into(new ImageViewTarget<Bitmap>(imageView) {
                    @Override
                    protected void setResource(@Nullable Bitmap resource) {
                        if (resource == null) return;
                        int minHeight = w - w / 4;
                        int maxHeight = (int) (w * 2.5);
                        int height = getHeightImageFromBitmap(resource);

                        if (height < minHeight) {
                            height = minHeight;
                        } else if (height > maxHeight) {
                            height = maxHeight;
                        }
                        view.setImageBitmap(resource);
                        view.getLayoutParams().height = height;
                        view.setScaleType(ImageView.ScaleType.FIT_XY);
                        view.requestLayout();
                    }
                });
    }


    public void loadImageBitmap(ImageView imageView, Object obj) {
        GlideApp.with(MyApplication.getInstance())
                .asBitmap()
                .load(obj)
                .thumbnail(0.1f)
                .dontAnimate()
                .priority(Priority.HIGH)
                .into(imageView);
    }

    public void loadImageItem(ImageView imageView, Object obj) {
        GlideApp.with(MyApplication.getInstance())
                .load(obj)
                .override(80, 80)
                .thumbnail(0.1f)
                .dontAnimate()
                .priority(Priority.HIGH)
                .into(imageView);
    }

    public void loadGifImage(ImageView imageView, Object obj) {
        GlideApp.with(MyApplication.getInstance())
                .asGif()
                .load(obj)
                .priority(Priority.HIGH)
                .into(imageView);
    }

    private int getHeightImageFromBitmap(Bitmap resource) {
        float w = (float) ((Function.getScreenWidth() / 2) - ((Function.getScreenWidth() / 2) / 10));
        float widthOri = resource.getWidth();
        float heightOri = resource.getHeight();
        if (heightOri == widthOri) return (int) w;
        if (heightOri - widthOri > 0) {
            float a;
            if ((widthOri - w) > 0) {
                a = widthOri / w;
            } else {
                a = w / widthOri;
            }
            return (int) (heightOri / a);
        } else {
            float a;
            if ((widthOri - w) > 0) {
                a = widthOri / w;
            } else {
                a = w / widthOri;
            }
            return (int) (heightOri / a);
        }
    }

    public interface LoadImageListener {
        void OnLoaded();
    }
}
