package com.dtalk.dd.imservice.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.dtalk.dd.DB.DBInterface;
import com.dtalk.dd.DB.entity.ApplicantEntity;
import com.dtalk.dd.DB.entity.GroupEntity;
import com.dtalk.dd.DB.entity.MessageEntity;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.DB.sp.ConfigurationSp;
import com.dtalk.dd.DB.sp.LoginSp;
import com.dtalk.dd.NativeRuntime;
import com.dtalk.dd.R;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.config.MessageConstant;
import com.dtalk.dd.config.SysConstant;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.user.UserClient;
import com.dtalk.dd.imservice.entity.UnreadEntity;
import com.dtalk.dd.imservice.event.ApplicantEvent;
import com.dtalk.dd.imservice.event.LoginEvent;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.imservice.manager.IMNotificationManager;
import com.dtalk.dd.protobuf.helper.ProtoBuf2JavaBean;
import com.dtalk.dd.ui.activity.LoginActivity;
import com.dtalk.dd.ui.activity.MainActivity;
import com.dtalk.dd.utils.FileUtils;
import com.dtalk.dd.utils.IMUIHelper;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.SandboxUtils;
import com.dtalk.dd.utils.StringUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.concurrent.ConcurrentHashMap;

import cn.jpush.android.api.JPushInterface;
import de.greenrobot.event.EventBus;


public class BootCompletedReceiver extends BroadcastReceiver {
	private ConcurrentHashMap<String, UnreadEntity> unreadMsgMap = new ConcurrentHashMap<>();
	private DBInterface dbInterface = DBInterface.instance();
	private LoginSp loginSp = LoginSp.instance();
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
			String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
			Logger.d("接收Registration Id : " + regId);
			if (loginSp == null) {
				return;
			}
			if (loginSp.getLoginIdentity() == null)
				return;
			int loginId = loginSp.getLoginIdentity().getLoginId();
//			String isUpdateCid = SandboxUtils.getInstance().get(IMApplication.getInstance(), loginId + "-regId");
//			if (StringUtils.empty(isUpdateCid)) {
				updateClienId(regId);
//			}
		}else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
			Logger.d( "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
			String data = bundle.getString(JPushInterface.EXTRA_MESSAGE);
			try {
				ApplicantEntity applicantEntity = JSON.parseObject(data, ApplicantEntity.class);
				applicantEntity.setId(System.currentTimeMillis() / 1000);
				switch (applicantEntity.getType()) {
					case 1:
						triggerEvent(LoginEvent.FRIEND_RELOAD);
						DBInterface.instance().insertOrUpdateApplicant(applicantEntity);
						triggerEvent(ApplicantEvent.NEW_FRIEND_APPLICANT);
						break;
					case 2:
						triggerEvent(LoginEvent.FRIEND_RELOAD);
						break;
					case 99:
					case 100:
						if (!isRunningForeground(context)) {
							onRecMsg(applicantEntity, context);
						} else {
							vibrate(context);
						}
						break;
					default:

						break;
				}

			} catch (Exception e) {
				Logger.e(e);
			}

		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
			Logger.d( "[MyReceiver] 接收到推送下来的通知");
			int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
			Logger.d( "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);

		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
			Logger.d( "[MyReceiver] 用户点击打开了通知");
//			Intent i = new Intent(context, MainActivity.class);
//			i.putExtras(bundle);
//			//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
//			context.startActivity(i);

		} else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
			Logger.d( "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
			//在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

		} else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
			boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
			Logger.d( "[MyReceiver]" + intent.getAction() +" connected state change to "+connected);
		} else {
			Logger.d( "[MyReceiver] Unhandled intent - " + intent.getAction());
		}
	}

	public void triggerEvent(Object event) {
		EventBus.getDefault().post(event);
	}

	private void updateClienId(String cid) {
		UserClient.updateUserPush(cid, null);
	}

	private boolean isRunningForeground(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		if (currentPackageName != null && currentPackageName.equals(context.getPackageName())) {
			return true;
		}
		return false;
	}

	private void vibrate(Context context) {
		Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
		long[] pattern = {100, 400, 100, 400};
		vibrator.vibrate(pattern, -1);
	}

	private void onRecMsg(ApplicantEntity applicantEntity, Context context) {
		MessageEntity recvMessage = ProtoBuf2JavaBean.getMessageEntity(applicantEntity);
		int loginId = loginSp.getLoginIdentity().getLoginId();
		boolean isSend = recvMessage.isSend(loginId);
		recvMessage.buildSessionKey(isSend);
		recvMessage.setStatus(MessageConstant.MSG_SUCCESS);
		/**对于混合消息，未读消息计数还是1,session已经更新*/

		add(recvMessage, context);
	}

	public void add(MessageEntity msg, Context context) {
		//更新session list中的msg信息
		//更新未读消息计数
		if (msg == null) {
			Logger.e("unread#unreadMgr#add msg is null!");
			return;
		}
		// isFirst场景:出现一条未读消息，出现小红点，需要触发 [免打扰的情况下]
		boolean isFirst = false;

		UnreadEntity unreadEntity;
		int loginId = loginSp.getLoginIdentity().getLoginId();
		String sessionKey = msg.getSessionKey();
		Logger.e(sessionKey);
		boolean isSend = msg.isSend(loginId);
		if (isSend) {
			IMNotificationManager.instance().cancelSessionNotifications(sessionKey);
			return;
		}

		if (unreadMsgMap.containsKey(sessionKey)) {
			unreadEntity = unreadMsgMap.get(sessionKey);
			// 判断最后一条msgId是否相同
			if (unreadEntity.getLaststMsgId() == msg.getMsgId()) {
				return;
			}
			unreadEntity.setUnReadCnt(unreadEntity.getUnReadCnt() + 1);
		} else {
			isFirst = true;
			unreadEntity = new UnreadEntity();
			unreadEntity.setUnReadCnt(1);
			unreadEntity.setPeerId(msg.getPeerId(isSend));
			unreadEntity.setSessionType(msg.getSessionType());
			unreadEntity.buildSessionKey();
		}
		unreadEntity.setLatestMsgData(msg.getMessageDisplay());
		unreadEntity.setLaststMsgId(msg.getMsgId());
		addIsForbidden(context, unreadEntity);

		/**放入manager 状态中*/
		unreadMsgMap.put(unreadEntity.getSessionKey(), unreadEntity);
		/**没有被屏蔽才会发送广播*/
		if (!unreadEntity.isForbidden() || isFirst) {
			handleMsgRecv(unreadEntity, context);
		}
	}

	private void addIsForbidden(Context context, UnreadEntity unreadEntity) {
		if (unreadEntity.getSessionType() == DBConstant.SESSION_TYPE_GROUP) {
			GroupEntity groupEntity = findGroup(context, unreadEntity.getPeerId());
			if (groupEntity != null && groupEntity.getStatus() == DBConstant.GROUP_STATUS_SHIELD) {
				unreadEntity.setForbidden(true);
			}
		}
	}

	private GroupEntity findGroup(Context context, int groupId) {
		if (!dbInterface.isInitOK()) {
			Logger.e("" + loginSp.getLoginIdentity().getLoginId());
			dbInterface.initDbHelp(context, loginSp.getLoginIdentity().getLoginId());
		}

		return dbInterface.getByGroupId(groupId);
	}

	private UserEntity findUser(Context context, int userId) {
		if (!dbInterface.isInitOK()) {
			Logger.e("" + loginSp.getLoginIdentity().getLoginId());
			dbInterface.initDbHelp(context, loginSp.getLoginIdentity().getLoginId());
		}

		return dbInterface.getByFriendId(userId);
	}

	private void handleMsgRecv(UnreadEntity entity, Context context) {
		Logger.e("notification#recv unhandled message");
		int peerId = entity.getPeerId();
		int sessionType = entity.getSessionType();

		//判断是否设定了免打扰
		if (entity.isForbidden()) {
			Logger.e("notification#GROUP_STATUS_SHIELD");
			return;
		}

		//PC端是否登陆 取消 【暂时先关闭】
//        if(IMLoginManager.instance().isPcOnline()){
//            Logger.d("notification#isPcOnline");
//            return;
//        }
		ConfigurationSp configurationSp = ConfigurationSp.instance(context, loginSp.getLoginIdentity().getLoginId());
		// 全局开关
		boolean globallyOnOff = configurationSp.getCfg(SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.NOTIFICATION);
		if (globallyOnOff) {
			Logger.e("notification#shouldGloballyShowNotification is false, return");
			return;
		}

		// 单独的设置
		boolean singleOnOff = configurationSp.getCfg(entity.getSessionKey(), ConfigurationSp.CfgDimension.NOTIFICATION);
		if (singleOnOff) {
			Logger.e("notification#shouldShowNotificationBySession is false, return");
			return;
		}

		//if the message is a multi login message which send from another terminal,not need notificate to status bar
		// 判断是否是自己的消息
		if (loginSp.getLoginIdentity().getLoginId() != peerId) {
			showNotification(entity, context, configurationSp);
		}
	}

	private void showNotification(final UnreadEntity unreadEntity, final Context context, final ConfigurationSp configurationSp) {
		// todo eric need to set the exact size of the big icon
		// 服务端有些特定的支持 尺寸是不是要调整一下 todo 100*100  下面的就可以不要了
		ImageSize targetSize = new ImageSize(80, 80);
		int peerId = unreadEntity.getPeerId();
		String avatarUrl = "";
		String title = "";
		String content = unreadEntity.getLatestMsgData();
		String unit = context.getString(R.string.msg_cnt_unit);
		int totalUnread = unreadEntity.getUnReadCnt();

		if (unreadEntity.getSessionType() == DBConstant.SESSION_TYPE_SINGLE) {
			UserEntity contact = findUser(context, peerId);
			if (contact != null) {
				title = contact.getMainName();
				avatarUrl = contact.getAvatar();
			} else {
				title = "User_" + peerId;
				avatarUrl = "";
			}

		} else {
			GroupEntity group = findGroup(context, peerId);
			if (group != null) {
				title = group.getMainName();
				avatarUrl = group.getAvatar();
			} else {
				title = "Group_" + peerId;
				avatarUrl = "";
			}
		}
		//获取头像
		avatarUrl = IMUIHelper.getRealAvatarUrl(avatarUrl);
		final String ticker = String.format("[%d%s]%s: %s", totalUnread, unit, title, content);
		final int notificationId = getSessionNotificationId(unreadEntity.getSessionKey());
		final Intent intent = new Intent(context, LoginActivity.class);

		Logger.e("notification#notification avatarUrl:%s", avatarUrl);
		final String finalTitle = title;
		ImageLoader.getInstance().loadImage(avatarUrl, targetSize, null, new SimpleImageLoadingListener() {

			@Override
			public void onLoadingComplete(String imageUri, View view,
										  Bitmap loadedImage) {
				Logger.e("notification#icon onLoadComplete");
				// holder.image.setImageBitmap(loadedImage);
				showInNotificationBar(configurationSp, context, finalTitle, ticker, loadedImage, notificationId, intent);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view,
										FailReason failReason) {
				Logger.e("notification#icon onLoadFailed");
				// 服务器支持的格式有哪些
				// todo eric default avatar is too small, need big size(128 * 128)
				Bitmap defaultBitmap = BitmapFactory.decodeResource(context.getResources(), IMUIHelper.getDefaultAvatarResId(unreadEntity.getSessionType()));
				showInNotificationBar(configurationSp, context, finalTitle, ticker, defaultBitmap, notificationId, intent);
			}
		});
	}

	private long hashBKDR(String str) {
		long seed = 131; // 31 131 1313 13131 131313 etc..
		long hash = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = (hash * seed) + str.charAt(i);
		}
		return hash;
	}

	public int getSessionNotificationId(String sessionKey) {
		int hashedNotificationId = (int) hashBKDR(sessionKey);
		return hashedNotificationId;
	}

	private void showInNotificationBar(ConfigurationSp configurationSp, Context context, String title, String ticker, Bitmap iconBitmap, int notificationId, Intent intent) {

		NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notifyMgr == null) {
			return;
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle(title);
		builder.setContentText(ticker);
		builder.setSmallIcon(R.drawable.tt_small_icon);
		builder.setTicker(ticker);
		builder.setWhen(System.currentTimeMillis());
		builder.setAutoCancel(true);

		// this is the content near the right bottom side
		// builder.setContentInfo("content info");

		if (configurationSp.getCfg(SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.VIBRATION)) {
			// delay 0ms, vibrate 200ms, delay 250ms, vibrate 200ms
			long[] vibrate = {0, 200, 250, 200};
			builder.setVibrate(vibrate);
		} else {
			Logger.d("notification#setting is not using vibration");
		}

		// sound
		if (configurationSp.getCfg(SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.SOUND)) {
			builder.setDefaults(Notification.DEFAULT_SOUND);
		} else {
			Logger.d("notification#setting is not using sound");
		}
		if (iconBitmap != null) {
			Logger.d("notification#fetch icon from network ok");
			builder.setLargeIcon(iconBitmap);
		} else {
			// do nothint ?
		}
		// if MessageActivity is in the background, the system would bring it to
		// the front
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		Notification notification = builder.build();
		notifyMgr.notify(notificationId, notification);
	}
}
