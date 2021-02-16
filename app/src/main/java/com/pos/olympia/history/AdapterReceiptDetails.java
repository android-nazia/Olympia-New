package com.pos.olympia.history;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.pos.olympia.R;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class AdapterReceiptDetails extends RecyclerView.Adapter<AdapterReceiptDetails.VersionViewHolder> {

    private Context context;
    int poss;
    ArrayList<HashMap<String, String>> arrayListt;
    HashMap<String, String> model;

    public AdapterReceiptDetails(Context activity, int pos, ArrayList<HashMap<String, String>> arrayList_,
                                 HashMap<String, String> mod) {
        this.context = activity;
        this.poss = pos;
        this.arrayListt = arrayList_;
        this.model = mod;

    }

    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_receipt, viewGroup, false);
        return new VersionViewHolder(view);
    }

    @Override
    public void onBindViewHolder( VersionViewHolder holder, final int position) {
        holder.tv_item.setText(arrayListt.get(position).get("ItemName"));

        String qty_price = arrayListt.get(position).get("Quantity")+"X"
                +arrayListt.get(position).get("Price");
        holder.tv_qty.setText(qty_price);

        float quantity = Float.parseFloat(arrayListt.get(position).get("Quantity"));
        float price = Float.parseFloat(arrayListt.get(position).get("Price"));
        float each_price = quantity*price;
        holder.tv_price.setText(each_price+"");

    }

    @Override
    public int getItemCount() {
        return arrayListt.size();
    }

    static class VersionViewHolder extends RecyclerView.ViewHolder {
        TextView tv_item, tv_qty, tv_price, tv_amount;
        ImageView img_delete;

        public VersionViewHolder(View itemView) {
            super(itemView);
            tv_item = itemView.findViewById(R.id.item_name);
            tv_qty = itemView.findViewById(R.id.item_qty_price);
            tv_price = itemView.findViewById(R.id.item_price);
            img_delete = itemView.findViewById(R.id.img_delete);
            tv_amount = itemView.findViewById(R.id.tv_amount);
        }
    }


    public String parseDate(String time) {
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        String outputPattern = "dd MMM, yyyy hh:mma";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.ENGLISH);
        Date date = null;
        String str = null;
        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }


}
