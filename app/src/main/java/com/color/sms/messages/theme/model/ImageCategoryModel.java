package com.color.sms.messages.theme.model;

public class ImageCategoryModel {
    private String firstPath;
    private String name;

    public ImageCategoryModel(String firstPath, String name) {
        this.firstPath = firstPath;
        this.name = name;
    }


    public String getFirstPath() {
        return firstPath;
    }

    public String getName() {
        return name;
    }
}
