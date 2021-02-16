package com.pos.olympia.home;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pos.olympia.R;
import com.pos.olympia.flytocart.CircleAnimationUtil;
import com.pos.olympia.model.ModelStock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdapterHome extends RecyclerView.Adapter<AdapterHome.Viewholder> {
    private Context c;
    private ImageView img_cart;
    private ProductItemActionListener actionListener;
    private ArrayList<ModelStock> arrayList, arrayList_copy;

    public AdapterHome(FragmentActivity activity, ImageView im_cart, ArrayList<ModelStock> arrayLis) {
        this.c = activity;
        this.img_cart = im_cart ;
        this.arrayList = arrayLis;
        this.arrayList_copy=new ArrayList<>();
        arrayList_copy.addAll(arrayList);
    }

    public void setActionListener(ProductItemActionListener actionListener) {
        this.actionListener = actionListener;
    }


    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(c).inflate(R.layout.row_home, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(final Viewholder holder, @SuppressLint("RecyclerView") int position) {
        holder.pos=position;
        holder.tv_item.setText(arrayList.get(position).getItemName());

        String price = "Price: Rs. "+arrayList.get(position).getStockPrice();
        holder.tv_price.setText(price);

        String quantity = "Left: "+arrayList.get(position).getStockQuantity();
        holder.tv_stock.setText(quantity);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionListener!=null)
                    actionListener.onItemTap(holder.img ,holder.getAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    static class Viewholder extends RecyclerView.ViewHolder {
        TextView tv_item, tv_price, tv_stock;
        ImageView img;
        int pos;

        public Viewholder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            tv_item = itemView.findViewById(R.id.tv_item);
            tv_price = itemView.findViewById(R.id.tv_price);
            tv_stock = itemView.findViewById(R.id.tv_stock);

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


    private void makeFlyAnimation(ImageView targetView) {
        new CircleAnimationUtil().attachActivity((Activity) c).setTargetView(targetView).setMoveDuration(500).setDestView(img_cart).setAnimationListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //btn_charge.setEnabled(true);
                //btn_charge.setTextColor(c.getResources().getColor(android.R.color.white));
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                //addItemToCart();
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        }).startAnimation();
    }


    public void animateTo(List<ModelStock> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);

    }

    private void applyAndAnimateRemovals(List<ModelStock> newModels) {
        for (int i = arrayList.size() - 1; i >= 0; i--) {
            final ModelStock model = arrayList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<ModelStock> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final ModelStock model = newModels.get(i);
            if (!arrayList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<ModelStock> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final ModelStock model = newModels.get(toPosition);
            final int fromPosition = arrayList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public ModelStock removeItem(int position) {
        final ModelStock model = arrayList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, ModelStock model) {
        arrayList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final ModelStock model = arrayList.remove(fromPosition);
        arrayList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }


    public interface ProductItemActionListener{
        void onItemTap(ImageView imageView , int position);
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