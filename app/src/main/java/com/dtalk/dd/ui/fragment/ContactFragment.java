package com.dtalk.dd.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dtalk.dd.DB.entity.DepartmentEntity;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.config.HandlerConstant;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.http.friend.FriendClient;
import com.dtalk.dd.imservice.event.GroupEvent;
import com.dtalk.dd.imservice.event.UserInfoEvent;
import com.dtalk.dd.imservice.manager.IMLoginManager;
import com.dtalk.dd.imservice.support.IMServiceConnector;
import com.dtalk.dd.imservice.manager.IMContactManager;
import com.dtalk.dd.imservice.service.IMService;
import com.dtalk.dd.ui.activity.CircleActivity;
import com.dtalk.dd.ui.adapter.ContactAdapter;
import com.dtalk.dd.ui.adapter.DeptAdapter;
import com.dtalk.dd.ui.widget.SortSideBar;
import com.dtalk.dd.ui.widget.SortSideBar.OnTouchingLetterChangedListener;
import com.dtalk.dd.ui.widget.flingswipe.SwipeFlingAdapterView;
import com.dtalk.dd.ui.widget.flingswipe.SwipeFlingAdapterView.OnItemClickListener;
import com.dtalk.dd.utils.IMUIHelper;
import com.dtalk.dd.utils.ThemeUtils;
import com.dtalk.dd.utils.ViewUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 通讯录 （全部、部门）
 */
public class ContactFragment extends MainFragment  {
    private View curView = null;
    private static Handler uiHandler = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static Handler getHandler() {
        return uiHandler;
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void initHandler() {
        uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HandlerConstant.HANDLER_CHANGE_CONTACT_TAB:
                        if (null != msg.obj) {
//                            curTabIndex = (Integer) msg.obj;
//                            if (0 == curTabIndex) {
//                                allContactListView.setVisibility(View.VISIBLE);
//                                departmentContactListView.setVisibility(View.GONE);
//                            } else {
//                                departmentContactListView.setVisibility(View.VISIBLE);
//                                allContactListView.setVisibility(View.GONE);
//                            }
                        }
                        break;
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_contact, topContentView);
        initRes();
        return curView;
    }

    /**
     * @Description 初始化界面资源
     */
    private void initRes() {
        setTopTitle(getActivity().getString(R.string.main_innernet));
        super.init(curView);

        RelativeLayout friendRelative = (RelativeLayout) curView.findViewById(R.id.re_friends);
        friendRelative.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            CircleActivity.openCircle(getActivity(), false);
            }
        });
    }


}
