package com.dtalk.dd.imservice.support;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.dtalk.dd.imservice.service.IMService;
import com.dtalk.dd.imservice.service.IMService.IMServiceBinder;
import com.dtalk.dd.utils.Logger;

/**
 * IMService绑定
 * @modify yingmu
 * 1. 供上层使用【activity】
 * 同层次的manager没有必要使用。
 */
public abstract class IMServiceConnector {

    public abstract void onIMServiceConnected();
    public abstract void onServiceDisconnected();

	private IMService imService;
	public IMService getIMService() {
		return imService;
	}

	// todo eric when to release?
	private ServiceConnection imServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// todo eric when to unbind the service?
			// TODO Auto-generated method stub
			Logger.i("onService(imService)Disconnected");
            IMServiceConnector.this.onServiceDisconnected();
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Logger.i("im#onService(imService)Connected");

			if (imService == null) {
				IMServiceBinder binder = (IMServiceBinder) service;
				imService = binder.getService();

				if (imService == null) {
					Logger.e("im#get imService failed");
					return;
				}
				Logger.d("im#get imService ok");
			}
            IMServiceConnector.this.onIMServiceConnected();
		}
	};

    public boolean connect(Context ctx) {
		return bindService(ctx);
	}

    public void disconnect(Context ctx) {
		Logger.d("im#disconnect");
		unbindService(ctx);
        IMServiceConnector.this.onServiceDisconnected();
	}

	public boolean bindService(Context ctx) {
		Logger.d("im#bindService");

		Intent intent = new Intent();
		intent.setClass(ctx, IMService.class);

		if (!ctx.bindService(intent, imServiceConnection, android.content.Context.BIND_AUTO_CREATE)) {
			Logger.e("im#bindService(imService) failed");
			return false;
		} else {
			Logger.i("im#bindService(imService) ok");
			return true;
		}
	}

	public void unbindService(Context ctx) {
		try {
			// todo eric .check the return value .check the right place to call it
			ctx.unbindService(imServiceConnection);
		} catch (IllegalArgumentException exception) {
		}
		Logger.i("unbindservice ok");
	}

}
