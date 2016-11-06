package com.dtalk.dd.ui.adapter;

import java.util.ArrayList;

import com.baidu.mapapi.search.core.PoiInfo;
import com.dtalk.dd.R;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LocationPoiAdapter extends BaseAdapter {
	
	Context context;
	ArrayList<PoiInfo> datas;
	
	public LocationPoiAdapter(Context context, ArrayList<PoiInfo> datas) {
		this.context = context;
		this.datas = datas;
	}
	
	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_poi, null);
			viewHolder = new ViewHolder();
			viewHolder.nameTV = (TextView) convertView.findViewById(R.id.nameTV);
			viewHolder.addressTV = (TextView) convertView.findViewById(R.id.addressTV);
			convertView.setTag(viewHolder);
		}	
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		PoiInfo p = datas.get(position);
		if (!TextUtils.isEmpty(p.name)) {
		    viewHolder.nameTV.setText(p.name);
		    viewHolder.addressTV.setText(p.address);
        }
		else {
		    viewHolder.nameTV.setText(p.address);
            viewHolder.addressTV.setText("");
		}
		return convertView;
	}

	class ViewHolder
	{
	    TextView nameTV;
		TextView addressTV;
	}
}
