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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.chk.mines.Beans.Mine;
import com.chk.mines.R;

import static com.chk.mines.GameActivity.GAME_OVER;
import static com.chk.mines.GameActivity.GAME_START;
import static com.chk.mines.GameActivity.GAME_SUCCESS;

/**
 * Created by chk on 18-2-1.
 */

public abstract class MineView extends View{

    public static int DEVICE_WIDTH;
    public static int DEVICE_HEIGHT;

    Bitmap mBlackMineBitmap;
    Bitmap mRedMineBitmap;
    Bitmap mFlagBitmap;
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

    PointType currentType = PointType.DRAG_MINE;    //默认是挖雷状态

    public enum PointType {    //用于判断点击下去时是什么状态，挖雷状态还是标记状态
        DRAG_MINE,FLAG_MINE
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
        checkResult();
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
                        if (row == i && column == j && !mines[i][j].isFlaged())
                            canvas.drawBitmap(mRedMineBitmap, rectBitmap, rectResize,mPaint);
                        else
                            canvas.drawBitmap(mBlackMineBitmap, rectBitmap, rectResize,mPaint);
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

    void setGameOver() {
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
        if (isGameOver || isGameSuccess)
            return;
        if (!isGameStart) { //开始游戏
            isGameStart = true;
            mHandler.sendEmptyMessage(GAME_START);
        }
        int row = (pointY - detalY) / mMineSize;
        int column = (pointX - detalX) / mMineSize;
        if (row<0 || row>= rows || column<0 || column >= columns)   //边界判断
            return;
        long startTime = System.currentTimeMillis();
        switch (currentType) {
            case DRAG_MINE:
                openCube(row,column);
                break;
            case FLAG_MINE:
                flagCube(row,column);
                break;
        }
        Log.i("MineView","openCube Cost Time:"+(System.currentTimeMillis() - startTime));
        invalidate();
    }

    /**
     * 标记雷
     * @param row
     * @param column
     */
    void flagCube(int row, int column) {
        if (mines[row][column].isOpen())
            return;
        if (mines[row][column].isFlaged())
            mines[row][column].setFlaged(false);
        else
            mines[row][column].setFlaged(true);
    }

    /**
     * 对周围进行递归找出可以打开的方块
     * @param row
     * @param column
     */
    void openCube(int row, int column) {
        if (mines[row][column].isMine()) {  //打开的是雷，游戏直接结束
            setGameOver();
            return;
        }

        if (mines[row][column].isOpen())    //已经打开过了，结束
            return;

        mines[row][column].setOpen(true);   //首先设置为打开状态
        mines[row][column].setFlaged(false);    //去掉标记状态

        if (mines[row][column].getNum() != 0) {    //如果打开的不是0，结束
            return;
        } else {     //如果打开的是0， 判断边界是否合法，对周围8个方向进行递归
            if (row-1 >= 0 && column-1 >= 0) {
                openCube(row-1,column-1);
            }

            if (row-1 >= 0) {
                openCube(row-1,column);
            }

            if (row-1 >= 0 && column+1 < columns) {
                openCube(row-1,column+1);
            }

            if (column-1 >= 0) {
                openCube(row,column-1);
            }

            if (column+1 < columns) {
                openCube(row,column+1);
            }

            if (row+1 < rows && column-1 >= 0 ) {
                openCube(row+1,column-1);
            }

            if (row+1 < rows) {
                openCube(row+1,column);
            }

            if (row+1 < rows && column+1 < columns) {
                openCube(row+1,column+1);

            }
        }
    }

    public void setCurrentType() {
        if (currentType == PointType.DRAG_MINE)
            this.currentType = PointType.FLAG_MINE;
        else
            this.currentType = PointType.DRAG_MINE;
    }

    /**
     * 检查游戏状态，结束还是成功?
     */
    void checkResult() {
        int openedCount = 0;
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                if (!mines[i][j].isMine() && mines[i][j].isOpen())
                    openedCount++;
            }
        }

        if (openedCount == rows * columns - mMineCount) {
            isGameSuccess = true;
            mHandler.sendEmptyMessage(GAME_SUCCESS); //通知GameActivity
        }
        if (isGameOver)
            mHandler.sendEmptyMessage(GAME_OVER);
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
        currentType = PointType.DRAG_MINE;
        setMines(mines,mineCount);
    }

}
