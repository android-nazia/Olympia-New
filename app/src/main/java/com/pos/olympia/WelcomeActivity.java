package com.pos.olympia;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import com.pos.olympia.db.DatabaseHandler;
import com.pos.olympia.model.ModelItem;

public class WelcomeActivity extends AppCompatActivity {

    TextView tv_loading_data;
    DatabaseHandler handler;
    SQLiteDatabase db;
    SharedPreference pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        handler = new DatabaseHandler(this);
        db = handler.getWritableDatabase();
        pref = new SharedPreference();
        pref.setSharedPrefInt(this, pref.PREFS_WELCOME , 1);

        setViews ();
        insertIntoStaff("Shakil Ahmed", "9686", "9836306644", "Admin",
                "olympiareadymade@gmail.com");
    }

    private void setViews(){
        tv_loading_data = findViewById(R.id.tv_loading_data);
        setListeners();
    }


    private void setListeners(){

    }

    private void insertIntoStaff(String name, String pin, String mobile, String staff, String email){
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.StaffName,name);
        values.put(DatabaseHandler.StaffPin,pin);
        values.put(DatabaseHandler.StaffType,staff);
        values.put(DatabaseHandler.StaffPhone,mobile);
        values.put(DatabaseHandler.StaffEmail,email);
        db.insert(DatabaseHandler.StaffTable,null,values);
        Log.e("TAG" , "--data inserted in Staff");
        insertIntoItem("Shirt blue");
    }

    private void insertIntoItem(String itemName){
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.ItemName,itemName);
        db.insert(DatabaseHandler.ItemTable,null,values);
        Log.e("TAG" , "--data inserted in Item");
        fetchItems();
    }


    private void fetchItems(){
        ModelItem model = new ModelItem();
        String query_doc = "SELECT * FROM ItemTable ";
        Cursor cursor = db.rawQuery(query_doc,null);
        cursor.moveToFirst();
        if(cursor.getCount()>0) {
            model.setItemId(cursor.getString(0));
            model.setItemName(cursor.getString(1));
            cursor.moveToNext();
        }
        if(cursor.getCount()>0) {
            insertIntoStock("50", "300",model.getItemName()+"-300", model.getItemId(),
                    model.getItemName());
        }else{
            Log.e("TAG" , "--no Item");
        }
    }

    private void insertIntoStock(String qty, String pri,String bcode, String item_id, String item_name){
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.StockQuantity,qty);
        values.put(DatabaseHandler.StockPrice,pri);
        values.put(DatabaseHandler.Barcode,bcode);
        values.put(DatabaseHandler.ItemId,item_id);
        values.put(DatabaseHandler.ItemName,item_name);
        db.insert(DatabaseHandler.StockTable,null,values);
        Log.e("TAG" , "--data inserted in Stock");

        Intent i = new Intent(this , PinVerification.class);
        startActivity(i);
        finish();

    }

}
