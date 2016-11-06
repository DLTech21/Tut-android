package com.dtalk.dd.imservice.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.dtalk.dd.DB.sp.SystemConfigSp;
import com.dtalk.dd.config.SysConstant;
import com.dtalk.dd.imservice.entity.FileMessage;
import com.dtalk.dd.imservice.event.MessageEvent;
import com.dtalk.dd.utils.FileUtil;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.MoGuHttpClient;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by Donal on 16/4/22.
 */
public class LoadFileService extends IntentService {


    public LoadFileService(){
        super("LoadFileService");
    }

    public LoadFileService(String name) {
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
        FileMessage messageInfo = (FileMessage)intent.getSerializableExtra(SysConstant.UPLOAD_FILE_INTENT_PARAMS);
        String result = null;
        try {
            File file= new File(messageInfo.getPath());
            if(file.exists())
            {
                MoGuHttpClient httpClient = new MoGuHttpClient();
                SystemConfigSp.instance().init(getApplicationContext());
                result = httpClient.uploadImage3(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.MSFSSERVER), FileUtil.File2byte(messageInfo.getPath()), messageInfo.getPath());
            }

            if (TextUtils.isEmpty(result)) {
                Logger.i("upload file faild,cause by result is empty/null");
                EventBus.getDefault().post(new MessageEvent(MessageEvent.Event.FILE_UPLOAD_FAILD
                        ,messageInfo));
            } else {
                String imageUrl = result;
                messageInfo.setUrl(imageUrl);
                EventBus.getDefault().post(new MessageEvent(
                        MessageEvent.Event.FILE_UPLOAD_SUCCESS
                        ,messageInfo));
            }
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
    }
}
