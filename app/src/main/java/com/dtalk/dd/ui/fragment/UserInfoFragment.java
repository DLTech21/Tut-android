package com.dtalk.dd.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.Security;
import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.config.IntentConstant;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.http.friend.FriendClient;
import com.dtalk.dd.http.moment.Moment;
import com.dtalk.dd.http.moment.MomentClient;
import com.dtalk.dd.http.moment.MomentList;
import com.dtalk.dd.ui.activity.CircleActivity;
import com.dtalk.dd.ui.widget.MultiImageViewLayout;
import com.dtalk.dd.utils.IMUIHelper;
import com.dtalk.dd.imservice.event.UserInfoEvent;
import com.dtalk.dd.imservice.service.IMService;
import com.dtalk.dd.ui.activity.DetailPortraitActivity;
import com.dtalk.dd.imservice.support.IMServiceConnector;
import com.dtalk.dd.ui.widget.IMBaseImageView;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.MD5Util;
import com.dtalk.dd.utils.SandboxUtils;
import com.dtalk.dd.utils.StringUtils;
import com.dtalk.dd.utils.ThemeUtils;
import com.dtalk.dd.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class UserInfoFragment extends MainFragment {
    private List<String> picUrls;
    private View curView = null;
    MultiImageViewLayout multiImageViewLayout;
    RelativeLayout picLayout;
    private IMService imService;
    private UserEntity currentUser;
    private int currentUserId;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            Logger.d("detail#onIMServiceConnected");
            if (!EventBus.getDefault().isRegistered(UserInfoFragment.this)) {
                EventBus.getDefault().register(UserInfoFragment.this);
            }
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                Logger.e("detail#imService is null");
                return;
            }
            if (currentUserId == 0) {
                Logger.e("detail#intent params error!!");
                return;
            }
            currentUser = imService.getContactManager().findContact(currentUserId);
            if (currentUser != null) {
                hideProgressBar();
                initBaseProfile();
                initDetailProfile();
            }
//            ArrayList<Integer> userIds = new ArrayList<>(1);
//            //just single type
//            userIds.add(currentUserId);
            imService.getContactManager().reqGetDetailUser(currentUserId + "");
            getMoment("0", currentUserId + "");
        }

        @Override
        public void onServiceDisconnected() {
            if (EventBus.getDefault().isRegistered(UserInfoFragment.this)) {
                EventBus.getDefault().unregister(UserInfoFragment.this);
            }
        }

    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(UserInfoFragment.this)) {
            EventBus.getDefault().unregister(UserInfoFragment.this);
        }
        imServiceConnector.disconnect(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        picUrls = new ArrayList<>();
        currentUserId = getActivity().getIntent().getIntExtra(IntentConstant.KEY_PEERID, 0);
        imServiceConnector.connect(getActivity());
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_user_detail, topContentView);
        picLayout = (RelativeLayout) curView.findViewById(R.id.picRL);
        multiImageViewLayout = (MultiImageViewLayout) curView.findViewById(R.id.multiimage);
        multiImageViewLayout.setOnItemClickListener(new MultiImageViewLayout.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int PressImagePosition, float PressX, float PressY) {
                CircleActivity.openCircle(getActivity(), true, currentUserId + "", currentUser.getAvatar(), currentUser.getMomentcover());
            }

            @Override
            public void onItemLongClick(View view, int PressImagePosition, float PressX, float PressY) {

            }
        });
        super.init(curView);
        showProgressBar();
        initRes();
        return curView;
    }

    @Override
    public void onResume() {
        Intent intent = getActivity().getIntent();
        if (null != intent) {
            String fromPage = intent.getStringExtra(IntentConstant.USER_DETAIL_PARAM);
            setTopLeftText(fromPage);
        }
        super.onResume();
    }

    /**
     * @Description 初始化资源
     */
    private void initRes() {
        // 设置标题栏
        setTopTitle(getActivity().getString(R.string.page_user_detail));
        setTopLeftButton(R.drawable.tt_top_back);
        topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                getActivity().finish();
            }
        });
        setTopLeftText(getResources().getString(R.string.top_left_back));
    }

    @Override
    protected void initHandler() {
    }

    public void onEventMainThread(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_UPDATE:
                currentUser = imService.getContactManager().findContact(currentUserId);
//                if(entity !=null && currentUser.equals(entity)){
                initBaseProfile();
                initDetailProfile();
//                }
                break;
        }
    }

    private void initBaseProfile() {
        Logger.d("detail#initBaseProfile");
        IMBaseImageView portraitImageView = (IMBaseImageView) curView.findViewById(R.id.user_portrait);

        setTextViewContent(R.id.nickName, currentUser.getMainName());
        setTextViewContent(R.id.userName, "Tut号: tut_"+ encrypt(currentUser.getPhone()));
        //头像设置
        portraitImageView.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setCorner(8);
        portraitImageView.setImageResource(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setImageUrl(currentUser.getAvatar());

        TextView tvRegion = (TextView) curView.findViewById(R.id.tv_region);
        if (StringUtils.notEmpty(currentUser.getArea())) {
            tvRegion.setText(currentUser.getArea());
        } else {
            tvRegion.setText("未知城市");
        }

        // 设置界面信息
        Button chatBtn = (Button) curView.findViewById(R.id.chat_btn);
        if (currentUserId == imService.getLoginManager().getLoginId()) {
            chatBtn.setVisibility(View.GONE);
        } else {
            if (currentUser.getIsFriend() == 0) {
                chatBtn.setText("添加好友");
            } else {
                chatBtn.setText(R.string.chat);
            }
            chatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (currentUser.getIsFriend() == 0) {
                        applyFriend();
                    } else {
                        IMUIHelper.openChatActivity(getActivity(), currentUser.getSessionKey());
                        getActivity().finish();
                    }
                }
            });
        }


    }

    private void initDetailProfile() {
        Logger.d("detail#initDetailProfile");
        hideProgressBar();
        setSex(currentUser.getGender());
    }

    private void setTextViewContent(int id, String content) {
        TextView textView = (TextView) curView.findViewById(id);
        if (textView == null) {
            return;
        }

        textView.setText(content);
    }

    private void setSex(int sex) {
        if (curView == null) {
            return;
        }

        TextView sexTextView = (TextView) curView.findViewById(R.id.sex);
        if (sexTextView == null) {
            return;
        }

        int textColor = Color.rgb(255, 138, 168); //xiaoxian
        String text = getString(R.string.sex_female_name);

        if (sex == DBConstant.SEX_MAILE) {
            textColor = Color.rgb(144, 203, 1);
            text = getString(R.string.sex_male_name);
        }

        sexTextView.setVisibility(View.VISIBLE);
        sexTextView.setText(text);
        sexTextView.setTextColor(textColor);
    }

    private void applyFriend() {
        FriendClient.applyFriend(currentUserId + "", "", new BaseClient.ClientCallback() {
            @Override
            public void onPreConnection() {
                ViewUtils.createProgressDialog(getActivity(), "", ThemeUtils.getThemeColor()).show();
            }

            @Override
            public void onCloseConnection() {
                ViewUtils.dismissProgressDialog();
            }

            @Override
            public void onSuccess(Object data) {
                BaseResponse response = (BaseResponse) data;
                Toast.makeText(getActivity(), response.getMsg(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMomentsByCache(String fxid) {
        MomentList list = (MomentList) SandboxUtils.getInstance().readObject(getActivity(), "momonets-" + fxid);
        if (list != null) {
//            for (Moment m : list.list) {
//                picUrls.addAll(m.image);
//            }
//            if (!picUrls.isEmpty()) {
//                picRL.setVisibility(View.VISIBLE);
//                buildMultiPic(picUrls, content_pic_multi);
//            }
        }
        getMoment("0", fxid);
    }

    private void getMoment(final String last, String fid) {
        MomentClient.fetchOnesMoment(fid, last, "10", new BaseClient.ClientCallback() {

            @Override
            public void onSuccess(Object data) {
                MomentList list = (MomentList) data;
                for (Moment m : list.list) {
                    picUrls.addAll(m.image);
                }
                Logger.e(picUrls.size() + "");
                if (picUrls.size() > 3) {
                    picLayout.setVisibility(View.VISIBLE);
                    List mStrings = new ArrayList();
                    for (int i = 0; i < 3; i++) {
                        mStrings.add(picUrls.get(i));
                    }
                    multiImageViewLayout.setList(mStrings);
                } else {
                    multiImageViewLayout.setList(picUrls);
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
            }
        });
    }

    private String encrypt(String content) {
        String en_content;
        en_content = content.replaceAll("1", "t");
        en_content = en_content.replaceAll("2", "a");
        en_content = en_content.replaceAll("3", "b");
        en_content = en_content.replaceAll("4", "h");
        en_content = en_content.replaceAll("5", "z");
        en_content = en_content.replaceAll("6", "g");
        en_content = en_content.replaceAll("7", "j");
        en_content = en_content.replaceAll("8", "w");
        en_content = en_content.replaceAll("9", "e");
        return en_content;
    }

    public static String decrypt(String content) {
        String en_content;
        en_content = content.replaceAll("a", "2");
        en_content = en_content.replaceAll("b", "3");
        en_content = en_content.replaceAll("h", "4");
        en_content = en_content.replaceAll("z", "5");
        en_content = en_content.replaceAll("g", "6");
        en_content = en_content.replaceAll("j", "7");
        en_content = en_content.replaceAll("w", "8");
        en_content = en_content.replaceAll("e", "9");
        en_content = en_content.replaceAll("t", "1");
        return en_content;
    }
}
