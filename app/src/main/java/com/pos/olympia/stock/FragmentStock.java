package com.pos.olympia.stock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.pos.olympia.R;
import com.pos.olympia.db.DatabaseHandler;
import com.pos.olympia.model.ModelStock;
import java.util.ArrayList;
import java.util.HashMap;
import cn.refactor.lib.colordialog.PromptDialog;

public class FragmentStock extends Fragment implements View.OnClickListener, AdapterStock.OnItemClickListener {

    Spinner spinner_item;
    Toolbar toolbar;
    EditText edt_qty, edt_price, edt_search;
    Button btn_add_item;
    TextView tvCategoryHeader;
    String selected_name="", selected_id="", stockId="";
    Cursor cursor_ord;
    DatabaseHandler handler;
    SQLiteDatabase db;
    ArrayList<HashMap<String, String>> arrayList;
    RecyclerView rc;
    ArrayList<ModelStock> arrayList_stock;
    CustomAdapter myAdapter;
    String operation = "add";
    ImageView img_cart, img_scan, img_clear;
    TextView noti_count;
    AdapterStock adapter;
    RelativeLayout rl_loader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new DatabaseHandler(getActivity());
        db = handler.getWritableDatabase();
        toolbar = getActivity().findViewById(R.id.toolbar);
        arrayList = new ArrayList<>();
        arrayList_stock = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.frag_stock,container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        setViews(view);
        return view;
    }

    private void setViews(View view){
        rc = view.findViewById(R.id.rc);
        rc.addItemDecoration(new DividerItemDecoration(rc.getContext(),
                DividerItemDecoration.VERTICAL));
        rc.setLayoutManager(new LinearLayoutManager(getActivity()));
        spinner_item = view.findViewById(R.id.spinner_item);
        edt_qty = view.findViewById(R.id.edt_qty);
        img_clear = view.findViewById(R.id.img_clear);
        edt_search = view.findViewById(R.id.edt_search);
        edt_price = view.findViewById(R.id.edt_price);
        rl_loader = view.findViewById(R.id.rl_loader);
        btn_add_item = view.findViewById(R.id.btn_add_item);
        tvCategoryHeader = toolbar.findViewById(R.id.tvCategoryHeader);
        tvCategoryHeader.setText("Stock");
        img_cart = toolbar.findViewById(R.id.img_cart);
        img_scan = toolbar.findViewById(R.id.img_scan);
        noti_count = toolbar.findViewById(R.id.noti_count);
        img_cart.setVisibility(View.GONE);
        img_scan.setVisibility(View.GONE);
        noti_count.setVisibility(View.GONE);
        setFilter();
        setListeners();
        fetchItemTable();
    }

    private void setFilter(){
        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setListeners(){
        btn_add_item.setOnClickListener(this);
        img_clear.setOnClickListener(this);
        fetchStocks("fetch");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_add_item:
                validation();
                break;

            case R.id.img_clear:
                edt_search.setText("");
                break;
        }
    }


    private void validation(){
        if(selected_name.equals("Select Item")){
            Toast.makeText(getActivity(),"Select Item" , Toast.LENGTH_SHORT).show();
        }else if(edt_qty.getText().toString().isEmpty() || edt_qty.getText().length()<1){
            edt_qty.setError("Enter total numbers of this Item");
            edt_qty.requestFocus();
        }else if(edt_price.getText().toString().isEmpty() || edt_price.getText().length()<1){
            edt_price.setError("Enter price of this Item");
            edt_price.requestFocus();
        }else if(Integer.parseInt(edt_qty.getText().toString())==0){
            edt_qty.setError("Zero is not allowed");
            edt_qty.requestFocus();
        }else if(Float.parseFloat(edt_price.getText().toString())==0.0){
            edt_price.setError("Zero is not allowed");
            edt_price.requestFocus();
        }else{
            String qty =edt_qty.getText().toString();
            String pri =edt_price.getText().toString();
            String bcod =selected_name+"-"+pri;

            Log.e("TAG", "--"+operation);

            if(operation.equals("add")) {
                insertIntoStock(qty, pri, bcod, selected_id, selected_name);
            }else{
                if(fetchSelectedStocks(selected_id)!=null){
                   ModelStock model = fetchSelectedStocks(selected_id);
                   int initial_qty = model.getStockQuantity();
                   Log.e("TAG" ,"initial_qty "+initial_qty);
                   Log.e("TAG" ,"qty "+qty);
                   int new_qty = Integer.parseInt(qty);
                   new_qty = new_qty + initial_qty;
                   Log.e("TAG" ,"selected_id "+selected_id);
                   Log.e("TAG" ,"initial_qtyy "+String.valueOf(new_qty));
                   Log.e("TAG" ,"pri "+String.valueOf(pri));

                   updateItems(stockId, String.valueOf(new_qty), pri);
                }

            }

        }
    }

    private void insertIntoStock(String qty, String pri, String bcode , String item_id, String item_name){
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.StockQuantity,qty);
        values.put(DatabaseHandler.StockPrice,pri);
        values.put(DatabaseHandler.Barcode,bcode);
        values.put(DatabaseHandler.ItemId,item_id);
        values.put(DatabaseHandler.ItemName,item_name);
        db.insert(DatabaseHandler.StockTable,null,values);
        Log.e("TAG" , "--data inserted in Item");
        new PromptDialog(getActivity())
                .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                .setAnimationEnable(true)
                .setTitleText("Success")
                .setContentText("Stock Added Successfully")
                .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                    @Override
                    public void onClick(PromptDialog dialog) {
                        dialog.dismiss();
                        edt_qty.setText("");
                        edt_price.setText("");
                        spinner_item.setSelection(0);
                    }
                }).show();
        fetchStocks("refresh");

    }

    private void fetchItemTable(){
        String query_doc = "SELECT * FROM ItemTable";//
        cursor_ord = db.rawQuery(query_doc,null);
        cursor_ord.moveToFirst();
        if(cursor_ord.getCount()>0) {
            for (int i = 0; i < cursor_ord.getCount(); i++) {
                HashMap<String, String> map = new HashMap<>();
                map.put("ItemId", cursor_ord.getString(0));
                map.put("ItemName", cursor_ord.getString(1));
                arrayList.add(map);
                cursor_ord.moveToNext();
            }
           /* HashMap<String, String> map = new HashMap<>();
            map.put("ItemId", "0");
            map.put("ItemName", "Select Item");
            arrayList.add(map);*/
        }else{
            //tv_nodata.setVisibility(View.VISIBLE);
        }
        if(arrayList.size()>0){
            populateSpinner();
        }else{
            Toast.makeText(getActivity(), "Please Add Items First" , Toast.LENGTH_SHORT).show();
        }
    }

    private void populateSpinner(){
        //Collections.reverse(arrayList);
        myAdapter = new CustomAdapter(getActivity(), R.layout.row_spinner_item, arrayList);
        myAdapter.setDropDownViewResource(R.layout.row_spinner_item);
        spinner_item.setAdapter(myAdapter);
        spinner_item.setSelection(0);
        spinner_item.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                selected_name = arrayList.get(position).get("ItemName").trim();
                selected_id = arrayList.get(position).get("ItemId");

                if(fetchSelectedStocks(selected_id)!=null){
                    ModelStock model = fetchSelectedStocks(selected_id);
                    edt_qty.setText("");
                    stockId = model.getStockId();
                    edt_price.setText(model.getStockPrice()+"");
                    edt_price.setFocusable(false);
                    operation = "update";
                    Log.e("TAG", "update");
                }else{
                    edt_qty.setText("");
                    edt_price.setText("");
                    edt_price.setFocusableInTouchMode(true);
                    edt_price.setFocusable(true);
                    operation = "add";
                    Log.e("TAG", "add");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    public class CustomAdapter extends ArrayAdapter<HashMap<String, String>>  {
        LayoutInflater flater;
        ArrayList<HashMap<String, String>> list;
        Context c;
        public CustomAdapter(Context context, int resourceId,
                                  ArrayList<HashMap<String, String>> objects) {
            super(context, resourceId, objects);
            this.list = objects;
            this.c = context;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return rowview(convertView,position);
        }
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return rowview(convertView,position);
        }

        @Override
        public int getCount() {
            // don't display last item. It is used as hint.
            int count = super.getCount();
            return count > 0 ? count  : count;
        }

        private View rowview(View convertView , int position){
            HashMap<String, String> rowItem = getItem(position);
            viewHolder holder ;
            View rowview = convertView;
            if (rowview==null) {
                holder = new viewHolder();
                flater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowview = flater.inflate(R.layout.row_spinner_item, null, false);
                holder.txtTitle = rowview.findViewById(R.id.tv_item);
                rowview.setTag(holder);
            }else{
                holder = (viewHolder) rowview.getTag();
            }
            holder.txtTitle.setText(rowItem.get("ItemName"));

            return rowview;
        }
        private class viewHolder{
            TextView txtTitle;
        }


    }

    private void fetchStocks(String message){
        arrayList_stock.clear();
        String query_doc = "SELECT * FROM StockTable ";
        Cursor cursor = db.rawQuery(query_doc,null);
        cursor.moveToFirst();
        if(cursor.getCount()>0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                ModelStock model = new ModelStock();
                model.setStockId(cursor.getString(0));
                model.setStockQuantity(cursor.getInt(1));
                model.setStockPrice(cursor.getInt(2));
                model.setBarcode(cursor.getString(3));
                model.setItemId(cursor.getString(4));
                model.setItemName(cursor.getString(5));
                arrayList_stock.add(model);
                cursor.moveToNext();
            }
        }
        if(cursor.getCount()>0) {
            Log.e("TAG" , "populating again");
            populateRc();
        }else{
            if(message.equalsIgnoreCase("fetch")) {
                Toast.makeText(getActivity(), "No Stock Found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void populateRc(){
        adapter = new AdapterStock(getActivity(), arrayList_stock,this);
        rc.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, final ModelStock item) {
        switch (view.getId()){
            case R.id.img_delete:
                new PromptDialog(getActivity())
                        .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                        .setAnimationEnable(true)
                        .setTitleText("Delete Stock")
                        .setContentText("Are you sure you want to delete this Stock?")
                        .setPositiveListener("Yes", new PromptDialog.OnPositiveListener() {
                            @Override
                            public void onClick(PromptDialog dialog) {
                                dialog.dismiss();
                                deleteItem(item.getStockId());
                            }
                        }).show();
                break;

            case R.id.img_edit:
                alertEditStock(item);
                break;
        }
    }

    private void deleteItem(String id){
        String query_doc = "Delete from StockTable where StockId = " + id ;
        Cursor cursor = db.rawQuery(query_doc,null);
        cursor.moveToFirst();
        fetchStocks("refresh");
        Toast.makeText(getActivity(), "Stock Deleted" , Toast.LENGTH_SHORT).show();
        spinner_item.setSelection(0);
        edt_qty.setText("");
        edt_price.setText("");
    }

    private void alertEditStock(final ModelStock item){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alert_edit_stock, null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);

        final TextView tv_name = dialogView.findViewById(R.id.tv_name);
        tv_name.setText("Edit "+item.getItemName());
        final EditText edt_qty = dialogView.findViewById(R.id.edt_qty);
        edt_qty.setText(item.getStockQuantity()+"");
        edt_qty.setSelection(String.valueOf(item.getStockQuantity()).length());
        final EditText edt_price = dialogView.findViewById(R.id.edt_price);
        edt_price.setText(item.getStockPrice()+"");
        edt_price.setSelection(String.valueOf(item.getStockPrice()).length());
        Button btn_update = dialogView.findViewById(R.id.btn_update);
        Button btn_close = dialogView.findViewById(R.id.btn_close);
        final AlertDialog alertDialog = dialogBuilder.create();
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_loader.setVisibility(View.VISIBLE);
                Log.e("TAG" , "cl");
                alertDialog.dismiss();

                updateItems(item.getStockId(), edt_qty.getText().toString(),edt_price.getText().toString() );
            }
        });
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void updateItems(String id, String qty, String pri){
        //update in stock table
        ContentValues values2 = new ContentValues();
        values2.put(DatabaseHandler.StockQuantity,qty);
        values2.put(DatabaseHandler.StockPrice,pri);

        Log.e("TAG" , "--"+id+"--"+qty+"--"+pri);

        db.update(DatabaseHandler.StockTable,values2, DatabaseHandler.StockId+" = "+id, null);

        fetchStocks("refresh");

        rl_loader.setVisibility(View.GONE);

        PromptDialog ppd = new PromptDialog(getActivity());
                ppd.setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                .setAnimationEnable(true)
                .setTitleText("Success")
                .setContentText("Stock Updated Successfully")
                .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                    @Override
                    public void onClick(PromptDialog dialog) {
                        dialog.dismiss();
                        edt_qty.setText("");
                        spinner_item.setSelection(0);
                        edt_search.setText("");
                    }
                });
                ppd.setCanceledOnTouchOutside(false);
                ppd.show();
    }


    private ModelStock fetchSelectedStocks(String id){
        ModelStock model = new ModelStock();
        String query_doc = "SELECT * FROM StockTable where ItemId = " + id ;
        Cursor cursor = db.rawQuery(query_doc,null);
        cursor.moveToFirst();
        if(cursor.getCount()>0) {
            model.setStockId(cursor.getString(0));
            model.setStockQuantity(cursor.getInt(1));
            model.setStockPrice(cursor.getInt(2));
            model.setItemId(cursor.getString(3));
            model.setItemName(cursor.getString(4));
            cursor.moveToNext();
        }
        if(cursor.getCount()>0) {
            return model;
        }else{
            return null;
        }
    }


}
