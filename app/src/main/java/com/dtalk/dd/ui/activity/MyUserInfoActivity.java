package com.dtalk.dd.ui.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.ui.base.TTBaseActivity;
import com.dtalk.dd.utils.StringUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yixia.camera.demo.ui.BaseActivity;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPreview;

/**
 * Created by Donal on 2017/1/6.
 */

public class MyUserInfoActivity extends TTBaseActivity implements View.OnClickListener{

    private RelativeLayout re_avatar;
    private RelativeLayout re_name;
    //    private RelativeLayout re_fxid;
    private RelativeLayout re_sex;
    private RelativeLayout re_region;
    private RelativeLayout re_erweima;

    private ImageView iv_avatar;
    private TextView tv_name;
    //    private TextView tv_fxid;
    private TextView tv_sex;
    private TextView tv_sign;
    private TextView tv_region;


    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    private static final int UPDATE_FXID = 4;// 结果
    private static final int UPDATE_NICK = 5;// 结果
    String hxid;
    //    String fxid;
    String sex;
    String sign;
    String nick;
    String region;
    private UserEntity currentUser;

    public static void launch(Context context, UserEntity entity) {
        context.startActivity(new Intent(context, MyUserInfoActivity.class).putExtra("user", entity));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_myuser, topContentView);
        currentUser = (UserEntity) getIntent().getSerializableExtra("user");
        initView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        setLeftButton(R.drawable.tt_top_back);
        setLeftText(getResources().getString(R.string.top_left_back));
        setTitle("注册");
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);

        hxid = currentUser.getId()+"";
        nick = currentUser.getMainName();
        sex = currentUser.getGender()+"";
//        sign = currentUser.get;

        region = currentUser.getArea();
        String avatar = currentUser.getAvatar();

        re_avatar = (RelativeLayout) this.findViewById(R.id.re_avatar);
        re_name = (RelativeLayout) this.findViewById(R.id.re_name);
//        re_fxid = (RelativeLayout) this.findViewById(R.id.re_fxid);
        re_sex = (RelativeLayout) this.findViewById(R.id.re_sex);
        re_region = (RelativeLayout) this.findViewById(R.id.re_region);
        re_erweima = (RelativeLayout) this.findViewById(R.id.re_erweima);
        re_avatar.setOnClickListener(new MyListener());
        re_name.setOnClickListener(new MyListener());
        re_sex.setOnClickListener(new MyListener());
        re_region.setOnClickListener(new MyListener());
        re_erweima.setOnClickListener(new MyListener());
        // 头像
        iv_avatar = (ImageView) this.findViewById(R.id.iv_avatar);
        tv_name = (TextView) this.findViewById(R.id.tv_name);
//        tv_fxid = (TextView) this.findViewById(R.id.tv_fxid);
        tv_sex = (TextView) this.findViewById(R.id.tv_sex);
        tv_sign = (TextView) this.findViewById(R.id.tv_sign);
        tv_region = (TextView) this.findViewById(R.id.tv_region);
        tv_name.setText(nick);
        if (StringUtils.notEmpty(region)) {
            tv_region.setText(region);

        } else {
            tv_region.setText("未知城市");
        }
        if (sex.equals("1")) {
            tv_sex.setText("男");

        } else if (sex.equals("2")) {
            tv_sex.setText("女");

        } else {
            tv_sex.setText("");
        }

//        if (sign.equals("0")) {
//            tv_sign.setText("未填写");
//        } else {
//            tv_sign.setText(sign);
//        }

//        showUserAvatar(iv_avatar, avatar);
//        refresh();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.left_btn:
            case R.id.left_txt:
                this.finish();
                break;
        }
    }

    class MyListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.re_avatar:
                    PhotoPicker.builder()
                            .setPhotoCount(1)
                            .start(MyUserInfoActivity.this);
                    break;
                case R.id.re_name:
//                    startActivityForResult(new Intent(MyUserInfoActivity.this,
//                            UpdateNickActivity.class),UPDATE_NICK);
                    break;
                case R.id.re_sex:
                    showSexDialog();
                    break;
                case R.id.re_region:
//                    startActivity(new Intent(MyUserInfoActivity.this, SelectProvinceActivity.class));
                    break;
                case R.id.re_erweima:
//                    startActivity(new Intent(MyUserInfoActivity.this, ErweimaActivity.class));
                    break;
            }
        }

    }

    private void showSexDialog() {
        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.show();
        Window window = dlg.getWindow();
        // *** 主要就是在这里实现这种效果的.
        // 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
        window.setContentView(R.layout.alertdialog);
        LinearLayout ll_title = (LinearLayout) window
                .findViewById(R.id.ll_title);
        ll_title.setVisibility(View.VISIBLE);
        TextView tv_title = (TextView) window.findViewById(R.id.tv_title);
        tv_title.setText("性别");
        // 为确认按钮添加事件,执行退出应用操作
        TextView tv_paizhao = (TextView) window.findViewById(R.id.tv_content1);
        tv_paizhao.setText("男");
        tv_paizhao.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SdCardPath")
            public void onClick(View v) {
                if (!sex.equals("1")) {
                    tv_sex.setText("男");

                    updateSex("1");
                }

                dlg.cancel();
            }
        });
        TextView tv_xiangce = (TextView) window.findViewById(R.id.tv_content2);
        tv_xiangce.setText("女");
        tv_xiangce.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!sex.equals("2")) {

                    tv_sex.setText("女");
                    updateSex("2");
                }

                dlg.cancel();
            }
        });

    }



    public void updateSex(final String sexnum) {
        String json = "{\"sex\":"+ "\"" + sexnum + "\"}";
//        updateUserInfo(json);
//        LocalUserInfo.getInstance(MyUserInfoActivity.this).setUserInfo("sex", sexnum);
    }

}
