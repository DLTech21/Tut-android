package com.dtalk.dd.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dtalk.dd.ui.widget.SortSideBar;
import com.dtalk.dd.ui.widget.flingswipe.SwipeFlingAdapterView;
import com.dtalk.dd.utils.Logger;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.dtalk.dd.DB.DBInterface;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.imservice.event.ApplicantEvent;
import com.dtalk.dd.imservice.event.FriendInfoEvent;
import com.dtalk.dd.imservice.manager.IMFriendManager;
import com.dtalk.dd.imservice.service.IMService;
import com.dtalk.dd.imservice.support.IMServiceConnector;
import com.dtalk.dd.ui.activity.ApplicantActivity;
import com.dtalk.dd.ui.activity.OwnGroupListActivity;
import com.dtalk.dd.ui.adapter.FriendAdapter;
import com.dtalk.dd.ui.adapter.InternalAdapter;
import com.dtalk.dd.ui.base.TTBaseFragment;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.List;

import de.greenrobot.event.EventBus;
/**
 * 通讯录 （全部、部门）
 */
public class FriendFragment extends MainFragment implements SortSideBar.OnTouchingLetterChangedListener {
    private View curView = null;
    private ListView internalListView;
    private View headerView;
    private SortSideBar sortSideBar;
    @ViewInject(R.id.new_friend_rl)
    RelativeLayout rlNewFriend;
    @ViewInject(R.id.group_rl)
    RelativeLayout rlGroup;
    private TextView dialog;
    @ViewInject(R.id.new_friend_notify)
    TextView newFriendNotifyTV;
    private FriendAdapter friendAdapter;
    private IMService imService;
    private IMFriendManager imFriendManager;


    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            Logger.d("contactUI#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                Logger.e("ContactFragment#onIMServiceConnected# imservice is null!!");
                return;
            }
            imFriendManager = imService.getImFriendManager();

            // 初始化视图
            initAdapter();
            renderEntityList();

        }

        @Override
        public void onServiceDisconnected() {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
        if (!EventBus.getDefault().isRegistered(FriendFragment.this)) {
            EventBus.getDefault().register(FriendFragment.this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(FriendFragment.this)) {
            EventBus.getDefault().unregister(FriendFragment.this);
        }
        imServiceConnector.disconnect(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_internal,
                topContentView);

        initRes();
        return curView;
    }

    private void initRes() {
        // 设置顶部标题栏
        setTopTitle(getActivity().getString(R.string.main_contact));
        internalListView = (ListView)curView.findViewById(R.id.all_contact_list);
        headerView = getActivity().getLayoutInflater().inflate(R.layout.tt_header_contact, null);
        ViewUtils.inject(this, headerView);
        internalListView.addHeaderView(headerView);
        try {
            newFriendNotifyTV.setVisibility(DBInterface.instance().loadAllUnResponseApplicants().size() > 0 ? View.VISIBLE : View.INVISIBLE);
            newFriendNotifyTV.setText(DBInterface.instance().loadAllUnResponseApplicants().size() + "");
        } catch (Exception e) {

        }
        rlNewFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FriendFragment.this.getActivity(), ApplicantActivity.class);
                startActivity(intent);
            }
        });
        rlGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FriendFragment.this.getActivity(), OwnGroupListActivity.class);
                startActivity(intent);
            }
        });
        sortSideBar = (SortSideBar) curView.findViewById(R.id.sidrbar);
        dialog = (TextView) curView.findViewById(R.id.dialog);
        sortSideBar.setTextView(dialog);
        sortSideBar.setOnTouchingLetterChangedListener(this);
        internalListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
    }

    @Override
    protected void initHandler() {
    }

    private void initAdapter(){
        friendAdapter = new FriendAdapter(getActivity(),imService);
        internalListView.setAdapter(friendAdapter);

        // 单击视图事件
        internalListView.setOnItemClickListener(friendAdapter);
        internalListView.setOnItemLongClickListener(friendAdapter);

    }

    private void renderEntityList() {
//        hideProgressBar();
        Logger.d("contact#renderEntityList");

        if (imFriendManager.isUserDataReady() ) {
            renderUserList();
        }
        showSearchFrameLayout();
    }

    private void renderUserList(){
        List<UserEntity> contactList = imFriendManager.getContactSortedList();
        // 没有任何的联系人数据
        Logger.e(contactList.size()+"");
        if (contactList.size() <= 0) {
            return;
        }
        friendAdapter.putUserList(contactList);
    }

    public void onEventMainThread(ApplicantEvent event) {
        switch (event) {
            case NEW_FRIEND_APPLICANT:
                if (newFriendNotifyTV != null) {
                    newFriendNotifyTV.setVisibility(DBInterface.instance().loadAllUnResponseApplicants().size() > 0 ? View.VISIBLE : View.INVISIBLE);
                    newFriendNotifyTV.setText(DBInterface.instance().loadAllUnResponseApplicants().size() + "");
                }
                break;
            case CONFIRM_FRIEND_APPLICANT:
                if (newFriendNotifyTV != null) {
                    newFriendNotifyTV.setVisibility(DBInterface.instance().loadAllUnResponseApplicants().size() > 0 ? View.VISIBLE : View.INVISIBLE);
                    newFriendNotifyTV.setText(DBInterface.instance().loadAllUnResponseApplicants().size() + "");
                }
                imFriendManager.onLocalNetOk();
                break;
            default:
                break;
        }
    }

    public void onEventMainThread(FriendInfoEvent event) {
        switch (event) {
            case FRIEND_INFO_OK:
                renderUserList();
                break;
        }
    }


    @Override
    public void onTouchingLetterChanged(String s) {
        int position = -1;
            position =  friendAdapter.getPositionForSection(s.charAt(0));
        if (position != -1) {
            internalListView.setSelection(position);
        }
    }


}
