package com.dtalk.dd.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.dtalk.dd.R;
import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.config.SysConstant;
import com.dtalk.dd.config.UrlConstant;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.http.base.ClientCallback;
import com.dtalk.dd.http.register.RegisterClient;
import com.dtalk.dd.imservice.event.RegisterEvent;
import com.dtalk.dd.qiniu.utils.Mac;
import com.dtalk.dd.qiniu.utils.PutPolicy;
import com.dtalk.dd.qiniu.utils.QNUploadManager;
import com.dtalk.dd.ui.base.TTBaseActivity;
import com.dtalk.dd.ui.plugin.ImageLoadManager;
import com.dtalk.dd.utils.CommonUtil;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.MD5Util;
import com.dtalk.dd.utils.ThemeUtils;
import com.dtalk.dd.utils.ViewUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPreview;


/**
 * Created by Donal on 16/4/19.
 */
public class RegisterActivity extends TTBaseActivity implements View.OnClickListener{
    private InputMethodManager intputManager;
    private String sex ;
    private String avatar ;

    private EditText et_usernick;
    private EditText et_usertel;
    private EditText et_password;
    private Button btn_register;
    private TextView tv_xieyi;
    private ImageView iv_hide;
    private ImageView iv_show;
    private ImageView iv_photo;

    private String mobile;
    private String code;
    private String url;
    private SVProgressHUD svProgressHUD;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        svProgressHUD.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        svProgressHUD = new SVProgressHUD(this);
        LayoutInflater.from(this).inflate(R.layout.tt_activity_register, topContentView);
        intputManager = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        sex = DBConstant.SEX_MAILE+"";
        url = DBConstant.SEX_MALE_AVATAR;
        initView();
    }

    private void initView() {
        setLeftButton(R.drawable.tt_top_back);
        setLeftText(getResources().getString(R.string.top_left_back));
        setTitle("注册");
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);
        topRightBtn.setOnClickListener(this);

        mobile = getIntent().getStringExtra("mobile");

        et_usernick = (EditText) findViewById(R.id.et_usernick);

        et_usertel = (EditText) findViewById(R.id.et_usertel);
        et_usertel.setText(mobile);
        et_usertel.setEnabled(false);
        et_password = (EditText) findViewById(R.id.et_password);

        // 监听多个输入框
        et_usernick.addTextChangedListener(new TextChange());
        et_usertel.addTextChangedListener(new TextChange());
        et_password.addTextChangedListener(new TextChange());
        btn_register = (Button) findViewById(R.id.btn_register);
        btn_register.setOnClickListener(this);
        tv_xieyi = (TextView) findViewById(R.id.tv_xieyi);
        iv_hide = (ImageView) findViewById(R.id.iv_hide);

        iv_show = (ImageView) findViewById(R.id.iv_show);
        iv_photo = (ImageView) findViewById(R.id.iv_photo);
        String xieyi = "<font color=" + "\"" + "#AAAAAA" + "\">" + "点击上面的"
                + "\"" + "注册" + "\"" + "按钮,即表示你同意" + "</font>" + "<u>"
                + "<font color=" + "\"" + "#576B95" + "\">" + "《盼盼软件许可及服务协议》"
                + "</font>" + "</u>";

        tv_xieyi.setText(Html.fromHtml(xieyi));
        iv_hide.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                iv_hide.setVisibility(View.GONE);
                iv_show.setVisibility(View.VISIBLE);
                et_password
                        .setTransformationMethod(HideReturnsTransformationMethod
                                .getInstance());
                // 切换后将EditText光标置于末尾
                CharSequence charSequence = et_password.getText();
                if (charSequence instanceof Spannable) {
                    Spannable spanText = (Spannable) charSequence;
                    Selection.setSelection(spanText, charSequence.length());
                }
            }
        });
        iv_show.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                iv_show.setVisibility(View.GONE);
                iv_hide.setVisibility(View.VISIBLE);
                et_password
                        .setTransformationMethod(PasswordTransformationMethod
                                .getInstance());
                // 切换后将EditText光标置于末尾
                CharSequence charSequence = et_password.getText();
                if (charSequence instanceof Spannable) {
                    Spannable spanText = (Spannable) charSequence;
                    Selection.setSelection(spanText, charSequence.length());
                }
            }

        });
        iv_photo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showCamera();
            }

        });

    }

    private void showCamera() {

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(RegisterActivity.this, android.R.style.Theme_Holo_Light_Dialog));
        builder.setTitle("头像");
        String[] items = new String[]{"拍照",
                "相册"};

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0 : {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        avatar = CommonUtil.getImageSavePath(String.valueOf(System
                                .currentTimeMillis())
                                + ".png");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(avatar)));
                        startActivityForResult(intent, SysConstant.CAMERA_WITH_DATA);
                    }
                        break;
                    case 1 : {
//                        if (albumList.size() < 1) {
//                            Toast.makeText(RegisterActivity.this,
//                                    getResources().getString(R.string.not_found_album), Toast.LENGTH_LONG)
//                                    .show();
//                            return;
//                        }
                        // 选择图片的时候要将session的整个回话 传过来
//                        Intent intent = new Intent(RegisterActivity.this, PickAvatarPhotoActivity.class);
//                        startActivityForResult(intent, SysConstant.ALBUM_BACK_DATA);
                        PhotoPicker.builder()
                                .setPhotoCount(1)
                                .start(RegisterActivity.this);

                        RegisterActivity.this.overridePendingTransition(R.anim.tt_album_enter, R.anim.tt_stay);
                    }
                        break;
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();

    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.left_btn:
            case R.id.left_txt:
                RegisterActivity.this.finish();
                break;
            case R.id.btn_register:
                doRegist();
                break;
//            case R.id.male_button: {
//                sex = DBConstant.SEX_MAILE + "";
//                avatar = DBConstant.SEX_MALE_AVATAR;
//                GenericDraweeHierarchy hierarchy = iconDraweeView.getHierarchy();
//                hierarchy.setPlaceholderImage(R.drawable.avatar_male);
//            }
//                break;
//            case R.id.female_button: {
//                sex = DBConstant.SEX_FEMALE + "";
//                avatar = DBConstant.SEX_FEMALE_AVATAR;
//                GenericDraweeHierarchy hierarchy = iconDraweeView.getHierarchy();
//                hierarchy.setPlaceholderImage(R.drawable.avatar_female);
//            }
//                break;
            default:
                break;
        }
    }


    private void doRegist() {
        String nickname = et_usernick.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        String username = et_usertel.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, getString(R.string.error_pwd_required), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, getString(R.string.error_name_required), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(nickname)) {
            Toast.makeText(this, getString(R.string.error_name_required), Toast.LENGTH_SHORT).show();
            return;
        }
        RegisterClient.registerUser(username, password, nickname, url, new ClientCallback() {
            @Override
            public void onPreConnection() {
                ViewUtils.createProgressDialog(getRunningActivity(), "正在注册...", ThemeUtils.getThemeColor()).show();
            }

            @Override
            public void onCloseConnection() {
                ViewUtils.dismissProgressDialog();
            }

            @Override
            public void onSuccess(Object data) {
                BaseResponse res = (BaseResponse) data;
                if (res.getStatus() == 0) {
                    ViewUtils.showMessage("注册成功");
                    EventBus.getDefault().post(new RegisterEvent());
                    RegisterActivity.this.finish();
                } else {
                    Toast.makeText(RegisterActivity.this, res.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Exception e) {
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

            boolean Sign1 = et_usernick.getText().length() > 0;
            boolean Sign2 = et_usertel.getText().length() > 0;
            boolean Sign3 = et_password.getText().length() > 0;

            if (Sign1 & Sign2 & Sign3) {
                btn_register.setTextColor(0xFFFFFFFF);
                btn_register.setEnabled(true);
            }
            // 在layout文件中，对Button的text属性应预先设置默认值，否则刚打开程序的时候Button是无显示的
            else {
                btn_register.setTextColor(0xFFD0EFC6);
                btn_register.setEnabled(false);
            }
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK &&
                (requestCode == PhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {

            List<String> photos = null;
            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }
            if (photos != null) {
                handleTakePhotoData(photos);
            }
        }
    }

    public void handleTakePhotoData(List<String> photos) {
        if (photos != null || photos.size() > 0) {
            avatar = photos.get(0);
            ImageLoadManager.setImageGlide(this, "file://"+avatar, iv_photo);
            String qiniuKey = "avatar/" + MD5Util.getMD5String(avatar) + ".png";
            url = UrlConstant.QINIU_PREFIX + qiniuKey;
            uploadAvatar(avatar, qiniuKey);
        }

    }

    private void uploadAvatar(String path, String qiniuKey) {
        List<String> list = new ArrayList<>();
        list.add(path);
        svProgressHUD.getProgressBar().setProgress(0);
        svProgressHUD.showWithProgress("正在上传头像", SVProgressHUD.SVProgressHUDMaskType.Black);
        QNUploadManager.getInstance(this).uploadCircleFiles(list, svProgressHUD, new QNUploadManager.OnQNUploadCallback() {
            @Override
            public void uploadCompleted(Map<String, String> uploadedFiles) {
                svProgressHUD.dismiss();
                url = (String) uploadedFiles.values().toArray()[0];
            }
        });
    }
}
