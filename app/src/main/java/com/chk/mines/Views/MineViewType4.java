package com.chk.mines.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by chk on 18-2-1.
 */

public class MineViewType4 extends MineView{

    public MineViewType4(Context context) {
        super(context);
    }

    public MineViewType4(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void drawFrame(Canvas canvas) {

    }

    @Override
    public void setTranslateDetal() {
        detalX = mMineSize;
        detalY = mMineSize;
    }
    @Override
    public void drawColorCube(Canvas canvas) {

    }

    @Override
    public void setViewSize() {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = DEVICE_WIDTH;
        setLayoutParams(lp);
    }
}
