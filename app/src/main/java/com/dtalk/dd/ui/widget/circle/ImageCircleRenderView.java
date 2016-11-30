package com.dtalk.dd.ui.widget.circle;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dtalk.dd.R;
import com.dtalk.dd.http.moment.EaluationListBean;
import com.dtalk.dd.http.moment.Moment;
import com.dtalk.dd.ui.activity.LookBigPicActivity;
import com.dtalk.dd.ui.widget.MultiImageViewLayout;
import com.dtalk.dd.utils.ScreenUtil;
import com.dtalk.dd.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.github.rockerhieu.emojicon.EmojiconHandler;

/**
 * Created by Donal on 16/7/29.
 */
public class ImageCircleRenderView extends BaseCircleRenderView {
    MultiImageViewLayout multiImageViewLayout;

    public static ImageCircleRenderView inflater(Context context,ViewGroup viewGroup){
        int resource = R.layout.circle_image_item;
        ImageCircleRenderView imageCircleRenderView = (ImageCircleRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        imageCircleRenderView.setParentView(viewGroup);
        return imageCircleRenderView;
    }

    public ImageCircleRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        multiImageViewLayout = (MultiImageViewLayout) findViewById(R.id.multiimage);
    }

    @Override
    public void render(Moment moment, final Context ctx, int position, boolean isSelf) {
        super.render(moment, ctx, position, isSelf);
        SpannableStringBuilder builder = new SpannableStringBuilder(moment.content);
        EmojiconHandler.addEmojis(getContext(), builder, getmEmojiconSize(), getmEmojiconAlignment(), getmEmojiconTextSize(), getmTextStart(), getmTextLength(), false);
        getContent().setText(builder);
        if (StringUtils.notEmpty(moment.content)) {
            getContent().setVisibility(VISIBLE);
        }
        else {
            getContent().setVisibility(GONE);
        }

        if (moment.image.size() > 9) {
            List mStrings = new ArrayList();
            for (int i=0;i<9;i++) {
                mStrings.add(moment.image.get(i));
            }
            multiImageViewLayout.setList(mStrings);
        }
        else {
            multiImageViewLayout.setList(moment.image);
        }
        final List<EaluationListBean.EaluationPicBean> mAttachmentsList = new ArrayList<>();
        for (String url:moment.image) {
            EaluationListBean.EaluationPicBean picBean = new EaluationListBean().new EaluationPicBean();
            picBean.imageUrl = url;
            picBean.smallImageUrl = url;
            mAttachmentsList.add(picBean);
        }
        multiImageViewLayout.setOnItemClickListener(new MultiImageViewLayout.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int PressImagePosition, float PressX, float PressY) {

                Intent intent = new Intent(ctx, LookBigPicActivity.class);
                Bundle bundle = new Bundle();

                setupCoords(ctx, view, mAttachmentsList, PressImagePosition);
                bundle.putSerializable(LookBigPicActivity.PICDATALIST, (Serializable) mAttachmentsList);
                intent.putExtras(bundle);
                intent.putExtra(LookBigPicActivity.CURRENTITEM, PressImagePosition);
                ctx.startActivity(intent);

            }

            @Override
            public void onItemLongClick(View view, int PressImagePosition, float PressX, float PressY) {

            }
        });
    }

    public ViewGroup getParentView() {
        return parentView;
    }

    public void setParentView(ViewGroup parentView) {
        this.parentView = parentView;
    }

    /**
     * 计算每个item的坐标
     * @param iv_image
     * @param mAttachmentsList
     * @param position
     */
    private void setupCoords(Context ctx, View iv_image, List<EaluationListBean.EaluationPicBean> mAttachmentsList, int position) {
//        x方向的第几个
        int xn=position%3+1;
//        y方向的第几个
        int yn=position/3+1;
//        x方向的总间距
        int h=(xn-1)* ScreenUtil.instance(ctx).dip2px(4);
//        y方向的总间距
        int v=h;
//        图片宽高
        int height = iv_image.getHeight();
        int width = iv_image.getWidth();
//        获取当前点击图片在屏幕上的坐标
        int[] points=new int[2];
        iv_image.getLocationInWindow(points);
//        获取第一张图片的坐标
        int x0=points[0]-(width+h)*(xn-1) ;
        int y0=points[1]-(height+v)*(yn-1);
//        给所有图片添加坐标信息
        for(int i=0;i<mAttachmentsList.size();i++){
            EaluationListBean.EaluationPicBean ealuationPicBean = mAttachmentsList.get(i);
            ealuationPicBean.width=width;
            ealuationPicBean.height=height;
            ealuationPicBean.x=x0+(i%3)*(width+h);
            ealuationPicBean.y=y0+(i/3)*(height+v)-ScreenUtil.instance(ctx).getStatusBarHeight(iv_image);
        }
    }
}
