package com.dtalk.dd.ui.widget.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dtalk.dd.DB.entity.MessageEntity;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.imservice.entity.EmotionMessage;
import com.dtalk.dd.ui.helper.Emoparser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * @author : fengzi on 15-1-25.
 * @email : fengzi@mogujie.com.
 * <p>
 * 虽然gif与text在服务端是同一种消息类型，但是在客户端应该区分开来
 */
public class EmojiRenderView extends BaseMsgRenderView {
    private GifImageView messageContent;

    public static EmojiRenderView inflater(Context context, ViewGroup viewGroup, boolean isMine) {
        int resource = isMine ? R.layout.tt_mine_emoji_message_item : R.layout.tt_other_emoji_message_item;
        EmojiRenderView gifRenderView = (EmojiRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        gifRenderView.setMine(isMine);
        gifRenderView.setParentView(viewGroup);
        return gifRenderView;
    }

    public EmojiRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        messageContent = (GifImageView) findViewById(R.id.message_content);
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
        EmotionMessage textMessage = (EmotionMessage) messageEntity;
        String content = textMessage.getContent();
        try {
            GifDrawable gifFromResource = new GifDrawable( getResources(), Emoparser.getInstance(getContext()).getResIdByCharSequence(content));
            messageContent.setImageDrawable(gifFromResource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void msgFailure(MessageEntity messageEntity) {
        super.msgFailure(messageEntity);
    }


    /**
     * ----------------set/get---------------------------------
     */
    public ImageView getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(GifImageView messageContent) {
        this.messageContent = messageContent;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }

    public ViewGroup getParentView() {
        return parentView;
    }

    public void setParentView(ViewGroup parentView) {
        this.parentView = parentView;
    }

    private byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}

