package com.dtalk.dd.ui.widget.message;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dtalk.dd.DB.entity.MessageEntity;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.imservice.entity.FileMessage;
import com.dtalk.dd.ui.widget.GifLoadTask;
import com.dtalk.dd.ui.widget.GifView;

/**
 * Created by Donal on 2016/12/19.
 */

public class GifFileRenderVIew extends BaseMsgRenderView {
    private GifView messageContent;

    public GifView getMessageContent() {
        return messageContent;
    }

    public static GifFileRenderVIew inflater(Context context, ViewGroup viewGroup, boolean isMine) {
        int resource = isMine ? R.layout.tt_mine_giffile_message_item : R.layout.tt_other_giffile_message_item;
        GifFileRenderVIew gifRenderView = (GifFileRenderVIew) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        gifRenderView.setMine(isMine);
        return gifRenderView;
    }

    public GifFileRenderVIew(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        messageContent = (GifView) findViewById(R.id.message_image);
    }

    /**
     * 控件赋值
     *
     * @param messageEntity
     * @param userEntity
     */
    @Override
    public void render(MessageEntity messageEntity, UserEntity userEntity, Context context) {
        super.render(messageEntity, userEntity, context);
        FileMessage fileMessage = (FileMessage) messageEntity;
        String url = fileMessage.getUrl();
        new GifLoadTask() {
            @Override
            protected void onPostExecute(byte[] bytes) {
                messageContent.setBytes(bytes);
                messageContent.startAnimation();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
        }.execute(url);
    }

    @Override
    public void msgFailure(MessageEntity messageEntity) {
        super.msgFailure(messageEntity);
    }

    /**
     * ----------------set/get---------------------------------
     */

    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }


    public void setParentView(ViewGroup parentView) {
        this.parentView = parentView;
    }
}
