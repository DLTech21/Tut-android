package com.dtalk.dd.ui.adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.dtalk.dd.DB.entity.MessageEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.config.MessageConstant;
import com.dtalk.dd.http.moment.Moment;
import com.dtalk.dd.imservice.entity.ImageMessage;
import com.dtalk.dd.ui.helper.AudioPlayerHandler;
import com.dtalk.dd.ui.widget.SpeekerToast;
import com.dtalk.dd.ui.widget.circle.BaseCircleRenderView;
import com.dtalk.dd.ui.widget.circle.CircleOperatePopup;
import com.dtalk.dd.ui.widget.circle.CircleType;
import com.dtalk.dd.ui.widget.circle.ImageCircleRenderView;
import com.dtalk.dd.ui.widget.circle.LongtxtCircleRenderView;
import com.dtalk.dd.ui.widget.circle.UrlCircleRenderView;
import com.dtalk.dd.ui.widget.circle.VideoCircleRenderView;
import com.dtalk.dd.ui.widget.message.MessageOperatePopup;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.VideoDisplayLoader;
import com.yixia.camera.demo.ui.record.VideoPlayerActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Donal on 16/7/29.
 */
public class CircleAdapter extends BaseAdapter {
    private CircleOperatePopup currentPop;

    private ArrayList<Moment> circleObjectList = new ArrayList<>();
    private Context ctx;
    private BaseCircleRenderView.OnDeleteCircleListener onDeleteCircleListener;
    private BaseCircleRenderView.OnMoreCircleListener onMoreCircleListener;
    public CircleAdapter(Context ctx, BaseCircleRenderView.OnDeleteCircleListener onDeleteCircleListener, BaseCircleRenderView.OnMoreCircleListener onMoreCircleListener) {
        this.onDeleteCircleListener = onDeleteCircleListener;
        this.onMoreCircleListener = onMoreCircleListener;
        this.ctx = ctx;
        CircleOperatePopup popupView = CircleOperatePopup.instance(ctx);
        currentPop = popupView;
    }


    public void addItem(final Moment msg) {
        circleObjectList.add(0, msg);
        notifyDataSetChanged();

    }

    public void addItemList(final List<Moment> historyList) {
        circleObjectList.addAll(historyList);
        notifyDataSetChanged();
    }

    public void clearAllItem() {
        circleObjectList.clear();
    }


    public ArrayList<Moment> getCircleObjectList() {
        return circleObjectList;
    }

    @Override
    public int getCount() {
        if (null == circleObjectList) {
            return 0;
        } else {
            return circleObjectList.size();
        }
    }

    @Override
    public int getViewTypeCount() {
        return CircleType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        try {
            /**默认是失败类型*/
            CircleType type = CircleType.CIRCLE_TYPE_INVALID;

            Object obj = circleObjectList.get(position);
            Moment info = (Moment) obj;
            if (info.type.equals("txt")) {
                type = CircleType.CIRCLE_TYPE_TEXT;
            } else if (info.type.equals("video")) {
                type = CircleType.CIRCLE_TYPE_VIDEO;
            } else if (info.type.equals("longtxt")) {
                type = CircleType.CIRCLE_TYPE_LONGTXT;
            } else if (info.type.equals("url")) {
                type = CircleType.CIRCLE_TYPE_URL;
            }
            return type.ordinal();
        } catch (Exception e) {
            Logger.e(e.getMessage());
            return CircleType.CIRCLE_TYPE_INVALID.ordinal();
        }
    }

    @Override
    public Object getItem(int position) {
        if (position >= getCount() || position < 0) {
            return null;
        }
        return circleObjectList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 视频类型的render
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    private View videoCircleRender(final int position, View convertView, final ViewGroup parent) {
        final VideoCircleRenderView videoCircleRenderView;
        final Moment moment = (Moment) circleObjectList.get(position);


        if (null == convertView) {
            videoCircleRenderView = VideoCircleRenderView.inflater(ctx, parent);
        } else {
            videoCircleRenderView = (VideoCircleRenderView) convertView;
        }

        videoCircleRenderView.setBtnVideoImageListener(new VideoCircleRenderView.BtnVideoImageListener() {
            @Override
            public void onVideo() {
                videoCircleRenderView.getImageProgress().showProgress();
                videoCircleRenderView.getImagePlay().setVisibility(View.INVISIBLE);
                VideoDisplayLoader.getIns().display(moment.content, new VideoDisplayLoader.VideoDisplayListener() {
                    @Override
                    public void onVideoLoadCompleted(String url, String path) {
                        videoCircleRenderView.getImagePlay().setVisibility(View.VISIBLE);
                        videoCircleRenderView.getImageProgress().hideProgress();
                        ctx.startActivity(new Intent(ctx, VideoPlayerActivity.class).putExtra(
                                "path", path).putExtra(
                                "cover_path", moment.cover).putExtra("justDisplay", true));
                    }
                });
            }
        });
        videoCircleRenderView.getTvMore().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPop.setmListener(new OperateItemClickListener(moment, position));
                currentPop.show(videoCircleRenderView.getTvMore(), true, position);
            }
        });
        videoCircleRenderView.render(moment, ctx, position);
        videoCircleRenderView.setOnDeleteCircleListener(onDeleteCircleListener);
        return videoCircleRenderView;
    }

    /**
     * 图文类型的render
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    private View imageCircleRender(final int position, View convertView, final ViewGroup parent) {
        final ImageCircleRenderView imageCircleRenderView;
        final Moment moment = (Moment) circleObjectList.get(position);


        if (null == convertView) {
            imageCircleRenderView = ImageCircleRenderView.inflater(ctx, parent);
        } else {
            imageCircleRenderView = (ImageCircleRenderView) convertView;
        }
        imageCircleRenderView.getTvMore().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPop.setmListener(new OperateItemClickListener(moment, position));
                currentPop.show(imageCircleRenderView.getTvMore(), true, position);
            }
        });
        imageCircleRenderView.render(moment, ctx, position);
        imageCircleRenderView.setOnDeleteCircleListener(onDeleteCircleListener);
        return imageCircleRenderView;
    }

    /**
     * 链接类型的render
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    private View urlCircleRender(final int position, View convertView, final ViewGroup parent) {
        final UrlCircleRenderView urlCircleRenderView;
        final Moment moment = (Moment) circleObjectList.get(position);


        if (null == convertView) {
            urlCircleRenderView = UrlCircleRenderView.inflater(ctx, parent);
        } else {
            urlCircleRenderView = (UrlCircleRenderView) convertView;
        }
        urlCircleRenderView.getTvMore().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPop.setmListener(new OperateItemClickListener(moment, position));
                currentPop.show(urlCircleRenderView.getTvMore(), true, position);
            }
        });
        urlCircleRenderView.render(moment, ctx, position);
        urlCircleRenderView.setOnDeleteCircleListener(onDeleteCircleListener);
        return urlCircleRenderView;
    }

    /**
     * 长文章类型的render
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    private View longtxtCircleRender(final int position, View convertView, final ViewGroup parent) {
        final LongtxtCircleRenderView longtxtCircleRenderView;
        final Moment moment = (Moment) circleObjectList.get(position);


        if (null == convertView) {
            longtxtCircleRenderView = LongtxtCircleRenderView.inflater(ctx, parent);
        } else {
            longtxtCircleRenderView = (LongtxtCircleRenderView) convertView;
        }
        longtxtCircleRenderView.getTvMore().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPop.setmListener(new OperateItemClickListener(moment, position));
                currentPop.show(longtxtCircleRenderView.getTvMore(), true, position);
            }
        });
        longtxtCircleRenderView.render(moment, ctx, position);
        longtxtCircleRenderView.setOnDeleteCircleListener(onDeleteCircleListener);
        return longtxtCircleRenderView;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            final int typeIndex = getItemViewType(position);

            CircleType renderType = CircleType.values()[typeIndex];
            Logger.e("getView " + position + " " + convertView + " type = " + renderType);
            // 改用map的形式
//            Logger.e("chat#%i", renderType);
            switch (renderType) {
                case CIRCLE_TYPE_INVALID:
                    // 直接返回
                    Logger.e("[fatal erro] render type:MESSAGE_TYPE_INVALID");
                    break;

                case CIRCLE_TYPE_LONGTXT:
                    convertView = longtxtCircleRender(position, convertView, parent);
                    break;
                case CIRCLE_TYPE_TEXT:
                    convertView = imageCircleRender(position, convertView, parent);
                    break;
                case CIRCLE_TYPE_URL:
                    convertView = urlCircleRender(position, convertView, parent);
                    break;
                case CIRCLE_TYPE_VIDEO:
                    convertView = videoCircleRender(position, convertView, parent);
                    break;
            }
            return convertView;
        } catch (Exception e) {
            return null;
        }
    }


    private CircleOperatePopup getPopMenu(ViewGroup parent, CircleOperatePopup.OnItemClickListener listener) {
        CircleOperatePopup popupView = CircleOperatePopup.instance(ctx);
        currentPop = popupView;
        popupView.setOnItemClickListener(listener);
        return popupView;
    }

    private class OperateItemClickListener
            implements
            CircleOperatePopup.OnItemClickListener {

        private Moment mMsgInfo;
        private int mPosition;

        public OperateItemClickListener(Moment msgInfo, int position) {
            mMsgInfo = msgInfo;
            mPosition = position;
        }

        @Override
        public void onFavorClick() {
            onMoreCircleListener.onFavorClick(mMsgInfo, mPosition);
        }

        @Override
        public void onCommentClick() {
            onMoreCircleListener.onCommentClick(mMsgInfo, mPosition, 0, null);
        }
    }


}
