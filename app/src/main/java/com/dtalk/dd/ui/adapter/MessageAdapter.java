package com.dtalk.dd.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dtalk.dd.http.moment.EaluationListBean;
import com.dtalk.dd.imservice.entity.ShortVideoMessage;
import com.dtalk.dd.ui.activity.LookBigPicActivity;
import com.dtalk.dd.ui.fragment.MessageImageFragment;
import com.dtalk.dd.ui.widget.message.GifFileRenderVIew;
import com.dtalk.dd.ui.widget.message.ShortVideoRenderView;
import com.dtalk.dd.utils.ScreenUtil;
import com.dtalk.dd.utils.StringUtils;
import com.dtalk.dd.utils.VideoDisplayLoader;
import com.google.gson.Gson;
import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.DB.entity.MessageEntity;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.config.MessageConstant;
import com.dtalk.dd.imservice.entity.EmotionMessage;
import com.dtalk.dd.imservice.entity.FileMessage;
import com.dtalk.dd.imservice.entity.LocationEntity;
import com.dtalk.dd.imservice.entity.LocationMessage;
import com.dtalk.dd.ui.activity.LocationCheckActivity;
import com.dtalk.dd.ui.helper.AudioPlayerHandler;
import com.dtalk.dd.config.IntentConstant;
import com.dtalk.dd.imservice.entity.AudioMessage;
import com.dtalk.dd.imservice.entity.ImageMessage;
import com.dtalk.dd.imservice.entity.MixMessage;
import com.dtalk.dd.imservice.entity.TextMessage;
import com.dtalk.dd.imservice.service.IMService;
import com.dtalk.dd.ui.activity.PreviewGifActivity;
import com.dtalk.dd.ui.activity.PreviewMessageImagesActivity;
import com.dtalk.dd.ui.activity.PreviewTextActivity;
import com.dtalk.dd.ui.helper.Emoparser;
import com.dtalk.dd.ui.widget.GifView;
import com.dtalk.dd.ui.widget.message.FileRenderView;
import com.dtalk.dd.ui.widget.message.GifImageRenderView;
import com.dtalk.dd.ui.widget.message.LocationRenderView;
import com.dtalk.dd.utils.CommonUtil;
import com.dtalk.dd.utils.DateUtil;
import com.dtalk.dd.utils.FileUtil;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.ui.widget.SpeekerToast;
import com.dtalk.dd.ui.widget.message.AudioRenderView;
import com.dtalk.dd.ui.widget.message.EmojiRenderView;
import com.dtalk.dd.ui.widget.message.ImageRenderView;
import com.dtalk.dd.ui.widget.message.MessageOperatePopup;
import com.dtalk.dd.ui.widget.message.RenderType;
import com.dtalk.dd.ui.widget.message.TextRenderView;
import com.dtalk.dd.ui.widget.message.TimeRenderView;
import com.dtalk.dd.ui.helper.listener.OnDoubleClickListener;
import com.yixia.camera.demo.ui.record.VideoPlayerActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author : yingmu on 15-1-8.
 * @email : yingmu@mogujie.com.
 */
public class MessageAdapter extends BaseAdapter {

    private ArrayList<Object> msgObjectList = new ArrayList<>();

    /**
     * 弹出气泡
     */
    private MessageOperatePopup currentPop;
    private Context ctx;
    /**
     * 依赖整体session状态的
     */
    private UserEntity loginUser;
    private IMService imService;

    public MessageAdapter(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * ----------------------init 的时候需要设定-----------------
     */

    public void setImService(IMService imService, UserEntity loginUser) {
        this.imService = imService;
        this.loginUser = loginUser;
    }

    /**
     * ----------------------添加历史消息-----------------
     */
    public void addItem(final MessageEntity msg) {
        if (msg.getDisplayType() == DBConstant.SHOW_GIF_TYPE) {
            msg.setGIfEmo(true);
        } else {
            msg.setGIfEmo(false);
        }
        int nextTime = msg.getCreated();
        if (getCount() > 0) {
            Object object = msgObjectList.get(getCount() - 1);
            if (object instanceof MessageEntity) {
                int preTime = ((MessageEntity) object).getCreated();
                boolean needTime = DateUtil.needDisplayTime(preTime, nextTime);
                if (needTime) {
                    Integer in = nextTime;
                    msgObjectList.add(in);
                }
            }
        } else {
            Integer in = msg.getCreated();
            msgObjectList.add(in);
        }
        /**消息的判断*/
        if (msg.getDisplayType() == DBConstant.SHOW_MIX_TEXT) {
            MixMessage mixMessage = (MixMessage) msg;
            msgObjectList.addAll(mixMessage.getMsgList());
        } else {
            msgObjectList.add(msg);
        }
        if (msg instanceof ImageMessage) {
            ImageMessage.addToImageMessageList((ImageMessage) msg);
        }
        Logger.d("#messageAdapter#addItem");
        notifyDataSetChanged();

    }

    private boolean isMsgGif(MessageEntity msg) {
        String content = msg.getContent();
        // @YM 临时处理  牙牙表情与消息混合出现的消息丢失
        if (TextUtils.isEmpty(content)
                || !(content.startsWith("[") && content.endsWith("]"))) {
            return false;
        }
        return Emoparser.getInstance(this.ctx).isMessageGif(msg.getContent());
    }

    public MessageEntity getTopMsgEntity() {
        if (msgObjectList.size() <= 0) {
            return null;
        }
        for (Object result : msgObjectList) {
            if (result instanceof MessageEntity) {
                return (MessageEntity) result;
            }
        }
        return null;
    }

    public static class MessageTimeComparator implements Comparator<MessageEntity> {
        @Override
        public int compare(MessageEntity lhs, MessageEntity rhs) {
            if (lhs.getCreated() == rhs.getCreated()) {
                return lhs.getMsgId() - rhs.getMsgId();
            }
            return lhs.getCreated() - rhs.getCreated();
        }
    }

    ;

    /**
     * 下拉载入历史消息,从最上面开始添加
     */
    public void loadHistoryList(final List<MessageEntity> historyList) {
        Logger.d("#messageAdapter#loadHistoryList");
        if (null == historyList || historyList.size() <= 0) {
            return;
        }
        Collections.sort(historyList, new MessageTimeComparator());
        ArrayList<Object> chatList = new ArrayList<>();
        int preTime = 0;
        int nextTime = 0;
        for (MessageEntity msg : historyList) {
            if (msg.getDisplayType() == DBConstant.MSG_TYPE_SINGLE_TEXT) {
                if (isMsgGif(msg)) {
                    msg.setGIfEmo(true);
                } else {
                    msg.setGIfEmo(false);
                }
            }
            nextTime = msg.getCreated();
            boolean needTimeBubble = DateUtil.needDisplayTime(preTime, nextTime);
            if (needTimeBubble) {
                Integer in = nextTime;
                chatList.add(in);
            }
            preTime = nextTime;
            if (msg.getDisplayType() == DBConstant.SHOW_MIX_TEXT) {
                MixMessage mixMessage = (MixMessage) msg;
                chatList.addAll(mixMessage.getMsgList());
            } else {
                chatList.add(msg);
            }
        }
        // 如果是历史消息，从头开始加
        msgObjectList.addAll(0, chatList);
        getImageList();
        Logger.d("#messageAdapter#addItem");
        notifyDataSetChanged();
    }

    /**
     * 获取图片消息列表
     */
    private void getImageList() {
        for (int i = msgObjectList.size() - 1; i >= 0; --i) {
            Object item = msgObjectList.get(i);
            if (item instanceof ImageMessage) {
                ImageMessage.addToImageMessageList((ImageMessage) item);
            }
        }
    }

    /**
     * 临时处理，一定要干掉
     */
    public void hidePopup() {
        if (currentPop != null) {
            currentPop.hidePopup();
        }
    }


    public void clearItem() {
        msgObjectList.clear();
    }

    /**
     * msgId 是消息ID
     * localId是本地的ID
     * position 是list 的位置
     * <p/>
     * 只更新item的状态
     * 刷新单条记录
     * <p/>
     */
    public void updateItemState(int position, final MessageEntity messageEntity) {
        //更新DB
        //更新单条记录
        imService.getDbInterface().insertOrUpdateMessage(messageEntity);
        notifyDataSetChanged();
    }

    /**
     * 对于混合消息的特殊处理
     */
    public void updateItemState(final MessageEntity messageEntity) {
        long dbId = messageEntity.getId();
        int msgId = messageEntity.getMsgId();
        int len = msgObjectList.size();
        for (int index = len - 1; index > 0; index--) {
            Object object = msgObjectList.get(index);
            if (object instanceof MessageEntity) {
                MessageEntity entity = (MessageEntity) object;
                if (object instanceof ImageMessage) {
                    ImageMessage.addToImageMessageList((ImageMessage) object);
                }
                if (entity.getId() == dbId && entity.getMsgId() == msgId) {
                    msgObjectList.set(index, messageEntity);
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (null == msgObjectList) {
            return 0;
        } else {
            return msgObjectList.size();
        }
    }

    @Override
    public int getViewTypeCount() {
        return RenderType.values().length;
    }


    @Override
    public int getItemViewType(int position) {
        try {
            /**默认是失败类型*/
            RenderType type = RenderType.MESSAGE_TYPE_INVALID;

            Object obj = msgObjectList.get(position);
            if (obj instanceof Integer) {
                type = RenderType.MESSAGE_TYPE_TIME_TITLE;
            } else if (obj instanceof MessageEntity) {
                MessageEntity info = (MessageEntity) obj;
                boolean isMine = info.getFromId() == loginUser.getPeerId();

                switch (info.getDisplayType()) {

                    case DBConstant.SHOW_FIEL_TYPE:
                        FileMessage fileMessage = (FileMessage) info;
                        if (fileMessage.getExt().toLowerCase().equals("gif")) {
                            type = isMine ? RenderType.MESSAGE_TYPE_MINE_FILE_GIF
                                    : RenderType.MESSAGE_TYPE_OTHER_FILE_GIF;
                        } else {
                            type = isMine ? RenderType.MESSAGE_TYPE_MINE_FILE
                                    : RenderType.MESSAGE_TYPE_OTHER_FILE;
                        }
                        break;
                    case DBConstant.SHOW_LOCATION_TYPE:
                        type = isMine ? RenderType.MESSAGE_TYPE_MINE_LOCATION
                                : RenderType.MESSAGE_TYPE_OTHER_LOCATION;
                        break;
                    case DBConstant.SHOW_AUDIO_TYPE:
                        type = isMine ? RenderType.MESSAGE_TYPE_MINE_AUDIO
                                : RenderType.MESSAGE_TYPE_OTHER_AUDIO;
                        break;
                    case DBConstant.SHOW_IMAGE_TYPE:
                        ImageMessage imageMessage = (ImageMessage) info;
                        if (CommonUtil.gifCheck(imageMessage.getUrl())) {
                            type = isMine ? RenderType.MESSAGE_TYPE_MINE_GIF_IMAGE
                                    : RenderType.MESSAGE_TYPE_OTHER_GIF_IMAGE;
                        } else {
                            type = isMine ? RenderType.MESSAGE_TYPE_MINE_IMAGE
                                    : RenderType.MESSAGE_TYPE_OTHER_IMAGE;
                        }

                        break;
                    case DBConstant.SHOW_ORIGIN_TEXT_TYPE:
                        type = isMine ? RenderType.MESSAGE_TYPE_MINE_TETX
                                : RenderType.MESSAGE_TYPE_OTHER_TEXT;

                        break;
                    case DBConstant.SHOW_GIF_TYPE:
                        type = isMine ? RenderType.MESSAGE_TYPE_MINE_GIF
                                : RenderType.MESSAGE_TYPE_OTHER_GIF;
                        break;
                    case DBConstant.SHOW_VIDEO_TYPE:
                        type = isMine ? RenderType.MESSAGE_TYPE_MINE_SHORTVIDEO
                                : RenderType.MESSAGE_TYPE_OTHER_SHORTVIDEO;
                        break;
                    case DBConstant.SHOW_MIX_TEXT:
                        //
                        Logger.e("混合的消息类型%s", obj);

                    default:
                        break;
                }
            }
            return type.ordinal();
        } catch (Exception e) {
            Logger.e(e.getMessage());
            return RenderType.MESSAGE_TYPE_INVALID.ordinal();
        }
    }

    @Override
    public Object getItem(int position) {
        if (position >= getCount() || position < 0) {
            return null;
        }
        return msgObjectList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    /**
     * 时间气泡的渲染展示
     */
    private View timeBubbleRender(int position, View convertView, ViewGroup parent) {
        TimeRenderView timeRenderView;
        Integer timeBubble = (Integer) msgObjectList.get(position);
        if (null == convertView) {
            timeRenderView = TimeRenderView.inflater(ctx, parent);
        } else {
            // 不用再使用tag 标签了
            timeRenderView = (TimeRenderView) convertView;
        }
        timeRenderView.setTime(timeBubble);
        return timeRenderView;
    }

    /**
     * 1.头像事件
     * mine:事件 other事件
     * 图片的状态  消息收到，没收到，图片展示成功，没有成功
     * 触发图片的事件  【长按】
     * <p/>
     * 图片消息类型的render
     *
     * @param position
     * @param convertView
     * @param parent
     * @param isMine
     * @return
     */
    private View shortvideoMsgRender(final int position, View convertView, final ViewGroup parent, final boolean isMine) {
        final ShortVideoRenderView shortVideoRenderView;
        final ShortVideoMessage shortVideoMessage = (ShortVideoMessage) msgObjectList.get(position);
        UserEntity userEntity = imService.getContactManager().findContact(shortVideoMessage.getFromId());

        /**保存在本地的path*/
        final String imagePath = shortVideoMessage.getVideo_cover();
        /**消息中的image路径*/
        final String imageUrl = shortVideoMessage.getVideo_cover_url();

        if (null == convertView) {
            shortVideoRenderView = ShortVideoRenderView.inflater(ctx, parent, isMine);
        } else {
            shortVideoRenderView = (ShortVideoRenderView) convertView;
        }

        final ImageView messageImage = shortVideoRenderView.getMessageImage();
        final int msgId = shortVideoMessage.getMsgId();
        shortVideoRenderView.setBtnImageListener(new ShortVideoRenderView.BtnImageListener() {
            @Override
            public void onMsgFailure() {
                /**
                 * 多端同步也不会拉到本地失败的数据
                 * 只有isMine才有的状态，消息发送失败
                 * 1. 图片上传失败。点击图片重新上传??[也是重新发送]
                 * 2. 图片上传成功，但是发送失败。 点击重新发送??
                 */
                if (FileUtil.isSdCardAvailuable()) {
//                    imageMessage.setLoadStatus(MessageStatus.IMAGE_UNLOAD);//如果是图片已经上传成功呢？
                    shortVideoMessage.setStatus(MessageConstant.MSG_SENDING);
                    if (imService != null) {
                        imService.getMessageManager().resendMessage(shortVideoMessage);
                    }
                    updateItemState(msgId, shortVideoMessage);
                } else {
                    Toast.makeText(ctx, ctx.getString(R.string.sdcard_unavaluable), Toast.LENGTH_LONG).show();
                }
            }

            //DetailPortraitActivity 以前用的是DisplayImageActivity 这个类
            @Override
            public void onMsgSuccess() {
                if (StringUtils.notEmpty(shortVideoMessage.getVideo_cover()) && StringUtils.notEmpty(shortVideoMessage.getVideo_path())) {
                    ctx.startActivity(new Intent(ctx, VideoPlayerActivity.class).putExtra(
                            "path", shortVideoMessage.getVideo_path()).putExtra(
                            "cover_path", shortVideoMessage.getVideo_cover()).putExtra("justDisplay", true));
                } else {
                    shortVideoRenderView.getImageProgress().showProgress();
                    shortVideoRenderView.getImagePlay().setVisibility(View.INVISIBLE);
                    VideoDisplayLoader.getIns().display(shortVideoMessage.getVideo_path_url(), new VideoDisplayLoader.VideoDisplayListener() {
                        @Override
                        public void onVideoLoadCompleted(String url, String path) {
                            shortVideoRenderView.getImagePlay().setVisibility(View.VISIBLE);
                            shortVideoRenderView.getImageProgress().hideProgress();
                            ctx.startActivity(new Intent(ctx, VideoPlayerActivity.class).putExtra(
                                    "path", path).putExtra(
                                    "cover_path", shortVideoMessage.getVideo_cover()).putExtra("justDisplay", true));
                        }
                    });
                }
            }
        });

        // 设定触发loadImage的事件
        shortVideoRenderView.setImageLoadListener(new ShortVideoRenderView.ImageLoadListener() {

            @Override
            public void onLoadComplete(String loaclPath) {
                Logger.d("chat#pic#save image ok");
                Logger.d("pic#setsavepath:%s", loaclPath);
//                imageMessage.setPath(loaclPath);//下载的本地路径不再存储
                shortVideoMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_SUCCESS);
                updateItemState(shortVideoMessage);
            }

            @Override
            public void onLoadFailed() {
                Logger.d("chat#pic#onBitmapFailed");
                shortVideoMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_FAILURE);
                updateItemState(shortVideoMessage);
                Logger.d("download failed");
            }
        });

        final View messageLayout = shortVideoRenderView.getMessageLayout();
        shortVideoRenderView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 创建一个pop对象，然后 分支判断状态，然后显示需要的内容
                MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(shortVideoMessage, position));
                boolean bResend = (shortVideoMessage.getStatus() == MessageConstant.MSG_FAILURE)
                        || (shortVideoMessage.getLoadStatus() == MessageConstant.IMAGE_UNLOAD);
                popup.show(messageLayout, DBConstant.SHOW_IMAGE_TYPE, bResend, isMine);
                return true;
            }
        });

        /**父类控件中的发送失败view*/
        shortVideoRenderView.getMessageFailed().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 重发或者重新加载
                MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(shortVideoMessage, position));
                popup.show(messageLayout, DBConstant.SHOW_IMAGE_TYPE, true, isMine);
            }
        });
        shortVideoRenderView.render(shortVideoMessage, userEntity, ctx);

        return shortVideoRenderView;
    }

    /**
     * 1.头像事件
     * mine:事件 other事件
     * 图片的状态  消息收到，没收到，图片展示成功，没有成功
     * 触发图片的事件  【长按】
     * <p/>
     * 图片消息类型的render
     *
     * @param position
     * @param convertView
     * @param parent
     * @param isMine
     * @return
     */
    private View imageMsgRender(final int position, View convertView, final ViewGroup parent, final boolean isMine) {
        final ImageRenderView imageRenderView;
        final ImageMessage imageMessage = (ImageMessage) msgObjectList.get(position);
        UserEntity userEntity = imService.getContactManager().findContact(imageMessage.getFromId());

        /**保存在本地的path*/
        final String imagePath = imageMessage.getPath();
        /**消息中的image路径*/
        final String imageUrl = imageMessage.getUrl();

        if (null == convertView) {
            imageRenderView = ImageRenderView.inflater(ctx, parent, isMine);
        } else {
            imageRenderView = (ImageRenderView) convertView;
        }

        final ImageView messageImage = imageRenderView.getMessageImage();
        final int msgId = imageMessage.getMsgId();
        imageRenderView.setBtnImageListener(new ImageRenderView.BtnImageListener() {
            @Override
            public void onMsgFailure() {
                /**
                 * 多端同步也不会拉到本地失败的数据
                 * 只有isMine才有的状态，消息发送失败
                 * 1. 图片上传失败。点击图片重新上传??[也是重新发送]
                 * 2. 图片上传成功，但是发送失败。 点击重新发送??
                 */
                if (FileUtil.isSdCardAvailuable()) {
//                    imageMessage.setLoadStatus(MessageStatus.IMAGE_UNLOAD);//如果是图片已经上传成功呢？
                    imageMessage.setStatus(MessageConstant.MSG_SENDING);
                    if (imService != null) {
                        imService.getMessageManager().resendMessage(imageMessage);
                    }
                    updateItemState(msgId, imageMessage);
                } else {
                    Toast.makeText(ctx, ctx.getString(R.string.sdcard_unavaluable), Toast.LENGTH_LONG).show();
                }
            }

            //DetailPortraitActivity 以前用的是DisplayImageActivity 这个类
            @Override
            public void onMsgSuccess() {
//                Intent i = new Intent(ctx, PreviewMessageImagesActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putSerializable(IntentConstant.CUR_MESSAGE, imageMessage);
//                i.putExtras(bundle);
//                ctx.startActivity(i);
//                ((Activity) ctx).overridePendingTransition(R.anim.tt_image_enter, R.anim.tt_stay);
                openBigPic(imageMessage, imageRenderView.getMessageImage());
            }
        });

        // 设定触发loadImage的事件
        imageRenderView.setImageLoadListener(new ImageRenderView.ImageLoadListener() {

            @Override
            public void onLoadComplete(String loaclPath) {
                Logger.d("chat#pic#save image ok");
                Logger.d("pic#setsavepath:%s", loaclPath);
//                imageMessage.setPath(loaclPath);//下载的本地路径不再存储
                imageMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_SUCCESS);
                updateItemState(imageMessage);
            }

            @Override
            public void onLoadFailed() {
                Logger.d("chat#pic#onBitmapFailed");
                imageMessage.setLoadStatus(MessageConstant.IMAGE_LOADED_FAILURE);
                updateItemState(imageMessage);
                Logger.d("download failed");
            }
        });

        final View messageLayout = imageRenderView.getMessageLayout();
        imageRenderView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 创建一个pop对象，然后 分支判断状态，然后显示需要的内容
                MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(imageMessage, position));
                boolean bResend = (imageMessage.getStatus() == MessageConstant.MSG_FAILURE)
                        || (imageMessage.getLoadStatus() == MessageConstant.IMAGE_UNLOAD);
                popup.show(messageLayout, DBConstant.SHOW_IMAGE_TYPE, bResend, isMine);
                return true;
            }
        });

        /**父类控件中的发送失败view*/
        imageRenderView.getMessageFailed().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 重发或者重新加载
                MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(imageMessage, position));
                popup.show(messageLayout, DBConstant.SHOW_IMAGE_TYPE, true, isMine);
            }
        });
        imageRenderView.render(imageMessage, userEntity, ctx);

        return imageRenderView;
    }

    private void openBigPic(ImageMessage imageMessage, View view) {
        ArrayList<ImageMessage> imageList = ImageMessage.getImageMessageList();
        int curImagePosition = 0;
        final List<EaluationListBean.EaluationPicBean> mAttachmentsList = new ArrayList<>();
        if (null != imageList && null != imageMessage) {
            for (int i = 0; i < imageList.size(); i++) {
                ImageMessage item = imageList.get(imageList.size() - i - 1);
                if (null == item) {
                    continue;
                }
                EaluationListBean.EaluationPicBean picBean = new EaluationListBean().new EaluationPicBean();
//                String imageUrl = item.getUrl();
//                if (!TextUtils.isEmpty(item.getPath()) && FileUtil.isFileExist(item.getPath())) {
//                    imageUrl = "file://" + item.getPath();
//                }

                picBean.imageUrl = item.getUrl();
                picBean.smallImageUrl = item.getUrl();
                mAttachmentsList.add(picBean);
                if (item.getMsgId() == imageMessage.getMsgId() && imageMessage.getId().equals(item.getId())) {
                    curImagePosition = i;
                }


            }
            Intent intent = new Intent(ctx, LookBigPicActivity.class);
            Bundle bundle = new Bundle();

            setupCoords(ctx, view, mAttachmentsList, curImagePosition);
            bundle.putSerializable(LookBigPicActivity.PICDATALIST, (Serializable) mAttachmentsList);
            intent.putExtras(bundle);
            intent.putExtra(LookBigPicActivity.CURRENTITEM, curImagePosition);
            ctx.startActivity(intent);
        }
    }

    /**
     * 计算每个item的坐标
     *
     * @param iv_image
     * @param mAttachmentsList
     * @param position
     */
    private void setupCoords(Context ctx, View iv_image, List<EaluationListBean.EaluationPicBean> mAttachmentsList, int position) {
//        x方向的第几个
        int xn = 1;
//        y方向的第几个
        int yn = 1;
//        x方向的总间距
        int h = (xn - 1) * ScreenUtil.instance(ctx).dip2px(4);
//        y方向的总间距
        int v = h;
//        图片宽高
        int height = iv_image.getHeight();
        int width = iv_image.getWidth();
//        获取当前点击图片在屏幕上的坐标
        int[] points = new int[2];
        iv_image.getLocationInWindow(points);
//        获取第一张图片的坐标
        int x0 = points[0] - (width + h) * (xn - 1);
        int y0 = points[1] - (height + v) * (yn - 1);
//        给所有图片添加坐标信息
        for (int i = 0; i < mAttachmentsList.size(); i++) {
            EaluationListBean.EaluationPicBean ealuationPicBean = mAttachmentsList.get(i);
            ealuationPicBean.width = width;
            ealuationPicBean.height = height;
            ealuationPicBean.x = x0;
            ealuationPicBean.y = y0 - ScreenUtil.instance(ctx).getStatusBarHeight(iv_image);
        }
    }

    /**
     *
     * @param position
     * @param convertView
     * @param parent
     * @param isMine
     * @return
     */
    private View GifImageMsgRender(final int position, View convertView, final ViewGroup parent, final boolean isMine) {
        GifImageRenderView imageRenderView;
        final ImageMessage imageMessage = (ImageMessage) msgObjectList.get(position);
        UserEntity userEntity = imService.getContactManager().findContact(imageMessage.getFromId());
        if (null == convertView) {
            imageRenderView = GifImageRenderView.inflater(ctx, parent, isMine);
        } else {
            imageRenderView = (GifImageRenderView) convertView;
        }
        GifView imageView = imageRenderView.getMessageContent();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final String url = imageMessage.getUrl();
                Intent intent = new Intent(ctx, PreviewGifActivity.class);
                intent.putExtra(IntentConstant.PREVIEW_TEXT_CONTENT, url);
                ctx.startActivity(intent);
                ((Activity) ctx).overridePendingTransition(R.anim.tt_image_enter, R.anim.tt_stay);
            }
        });
        imageRenderView.render(imageMessage, userEntity, ctx);
        return imageRenderView;
    }

    /**
     * 语音的路径，判断收发的状态
     * 展现的状态
     * 播放动画相关
     * 获取语音的读取状态/
     * 语音长按事件
     *
     * @param position
     * @param convertView
     * @param parent
     * @param isMine
     * @return
     */
    private View audioMsgRender(final int position, View convertView, final ViewGroup parent, final boolean isMine) {
        AudioRenderView audioRenderView;
        final AudioMessage audioMessage = (AudioMessage) msgObjectList.get(position);
        UserEntity entity = imService.getContactManager().findContact(audioMessage.getFromId());
        if (null == convertView) {
            audioRenderView = AudioRenderView.inflater(ctx, parent, isMine);
        } else {
            audioRenderView = (AudioRenderView) convertView;
        }
        final String audioPath = audioMessage.getAudioPath();

        final View messageLayout = audioRenderView.getMessageLayout();
        if (!TextUtils.isEmpty(audioPath)) {
            // 播放的路径为空,这个消息应该如何展示
            messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(audioMessage, position));
                    boolean bResend = audioMessage.getStatus() == MessageConstant.MSG_FAILURE;
                    popup.show(messageLayout, DBConstant.SHOW_AUDIO_TYPE, bResend, isMine);
                    return true;
                }
            });
        }


        audioRenderView.getMessageFailed().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                MessageOperatePopup popup = getPopMenu(parent, new OperateItemClickListener(audioMessage, position));
                popup.show(messageLayout, DBConstant.SHOW_AUDIO_TYPE, true, isMine);
            }
        });


        audioRenderView.setBtnImageListener(new AudioRenderView.BtnImageListener() {
            @Override
            public void onClickUnread() {
                Logger.d("chat#audio#set audio meessage read status");
                audioMessage.setReadStatus(MessageConstant.AUDIO_READED);
                imService.getDbInterface().insertOrUpdateMessage(audioMessage);
            }

            @Override
            public void onClickReaded() {
            }
        });
        audioRenderView.render(audioMessage, entity, ctx);
        return audioRenderView;
    }


    /**
     * text类型的: 1. 设定内容Emoparser
     * 2. 点击事件  单击跳转、 双击方法、长按pop menu
     * 点击头像的事件 跳转
     *
     * @param position
     * @param convertView
     * @param viewGroup
     * @param isMine
     * @return
     */
    private View textMsgRender(final int position, View convertView, final ViewGroup viewGroup, final boolean isMine) {
        TextRenderView textRenderView;
        final TextMessage textMessage = (TextMessage) msgObjectList.get(position);
        UserEntity userEntity = imService.getContactManager().findContact(textMessage.getFromId());

        if (null == convertView) {
            textRenderView = TextRenderView.inflater(ctx, viewGroup, isMine); //new TextRenderView(ctx,viewGroup,isMine);
        } else {
            textRenderView = (TextRenderView) convertView;
        }

        final TextView textView = textRenderView.getMessageContent();

        // 失败事件添加
        textRenderView.getMessageFailed().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(textMessage, position));
                popup.show(textView, DBConstant.SHOW_ORIGIN_TEXT_TYPE, true, isMine);
            }
        });

        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 弹窗类型
                MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(textMessage, position));
                boolean bResend = textMessage.getStatus() == MessageConstant.MSG_FAILURE;
                popup.show(textView, DBConstant.SHOW_ORIGIN_TEXT_TYPE, bResend, isMine);
                return true;
            }
        });

        // url 路径可以设定 跳转哦哦
        final String content = textMessage.getContent();
        textView.setOnTouchListener(new OnDoubleClickListener() {
            @Override
            public void onClick(View view) {
                //todo
            }

            @Override
            public void onDoubleClick(View view) {
                Intent intent = new Intent(ctx, PreviewTextActivity.class);
                intent.putExtra(IntentConstant.PREVIEW_TEXT_CONTENT, content);
                ctx.startActivity(intent);
            }
        });
        textRenderView.render(textMessage, userEntity, ctx);
        return textRenderView;
    }

    /**
     * 牙牙表情等gif类型的消息: 1. 设定内容Emoparser
     * 2. 点击事件  单击跳转、 双击方法、长按pop menu
     * 点击头像的事件 跳转
     *
     * @param position
     * @param convertView
     * @param viewGroup
     * @param isMine
     * @return
     */
    private View gifMsgRender(final int position, View convertView, final ViewGroup viewGroup, final boolean isMine) {
        EmojiRenderView gifRenderView;
        final EmotionMessage emoMessage = (EmotionMessage) msgObjectList.get(position);
        UserEntity userEntity = imService.getContactManager().findContact(emoMessage.getFromId());
        if (null == convertView) {
            gifRenderView = EmojiRenderView.inflater(ctx, viewGroup, isMine); //new TextRenderView(ctx,viewGroup,isMine);
        } else {
            gifRenderView = (EmojiRenderView) convertView;
        }

        final ImageView imageView = gifRenderView.getMessageContent();
        // 失败事件添加
        gifRenderView.getMessageFailed().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(emoMessage, position));
                popup.show(imageView, DBConstant.SHOW_GIF_TYPE, true, isMine);
            }
        });

        gifRenderView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(emoMessage, position));
                boolean bResend = emoMessage.getStatus() == MessageConstant.MSG_FAILURE;
                popup.show(imageView, DBConstant.SHOW_GIF_TYPE, bResend, isMine);

                return true;
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final String content = emoMessage.getContent();
                Intent intent = new Intent(ctx, PreviewGifActivity.class);
                intent.putExtra(IntentConstant.PREVIEW_TEXT_CONTENT, content);
                ctx.startActivity(intent);
                ((Activity) ctx).overridePendingTransition(R.anim.tt_image_enter, R.anim.tt_stay);
            }
        });

        gifRenderView.render(emoMessage, userEntity, ctx);
        return gifRenderView;
    }

    /**
     * location类型的: 1. 设定内容location
     * 2. 点击事件  单击跳转、 双击方法、长按pop menu
     * 点击头像的事件 跳转
     *
     * @param position
     * @param convertView
     * @param viewGroup
     * @param isMine
     * @return
     */
    private View locationMsgRender(final int position, View convertView, final ViewGroup viewGroup, final boolean isMine) {
        LocationRenderView locationRenderView;
        final LocationMessage locationMessage = (LocationMessage) msgObjectList.get(position);
        UserEntity userEntity = imService.getContactManager().findContact(locationMessage.getFromId());

        if (null == convertView) {
            locationRenderView = LocationRenderView.inflater(ctx, viewGroup, isMine); //new TextRenderView(ctx,viewGroup,isMine);
        } else {
            locationRenderView = (LocationRenderView) convertView;
        }

        final ImageView imageView = locationRenderView.getMessageImage();

        // 失败事件添加
        locationRenderView.getMessageFailed().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(locationMessage, position));
                popup.show(imageView, DBConstant.SHOW_LOCATION_TYPE, true, isMine);
            }
        });

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(locationMessage, position));
                boolean bResend = locationMessage.getStatus() == MessageConstant.MSG_FAILURE;
                popup.show(imageView, DBConstant.SHOW_LOCATION_TYPE, bResend, isMine);

                return true;
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String content = locationMessage.getContent();
                LocationEntity locationEntity = new Gson().fromJson(content, LocationEntity.class);
                Intent intent = new Intent(ctx, LocationCheckActivity.class);
                intent.putExtra("latitude", Double.valueOf(locationEntity.getLat()));
                intent.putExtra("longitude", Double.valueOf(locationEntity.getLng()));
                intent.putExtra("address", locationEntity.getAddress());
                ctx.startActivity(intent);
                ((Activity) ctx).overridePendingTransition(R.anim.tt_image_enter, R.anim.tt_stay);
            }
        });

        locationRenderView.render(locationMessage, userEntity, ctx);
        return locationRenderView;
    }

    /**
     * file类型的: 1. 设定内容file
     * 2. 点击事件  单击跳转、 双击方法、长按pop menu
     * 点击头像的事件 跳转
     *
     * @param position
     * @param convertView
     * @param viewGroup
     * @param isMine
     * @return
     */
    private View fileMsgRender(final int position, View convertView, final ViewGroup viewGroup, final boolean isMine) {
        FileRenderView fileRenderView;
        final FileMessage fileMessage = (FileMessage) msgObjectList.get(position);
        UserEntity userEntity = imService.getContactManager().findContact(fileMessage.getFromId());

        if (null == convertView) {
            fileRenderView = FileRenderView.inflater(ctx, viewGroup, isMine); //new TextRenderView(ctx,viewGroup,isMine);
        } else {
            fileRenderView = (FileRenderView) convertView;
        }

        final TextView textView = fileRenderView.getMessageContent();

        // 失败事件添加
        fileRenderView.getMessageFailed().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(fileMessage, position));
                popup.show(textView, DBConstant.SHOW_FIEL_TYPE, true, isMine);
            }
        });

        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 弹窗类型
                MessageOperatePopup popup = getPopMenu(viewGroup, new OperateItemClickListener(fileMessage, position));
                boolean bResend = fileMessage.getStatus() == MessageConstant.MSG_FAILURE;
                popup.show(textView, DBConstant.SHOW_FIEL_TYPE, bResend, isMine);
                return true;
            }
        });

        // url 路径可以设定 跳转哦哦
        final String content = fileMessage.getContent();
        textView.setOnTouchListener(new OnDoubleClickListener() {
            @Override
            public void onClick(View view) {
                //todo
            }

            @Override
            public void onDoubleClick(View view) {
                Intent intent = new Intent(ctx, PreviewTextActivity.class);
                intent.putExtra(IntentConstant.PREVIEW_TEXT_CONTENT, content);
                ctx.startActivity(intent);
            }
        });
        fileRenderView.render(fileMessage, userEntity, ctx);
        return fileRenderView;
    }

    /**
     * 文件是gif
     * @param position
     * @param convertView
     * @param parent
     * @param isMine
     * @return
     */
    private View GifFileMsgRender(final int position, View convertView, final ViewGroup parent, final boolean isMine) {
        GifFileRenderVIew gifFileRenderVIew;
        final FileMessage fileMessage = (FileMessage) msgObjectList.get(position);
        UserEntity userEntity = imService.getContactManager().findContact(fileMessage.getFromId());
        if (null == convertView) {
            gifFileRenderVIew = GifFileRenderVIew.inflater(ctx, parent, isMine);
        } else {
            gifFileRenderVIew = (GifFileRenderVIew) convertView;
        }
        GifView imageView = gifFileRenderVIew.getMessageContent();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final String url = fileMessage.getUrl();
                Intent intent = new Intent(ctx, PreviewGifActivity.class);
                intent.putExtra(IntentConstant.PREVIEW_TEXT_CONTENT, url);
                ctx.startActivity(intent);
                ((Activity) ctx).overridePendingTransition(R.anim.tt_image_enter, R.anim.tt_stay);
            }
        });
        gifFileRenderVIew.render(fileMessage, userEntity, ctx);
        return gifFileRenderVIew;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            final int typeIndex = getItemViewType(position);
            RenderType renderType = RenderType.values()[typeIndex];
            // 改用map的形式

            switch (renderType) {
                case MESSAGE_TYPE_INVALID:
                    // 直接返回
                    Logger.e("[fatal erro] render type:MESSAGE_TYPE_INVALID");
                    break;

                case MESSAGE_TYPE_TIME_TITLE:
                    convertView = timeBubbleRender(position, convertView, parent);
                    break;

                case MESSAGE_TYPE_MINE_AUDIO:
                    convertView = audioMsgRender(position, convertView, parent, true);
                    break;
                case MESSAGE_TYPE_OTHER_AUDIO:
                    convertView = audioMsgRender(position, convertView, parent, false);
                    break;
                case MESSAGE_TYPE_MINE_GIF_IMAGE:
                    convertView = GifImageMsgRender(position, convertView, parent, true);
                    break;
                case MESSAGE_TYPE_OTHER_GIF_IMAGE:
                    convertView = GifImageMsgRender(position, convertView, parent, false);
                    break;
                case MESSAGE_TYPE_MINE_IMAGE:
                    convertView = imageMsgRender(position, convertView, parent, true);
                    break;
                case MESSAGE_TYPE_OTHER_IMAGE:
                    convertView = imageMsgRender(position, convertView, parent, false);
                    break;
                case MESSAGE_TYPE_MINE_TETX:
                    convertView = textMsgRender(position, convertView, parent, true);
                    break;
                case MESSAGE_TYPE_OTHER_TEXT:
                    convertView = textMsgRender(position, convertView, parent, false);
                    break;

                case MESSAGE_TYPE_MINE_GIF:
                    convertView = gifMsgRender(position, convertView, parent, true);
                    break;
                case MESSAGE_TYPE_OTHER_GIF:
                    convertView = gifMsgRender(position, convertView, parent, false);
                    break;
                case MESSAGE_TYPE_MINE_LOCATION:
                    convertView = locationMsgRender(position, convertView, parent, true);
                    break;
                case MESSAGE_TYPE_OTHER_LOCATION:
                    convertView = locationMsgRender(position, convertView, parent, false);
                    break;
                case MESSAGE_TYPE_MINE_FILE:
                    convertView = fileMsgRender(position, convertView, parent, true);
                    break;
                case MESSAGE_TYPE_OTHER_FILE:
                    convertView = fileMsgRender(position, convertView, parent, false);
                    break;
                case MESSAGE_TYPE_MINE_SHORTVIDEO:
                    convertView = shortvideoMsgRender(position, convertView, parent, true);
                    break;
                case MESSAGE_TYPE_OTHER_SHORTVIDEO:
                    convertView = shortvideoMsgRender(position, convertView, parent, false);
                    break;
                case MESSAGE_TYPE_MINE_FILE_GIF:
                    convertView = GifFileMsgRender(position, convertView, parent, true);
                    break;
                case MESSAGE_TYPE_OTHER_FILE_GIF:
                    convertView = GifFileMsgRender(position, convertView, parent, false);
                    break;
            }
            return convertView;
        } catch (Exception e) {
            Logger.e("chat#%s", e);
            return null;
        }
    }

    /**
     * 点击事件的定义
     */
    private MessageOperatePopup getPopMenu(ViewGroup parent, MessageOperatePopup.OnItemClickListener listener) {
        MessageOperatePopup popupView = MessageOperatePopup.instance(ctx, parent);
        currentPop = popupView;
        popupView.setOnItemClickListener(listener);
        return popupView;
    }

    private class OperateItemClickListener
            implements
            MessageOperatePopup.OnItemClickListener {

        private MessageEntity mMsgInfo;
        private int mType;
        private int mPosition;

        public OperateItemClickListener(MessageEntity msgInfo, int position) {
            mMsgInfo = msgInfo;
            mType = msgInfo.getDisplayType();
            mPosition = position;
        }

        @SuppressWarnings("deprecation")
        @SuppressLint("NewApi")
        @Override
        public void onCopyClick() {
            try {
                ClipboardManager manager = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);

                Logger.d("menu#onCopyClick content:%s", mMsgInfo.getContent());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                    ClipData data = ClipData.newPlainText("data", mMsgInfo.getContent());
                    manager.setPrimaryClip(data);
                } else {
                    manager.setText(mMsgInfo.getContent());
                }
            } catch (Exception e) {
                Logger.e(e.getMessage());
            }
        }

        @Override
        public void onResendClick() {
            try {
                if (mType == DBConstant.SHOW_AUDIO_TYPE
                        || mType == DBConstant.SHOW_ORIGIN_TEXT_TYPE) {

                    if (mMsgInfo.getDisplayType() == DBConstant.SHOW_AUDIO_TYPE) {
                        if (mMsgInfo.getSendContent().length < 4) {
                            return;
                        }
                    }
                } else if (mType == DBConstant.SHOW_IMAGE_TYPE) {
                    Logger.d("pic#resend");
                    // 之前的状态是什么 上传没有成功继续上传
                    // 上传成功，发送消息
                    ImageMessage imageMessage = (ImageMessage) mMsgInfo;
                    if (TextUtils.isEmpty(imageMessage.getPath())) {
                        Toast.makeText(ctx, ctx.getString(R.string.image_path_unavaluable), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                mMsgInfo.setStatus(MessageConstant.MSG_SENDING);
                msgObjectList.remove(mPosition);
                addItem(mMsgInfo);
                if (imService != null) {
                    imService.getMessageManager().resendMessage(mMsgInfo);
                }

            } catch (Exception e) {
                Logger.e("chat#exception:" + e.toString());
            }
        }

        @Override
        public void onSpeakerClick() {
            AudioPlayerHandler audioPlayerHandler = AudioPlayerHandler.getInstance();
            if (audioPlayerHandler.getAudioMode(ctx) == AudioManager.MODE_NORMAL) {
                audioPlayerHandler.setAudioMode(AudioManager.MODE_IN_CALL, ctx);
                SpeekerToast.show(ctx, ctx.getText(R.string.audio_in_call), Toast.LENGTH_SHORT);
            } else {
                audioPlayerHandler.setAudioMode(AudioManager.MODE_NORMAL, ctx);
                SpeekerToast.show(ctx, ctx.getText(R.string.audio_in_speeker), Toast.LENGTH_SHORT);
            }
        }
    }
}
