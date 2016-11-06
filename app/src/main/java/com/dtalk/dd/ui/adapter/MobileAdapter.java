package com.dtalk.dd.ui.adapter;


import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.dtalk.dd.DB.DBInterface;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.imservice.manager.IMFriendManager;
import com.dtalk.dd.imservice.service.IMService;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 简单的好友Adapter实现
 * 
 */
public class MobileAdapter extends ArrayAdapter<UserEntity> implements
        SectionIndexer {

    List<String> list;
    List<UserEntity> userList;
    List<UserEntity> copyUserList;
    List<String> selectUser;
    private LayoutInflater layoutInflater;
    private SparseIntArray positionOfSection;
    private SparseIntArray sectionOfPosition;
    private int res;
//    public MyFilter myFilter;

    private DisplayImageOptions avatarOp = new DisplayImageOptions.Builder()
    .cacheInMemory(true)
    .cacheOnDisk(true)
    .showImageOnLoading(R.drawable.tt_default_user_portrait_corner)
    .showImageForEmptyUri(R.drawable.tt_default_user_portrait_corner)
    .showImageOnFail(R.drawable.tt_default_user_portrait_corner)
    .bitmapConfig(Bitmap.Config.RGB_565)
    .build();
    private Context context;
    @SuppressLint("SdCardPath")
    public MobileAdapter(Context context, int resource, List<UserEntity> objects) {
        super(context, resource, objects);
        this.context = context;
        this.res = resource;
        this.userList = objects;
        copyUserList = new ArrayList<UserEntity>();
        copyUserList.addAll(objects);
        layoutInflater = LayoutInflater.from(context);
        selectUser = new ArrayList<String>();
    }
    
    class ViewHolder {
        @ViewInject(R.id.iv_avatar)
        ImageView iv_avatar;
        
        @ViewInject(R.id.tv_name)
        TextView tv_name;
        
        @ViewInject(R.id.tv_id)
        TextView tv_id;
        
        @ViewInject(R.id.header)
        TextView tv_header;
        
        @ViewInject(R.id.view_temp)
        View view_temp;
        
        @ViewInject(R.id.btn_invite)
        Button btnInvite;
        
        @ViewInject(R.id.btn_add)
        Button btnAdd;
        
        @ViewInject(R.id.tv_add)
        TextView tvAdd;
        
        @ViewInject(R.id.btnSelect)
        CheckBox btnSelect;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(res, null);
            viewHolder = new ViewHolder();
            ViewUtils.inject(viewHolder, convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final UserEntity user = getItem(position);
        if (user == null)
            Log.d("ContactAdapter", position + "");

        String header = user.getPinyinName().toUpperCase().substring(0,1);
        final String usernick = user.getRealName();
        String useravatar = user.getAvatar();
        if (position == 0 || header != null
                && !header.equals(getItem(position - 1).getPinyinName().toUpperCase().substring(0,1))) {
            if ("".equals(header)) {
                viewHolder.tv_header.setVisibility(View.GONE);
                viewHolder.view_temp.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tv_header.setVisibility(View.VISIBLE);
                viewHolder.tv_header.setText(header);
                viewHolder.view_temp.setVisibility(View.GONE);
            }
        } else {
            viewHolder.tv_header.setVisibility(View.GONE);
            viewHolder.view_temp.setVisibility(View.VISIBLE);
        }
        // 显示申请与通知item

        viewHolder.tv_name.setText(usernick);
        viewHolder.tv_id.setText(user.getPhone());
        showUserAvatar(viewHolder.iv_avatar, useravatar);
        if (user.getIsFriend() == 1) {
            viewHolder.btnInvite.setVisibility(View.INVISIBLE);
            viewHolder.btnSelect.setVisibility(View.GONE);
            if (DBInterface.instance().getByFriendUserName(user.getPhone()) != null) {
                viewHolder.tvAdd.setVisibility(View.VISIBLE);
                viewHolder.btnAdd.setVisibility(View.INVISIBLE);
            }
            else {
                viewHolder.tvAdd.setVisibility(View.INVISIBLE);
                viewHolder.btnAdd.setVisibility(View.VISIBLE);
            }
        }
        else {
            viewHolder.btnSelect.setVisibility(View.VISIBLE);
            viewHolder.btnInvite.setVisibility(View.VISIBLE);
            viewHolder.tvAdd.setVisibility(View.INVISIBLE);
            viewHolder.btnAdd.setVisibility(View.INVISIBLE);
        }
        viewHolder.btnSelect.setButtonDrawable(R.drawable.check_blue);
        viewHolder.btnInvite.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                Uri smsToUri = Uri.parse("smsto:"+user.getPhone());
                Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
//                int resId = getStringRes(context, "smssdk_invite_content");
//                if (resId > 0) {
//                    intent.putExtra("sms_body", context.getString(resId));
//                }
                context.startActivity(intent);
            }
        });
        viewHolder.btnAdd.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
//                Intent intent = new Intent();
//                intent.putExtra("hxid", user.getPe);
//                intent.putExtra("avatar", user.getAvatar());
//                intent.putExtra("nick", usernick);
//                intent.putExtra("sex", "0");
//                intent.setClass(context,
//                        UserInfoActivity.class);
//                context.startActivity(intent);
            }
        });
        
        viewHolder.btnSelect.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {

//                if (isChecked) {
//                    selectUser.add(user.getFxid());
//                } else {
//                    if (selectUser.contains(user.getFxid())) {
//                        selectUser.remove(user.getFxid());
//                    }
//                }

            }
        });
        return convertView;
    }

    @Override
    public UserEntity getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    public int getPositionForSection(int section) {
        return positionOfSection.get(section);
    }

    public int getSectionForPosition(int position) {
        return sectionOfPosition.get(position);
    }
    
    

    public List<String> getSelectUser() {
        return selectUser;
    }

    @Override
    public Object[] getSections() {
        positionOfSection = new SparseIntArray();
        sectionOfPosition = new SparseIntArray();
        int count = getCount();
        list = new ArrayList<String>();
        list.add(getContext().getString(R.string.search_header));
        positionOfSection.put(0, 0);
        sectionOfPosition.put(0, 0);
        for (int i = 1; i < count; i++) {

            String letter = getItem(i).getPinyinName();
            int section = list.size() - 1;
            if (list.get(section) != null && !list.get(section).equals(letter)) {
                list.add(letter);
                section++;
                positionOfSection.put(section, i);
            }
            sectionOfPosition.put(i, section);
        }
        return list.toArray(new String[list.size()]);
    }

    private void showUserAvatar(ImageView iamgeView, String avatar) {
        ImageLoader.getInstance().displayImage(avatar, iamgeView, avatarOp);
    }

}
