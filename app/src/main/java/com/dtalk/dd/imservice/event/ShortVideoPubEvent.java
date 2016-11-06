package com.dtalk.dd.imservice.event;

/**
 * Created by Donal on 16/6/13.
 */
public class ShortVideoPubEvent {

    public String cover;
    public String path;

    public ShortVideoPubEvent(String cover, String path) {
        this.cover = cover;
        this.path = path;
    }
}
