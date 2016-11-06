package com.dtalk.dd.ui.widget.message;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.dtalk.dd.DB.entity.MessageEntity;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.imservice.entity.LocationEntity;
import com.dtalk.dd.imservice.entity.LocationMessage;
import com.dtalk.dd.ui.widget.BubbleImageView;

/**
 * Created by Donal on 16/3/31.
 *
 * 样式根据mine 与other不同可以分成两个
 */
public class LocationRenderView extends  BaseMsgRenderView {
    /** 文字消息体 */
    private BubbleImageView messageImage;

    public static LocationRenderView inflater(Context context,ViewGroup viewGroup,boolean isMine){
        int resource = isMine?R.layout.tt_mine_location_message_item:R.layout.tt_other_location_message_item;

        LocationRenderView  locationRenderView = (LocationRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        locationRenderView.setMine(isMine);
        locationRenderView.setParentView(viewGroup);
        return locationRenderView;
    }

    public LocationRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        messageImage = (BubbleImageView) findViewById(R.id.message_image);
    }


    /**
     * 控件赋值
     * @param messageEntity
     * @param userEntity
     */
    @Override
    public void render(MessageEntity messageEntity, UserEntity userEntity,Context context) {
        super.render(messageEntity, userEntity, context);
        LocationMessage locationMessage = (LocationMessage) messageEntity;
        String content = locationMessage.getContent();
        LocationEntity locationEntity = new Gson().fromJson(content, LocationEntity.class);
        messageImage.setImageUrl(String.format("http://api.map.baidu.com/staticimage?center=%s,%s&markers=%s&zoom=19.png", locationEntity.getLng(), locationEntity.getLat(), locationEntity.getAddress()));
    }

    @Override
    public void msgFailure(MessageEntity messageEntity) {
        super.msgFailure(messageEntity);
    }

    /**----------------set/get---------------------------------*/
    public ImageView getMessageImage() {
        return messageImage;
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
}
