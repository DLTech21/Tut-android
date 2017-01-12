package com.dtalk.dd.ui.widget.message;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dtalk.dd.DB.entity.MessageEntity;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.config.MessageConstant;
import com.dtalk.dd.ui.plugin.ImageLoadManager;
import com.dtalk.dd.utils.IMUIHelper;
import com.dtalk.dd.ui.widget.IMBaseImageView;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.SandboxUtils;

/**
 * @author : yingmu on 15-1-9.
 * @email : yingmu@mogujie.com.
 */
public abstract class BaseMsgRenderView extends RelativeLayout {
    /**
     * 头像
     */
    protected IMBaseImageView portrait;
    /**
     * 消息状态
     */
    protected ImageView messageFailed;
    protected ProgressBar loadingProgressBar;
    protected TextView name;

    /**
     * 渲染的消息实体
     */
    protected MessageEntity messageEntity;
    protected ViewGroup parentView;
    protected boolean isMine;

    protected BaseMsgRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 渲染之后做的事情 子类会调用到这个地方嘛?
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        portrait = (IMBaseImageView) findViewById(R.id.user_portrait);
        messageFailed = (ImageView) findViewById(R.id.message_state_failed);
        loadingProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        name = (TextView) findViewById(R.id.name);
    }


    // 消息失败绑定事件 三种不同的弹窗
    // image的load状态就是 sending状态的一个子状态
    public void msgSendinging(final MessageEntity messageEntity) {
        messageFailed.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    public void msgFailure(final MessageEntity messageEntity) {
        messageFailed.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.GONE);
    }

    public void msgSuccess(final MessageEntity messageEntity) {
        messageFailed.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);
    }

    public void msgStatusError(final MessageEntity messageEntity) {
        messageFailed.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.GONE);
    }

    /**
     * 控件赋值
     */
    public void render(final MessageEntity entity, UserEntity userEntity, final Context ctx) {
        this.messageEntity = entity;
        if(userEntity == null){
            userEntity=new UserEntity();
            userEntity.setMainName("");
            userEntity.setRealName("");
        }

        String avatar = userEntity.getAvatar();
        int msgStatus = messageEntity.getStatus();

        //头像设置
        portrait.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        portrait.setCorner(5);
        portrait.setImageUrl(avatar);
        // 设定姓名 应该消息都是有的
        if (!isMine) {
            name.setText(userEntity.getMainName());
            name.setVisibility(View.GONE);
        } else {
            try {
                avatar = SandboxUtils.getInstance().getUser().getAvatar();
                ImageLoadManager.setCircleAvatarGlide(IMApplication.getInstance(), avatar, portrait);
            } catch (Exception e) {
                Logger.e(e);
            }
        }
        /**头像的跳转事件暂时放在这里， todo 业务结合紧密，但是应该不会改了*/
        final int userId = userEntity.getPeerId();
        portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMUIHelper.openUserProfileActivity(getContext(), userId);
            }
        });
        /**头像事件 end*/

        // 设定三种信息的弹窗类型
        // 判定消息的状态 成功还是失败  todo 具体实现放在子类中
        switch (msgStatus) {
            case MessageConstant.MSG_FAILURE:
                msgFailure(messageEntity);
                break;
            case MessageConstant.MSG_SUCCESS:
                msgSuccess(messageEntity);
                break;
            case MessageConstant.MSG_SENDING:
                msgSendinging(messageEntity);
                break;
            default:
                msgStatusError(messageEntity);
        }

        // 如果消息还是处于loading 状态
        // 判断是否是image类型  image类型的才有 loading，其他的都GONE
        // todo 上面这些由子类做
        // 消息总体的类型有两种
        // 展示类型有四种(图片、文字、语音、混排)
    }

    /**
     * -------------------------set/get--------------------------
     */
    public ImageView getPortrait() {
        return portrait;
    }

    public ImageView getMessageFailed() {
        return messageFailed;
    }


    public ProgressBar getLoadingProgressBar() {
        return loadingProgressBar;
    }

    public TextView getName() {
        return name;
    }

}
