package com.dtalk.dd.imservice.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;


public class HostMonitor extends Service {
	private final static int GRAY_SERVICE_ID = 1001;
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		PushManager.getInstance().initialize(this.getApplicationContext());
		if (Build.VERSION.SDK_INT < 18) {
			startForeground(GRAY_SERVICE_ID, new Notification());//API < 18 ，此方法能有效隐藏Notification上的图标
		} else {
			startForeground(GRAY_SERVICE_ID, new Notification());
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
