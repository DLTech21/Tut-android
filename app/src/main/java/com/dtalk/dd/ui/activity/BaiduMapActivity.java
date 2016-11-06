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

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapTouchListener;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.dtalk.dd.R;
import com.dtalk.dd.imservice.event.PoiSearchEvent;
import com.dtalk.dd.ui.adapter.LocationPoiAdapter;
import com.dtalk.dd.utils.ThemeUtils;
import com.dtalk.dd.utils.ViewUtils;

import de.greenrobot.event.EventBus;

public class BaiduMapActivity extends Activity implements OnClickListener, OnGetGeoCoderResultListener, OnMapStatusChangeListener, OnItemClickListener{

	static MapView mMapView = null;
	FrameLayout mMapViewContainer = null;
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	public NotifyLister mNotifyer = null;

	Button sendButton = null;

	// LocationData locData = null;
	static LatLng lastLatLng = null;
	static String lastName = null;
	public static BaiduMapActivity instance = null;
	private BaiduMap mBaiduMap;
	
	private LocationMode mCurrentMode;
	
	GeoCoder mSearch = null;
	ListView listView;
    ArrayList<PoiInfo> datas;
    LocationPoiAdapter adapter;
    
    boolean isTouch = true;
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
		EventBus.getDefault().register(this);
		//在使用SDK各组件之前初始化context信息，传入ApplicationContext  
        //注意该方法要再setContentView方法之前实现  
        SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_baidumap);
		initView();
		mMapView = (MapView) findViewById(R.id.bmapView);
		Intent intent = getIntent();
		double latitude = intent.getDoubleExtra("latitude", 0);
		mCurrentMode = LocationMode.NORMAL;
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
		mBaiduMap.setMapStatus(msu);
		initMapView();
		mMapView = new MapView(this, new BaiduMapOptions());
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
						mCurrentMode, true, null));
		showMapWithLocationClient();
		// 注册 SDK 广播监听者
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		mBaiduReceiver = new BaiduSDKReceiver();
		registerReceiver(mBaiduReceiver, iFilter);
		listView = (ListView) findViewById(R.id.listview);
		datas = new ArrayList<PoiInfo>();
        adapter = new LocationPoiAdapter(this, datas);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        
        mBaiduMap.setOnMapTouchListener(new OnMapTouchListener() {
            
            @Override
            public void onTouch(MotionEvent arg0) {
                isTouch = true;
            }
        });
	}

	private void initView() {
		TextView topTitleTxt = (TextView) findViewById(R.id.base_activity_title);
		ImageView topLeftBtn = (ImageView) findViewById(R.id.left_btn);
		TextView letTitleTxt = (TextView) findViewById(R.id.left_txt);
		letTitleTxt.setText(getResources().getString(R.string.top_left_back));
		topTitleTxt.setText("位置");
		topLeftBtn.setImageResource(R.drawable.tt_top_back);
		topLeftBtn.setOnClickListener(this);
		letTitleTxt.setOnClickListener(this);
	}

	private void showMap(double latitude, double longtitude, String address) {
		sendButton.setVisibility(View.GONE);
		LatLng llA = new LatLng(latitude, longtitude);
		CoordinateConverter converter= new CoordinateConverter();
		converter.coord(llA);
		converter.from(CoordinateConverter.CoordType.COMMON);
		LatLng convertLatLng = converter.convert();
		OverlayOptions ooA = new MarkerOptions().position(convertLatLng).icon(BitmapDescriptorFactory
				.fromResource(R.drawable.icon_marka))
				.zIndex(4).draggable(true);
		mBaiduMap.addOverlay(ooA);
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(convertLatLng, 20.0f);
		mBaiduMap.animateMapStatus(u);
	}

	private void showMapWithLocationClient() {
		ViewUtils.createProgressDialog(this, "正在确定你的位置....", ThemeUtils.getThemeColor()).show();

		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);

		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll");
		option.setScanSpan(300);
		option.setAddrType("all");
		mLocClient.setLocOption(option);
		mLocClient.start();
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
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
		EventBus.getDefault().unregister(this);
	}
	private void initMapView() {
	    mBaiduMap.setOnMapStatusChangeListener(this);
		mMapView.setLongClickable(true);
		mBaiduMap.setMyLocationEnabled(true);
	}

	/**
	 * 监听函数，有新位置的时候，格式化成字符串，输出到屏幕中
	 */
	public class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
            ViewUtils.dismissProgressDialog();
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
            lastLatLng = ll;
            lastName = location.getAddrStr();
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(20.0f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
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
				BaiduMapActivity.this.finish();
				break;
        case R.id.btn_location_send:
            sendLocation(v);
            break;
        case R.id.btn_location_search:
            startActivity(new Intent(this, BaiduMapSearchActivity.class));
            break;
        	default:
				break;
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        datas.clear();
        adapter.notifyDataSetChanged();
        for (PoiInfo i : result.getPoiList()) {
            datas.add(i);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMapStatusChange(MapStatus status) {
        if (isTouch) {
            datas.clear();
            mSearch.reverseGeoCode(new ReverseGeoCodeOption()
            .location(status.target));
        }
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus arg0) {

    }

    @Override
    public void onMapStatusChangeStart(MapStatus arg0) {

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        isTouch = false;
        PoiInfo p = datas.get(position);
        lastLatLng = p.location;
        lastName = p.name;
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(p.location));
    }
    
    public void onEvent(PoiSearchEvent event) {
        isTouch = true;
        PoiInfo p = event.p;
        lastLatLng = p.location;
        lastName = p.name;
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(p.location));
    }
}
