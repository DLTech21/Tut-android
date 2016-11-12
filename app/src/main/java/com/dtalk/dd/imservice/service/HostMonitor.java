package com.dtalk.dd.imservice.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class HostMonitor extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		PushManager.getInstance().initialize(this.getApplicationContext());
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
