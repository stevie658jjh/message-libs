package com.color.sms.messages.theme.model;

import android.graphics.Bitmap;

import com.color.sms.messages.theme.utils.FileController;

public class MediaAddingModel {
    public enum Type {
        GIF,
        EMOJI,
        CONTACT,
        AUDIO,
        ATTACH_IMAGE,
        ATTACH_VIDEO,
        ATTACH_FILE
    }

    private Type type;
    private String path;
    private Bitmap bitmap;
    private Contact contact;

    public MediaAddingModel() {
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getMinType() {
        if (path != null) {
            return FileController.getMimeType(path);
        }
        return "image/jpeg";
    }
}
