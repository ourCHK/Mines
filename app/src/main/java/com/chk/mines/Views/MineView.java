package com.chk.mines.Views;

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

import com.chk.mines.Beans.Mine;
import com.chk.mines.R;

import static com.chk.mines.GameActivity.PointDown;

/**
 * Created by chk on 18-2-1.
 */

public abstract class MineView extends View{

    public static int DEVICE_WIDTH;
    public static int DEVICE_HEIGHT;

    Bitmap mBlackMineBitmap;
    Bitmap mRedMineBitmap;
    Bitmap mFlagBitmap;
    Bitmap mFlagAndConfuzedBitmap;
    Rect rectResize;
    Rect rectBitmap;

    Paint mPaint;
    Paint mNumPaint;
    Paint mOpenedCubePaint;
    Paint mCubePaint;

    int mWidth; //View的宽高
    int mHeight;

    int mMineSize; //雷的宽高
    int detalX; //画布的偏移
    int detalY;

    int pointX;
    int pointY;

    Mine[][] mines;
    int mMineCount;
    int rows;
    int columns;
    int row;
    int column;
    RectF rectCube;
    int mDraggedCount;  //挖掘的数量

    Handler mHandler;
    boolean isGameStart;
    boolean isGameOver;
    boolean isGameSuccess;

    PointType currentType = PointType.DRAG;    //默认是挖雷状态

    public enum PointType {    //用于判断点击下去时是什么状态，挖雷状态还是标记状态,又或者是疑惑雷标记等等
        DRAG, FLAG, FLAG_CONFUSED;
    }

    public MineView(Context context) {
        super(context);
        init1();
    }

    public MineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init1();
    }

    void init1() {
        mBlackMineBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.mine_black);
        mRedMineBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.mine_red);
        mFlagBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.flag);
        mFlagAndConfuzedBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.flag_confused);
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
    }

    void init2() {
        mWidth = getWidth();
        mHeight = getHeight();

        setTranslateDetal();
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

    public void setMines(Mine[][] mines,int mineCount) {
        this.mines = mines;
        if (mines != null) {
            rows = mines.length;
            columns = mines[0].length;
            mMineCount = mineCount;
        }
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawColorCube(canvas);
        drawNum(canvas);
        drawMines(canvas);
        drawFlag(canvas);
        drawConfused(canvas);
    }

    /**
     * 绘制边框
     * @param canvas
     */
    public  void drawFrame(Canvas canvas) {
        canvas.save();
        canvas.translate(detalX,detalY);

        for (int i=0; i<=rows; i++) {
            canvas.drawLine(0,i*mMineSize,columns*mMineSize,i*mMineSize,mPaint);
        }

        for (int j=0; j<=columns; j++) {
            canvas.drawLine(j*mMineSize,0,j*mMineSize,rows*mMineSize,mPaint);
        }
        canvas.restore();
    }

    /**
     * 绘制方块
     * @param canvas
     */
    public void drawColorCube(Canvas canvas) {
        canvas.save();
        canvas.translate(detalX,detalY);
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
        canvas.restore();
    }

    /**
     * 设置绘制时画布的偏移量
     */
    public abstract void setTranslateDetal();

    /**
     * 这个方法在onMeasure中调用，可以在这个方法内利用LayoutParams设置设置view的大小
     */
    public abstract void setViewSize();

    /**
     * 绘制雷
     * @param canvas
     */
    public void drawMines(Canvas canvas) {
        if (isGameOver) {
            int row = (pointY - detalY) / mMineSize;
            int column = (pointX - detalX) / mMineSize;
            canvas.save();
            canvas.translate(detalX,detalY);
            for (int i=0; i<rows; i++) {
                for (int j=0; j<columns; j++) {
//                rectCube.top = 5 + i*mMineSize;
//                rectCube.left = 5 + j * mMineSize;
//                rectCube.bottom = (i+1) * mMineSize - 5;
//                rectCube.right = (j+1) * mMineSize - 5;
//                canvas.drawRoundRect(rectCube,5,5,mCubePaint);
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
            canvas.restore();
        }

    }

    /**
     * 绘制标记的旗帜
     * @param canvas
     */
    public void drawFlag(Canvas canvas) {
        canvas.save();
        canvas.translate(detalX,detalY);
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
        canvas.restore();
    }

    public void drawConfused(Canvas canvas) {
        canvas.save();
        canvas.translate(detalX,detalY);
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
                        canvas.drawBitmap(mFlagAndConfuzedBitmap, rectBitmap, rectResize,mPaint);
                    }
                }
            }
        }
        canvas.restore();
    }

    /**
     * 绘制数字
     * @param canvas
     */
    public void drawNum(Canvas canvas) {
        canvas.save();
        canvas.translate(detalX,detalY);
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
//                rectCube.top = 5 + i*mMineSize;
//                rectCube.left = 5 + j * mMineSize;
//                rectCube.bottom = (i+1) * mMineSize - 5;
//                rectCube.right = (j+1) * mMineSize - 5;
//                canvas.drawRoundRect(rectCube,5,5,mCubePaint);
                if (mines[i][j].getNum() == 0 && mines[i][j].isOpen())
                    continue;
                if (mines[i][j].isOpen() && !mines[i][j].isMine()) {
                    int baseY = (int) (mMineSize/2+mMineSize*i - ((mNumPaint.descent() + mNumPaint.ascent()) / 2));
                    canvas.drawText(mines[i][j].getNum()+"",mMineSize/2+mMineSize*j,baseY,mNumPaint);
                }
            }
        }
        canvas.restore();
    }

    public void setGameOver() {
        isGameOver = true;
        Log.i("MineView","GameOver");
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
        return true;
    }

    /**
     * 对点击的点进行事件处理
     * @param pointX
     * @param pointY
     */
    void dealPointer(int pointX, int pointY) {
//        if (isGameOver || isGameSuccess)
//            return;
//        if (!isGameStart) { //开始游戏
//            isGameStart = true;
//            mHandler.sendEmptyMessage(GAME_START);
//        }
        int row = (pointY - detalY) / mMineSize;
        int column = (pointX - detalX) / mMineSize;
        if (row<0 || row>= rows || column<0 || column >= columns)   //边界判断
            return;
        Message msg = mHandler.obtainMessage();
        msg.arg1 = row;
        msg.arg2 = column;
        msg.what = PointDown;
        mHandler.sendMessage(msg);
//        switch (currentType) {
//            case DRAG:
//                openCube(row,column);
//                break;
//            case FLAG:
//                flagCube(row,column);
//                break;
//        }
//        Log.i("MineView","openCube Cost Time:"+(System.currentTimeMillis() - startTime));
//        invalidate();
    }

    public void setCurrentType() {
        if (currentType == PointType.DRAG)
            this.currentType = PointType.FLAG;
        else
            this.currentType = PointType.DRAG;
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    /**
     * 重新开始游戏,初始化标志
     * @param mines
     * @param mineCount
     */
    public void restart(Mine[][] mines,int mineCount) {
        isGameOver = false;
        isGameSuccess = false;
        isGameStart = false;
        currentType = PointType.DRAG;
        setMines(mines,mineCount);
    }

}
