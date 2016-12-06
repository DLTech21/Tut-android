package com.dtalk.dd.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bigkoo.svprogresshud.SVProgressHUD;
import com.dtalk.dd.DB.sp.SystemConfigSp;
import com.dtalk.dd.R;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.config.SysConstant;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.http.moment.Comment;
import com.dtalk.dd.http.moment.Moment;
import com.dtalk.dd.http.moment.MomentClient;
import com.dtalk.dd.http.moment.MomentList;
import com.dtalk.dd.http.user.UserInfo;
import com.dtalk.dd.imservice.entity.ShortVideoMessage;
import com.dtalk.dd.imservice.event.ShortVideoPubEvent;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.qiniu.utils.QNUploadManager;
import com.dtalk.dd.ui.adapter.CircleAdapter;
import com.dtalk.dd.ui.base.TTBaseActivity;
import com.dtalk.dd.ui.helper.Emoparser;
import com.dtalk.dd.ui.plugin.ImageLoadManager;
import com.dtalk.dd.ui.widget.CustomEditView;
import com.dtalk.dd.ui.widget.EmoGridView;
import com.dtalk.dd.ui.widget.Lu_Comment_TextView;
import com.dtalk.dd.ui.widget.YayaEmoGridView;
import com.dtalk.dd.ui.widget.circle.BaseCircleRenderView;
import com.dtalk.dd.ui.widget.ptrwidget.FriendCirclePtrListView;
import com.dtalk.dd.ui.widget.ptrwidget.OnLoadMoreRefreshListener;
import com.dtalk.dd.ui.widget.ptrwidget.OnPullDownRefreshListener;
import com.dtalk.dd.ui.widget.ptrwidget.PullMode;
import com.dtalk.dd.utils.ImageLoaderUtil;
import com.dtalk.dd.utils.KeyboardUtils;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.SandboxUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.yixia.camera.demo.ui.record.MediaRecorderActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import in.srain.cube.views.ptr.PtrClassicDefaultHeader;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

import static com.yixia.camera.demo.utils.ToastUtils.showToast;

/**
 * Created by Donal on 16/7/29.
 */
public class CircleActivity extends TTBaseActivity implements View.OnClickListener,
        BaseCircleRenderView.OnDeleteCircleListener,
        TextWatcher,
        BaseCircleRenderView.OnMoreCircleListener {
    public static final String IS_SELF = "IS_SELF";
    private boolean isSelf;
    private CircleAdapter adapter;
    private View friendCircleHeader;
    private FriendCirclePtrListView listView;
    String lastId;
    private int lvDataState;
    private SVProgressHUD svProgressHUD;
    @ViewInject(R.id.tt_layout_bottom)
    RelativeLayout tt_layout_bottom;
    @ViewInject(R.id.pannel_container)
    RelativeLayout pannelContainer;
    @ViewInject(R.id.message_text)
    CustomEditView messageEdt;
    @ViewInject(R.id.show_emo_btn)
    ImageView addEmoBtn;
    @ViewInject(R.id.send_message_btn)
    TextView sendBtn;
    @ViewInject(R.id.emo_layout)
    private LinearLayout emoLayout;
    private EmoGridView emoGridView = null;
    private InputMethodManager inputManager = null;
    private int rootBottom = Integer.MIN_VALUE, keyboardHeight = 0;
    private switchInputMethodReceiver receiver;
    private String currentInputMethod;
    private String replyCommentUid;
    private ImageView avatarImage;
    private ImageView coverImage;
    //    @Override
//    protected void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putInt(STATE_SCORE, listView.getFirstVisiblePosition());
//        savedInstanceState.putSerializable("listdata", (Serializable) adapter.getCircleObjectList());
//
//        super.onSaveInstanceState(savedInstanceState);
//    }
    private String fid;
    private String avatar;
    private String cover;

    public static void openCircle(Context context, boolean isSelf, String fid, String avatar, String cover) {
        context.startActivity(new Intent(context, CircleActivity.class)
                .putExtra(IS_SELF, isSelf)
                .putExtra("fid", fid)
                .putExtra("avatar", avatar)
                .putExtra("cover", cover));
    }

    public static void openCircle(Context context, boolean isSelf) {
        context.startActivity(new Intent(context, CircleActivity.class).putExtra(IS_SELF, isSelf));
    }

    @Override
    public void onBackPressed() {
        if (tt_layout_bottom.getVisibility() == View.VISIBLE) {
            tt_layout_bottom.setVisibility(View.INVISIBLE);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        EventBus.getDefault().unregister(this);
//        MomentList temp = new MomentList();
//        temp.list = new ArrayList<>();
//        temp.list.addAll(adapter.getCircleObjectList());
//        temp.status = 1;
//        SandboxUtils.getInstance().saveObject(IMApplication.getInstance(), temp, "circle");
        super.onDestroy();
        svProgressHUD.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        isSelf = getIntent().getBooleanExtra(IS_SELF, false);
        if (isSelf) {
            fid = getIntent().getStringExtra("fid");
            avatar = getIntent().getStringExtra("avatar");
            cover = getIntent().getStringExtra("cover");
        }
        svProgressHUD = new SVProgressHUD(this);
        initSoftInputMethod();
        initView();
//        MomentList temp = (MomentList) SandboxUtils.getInstance().readObject(IMApplication.getInstance(), "circle");
//        if (temp != null) {
//            adapter.addItemList(temp.list);
//            listView.setSelectionFromTop(temp.status, 0);
//            if (!temp.list.isEmpty()) {
//                lastId = temp.list.get(temp.list.size() - 1).moment_id;
//                lvDataState = SysConstant.LISTVIEW_DATA_MORE;
//            } else {
//                lvDataState = SysConstant.LISTVIEW_DATA_FULL;
//                if (lvDataState == SysConstant.LISTVIEW_DATA_FULL) {
//                    ptr_classic_footer_rotate_view_footer_title.setText(R.string.cube_ptr_finish);
//                    ptr_classic_footer_rotate_view_progressbar.setVisibility(View.GONE);
//                }
//            }
//        } else {
        fetchMoments("0");
//        }
    }

    private void initView() {
        LayoutInflater.from(this).inflate(R.layout.activity_circle, topContentView);
        ViewUtils.inject(this);
        setLeftButton(R.drawable.tt_top_back);
        setLeftText(getResources().getString(R.string.top_left_back));
        if (!isSelf) {
            setRightButton(R.drawable.circle_camera);
        }
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);
        topRightBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
        messageEdt.setOnFocusChangeListener(msgEditOnFocusChangeListener);
        messageEdt.setOnClickListener(this);
        messageEdt.addTextChangedListener(this);

        listView = (FriendCirclePtrListView) findViewById(R.id.lvShare);
        friendCircleHeader = LayoutInflater.from(this).inflate(R.layout.item_header, null, false);
        avatarImage = (ImageView) friendCircleHeader.findViewById(R.id.friend_avatar);
        coverImage = (ImageView) friendCircleHeader.findViewById(R.id.friend_wall_pic);
        listView.setVerticalScrollBarEnabled(false);
        adapter = new CircleAdapter(this, this, this, isSelf);
        listView.setRotateIcon((ImageView) findViewById(R.id.rotate_icon));
        listView.addHeaderView(friendCircleHeader);
        listView.setAdapter(adapter);
        listView.setOnPullDownRefreshListener(new OnPullDownRefreshListener() {
            @Override
            public void onRefreshing(PtrFrameLayout frame) {
                onPullDownRefresh();
            }
        });
        listView.setOnLoadMoreRefreshListener(new OnLoadMoreRefreshListener() {
            @Override
            public void onRefreshing(PtrFrameLayout frame) {
                onLoadMore();
            }
        });
        listView.manualRefresh();
        lastId = "0";
        addEmoBtn.setOnClickListener(this);
        RelativeLayout.LayoutParams paramEmoLayout = (RelativeLayout.LayoutParams) emoLayout.getLayoutParams();
        if (keyboardHeight > 0) {
            paramEmoLayout.height = keyboardHeight;
            emoLayout.setLayoutParams(paramEmoLayout);
        }
        Emoparser.getInstance(CircleActivity.this);
        emoGridView = (EmoGridView) findViewById(R.id.emo_gridview);
        emoGridView.setOnEmoGridViewItemClick(onEmoGridViewItemClick);
        emoGridView.setAdapter();
        baseRoot.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        if (isSelf) {
            ImageLoadManager.setCircleAvatarGlide(this, avatar, avatarImage);
            ImageLoadManager.setCircleCoverGlide(this, cover, coverImage);
        } else {
            ImageLoadManager.setCircleAvatarGlide(this, SandboxUtils.getInstance().get(this, "avatar"), avatarImage);
            ImageLoadManager.setCircleCoverGlide(this, SandboxUtils.getInstance().get(this, "cover"), coverImage);
        }
    }

    private void initSoftInputMethod() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        receiver = new CircleActivity.switchInputMethodReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.INPUT_METHOD_CHANGED");
        registerReceiver(receiver, filter);

        SystemConfigSp.instance().init(this);
        currentInputMethod = Settings.Secure.getString(CircleActivity.this.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        keyboardHeight = SystemConfigSp.instance().getIntConfig(currentInputMethod);
    }

    public void onPullDownRefresh() {
        lastId = "0";
        fetchMoments(lastId);
    }

    public void onLoadMore() {
        fetchMoments(lastId);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.left_btn:
            case R.id.left_txt:
                finish();
                break;
            case R.id.right_btn:
                new MaterialDialog.Builder(this)
                        .items(R.array.circle_media)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        CircleImagePubActivity.launch(CircleActivity.this);
                                        break;
                                    case 1:
                                        takeShortVideo();
                                        break;
                                }
                            }
                        })
                        .show();
                break;
            case R.id.send_message_btn:
                sendComment();
                break;
            case R.id.show_emo_btn: {
                if (keyboardHeight != 0) {
                    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                }
                if (emoLayout.getVisibility() == View.VISIBLE) {
                    emoGridView.setVisibility(View.VISIBLE);
                    if (!messageEdt.hasFocus()) {
                        messageEdt.requestFocus();
                    }
                    inputManager.toggleSoftInputFromWindow(messageEdt.getWindowToken(), 1, 0);
                    if (keyboardHeight == 0) {
                        emoLayout.setVisibility(View.GONE);
                    }
                }
            }
            break;
            default:
                break;
        }
    }

    private void sendComment() {
        String content = messageEdt.getText().toString();
        if (content.trim().equals("")) {
            Toast.makeText(CircleActivity.this,
                    getResources().getString(R.string.message_null), Toast.LENGTH_LONG).show();
            return;
        }
        final Moment moment = (Moment) messageEdt.getTag();
        final String moment_id = moment.moment_id;
        MomentClient.commentMoment(moment_id, replyCommentUid, content, new BaseClient.ClientCallback() {
            public void onPreConnection() {
                svProgressHUD.show();
            }

            @Override
            public void onCloseConnection() {
                svProgressHUD.dismiss();
            }

            @Override
            public void onSuccess(Object data) {
                Comment comm = (Comment) data;
                moment.comment.add(comm);
                adapter.notifyDataSetChanged();
                messageEdt.setText("");
                if (tt_layout_bottom.getVisibility() == View.VISIBLE) {
                    tt_layout_bottom.setVisibility(View.INVISIBLE);
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

    private void takeShortVideo() {
        Intent intent = new Intent(getApplication(),
                MediaRecorderActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_up_in,
                R.anim.push_up_out);
    }

    public void onEventMainThread(ShortVideoPubEvent event) {
        handleSendVideoAttachMessage(event.cover, event.path);
    }

    /**
     * 处理发送小视频
     *
     * @param coverName
     * @param pathName
     */
    private void handleSendVideoAttachMessage(String coverName, final String pathName) {
        if (TextUtils.isEmpty(pathName)) {
            return;
        }
        List<String> list = new ArrayList<>();
        list.add(coverName);
        svProgressHUD.getProgressBar().setProgress(0);
        svProgressHUD.showWithProgress("正在上传小视频", SVProgressHUD.SVProgressHUDMaskType.Black);
        QNUploadManager.getInstance(this).uploadCircleFiles(list, null, new QNUploadManager.OnQNUploadCallback() {
            @Override
            public void uploadCompleted(Map<String, String> uploadedFiles) {
                Logger.d(uploadedFiles.values().toString());
                uploadVideo((String) uploadedFiles.values().toArray()[0], pathName);
            }
        });
    }

    private void uploadVideo(final String videoCover, final String path) {
        svProgressHUD.getProgressBar().setProgress(0);
        QNUploadManager.getInstance(this).uploadCircleVideo(path, svProgressHUD, new QNUploadManager.OnQNUploadCallback() {
            @Override
            public void uploadCompleted(Map<String, String> uploadedFiles) {
                postVideo((String) uploadedFiles.values().toArray()[0], videoCover, path);
            }
        });
    }

    public void pushList(List<Moment> entityList) {
        adapter.addItemList(entityList);
    }

    private void fetchMoments(final String last) {
        if (isSelf) {
            MomentClient.fetchOnesMoment(fid, last, "10", new BaseClient.ClientCallback() {

                @Override
                public void onSuccess(Object data) {
                    MomentList list = (MomentList) data;
                    if (last.equals("0")) {
                        adapter.clearAllItem();
                    }
                    pushList(list.list);
                    if (!list.list.isEmpty()) {
                        lastId = list.list.get(list.list.size() - 1).moment_id;
                        lvDataState = SysConstant.LISTVIEW_DATA_MORE;
                        listView.setHasMore(true);
                    } else {
                        lvDataState = SysConstant.LISTVIEW_DATA_FULL;
                        listView.setHasMore(false);
                    }
                }

                @Override
                public void onPreConnection() {

                }

                @Override
                public void onFailure(String message) {

                }

                @Override
                public void onException(Exception e) {

                }

                @Override
                public void onCloseConnection() {
                    if (listView != null) {
                        listView.refreshComplete();
                        if (listView.getCurMode() == PullMode.FROM_BOTTOM) {
                            listView.loadmoreCompelete();
                        }
                    }
                }
            });
        } else {
            MomentClient.fetchMoment((String.valueOf(IMLoginManager.instance().getLoginId())), SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"), last, "10", new BaseClient.ClientCallback() {

                @Override
                public void onSuccess(Object data) {
                    MomentList list = (MomentList) data;
                    if (last.equals("0")) {
                        adapter.clearAllItem();
                    }
                    pushList(list.list);
                    if (!list.list.isEmpty()) {
                        lastId = list.list.get(list.list.size() - 1).moment_id;
                        lvDataState = SysConstant.LISTVIEW_DATA_MORE;
                        listView.setHasMore(true);
                    } else {
                        lvDataState = SysConstant.LISTVIEW_DATA_FULL;
                        listView.setHasMore(false);
                    }
                }

                @Override
                public void onPreConnection() {
                }

                @Override
                public void onFailure(String message) {
                }

                @Override
                public void onException(Exception e) {
                }


                @Override
                public void onCloseConnection() {
                    if (listView != null) {
                        listView.refreshComplete();
                        if (listView.getCurMode() == PullMode.FROM_BOTTOM) {
                            listView.loadmoreCompelete();
                        }
                    }
                }
            });
        }
    }

    private EmoGridView.OnEmoGridViewItemClick onEmoGridViewItemClick = new EmoGridView.OnEmoGridViewItemClick() {
        @Override
        public void onItemClick(int facesPos, int viewIndex) {
            int deleteId = (++viewIndex) * (SysConstant.pageSize - 1);
            if (deleteId > Emoparser.getInstance(CircleActivity.this).getResIdList().length) {
                deleteId = Emoparser.getInstance(CircleActivity.this).getResIdList().length;
            }
            if (deleteId == facesPos) {
                String msgContent = messageEdt.getText().toString();
                if (msgContent.isEmpty())
                    return;
                if (msgContent.contains("["))
                    msgContent = msgContent.substring(0, msgContent.lastIndexOf("["));
                messageEdt.setText(msgContent);
            } else {
                int resId = Emoparser.getInstance(CircleActivity.this).getResIdList()[facesPos];
                String pharse = Emoparser.getInstance(CircleActivity.this).getIdPhraseMap()
                        .get(resId);
                int startIndex = messageEdt.getSelectionStart();
                Editable edit = messageEdt.getEditableText();
                if (startIndex < 0 || startIndex >= edit.length()) {
                    if (null != pharse) {
                        edit.append(pharse);
                    }
                } else {
                    if (null != pharse) {
                        edit.insert(startIndex, pharse);
                    }
                }
            }
            Editable edtable = messageEdt.getText();
            int position = edtable.length();
            Selection.setSelection(edtable, position);
        }
    };

    private View.OnTouchListener lvPTROnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
            }
            return false;
        }
    };

    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            baseRoot.getGlobalVisibleRect(r);
            if (rootBottom == Integer.MIN_VALUE) {
                rootBottom = r.bottom;
                return;
            }
            // adjustResize，软键盘弹出后高度会变小
            if (r.bottom < rootBottom) {
                //按照键盘高度设置表情框和发送图片按钮框的高度
                keyboardHeight = rootBottom - r.bottom;
                SystemConfigSp.instance().init(CircleActivity.this);
                SystemConfigSp.instance().setIntConfig(currentInputMethod, keyboardHeight);
                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) emoLayout.getLayoutParams();
                params1.height = keyboardHeight;
            }
        }
    };


    private View.OnFocusChangeListener msgEditOnFocusChangeListener = new android.view.View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                if (keyboardHeight == 0) {
                    emoLayout.setVisibility(View.GONE);
                } else {
                    CircleActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                    if (emoLayout.getVisibility() == View.GONE) {
                        emoLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };

    private void postVideo(String videoUrl, String videoCover, String localPath) {
        MomentClient.postVideoMoment((String.valueOf(IMLoginManager.instance().getLoginId())), SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"), videoUrl, videoCover, localPath, new BaseClient.ClientCallback() {
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
                    lastId = "0";
                    fetchMoments(lastId);
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
    public void onDeleteCircle(final Moment moment) {
        new MaterialDialog.Builder(this)
                .title("提示")
                .content("删除该条朋友圈?")
                .positiveText("确定")
                .negativeText("返回")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        deleteCircle(moment);
                    }
                })
                .show();
    }

    private void deleteComment(Moment moment, Lu_Comment_TextView.Lu_PingLun_info_Entity lu_pingLun_info_entity, final int itemposition) {
        moment.comment.remove(itemposition);
        adapter.notifyDataSetChanged();
        MomentClient.delcommentMoment(lu_pingLun_info_entity.getID(), null);
    }

    private void deleteCircle(Moment moment) {
        adapter.getCircleObjectList().remove(moment);
        adapter.notifyDataSetChanged();
        MomentClient.deleteMoment((String.valueOf(IMLoginManager.instance().getLoginId())), SandboxUtils.getInstance().get(IMApplication.getInstance(), "token"), moment.moment_id, null);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            sendBtn.setEnabled(true);
        } else {
            sendBtn.setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onFavorClick(Moment moment, int position) {
        favor(moment);
    }

    private void favor(final Moment moment) {
        MomentClient.likeMoment(moment.moment_id, !moment.isFavor, new BaseClient.ClientCallback() {
            @Override
            public void onPreConnection() {
            }

            @Override
            public void onCloseConnection() {
            }

            @Override
            public void onSuccess(Object data) {
                UserInfo comm = (UserInfo) data;
                moment.isFavor = !moment.isFavor;
                if (moment.isFavor) {
                    moment.like_maps.put(comm.getUid(), comm);
                } else {
                    moment.like_maps.remove(comm.getUid());
                }
                adapter.notifyDataSetChanged();
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
    public void onCommentClick(final Moment moment, int position, final int itemposition, final Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity) {
        if (mLu_pingLun_info_entity != null) {
            if (mLu_pingLun_info_entity.getUser_A_ID().equals(String.valueOf(IMLoginManager.instance().getLoginId()))) {
                new MaterialDialog.Builder(this)
                        .title("提示")
                        .content("删除该条评论?")
                        .positiveText("确定")
                        .negativeText("返回")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                deleteComment(moment, mLu_pingLun_info_entity, itemposition);
                            }
                        })
                        .show();
                return;
            } else {
                messageEdt.setHint("回复" + mLu_pingLun_info_entity.getUser_A_Name() + ":");
                replyCommentUid = mLu_pingLun_info_entity.getUser_A_ID();
            }
        } else {
            messageEdt.setHint("");
            replyCommentUid = moment.uid;
        }
        messageEdt.setTag(moment);
        tt_layout_bottom.setVisibility(View.VISIBLE);
        KeyboardUtils.showSoftInput(this, messageEdt);
    }

    private class switchInputMethodReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.INPUT_METHOD_CHANGED")) {
                currentInputMethod = Settings.Secure.getString(CircleActivity.this.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
                SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.DEFAULTINPUTMETHOD, currentInputMethod);
                int height = SystemConfigSp.instance().getIntConfig(currentInputMethod);
                if (keyboardHeight != height) {
                    keyboardHeight = height;
                    emoLayout.setVisibility(View.GONE);
                    CircleActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    messageEdt.requestFocus();
                    if (keyboardHeight != 0 && emoLayout.getLayoutParams().height != keyboardHeight) {
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) emoLayout.getLayoutParams();
                        params.height = keyboardHeight;
                    }
                } else {
                    emoLayout.setVisibility(View.VISIBLE);
                    CircleActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                    messageEdt.requestFocus();
                }
            }
        }
    }
}
