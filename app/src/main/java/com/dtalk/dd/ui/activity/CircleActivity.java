package com.dtalk.dd.ui.activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dtalk.dd.R;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.config.SysConstant;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.moment.Moment;
import com.dtalk.dd.http.moment.MomentClient;
import com.dtalk.dd.http.moment.MomentList;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.ui.adapter.CircleAdapter;
import com.dtalk.dd.ui.base.TTBaseActivity;
import com.dtalk.dd.utils.SandboxUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicDefaultHeader;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

import static com.yixia.camera.demo.utils.ToastUtils.showToast;

/**
 * Created by Donal on 16/7/29.
 */
public class CircleActivity extends TTBaseActivity implements View.OnClickListener, AbsListView.OnScrollListener {
    CircleAdapter adapter;

    @ViewInject(R.id.ptrFrameLayoutShare)
    private PtrFrameLayout ptrFrameLayoutShare;
    private View footer;
    @ViewInject(R.id.ptr_classic_footer_rotate_view_footer_title)
    private TextView ptr_classic_footer_rotate_view_footer_title;
    @ViewInject(R.id.ptr_classic_footer_rotate_view_progressbar)
    private ProgressBar ptr_classic_footer_rotate_view_progressbar;
    private ListView listView;
    String lastId;
    private int lvDataState;

//    @Override
//    protected void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putInt(STATE_SCORE, listView.getFirstVisiblePosition());
//        savedInstanceState.putSerializable("listdata", (Serializable) adapter.getCircleObjectList());
//
//        super.onSaveInstanceState(savedInstanceState);
//    }

    @Override
    protected void onDestroy() {
        MomentList temp = new MomentList();
        temp.list = new ArrayList<>();
        temp.list.addAll(adapter.getCircleObjectList());
        temp.status = listView.getFirstVisiblePosition();
        SandboxUtils.getInstance().saveObject(IMApplication.getInstance(), temp, "circle");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        MomentList temp = (MomentList) SandboxUtils.getInstance().readObject(IMApplication.getInstance(), "circle");
        if (temp != null) {
            adapter.addItemList(temp.list);
            listView.setSelectionFromTop(temp.status, 0);
            if (!temp.list.isEmpty()) {
                lastId = temp.list.get(temp.list.size() - 1).moment_id;
                lvDataState = SysConstant.LISTVIEW_DATA_MORE;
            } else {
                lvDataState = SysConstant.LISTVIEW_DATA_FULL;
                if (lvDataState == SysConstant.LISTVIEW_DATA_FULL) {
                    ptr_classic_footer_rotate_view_footer_title.setText(R.string.cube_ptr_finish);
                    ptr_classic_footer_rotate_view_progressbar.setVisibility(View.GONE);
                }
            }
        } else {
            fetchMoments("0");
        }
    }

    private void initView() {
        LayoutInflater.from(this).inflate(R.layout.activity_circle, topContentView);
        ViewUtils.inject(this);
        setLeftButton(R.drawable.tt_top_back);
        setLeftText(getResources().getString(R.string.top_left_back));
        setRightButton(R.drawable.circle_camera);
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);
        topRightBtn.setOnClickListener(this);
        PtrClassicDefaultHeader ptrHeader = new PtrClassicDefaultHeader(this);
        ptrFrameLayoutShare.setDurationToCloseHeader(500);
        ptrFrameLayoutShare.setHeaderView(ptrHeader);
        ptrFrameLayoutShare.addPtrUIHandler(ptrHeader);
        ptrFrameLayoutShare.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(ptrFrameLayoutShare, content, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                lastId = "0";
                fetchMoments(lastId);
            }
        });

        listView = (ListView) findViewById(R.id.lvShare);
//        listView.addHeaderView(header);
        footer = LayoutInflater.from(this).inflate(R.layout.cube_ptr_classic_default_footer, null);
        ViewUtils.inject(this, footer);
        listView.addFooterView(footer, null, false);
        listView.setVerticalScrollBarEnabled(false);
        listView.setOnScrollListener(this);

        adapter = new CircleAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        lastId = "0";
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
                        .items(R.array.socialNetworks)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            }
                        })
                        .show();
//                CircleImagePubActivity.launch(this);
                break;
            default:
                break;
        }
    }

    /**
     * @param msg
     */
    public void pushList(Moment msg) {
        adapter.addItem(msg);
    }

    public void pushList(List<Moment> entityList) {
        adapter.addItemList(entityList);
    }

    private void fetchMoments(final String last) {
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
                } else {
                    lvDataState = SysConstant.LISTVIEW_DATA_FULL;
                    if (lvDataState == SysConstant.LISTVIEW_DATA_FULL) {
                        ptr_classic_footer_rotate_view_footer_title.setText(R.string.cube_ptr_finish);
                        ptr_classic_footer_rotate_view_progressbar.setVisibility(View.GONE);
                    }
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
                if (ptrFrameLayoutShare != null) {
                    ptrFrameLayoutShare.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ptrFrameLayoutShare.refreshComplete();
                        }
                    }, 1000);
                }
            }
        });
    }

    private View.OnTouchListener lvPTROnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
            }
            return false;
        }
    };

    @Override
    public void onScrollStateChanged(AbsListView view, int i) {
        boolean scrollEnd = false;
        try {
            if (view.getPositionForView(footer) == view.getLastVisiblePosition())
                scrollEnd = true;
        } catch (Exception e) {
            scrollEnd = false;
        }
        if (scrollEnd && lvDataState == SysConstant.LISTVIEW_DATA_MORE) {
            lvDataState = SysConstant.LISTVIEW_DATA_LOADING;
            ptr_classic_footer_rotate_view_footer_title.setText(R.string.cube_ptr_refreshing);
            ptr_classic_footer_rotate_view_progressbar.setVisibility(View.VISIBLE);
            fetchMoments(lastId);
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {

    }
}
