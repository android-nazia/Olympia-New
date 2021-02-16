package com.pos.olympia.history;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.olympia.R;
import com.pos.olympia.db.DatabaseHandler;
import com.pos.olympia.model.ModelStock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import cn.refactor.lib.colordialog.PromptDialog;

/**
 * Created by ANDROID on 12/13/2017.
 */

public class FragmentReceiptDetails extends Fragment {
    RecyclerView recyclerview;
    TextView order_id, tv_date, total_value, discount_value, cash_value, cashier_name;
    Toolbar toolbar_receipt_details;
    ImageView left;
    Button btn_cancel;
    String unique_key, cancel;
    DatabaseHandler handler;
    SQLiteDatabase db;
    ArrayList<HashMap<String, String>> arrayList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new DatabaseHandler(getActivity());
        db = handler.getWritableDatabase();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receipts_details, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();


        btn_cancel = view.findViewById(R.id.btn_cancel);
        order_id = view.findViewById(R.id.order_id);
        cashier_name = view.findViewById(R.id.cashier_name);
        tv_date = view.findViewById(R.id.tv_date);
        total_value = view.findViewById(R.id.total_value);
        discount_value = view.findViewById(R.id.discount_value);
        cash_value = view.findViewById(R.id.cash_value);

        recyclerview = view.findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerview.setLayoutManager(layoutManager);
        recyclerview.setHasFixedSize(true);


        /*Log.e("TAG", "hhh "+arrayList.size());
        for(int i=0;i<arrayList.size();i++){
            Log.e("TAG", "HistoryId - "+arrayList.get(i).get("Item"));
        }*/

        int pos = getArguments().getInt("position");
        arrayList= (ArrayList<HashMap<String, String>>)
                getArguments().getSerializable("arr_his");
        HashMap<String, String> model = (HashMap<String, String>)
                getArguments().getSerializable("arr_ord");



        order_id.setText(model.get("OrderId"));
        unique_key=model.get("UniqueKey");
        cancel = model.get("CancelOrder");
        if(cancel.equals("yes")){
            btn_cancel.setVisibility(View.GONE);
        }
        tv_date.setText(parseDate(model.get("CreatedTime")));
        total_value.setText("Rs. "+model.get("TotalPrice"));
        discount_value.setText(model.get("Discount")+" %");
        cash_value.setText("Rs. "+model.get("AmountPayable"));
        cashier_name.setText("Cashier : "+model.get("User"));


        AdapterReceiptDetails adapter = new AdapterReceiptDetails(getActivity(), pos , arrayList, model);
        recyclerview.setAdapter(adapter);

        Toolbar toolbar = view.findViewById(R.id.toolbar_receipt_details);
        left = toolbar.findViewById(R.id.left);
        TextView tv_refund = toolbar.findViewById(R.id.tv_refund);
        TextView save = toolbar.findViewById(R.id.save);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //moveToSales();
                //FragmentReceiptDetails.this.getActivity().onBackPressed();
                //getFragmentManager().beginTransaction().remove(FragmentReceiptDetails.this).commit();
                getFragmentManager().popBackStack();
            }
        });


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new PromptDialog(getActivity())
                        .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                        .setAnimationEnable(true)
                        .setTitleText("Cancel Order")
                        .setContentText("Are you sure you want to cancel the order?")
                        .setPositiveListener("Yes", new PromptDialog.OnPositiveListener() {
                            @Override
                            public void onClick(PromptDialog dialog) {
                                dialog.dismiss();
                                updateIntoHistory(unique_key);
                                updateIntoOrder(unique_key);
                                for(int i=0;i<arrayList.size();i++){
                                    fetchSelectedStocks(arrayList.get(i).get("ItemId"),
                                            Integer.parseInt(arrayList.get(i).get("Quantity")));
                                }

                                Toast.makeText(getActivity(), "Cancelled" , Toast.LENGTH_SHORT).show();
                                getFragmentManager().popBackStack();
                                //getActivity().onBackPressed();
                            }
                        }).show();


            }
        });

        return view;
    }



    private void updateIntoHistory(String id){
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.CancelOrder,"yes");
        db.update(DatabaseHandler.HistoryTable,values,DatabaseHandler.UniqueKey+" = "
                +"'"+id+"'", null);
        Log.e("TAG" , "--data updated in History");
    }

    private void updateIntoOrder(String id){
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.CancelOrder,"yes");
        db.update(DatabaseHandler.OrderTable,values,DatabaseHandler.UniqueKey+" = "
                +"'"+id+"'", null);
        Log.e("TAG" , "--data updated in Order");
    }

    private void fetchSelectedStocks(String id, int qty){
        String query_doc = "SELECT * FROM StockTable where ItemId = " + id ;
        Cursor cursor = db.rawQuery(query_doc,null);
        cursor.moveToFirst();
        if(cursor.getCount()>0) {
            int q = cursor.getInt(1);
            ContentValues values = new ContentValues();
            values.put(DatabaseHandler.StockQuantity,q+qty);
            db.update(DatabaseHandler.StockTable,values,DatabaseHandler.ItemId+" = "
                    +id, null);
            Log.e("TAG" , "--data updated in Order");
            cursor.moveToNext();
        }

    }

   /* public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.Viewholder> {

        int poss;
        ArrayList<HashMap<String, String>> arrayListt;
        HashMap<String, String> model;

        public DetailAdapter(int pos, ArrayList<HashMap<String, String>> arrayList_,
                             HashMap<String, String> mod) {
            this.poss = pos;
            this.arrayListt = arrayList_;
            this.model = mod;
        }

        @NonNull
        @Override
        public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity().getApplicationContext())
                    .inflate(R.layout.row_receipt, parent, false);
            return new Viewholder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Viewholder holder, final int position) {
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

        class Viewholder extends RecyclerView.ViewHolder {
            TextView tv_item, tv_qty, tv_price, tv_amount;
            ImageView img_delete;

            public Viewholder(View itemView) {
                super(itemView);
                tv_item = itemView.findViewById(R.id.item_name);
                tv_qty = itemView.findViewById(R.id.item_qty_price);
                tv_price = itemView.findViewById(R.id.item_price);
                img_delete = itemView.findViewById(R.id.img_delete);
                tv_amount = itemView.findViewById(R.id.tv_amount);
            }
        }

    }*/

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
