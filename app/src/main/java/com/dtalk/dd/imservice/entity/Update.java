package com.dtalk.dd.imservice.entity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;

/**
 * Created by Donal on 16/5/18.
 */
public class Update implements Serializable {
    //	public int version_code;
//	public String download_url;
//	public String version_info;
//	public int minVersion;
//
    public int status;
    public String msg;

    public String version;
    public String changelog;
    public String installUrl;
    public String install_url;
    public String direct_install_url;

    public static Update parse(String json)
    {
        Update data = new Update();
        Gson gson = new Gson();
        data = gson.fromJson(json, new TypeToken<Update>()
        {
        }.getType());
        return data;
    }
}
