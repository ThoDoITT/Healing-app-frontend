package com.example.healingapp.data.models;

public class GridModelList {
    private final String title;
    private final Integer icon;

    public GridModelList(String title, Integer icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public Integer getIcon() {
        return icon;
    }
}
