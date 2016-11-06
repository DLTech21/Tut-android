package com.dtalk.dd.imservice.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dtalk.dd.NativeRuntime;
import com.dtalk.dd.utils.FileUtils;


public class BootCompletedReceiver extends BroadcastReceiver {

	public static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null)
			return;
//		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
//			NativeRuntime.getInstance().startService(context.getPackageName() + "/com.dtalk.dd.imservice.service.HostMonitor", FileUtils.createRootPath());
//		}
	}

}
