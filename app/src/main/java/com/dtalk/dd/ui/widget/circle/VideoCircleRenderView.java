package com.dtalk.dd.ui.widget.circle;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dtalk.dd.R;
import com.dtalk.dd.http.moment.Moment;
import com.dtalk.dd.ui.widget.MGProgressbar;
import com.dtalk.dd.utils.StringUtils;

import io.github.rockerhieu.emojicon.EmojiconHandler;

/**
 * Created by Donal on 16/7/29.
 */
public class VideoCircleRenderView extends BaseCircleRenderView {
    ImageView imgVideoCover;
    /** 图片状态指示*/
    private MGProgressbar imageProgress;

    private ImageView imagePlay;

    private BtnVideoImageListener btnVideoImageListener;

    public VideoCircleRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static VideoCircleRenderView inflater(Context context,ViewGroup viewGroup){
        int resource = R.layout.circle_video_item;
        VideoCircleRenderView videoCircleRenderView = (VideoCircleRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        videoCircleRenderView.setParentView(viewGroup);
        return videoCircleRenderView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        imgVideoCover = (ImageView) findViewById(R.id.iv_video_thumb);
        imageProgress = (MGProgressbar) findViewById(R.id.tt_image_progress);
        imagePlay = (ImageView) findViewById(R.id.message_state_paly);
        imageProgress.setShowText(false);
    }

    @Override
    public void render(Moment moment, Context ctx, int position) {
        super.render(moment, ctx, position);
        SpannableStringBuilder builder = new SpannableStringBuilder(moment.title);
        EmojiconHandler.addEmojis(getContext(), builder, getmEmojiconSize(), getmEmojiconAlignment(), getmEmojiconTextSize(), getmTextStart(), getmTextLength(), false);
        getContent().setText(builder);
        if (StringUtils.notEmpty(moment.title)) {
            getContent().setVisibility(VISIBLE);
        }
        else {
            getContent().setVisibility(GONE);
        }
        setImageGlide(ctx, moment.cover, imgVideoCover);

        imgVideoCover.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnVideoImageListener != null) {
                    btnVideoImageListener.onVideo();
                }
            }
        });
    }

    public interface  BtnVideoImageListener{
        public void onVideo();
    }

    public void setBtnVideoImageListener(BtnVideoImageListener btnVideoImageListener) {
        this.btnVideoImageListener = btnVideoImageListener;
    }

    public MGProgressbar getImageProgress() {
        return imageProgress;
    }

    public ViewGroup getParentView() {
        return parentView;
    }

    public void setParentView(ViewGroup parentView) {
        this.parentView = parentView;
    }

    public ImageView getImagePlay() {
        return imagePlay;
    }


}
