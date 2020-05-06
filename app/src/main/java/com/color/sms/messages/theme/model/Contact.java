package com.color.sms.messages.theme.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.color.sms.messages.theme.tools.ColorRandom;

public class Contact implements Parcelable {
    private String id;
    private String name;
    private String phone;
    private String photoUri;
    private int type;
    private int color;

    public Contact() {
        color = ColorRandom.getColor();
    }

    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
        color = ColorRandom.getColor();
    }

    public Contact(String id, String name, String phone, String photoUri, int type) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.photoUri = photoUri;
        this.type = type;
        color = ColorRandom.getColor();
    }

    public int getType() {
        return type;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameA() {
        if (!TextUtils.isEmpty(name)) return name.charAt(0) + "";
        return "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTypeString() {
        if (type == 1) {
            return "Home";
        } else if (type == 2) {
            return ("Mobile");
        } else {
            return ("Work");
        }
    }

    public int getVisibility() {
        if (photoUri == null) return View.GONE;
        return View.VISIBLE;
    }

    public int getColor() {
        if (color == 0) {
            color = ColorRandom.getColor();
        }
        return color;
    }

    protected Contact(Parcel in) {
        id = in.readString();
        name = in.readString();
        phone = in.readString();
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(phone);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        try {
            if (obj instanceof Contact) {
                return this.phone.equalsIgnoreCase(((Contact) obj).phone);
            } else if (obj instanceof String) {
                return this.phone.equalsIgnoreCase((String) obj);
            } else return false;
        }catch (Exception e){
            return false;
        }

    }
}
