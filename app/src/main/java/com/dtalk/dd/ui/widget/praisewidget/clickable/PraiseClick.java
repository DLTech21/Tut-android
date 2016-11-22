package com.dtalk.dd.ui.widget.praisewidget.clickable;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.Toast;

import com.dtalk.dd.R;
import com.dtalk.dd.imservice.callback.Packetlistener;
import com.dtalk.dd.ui.widget.praisewidget.widget.PraiseWidget;

/**
 * Created by 大灯泡 on 2015/11/21.
 */
public class PraiseClick extends ClickableSpan {
    private static final int DEFAULT_COLOR = 0xff517fae;

    private int color;
    private String userID;
    private String userNick;
    private Context mContext;
    private int textSize;
    private PraiseWidget.PraiseWidgetListener mPraiseWidgetListener;

    public PraiseClick(Context context, String userNick, String userID, int color) {
        mContext = context;
        this.userNick = userNick;
        this.userID = userID;
        this.color = color;
    }

//    public PraiseClick(Context context, String userID, int color) {
//        this(context, "", userID, color);
//    }
//
//    public PraiseClick(Context context, String userID) {
//        this(context, "", userID, 0);
//    }
//
//    public PraiseClick(Context context, String userNick, String userID) {
//        this(context, userNick, userID, 0);
//    }

    public PraiseClick(Context context, String userNick, String userID, int color, int textSize, PraiseWidget.PraiseWidgetListener praiseWidgetListener) {
        this(context, userNick, userID, color);
        this.textSize = textSize;
        this.mPraiseWidgetListener = praiseWidgetListener;
    }

    @Override
    public void onClick(View widget) {
        this.mPraiseWidgetListener.onNameClickListener(userID, userNick);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        //去掉下划线
        if (color == 0) {
            ds.setColor(DEFAULT_COLOR);
        } else {
            ds.setColor(color);
        }
        ds.setTextSize(textSize);
        ds.setUnderlineText(false);
    }
}
