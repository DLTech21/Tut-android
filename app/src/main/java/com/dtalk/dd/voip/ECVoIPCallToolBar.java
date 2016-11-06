package com.dtalk.dd.voip;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.dtalk.dd.R;


/**
 * com.yuntongxun.ecdemo.ui.voip in ECDemo_Android
 * Created by Jorstin on 2015/7/9.
 */
public class ECVoIPCallToolBar extends LinearLayout {
    public ECVoIPCallToolBar(Context context) {
        this(context, null);
    }

    public ECVoIPCallToolBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ECVoIPCallToolBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        initView();
    }

    private void initView(){
        View.inflate(getContext(), R.layout.ec_voip_toolbar, this);
    }
}
