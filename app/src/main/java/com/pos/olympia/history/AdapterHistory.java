package com.pos.olympia.history;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pos.olympia.R;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterHistory extends RecyclerView.Adapter<AdapterHistory.ViewHolder> {

    private ArrayList<HashMap<String, String>> itemList,  itemList_short;
    private Context context;
    private OnItemClicked onClick;
    private RecyclerView.LayoutManager mLayoutmanager;
    ArrayList<ArrayList<HashMap<String, String>>> arrayList;

    public AdapterHistory(Context context, ArrayList<HashMap<String, String>> itemLi,
                          ArrayList<HashMap<String, String>> itemList_shor) {
        this.itemList_short = itemList_shor;
        this.itemList = itemLi;
        this.context = context;
        arrayList = new ArrayList<>();

        if(itemList_short.size()>0) {


            for (int i = 0; i < itemList.size(); i++) {
                ArrayList<HashMap<String, String>> sub_list = new ArrayList<>();

                for (int k = 0; k < itemList_short.size(); k++) {

                    if(i==0 && k==0){
                        sub_list.add(itemList.get(i));
                    }else{

                        Log.e("TAG", "cc "+itemList.get(i).get("DateTime") +"|"
                                + itemList_short.get(k).get("DateTime"));

                        if(itemList.get(i).get("DateTime")
                                .equals(itemList_short.get(k).get("DateTime"))) {
                            sub_list.add(itemList.get(i));
                        }
                    }

                }
                arrayList.add(sub_list);
            }



        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView date, receipt;
        RecyclerView recyclerview;
        public ViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            receipt = itemView.findViewById(R.id.receipt);
            recyclerview = itemView.findViewById(R.id.recyclerview);
            mLayoutmanager = new LinearLayoutManager(context);
            recyclerview.setLayoutManager(mLayoutmanager);


        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_header_layout,
                parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position){


        if(itemList_short.size()>0) {
            try{
                holder.date.setText(itemList_short.get(position).get("Time"));
                //holder.receipt.setText(itemList.get(position).getTournamentName());


                populateRV(holder.recyclerview, arrayList.get(position));

            }catch  (IndexOutOfBoundsException e) {
                // Log.e("Error", e.getMessage());
            }
        }else {
            //holder.tournament.setText("No match found !!");
        }

    }

    private void populateRV(RecyclerView rc,ArrayList<HashMap<String, String>> itemL){
        AdapterHistoryDetail mAdapter = new AdapterHistoryDetail(context, itemL);
        rc.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        //mAdapter.setOnClick(context);
    }

    @Override
    public int getItemCount() {
        return itemList_short.size();
    }

    public void setOnClick(OnItemClicked onClick) {
        this.onClick = onClick;
    }

    public interface OnItemClicked{
        void OnItemClick(View v, int position);
    }


}


