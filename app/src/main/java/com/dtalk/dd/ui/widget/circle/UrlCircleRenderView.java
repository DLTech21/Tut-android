package com.dtalk.dd.ui.widget.circle;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dtalk.dd.R;
import com.dtalk.dd.http.moment.Moment;
import com.dtalk.dd.utils.StringUtils;

import io.github.rockerhieu.emojicon.EmojiconHandler;

/**
 * Created by Donal on 16/7/29.
 */
public class UrlCircleRenderView extends BaseCircleRenderView {
    ImageView imgCover;
    TextView tvTitle;

    public UrlCircleRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static UrlCircleRenderView inflater(Context context,ViewGroup viewGroup){
        int resource = R.layout.circle_url_item;
        UrlCircleRenderView urlCircleRenderView = (UrlCircleRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        urlCircleRenderView.setParentView(viewGroup);
        return urlCircleRenderView;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        imgCover = (ImageView) findViewById(R.id.imgCover);
        tvTitle = (TextView) findViewById(R.id.tvContent);
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
        setImageGlide(ctx, moment.cover, imgCover);
        tvTitle.setText(moment.title);
    }

    public ViewGroup getParentView() {
        return parentView;
    }

    public void setParentView(ViewGroup parentView) {
        this.parentView = parentView;
    }
}
