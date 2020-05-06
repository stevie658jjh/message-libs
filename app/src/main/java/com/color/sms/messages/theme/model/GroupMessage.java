package com.color.sms.messages.theme.model;

public class GroupMessage {
    private int groupId;
    private String contactId;
    private String groupName;
    private long date;
    private String groupNumber;
    private String body;

    public GroupMessage() {
    }

    public GroupMessage(String groupName, long date, String groupNumber, String body) {
        this.groupName = groupName;
        this.date = date;
        this.groupNumber = groupNumber;
        this.body = body;
    }

    public GroupMessage(int groupId, String groupName, long date, String groupNumber, String body) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.date = date;
        this.groupNumber = groupNumber;
        this.body = body;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(String groupNumber) {
        this.groupNumber = groupNumber;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
