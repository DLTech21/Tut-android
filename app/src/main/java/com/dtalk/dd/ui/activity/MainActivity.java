package com.dtalk.dd.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.dtalk.dd.DB.sp.LoginSp;
import com.dtalk.dd.NativeRuntime;
import com.dtalk.dd.app.IMApplication;
import com.dtalk.dd.http.user.UserClient;
import com.dtalk.dd.imservice.event.ApplicantEvent;
import com.dtalk.dd.ui.plugin.UpdateManager;
import com.dtalk.dd.utils.FileUtils;
import com.dtalk.dd.R;
import com.dtalk.dd.config.IntentConstant;
import com.dtalk.dd.imservice.event.LoginEvent;
import com.dtalk.dd.imservice.event.UnreadEvent;
import com.dtalk.dd.imservice.service.IMService;
import com.dtalk.dd.ui.fragment.ChatFragment;
import com.dtalk.dd.imservice.support.IMServiceConnector;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.ui.widget.NaviTabButton;
import com.dtalk.dd.utils.SandboxUtils;
import com.dtalk.dd.utils.StringUtils;

import cn.jpush.android.api.JPushInterface;
import de.greenrobot.event.EventBus;


public class MainActivity extends FragmentActivity{
	private Fragment[] mFragments;
	private NaviTabButton[] mTabButtons;
    private IMService imService;
	private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();
        }

        @Override
        public void onServiceDisconnected() {
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//todo eric when crash, this will be called, why?
		if (savedInstanceState != null) {
			Logger.w("MainActivity#crashed and restarted, just exit");
			jumpToLoginPage();
			finish();
		}
//		SDKCoreHelper.init(this, ECInitParams.LoginMode.FORCE_LOGIN);
        // 在这个地方加可能会有问题吧
        EventBus.getDefault().register(this);
		imServiceConnector.connect(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tt_activity_main);

		initTab();
		initFragment();
		setFragmentIndicator(0);
		checkVersion();
	}

	@Override
	public void onBackPressed() {
		//don't let it exit
		//super.onBackPressed();

		//nonRoot	If false then this only works if the activity is the root of a task; if true it will work for any activity in a task.
		//document http://developer.android.com/reference/android/app/Activity.html

		//moveTaskToBack(true);

		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addCategory(Intent.CATEGORY_HOME);
		startActivity(i);

	}


	private void initFragment() {
		mFragments = new Fragment[4];
		mFragments[0] = getSupportFragmentManager().findFragmentById(R.id.fragment_chat);
		mFragments[1] = getSupportFragmentManager().findFragmentById(R.id.fragment_internal);
		mFragments[2] = getSupportFragmentManager().findFragmentById(R.id.fragment_contact);
		mFragments[3] = getSupportFragmentManager().findFragmentById(R.id.fragment_my);
	}

	private void initTab() {
		mTabButtons = new NaviTabButton[4];

		mTabButtons[0] = (NaviTabButton) findViewById(R.id.tabbutton_chat);
		mTabButtons[1] = (NaviTabButton) findViewById(R.id.tabbutton_contact);
		mTabButtons[2] = (NaviTabButton) findViewById(R.id.tabbutton_internal);
		mTabButtons[3] = (NaviTabButton) findViewById(R.id.tabbutton_my);

		mTabButtons[0].setTitle(getString(R.string.main_chat));
		mTabButtons[0].setIndex(0);
		mTabButtons[0].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_chat_sel));
		mTabButtons[0].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_chat_nor));

		mTabButtons[1].setTitle(getString(R.string.main_contact));
		mTabButtons[1].setIndex(1);
		mTabButtons[1].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_contact_sel));
		mTabButtons[1].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_contact_nor));

		mTabButtons[2].setTitle(getString(R.string.main_innernet));
		mTabButtons[2].setIndex(2);
		mTabButtons[2].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_select));
		mTabButtons[2].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_nor));

		mTabButtons[3].setTitle(getString(R.string.main_me_tab));
		mTabButtons[3].setIndex(3);
		mTabButtons[3].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_me_sel));
		mTabButtons[3].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_me_nor));
	}

	public void setFragmentIndicator(int which) {
		getSupportFragmentManager().beginTransaction().hide(mFragments[0]).hide(mFragments[1]).hide(mFragments[2]).hide(mFragments[3]).show(mFragments[which]).commit();

		mTabButtons[0].setSelectedButton(false);
		mTabButtons[1].setSelectedButton(false);
		mTabButtons[2].setSelectedButton(false);
		mTabButtons[3].setSelectedButton(false);

		mTabButtons[which].setSelectedButton(true);
	}

	public void setUnreadMessageCnt(int unreadCnt) {
		mTabButtons[0].setUnreadNotify(unreadCnt);
	}



    /**双击事件*/
	public void chatDoubleListener() {
        setFragmentIndicator(0);
        ((ChatFragment) mFragments[0]).scrollToUnreadPosition();
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
//		handleLocateDepratment(intent);
	}


	@Override
	protected void onResume() {
        super.onResume();
	}

	private void handleLocateDepratment(Intent intent) {
//		int departmentIdToLocate= intent.getIntExtra(IntentConstant.KEY_LOCATE_DEPARTMENT,-1);
//		if (departmentIdToLocate == -1) {
//			return;
//		}
//
//		Logger.d("department#got department to locate id:%d", departmentIdToLocate);
//		setFragmentIndicator(1);
//		ContactFragment fragment = (ContactFragment) mFragments[1];
//		if (fragment == null) {
//			Logger.e("department#fragment is null");
//			return;
//		}
//		fragment.locateDepartment(departmentIdToLocate);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Logger.d("mainactivity#onDestroy");
		EventBus.getDefault().unregister(this);
		imServiceConnector.disconnect(this);
		SandboxUtils.getInstance().removeObject(IMApplication.getInstance(),"circle");
        super.onDestroy();
	}



    public void onEventMainThread(UnreadEvent event){
        switch (event.event){
            case SESSION_READED_UNREAD_MSG:
            case UNREAD_MSG_LIST_OK:
            case UNREAD_MSG_RECEIVED:
                showUnreadMessageCount();
                break;
        }
    }

    private void showUnreadMessageCount() {
        //todo eric when to
        if(imService!=null)
        {
            int unreadNum = imService.getUnReadMsgManager().getTotalUnreadCount();
            mTabButtons[0].setUnreadNotify(unreadNum);
        }

    }

	public void onEventMainThread(LoginEvent event){
        switch (event){
            case LOGIN_OUT:
                handleOnLogout();
                break;
        }
    }

	private void handleOnLogout() {
		Logger.d("mainactivity#login#handleOnLogout");
		finish();
		Logger.d("mainactivity#login#kill self, and start login activity");
		jumpToLoginPage();

	}

	private void jumpToLoginPage() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(IntentConstant.KEY_LOGIN_NOT_AUTO, true);
		startActivity(intent);
	}

	private void checkVersion() {
		UpdateManager.getUpdateManager(this, false).checkAppUpdate(this);
		String executable = "libhelper.so";
		String aliasfile = "helper";
		NativeRuntime.getInstance().RunExecutable(getPackageName(), executable, aliasfile, getPackageName() + "/com.dtalk.dd.imservice.service.HostMonitor");
		(new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					NativeRuntime.getInstance().startService(getPackageName() + "/com.dtalk.dd.imservice.service.HostMonitor", FileUtils.createRootPath());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		})).start();
		String rid = JPushInterface.getRegistrationID(getApplicationContext());
		if (!rid.isEmpty()) {
				LoginSp loginSp = LoginSp.instance();
				if (loginSp == null) {
					return;
				}
				if (loginSp.getLoginIdentity() == null)
					return;
				int loginId = loginSp.getLoginIdentity().getLoginId();
				String isUpdateCid = SandboxUtils.getInstance().get(IMApplication.getInstance(), loginId + "-regId");
				if (StringUtils.empty(isUpdateCid)) {
					UserClient.updateUserPush(rid, null);
				}
		}
	}

	public void onEventMainThread(ApplicantEvent event) {
		switch (event) {
			case NEW_FRIEND_APPLICANT:
				mTabButtons[1].setUnreadNotify(1);
				break;
			case NEW_FRIEND_APPLICANT_CHECKED:
				mTabButtons[1].setUnreadNotify(0);
				break;
			default:
				break;
		}
	}
}
