package com.chk.mines.CustomAdapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chk.mines.Beans.Record;
import com.chk.mines.R;

import java.util.ArrayList;

/**
 * Created by chk on 18-3-19.
 */

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordHolder> {

    ArrayList<Record> mRecordList = new ArrayList<>();

    public RecordAdapter(ArrayList<Record> recordList) {
        this.mRecordList = recordList;
    }

    @Override
    public RecordHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_layout_record_item,parent,false);
        return new RecordHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecordHolder holder, int position) {
        Record record = mRecordList.get(position);
        holder.rank.setText((position+1)+"");
        holder.name.setText(record.getName()+"");
        holder.time.setText(record.getGameTime()+"");
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mRecordList.size();
    }

    class RecordHolder extends RecyclerView.ViewHolder {
        public TextView rank;
        public TextView name;
        public TextView time;

        public RecordHolder(View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.recordRank);
            name = itemView.findViewById(R.id.recordName);
            time = itemView.findViewById(R.id.recordTime);
        }
    }
}
