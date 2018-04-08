package com.dtalk.dd.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dtalk.dd.R;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.http.base.ClientCallback;
import com.dtalk.dd.http.register.RegisterClient;
import com.dtalk.dd.imservice.event.RegisterEvent;
import com.dtalk.dd.ui.base.TTBaseActivity;
import com.dtalk.dd.utils.StringUtils;
import com.dtalk.dd.utils.ThemeUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;


public class ResetPasswordActivity extends TTBaseActivity implements OnClickListener{

    private EditText et_mobile;
    private EditText et_code;
    @ViewInject(R.id.password)
    private EditText et_password;
    private TextView tv_send;
    private TextView tv_next;
    private TimeCount time;

    public static void launch(Activity from) {
        from.startActivity(new Intent(from, ResetPasswordActivity.class));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        LayoutInflater.from(this).inflate(R.layout.activity_reset_password, topContentView);
        ViewUtils.inject(this);
        init();
    }

    public void init() {
        setLeftButton(R.drawable.tt_top_back);
        setLeftText(getResources().getString(R.string.top_left_back));
        setTitle("重置密码");
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);
        et_mobile = (EditText) findViewById(R.id.mobile);
        et_code = (EditText) findViewById(R.id.auth);
        tv_send = (TextView) findViewById(R.id.send);
        tv_send.setOnClickListener(this);
        tv_next = (TextView) findViewById(R.id.next);
        LinearLayout nextLinear = (LinearLayout) findViewById(R.id.next_linear);
        nextLinear.setOnClickListener(this);

        time = new TimeCount(60000, 1000);// 构造CountDownTimer对象
    }

    public void back(View view) {
        finish();
    }

    private void sendmob(String mobile) {
        if (TextUtils.isEmpty(mobile)) {
            WarningDialog("手机号码不能为空");
            return;
        }

        if (!StringUtils.isMobileNO(mobile)) {
            WarningDialog("手机号码格式不正确");
            return;
        }

        tv_send.setEnabled(false);
        RegisterClient.sendSmsCode(mobile, "reset", new ClientCallback() {

            @Override
            public void onSuccess(Object data) {
                time.start();
            }

            @Override
            public void onPreConnection() {
                com.dtalk.dd.utils.ViewUtils.createProgressDialog(getRunningActivity(), "获取验证码...", ThemeUtils.getThemeColor()).show();
            }

            @Override
            public void onFailure(String message) {
                WarningDialog(message);
            }

            @Override
            public void onException(Exception e) {
                WarningDialog(e.toString());
            }

            @Override
            public void onCloseConnection() {
                com.dtalk.dd.utils.ViewUtils.dismissProgressDialog();
            }
        });
    }

    private void next(final String mobile, final String code, String password) {
        if (TextUtils.isEmpty(mobile) || TextUtils.isEmpty(code)) {
            WarningDialog("手机号码和验证码不能为空");
            return;
        }
        
        if (!StringUtils.isMobileNO(mobile)) {
            WarningDialog("手机号码格式不正确");
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            WarningDialog("密码不能为空");
            return;
        }
        RegisterClient.resetPassword(mobile, code, password, new ClientCallback() {
            
            @Override
            public void onSuccess(Object data) {
                BaseResponse baseResponse = (BaseResponse)data;
                if (baseResponse.getStatus() == 1) {
                    Toast.makeText(ResetPasswordActivity.this, "重置密码成功", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    WarningDialog(baseResponse.getMsg());
                }
            }
            
            @Override
            public void onPreConnection() {
                com.dtalk.dd.utils.ViewUtils.createProgressDialog(getRunningActivity(), "重置密码中...", ThemeUtils.getThemeColor()).show();
            }
            
            @Override
            public void onFailure(String message) {
                WarningDialog(message);
            }
            
            @Override
            public void onException(Exception e) {
                
            }
            
            @Override
            public void onCloseConnection() {
                com.dtalk.dd.utils.ViewUtils.dismissProgressDialog();
            }
        });
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
        case R.id.left_btn:
        case R.id.left_txt:
                this.finish();
                break;
        case R.id.send:
            sendmob(et_mobile.getText().toString());
            break;
        case R.id.next_linear:
            next(et_mobile.getText().toString(), et_code.getText().toString(), et_password.getText().toString());
            break;
        default:
            break;
        }
    }

    /* 定义一个倒计时的内部类 */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            tv_send.setText("重新验证");
            tv_send.setEnabled(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            tv_send.setEnabled(false);
            tv_send.setText(millisUntilFinished / 1000 + "秒");
        }
    }
    
}
