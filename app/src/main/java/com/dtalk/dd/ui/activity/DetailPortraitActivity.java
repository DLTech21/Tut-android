package com.dtalk.dd.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.R;
import com.dtalk.dd.config.IntentConstant;
import com.dtalk.dd.utils.IMUIHelper;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.ui.widget.ZoomableImageView;

public class DetailPortraitActivity extends Activity  {

    public static String imageUri = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_detail_portrait);
		
		Intent intent = getIntent();
		if (intent == null) {
			Logger.e("detailPortrait#displayimage#null intent");
			return;
		}

		String resUri = intent.getStringExtra(IntentConstant.KEY_AVATAR_URL);
        imageUri = resUri;

		boolean isContactAvatar = intent.getBooleanExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, false);

		final ZoomableImageView portraitView = (ZoomableImageView) findViewById(R.id.detail_portrait);


		if (portraitView == null) {
			Logger.e("detailPortrait#displayimage#portraitView is null");
			return;
		}

		Logger.d("detailPortrait#displayimage#going to load the detail portrait");


		if (isContactAvatar) {
			IMUIHelper.setEntityImageViewAvatarNoDefaultPortrait(portraitView, resUri, DBConstant.SESSION_TYPE_SINGLE, 0);
		} else {
			IMUIHelper.displayImageNoOptions(portraitView, resUri, -1, 0);
		}

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                portraitView.setFinishActivity(new finishActivity() {
                    @Override
                    public void finish() {
                        if(DetailPortraitActivity.this!=null)
                        {
                            DetailPortraitActivity.this.finish();
                            overridePendingTransition(
                                    R.anim.tt_stay, R.anim.tt_image_exit);
                        }
                    }
                });
            }
        },500);

	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

    public interface finishActivity{
        public void finish();
    }

}
