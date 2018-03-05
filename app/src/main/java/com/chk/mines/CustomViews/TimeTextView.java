package com.chk.mines.CustomViews;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by chk on 18-2-3.
 */

public class TimeTextView extends android.support.v7.widget.AppCompatTextView{

    public TimeTextView(Context context) {
        super(context);
        init(context);
    }

    public TimeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        Typeface newFont = Typeface.createFromAsset(context.getAssets(), "fonts/display_font.ttf");
        setTypeface(newFont);
    }
}
