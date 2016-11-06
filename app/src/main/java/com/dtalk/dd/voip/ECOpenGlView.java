package com.dtalk.dd.voip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.dtalk.dd.R;


/**
 * com.yuntongxun.ecdemo.ui.voip in ECDemo_Android
 * Created by Jorstin on 2015/7/8.
 */
public class ECOpenGlView extends SurfaceView
        implements SurfaceHolder.Callback{

    private static final String TAG = "ECSDK_Demo.ECOpenGlView";
    private Context mContext;
    public SurfaceHolder mSurfaceHolder;
    public boolean isSurfaceReady = false;
    Paint mPaint;
    Paint mPaint2;
    Paint mPaint3;
    int mStrokeWidth = 4;

    final int aLM = 72;
    final int aLN = 96;
    final int aLO = 92;
    final int aLP = 116;
    int mHeight = 240;
    int mWidth = 320;
    int aLR = mWidth / 2;
    int aLS = mHeight / 2;
    int aLT = 0;
    int aLU = 0;
    float LV = 1.0F;
    long mPreviousTime = 0;


    public ECOpenGlView(Context context) {
        super(context);
        mContext = context;
        initOpenGlView();
    }

    public ECOpenGlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initOpenGlView();
    }

    public ECOpenGlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initOpenGlView();
    }

    private void initOpenGlView() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setFilterBitmap(false);

        mPaint2 = new Paint();
        mPaint2.setColor(-1);
        mPaint2.setStyle(Paint.Style.STROKE);
        mPaint2.setStrokeWidth(mStrokeWidth);
        mPaint3 = new Paint();
        mPaint3.setColor(-1);
        mPaint3.setAntiAlias(true);
    }

    /**
     * 设置显示的款和高
     * @param width 宽
     * @param height 高
     */
    public void setDisplaySize(int width , int height) {
        mWidth = width;
        mHeight = height;
        setLayoutParams(new RelativeLayout.LayoutParams(mWidth, mHeight));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
    }

    public void drawBitmap(Bitmap bitmap) {
        if(bitmap == null || bitmap.isRecycled()) {
            return ;
        }
        if(mPreviousTime == 0L) {
            mPreviousTime = System.currentTimeMillis();
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Canvas canvas = mSurfaceHolder.lockCanvas();
        if(canvas == null) {
            return ;
        }

        Matrix matrix = new Matrix();
        float beW = canvas.getWidth() / height;
        float beH = canvas.getHeight() / width;
        matrix.postRotate(90.0F, 0.0F, height);
        matrix.postTranslate(0.0F, -height);
        matrix.postScale(beW , beH);
        canvas.drawBitmap(bitmap, matrix, mPaint);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isSurfaceReady = true;
        Canvas localCanvas = mSurfaceHolder.lockCanvas(null);
        localCanvas.drawColor(mContext.getResources().getColor(R.color.ec_voip_talk_bg_color));
        mSurfaceHolder.unlockCanvasAndPost(localCanvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        isSurfaceReady = true;
        try {
            mSurfaceHolder.removeCallback(this);
            mSurfaceHolder = holder;
            mSurfaceHolder.addCallback(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceReady = false;
    }
}
