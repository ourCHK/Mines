package com.chk.mines.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chk.mines.Interfaces.OnDialogButtonClickListener;
import com.chk.mines.R;

/**
 * Created by chk on 18-3-23.
 * 新纪录Dialog
 *
 */

public abstract class NewRecordDialog extends Dialog implements OnDialogButtonClickListener{

    TextView timeText;
    Button leftButton;
    Button rightButton;
    EditText inputName;

    int time;


    public NewRecordDialog(@NonNull Context context) {
        super(context);
    }

    public NewRecordDialog(@NonNull Context context, int themeResId, int time) {
        super(context, themeResId);
        this.time = time;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_new_record);

        timeText = findViewById(R.id.successTime);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        inputName = findViewById(R.id.inputName);

        timeText.setText("时间："+time+"秒");

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLeftClick();
                dismiss();
                setInputNameText("");
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRightClick();
                dismiss();
                setInputNameText("");
            }
        });
    }

    public void getInputNameText() {
        inputName.getText().toString();
    }

    public void setInputNameText(String text) {
        inputName.setText(text);
    }


}
