package com.dtalk.dd.ui.widget.circle;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.style.DynamicDrawableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dtalk.dd.R;
import com.dtalk.dd.http.moment.Comment;
import com.dtalk.dd.http.moment.Moment;
import com.dtalk.dd.http.user.UserInfo;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.ui.plugin.ImageLoadManager;
import com.dtalk.dd.ui.widget.IMBaseImageView;
import com.dtalk.dd.ui.widget.Lu_Comment_TextView;
import com.dtalk.dd.ui.widget.Lu_PingLunLayout;
import com.dtalk.dd.ui.widget.praisewidget.bean.PraiseBean;
import com.dtalk.dd.ui.widget.praisewidget.widget.PraiseWidget;
import com.dtalk.dd.utils.DateUtil;
import com.dtalk.dd.utils.IMUIHelper;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Donal on 16/7/29.
 */
public class BaseCircleRenderView extends RelativeLayout {
    /**
     * 头像
     */
    protected IMBaseImageView portrait;
    protected TextView name;
    protected TextView content;
    protected TextView tvTime;
    protected TextView tvDelete;
    protected TextView tvMore;
    protected Lu_PingLunLayout layComment;
    protected PraiseWidget layPraise;

    /**
     * 渲染的消息实体
     */
    protected Moment moment;
    protected int position;
    protected ViewGroup parentView;
    protected boolean isMine;
    protected OnMoreCircleListener onMoreCircleListener;
    protected OnDeleteCircleListener onDeleteCircleListener;

    private int mEmojiconSize;
    private int mEmojiconAlignment;
    private int mEmojiconTextSize;
    private int mTextStart = 0;
    private int mTextLength = -1;

    protected BaseCircleRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Emojicon);
            mEmojiconAlignment = a.getInt(R.styleable.Emojicon_emojiconAlignment, DynamicDrawableSpan.ALIGN_BASELINE);
            mTextStart = a.getInteger(R.styleable.Emojicon_emojiconTextStart, 0);
            mTextLength = a.getInteger(R.styleable.Emojicon_emojiconTextLength, -1);
            a.recycle();
        }
    }

    // 渲染之后做的事情 子类会调用到这个地方嘛?
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        portrait = (IMBaseImageView) findViewById(R.id.user_portrait);
        name = (TextView) findViewById(R.id.name);
        content = (TextView) findViewById(R.id.tv_content);
        mEmojiconSize = (int) content.getTextSize();
        tvTime = (TextView) findViewById(R.id.date);
        tvDelete = (TextView) findViewById(R.id.delete);
        tvMore = (TextView) findViewById(R.id.btn_more);
        layComment = (Lu_PingLunLayout) findViewById(R.id.commentLayout);
        layPraise = (PraiseWidget) findViewById(R.id.praise_widget);
    }

    /**
     * 控件赋值
     */
    public void render(final Moment moment, final Context ctx, final int position, boolean isSelf) {
        this.moment = moment;
        this.position = position;
        String avatar = moment.avatar;

        portrait.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        portrait.setCorner(5);
        portrait.setImageUrl(avatar);
        name.setText(moment.nickname);
        name.setVisibility(View.VISIBLE);


        /**头像的跳转事件暂时放在这里， todo 业务结合紧密，但是应该不会改了*/
        final int userId = Integer.valueOf(moment.uid);
        portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMUIHelper.openUserProfileActivity(getContext(), userId);
            }
        });
        /**头像事件 end*/

        long timeStamp = Long.valueOf(moment.created);
        Date msgTimeDate = new Date(timeStamp * 1000);
        tvTime.setText(DateUtil.getTimeDiffDesc(msgTimeDate));

        if (String.valueOf(IMLoginManager.instance().getLoginId()).equals(moment.uid)) {
            getTvDelete().setVisibility(VISIBLE);
        } else {
            getTvDelete().setVisibility(INVISIBLE);
        }
        getTvDelete().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onDeleteCircleListener != null) {
                    onDeleteCircleListener.onDeleteCircle(moment);
                }
            }
        });

        if (isSelf) {
            layPraise.setVisibility(GONE);
            getTvMore().setVisibility(GONE);
            layComment.setVisibility(GONE);
            return;
        }

        Lu_Comment_TextView temp = new Lu_Comment_TextView(ctx);
        final List<Lu_Comment_TextView.Lu_PingLun_info_Entity> mList = new ArrayList<Lu_Comment_TextView.Lu_PingLun_info_Entity>();
        for (Comment comment : moment.comment) {
            if (!comment.uid.equals(comment.reply_uid)) {
                Lu_Comment_TextView.Lu_PingLun_info_Entity mEntity = temp.getLu_pingLun_info_entity(comment.comment_id, comment.uid, comment.nickname, comment.avatar, comment.reply_uid, comment.reply_nickname, comment.avatar, comment.content);
                mList.add(mEntity);
            } else {
                Lu_Comment_TextView.Lu_PingLun_info_Entity mEntity = temp.getLu_pingLun_info_entity(comment.comment_id, comment.uid, comment.nickname, comment.avatar, null, null, null, comment.content);
                mList.add(mEntity);
            }
        }
        layComment.setEntities(mList, new Lu_PingLunLayout.Lu_PingLunLayoutListener() {
            @Override
            public void onNameClickListener(String onClickID, String onClickName, String onClickLogo, Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity, int FuncPosition, int itemPosition) {
                IMUIHelper.openUserProfileActivity(getContext(), Integer.valueOf(onClickID));
            }

            @Override
            public void onTextClickListener(String onClickText, Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity, int FuncPosition, int itemPosition) {
                Logger.d("onTextClickListener = [" + onClickText + "], mLu_pingLun_info_entity = [" + mLu_pingLun_info_entity + "], FuncPosition = [" + FuncPosition + "], itemPosition = [" + itemPosition + "]");
            }

            @Override
            public void onClickOtherListener(Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity, int itemPosition) {
                Logger.d("onClickOtherListener = [" + mLu_pingLun_info_entity + "], itemPosition = [" + itemPosition + "]");
            }

            @Override
            public void onLongClickListener(Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity, int itemPosition) {
                Logger.d("onLongClickListener = [" + mLu_pingLun_info_entity + "], itemPosition = [" + itemPosition + "]");
            }

            @Override
            public void onClickListener(Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity, int itemPosition) {
                Logger.d("onClickListener = [" + mLu_pingLun_info_entity + "], itemPosition = [" + itemPosition + "]");
                onMoreCircleListener.onCommentClick(moment, position, itemPosition, mLu_pingLun_info_entity);
            }
        });

        List<PraiseBean> praiseBeanList = new ArrayList<>();
        for (UserInfo userInfo : moment.like_maps.values()) {
            PraiseBean bean = new PraiseBean();
            bean.userNick = userInfo.getNickname();
            bean.userId = userInfo.getUid();
            praiseBeanList.add(bean);
        }
        layPraise.setPraiseWidgetListener(new PraiseWidget.PraiseWidgetListener() {
            @Override
            public void onNameClickListener(String onClickID, String onClickName) {
                IMUIHelper.openUserProfileActivity(getContext(), Integer.valueOf(onClickID));
            }
        });
        if (praiseBeanList.size() > 0) {
            layPraise.setVisibility(VISIBLE);
            layPraise.setDataByArray(praiseBeanList);
        } else {
            layPraise.setVisibility(GONE);
        }
    }

    public int getmEmojiconSize() {
        return mEmojiconSize;
    }

    public int getmEmojiconAlignment() {
        return mEmojiconAlignment;
    }

    public int getmEmojiconTextSize() {
        return mEmojiconTextSize;
    }

    public int getmTextStart() {
        return mTextStart;
    }

    public int getmTextLength() {
        return mTextLength;
    }

    /**
     * -------------------------set/get--------------------------
     */

    public ImageView getPortrait() {
        return portrait;
    }

    public TextView getName() {
        return name;
    }

    public TextView getContent() {
        return content;
    }

    public TextView getTvTime() {
        return tvTime;
    }

    public TextView getTvDelete() {
        return tvDelete;
    }

    public TextView getTvMore() {
        return tvMore;
    }

    public OnDeleteCircleListener getOnDeleteCircleListener() {
        return onDeleteCircleListener;
    }

    public void setOnDeleteCircleListener(OnDeleteCircleListener onDeleteCircleListener) {
        this.onDeleteCircleListener = onDeleteCircleListener;
    }

    public void setImageGlide(Context mContext, String PicURL, ImageView mImageView) {
        ImageLoadManager.setCircleGlide(mContext, PicURL, mImageView);
    }

    public void setOnMoreCircleListener(OnMoreCircleListener onMoreCircleListener) {
        this.onMoreCircleListener = onMoreCircleListener;
    }

    public interface OnDeleteCircleListener {
        public void onDeleteCircle(Moment moment);
    }

    public interface OnMoreCircleListener {
        public void onFavorClick(Moment moment, int position);

        public void onCommentClick(Moment moment, int position, int itemposition, Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity);
    }
}
