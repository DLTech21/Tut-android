package com.dtalk.dd.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.friend.OtherUserInfoNoRemark;
import com.dtalk.dd.http.user.UserClient;
import com.dtalk.dd.http.user.UserInfo;
import com.dtalk.dd.imservice.event.UpdateUserInfoEvent;
import com.dtalk.dd.ui.base.TTBaseActivity;
import com.dtalk.dd.utils.SandboxUtils;

import de.greenrobot.event.EventBus;


public class UpdateNickActivity extends TTBaseActivity implements View.OnClickListener {
    EditText et_nick;
    String nick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_update_nick, topContentView);
        nick = SandboxUtils.getInstance().getUser().getNickname();
        et_nick = (EditText) this.findViewById(R.id.et_nick);
        et_nick.setText(nick);
        setLeftButton(R.drawable.tt_top_back);
        setLeftText(getResources().getString(R.string.top_left_back));
        setTitle("个人资料");
        setTopRightText("保存");
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);
        topRightTitleTxt.setOnClickListener(this);
    }

    private void updateIvnServer(final String newNick) {
        final ProgressDialog dialog = new ProgressDialog(UpdateNickActivity.this);
        dialog.setMessage("正在更新...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        String json = "{\"nickname\":" + "\"" + newNick + "\"}";
        UserClient.updateUserByJson(json, new BaseClient.ClientCallback() {
            @Override
            public void onPreConnection() {

            }

            @Override
            public void onCloseConnection() {

            }

            @Override
            public void onSuccess(Object data) {
                OtherUserInfoNoRemark userInfo = SandboxUtils.getInstance().getUser();
                userInfo.setNickname(newNick);
                SandboxUtils.getInstance().saveObject(IMApplication.getInstance(), userInfo, "user");
                EventBus.getDefault().post(new UpdateUserInfoEvent());
                finish();
            }

            @Override
            public void onFailure(String message) {

            }

            @Override
            public void onException(Exception e) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.left_btn:
            case R.id.left_txt:
                this.finish();
                break;
            case R.id.right_txt:
                String newNick = et_nick.getText().toString().trim();
                if (nick.equals(newNick) || newNick.equals("") || newNick.equals("0")) {
                    return;
                }
                updateIvnServer(newNick);
                break;
        }
    }
}
