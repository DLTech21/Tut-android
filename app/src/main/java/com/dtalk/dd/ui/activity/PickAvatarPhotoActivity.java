package com.dtalk.dd.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.dtalk.dd.R;
import com.dtalk.dd.config.IntentConstant;
import com.dtalk.dd.ui.adapter.album.AlbumHelper;
import com.dtalk.dd.ui.adapter.album.ImageBucket;
import com.dtalk.dd.ui.adapter.album.ImageBucketAdapter;
import com.dtalk.dd.utils.Logger;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Donal on 16/7/18.
 */
public class PickAvatarPhotoActivity extends Activity {
    List<ImageBucket> dataList = null;
    ListView listView = null;
    ImageBucketAdapter adapter = null;
    AlbumHelper helper = null;
    TextView cancel = null;
    public static Bitmap bimap = null;
    boolean touchable = true;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==Activity.RESULT_OK)
        {
            this.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d("pic#PickPhotoActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_pick_photo);
        initData();
        initView();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        helper = AlbumHelper.getHelper(getApplicationContext());
        dataList = helper.getImagesBucketList(true);
        bimap = BitmapFactory.decodeResource(getResources(),
                R.drawable.tt_default_album_grid_image);
    }

    /**
     * 初始化view
     */
    private void initView() {
        listView = (ListView) findViewById(R.id.list);
        adapter = new ImageBucketAdapter(this, dataList);

        listView.setAdapter(adapter);
//        listView.setOnTouchListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(PickAvatarPhotoActivity.this,
                        AvatarImageGridActivity.class);
                intent.putExtra(IntentConstant.EXTRA_IMAGE_LIST,
                        (Serializable) dataList.get(position).imageList);
                intent.putExtra(IntentConstant.EXTRA_ALBUM_NAME,
                        dataList.get(position).bucketName);
                startActivityForResult(intent, 1);//requestcode》＝0
//                PickPhotoActivity.this.finish();
            }
        });
        cancel = (TextView) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK, null);
                PickAvatarPhotoActivity.this.finish();
                overridePendingTransition(R.anim.tt_stay, R.anim.tt_album_exit);
            }
        });

    }
}
