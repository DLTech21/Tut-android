package com.dtalk.dd.ui.widget.praisewidget.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.LruCache;
import android.widget.TextView;

import com.dtalk.dd.R;
import com.dtalk.dd.ui.widget.praisewidget.bean.PraiseBean;
import com.dtalk.dd.ui.widget.praisewidget.clickable.PraiseClick;

import java.lang.reflect.Field;
import java.util.List;


/**
 * Created by 大灯泡 on 2015/9/25.
 * 这是实现点赞显示的控件<br>
 * <strong>请用setDataByArray（PraiseBean数组）来绑定数据</strong>
 * <br>
 * <p>
 * <br>
 * <strong>
 * smaple:<br>
 * </strong>
 * <p>
 * < com.weijuba.widget.moment.PraiseWidget<br>
 * <l>android:id="@+id/test_friend"</l><br>
 * <l>android:layout_width="250dp"//可以是match</l><br>
 * <l>android:maxLines="3"//最大显示行数</l><br>
 * <l>android:lineSpacingExtra="2.5px" //行距</l><br>
 * <l>android:lineSpacingMultiplier="1"//行距倍数</l><br>
 * <l>android:layout_height="wrap_content"</l><br>
 * <l>app:font_size="@dimen/sp_16"//内部字体大小</l><br>
 * <l>app:font_color="#ff259cf8"//内部字体颜色</l><br>
 * <l>app:zan_icon="@drawable/ba_zan"//赞图标</l><br>
 * </p>
 * <l>/></l><br>
 */
public class PraiseWidget extends TextView {
    private static final String TAG = "PraiseWidget";

    //===================参数定义====================
    private int color = 0xff517fae;
    private int size = 16;
    private Context mContext;
    private int iconID = R.drawable.ic_moment_liked;
    private List<PraiseBean> mBeans;
    private int curLine;//渲染当前文本的行数
    private int mMaxLine = 3;
    float LineSpacingMultiplier = 0.0f;
    float LineSpacingExtra = 0.0f;
    private int clickBgColor = 0x00000000;
    private PraiseWidgetListener praiseWidgetListener;

    //缓存
    private static LruCache<String, SpannableStringBuilderAllVer> mCache =
            new LruCache<String, SpannableStringBuilderAllVer>(50) {
                @Override
                protected int sizeOf(String key, SpannableStringBuilderAllVer value) {
                    return 1;
                }
            };

    //销毁窗口记得清除缓存，清掉对context的引用
    public static void clearPraiseWidgetCache() {
        if (mCache != null) mCache.evictAll();
    }

    public static int getPraiseWidgetCacheEvictionCount() {
        if (mCache != null) {
            return mCache.evictionCount();
        } else {
            return -1;
        }
    }

    public PraiseWidget(Context context) {
        this(context, null);
    }

    public PraiseWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PraiseWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;

        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.PraiseWidget);
        this.color = attr.getColor(R.styleable.PraiseWidget_font_color, 0xff517fae);
        this.size = attr.getDimensionPixelSize(R.styleable.PraiseWidget_font_size, 16);
        this.iconID =
                attr.getResourceId(R.styleable.PraiseWidget_zan_icon, R.drawable.ic_moment_liked);
        TypedArray systemAttr =
                context.obtainStyledAttributes(attrs, new int[]{android.R.attr.maxLines});
        this.mMaxLine = systemAttr.getInt(0, 3);
        this.clickBgColor = attr.getColor(R.styleable.PraiseWidget_click_bg_color, 0x00000000);
        attr.recycle();
        systemAttr.recycle();
        //如果不设置，clickableSpan不能响应点击事件
        this.setMovementMethod(LinkMovementMethod.getInstance());
        this.setHighlightColor(0x00000000);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getMeasuredWidth() > 0) {
            renderView();
        }
    }

    //------------------------------------------传参-----------------------------------------------
    public void setDataByArray(List<PraiseBean> list) {
        this.mBeans = list;
        if (getMeasuredWidth() > 0) {
            renderView();
        } else {
            requestLayout();
        }
    }

    public void setPraiseWidgetListener(PraiseWidgetListener praiseWidgetListener) {
        this.praiseWidgetListener = praiseWidgetListener;
    }

    private void renderView() {
        if (mBeans == null || mBeans.size() == 0) {
            setText("");
            return;
        }

        int textTotalWidth = getMeasuredWidth();
        //从缓存读取，避免重复测量导致的过多对象被创建问题
        String key = Integer.toString(mBeans.hashCode()) + mBeans.size() + textTotalWidth;
        SpannableStringBuilderAllVer spannable = mCache.get(key);
        if (spannable != null) {
            setText(spannable);
        } else {
            int lastPos = 0;//最后一个位置
            curLine = 0;
            int maxLine = mMaxLine;
            int beanSize = mBeans.size();
            String peopleCount =
                    mContext.getResources().getString(R.string.praise_zan, mBeans.size());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("like  ");//预留位置给点赞的心，防止超出指定行数行
            for (int i = 0; i < beanSize; i++) {
                stringBuilder.append(mBeans.get(i).userNick);
                /**测量当前文字的所属行数（加上“等xxx人测量，保证最后一个可以被顶替掉”）*/
                curLine = createWorkingLayout(stringBuilder.toString() + peopleCount,
                        textTotalWidth).getLineCount();
                if (curLine <= maxLine) {
                    lastPos = i;
                    stringBuilder.append(", ");
                } else {
                    break;
                }
            }
            spannable = addClickablePart(lastPos);
            setText(spannable);
            mCache.put(key, spannable);
        }
    }

    private SpannableStringBuilderAllVer addClickablePart(int LastPos) {
        // 第一个心心图标
        CustomImageSpan span = new CustomImageSpan(mContext, iconID);
        //空字符，保证有一个位置
        SpannableString spanStr = new SpannableString(" ");
        spanStr.setSpan(span, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        // 构建 builder
        SpannableStringBuilderAllVer spanBuilder = new SpannableStringBuilderAllVer(spanStr);

        for (int i = 0; i <= LastPos; i++) {
            PraiseBean bean = mBeans.get(i);
            if (i == 0) {
                spanBuilder.append("  " + bean.userNick,
                        new PraiseClick(mContext, bean.userNick, bean.userId, color, size, praiseWidgetListener), 0);
            } else {
                spanBuilder.append(mBeans.get(i).userNick,
                        new PraiseClick(mContext, bean.userNick, bean.userId, color, size, praiseWidgetListener), 0);
            }
            if (i != LastPos) spanBuilder.append(", ");
        }
        if (LastPos < mBeans.size() - 1) {
            //等xxx人
            return spanBuilder.append(
                    mContext.getResources().getString(R.string.praise_zan, mBeans.size() - LastPos));
        } else {
            return spanBuilder;
        }
    }

    private Layout createWorkingLayout(String workingText, int textTotalWidth) {

        /**
         *  float spacingmult:相对行间距，相对字体大小，1.5f表示行间距为1.5倍的字体高度。
         *  float spacingadd:在基础行距上添加多少
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            LineSpacingMultiplier = getLineSpacingMultiplier();
            LineSpacingExtra = getLineSpacingExtra();
        } else {
            if (LineSpacingMultiplier == 0.0f && LineSpacingExtra == 0.0f) {
                try {
                    Field Multiplier = TextView.class.getDeclaredField("mSpacingMult");
                    Multiplier.setAccessible(true);
                    LineSpacingMultiplier = Multiplier.getFloat(this);

                    Field SpacingExtra = TextView.class.getDeclaredField("mSpacingAdd");
                    SpacingExtra.setAccessible(true);
                    LineSpacingExtra = SpacingExtra.getFloat(this);
                } catch (Exception e) {
                    e.printStackTrace();
                    LineSpacingMultiplier = 1.0f;
                    LineSpacingExtra = 3.0f;
                }
            }
        }
        return new DynamicLayout(workingText, getPaint(),
                (textTotalWidth == 0 ? getScreenPixWidth(mContext) : textTotalWidth),
                Layout.Alignment.ALIGN_NORMAL, LineSpacingMultiplier, LineSpacingExtra, false);
    }

    /**
     * 获取屏幕分辨率：宽
     */
    public int getScreenPixWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public interface PraiseWidgetListener {
        public void onNameClickListener(String onClickID, String onClickName);
    }
}