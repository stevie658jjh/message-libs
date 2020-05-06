package lib.message.theme.tools.linkPreview;

public interface ResponseListener {

    void onData(MetaData metaData);

    void onError(Exception e);
}