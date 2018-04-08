package com.dtalk.dd.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.dtalk.dd.R;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.http.base.ClientCallback;
import com.dtalk.dd.http.moment.MomentClient;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.model.Photo4Gallery;
import com.dtalk.dd.qiniu.utils.QNUploadManager;
import com.dtalk.dd.ui.adapter.ShareListPicsAdapter;
import com.dtalk.dd.ui.base.TTBaseActivity;
import com.dtalk.dd.ui.widget.GridViewForScrollView;
import com.dtalk.dd.utils.KeyboardUtils;
import com.dtalk.dd.utils.SandboxUtils;
import com.dtalk.dd.utils.StringUtils;
import com.dtalk.tools.ScreenTools;
import com.lidroid.xutils.ViewUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPreview;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * Created by Donal on 16/11/3.
 */

public class CircleImagePubActivity extends TTBaseActivity implements
        View.OnClickListener, QNUploadManager.OnQNUploadCallback {
    private static final int EVENT_MESSAGE_MAX_COUNT = 500;

    private boolean iSPublic = true;

    EditText editText;
    GridViewForScrollView imageLayout;
    private List<Photo4Gallery> pics;
    private ShareListPicsAdapter picAdapter;
    LinearLayout limitLayout;

    private boolean haveAddImageView = true;

    ImageView addImageView;

    List<String> contentImages;

    TextView limit_content_text;

    private SVProgressHUD svProgressHUD;

    public static void launch(Context context) {
        Intent intent = new Intent(context, CircleImagePubActivity.class);
        context.startActivity(intent);
    }

    public void initUI() {
        setLeftButton(R.drawable.tt_top_back);
        setLeftText(getResources().getString(R.string.top_left_back));
        setTopRightText(getResources().getString(R.string.confirm));
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);
        topRightTitleTxt.setOnClickListener(this);
        contentImages = new ArrayList<String>();
        editText = (EditText) findViewById(R.id.content_edit);
        pics = new ArrayList<Photo4Gallery>();
        imageLayout = (GridViewForScrollView) findViewById(R.id.content_more_image);
        Photo4Gallery fp = new Photo4Gallery(Photo4Gallery.FUNCTION_TYPE);
        pics.add(fp);
        ScreenTools screenTools = ScreenTools.instance(this);
        int totalWidth = screenTools.getScreenWidth();
        int singleWidth = (totalWidth - 10 - 6) / 4;
        picAdapter = new ShareListPicsAdapter(this, pics, singleWidth);
        imageLayout.setAdapter(picAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.activity_circle_image_pub, topContentView);
        ViewUtils.inject(this);
        initUI();
        svProgressHUD = new SVProgressHUD(this);
    }

    public boolean isiSPublic() {
        return iSPublic;
    }

    public void setiSPublic(boolean iSPublic) {
        this.iSPublic = iSPublic;
    }

    public boolean isHaveAddImageView() {
        return haveAddImageView;
    }

    public void setHaveAddImageView(boolean haveAddImageView) {
        this.haveAddImageView = haveAddImageView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        svProgressHUD.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (resultCode == RESULT_OK &&
                (requestCode == PhotoPicker.REQUEST_CODE || requestCode == PhotoPreview.REQUEST_CODE)) {
            List<String> photos = null;
            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }
            if (photos != null) {
                displayImage(photos);
            }
        }
    }

    private void displayImage(List<String> photos) {
        for (String path : photos) {
            Photo4Gallery photo4Gallery = new Photo4Gallery();
            photo4Gallery.path = path;
            photo4Gallery.type = Photo4Gallery.NORMAL_YPE;
            pics.add(pics.size() - 1, photo4Gallery);
        }
        picAdapter.notifyDataSetChanged();
        changeContentImages();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_btn:
            case R.id.left_txt:
                onNaviBarClickCancel();
                break;
            case R.id.right_txt:
                onNaviBarClickSend();
                break;
            default:
                break;
        }
    }

    public void onNaviBarClickSend() {
        KeyboardUtils.hideSoftInput(this);
        if (editText.getText().length() > EVENT_MESSAGE_MAX_COUNT) {
            Toast.makeText(getApplication(), "消息的文字过长，请适当删除",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (contentImages.size() > 0) {
            svProgressHUD.getProgressBar().setProgress(0);
            svProgressHUD.showWithProgress("正在上传图片", SVProgressHUD.SVProgressHUDMaskType.Black);
            compressWithLs(contentImages);
        } else {
            if (StringUtils.empty(editText.getText().toString())) {
                Toast.makeText(getApplication(), "请输入文字",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            List<String> imgs = new ArrayList<>();
            postImage(imgs);
        }
    }

    private void handleImagePickData(List<String> list) {
        QNUploadManager.getInstance(this).uploadCircleFiles(list, svProgressHUD, this);
    }

    private void compressWithLs(final List<String> list) {
        final List<String> compressedFiles = new ArrayList<>();
        for (String item : list) {
            final File imgFile = new File(item);
            Luban.get(CircleImagePubActivity.this)
                    .load(imgFile)
                    .putGear(Luban.THIRD_GEAR)
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {
                        }

                        @Override
                        public void onSuccess(File file) {
                            compressedFiles.add(file.getAbsolutePath());
                            if (compressedFiles.size() == list.size()) {
                                handleImagePickData(compressedFiles);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            compressedFiles.add(imgFile.getAbsolutePath());
                            if (compressedFiles.size() == list.size()) {
                                handleImagePickData(compressedFiles);
                            }
                        }
                    }).launch();
        }
    }

    private void onNaviBarClickCancel() {
        KeyboardUtils.hideSoftInput(this);
        finish();
    }

    private void changeContentImages() {
        contentImages.clear();
        for (Photo4Gallery p : pics) {
            if (p.type == Photo4Gallery.NORMAL_YPE) {
                contentImages.add(p.path);
            }
        }
    }

    public void PhotoChooseOption() {
        PhotoPicker.builder()
                .setPhotoCount(30)
                .setGridColumnCount(4)
                .setShowGif(false)
                .start(CircleImagePubActivity.this);
    }

    public void DelAndShowbigImage(final int index) {
        CharSequence[] item = {"看大图", "删除"};
        AlertDialog imageDialog = new AlertDialog.Builder(this).setTitle(null).setIcon(android.R.drawable.btn_star)
                .setItems(item, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            // 看大图

                        } else if (item == 1) {
                            // 删除
                            DelImage(index);
                        }
                    }
                }).create();
        imageDialog.show();
    }

    public void DelImage(int index) {
        pics.remove(index);
        picAdapter.notifyDataSetChanged();
        changeContentImages();
    }

    public void showLimitDialog() {
        CharSequence[] item = {"公开", "不公开"};
        AlertDialog imageDialog = new AlertDialog.Builder(this).setTitle(null).setIcon(android.R.drawable.btn_star)
                .setItems(item, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            // 公开
                            limit_content_text.setText("公开");
                            setiSPublic(true);
                        } else if (item == 1) {
                            // 不公开
                            limit_content_text.setText("不公开");
                            setiSPublic(false);
                        }
                    }
                }).create();
        imageDialog.show();
    }

    private void postImage(List<String> images) {
        MomentClient.postImageMoment((String.valueOf(IMLoginManager.instance().getLoginId())), SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"), editText.getText().toString(), images, new ClientCallback() {
            @Override
            public void onPreConnection() {
                svProgressHUD.showWithStatus("发送朋友圈");
            }

            @Override
            public void onCloseConnection() {
                svProgressHUD.dismiss();
            }

            @Override
            public void onSuccess(Object data) {
                BaseResponse response = (BaseResponse) data;
                if (response.getStatus() == 1) {
                    finish();
                } else {
                    svProgressHUD.showErrorWithStatus(response.getMsg());
                }
            }

            @Override
            public void onFailure(String message) {
                svProgressHUD.showErrorWithStatus(message);
            }

            @Override
            public void onException(Exception e) {

            }
        });
    }

    @Override
    public void uploadCompleted(Map<String, String> uploadedFiles) {
        List<String> imgs = new ArrayList<>();
        imgs.addAll(uploadedFiles.values());
        postImage(imgs);
    }

}

