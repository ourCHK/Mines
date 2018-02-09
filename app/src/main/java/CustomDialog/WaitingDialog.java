package CustomDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.chk.mines.Interfaces.OnDialogButtonClickListener;

/**
 * Created by chk on 18-2-3.
 */

public class WaitingDialog extends Dialog {

    Context mContext;
    int mLayoutId;

    OnDialogButtonClickListener mOnDialogButtonClickListener;

    public WaitingDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public WaitingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    public WaitingDialog(@NonNull Context context, int themeResId, int layoutId) {
        super(context,themeResId);
        this.mContext = context;
        this.mLayoutId = layoutId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mLayoutId);

//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.width = display.getWidth() * 4 / 5 ; // 设置dialog宽度为屏幕的4/5
//        lp.height = display.getHeight() * 2 / 5;
//        getWindow().setAttributes(lp);
    }

    public void setOnDialogButtonClickListener(OnDialogButtonClickListener mOnDialogButtonClickListener) {
        this.mOnDialogButtonClickListener = mOnDialogButtonClickListener;
    }
}
