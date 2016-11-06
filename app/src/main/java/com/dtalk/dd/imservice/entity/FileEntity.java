package com.dtalk.dd.imservice.entity;

/**
 * Created by Donal on 16/4/22.
 */
public class FileEntity {
    /**本地保存的path*/
    private String path = "";
    private String fileName = "";
    private String ext="";
    /**图片的网络地址*/
    private String url = "";
    private int loadStatus;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(int loadStatus) {
        this.loadStatus = loadStatus;
    }
}
