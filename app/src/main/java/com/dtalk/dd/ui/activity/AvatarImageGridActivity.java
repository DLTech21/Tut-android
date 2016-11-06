package com.dtalk.dd.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dtalk.dd.R;
import com.dtalk.dd.config.IntentConstant;
import com.dtalk.dd.config.SysConstant;
import com.dtalk.dd.imservice.event.SelectEvent;
import com.dtalk.dd.imservice.service.IMService;
import com.dtalk.dd.imservice.support.IMServiceConnector;
import com.dtalk.dd.ui.adapter.album.AvatarImageGridAdapter;
import com.dtalk.dd.ui.adapter.album.ImageItem;
import com.dtalk.dd.utils.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by Donal on 16/7/18.
 */
public class AvatarImageGridActivity extends Activity implements View.OnTouchListener {
    private List<ImageItem> dataList = null;
    private GridView gridView = null;
    private TextView title = null;
    private TextView cancel = null;
    private static TextView finish = null;
    private TextView preview = null;
    private String name = null;
    private ImageView leftBtn = null;
    private static Context context = null;
    private static AvatarImageGridAdapter adapter = null;

    private IMService imService;

    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();
            if(imService == null){
                throw new RuntimeException("#connect imservice success,but is null");
            }
        }

        @Override
        public void onServiceDisconnected() {

        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(AvatarImageGridActivity.this,
                            "最多选择" + SysConstant.MAX_SELECT_IMAGE_COUNT + "张图片",
                            Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    adapter.lock();
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    adapter.unlock();
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    adapter.lock();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(this);
        setContentView(R.layout.tt_activity_image_grid);
        context = this;
        name = (String) getIntent().getSerializableExtra(
                IntentConstant.EXTRA_ALBUM_NAME);
        dataList = (List<ImageItem>) getIntent().getSerializableExtra(
                IntentConstant.EXTRA_IMAGE_LIST);
        initView();
        initAdapter();
    }

    private void initAdapter() {
        adapter = new AvatarImageGridAdapter(AvatarImageGridActivity.this, dataList,
                mHandler);
        adapter.setTextCallback(new AvatarImageGridAdapter.TextCallback() {
            public void onListen(int count) {
                setSendText(count);
            }
        });
        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(onScrollListener);
    }

    private void initView() {
        findViewById(R.id.rl_bottom_preview).setVisibility(View.GONE);
        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
//                adapter.notifyDataSetChanged();
                ImageItem imgItem = dataList.get(position);
                List<ImageItem> itemList = new ArrayList<>();
                itemList.add(imgItem);
                EventBus.getDefault().post(new SelectEvent(itemList));
                AvatarImageGridActivity.this.setResult(RESULT_OK, null);
                AvatarImageGridActivity.this.finish();
            }
        });

        title = (TextView) findViewById(R.id.base_fragment_title);
        if (name.length() > 12) {
            name = name.substring(0, 11) + "...";
        }
        title.setText(name);
        leftBtn = (ImageView) findViewById(R.id.back_btn);
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AvatarImageGridActivity.this.finish();
            }
        });
        cancel = (TextView) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.setSelectMap(null);
                AvatarImageGridActivity.this.finish();
            }
        });
        finish = (TextView) findViewById(R.id.finish);
        finish.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Logger.d("pic#click send image btn");
                if (adapter.getSelectMap().size() > 0) {
                    List<ImageItem> itemList = new ArrayList<>();
                    Iterator<Integer> iter = adapter.getSelectMap().keySet()
                            .iterator();

                    for(Map.Entry<Integer,ImageItem> entity :adapter.getSelectMap().entrySet()){
                        int position = entity.getKey();
                        ImageItem imageItem = entity.getValue();

                    }
                    while (iter.hasNext()) {
                        int position = iter.next();
                        ImageItem imgItem = adapter.getSelectMap()
                                .get(position);
                        itemList.add(imgItem);
                    }

                    setSendText(0);
                    EventBus.getDefault().post(new SelectEvent(itemList));
                    AvatarImageGridActivity.this.setResult(RESULT_OK, null);
                    AvatarImageGridActivity.this.finish();
                } else {
                    Toast.makeText(AvatarImageGridActivity.this,
                            R.string.need_choose_images, Toast.LENGTH_SHORT)
                            .show();
                }
            }

        });
        preview = (TextView) findViewById(R.id.preview);
        preview.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (adapter.getSelectMap().size() > 0) {
                    Intent intent = new Intent(AvatarImageGridActivity.this,
                            PreviewActivity.class);
                    startActivityForResult(intent,
                            SysConstant.ALBUM_PREVIEW_BACK);
                } else {
                    Toast.makeText(AvatarImageGridActivity.this,
                            R.string.need_choose_images, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        setAdapterSelectedMap(null);
        imServiceConnector.disconnect(this);
        super.onStop();
    }

    public static void setSendText(int selNum) {
        if (selNum == 0) {
            finish.setText(context.getResources().getString(R.string.send));
        } else {
            finish.setText(context.getResources().getString(R.string.send)
                    + "(" + selNum + ")");
        }
    }

    public static void setAdapterSelectedMap(Map<Integer, ImageItem> map) {
        Iterator<Integer> it = adapter.getSelectMap().keySet().iterator();
        if (map != null) {
            while (it.hasNext()) {
                int key = (Integer) it.next();
                if (map.containsKey(key)) {
                    adapter.updateSelectedStatus(key, true);
                } else {
                    adapter.updateSelectedStatus(key, false);
                }
            }
            adapter.setSelectMap(map);
            adapter.setSelectTotalNum(map.size());
        } else {
            while (it.hasNext()) {
                int key = (Integer) it.next();
                adapter.updateSelectedStatus(key, false);
            }
            adapter.setSelectMap(null);
            adapter.setSelectTotalNum(0);
        }
        adapter.notifyDataSetChanged();
    }

    public static AvatarImageGridAdapter getAdapter() {
        return adapter;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                adapter.unlock();
                break;
        }
        return false;
    }
}
