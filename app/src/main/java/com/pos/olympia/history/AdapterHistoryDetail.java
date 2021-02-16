package com.pos.olympia.history;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pos.olympia.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class AdapterHistoryDetail extends RecyclerView.Adapter<AdapterHistoryDetail.ViewHolder> {

    private ArrayList<HashMap<String, String>> arr;
    private Context context;
    private OnItemClicked onClick;


    public AdapterHistoryDetail(Context context,ArrayList<HashMap<String, String>> itemL) {
        this.arr = itemL;
        this.context = context;

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView item_amount, item_time;

        public ViewHolder(View itemView) {
            super(itemView);
            item_amount = itemView.findViewById(R.id.item_amount);
            item_time = itemView.findViewById(R.id.item_time);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_layout,
                parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position){


        if(arr.size()>0) {
            Log.e("TAG" , "fize "+arr.size());
            try{
                holder.item_amount.setText(arr.get(position).get("TotalPrice"));
                holder.item_time.setText(timeConversion(arr.get(position).get("CreatedTime")));

            }catch  (IndexOutOfBoundsException e) {
                // Log.e("Error", e.getMessage());
            }
        }else {
            //holder.tournament.setText("No match found !!");
        }

    }

    @Override
    public int getItemCount() {
        return arr.size();
    }

    public void setOnClick(OnItemClicked onClick) {
        this.onClick = onClick;
    }

    public interface OnItemClicked{
        void OnItemClick(View v, int position);
    }

    public String timeConversion(String date){
        SimpleDateFormat spf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date newDate= null;
        try {
            newDate = spf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        spf= new SimpleDateFormat("hh:mm aaa",Locale.ENGLISH);
        date = spf.format(newDate);
        return date;
    }

}


