package com.dtalk.dd.ui.activity;

/**
 * Created by Donal on 2017/1/7.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.dtalk.dd.R;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.user.UserClient;
import com.dtalk.dd.ui.base.TTBaseActivity;


public class UpdateGroupNameActivity extends TTBaseActivity implements View.OnClickListener {
    EditText et_nick;
    String nick;
    String id;

    public static void open(Context context, String id, String name) {
        context.startActivity(new Intent(context, UpdateGroupNameActivity.class).putExtra("id", id).putExtra("name", name));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_update_nick, topContentView);
        nick = getIntent().getStringExtra("name");
        id = getIntent().getStringExtra("id");
        et_nick = (EditText) this.findViewById(R.id.et_nick);
        et_nick.setText(nick);
        setLeftButton(R.drawable.tt_top_back);
        setLeftText(getResources().getString(R.string.top_left_back));
        setTitle("群名称");
        setTopRightText("保存");
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);
        topRightTitleTxt.setOnClickListener(this);
    }

    private void updateIvnServer(final String newNick) {
        final ProgressDialog dialog = new ProgressDialog(UpdateGroupNameActivity.this);
        dialog.setMessage("正在更新...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        UserClient.updateName(id, newNick, new BaseClient.ClientCallback() {
            @Override
            public void onPreConnection() {

            }

            @Override
            public void onCloseConnection() {

            }

            @Override
            public void onSuccess(Object data) {
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
