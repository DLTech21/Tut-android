/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtalk.dd.ui.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.baidu.mapapi.utils.OpenClientUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.dtalk.dd.R;
import com.dtalk.dd.ui.base.TTBaseActivity;
import com.dtalk.dd.utils.ThemeUtils;

public class LocationCheckActivity extends TTBaseActivity implements OnClickListener{
    
    @ViewInject(R.id.tvLocation)
    TextView tvLocation;
    static MapView mMapView = null;
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();

    // LocationData locData = null;
    LatLng lastLatLng = null;
    String lastName = null;
    public static LocationCheckActivity instance = null;
    private BaiduMap mBaiduMap;
    
    private LatLng geoLocation;
    String geoName = null;
    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class BaiduSDKReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                Toast.makeText(instance, "key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置", Toast.LENGTH_SHORT).show();
            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                Toast.makeText(instance, "网络出错", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private BaiduSDKReceiver mBaiduReceiver;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext  
        //注意该方法要再setContentView方法之前实现  
        SDKInitializer.initialize(getApplicationContext());  
        LayoutInflater.from(this).inflate(R.layout.activity_baidumap_check, topContentView);
        ViewUtils.inject(this);
        mMapView = (MapView) findViewById(R.id.bmapView);
        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("latitude", 0);
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);
        initMapView();
        double longtitude = intent.getDoubleExtra("longitude", 0);
        String address = intent.getStringExtra("address");
        LatLng p = new LatLng(latitude, longtitude);
        lastLatLng = p;
        lastName = address;
        tvLocation.setText(lastName);
        setLeftButton(R.drawable.tt_top_back);
        setLeftText(getResources().getString(R.string.top_left_back));
        setTitle(lastName);
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);
        // 注册 SDK 广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mBaiduReceiver = new BaiduSDKReceiver();
        registerReceiver(mBaiduReceiver, iFilter);
        

        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);

        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll");
        option.setScanSpan(300);
        option.setAddrType("all");
        mLocClient.setLocOption(option);
        showMap(lastLatLng.latitude, lastLatLng.longitude, lastName);
    }

    private void showMap(double latitude, double longtitude, String address) {
        LatLng llA = new LatLng(latitude, longtitude);
        OverlayOptions ooA = new MarkerOptions().position(llA).icon(BitmapDescriptorFactory
                .fromResource(R.drawable.map_pin))
                .zIndex(4).draggable(true);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(llA, 17.0f);
        mBaiduMap.animateMapStatus(u);
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        if (mLocClient != null) {
            mLocClient.stop();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mLocClient != null)
            mLocClient.stop();
        mMapView.onDestroy();
        unregisterReceiver(mBaiduReceiver);
        super.onDestroy();
    }
    private void initMapView() {
        mMapView.setLongClickable(true);
        mBaiduMap.setMyLocationEnabled(true);
    }

    /**
     * 监听函数，有新位置的时候，格式化成字符串，输出到屏幕中
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            com.dtalk.dd.utils.ViewUtils.dismissProgressDialog();
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            LatLng ll = new LatLng(location.getLatitude(),
                    location.getLongitude());
            geoLocation = ll;
            geoName = location.getAddrStr();
            
            startNavi();
        }

        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null) {
                return;
            }
        }
    }

    public class NotifyLister extends BDNotifyListener {
        public void onNotify(BDLocation mlocation, float distance) {
        }
    }

    public void back(View v) {
        finish();
    }

    public void sendLocation(View view) {
        Intent intent = this.getIntent();
        intent.putExtra("latitude", lastLatLng.latitude);
        intent.putExtra("longitude", lastLatLng.longitude);
        intent.putExtra("address", lastName);
        this.setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_btn:
            case R.id.left_txt:
                finish();
                break;
            case R.id.btnNav:
                com.dtalk.dd.utils.ViewUtils.createProgressDialog(getRunningActivity(), "正在确定位置...", ThemeUtils.getThemeColor()).show();
                if (mLocClient != null) {
                    mLocClient.start();
                }
                break;
        
            default:
                break;
            }
    }
    
    public void startNavi() {
        try {
            NaviParaOption para = new NaviParaOption()
            .startPoint(geoLocation).endPoint(lastLatLng)
            .startName(geoName).endName(lastName);
            BaiduMapNavigation.openBaiduMapNavi(para, this);
        } catch (BaiduMapAppNotSupportNaviException e) {
            showDialog();
        }
    }
    
    /**
     * 提示未安装百度地图app或app版本过低
     */
    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                OpenClientUtil.getLatestBaiduMapApp(getApplicationContext());
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();

    }

}
