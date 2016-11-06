package com.dtalk.dd.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.dtalk.dd.DB.entity.GroupEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.imservice.manager.IMGroupManager;
import com.dtalk.dd.imservice.service.IMService;
import com.dtalk.dd.imservice.support.IMServiceConnector;
import com.dtalk.dd.ui.adapter.ContactAdapter;
import com.dtalk.dd.ui.base.TTBaseActivity;
import com.dtalk.dd.utils.Logger;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Donal on 16/4/27.
 */
public class OwnGroupListActivity extends TTBaseActivity implements View.OnClickListener{
    private ListView internalListView;
    private ContactAdapter contactAdapter;

    private IMService imService;
    private IMGroupManager imGroupManager;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                Logger.e("ContactFragment#onIMServiceConnected# imservice is null!!");
                return;
            }
            imGroupManager = imService.getGroupManager();

            renderEntityList();
        }

        @Override
        public void onServiceDisconnected() {
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(OwnGroupListActivity.this)){
            EventBus.getDefault().unregister(OwnGroupListActivity.this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(this);
        LayoutInflater.from(this).inflate(R.layout.tt_activity_group, topContentView);
        initView();
    }

    private void initView() {
        setLeftButton(R.drawable.tt_top_back);
        setLeftText(getResources().getString(R.string.top_left_back));
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);
        setTitle("群聊");
        internalListView = (ListView)findViewById(R.id.all_contact_list);

    }

    private void renderEntityList() {
        contactAdapter = new ContactAdapter(this,imService);
        internalListView.setAdapter(contactAdapter);
        internalListView.setOnItemClickListener(contactAdapter);
        if (imGroupManager.isGroupReady()) {
            renderGroupList();
        }
    }

    private void renderGroupList() {
        List<GroupEntity> originList = imGroupManager.getNormalGroupSortedList();
        if(originList.size() <= 0){
            return;
        }
        contactAdapter.putGroupList(originList);
    }


    @Override
    public void onClick(View view) {
        final int id = view.getId();
        switch (id) {
            case R.id.left_btn:
            case R.id.left_txt:
                this.finish();
                break;
            default:
                break;
        }
    }
}
