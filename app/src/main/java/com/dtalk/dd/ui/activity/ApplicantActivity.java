package com.dtalk.dd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dtalk.dd.DB.DBInterface;
import com.dtalk.dd.DB.entity.ApplicantEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.imservice.event.ApplicantEvent;
import com.dtalk.dd.ui.adapter.ApplicantAdapter;
import com.dtalk.dd.ui.base.TTBaseActivity;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Donal on 16/4/27.
 */
public class ApplicantActivity extends TTBaseActivity implements View.OnClickListener{
    private ListView internalListView;
    private ApplicantAdapter applicantAdapter;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(ApplicantActivity.this)){
            EventBus.getDefault().unregister(ApplicantActivity.this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().registerSticky(ApplicantActivity.this);
        LayoutInflater.from(this).inflate(R.layout.activity_applicant, topContentView);
        initView();
        renderApplicantList();
    }

    private void initView() {
        setLeftButton(R.drawable.tt_top_back);
        setLeftText(getResources().getString(R.string.top_left_back));
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);
        setTitle("新的朋友");
        internalListView = (ListView)findViewById(R.id.internalListView);
        applicantAdapter = new ApplicantAdapter(this);
        internalListView.setAdapter(applicantAdapter);
        internalListView.setOnItemClickListener(applicantAdapter);
        applicantAdapter.triggerEvent(ApplicantEvent.NEW_FRIEND_APPLICANT_CHECKED);
        TextView et_search = (TextView) findViewById(R.id.et_search);
        Button tv_add = (Button) findViewById(R.id.btnMobile);
        et_search.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(ApplicantActivity.this, SearchActivity.class));
            }

        });
        tv_add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(ApplicantActivity.this,
                        MobileFragment.class));
            }

        });
    }

    private void renderApplicantList(){
        List<ApplicantEntity> contactList = DBInterface.instance().loadAllApplicants();
        if (contactList.size() <= 0) {
            return;
        }
        applicantAdapter.putUserList(contactList);
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

    public void onEventMainThread(ApplicantEvent event) {
        switch (event) {
            case NEW_FRIEND_APPLICANT:
            case CONFIRM_FRIEND_APPLICANT:
                renderApplicantList();
                break;
            default:
                break;
        }
    }
}
