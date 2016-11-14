package com.dtalk.dd.imservice.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dtalk.dd.DB.sp.LoginSp;
import com.dtalk.dd.NativeRuntime;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.user.UserClient;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.utils.FileUtils;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.SandboxUtils;
import com.dtalk.dd.utils.StringUtils;

import cn.jpush.android.api.JPushInterface;


public class BootCompletedReceiver extends BroadcastReceiver {

	private LoginSp loginSp = LoginSp.instance();
	@Override
	public void onReceive(Context context, Intent intent) {
		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
			String regId = intent.getExtras().getString(JPushInterface.EXTRA_REGISTRATION_ID);
			Logger.d("接收Registration Id : " + regId);
			if (loginSp == null) {
				return;
			}
			if (loginSp.getLoginIdentity() == null)
				return;
			int loginId = loginSp.getLoginIdentity().getLoginId();
			String isUpdateCid = SandboxUtils.getInstance().get(IMApplication.getInstance(), loginId + "-regId");
			if (StringUtils.empty(isUpdateCid)) {
				updateClienId(regId);
			}
		}
	}

	private void updateClienId(String cid) {
		UserClient.updateUserPush(cid, new BaseClient.ClientCallback() {
			@Override
			public void onPreConnection() {

			}

			@Override
			public void onCloseConnection() {

			}

			@Override
			public void onSuccess(Object data) {
				SandboxUtils.getInstance().set(IMApplication.getInstance(), IMLoginManager.instance().getLoginId() + "-regId", "1");
			}

			@Override
			public void onFailure(String message) {

			}

			@Override
			public void onException(Exception e) {

			}
		});
	}
}
