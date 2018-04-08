package com.dtalk.dd.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.dtalk.dd.DB.DBInterface;
import com.dtalk.dd.DB.entity.ApplicantEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.http.base.ClientCallback;
import com.dtalk.dd.http.friend.FriendClient;
import com.dtalk.dd.imservice.event.ApplicantEvent;
import com.dtalk.dd.imservice.event.LoginEvent;
import com.dtalk.dd.ui.widget.IMBaseImageView;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.ThemeUtils;
import com.dtalk.dd.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Donal on 16/4/27.
 */
public class ApplicantAdapter extends BaseAdapter implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    public List<ApplicantEntity> userList = new ArrayList<>();

    private Context ctx;

    public ApplicantAdapter(Context context){
        this.ctx = context;
    }

    public void putUserList(List<ApplicantEntity> pUserList){
        this.userList.clear();
        if(pUserList == null || pUserList.size() <=0){
            return;
        }
        this.userList = pUserList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        int userSize = userList==null?0:userList.size();
        return userSize;
    }

    @Override
    public Object getItem(int i) {
        return userList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = renderUser(position,convertView,parent);
        return view;
    }

    public View renderUser(int position, View view, ViewGroup parent){
        ApplicantHolder applicantHolder = null;
        final ApplicantEntity applicantEntity = (ApplicantEntity) getItem(position);
        if(applicantEntity == null){
            return null;
        }
        if (view == null) {
            applicantHolder = new ApplicantHolder();
            view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_applicant, parent,false);
            applicantHolder.nameView = (TextView) view.findViewById(R.id.contact_item_title);
            applicantHolder.avatar = (IMBaseImageView)view.findViewById(R.id.contact_portrait);
            applicantHolder.confirm = (Button)view.findViewById(R.id.confrim_btn);
            applicantHolder.confirmTv = (TextView)view.findViewById(R.id.confrim_tv);
            view.setTag(applicantHolder);
        } else {
            applicantHolder = (ApplicantHolder) view.getTag();
        }
        applicantHolder.avatar.setImageResource(R.drawable.tt_default_user_portrait_corner);
        applicantHolder.nameView.setText(applicantEntity.getNickname());
        applicantHolder.avatar.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        applicantHolder.avatar.setCorner(0);
        applicantHolder.avatar.setImageUrl(applicantEntity.getAvatar());
        applicantHolder.confirm.setVisibility(applicantEntity.getResponse() == 0 ? View.VISIBLE : View.GONE);
        applicantHolder.confirmTv.setVisibility(applicantEntity.getResponse() == 1 ? View.VISIBLE : View.GONE);
        confirmButton(applicantEntity, applicantHolder);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    public static class ApplicantHolder {
        TextView nameView;
        IMBaseImageView avatar;
        Button confirm;
        TextView confirmTv;
    }

    private void confirmButton(final ApplicantEntity applicantEntity, final ApplicantHolder applicantHolder) {
        applicantHolder.confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirFriend(applicantEntity, applicantHolder);
            }
        });
    }

    private void confirFriend(final ApplicantEntity applicantEntity, final ApplicantHolder applicantHolder) {
        FriendClient.confirmFriend(applicantEntity.getUid() + "", new ClientCallback() {
            @Override
            public void onPreConnection() {
                ViewUtils.createProgressDialog((Activity) ctx, "添加好友...", ThemeUtils.getThemeColor()).show();
            }

            @Override
            public void onCloseConnection() {
                ViewUtils.dismissProgressDialog();
            }

            @Override
            public void onSuccess(Object data) {
                BaseResponse res = (BaseResponse) data;
//                if (res.getStatus() == 0) {


//                }
//                else {
//
//                }
                applicantEntity.setResponse(1);
                DBInterface.instance().insertOrUpdateApplicant(applicantEntity);
                applicantHolder.confirm.setVisibility(View.INVISIBLE);
                triggerEvent(ApplicantEvent.CONFIRM_FRIEND_APPLICANT);
                triggerEvent(LoginEvent.FRIEND_RELOAD);
            }

            @Override
            public void onFailure(String message) {

            }

            @Override
            public void onException(Exception e) {

            }
        });
    }

    /**
     * 自身的事件驱动
     * @param event
     */
    public void triggerEvent(Object event) {
        EventBus.getDefault().post(event);
    }
}
