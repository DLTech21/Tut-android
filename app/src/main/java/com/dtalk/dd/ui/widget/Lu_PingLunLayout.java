package com.dtalk.dd.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dtalk.dd.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lu JianChao on 2016/5/31.
 * https://github.com/hnsugar
 *
 * @author Lu JianChao
 */
public class Lu_PingLunLayout extends LinearLayout {
    private List<Lu_Comment_TextView.Lu_PingLun_info_Entity> mEntities = new ArrayList<Lu_Comment_TextView.Lu_PingLun_info_Entity>();
    private Context mContext;
    private Lu_PingLunLayoutListener mLu_pingLunListener;

    public Lu_PingLunLayout(Context mContext, AttributeSet attrs) {
        super(mContext, attrs);
        this.mContext = mContext;
        this.setOrientation(VERTICAL);
    }

    public Lu_PingLunLayout(Context mContext, List<Lu_Comment_TextView.Lu_PingLun_info_Entity> mEntities) {
        super(mContext);
        this.mContext = mContext;
        this.setOrientation(VERTICAL);
    }

    public int getCount() {
        return mEntities.size();
    }

    public void setEntities(List<Lu_Comment_TextView.Lu_PingLun_info_Entity> mEntities, Lu_PingLunLayoutListener mListener) {
        this.mEntities = mEntities;
        mLu_pingLunListener = mListener;
        notifyDataSetChanged();
    }

    public TextView getView(final int position) {
        Lu_Comment_TextView converview = new Lu_Comment_TextView(mContext);
//        Lu_PingLunTextView.Lu_PingLun_info_Entity mEntity = converview.getLu_pingLun_info_entity("userid" + position, "username" + position, "userlogo" + position, "comment" + position);
        converview.setText_PingLun(mEntities.get(position), new Lu_Comment_TextView.Lu_PingLunListener() {
            @Override
            public void onNameClickListener(String onClickID, String onClickName, String onClickLogo, Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity, int FuncPosition) {
                mLu_pingLunListener.onNameClickListener(onClickID, onClickName, onClickLogo, mLu_pingLun_info_entity, FuncPosition, position);
            }

            @Override
            public void onTextClickListener(String onClickText, Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity, int FuncPosition) {
                mLu_pingLunListener.onTextClickListener(onClickText, mLu_pingLun_info_entity, FuncPosition, position);
            }

            @Override
            public void onClickOtherListener(Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity) {
                mLu_pingLunListener.onClickOtherListener(mLu_pingLun_info_entity, position);
            }

            @Override
            public void onLongClickListener(Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity) {
                mLu_pingLunListener.onLongClickListener(mLu_pingLun_info_entity, position);
            }

            @Override
            public void onClickListener(Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity) {
                mLu_pingLunListener.onClickListener(mLu_pingLun_info_entity, position);
            }
        });
        return converview;
    }

    public void notifyDataSetChanged() {

        removeAllViews();
        if (mEntities == null || mEntities.size() == 0) {
            return;
        }
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < getCount(); i++) {
            TextView view = getView(i);
            if (view == null) {
                throw new NullPointerException("listview item layout is null, please check getView()...");
            }
            view.setPadding(5, 2, 5, 2);
            addView(view, layoutParams);
            if (i < getCount() - 1) {
                TextView line = new TextView(mContext);
                line.setHeight(1);
                line.setBackgroundResource(R.drawable.divider);
                addView(line, layoutParams);
            }
        }
    }

    public void RemoveViewAtPosition(int Position) {
        mEntities.remove(Position);
        removeViewAt(Position * 2);
        if (Position != 0) {
            removeViewAt(Position * 2 - 1);
        }
        notifyDataSetChanged();
    }

    public interface Lu_PingLunLayoutListener {
        /**
         * 返回点击评论区被选择人信息
         * 为什么要传回这么多参数？总比少了强，哪个方便用哪个！！！
         *
         * @param onClickID               被点击用户ID
         * @param onClickName             被点击用户名
         * @param mLu_pingLun_info_entity 评论消息对象
         * @param FuncPosition            被点击位置，1为第一个人，2为第二个人，3为评论区点击
         * @param itemPosition            被点击评论位置
         */
        public void onNameClickListener(String onClickID, String onClickName, String onClickLogo, Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity, int FuncPosition, int itemPosition);

        /**
         * 评论文本监听
         *
         * @param onClickText
         * @param mLu_pingLun_info_entity
         * @param FuncPosition
         * @param itemPosition
         */
        public void onTextClickListener(String onClickText, Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity, int FuncPosition, int itemPosition);

        /**
         * 点击其他区域监听
         *
         * @param mLu_pingLun_info_entity
         */
        public void onClickOtherListener(Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity, int itemPosition);

        /**
         * 长按任何位置监听
         *
         * @param mLu_pingLun_info_entity
         */
        public void onLongClickListener(Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity, int itemPosition);

        /**
         * 任何位置点击监听
         *
         * @param mLu_pingLun_info_entity
         */
        public void onClickListener(Lu_Comment_TextView.Lu_PingLun_info_Entity mLu_pingLun_info_entity, int itemPosition);
    }
}
