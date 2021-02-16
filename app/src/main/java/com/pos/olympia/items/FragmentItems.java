package com.pos.olympia.items;

import android.app.Activity;
import android.content.ContentValues;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.pos.olympia.R;
import com.pos.olympia.db.DatabaseHandler;
import com.pos.olympia.model.ModelItem;
import java.util.ArrayList;
import cn.refactor.lib.colordialog.PromptDialog;

public class FragmentItems extends Fragment
        implements View.OnClickListener, AdapterItems.OnItemClickListener {

    ImageView img_cart, img_scan, img_clear;
    TextView noti_count, tvCategoryHeader;
    Toolbar toolbar;
    EditText edt_item_name;
    Button btn_add_item;
    DatabaseHandler handler;
    SQLiteDatabase db;
    RecyclerView rc;
    ArrayList<ModelItem> arrayList_item;
    AdapterItems adapter;
    EditText edt_search;
    RelativeLayout rl_loader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new DatabaseHandler(getActivity());
        db = handler.getWritableDatabase();
        toolbar = getActivity().findViewById(R.id.toolbar);
        arrayList_item = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.frag_item,container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        setViews(view);
        return view;
    }

    private void setViews(View view){
        rc = view.findViewById(R.id.rc);
        rc.addItemDecoration(new DividerItemDecoration(rc.getContext(),
                DividerItemDecoration.VERTICAL));
        rc.setLayoutManager(new LinearLayoutManager(getActivity()));
        img_cart = toolbar.findViewById(R.id.img_cart);
        img_cart.setVisibility(View.GONE);
        img_clear = view.findViewById(R.id.img_clear);
        edt_search = view.findViewById(R.id.edt_search);
        rl_loader = view.findViewById(R.id.rl_loader);
        img_scan = toolbar.findViewById(R.id.img_scan);
        img_scan.setVisibility(View.GONE);
        noti_count = toolbar.findViewById(R.id.noti_count);
        noti_count.setVisibility(View.GONE);
        tvCategoryHeader = toolbar.findViewById(R.id.tvCategoryHeader);
        tvCategoryHeader.setText("Items");
        edt_item_name = view.findViewById(R.id.edt_item_name);
        btn_add_item = view.findViewById(R.id.btn_add_item);
        setFilter();
        setListeners();
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
        fetchItems("fetch");

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_add_item:
                if(edt_item_name.getText().length()>0){
                    if(isNotExistingInItemTable(edt_item_name.getText().toString())){
                        insertIntoItem(edt_item_name.getText().toString());
                    }
                }else{
                    edt_item_name.setError("Enter Item Name");
                    edt_item_name.requestFocus();
                }
                break;

            case R.id.img_clear:
                edt_search.setText("");
                break;
        }
    }

    private void fetchItems(String message){
        arrayList_item.clear();
        String query_doc = "SELECT * FROM ItemTable ";
        Cursor cursor = db.rawQuery(query_doc,null);
        cursor.moveToFirst();
        if(cursor.getCount()>0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                ModelItem model = new ModelItem();
                model.setItemId(cursor.getString(0));
                model.setItemName(cursor.getString(1));
                arrayList_item.add(model);
                cursor.moveToNext();
            }
        }
        if(cursor.getCount()>0) {
            populateRc();
        }else{
            if(adapter!=null) {
                adapter.notifyDataSetChanged();
            }
            if(message.equalsIgnoreCase("fetch")) {
                Toast.makeText(getActivity(), "No Item Found", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void populateRc(){
        adapter = new AdapterItems(getActivity(), arrayList_item,this);
        rc.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, final ModelItem item) {
        switch (view.getId()){
            case R.id.img_delete:
                if(isNotExistingInStockTable(item.getItemId())){
                    new PromptDialog(getActivity())
                            .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                            .setAnimationEnable(true)
                            .setTitleText("Delete Item")
                            .setContentText("Are you sure you want to delete this Item?")
                            .setPositiveListener("Yes", new PromptDialog.OnPositiveListener() {
                                @Override
                                public void onClick(PromptDialog dialog) {
                                    dialog.dismiss();
                                    deleteItem(item.getItemId());
                                }
                            }).show();
                }
                break;

            case R.id.img_edit:
                alertEditName(item);
                break;
        }
    }

    private boolean isNotExistingInItemTable(String name){
        String query_doc = "SELECT * FROM ItemTable where ItemName = '" + name + "' Collate Nocase";
        Cursor cursor = db.rawQuery(query_doc,null);
        cursor.moveToFirst();
        if(cursor.getCount()>0) {
            //Log.e("TAG" , "size "+ cursor.getCount());
            Toast.makeText(getActivity(), "This item already exist." , Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }


    private void insertIntoItem(String itemName){
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.ItemName,itemName);
        db.insert(DatabaseHandler.ItemTable,null,values);
        Log.e("TAG" , "--data inserted in Item");

        new PromptDialog(getActivity())
                .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                .setAnimationEnable(true)
                .setTitleText("Success")
                .setContentText("Item Added Successfully")
                .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                    @Override
                    public void onClick(PromptDialog dialog) {
                        dialog.dismiss();
                        edt_item_name.setText("");
                    }
                }).show();

        fetchItems("refresh");
    }

    private void deleteItem(String itemId){
        String query_doc = "Delete from ItemTable where ItemId = " + itemId ;
        Cursor cursor = db.rawQuery(query_doc,null);
        cursor.moveToFirst();
        fetchItems("refresh");
        Toast.makeText(getActivity(), "Item Deleted" , Toast.LENGTH_SHORT).show();


    }


    private void alertEditName(final ModelItem item){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alert_edit_item, null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);
        final EditText edt_name = dialogView.findViewById(R.id.edt_name);
        edt_name.setText(item.getItemName());
        edt_name.setSelection(item.getItemName().length());
        Button btn_update = dialogView.findViewById(R.id.btn_update);
        Button btn_close = dialogView.findViewById(R.id.btn_close);
        final AlertDialog alertDialog = dialogBuilder.create();
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                if(isNotExistingInItemTable(edt_name.getText().toString())){
                    updateItems(item.getItemId(), edt_name.getText().toString());
                }
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

    private void updateItems(String id, String name){
        //update in item table
        ContentValues values1 = new ContentValues();
        values1.put(DatabaseHandler.ItemName,name);
        db.update(DatabaseHandler.ItemTable,values1, DatabaseHandler.ItemId+" = "+id, null);


        //update in stock table
        ContentValues values2 = new ContentValues();
        values2.put(DatabaseHandler.ItemName,name);
        db.update(DatabaseHandler.StockTable,values2, DatabaseHandler.ItemId+" = "+id, null);


        fetchItems("refresh");
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        Toast.makeText(getActivity(), "Item Updated" , Toast.LENGTH_SHORT).show();
    }

    private boolean isNotExistingInStockTable(String id){
        String query_doc = "SELECT * FROM StockTable where ItemId = '" + id + "' Collate Nocase";
        Cursor cursor = db.rawQuery(query_doc,null);
        cursor.moveToFirst();
        if(cursor.getCount()>0) {
            //Log.e("TAG" , "size "+ cursor.getCount());
            Toast.makeText(getActivity(), "Cannot delete this Item. This item already present in stock." , Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }



}

