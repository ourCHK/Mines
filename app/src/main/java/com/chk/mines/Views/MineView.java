package com.chk.mines.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

/**
 * Created by chk on 18-1-31.
 */

public class MineView extends SurfaceView implements SurfaceHolder.Callback,Runnable{

    public static int DEVICE_WIDTH;
    public static int DEVICE_HEIGHT;

    SurfaceHolder mHolder;

    int mWidth;
    int mHeight;
    int testX;

    Canvas canvas;
    Paint mPaint;

    Thread mGameThread;
    boolean isOnRun;


    public MineView(Context context) {
        super(context);
        init1();
    }

    public MineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init1();
    }

    void init1() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);

        DisplayMetrics dm = getResources().getDisplayMetrics(); //获取屏幕尺寸大小
        DEVICE_WIDTH = dm.widthPixels;
        DEVICE_HEIGHT = dm.heightPixels;

        mHolder = getHolder();
        mHolder.addCallback(this);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(20);
        mPaint.setStyle(Paint.Style.STROKE);

        mGameThread = new Thread(this);


    }

    void init2() {
        mWidth = getWidth();
        mHeight = getHeight();

        Log.i("init2",mWidth+"  "+mHeight);

        onMyDraw();

        if (!mGameThread.isAlive()) {
            isOnRun = true;
            mGameThread.start();
            Log.i("init2","run");
        }
    }

    @Override
    public void run() {
        while (isOnRun) {
            try {
                onMyDraw();
                Log.i("onDraw","run");
                scrollTo(testX++,0);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void onMyDraw() {
        canvas = mHolder.lockCanvas();
        if (canvas != null) {
            canvas.drawRect(0,0,mWidth,mHeight,mPaint);
            canvas.drawText("Hello",100,100,mPaint);
            Log.i("onMyDraw","run");
            mHolder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = 2000;
        layoutParams.height = 3000;
        this.setLayoutParams(layoutParams);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        init2();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isOnRun = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }


    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }
}
