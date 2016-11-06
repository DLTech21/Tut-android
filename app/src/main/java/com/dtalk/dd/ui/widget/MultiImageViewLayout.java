package com.dtalk.dd.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dtalk.dd.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 多张图片显示
 * 因为使用Glide，View.setTag方法必须使用两个参数的方法：资源文件定义values定义一个ids文件，使用方式R.id.FriendLife_Position,
 * 加了缓存
 *
 * @author Lu JianChao
 *         Created by Lu JianChao on 2016/5/31.
 */

public class MultiImageViewLayout extends LinearLayout {
    public static int MAX_WIDTH = 0;
    private Context mContext;
    private ImageView mImageViewcache;
    // 照片的Url列表
    private List<String> imagesList;
    private List<ImageView> mImageViews = new ArrayList<ImageView>();

    /**
     * 长度 单位为Pixel
     **/
    private int pxOneMaxWandH;  // 单张图最大允许宽高
    private int pxMoreWandH = 0;// 多张图的宽高
    private int pxImagePadding = dip2px(getContext(), 3);// 图片间的间距
    private int MAX_PER_ROW_COUNT = 3;// 每行显示最大数

    private LayoutParams onePicPara;
    private LayoutParams morePara, moreParaColumnFirst;
    private LayoutParams rowPara;

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public MultiImageViewLayout(Context context) {
        super(context);
        mContext = context;
    }

    public MultiImageViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void setList(List<String> lists) throws IllegalArgumentException {
        if (lists == null) {
            throw new IllegalArgumentException("imageList is null...");
        }
        imagesList = lists;

        if (MAX_WIDTH > 0) {
            pxMoreWandH = (MAX_WIDTH - pxImagePadding * 2) / 3; //解决右侧图片和内容对不齐问题
            pxOneMaxWandH = MAX_WIDTH * 2 / 3;
            initImageLayoutParams();
        }

        initView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MAX_WIDTH == 0) {
            int width = measureWidth(widthMeasureSpec);
            if (width > 0) {
                MAX_WIDTH = width;
                if (imagesList != null && imagesList.size() > 0) {
                    setList(imagesList);
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            // result = (int) mTextPaint.measureText(mText) + getPaddingLeft()
            // + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by
                // measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private void initImageLayoutParams() {
        onePicPara = new LayoutParams(pxOneMaxWandH, LayoutParams.WRAP_CONTENT);
        moreParaColumnFirst = new LayoutParams(pxMoreWandH, pxMoreWandH);
        morePara = new LayoutParams(pxMoreWandH, pxMoreWandH);
        morePara.setMargins(pxImagePadding, 0, 0, 0);
        rowPara = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    // 根据imageView的数量初始化不同的View布局,还要为每一个View作点击效果
    private void initView() {
        this.setOrientation(VERTICAL);
        RemoveAllChild(this);
        if (MAX_WIDTH == 0) {
            //为了触发onMeasure()来测量MultiImageView的最大宽度，MultiImageView的宽设置为match_parent
            addView(new View(getContext()));
            return;
        }

        if (imagesList == null || imagesList.size() == 0) {
            return;
        }

        if (imagesList.size() == 1) {
            addView(getImageViewFromCache(0, false));
        } else {
            int allCount = imagesList.size();
            if (allCount == 4) {
                MAX_PER_ROW_COUNT = 2;
            } else {
                MAX_PER_ROW_COUNT = 3;
            }
            int rowCount = allCount / MAX_PER_ROW_COUNT + (allCount % MAX_PER_ROW_COUNT > 0 ? 1 : 0);// 行数
            for (int rowCursor = 0; rowCursor < rowCount; rowCursor++) {
                LinearLayout rowLayout = new LinearLayout(getContext());
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setLayoutParams(rowPara);
                if (rowCursor != 0) {
                    rowLayout.setPadding(0, pxImagePadding, 0, 0);
                }
                int columnCount = allCount % MAX_PER_ROW_COUNT == 0 ? MAX_PER_ROW_COUNT
                        : allCount % MAX_PER_ROW_COUNT;//每行的列数
                if (rowCursor != rowCount - 1) {
                    columnCount = MAX_PER_ROW_COUNT;
                }
                addView(rowLayout);
                int rowOffset = rowCursor * MAX_PER_ROW_COUNT;// 行偏移
                for (int columnCursor = 0; columnCursor < columnCount; columnCursor++) {
                    int position = columnCursor + rowOffset;
                    rowLayout.addView(getImageViewFromCache(position, true));
                }
            }
        }
    }

    private void RemoveAllChild(ViewGroup mViewGroup) {
        for (int i = 0; i < mViewGroup.getChildCount(); i++) {
            if (this.getChildAt(i) instanceof ViewGroup) {
                ((ViewGroup) getChildAt(i)).removeAllViews();
            }
        }
        this.removeAllViews();
    }

    private ImageView getImageViewFromCache(int position, boolean isMultiImage) {
        ImageView mImageView = null;
        if (!isMultiImage) {
            if (mImageViewcache == null) {
                mImageViewcache = new ImageView(getContext());
            }
            mImageViewcache.setAdjustViewBounds(true);
            mImageViewcache.setScaleType(ScaleType.FIT_START);
            mImageViewcache.setMaxHeight(pxOneMaxWandH);
            mImageViewcache.setLayoutParams(onePicPara);
            mImageView = mImageViewcache;
        } else {
            for (int i = 0; i < mImageViews.size(); i++) {
                if (mImageViews.get(i).getParent() == null) {
                    mImageView = mImageViews.get(i);
                    break;
                }
            }
            if (mImageView == null) {
                mImageView = new ImageView(getContext());
                mImageView.setScaleType(ScaleType.CENTER_CROP);
                mImageView.setLayoutParams(position % MAX_PER_ROW_COUNT == 0 ? moreParaColumnFirst : morePara);
                mImageViews.add(mImageView);
            }
        }
        final float[] PressX = new float[1];
        final float[] PressY = new float[1];
        String url = imagesList.get(position);
        mImageView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:  // 按下时图像变灰
                        ((ImageView) v).setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                        PressX[0] = event.getX();
                        PressY[0] = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:   // 手指离开或取消操作时恢复原色
                    case MotionEvent.ACTION_CANCEL:
                        ((ImageView) v).setColorFilter(Color.TRANSPARENT);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, (Integer) v.getTag(R.id.FriendLife_Position),PressX[0],PressY[0]);
                }
            }
        });
        mImageView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemLongClick(v, (Integer) v.getTag(R.id.FriendLife_Position),PressX[0],PressY[0]);
                }
                return false;
            }
        });
        mImageView.setId(url.hashCode());
        mImageView.setTag(R.id.FriendLife_Position, position);
        setImageGlide(mContext, url, mImageView);
//        ImageLoader.getInstance().displayImage(url, mImageView);
        return mImageView;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int PressImagePosition, float PressX, float PressY);

        public void onItemLongClick(View view, int PressImagePosition, float PressX, float PressY);
    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * glide加载图片
     */
    private RequestManager glideRequest;

    private void setImageGlide(Context mContext, String PicURL, ImageView mImageView) {
        glideRequest = Glide.with(mContext);
        glideRequest.load(PicURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .placeholder(R.drawable.tt_message_image_default)
                .error(R.drawable.tt_default_image_error)
                .into(mImageView);
    }
}