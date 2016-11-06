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
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dtalk.dd.R;


/**
 * 呼叫界面顶部呼叫信息显示区域
 * com.yuntongxun.ecdemo.ui.voip in ECDemo_Android
 * Created by Jorstin on 2015/7/3.
 */
public class ECCallHeadUILayout extends LinearLayout  {

    /**通话者昵称*/
    private TextView mCallName;
    /**通话号码*/
    private TextView mCallNumber;
    /**通话时间*/
    private Chronometer mCallTime;
    /**呼叫状态描述*/
    private TextView mCallMsg;
    /**头像*/
    private ImageView mPhotoView;
    /**当前是否正在进行通话*/
    private boolean mCalling = false;
    /**是否显示通话参数信息*/
    private boolean mShowCallTips = false;
    

    public ECCallHeadUILayout(Context context) {
        this(context , null);
    }

    public ECCallHeadUILayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ECCallHeadUILayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.ec_call_head_layout, this);
        mCallName = (TextView) findViewById(R.id.layout_call_name);
        mCallNumber = (TextView) findViewById(R.id.layout_call_number);
        mCallTime = (Chronometer) findViewById(R.id.chronometer);
        mCallMsg = (TextView) findViewById(R.id.layout_call_msg);
        mPhotoView = (ImageView) findViewById(R.id.layout_call_photo);

    }
    
   

    /**
     * 设置当前的呼叫状态
     * @param calling
     */
    public void setCalling(boolean calling) {
        this.mCalling = calling;

        if(calling) {
            mCallTime.setBase(SystemClock.elapsedRealtime());
            mCallTime.setVisibility(View.VISIBLE);
            mCallTime.start();
        } else {
            mCallTime.stop();
            mCallTime.setVisibility(View.GONE);
        }
        mCallMsg.setVisibility((calling && !mShowCallTips)? View.GONE:View.VISIBLE);
    }

    /**
     * 是否显示通话参数信息
     * @param isShowing
     */
    public void setCallTipsShowing(boolean isShowing) {
        mShowCallTips = isShowing;
    }

    /**
     * 设置呼叫昵称
     * @param text 昵称
     */
    public void setCallName(CharSequence text) {
        if(mCallName != null) {
            mCallName.setText(text);
        }
    }

    /**
     * 设置呼叫号码
     * @param mobile 号码
     */
    public void setCallNumber(CharSequence mobile) {
        if(mCallNumber != null) {
            mCallNumber.setText(mobile);

            if(mobile != null) {
//                ECContacts mContacts = ContactSqlManager.getContact(mobile.toString());
//                if(mContacts != null && mContacts.getRemark() != null && !"personal_center_default_avatar.png".equals(mContacts.getRemark())) {
//                    mPhotoView.setImageBitmap(ContactLogic.getPhoto(mContacts.getRemark()));
//                }
            }
        }
    }

    /**
     * 设置呼叫状态描述
     * @param text
     */
    public void setCallTextMsg(String text) {
        if(mCallMsg == null) {
            return ;
        }
        if((text == null || text.length() <= 0) && !mCalling ) {
            mCallMsg.setVisibility(View.GONE);
        } else {
            mCallMsg.setText(text);
            mCallMsg.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置呼叫状态描述
     * @param resid
     */
    public void setCallTextMsg(int resid) {
        setCallTextMsg(getResources().getString(resid));
    }
    
    private boolean isShowDial=false;
    public void controllerDiaNumUI(){
    	
    	if(!isShowDial){
    		mPhotoView.setVisibility(View.GONE);
    		isShowDial=true;
    	}else {
    		mPhotoView.setVisibility(View.VISIBLE);
    		isShowDial=false;
    	}
    	
    }

//	@Override
//	 void keyPressed(int keyCode) {
//
//			KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
//			if(sendDTMFDelegate!=null){
////			sendDTMFDelegate.sendDTMF(mDmfInput.getText().toString().toCharArray()[0]);
//			}
//		}
	 
	 public interface OnSendDTMFDelegate {
	        void sendDTMF(char c);
	    }
	 
	 private OnSendDTMFDelegate sendDTMFDelegate;

	public OnSendDTMFDelegate getSendDTMFDelegate() {
		return sendDTMFDelegate;
	}

	public void setSendDTMFDelegate(OnSendDTMFDelegate sendDTMFDelegate) {
		this.sendDTMFDelegate = sendDTMFDelegate;
	}
	 
	 

}
