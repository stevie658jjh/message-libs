package com.color.sms.messages.theme.model;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class Message implements Parcelable {
    private long id;
    private long threadId;
    private String address;
    private String body;
    private long date;
    private int type;
    private String user;
    private Bitmap bitmap;
    private Uri image;
    private Uri sticker;
    private Uri audio;
    private Uri video;
    private boolean isGif;

    public Message() {
    }

    public Message(ContentValues contentValues) {
        address = contentValues.getAsString("address");
        body = contentValues.getAsString("body");
        date = contentValues.getAsLong("date");
        type = contentValues.getAsInteger("type");
        id = contentValues.getAsLong("id");
        threadId = contentValues.getAsLong("thread_id");
    }

    public Message(int threadId, String address, String body, long date, int type) {
        this.threadId = threadId;
        this.address = address;
        this.body = body;
        this.date = date;
        this.type = type;
    }

    public Message(long threadId, String address, String body, long date, int type) {
        this.threadId = threadId;
        this.address = address;
        this.body = body;
        this.date = date;
        this.type = type;
    }

    public Message(Parcel in) {
        id = in.readLong();
        threadId = in.readLong();
        address = in.readString();
        body = in.readString();
        date = in.readLong();
        type = in.readInt();
        user = in.readString();
        image = in.readParcelable(Uri.class.getClassLoader());
        sticker = in.readParcelable(Uri.class.getClassLoader());
        audio = in.readParcelable(Uri.class.getClassLoader());
        video = in.readParcelable(Uri.class.getClassLoader());
        isGif = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(threadId);
        dest.writeString(address);
        dest.writeString(body);
        dest.writeLong(date);
        dest.writeInt(type);
        dest.writeString(user);
        dest.writeParcelable(image, flags);
        dest.writeParcelable(sticker, flags);
        dest.writeParcelable(audio, flags);
        dest.writeParcelable(video, flags);
        dest.writeByte((byte) (isGif ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public Uri getSticker() {
        return sticker;
    }

    public void setSticker(Uri sticker) {
        this.sticker = sticker;
    }

    public Uri getVideo() {
        return video;
    }

    public void setVideo(Uri video) {
        this.video = video;
    }

    public Uri getAudio() {
        return audio;
    }

    public void setAudio(Uri audio) {
        this.audio = audio;
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setGif(boolean gif) {
        isGif = gif;
    }

    public boolean isGif() {
        return isGif;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Message) {
            return ((Message) obj).threadId == this.threadId;
        } else return false;
    }
}
