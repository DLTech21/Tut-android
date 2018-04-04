package com.dtalk.dd.ui.activity;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts.Data;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.ui.adapter.MobileAdapter;
import com.dtalk.dd.ui.base.TTBaseActivity;
import com.dtalk.dd.ui.widget.MobileSideBar;
import com.dtalk.dd.utils.SandboxUtils;
import com.dtalk.dd.utils.pinyin.PinYin;
import cn.smssdk.SMSSDK;
import cn.smssdk.EventHandler;

public class MobileFragment extends TTBaseActivity implements View.OnClickListener {
    private MobileAdapter adapter;
    private List<UserEntity> contactList;
    private List<UserEntity> allContactList;
    private ListView listView;
    private MobileSideBar sidebar;
    private MyAsyncQueryHandler asyncQuery;
    private Uri uri;
    private ArrayList<HashMap<String, Object>> friendsInApp = new ArrayList<HashMap<String, Object>>();
    private int mode;
    private EventHandler handler = new EventHandler() {
        @SuppressWarnings("unchecked")
        public void afterEvent(final int event, final int result, final Object data) {
            if (result == SMSSDK.RESULT_COMPLETE) {
                if (event == SMSSDK.EVENT_GET_CONTACTS) {
                } else if (event == SMSSDK.EVENT_GET_FRIENDS_IN_APP) {
                    // 请求获取服务器上，应用内的朋友
                    friendsInApp = (ArrayList<HashMap<String, Object>>) data;
                    SandboxUtils.getInstance().saveObject(IMApplication.getInstance(), friendsInApp, "friendsInApp");
                    handlerFriend();
                }
            } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        friendsInApp = (ArrayList<HashMap<String, Object>>) SandboxUtils.getInstance().readObject(IMApplication.getInstance(), "friendsInApp");
                        handlerFriend();
                    }
                });
            }
        }
    };

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

    private void handlerFriend() {
        for (HashMap<String, Object> friend : friendsInApp) {
            String phone = String.valueOf(friend.get("phone"));
            if (phone != null) {
                for (int i = 0; i < contactList.size(); i++) {
                    UserEntity ent = contactList.get(i);
                    String cp = ent.getPhone();
                    if (phone.equals(cp)) {
//                        friend.put("contact", ent.getValue());
//                        friend.put("fia", true);
//                        tmpFia.add((HashMap<String, Object>) friend.clone());
                        ent.setAvatar(friend.get("avatar").toString());
                        ent.setMainName(friend.get("nickname").toString());
                        ent.setIsFriend(1);
                    }
                }
            }
        }
        runOnUiThread(new Runnable() {
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        LayoutInflater.from(this).inflate(R.layout.activity_mobile, topContentView);
        setLeftButton(R.drawable.tt_top_back);
        setLeftText(getResources().getString(R.string.top_left_back));
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);
        setTitle("手机通讯录");
        initsms();
        listView = (ListView) findViewById(R.id.list);
        allContactList = new ArrayList<UserEntity>();
        contactList = new ArrayList<UserEntity>();
        LayoutInflater infalter = LayoutInflater.from(this);
        View headView = infalter.inflate(R.layout.item_mobile_header,
                null);
        listView.addHeaderView(headView);
        getContactList();
        sidebar = (MobileSideBar) findViewById(R.id.sidebar);
        sidebar.setListView(listView);

        adapter = new MobileAdapter(this, R.layout.item_moblile_list,
                contactList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position != 0 && position != contactList.size() + 1) {
                    final UserEntity user = contactList.get(position - 1);
                    if (user.getIsFriend() == 1) {
//                        Intent intent = new Intent();
//                        intent.putExtra("hxid", user.getUsername());
//                        intent.putExtra("avatar", user.getAvatar());
//                        intent.putExtra("nick", user.getNick());
//                        intent.putExtra("sex", "0");
//                        intent.setClass(MobileFragment.this,
//                                UserInfoActivity.class);
//                        startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 获取联系人列表，并过滤掉黑名单和排序
     */
    private void getContactList() {
        mode = 0;
        asyncQuery = new MyAsyncQueryHandler(getContentResolver());
        uri = Phone.CONTENT_URI;
        asyncQuery.startQuery(0, null, uri, projection, null, null, "sort_key COLLATE LOCALIZED asc");

    }

    @SuppressLint("DefaultLocale")
    public class PinyinComparator implements Comparator<UserEntity> {

        @SuppressLint("DefaultLocale")
        @Override
        public int compare(UserEntity o1, UserEntity o2) {
            // TODO Auto-generated method stub
            String py1 = o1.getPinyinName();
            String py2 = o2.getPinyinName();
            // 判断是否为空""
            if (isEmpty(py1) && isEmpty(py2))
                return 0;
            if (isEmpty(py1))
                return -1;
            if (isEmpty(py2))
                return 1;
            String str1 = "";
            String str2 = "";
            try {
                str1 = ((o1.getPinyinName()).toUpperCase()).substring(0, 1);
                str2 = ((o2.getPinyinName()).toUpperCase()).substring(0, 1);
            } catch (Exception e) {
                System.out.println("某个str为\" \" 空");
            }
            return str1.compareTo(str2);
        }

        private boolean isEmpty(String str) {
            return "".equals(str.trim());
        }
    }

    Map<String, UserEntity> users = new HashMap<String, UserEntity>();
    private String[] projection = {Data.MIMETYPE, Phone.NUMBER, "display_name", "contact_id", "sort_key", "photo_thumb_uri"};
    private final static int MIMETYPE_INDEX = 0;
    private final static int NUMBER_INDEX = 1;
    private final static int NAME_INDEX = 2;
    private final static int ID_INDEX = 3;
    private final static int SORT_INDEX = 4;
    private final static int PHOTO_INDEX = 5;

    private class MyAsyncQueryHandler extends AsyncQueryHandler {
        public MyAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            handleCursor(cursor);
        }
    }

    private void handleCursor(final Cursor cursor) {
        final Handler handler1 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    contactList.clear();
                    // 获取本地好友列表
                    Iterator<Entry<String, UserEntity>> iterator = users.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Entry<String, UserEntity> entry = iterator.next();
//                      if (!entry.getKey().equals(Constant.NEW_FRIENDS_USERNAME)
//                              && !entry.getKey().equals(Constant.GROUP_USERNAME)
//                              )
                        contactList.add(entry.getValue());
                    }

                    if (mode == 0) {
                        allContactList.addAll(contactList);
                    }
                    // 对list进行排序
                    Collections.sort(contactList, new PinyinComparator() {
                    });
                    adapter.notifyDataSetChanged();
                    SMSSDK.getFriendsInApp();
                } else if (msg.what == -1) {
                    WarningDialog();
                } else {
                    WarningDialog("手机通讯录还没有联系人哦");
                }
            }
        };
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (cursor == null) {
                    handler1.sendEmptyMessage(-1);
                    return;
                }
                if (cursor.getColumnCount() == 1) {
                    handler1.sendEmptyMessage(-1);
                    return;
                }
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    users.clear();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        String mimetype = cursor.getString(MIMETYPE_INDEX);
                        if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
                            String phone = cursor.getString(NUMBER_INDEX);
                            phone = phone.replace(" ", "");
                            phone = phone.replace("+86", "");
                            phone = phone.replace("-", "");
                            String hxid = phone;
                            String fxid = phone;
                            String nick = cursor.getString(NAME_INDEX);
                            String avatar = cursor.getString(PHOTO_INDEX);
                            String tel = phone;

                            UserEntity user = new UserEntity();
                            user.setPhone(fxid);
                            user.setMainName(hxid);
                            user.setIsFriend(0);
                            user.setRealName(nick);
                            user.setAvatar(avatar);
                            setUserHearder(hxid, user);
                            users.put(hxid, user);
                        }
                    }
                    Log.i("ginye", users.size() + "");
                    handler1.sendEmptyMessage(1);
                } else {
                    handler1.sendEmptyMessage(2);
                }
            }
        });
    }

    @SuppressLint("DefaultLocale")
    protected void setUserHearder(String username, UserEntity user) {
        String headerName = null;
        if (!TextUtils.isEmpty(user.getRealName())) {
            headerName = user.getRealName();
        } else {
            headerName = user.getMainName();
        }
        headerName = headerName.trim();
        if (Character.isDigit(headerName.charAt(0))) {
            user.setPinyinName("#");
        } else {
            PinYin.getPinYin(user.getRealName(), user.getPinyinElement());
            user.setPinyinName(user.getPinyinElement().pinyin);
            char header = user.getPinyinName().toLowerCase().charAt(0);
            if (header < 'a' || header > 'z') {
                user.setPinyinName("#");
            }
        }
    }

    protected void WarningDialog() {
        String message = "请在手机的【设置】->【应用】->【Tut】->底部的【权限管理】->【信任该程序】即可";
        Builder builder = new Builder(this);
        builder.setMessage(message);
        builder.setTitle("温馨提示：");
        builder.setPositiveButton("确定", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void initsms() {
        // 初始化短信认证
        SMSSDK.initSDK(this, "272bc3eaa8dc", "a10211aac04334b49d39b779a350621e");
        SMSSDK.registerEventHandler(handler);
    }

}
