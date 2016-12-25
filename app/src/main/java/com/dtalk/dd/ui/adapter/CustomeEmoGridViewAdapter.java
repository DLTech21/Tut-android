package com.dtalk.dd.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dtalk.dd.DB.entity.GifEmoEntity;
import com.dtalk.dd.ui.plugin.ImageLoadManager;
import com.dtalk.dd.utils.Logger;

import java.util.List;

/**
 * Created by Donal on 2016/12/24.
 */

public class CustomeEmoGridViewAdapter extends BaseAdapter {
    private Context context = null;
    private List<GifEmoEntity> emoResIds = null;

    public CustomeEmoGridViewAdapter(Context cxt, List<GifEmoEntity> ids) {
        this.context = cxt;
        this.emoResIds = ids;
    }

    @Override
    public int getCount() {
        return emoResIds.size();
    }

    @Override
    public Object getItem(int position) {
        return emoResIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return emoResIds.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            GridViewHolder gridViewHolder = null;
            if (null == convertView && null != context) {
                gridViewHolder = new GridViewHolder();
                convertView = gridViewHolder.layoutView;
                if (convertView != null) {
                    convertView.setTag(gridViewHolder);
                }
            } else {
                gridViewHolder = (GridViewHolder) convertView.getTag();
            }
            if (null == gridViewHolder || null == convertView) {
                return null;
            }
//            gridViewHolder.faceIv.setImageBitmap(getBitmap(position));
            ImageLoadManager.setImageGlide(context, emoResIds.get(position).getUrl(), gridViewHolder.faceIv);
            return convertView;
        } catch (Exception e) {
            Logger.e(e.getMessage());
            return null;
        }
    }

//    private Bitmap getBitmap(int position) {
//        Bitmap bitmap = null;
//        try {
//            bitmap = BitmapFactory.decodeResource(context.getResources(),
//                    emoResIds[position]);
//        } catch (Exception e) {
//            Logger.e(e.getMessage());
//        }
//        return bitmap;
//    }

    public class GridViewHolder {
        public LinearLayout layoutView;
        public ImageView faceIv;

        public GridViewHolder() {
            try {
                AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
                layoutView = new LinearLayout(context);
                faceIv = new ImageView(context);
                layoutView.setLayoutParams(layoutParams);
                layoutView.setOrientation(LinearLayout.VERTICAL);
                layoutView.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        105,
                        115);
                params.gravity = Gravity.CENTER;
                layoutView.addView(faceIv, params);
            } catch (Exception e) {
                Logger.e(e.getMessage());
            }
        }
    }
}
