package com.pos.olympia.home;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.olympia.GlobalClass;
import com.pos.olympia.R;
import com.pos.olympia.cart.CartActivity;
import com.pos.olympia.db.DatabaseHandler;
import com.pos.olympia.flytocart.CircleAnimationUtil;
import com.pos.olympia.model.ModelStock;

import java.util.ArrayList;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class FragmentHome extends Fragment implements View.OnClickListener {

    ImageView img_cart, img_scan, img_clear;
    TextView noti_count, tvCategoryHeader;
    Toolbar toolbar;
    RecyclerView rc;
    Button btn_scan, btn_cart;
    ArrayList<ModelStock> arrayList_stock, arrayList_cart, arrayList;
    Cursor cursor_stock, cursor_stock_scan;
    DatabaseHandler handler;
    SQLiteDatabase db;
    GlobalClass global;
    EditText edt_search;
    AdapterHome adapter;
    public final int CUSTOMIZED_REQUEST_CODE = 0x0000ffff;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arrayList_stock = new ArrayList<>();
        arrayList_cart = new ArrayList<>();
        arrayList = new ArrayList<>();
        toolbar = getActivity().findViewById(R.id.toolbar);
        handler = new DatabaseHandler(getActivity());
        db = handler.getWritableDatabase();
        global = (GlobalClass)getActivity().getApplicationContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate( R.layout.frag_home,container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        setViews(view);
        return view;
    }

    private void setViews(View view){
        edt_search = view.findViewById(R.id.edt_search);
        img_cart = toolbar.findViewById(R.id.img_cart);
        img_cart.setVisibility(View.VISIBLE);
        img_scan = toolbar.findViewById(R.id.img_scan);
        img_scan.setVisibility(View.VISIBLE);
        img_clear = view.findViewById(R.id.img_clear);
        noti_count = toolbar.findViewById(R.id.noti_count);
        noti_count.setVisibility(View.VISIBLE);
        tvCategoryHeader = toolbar.findViewById(R.id.tvCategoryHeader);
        tvCategoryHeader.setText("Home");
        rc = view.findViewById(R.id.rc);
        rc.addItemDecoration(new DividerItemDecoration(rc.getContext(),
                DividerItemDecoration.VERTICAL));
        rc.setLayoutManager(new LinearLayoutManager(getActivity()));
        btn_scan = view.findViewById(R.id.btn_scan);
        btn_cart = view.findViewById(R.id.btn_cart);

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
        btn_scan.setOnClickListener(this);
        img_scan.setOnClickListener(this);
        btn_cart.setOnClickListener(this);
        img_cart.setOnClickListener(this);
        img_clear.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.img_scan:
            case R.id.btn_scan:
                Log.e("TAG", "clicked");
                scanBarCode();
                break;

            case R.id.btn_cart:
            case R.id.img_cart:
                gotoCart();
                break;

            case R.id.img_clear:
                edt_search.setText("");
                break;
        }

    }

    private void fetchStockTable(){
        arrayList_stock.clear();
        String query_doc = "SELECT * FROM StockTable where StockQuantity != '" + 0 + "'";
        cursor_stock = db.rawQuery(query_doc,null);
        cursor_stock.moveToFirst();
        if(cursor_stock.getCount()>0) {
            for (int i = 0; i < cursor_stock.getCount(); i++) {
                ModelStock model = new ModelStock();
                model.setStockId(cursor_stock.getString(0));
                model.setStockQuantity(cursor_stock.getInt(1));
                model.setStockPrice(cursor_stock.getInt(2));
                model.setBarcode(cursor_stock.getString(3));
                model.setItemId(cursor_stock.getString(4));
                model.setItemName(cursor_stock.getString(5));
                model.setCreatedTime(cursor_stock.getString(6));
                arrayList_stock.add(model);
                cursor_stock.moveToNext();
            }
        }else{
            //tv_nodata.setVisibility(View.VISIBLE);
        }
        if(arrayList_stock.size()>0){
            populateRC();
        }else{
            Toast.makeText(getActivity(), "Please Add Stocks First" , Toast.LENGTH_SHORT).show();
        }
    }

    private void populateRC(){

        adapter = new AdapterHome(getActivity(), img_cart, arrayList_stock);
        rc.setAdapter(adapter);

        adapter.setActionListener(new AdapterHome.ProductItemActionListener(){
            @Override
            public void onItemTap(ImageView imageView, int position) {
                if (imageView != null){
                    makeFlyAnimation(imageView, position);
                    //addToTicket( all_item_model.get(position));
                }
            }
        });

    }

    private void makeFlyAnimation(ImageView targetView, final int pos) {
        new CircleAnimationUtil().attachActivity(getActivity()).setTargetView(targetView).setMoveDuration(500).setDestView(img_cart).setAnimationListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //btn_charge.setEnabled(true);
                //btn_charge.setTextColor(c.getResources().getColor(android.R.color.white));
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                addItemToCart(pos);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        }).startAnimation();
    }

    private void addItemToCart(int position){
        ModelStock model = new ModelStock();
        model.setStockId(arrayList_stock.get(position).getStockId());
        model.setStockQuantity(1);
        model.setStockPrice(arrayList_stock.get(position).getStockPrice());
        model.setBarcode(arrayList_stock.get(position).getBarcode());
        model.setItemId(arrayList_stock.get(position).getItemId());
        model.setItemName(arrayList_stock.get(position).getItemName());

        //if item is already present in the cart, its quantity gets updated.

        if(arrayList_cart.size()>0){
            String match ="no";
            for(int i=0;i<arrayList_cart.size();i++){
                String item_id = arrayList_stock.get(position).getItemId();
                Log.e("TAG" , "check "+arrayList_cart.get(i).getItemId()+"|"+item_id);
                if(arrayList_cart.get(i).getItemId().equals(item_id)){
                    //update
                    Log.e("TAG" , "update");
                    int qt = arrayList_cart.get(i).getStockQuantity();
                    qt++;
                    model.setStockQuantity(qt);
                    arrayList_cart.set(i, model);
                    match="yes";
                    break;
                }
            }
            if(match.equals("no")){
                //add
                Log.e("TAG" , "add");
                arrayList_cart.add(model);
            }
        }else{
            arrayList_cart.add(model);
        }
        Toast.makeText(getActivity(), "Item Added", Toast.LENGTH_SHORT).show();
        global.setGlobal_arr_cart(arrayList_cart);
        countItems();
    }

    private void countItems(){
        int count = 0;
        ArrayList<ModelStock> arr = global.getGlobal_arr_cart();
        for(int i = 0 ; i<arr.size() ; i++) {
            count = count + arr.get(i).getStockQuantity();
        }
        noti_count.setText(count + "");
    }

    private void gotoCart(){
        if(global.getGlobal_arr_cart()!=null && global.getGlobal_arr_cart().size()>0) {
            Intent i = new Intent(getActivity(), CartActivity.class);
            startActivity(i);
        }else{
            Toast.makeText(getActivity(), "Add Item to the cart", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchStockTable();
        if (global.getGlobal_arr_cart()!=null && global.getGlobal_arr_cart().size() > 0) {
            countItems();
        }else{
            noti_count.setText("0");
        }
    }


@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CUSTOMIZED_REQUEST_CODE && requestCode != IntentIntegrator.REQUEST_CODE) {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        switch (requestCode) {
            case CUSTOMIZED_REQUEST_CODE: {
                Toast.makeText(getActivity(), "REQUEST_CODE = " + requestCode, Toast.LENGTH_LONG).show();
                break;
            }
            default:
                break;
        }

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);

        if(result.getContents() == null) {
            Log.e("TAG", "Cancelled scan");
            //Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
        } else {
            Log.e("TAG", "Scanned");

            Toast.makeText(getActivity(), "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

            readBarCode(result.getContents());
        }
    }


    private void readBarCode(String item){
        Log.e("TAG" , "scan "+item);
        fetchStockWithBarcode(item);

        /*String[] separated = item.split("-");
        String name="",pri="";

        if(separated[0]!=null){
            name = separated[0];
        }
        if(separated.length>1) {
            if (separated[1] != null) {
                pri = separated[1];
            }
        }*/



    }


     private void fetchStockWithBarcode(String code){
        String query_doc = "SELECT * FROM StockTable where Barcode = '" + code + "'";
        cursor_stock_scan = db.rawQuery(query_doc,null);
        cursor_stock_scan.moveToFirst();
        if(cursor_stock_scan.getCount()>0) {
            ModelStock model = new ModelStock();
            model.setStockId(cursor_stock_scan.getString(0));
            model.setStockQuantity(1);
            model.setStockPrice(cursor_stock_scan.getInt(2));
            model.setBarcode(cursor_stock_scan.getString(3));
            model.setItemId(cursor_stock_scan.getString(4));
            model.setItemName(cursor_stock_scan.getString(5));
            cursor_stock_scan.moveToNext();
            addItemToCartFromScanning(model);
        }else{
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
            builder1.setTitle("Item not found");
            builder1.setMessage("This item has not been added in the stock.");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
     }


    private void addItemToCartFromScanning(ModelStock model){
        /*model.setStockId(model.getStockId());
        model.setStockQuantity(1);
        model.setStockPrice(model.getStockPrice());
        model.setBarcode(model.getBarcode());
        model.setItemId(model.getItemId());
        model.setItemName(model.getItemName());*/

        //if item is already present in the cart, its quantity gets updated.

        if(arrayList_cart.size()>0){
            String match ="no";
            for(int i=0;i<arrayList_cart.size();i++){
                String item_id = model.getItemId();
                Log.e("TAG" , "check "+arrayList_cart.get(i).getItemId()+"|"+item_id);
                if(arrayList_cart.get(i).getItemId().equals(item_id)){
                    //update
                    Log.e("TAG" , "update");
                    int qt = arrayList_cart.get(i).getStockQuantity();
                    qt++;
                    model.setStockQuantity(qt);
                    arrayList_cart.set(i, model);
                    match="yes";
                    break;
                }
            }
            if(match.equals("no")){
                //add
                Log.e("TAG" , "add");
                arrayList_cart.add(model);
            }
        }else{
            arrayList_cart.add(model);
        }
        Toast.makeText(getActivity(), "Item Added", Toast.LENGTH_SHORT).show();
        global.setGlobal_arr_cart(arrayList_cart);
        countItems();

        scanBarCode();
    }

    private void scanBarCode(){
        IntentIntegrator.forSupportFragment(this).initiateScan();
    }



}
