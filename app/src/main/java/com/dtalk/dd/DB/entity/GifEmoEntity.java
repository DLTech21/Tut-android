package com.dtalk.dd.DB.entity;

/**
 * Created by Donal on 2016/12/21.
 */

public class GifEmoEntity {
    private Long id;
    private String url;
    private String path;
    private String mean;
    private int type;

    public GifEmoEntity() {
    }

    public GifEmoEntity(Long id, String url, String path, String mean, int type) {
        this.id = id;
        this.url = url;
        this.path = path;
        this.mean = mean;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMean() {
        return mean;
    }

    public void setMean(String mean) {
        this.mean = mean;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
