package com.dtalk.dd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dtalk.dd.DB.sp.LoginSp;
import com.dtalk.dd.DB.sp.SystemConfigSp;
import com.dtalk.dd.R;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.config.IntentConstant;
import com.dtalk.dd.config.UrlConstant;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.ClientCallback;
import com.dtalk.dd.http.friend.OtherUserInfoNoRemark;
import com.dtalk.dd.http.user.UserClient;
import com.dtalk.dd.http.user.UserInfo;
import com.dtalk.dd.protobuf.IMLogin;
import com.dtalk.dd.utils.IMUIHelper;
import com.dtalk.dd.imservice.event.LoginEvent;
import com.dtalk.dd.imservice.event.SocketEvent;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.imservice.service.IMService;
import com.dtalk.dd.ui.base.TTBaseActivity;
import com.dtalk.dd.imservice.support.IMServiceConnector;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.SandboxUtils;
import com.dtalk.dd.utils.ThemeUtils;
import com.dtalk.dd.utils.ViewUtils;

import cn.smssdk.SMSSDK;
import de.greenrobot.event.EventBus;


/**
 * @YM 1. 链接成功之后，直接判断是否loginSp是否可以直接登陆
 * true: 1.可以登陆，从DB中获取历史的状态
 * 2.建立长连接，请求最新的数据状态 【网络断开没有这个状态】
 * 3.完成
 * <p/>
 * false:1. 不能直接登陆，跳转到登陆页面
 * 2. 请求消息服务器地址，链接，验证，触发loginSuccess
 * 3. 保存登陆状态
 */
public class LoginActivity extends TTBaseActivity {

    private Handler uiHandler = new Handler();
    private EditText mNameView;
    private EditText mPasswordView;
    private View loginPage;
    private View splashPage;
    private InputMethodManager intputManager;
    private Button loginBtn;

    private IMService imService;
    private boolean autoLogin = true;
    private boolean loginSuccess = false;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            Logger.d("login#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            try {
                do {
                    if (imService == null) {
                        //后台服务启动链接失败
                        break;
                    }
                    IMLoginManager loginManager = imService.getLoginManager();
                    LoginSp loginSp = imService.getLoginSp();
                    if (loginManager == null || loginSp == null) {
                        // 无法获取登陆控制器
                        break;
                    }

                    LoginSp.SpLoginIdentity loginIdentity = loginSp.getLoginIdentity();
                    if (loginIdentity == null) {
                        // 之前没有保存任何登陆相关的，跳转到登陆页面
                        break;
                    }

                    mNameView.setText(loginIdentity.getLoginName());
                    if (TextUtils.isEmpty(loginIdentity.getPwd())) {
                        // 密码为空，可能是loginOut
                        break;
                    }
                    mPasswordView.setText(loginIdentity.getPwd());

                    if (autoLogin == false) {
                        break;
                    }

                    handleGotLoginIdentity(loginIdentity);
                    return;
                } while (false);

                // 异常分支都会执行这个
                handleNoLoginIdentity();
            } catch (Exception e) {
                // 任何未知的异常
                Logger.w("loadIdentity failed");
                handleNoLoginIdentity();
            }
        }
    };


    /**
     * 跳转到登陆的页面
     */
    private void handleNoLoginIdentity() {
        Logger.i("login#handleNoLoginIdentity");
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showLoginPage();
            }
        }, 1000);
    }

    /**
     * 自动登陆
     */
    private void handleGotLoginIdentity(final LoginSp.SpLoginIdentity loginIdentity) {
        Logger.i("login#handleGotLoginIdentity");

        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Logger.d("login#start auto login");
                if (imService == null || imService.getLoginManager() == null) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                    showLoginPage();
                }
                String loginUserName = loginIdentity.getLoginName();
                String loginPwd = loginIdentity.getPwd();
                UserClient.doLogin(loginUserName, loginPwd, new ClientCallback() {
                    @Override
                    public void onPreConnection() {
                        showProgress(true);
                    }

                    @Override
                    public void onCloseConnection() {
                        ViewUtils.dismissProgressDialog();
                    }

                    @Override
                    public void onSuccess(Object data) {
                        UserInfo user = (UserInfo) data;
                        if (user.getStatus() == 1) {
                            OtherUserInfoNoRemark userInfoNoRemark = new OtherUserInfoNoRemark();
                            userInfoNoRemark.setAvatar(user.getAvatar());
                            userInfoNoRemark.setUid(user.getUid());
                            userInfoNoRemark.setNickname(user.getNickname());
                            userInfoNoRemark.setMoment_cover(user.getMoment_cover());
                            SandboxUtils.getInstance().saveObject(IMApplication.getInstance(), userInfoNoRemark, "user");
                            SandboxUtils.getInstance().set(IMApplication.getInstance(), "token", user.getToken());
                            imService.getLoginManager().login(loginIdentity);
                        } else {
                            Toast.makeText(getApplicationContext(), user.getMsg(), Toast.LENGTH_SHORT).show();
                            handleNoLoginIdentity();
                        }
                    }

                    @Override
                    public void onFailure(String message) {

                    }

                    @Override
                    public void onException(Exception e) {

                    }
                });

            }
        }, 500);
    }


    private void showLoginPage() {
        splashPage.setVisibility(View.GONE);
        loginPage.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        intputManager = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        SMSSDK.initSDK(this, "272bc3eaa8dc", "a10211aac04334b49d39b779a350621e");
        SystemConfigSp.instance().init(getApplicationContext());
        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, UrlConstant.ACCESS_MSG_ADDRESS);
        }

        imServiceConnector.connect(LoginActivity.this);
        EventBus.getDefault().register(this);

        setContentView(R.layout.tt_activity_login);

        mNameView = (EditText) findViewById(R.id.et_usertel);
        mPasswordView = (EditText) findViewById(R.id.et_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {

                if (id == R.id.sign_in_button || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mNameView.addTextChangedListener(new TextChange());
        mPasswordView.addTextChangedListener(new TextChange());

        loginBtn = (Button) findViewById(R.id.sign_in_button);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intputManager.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
                attemptLogin();
            }
        });
        findViewById(R.id.btn_qtlogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intputManager.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
                AuthActivity.launch(LoginActivity.this, 0);

            }
        });
        findViewById(R.id.btn_forget).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intputManager.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
                ResetPasswordActivity.launch(LoginActivity.this);
            }
        });
        initAutoLogin();
    }

    private void initAutoLogin() {
        Logger.i("login#initAutoLogin");

        splashPage = findViewById(R.id.splash_page);
        loginPage = findViewById(R.id.login_page);
        autoLogin = shouldAutoLogin();

        splashPage.setVisibility(autoLogin ? View.VISIBLE : View.GONE);
        loginPage.setVisibility(autoLogin ? View.GONE : View.VISIBLE);

        loginPage.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (mPasswordView != null) {
                    intputManager.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
                }

                if (mNameView != null) {
                    intputManager.hideSoftInputFromWindow(mNameView.getWindowToken(), 0);
                }

                return false;
            }
        });

        if (autoLogin) {
            Animation splashAnimation = AnimationUtils.loadAnimation(this, R.anim.login_splash);
            if (splashAnimation == null) {
                Logger.e("login#loadAnimation login_splash failed");
                return;
            }

            splashPage.startAnimation(splashAnimation);
        }
    }

    // 主动退出的时候， 这个地方会有值,更具pwd来判断
    private boolean shouldAutoLogin() {
        Intent intent = getIntent();
        if (intent != null) {
            boolean notAutoLogin = intent.getBooleanExtra(IntentConstant.KEY_LOGIN_NOT_AUTO, false);
            if (notAutoLogin) {
                return false;
            }
        }
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        imServiceConnector.disconnect(LoginActivity.this);
        EventBus.getDefault().unregister(this);
        splashPage = null;
        loginPage = null;
    }


    public void attemptLogin() {
        String loginName = mNameView.getText().toString();
        String mPassword = mPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mPassword)) {
            Toast.makeText(this, getString(R.string.error_pwd_required), Toast.LENGTH_SHORT).show();
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(loginName)) {
            Toast.makeText(this, getString(R.string.error_name_required), Toast.LENGTH_SHORT).show();
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {

            if (imService != null) {
//				boolean userNameChanged = true;
//				boolean pwdChanged = true;
                final String username = loginName.trim();
                final String password = mPassword.trim();
                UserClient.doLogin(username, password, new ClientCallback() {
                    @Override
                    public void onPreConnection() {
                        showProgress(true);
                    }

                    @Override
                    public void onCloseConnection() {
                        ViewUtils.dismissProgressDialog();
                    }

                    @Override
                    public void onSuccess(Object data) {
                        UserInfo user = (UserInfo) data;
                        if (user.getStatus() == 1) {
                            OtherUserInfoNoRemark userInfoNoRemark = new OtherUserInfoNoRemark();
                            userInfoNoRemark.setAvatar(user.getAvatar());
                            userInfoNoRemark.setUid(user.getUid());
                            userInfoNoRemark.setNickname(user.getNickname());
                            userInfoNoRemark.setMoment_cover(user.getMoment_cover());
                            SandboxUtils.getInstance().saveObject(IMApplication.getInstance(), userInfoNoRemark, "user");
                            SandboxUtils.getInstance().set(IMApplication.getInstance(), "token", user.getToken());
                            SandboxUtils.getInstance().set(IMApplication.getInstance(), "avatar", user.getAvatar());
                            SandboxUtils.getInstance().set(IMApplication.getInstance(), "cover", user.getMoment_cover());
                            SMSSDK.submitUserInfo(username, user.getNickname(), user.getAvatar(), "86", username);
                            imService.getLoginManager().login(username, password);
                        } else
                            Toast.makeText(getApplicationContext(), user.getMsg(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String message) {

                    }

                    @Override
                    public void onException(Exception e) {

                    }
                });

            }
        }
    }

    private void showProgress(final boolean show) {
        if (show) {
            ViewUtils.createProgressDialog(getRunningActivity(), "正在登录...", ThemeUtils.getThemeColor()).show();
        } else {
            ViewUtils.dismissProgressDialog();
        }
    }

    // 为什么会有两个这个
    // 可能是 兼容性的问题 导致两种方法onBackPressed
    @Override
    public void onBackPressed() {
        Logger.d("login#onBackPressed");
        //imLoginMgr.cancel();
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//            LoginActivity.this.finish();
//            return true;
//        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * ----------------------------event 事件驱动----------------------------
     */
    public void onEventMainThread(LoginEvent event) {
        switch (event) {
            case LOCAL_LOGIN_SUCCESS:
            case LOGIN_OK:
                onLoginSuccess();
                break;
            case LOGIN_AUTH_FAILED:
            case LOGIN_INNER_FAILED:
                if (!loginSuccess)
                    onLoginFailure(event);
                break;
        }
    }

    class TextChange implements TextWatcher {

        @Override
        public void afterTextChanged(Editable arg0) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {

        }

        @Override
        public void onTextChanged(CharSequence cs, int start, int before,
                                  int count) {

            boolean Sign2 = mNameView.getText().length() > 0;
            boolean Sign3 = mPasswordView.getText().length() > 0;

            if (Sign2 & Sign3) {
                loginBtn.setTextColor(0xFFFFFFFF);
                loginBtn.setEnabled(true);
            }
            // 在layout文件中，对Button的text属性应预先设置默认值，否则刚打开程序的时候Button是无显示的
            else {
                loginBtn.setTextColor(0xFFD0EFC6);
                loginBtn.setEnabled(false);
            }
        }
    }

    public void onEventMainThread(SocketEvent event) {
        switch (event) {
            case CONNECT_MSG_SERVER_FAILED:
            case REQ_MSG_SERVER_ADDRS_FAILED:
                if (!loginSuccess)
                    onSocketFailure(event);
                break;
        }
    }

    private void onLoginSuccess() {
        Logger.i("login#onLoginSuccess");
        loginSuccess = true;
        int loginId = IMLoginManager.instance().getLoginId();
        SandboxUtils.getInstance().remove(IMApplication.getInstance(), loginId + "-cid");
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        LoginActivity.this.finish();
    }

    private void onLoginFailure(LoginEvent event) {
        Logger.e("login#onLoginError -> errorCode:%s", event.name());
        showLoginPage();
        String errorTip = getString(IMUIHelper.getLoginErrorTip(event));
        ViewUtils.dismissProgressDialog();
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
    }

    private void onSocketFailure(SocketEvent event) {
        Logger.e("login#onLoginError -> errorCode:%s,", event.name());
        showLoginPage();
        String errorTip = getString(IMUIHelper.getSocketErrorTip(event));
        ViewUtils.dismissProgressDialog();
        Toast.makeText(this, errorTip, Toast.LENGTH_SHORT).show();
    }
}
