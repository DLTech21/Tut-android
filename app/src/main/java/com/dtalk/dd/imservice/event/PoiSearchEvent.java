package com.dtalk.dd.imservice.event;

import com.baidu.mapapi.search.core.PoiInfo;

public class PoiSearchEvent {
    public PoiInfo p;
    
    public PoiSearchEvent(PoiInfo data) {
        this.p = data;
    }
}
