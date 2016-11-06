package com.dtalk.dd.imservice.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.view.View;

import com.dtalk.dd.DB.entity.GroupEntity;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.DB.sp.ConfigurationSp;
import com.dtalk.dd.R;
import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.config.IntentConstant;
import com.dtalk.dd.config.SysConstant;
import com.dtalk.dd.imservice.entity.UnreadEntity;
import com.dtalk.dd.imservice.event.GroupEvent;
import com.dtalk.dd.imservice.event.UnreadEvent;
import com.dtalk.dd.ui.activity.MessageActivity;
import com.dtalk.dd.utils.IMUIHelper;
import com.dtalk.dd.utils.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import de.greenrobot.event.EventBus;

/**
 * 伪推送; app退出之后就不会收到推送的信息
 * 通知栏新消息通知
 * a.每个session 只显示一条
 * b.每个msg 信息都显示
 * 配置依赖与 configure
 */
public class IMNotificationManager extends IMManager{

	private static IMNotificationManager inst = new IMNotificationManager();
	public static IMNotificationManager instance() {
			return inst;
	}
    private ConfigurationSp configurationSp;

	private IMNotificationManager() {
	}

    @Override
    public void doOnStart() {
        cancelAllNotifications();
    }

    public void onLoginSuccess(){
        int loginId = IMLoginManager.instance().getLoginId();
        configurationSp = ConfigurationSp.instance(ctx,loginId);
        if(!EventBus.getDefault().isRegistered(inst)){
            EventBus.getDefault().register(inst);
        }
    }

    public void reset() {
        EventBus.getDefault().unregister(this);
        cancelAllNotifications();
    }


    public void onEventMainThread(UnreadEvent event){
        switch (event.event){
            case UNREAD_MSG_RECEIVED:
//                UnreadEntity unreadEntity = event.entity;
//                handleMsgRecv(unreadEntity);
                break;
        }
    }

    // 屏蔽群，相关的通知全部删除
    public void onEventMainThread(GroupEvent event){
        GroupEntity gEntity = event.getGroupEntity();
        if(event.getEvent()== GroupEvent.Event.SHIELD_GROUP_OK){
            if(gEntity == null){
                return;
            }
            cancelSessionNotifications(gEntity.getSessionKey());
        }
    }

    private void handleMsgRecv(UnreadEntity entity) {
        Logger.d("notification#recv unhandled message");
        int peerId = entity.getPeerId();
        int sessionType =  entity.getSessionType();

        //判断是否设定了免打扰
        if(entity.isForbidden()){
               Logger.d("notification#GROUP_STATUS_SHIELD");
               return;
        }

        //PC端是否登陆 取消 【暂时先关闭】
//        if(IMLoginManager.instance().isPcOnline()){
//            Logger.d("notification#isPcOnline");
//            return;
//        }

        // 全局开关
        boolean  globallyOnOff = configurationSp.getCfg(SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.NOTIFICATION);
        if (globallyOnOff) {
            Logger.d("notification#shouldGloballyShowNotification is false, return");
            return;
        }

        // 单独的设置
        boolean singleOnOff = configurationSp.getCfg(entity.getSessionKey(),ConfigurationSp.CfgDimension.NOTIFICATION);
        if (singleOnOff) {
            Logger.d("notification#shouldShowNotificationBySession is false, return");
            return;
        }

        //if the message is a multi login message which send from another terminal,not need notificate to status bar
        // 判断是否是自己的消息
        if(IMLoginManager.instance().getLoginId() != peerId){
             showNotification(entity);
        }
    }


	public void cancelAllNotifications() {
		Logger.d("notification#cancelAllNotifications");
        if(null == ctx){
            return;
        }
		NotificationManager notifyMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notifyMgr == null) {
			return;
		}
		notifyMgr.cancelAll();
	}


    /**
     * 在通知栏中删除特定回话的状态
     * @param sessionKey
     */
    public void cancelSessionNotifications(String sessionKey) {
        Logger.d("notification#cancelSessionNotifications");
        NotificationManager notifyMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null == notifyMgr) {
            return;
        }
        int notificationId = getSessionNotificationId(sessionKey);
        notifyMgr.cancel(notificationId);
    }


	private void showNotification(final UnreadEntity unreadEntity) {
		// todo eric need to set the exact size of the big icon
        // 服务端有些特定的支持 尺寸是不是要调整一下 todo 100*100  下面的就可以不要了
		ImageSize targetSize = new ImageSize(80, 80);
        int peerId = unreadEntity.getPeerId();
        int sessionType = unreadEntity.getSessionType();
        String avatarUrl = "";
        String title = "";
        String content = unreadEntity.getLatestMsgData();
        String unit = ctx.getString(R.string.msg_cnt_unit);
        int totalUnread = unreadEntity.getUnReadCnt();

        if(unreadEntity.getSessionType() == DBConstant.SESSION_TYPE_SINGLE){
            UserEntity contact = IMContactManager.instance().findContact(peerId);
            if(contact !=null){
                title = contact.getMainName();
                avatarUrl = contact.getAvatar();
            }else{
                title = "User_"+peerId;
                avatarUrl = "";
            }

        }else{
            GroupEntity group = IMGroupManager.instance().findGroup(peerId);
            if(group !=null){
                title = group.getMainName();
                avatarUrl = group.getAvatar();
            }else{
                title = "Group_"+peerId;
                avatarUrl = "";
            }
        }
        //获取头像
		avatarUrl = IMUIHelper.getRealAvatarUrl(avatarUrl);
        final String ticker = String.format("[%d%s]%s: %s", totalUnread, unit, title, content);
        final int notificationId = getSessionNotificationId(unreadEntity.getSessionKey());
        final Intent intent = new Intent(ctx, MessageActivity.class);
        intent.putExtra(IntentConstant.KEY_SESSION_KEY, unreadEntity.getSessionKey());

        Logger.d("notification#notification avatarUrl:" + avatarUrl);
        final String finalTitle = title;
        ImageLoader.getInstance().loadImage(avatarUrl, targetSize, null, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view,
                                          Bitmap loadedImage) {
                Logger.d("notification#icon onLoadComplete");
                // holder.image.setImageBitmap(loadedImage);
                showInNotificationBar(finalTitle,ticker,loadedImage,notificationId,intent);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view,
                                        FailReason failReason) {
                Logger.d("notification#icon onLoadFailed");
                // 服务器支持的格式有哪些
                // todo eric default avatar is too small, need big size(128 * 128)
                Bitmap defaultBitmap = BitmapFactory.decodeResource(ctx.getResources(), IMUIHelper.getDefaultAvatarResId(unreadEntity.getSessionType()));
                showInNotificationBar(finalTitle,ticker,defaultBitmap,notificationId,intent);
            }
        });
	}

	private void showInNotificationBar(String title,String ticker, Bitmap iconBitmap,int notificationId,Intent intent) {

		NotificationManager notifyMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		if (notifyMgr == null) {
			return;
		}

		Builder builder = new NotificationCompat.Builder(ctx);
		builder.setContentTitle(title);
		builder.setContentText(ticker);
		builder.setSmallIcon(R.drawable.tt_small_icon);
		builder.setTicker(ticker);
		builder.setWhen(System.currentTimeMillis());
		builder.setAutoCancel(true);

		// this is the content near the right bottom side
		// builder.setContentInfo("content info");

		if (configurationSp.getCfg(SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.VIBRATION)) {
			// delay 0ms, vibrate 200ms, delay 250ms, vibrate 200ms
			long[] vibrate = {0, 200, 250, 200};
			builder.setVibrate(vibrate);
		} else {
			Logger.d("notification#setting is not using vibration");
		}

		// sound
		if (configurationSp.getCfg(SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.SOUND)) {
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
		PendingIntent pendingIntent = PendingIntent.getActivity(ctx, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		Notification notification = builder.build();
		notifyMgr.notify(notificationId, notification);
	}

	// come from
	// http://www.partow.net/programming/hashfunctions/index.html#BKDRHashFunction
	private long hashBKDR(String str) {
		long seed = 131; // 31 131 1313 13131 131313 etc..
		long hash = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = (hash * seed) + str.charAt(i);
		}
		return hash;
	}

	/* End Of BKDR Hash Function */
	public int getSessionNotificationId(String sessionKey) {
		int hashedNotificationId = (int) hashBKDR(sessionKey);
		return hashedNotificationId;
	}
}
