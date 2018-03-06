package com.chk.mines.CustomViews;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by chk on 18-2-1.
 */

public class MineViewType1 extends MineView {

    public MineViewType1(Context context) {
        super(context);
    }

    public MineViewType1(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTranslateDetal() {
        detalX = mMineSize * 2;
        detalY = (mHeight - 8*mMineSize)/2;
    }

    @Override
    public void setViewSize() {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = DEVICE_WIDTH;
        setLayoutParams(lp);
    }
}
