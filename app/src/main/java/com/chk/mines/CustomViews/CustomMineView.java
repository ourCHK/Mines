package com.chk.mines.CustomViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.chk.mines.Beans.Mine;
import com.chk.mines.R;

import static com.chk.mines.GameActivity.PointDown;

/**
 * Created by chk on 18-3-2.
 * 可自定义的MineView，其实后面可以全部按这个来
 */

public class CustomMineView extends View {

    public static int DEVICE_WIDTH;
    public static int DEVICE_HEIGHT;

    Handler mGameHandler;
    boolean isGameOver;

    Bitmap mBlackMineBitmap;
    Bitmap mRedMineBitmap;
    Bitmap mFlagBitmap;
    Bitmap mFlagAndConfusedBitmap;
    Rect rectResize;
    Rect rectBitmap;

    Paint mPaint;
    Paint mNumPaint;
    Paint mOpenedCubePaint;
    Paint mCubePaint;
    int dX; //画布偏移X距离
    int dY; //画布偏移Y距离

    int mMineSize; //雷的宽高
    int pointX; //记录点击的位置
    int pointY;

    int mWidth; //View的宽高
    int mHeight;

    Mine[][] mines;
    int mMineCount;
    int rows;
    int columns;
    RectF rectCube;

    public CustomMineView(Context context, int rows, int columns) {
        super(context);
        this.rows = rows;
        this.columns = columns;
        init1();
    }

    public CustomMineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.rows = 12;
        this.columns = 12;
        init1();
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

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(dX,dY);
        drawColorCube(canvas);
        drawNum(canvas);
        drawMines(canvas);
        drawFlag(canvas);
        drawConfused(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                pointX = (int) event.getX();
                pointY = (int) event.getY();
                dealPointer(pointX,pointY);
                break;
        }
        return true;    //消耗该事件
    }

    void init1() {
        mBlackMineBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.mine_black);
        mRedMineBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.mine_red);
        mFlagBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.flag);
        mFlagAndConfusedBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.flag_confused);
        rectResize = new Rect();
        rectBitmap = new Rect(0,0,mBlackMineBitmap.getWidth(),mBlackMineBitmap.getHeight());

        DisplayMetrics dm = getResources().getDisplayMetrics(); //获取屏幕尺寸大小
        DEVICE_WIDTH = dm.widthPixels;
        DEVICE_HEIGHT = dm.heightPixels;
        mMineSize = DEVICE_WIDTH / 12;    //其实这里应该加个判断屏幕横竖的判断

        rectCube = new RectF();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);
        mPaint.setTextSize(50);

        mNumPaint = new Paint();
        mNumPaint.setAntiAlias(true);
        mNumPaint.setTextAlign(Paint.Align.CENTER);
        mNumPaint.setColor(Color.BLACK);
        mNumPaint.setTextSize(50);

        mOpenedCubePaint = new Paint();
        mOpenedCubePaint.setAntiAlias(true);
        mOpenedCubePaint.setTextAlign(Paint.Align.CENTER);
        mOpenedCubePaint.setColor(Color.parseColor("#DEDEDE"));
        mOpenedCubePaint.setTextSize(50);

        mCubePaint = new Paint();
        mCubePaint.setAntiAlias(true);
        mCubePaint.setColor(Color.GRAY);
        mCubePaint.setStyle(Paint.Style.FILL);

        dX = mMineSize;
        dY = mMineSize;
    }

    /**
     * 获取宽高
     */
    void init2() {
        mWidth = getWidth();
        mHeight = getHeight();
    }

    /**
     * 绘制方块
     * @param canvas
     */
    public void drawColorCube(Canvas canvas) {
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                rectCube.top = 5 + i*mMineSize;
                rectCube.left = 5 + j * mMineSize;
                rectCube.bottom = (i+1) * mMineSize - 5;
                rectCube.right = (j+1) * mMineSize - 5;
                if (mines[i][j].isOpen())
                    canvas.drawRoundRect(rectCube,5,5, mOpenedCubePaint);
                else
                    canvas.drawRoundRect(rectCube,5,5,mCubePaint);
            }
        }
    }

    /**
     * 绘制数字
     * @param canvas
     */
    public void drawNum(Canvas canvas) {
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                if (mines[i][j].getNum() == 0 && mines[i][j].isOpen())
                    continue;
                if (mines[i][j].isOpen() && !mines[i][j].isMine()) {
                    int baseY = (int) (mMineSize/2+mMineSize*i - ((mNumPaint.descent() + mNumPaint.ascent()) / 2));
                    canvas.drawText(mines[i][j].getNum()+"",mMineSize/2+mMineSize*j,baseY,mNumPaint);
                }
            }
        }
    }

    /**
     * 绘制雷
     * @param canvas
     */
    public void drawMines(Canvas canvas) {
        if (isGameOver) {
            int row = pointY/ mMineSize;
            int column = pointX / mMineSize;
            for (int i=0; i<rows; i++) {
                for (int j=0; j<columns; j++) {
                    if (mines[i][j].isMine()) {
                        rectResize.top = i * mMineSize;
                        rectResize.left = j * mMineSize;
                        rectResize.bottom = (i+1) * mMineSize;
                        rectResize.right = (j+1) * mMineSize;
                        if (!mines[i][j].isFlaged()) {
                            if (row == i && column == j)
                                canvas.drawBitmap(mRedMineBitmap, rectBitmap, rectResize,mPaint);
                            else
                                canvas.drawBitmap(mBlackMineBitmap, rectBitmap, rectResize,mPaint);
                        }
                    }
                }
            }
        }

    }

    /**
     * 绘制标记的旗帜
     */
    public void drawFlag(Canvas canvas) {
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                if (mines[i][j].isFlaged()) {
                    if (mines[i][j].isFlaged()) {
                        rectResize.top = i * mMineSize;
                        rectResize.left = j * mMineSize;
                        rectResize.bottom = (i+1) * mMineSize;
                        rectResize.right = (j+1) * mMineSize;
                        canvas.drawBitmap(mFlagBitmap, rectBitmap, rectResize,mPaint);
                    }
                }
            }
        }
    }

    public void drawConfused(Canvas canvas) {
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                if (mines[i][j].isConfused()) {
                    if (mines[i][j].isConfused()) {
                        rectResize.top = i * mMineSize;
                        rectResize.left = j * mMineSize;
                        rectResize.bottom = (i+1) * mMineSize;
                        rectResize.right = (j+1) * mMineSize;
                        if (isGameOver) {
                            if (mines[i][j].isMine())   //刚好位置也是雷，那就不画这个confused
                                continue;
                        }
                        canvas.drawBitmap(mFlagAndConfusedBitmap, rectBitmap, rectResize,mPaint);
                    }
                }
            }
        }
    }

    /**
     * 对点击的点进行事件处理
     * @param pointX
     * @param pointY
     */
    void dealPointer(int pointX, int pointY) {
        int row = (pointY - dY) / mMineSize;
        int column = (pointX - dX) / mMineSize;
        if (row<0 || row>= rows || column<0 || column >= columns)   //边界判断
            return;
        Message msg = mGameHandler.obtainMessage();
        msg.arg1 = row;
        msg.arg2 = column;
        msg.what = PointDown;
        mGameHandler.sendMessage(msg);
    }


    public void setGameOver() {
        isGameOver = true;
        Log.i("MineView","GameOver");
    }

    /**
     * 设置雷的数据并重新绘制雷
     * @param mines
     * @param mineCount
     */
    public void setMines(Mine[][] mines,int mineCount) {
        this.mines = mines;
        if (mines != null) {
            rows = mines.length;
            columns = mines[0].length;
            mMineCount = mineCount;
        }
        //必须重新初始化变量
        isGameOver = false;

        invalidate();
    }

    /**
     * 这个方法在onMeasure中调用，可以在这个方法内利用LayoutParams设置设置view的大小
     * 每个Cube的宽度是DEVICE_WIDTH/12
     */
    public void setViewSize() {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = columns * mMineSize + 2*mMineSize;   //多出来的两个用于左右间隔偏移
        lp.height = rows * mMineSize + 2*mMineSize;
        setLayoutParams(lp);
    }

    public void setHandler(Handler mHandler) {
        this.mGameHandler = mHandler;
    }
}
