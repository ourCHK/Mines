package com.chk.mines.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.chk.mines.Beans.Mine;

/**
 * Created by chk on 18-2-1.
 */

public abstract class MineView extends View{

    public static int DEVICE_WIDTH;
    public static int DEVICE_HEIGHT;

    Paint mPaint;
    Paint mNumPaint;
    Paint mCubePaint;


    int mWidth; //View的宽高
    int mHeight;

    int mMineSize; //雷的宽高
    int detalX; //画布的偏移
    int detalY;

    Mine[][] mines;
    int rows;
    int columns;
    RectF rectF;

    public MineView(Context context) {
        super(context);
        init1();
    }

    public MineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init1();
    }

    void init1() {
        DisplayMetrics dm = getResources().getDisplayMetrics(); //获取屏幕尺寸大小
        DEVICE_WIDTH = dm.widthPixels;
        DEVICE_HEIGHT = dm.heightPixels;
        mMineSize = DEVICE_WIDTH / 12;    //其实这里应该加个判断屏幕横竖的判断

        rectF = new RectF();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);
        mPaint.setTextSize(50);

        mNumPaint = new Paint();
        mNumPaint.setAntiAlias(true);
        mNumPaint.setColor(Color.BLACK);

        mCubePaint = new Paint();
        mCubePaint.setAntiAlias(true);
        mCubePaint.setColor(Color.GRAY);
        mCubePaint.setStyle(Paint.Style.FILL);

    }

    void init2() {
        mWidth = getWidth();
        mHeight = getHeight();

        detalX = mMineSize * 2;
        detalY = (mHeight - 8*mMineSize)/2;
        Log.i("init2",mWidth+"  "+mHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setViewSize();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init2();
    }

    public void setMines(Mine[][] mines) {
        this.mines = mines;
        if (mines != null) {
            rows = mines.length;
            columns = mines[0].length;
        }
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
//        drawFrame(canvas);
        drawColorCube(canvas);
    }

    /**
     * 绘制边框
     * @param canvas
     */
    public  void drawFrame(Canvas canvas) {
        canvas.save();
        canvas.translate(mMineSize,mMineSize);

        for (int i=0; i<=rows; i++) {
            canvas.drawLine(0,i*mMineSize,columns*mMineSize,i*mMineSize,mPaint);
        }

        for (int j=0; j<=columns; j++) {
            canvas.drawLine(j*mMineSize,0,j*mMineSize,rows*mMineSize,mPaint);
        }
        canvas.restore();
    }

    /**
     * 绘制雷
     * @param canvas
     */
    public abstract void drawMines(Canvas canvas);

    /**
     * 绘制方块
     * @param canvas
     */
    public void drawColorCube(Canvas canvas) {
        canvas.save();
        canvas.translate(mMineSize,mMineSize);
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                rectF.top = 5 + i*mMineSize;
                rectF.left = 5 + j * mMineSize;
                rectF.bottom = (i+1) * mMineSize - 5;
                rectF.right = (j+1) * mMineSize - 5;
                canvas.drawRoundRect(rectF,5,5,mCubePaint);
            }
        }
        canvas.restore();
    }

    /**
     * 绘制数字
     * @param canvas
     */
    public abstract void drawNum(Canvas canvas);

    /**
     * 这个方法在onMeasure中调用，可以在这个方法内利用LayoutParams设置设置view的大小
     */
    public abstract void setViewSize();



}
