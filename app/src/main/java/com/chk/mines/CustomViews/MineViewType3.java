package com.chk.mines.CustomViews;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by chk on 18-2-1.
 */

public class MineViewType3 extends MineView{

    public MineViewType3(Context context) {
        super(context);
    }

    public MineViewType3(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setViewSize() {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = mMineSize*32;
        lp.height = mMineSize * 18;
        setLayoutParams(lp);
    }

    @Override
    public void setTranslateDetal() {
        detalX = mMineSize;
        detalY = mMineSize;
    }

}
