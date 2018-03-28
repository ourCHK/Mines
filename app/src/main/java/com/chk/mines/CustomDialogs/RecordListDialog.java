package com.chk.mines.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.chk.mines.Beans.RecordList;
import com.chk.mines.CustomAdapter.RecordAdapter;
import com.chk.mines.R;

/**
 * Created by chk on 18-3-15.
 */

public class RecordListDialog extends Dialog {

    ImageView mClose;
    RecyclerView mRecyclerView;
    Spinner mRecordSpinner;

    RecordList mTypeOneList;
    RecordList mTypeTwoList;
    RecordList mTypeThreeList;
    RecordAdapter mRecordAdapterOne;
    RecordAdapter mRecordAdapterTwo;
    RecordAdapter mRecordAdapterThree;
    Context mContext;

    public RecordListDialog(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public RecordListDialog(@NonNull Context context, int themeResId, RecordList listOne,  RecordList listTwo,  RecordList listThree ) {
        super(context, themeResId);
        this.mContext = context;
        mTypeOneList = listOne;
        mTypeTwoList = listTwo;
        mTypeThreeList = listThree;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_record);
        init();

        setCancelable(false);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth(); // 设置dialog宽度为屏幕的4/5
        getWindow().setAttributes(lp);

    }

    void init() {
        viewInit();
        dataInit();
    }

    void viewInit() {
        mClose = findViewById(R.id.close);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mRecyclerView = findViewById(R.id.recordRecyclerView);
        mRecordSpinner = findViewById(R.id.recordSpinner);
        mRecordSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(mContext, "you click the:"+i+"", Toast.LENGTH_SHORT).show();
                switch (i) {
                    case 0:
                        mRecyclerView.setAdapter(mRecordAdapterOne);
                        mRecordAdapterOne.notifyDataSetChanged();
                        break;
                    case 1:
                        mRecyclerView.setAdapter(mRecordAdapterTwo);
                        mRecordAdapterOne.notifyDataSetChanged();
                        break;
                    case 2:
                        mRecyclerView.setAdapter(mRecordAdapterThree);
                        mRecordAdapterOne.notifyDataSetChanged();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void dataInit() {
//        mCurrentList = new ArrayList<>();
//        mTypeOneList = new ArrayList<>();
//        mTypeTwoList = new ArrayList<>();
//        mTypeThreeList = new ArrayList<>();
//        for (int i=0;i<5;i++) {
//            Record record = new Record();
//            record.setGamePlayer("CHK1");
//            record.setGameTime(i);
//            mTypeOneList.add(record);
//            mCurrentList.add(record);
//        }
//        for (int i=0;i<5;i++) {
//            Record record = new Record();
//            record.setGamePlayer("CHK2");
//            record.setGameTime(i);
//            mTypeTwoList.add(record);
//        }
//        for (int i=0;i<5;i++) {
//            Record record = new Record();
//            record.setGamePlayer("CHK3");
//            record.setGameTime(i);
//            mTypeThreeList.add(record);
//        }
//        RecordList recordList = new RecordList(mTypeOneList);
        mRecordAdapterOne = new RecordAdapter(mTypeOneList);
        mRecordAdapterTwo = new RecordAdapter(mTypeTwoList);
        mRecordAdapterThree = new RecordAdapter(mTypeThreeList);

        mRecyclerView.setAdapter(mRecordAdapterOne);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecordAdapterOne.notifyDataSetChanged();
    }

    @Override
    public void show() {
        super.show();
        mRecordSpinner.setSelection(0);
        mRecyclerView.setAdapter(mRecordAdapterOne);
        mRecordAdapterOne.notifyDataSetChanged();
    }
}

