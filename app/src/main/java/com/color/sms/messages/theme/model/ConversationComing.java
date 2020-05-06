package com.color.sms.messages.theme.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ConversationComing implements Parcelable {
    private String address, body;
    private long thread_id, time, id;

    public ConversationComing(String address, String body, long thread_id, long time) {
        this.address = address;
        this.body = body;
        this.thread_id = thread_id;
        this.time = time;
    }

    protected ConversationComing(Parcel in) {
        address = in.readString();
        body = in.readString();
        thread_id = in.readLong();
        time = in.readLong();
    }

    public static final Creator<ConversationComing> CREATOR = new Creator<ConversationComing>() {
        @Override
        public ConversationComing createFromParcel(Parcel in) {
            return new ConversationComing(in);
        }

        @Override
        public ConversationComing[] newArray(int size) {
            return new ConversationComing[size];
        }
    };

    public String getAddress() {
        return address;
    }

    public String getBody() {
        return body;
    }

    public long getThread_id() {
        return thread_id;
    }

    public long getTime() {
        return time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(body);
        dest.writeLong(thread_id);
        dest.writeLong(time);
    }
}
