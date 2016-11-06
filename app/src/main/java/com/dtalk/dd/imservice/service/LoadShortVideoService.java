package com.dtalk.dd.imservice.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.dtalk.dd.DB.sp.SystemConfigSp;
import com.dtalk.dd.config.SysConstant;
import com.dtalk.dd.imservice.entity.ShortVideoMessage;
import com.dtalk.dd.imservice.event.MessageEvent;
import com.dtalk.dd.utils.FileUtil;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.MoGuHttpClient;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by Donal on 16/6/12.
 */
public class LoadShortVideoService extends IntentService {


    public LoadShortVideoService(){
        super("LoadShortVideoService");
    }

    public LoadShortVideoService(String name) {
        super(name);
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               android.content.Context#startService(android.content.Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        ShortVideoMessage messageInfo = (ShortVideoMessage)intent.getSerializableExtra(SysConstant.UPLOAD_VIDEO_INTENT_PARAMS);
        String result = null;
        try {
            MoGuHttpClient httpClient = new MoGuHttpClient();
            SystemConfigSp.instance().init(getApplicationContext());
            File file= new File(messageInfo.getVideo_path());
            if(file.exists())
            {
                result = httpClient.uploadImage3(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), FileUtil.File2byte(messageInfo.getVideo_path()), messageInfo.getVideo_path());
            }

            if (TextUtils.isEmpty(result)) {
                Logger.i("upload file faild,cause by result is empty/null");
                EventBus.getDefault().post(new MessageEvent(MessageEvent.Event.VIDEO_UPLOAD_FAILD
                        ,messageInfo));
            } else {
                String imageUrl = result;
                messageInfo.setVideo_path_url(imageUrl);
                String result1 = null;
                File file1= new File(messageInfo.getVideo_cover());
                if(file1.exists())
                {
//                    MoGuHttpClient httpClient = new MoGuHttpClient();
//                    SystemConfigSp.instance().init(getApplicationContext());
                    result1 = httpClient.uploadImage3(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), FileUtil.File2byte(messageInfo.getVideo_cover()), messageInfo.getVideo_cover());
                }

                if (TextUtils.isEmpty(result1)) {
                    Logger.i("upload file faild,cause by result is empty/null");
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.Event.VIDEO_UPLOAD_FAILD
                            ,messageInfo));
                } else {
                    String imageUrl1 = result1;
                    messageInfo.setVideo_cover_url(imageUrl1);
                    EventBus.getDefault().post(new MessageEvent(
                            MessageEvent.Event.VIDEO_UPLOAD_SUCCESS
                            ,messageInfo));
                }
            }
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
    }
}