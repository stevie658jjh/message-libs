package com.color.sms.messages.theme.model;

import androidx.annotation.Nullable;

import com.color.sms.messages.theme.tools.ColorRandom;

public class Conversation {
    private String recipientNumber;
    private int messageCount;
    private String recipientIds;
    private String snippet;
    private String address;
    private String readcount, snippet_cs, type, error, has_attachment, status;
    private long date;
    private int read;
    private int id;
    private int color;
    private boolean isBlocked;

    public Conversation() {
        color = ColorRandom.getColor();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getRecipientIds() {
        return recipientIds;
    }

    public void setRecipientIds(String recipientIds) {
        this.recipientIds = recipientIds;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public String getRecipientNumber() {
        return recipientNumber;
    }

    public void setRecipientNumber(String recipientNumber) {
        this.recipientNumber = recipientNumber;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public int getColor() {
        if (color == 0)
            color = ColorRandom.getColor();
        return color;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Conversation) {
            return ((Conversation) obj).id == this.id;
        } else return false;
    }
}
