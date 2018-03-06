package com.chk.mines.CustomViews;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
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
    public void setTranslateDetal() {
        detalX = mMineSize;
        detalY = mMineSize;
    }

    @Override
    public void setViewSize() {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = mMineSize*18;
        lp.height = mMineSize*18;
        setLayoutParams(lp);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch(event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                break;
//            case MotionEvent.ACTION_MOVE:
//                break;
//            case MotionEvent.ACTION_UP:
//                break;
//        }
//        Log.i("MineViewType2",event.getX()+"   "+event.getY()+"   raw:"+event.getRawX()+"  "+event.getRawY());
//        return true;
//    }
}
