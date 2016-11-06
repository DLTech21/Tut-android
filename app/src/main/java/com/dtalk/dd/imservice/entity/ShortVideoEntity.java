package com.dtalk.dd.imservice.entity;

/**
 * Created by Donal on 16/6/13.
 */
public class ShortVideoEntity {
    /**本地保存的path*/
    private String video_path = "";
    private String video_cover = "";
    /**图片的网络地址*/
    private String video_path_url = "";
    private String video_cover_url = "";
    private int loadStatus;

    public String getVideo_path() {
        return video_path;
    }

    public void setVideo_path(String video_path) {
        this.video_path = video_path;
    }

    public String getVideo_cover() {
        return video_cover;
    }

    public void setVideo_cover(String video_cover) {
        this.video_cover = video_cover;
    }

    public String getVideo_path_url() {
        return video_path_url;
    }

    public void setVideo_path_url(String video_path_url) {
        this.video_path_url = video_path_url;
    }

    public String getVideo_cover_url() {
        return video_cover_url;
    }

    public void setVideo_cover_url(String video_cover_url) {
        this.video_cover_url = video_cover_url;
    }

    public int getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(int loadStatus) {
        this.loadStatus = loadStatus;
    }
}
