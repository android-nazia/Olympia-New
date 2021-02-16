package com.pos.olympia.stock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.pos.olympia.R;
import com.pos.olympia.model.ModelStock;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AdapterStock extends RecyclerView.Adapter<AdapterStock.Viewholder> {
    Context c;
    private ArrayList<ModelStock> arrayList, arrayList_copy;
    private final AdapterStock.OnItemClickListener listener;

    public AdapterStock(FragmentActivity activity, ArrayList<ModelStock> arrayLis, AdapterStock.OnItemClickListener listene) {
        this.c = activity;
        this.arrayList = arrayLis;
        this.listener = listene;
        this.arrayList_copy=new ArrayList<>();
        arrayList_copy.addAll(arrayList);
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(c).inflate(R.layout.row_stock, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Viewholder holder, @SuppressLint("RecyclerView") int position) {
        holder.pos=position;
        holder.tv_item.setText(arrayList.get(position).getItemName());

        int id = arrayList.get(position).getStockQuantity();
        holder.tv_qty.setText("Quantity : "+id);

        int name = arrayList.get(position).getStockPrice();
        holder.tv_price.setText("Rs : "+name);

        holder.img_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(holder.img_delete,arrayList.get(holder.getAdapterPosition()));
            }
        });

        holder.img_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(holder.img_edit, arrayList.get(holder.getAdapterPosition()));
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class Viewholder extends RecyclerView.ViewHolder {
        TextView tv_item, tv_qty, tv_price;
        ImageView img_delete, img_edit;
        int pos;

        public Viewholder(View itemView) {
            super(itemView);
            tv_item = itemView.findViewById(R.id.tv_item);
            tv_qty = itemView.findViewById(R.id.tv_qty);
            tv_price = itemView.findViewById(R.id.tv_price);
            img_delete = itemView.findViewById(R.id.img_delete);
            img_edit = itemView.findViewById(R.id.img_edit);

        }
    }

    public String parseDate(String time) {
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "dd MMM, yyyy";
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

    public interface OnItemClickListener {
        void onItemClick(View view, ModelStock item);
    }

    public void filter(CharSequence sequence) {
        ArrayList<ModelStock> temp = new ArrayList<>();
        if (!TextUtils.isEmpty(sequence)) {
            for (ModelStock s : arrayList) {
                if (s.getItemName().toLowerCase().contains(sequence)) {
                    temp.add(s);
                }
            }
        } else {
            temp.addAll(arrayList_copy);
        }
        arrayList.clear();
        arrayList.addAll(temp);
        notifyDataSetChanged();
        temp.clear();
    }



}
