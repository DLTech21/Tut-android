package com.dtalk.dd.ui.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.dtalk.dd.R;
import com.dtalk.dd.imservice.event.PoiSearchEvent;
import com.dtalk.dd.ui.adapter.LocationPoiAdapter;
import com.dtalk.dd.ui.base.TTBaseActivity;
import com.dtalk.dd.ui.widget.SearchEditText;
import com.dtalk.dd.utils.StringUtils;

import de.greenrobot.event.EventBus;

public class BaiduMapSearchActivity extends TTBaseActivity implements OnClickListener, OnItemClickListener, OnScrollListener, OnGetSuggestionResultListener{
    
    @ViewInject(R.id.chat_title_search)
    SearchEditText topSearchEdt;

    @ViewInject(R.id.listview)
    ListView listView;
    ArrayList<PoiInfo> datas;
    LocationPoiAdapter adapter;
    SuggestionSearch mSuggestionSearch;
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSuggestionSearch.destroy();
    }
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_baidumap_search);
        ViewUtils.inject(this);
        initView();
        datas = new ArrayList<PoiInfo>();
        adapter = new LocationPoiAdapter(this, datas);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
        

    }

    private void initView() {
        ImageView topLeftBtn = (ImageView) findViewById(R.id.left_btn);
        topLeftBtn.setImageResource(R.drawable.tt_top_back);
        topLeftBtn.setOnClickListener(this);

        topSearchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                String key = s.toString();
                if (!key.isEmpty()) {
                    mSuggestionSearch.requestSuggestion((new SuggestionSearchOption()
                            .keyword(key)
                            .city("")
                    ));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.left_btn:
            finish();
            break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        PoiInfo p = datas.get(position);
        EventBus.getDefault().post(new PoiSearchEvent(p));
        finish();
    }


    @Override
    public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {

    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {
    }

    @Override
    public void onGetSuggestionResult(SuggestionResult suggestionResult) {
        if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
            return;
        }
        datas.clear();
        adapter.notifyDataSetChanged();
        for (SuggestionResult.SuggestionInfo i : suggestionResult.getAllSuggestions()) {
            if (StringUtils.notEmpty(i.pt)) {
                PoiInfo p = new PoiInfo();
                p.name = i.key;
                p.location = i.pt;
                if (StringUtils.notEmpty(i.city) && StringUtils.notEmpty(i.district)) {
                    p.address = i.city+""+i.district;
                }
                datas.add(p);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
