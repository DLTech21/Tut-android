package com.dtalk.dd.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

import com.dtalk.dd.R;
import com.dtalk.dd.model.Photo4Gallery;
import com.dtalk.dd.ui.activity.CircleImagePubActivity;
import com.dtalk.dd.ui.plugin.ImageLoadManager;
import com.dtalk.dd.utils.Logger;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.List;

public class ShareListPicsAdapter extends BaseAdapter{
	private Context context;
	private List<Photo4Gallery> datas;
	private int columnWidth;
	
	static class ViewHolder {
		@ViewInject(R.id.imgPic)
		public ImageView imgPic;
	}
	
	public ShareListPicsAdapter(Context context, List<Photo4Gallery> datas, int columnWidth) {
		this.context = context;
		this.datas = datas;
		this.columnWidth = columnWidth;
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
			convertView = LayoutInflater.from(context).inflate(R.layout.item_share_list_pic, null);
			viewHolder = new ViewHolder();
			ViewUtils.inject(viewHolder, convertView);
			LayoutParams p = (LayoutParams) viewHolder.imgPic.getLayoutParams();
			p.width = columnWidth;
			p.height = columnWidth;
//			viewHolder.imgPic.setLayoutParams(p);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Photo4Gallery mj = datas.get(position);
		if (mj.type == Photo4Gallery.FUNCTION_TYPE) {
			ImageLoadManager.getInstance(context).setDrawableGlide( R.drawable.tt_group_manager_add_user, viewHolder.imgPic);
        }
		else {
			Logger.i("file://"+mj.path);
			ImageLoadManager.getInstance(context).setCirclePubGlide("file://"+mj.path, viewHolder.imgPic);
		}
		clickImg(viewHolder, mj, position);
		return convertView;
	}
	
	private void clickImg(final ViewHolder viewHolder, final Photo4Gallery mj, final int position) {
		viewHolder.imgPic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			    if (mj.type == Photo4Gallery.FUNCTION_TYPE) {
			        ((CircleImagePubActivity)context).PhotoChooseOption();
		        }
		        else {
		            ((CircleImagePubActivity)context).DelAndShowbigImage(position);
		        }
			}
		});
	}
	
	
}
