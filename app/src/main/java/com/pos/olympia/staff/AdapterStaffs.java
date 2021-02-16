package com.pos.olympia.staff;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.pos.olympia.R;
import com.pos.olympia.model.ModelStaff;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AdapterStaffs extends RecyclerView.Adapter<AdapterStaffs.Viewholder> {
    Context c;
    private ArrayList<ModelStaff> arrayList;
    private final OnItemClickListener listener;

    public AdapterStaffs(FragmentActivity activity, ArrayList<ModelStaff> arrayLis,
                         OnItemClickListener listene) {
        this.c = activity;
        this.arrayList = arrayLis;
        this.listener = listene;
    }

    @NonNull
    @Override
    public AdapterStaffs.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(c).inflate(R.layout.row_staff, parent, false);
        return new AdapterStaffs.Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterStaffs.Viewholder holder, final int position) {
        holder.pos=position;
        holder.tv_name.setText(arrayList.get(position).getStaffName());

        String id = arrayList.get(position).getStaffPin();
        holder.tv_pin.setText("Pin : "+id);

        String phone = arrayList.get(position).getStaffPhone();
        holder.tv_phone.setText("Mobile No : "+phone);

        String type = arrayList.get(position).getStaffType();
        holder.tv_type.setText(type);
        if(type.equalsIgnoreCase("admin")){
            holder.img_delete.setVisibility(View.GONE);
            holder.tv_pin.setVisibility(View.GONE);
        }else{
            holder.img_delete.setVisibility(View.VISIBLE);
            holder.tv_pin.setVisibility(View.VISIBLE);
        }

        holder.img_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(holder.img_delete,arrayList.get(position));
            }
        });

        holder.img_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(holder.img_edit, arrayList.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class Viewholder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_pin, tv_type, tv_phone;
        ImageView img_delete, img_edit;
        int pos;

        public Viewholder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_pin = itemView.findViewById(R.id.tv_pin);
            tv_type = itemView.findViewById(R.id.tv_type);
            tv_phone = itemView.findViewById(R.id.tv_phone);
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
        void onItemClick(View view, ModelStaff item);
    }

}
