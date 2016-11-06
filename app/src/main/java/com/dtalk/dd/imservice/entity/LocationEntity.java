package com.dtalk.dd.imservice.entity;

/**
 * Created by Donal on 16/4/1.
 */
public class LocationEntity {
    private String addr;
    private String lat;
    private String lng;

    public String getAddress() {
        return addr;
    }

    public void setAddress(String address) {
        this.addr = address;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
