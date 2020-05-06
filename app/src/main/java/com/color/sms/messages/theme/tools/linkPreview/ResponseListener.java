package com.color.sms.messages.theme.tools.linkPreview;

public interface ResponseListener {

    void onData(MetaData metaData);

    void onError(Exception e);
}