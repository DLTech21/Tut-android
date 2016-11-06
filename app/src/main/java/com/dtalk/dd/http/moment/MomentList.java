package com.dtalk.dd.http.moment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MomentList implements Serializable{
    public List<Moment> list = new ArrayList<Moment>();
    public int status;
    public String msg;
    

    public static MomentList parse(String json) throws JSONException
    {
        MomentList data = new MomentList();
        data.list = new ArrayList<Moment>();
        Gson gson = new Gson();
//        data = gson.fromJson(json, new TypeToken<MomentList>()
//        {
//        }.getType());
        JSONObject obj = new JSONObject(json);
        JSONArray array = obj.getJSONArray("list");
        for (int i = 0; i < array.length(); i++) {
            JSONObject j = array.getJSONObject(i);
            Moment m = new Moment();
//            m.media = new Media();
//            m.media.longtxt = new LongTextBean();
//            m.media.url = new URLBean();
            m = gson.fromJson(j.toString(), new TypeToken<Moment>(){}.getType());
            data.list.add(m);
        }
        return data;
    }
}
