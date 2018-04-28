package com.chk.mines.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.chk.mines.Interfaces.OnDialogButtonClickListener;
import com.chk.mines.R;

/**
 * Created by chk on 18-3-23.
 * 新纪录Dialog
 *
 */

public abstract class CustomGameDialog extends Dialog implements OnDialogButtonClickListener{

    Button leftButton;
    Button rightButton;
    EditText mineRow;
    EditText mineColumn;
    EditText minePercent;


    public CustomGameDialog(@NonNull Context context) {
        super(context);
    }

    public CustomGameDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_custom_game);
        setCancelable(false);

        mineRow = findViewById(R.id.mineRow);
        mineColumn = findViewById(R.id.mineColumn);
        minePercent = findViewById(R.id.minePercent);

        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLeftClick();
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRightClick();
            }
        });
    }

    /**
     * 判断是否有输入框为空
     * @return
     */
    public boolean isAnyEmpty() {
        return mineRow.getText().toString().isEmpty() || mineColumn.getText().toString().isEmpty() || minePercent.getText().toString().isEmpty();
    }

    public int getMineRow() {
        int row = Integer.parseInt(mineRow.getText().toString());
        return row;
    }

    public int getMineColumn() {
        int column = Integer.parseInt(mineColumn.getText().toString());
        return column;
    }

    public int getMinePercent() {
        int percent = Integer.parseInt(minePercent.getText().toString());
        return percent;
    }

    public int getMineCount() {
        int percent = Integer.parseInt(minePercent.getText().toString());
        int mineCount = getMineRow() * getMineColumn() * percent / 100;
        return mineCount;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        clearAll();
    }

    void clearAll() {
        mineRow.setText("");
        mineColumn.setText("");
        minePercent.setText("");
        minePercent.clearFocus();   //清除焦点
    }

}