package com.dtalk.dd.ui.widget.circle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.dtalk.dd.R;
import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.ui.helper.AudioPlayerHandler;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.ScreenUtils;

/**
 * Created by Donal on 16/8/31.
 */
public class CircleOperatePopup implements View.OnClickListener, View.OnTouchListener {

    private PopupWindow mPopup;
    private static CircleOperatePopup circleOperatePopup;
    private OnItemClickListener mListener;

    private int mWidth;
    private int mHeight;
    private TextView favorBtn, commentBtn ;
    private int position = -1;

    private Context context = null;

    public static CircleOperatePopup instance(Context ctx){
        if(null == circleOperatePopup ){
            synchronized (CircleOperatePopup.class){
                Logger.i("dsfsd");
                circleOperatePopup = new CircleOperatePopup(ctx);
            }
        }
        return circleOperatePopup;
    }

    public void hidePopup() {
        if (circleOperatePopup != null) {
            circleOperatePopup.dismiss();
        }
    }


    @SuppressWarnings("deprecation")
    private CircleOperatePopup(Context ctx) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.circle_popup_list,
                null);
        this.context = ctx;

        // popView = (LinearLayout) view.findViewById(R.id.popup_list);

        favorBtn = (TextView) view.findViewById(R.id.favor_btn);
        favorBtn.setOnClickListener(this);
        favorBtn.setOnTouchListener(this);
        favorBtn.setPadding(0, 13, 0, 8);

        commentBtn = (TextView) view.findViewById(R.id.comment_btn);
        commentBtn.setOnClickListener(this);
        commentBtn.setOnTouchListener(this);
        commentBtn.setPadding(0, 13, 0, 8);


        mWidth = ScreenUtils.getScreenWidth(context)/2;
        mHeight = (int) context.getResources().getDimension(
                R.dimen.circle_item_popup_height);

        mPopup = new PopupWindow(view, mWidth, mHeight);
        // mPopup.setFocusable(true);
        // 设置允许在外点击消失
        mPopup.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        mPopup.setBackgroundDrawable(new BitmapDrawable());
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mListener = l;
    }

    public void show(View item, boolean favor, int cposition) {
        Logger.d(mPopup.toString()+"");
        Logger.d(mPopup.isShowing()+"");
        if (mPopup == null || mPopup.isShowing() || position == cposition) {
            position = -1;
            return;
        }
        int[] location = new int[2];
        item.getLocationOnScreen(location);
        mPopup.showAtLocation(item, Gravity.NO_GRAVITY,
                    location[0] - item.getWidth()/2  - mWidth,
                    location[1] - 10 );
        position = cposition;
        Logger.d(mPopup.isShowing()+"");
    }

    public void dismiss() {
        if (mPopup == null || !mPopup.isShowing()) {
            return;
        }
        position = -1;
        mPopup.dismiss();
    }

    public interface OnItemClickListener {
        void onFavorClick();

        void onCommentClick();

    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();

        if (R.id.favor_btn == id) {
            dismiss();
            if (mListener != null) {
                mListener.onFavorClick();
            }
        } else if (R.id.comment_btn == id) {
            dismiss();
            if (mListener != null) {
                mListener.onCommentClick();
            }
        }
    }

    public void setmListener(OnItemClickListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        Resources resource = context.getResources();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (R.id.copy_btn == v.getId()) {
                Drawable drawable = null;
//                if (bcopyShow && bresendShow) {
//                    drawable = resource
//                            .getDrawable(R.drawable.tt_bg_popup_left_nomal);
//                } else if (bcopyShow || bresendShow) {
//                    drawable = resource
//                            .getDrawable(R.drawable.tt_bg_popup_normal);
//                }
//                if (drawable != null) {
//                    copyBtn.setBackgroundDrawable(drawable);
//                    copyBtn.setPadding(0, 13, 0, 8);
//                }
            } else if (R.id.resend_btn == v.getId()) {
                Drawable drawable = null;
//                if (bcopyShow && bresendShow) {
//                    drawable = resource
//                            .getDrawable(R.drawable.tt_bg_popup_right_nomal);
//                } else if (bcopyShow || bresendShow) {
//                    if (bspeakerShow) {
//                        drawable = resource
//                                .getDrawable(R.drawable.tt_bg_popup_right_nomal);
//                    } else {
//                        drawable = resource
//                                .getDrawable(R.drawable.tt_bg_popup_normal);
//                    }
//                }
//                if (drawable != null) {
//                    resendBtn.setBackgroundDrawable(drawable);
//                    resendBtn.setPadding(0, 13, 0, 8);
//                }
            } else if (R.id.speaker_btn == v.getId()) {
                Drawable drawable = null;
//                if (bresendShow) {
//                    drawable = resource
//                            .getDrawable(R.drawable.tt_bg_popup_left_nomal);
//                } else if (bspeakerShow) {
//                    drawable = resource
//                            .getDrawable(R.drawable.tt_bg_popup_normal);
//                }
//                if (drawable != null) {
//                    speakerBtn.setBackgroundDrawable(drawable);
//                    speakerBtn.setPadding(0, 13, 0, 8);
//                }
            }
        } else {
            if (R.id.copy_btn == v.getId()) {
                Drawable drawableResend = null;
                Drawable drawableCopy = null;
//                if (bcopyShow && bresendShow) {
//                    drawableCopy = resource
//                            .getDrawable(R.drawable.tt_bg_popup_left_pressed);
//                    drawableResend = resource
//                            .getDrawable(R.drawable.tt_bg_popup_right_nomal);
//                } else if (bcopyShow || bresendShow) {
//                    drawableCopy = resource
//                            .getDrawable(R.drawable.tt_bg_popup_pressed);
//                }
//                if (drawableCopy != null) {
//                    copyBtn.setBackgroundDrawable(drawableCopy);
//                    copyBtn.setPadding(0, 13, 0, 8);
//                }
//                if (drawableResend != null) {
//                    resendBtn.setBackgroundDrawable(drawableResend);
//                    resendBtn.setPadding(0, 13, 0, 8);
//                }
            } else if (R.id.resend_btn == v.getId()) {
                Drawable drawableCopy = null;
                Drawable drawableResend = null;
                Drawable drawableSpeaker = null;
//                if (bcopyShow && bresendShow) {
//                    drawableCopy = resource
//                            .getDrawable(R.drawable.tt_bg_popup_left_nomal);
//                    drawableResend = resource
//                            .getDrawable(R.drawable.tt_bg_popup_right_pressed);
//                } else if (bcopyShow || bresendShow) {
//                    if (bspeakerShow) {
//                        drawableSpeaker = resource
//                                .getDrawable(R.drawable.tt_bg_popup_left_nomal);
//                        drawableResend = resource
//                                .getDrawable(R.drawable.tt_bg_popup_right_pressed);
//                    } else {
//                        drawableResend = resource
//                                .getDrawable(R.drawable.tt_bg_popup_pressed);
//                    }
//                }
//                if (drawableResend != null) {
//                    resendBtn.setBackgroundDrawable(drawableResend);
//                    resendBtn.setPadding(0, 13, 0, 8);
//                }
//                if (drawableCopy != null) {
//                    copyBtn.setBackgroundDrawable(drawableCopy);
//                    copyBtn.setPadding(0, 13, 0, 8);
//                }
//                if (drawableSpeaker != null) {
//                    speakerBtn.setBackgroundDrawable(drawableSpeaker);
//                    speakerBtn.setPadding(0, 13, 0, 8);
//                }
            } else if (R.id.speaker_btn == v.getId()) {
                // Drawable drawableCopy = null;
                Drawable drawableResend = null;
                Drawable drawableSpeaker = null;
//                if (bresendShow && bspeakerShow) {
//                    drawableSpeaker = resource
//                            .getDrawable(R.drawable.tt_bg_popup_left_pressed);
//                    drawableResend = resource
//                            .getDrawable(R.drawable.tt_bg_popup_right_nomal);
//                } else if (bspeakerShow) {
//                    drawableSpeaker = resource
//                            .getDrawable(R.drawable.tt_bg_popup_pressed);
//                }
//                if (drawableResend != null) {
//                    resendBtn.setBackgroundDrawable(drawableResend);
//                    resendBtn.setPadding(0, 13, 0, 8);
//                }
//                if (drawableSpeaker != null) {
//                    speakerBtn.setBackgroundDrawable(drawableSpeaker);
//                    speakerBtn.setPadding(0, 13, 0, 8);
//                }
            }
        }
        return false;
    }
}
