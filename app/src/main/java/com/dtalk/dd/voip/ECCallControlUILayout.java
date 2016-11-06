/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */package com.dtalk.dd.voip;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dtalk.dd.R;


/**
 * 通话界面功能控制按钮操作区域
 * com.yuntongxun.ecdemo.ui.voip in ECDemo_Android
 * Created by Jorstin on 2015/7/3.
 */
public class ECCallControlUILayout extends LinearLayout
        implements View.OnClickListener {

    /**来电操作按钮区域*/
    private LinearLayout mIncomingCallLayout;
    /**挂断按钮*/
    private ImageButton mCallCancel;
    /**接听按钮*/
    private ImageButton mCallAccept;
    /**通话功能按钮区域*/
    private LinearLayout mCallSetupLayout;
    /**通话控制区域*/
    private LinearLayout mCallingshowPanel;
    /**静音按钮*/
    private ImageView mCallMute;
    /**免提按钮*/
    private ImageView mCallHandFree;
    private ImageView mDiaerpadBtn;
    /**挂断按钮*/
    private ImageButton mCallRelease;
    /**控制按钮监听接口*/
    private OnCallControlDelegate mOnCallControlDelegate;
    
    
    
    
    
    public ECCallControlUILayout(Context context) {
        this(context, null);
    }

    public ECCallControlUILayout(Context context, AttributeSet attrs) {
        this(context, attrs , 0);
    }

    public ECCallControlUILayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        initView();
        TypedArray typedArray = context.obtainStyledAttributes(attrs ,R.styleable.call_control);
        int callDirect = typedArray.getInt(R.styleable.call_control_call_direct , 0);
        // 设置呼叫界面显示
        setCallDirect(CallLayout.values()[callDirect]);
    }

    /**
     * 设置通话界面显示的类型
     * @param callDirect
     */
    public void setCallDirect(CallLayout callDirect) {
        if(callDirect == CallLayout.INCOMING) {
            mIncomingCallLayout.setVisibility(View.VISIBLE);
            mCallingshowPanel.setVisibility(View.GONE);
        } else if (callDirect == CallLayout.OUTGOING || callDirect == CallLayout.ALERTING) {
            mIncomingCallLayout.setVisibility(View.GONE);
            mCallingshowPanel.setVisibility(View.VISIBLE);
            setControlEnable(callDirect == CallLayout.ALERTING);
        } else if (callDirect == CallLayout.INCALL) {
            mIncomingCallLayout.setVisibility(View.GONE);
            mCallingshowPanel.setVisibility(View.VISIBLE);
            setControlEnable(true);
        }
    }

    /**
     * 设置通话控制按钮是否可用
     * @param enable
     */
    public void setControlEnable(boolean enable) {
        mCallMute.setEnabled(enable);
        mCallHandFree.setEnabled(enable);
        mDiaerpadBtn.setEnabled(enable);
    }
    
   

    private void initView() {
        View.inflate(getContext(), R.layout.ec_call_control_layout, this);
        // 来电显示界面
        mIncomingCallLayout = (LinearLayout) findViewById(R.id.incoming_call_bottom_show);
        mCallCancel = (ImageButton) findViewById(R.id.layout_call_cancel);
        mCallAccept = (ImageButton) findViewById(R.id.layout_call_accept);
        mCallAccept.setOnClickListener(this);
        mCallCancel.setOnClickListener(this);

		// 通话进行过程中显示
		mCallingshowPanel = (LinearLayout) findViewById(R.id.calling_bottom_show);
		// 通话控制按钮区域
		mCallSetupLayout = (LinearLayout) findViewById(R.id.call_mute_container);
		mCallMute = (ImageView) findViewById(R.id.layout_call_mute);
		mCallHandFree = (ImageView) findViewById(R.id.layout_call_handfree);

		mCallHandFree.setClickable(true);
		mCallMute.setClickable(true);
		mCallHandFree.setOnClickListener(l);
		mCallMute.setOnClickListener(l);

		mDiaerpadBtn = (ImageView) findViewById(R.id.layout_call_dialnum);

        // 挂断电话按钮
        mCallRelease = (ImageButton) findViewById(R.id.layout_call_release);
        mCallRelease.setOnClickListener(this);
        mDiaerpadBtn.setOnClickListener(l);
        
        
    }

	/**
	 * 监听通话按钮
	 * 
	 * @param delegate
	 */
	public void setOnCallControlDelegate(OnCallControlDelegate delegate) {
		mOnCallControlDelegate = delegate;
	}

	private OnClickListener l = new OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.layout_call_mute:
				VoIPCallHelper.setMute();
                boolean mute = VoIPCallHelper.getMute();
                mCallMute.setImageResource(mute ? R.drawable.ec_call_interface_mute_on : R.drawable.ec_call_interface_mute);

				break;
			case R.id.layout_call_handfree:
				VoIPCallHelper.setHandFree();
                boolean handFree = VoIPCallHelper.getHandFree();
                mCallHandFree .setImageResource(handFree ? R.drawable.ec_call_interface_hands_free_on : R.drawable.ec_call_interface_hands_free);
				break;
			case R.id.layout_call_dialnum:
				
				mOnCallControlDelegate.setDialerpadUI();
				
				break;
			}
		}
	};
	
	
	

    @Override
    public void onClick(View v) {
        if(mOnCallControlDelegate == null) {
            return ;
        }
        switch (v.getId()) {
            case R.id.layout_call_accept:
            	ImageButton view = (ImageButton) v;
                mOnCallControlDelegate.onViewAccept(this, view);
                break;
            case R.id.layout_call_cancel:
            	ImageButton viewCancel = (ImageButton) v;
                mOnCallControlDelegate.onViewReject(this , viewCancel);
                break;
            case R.id.layout_call_release:
            	ImageButton viewRelease = (ImageButton) v;
                mOnCallControlDelegate.onViewRelease(this, viewRelease);
                break;
            
            
                
            default:
                break;
        }
    }
    
    
   

    /**
     * 通话界面显示类型
     */
    public enum CallLayout {
        INCOMING , ALERTING ,OUTGOING , INCALL
    }

    public interface OnCallControlDelegate {
        void onViewAccept(ECCallControlUILayout controlPanelView, ImageButton view);
        void onViewReject(ECCallControlUILayout controlPanelView, ImageButton view);
        void onViewRelease(ECCallControlUILayout controlPanelView, ImageButton view);
        void setDialerpadUI();
    }
}
