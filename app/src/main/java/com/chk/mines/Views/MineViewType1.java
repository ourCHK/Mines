package com.chk.mines.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by chk on 18-2-1.
 */

public class MineViewType1 extends MineView{

    public MineViewType1(Context context) {
        super(context);
    }

    public MineViewType1(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void drawMines(Canvas canvas) {

    }

    @Override
    public void drawColorCube(Canvas canvas) {
        canvas.save();
        canvas.translate(detalX,detalY);
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

    @Override
    public void drawFrame(Canvas canvas) {
        canvas.save();
        canvas.translate(detalX,detalY);

        for (int i=0; i<=rows; i++) {
            canvas.drawLine(0,i*mMineSize,rows*mMineSize,i*mMineSize,mPaint);
        }

        for (int j=0; j<=columns; j++) {
            canvas.drawLine(j*mMineSize,0,j*mMineSize,columns*mMineSize,mPaint);
        }
        canvas.restore();
    }

    @Override
    public void drawNum(Canvas canvas) {

    }

    @Override
    public void setViewSize() {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = DEVICE_WIDTH;
//        lp.height = mMineSize * 12;
        setLayoutParams(lp);
    }
}
