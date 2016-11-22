package com.dtalk.dd.http.moment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dtalk.dd.http.user.UserInfo;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MomentList implements Serializable{
    public List<Moment> list = new ArrayList<Moment>();
    public int status;
    public String msg;
}
