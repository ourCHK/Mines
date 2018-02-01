package com.chk.mines.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

/**
 * Created by chk on 18-2-1.
 */

public class MineViewType2 extends MineView{

    public MineViewType2(Context context) {
        super(context);
    }

    public MineViewType2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void drawMines(Canvas canvas) {

    }


    @Override
    public void drawNum(Canvas canvas) {

    }

    @Override
    public void setViewSize() {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = mMineSize*18;
        lp.height = mMineSize*18;
        setLayoutParams(lp);
    }
}
