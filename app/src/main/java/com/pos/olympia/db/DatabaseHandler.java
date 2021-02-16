package com.pos.olympia.db;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Nazia Hassan on 26-08-2018.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private Context context;
    public static final String DATABASE_NAME = "OlympiaPOS";
    public static final int DATABASE_VERSION = 1;
    public static final String HistoryTable = "HistoryTable"; // HistoryTable
    public static final String HistoryId = "HistoryId";
    public static final String OrderId = "OrderId";
    public static final String CancelOrder = "CancelOrder";
    public static final String User = "User";
    public static final String Price = "Price";
    public static final String Discount = "Discount";
    public static final String Quantity = "Quantity";
    public static final String TotalPrice = "TotalPrice";
    public static final String AmountPayable = "AmountPayable";
    public static final String DateTime = "DateTime";
    public static final String CreatedTime = "CreatedTime";
    public static final String OrderTable = "OrderTable";
    public static final String OrdId = "OrdId";
    public static final String TotalItem = "TotalItem";
    public static final String UniqueKey = "UniqueKey";
    public static final String ItemTable = "ItemTable";
    public static final String ItemId = "ItemId";
    public static final String ItemName = "ItemName";
    public static final String StockTable = "StockTable";
    public static final String StockId = "StockId";
    public static final String StockQuantity = "StockQuantity";
    public static final String StockPrice = "StockPrice";
    public static final String Barcode = "Barcode";
    public static final String StaffTable = "StaffTable";
    public static final String StaffId = "StaffId";
    public static final String StaffName = "StaffName";
    public static final String StaffPin = "StaffPin";
    public static final String StaffType = "StaffType";
    public static final String StaffPhone = "StaffPhone";
    public static final String StaffEmail = "StaffEmail";


    // table history
    private static final String CREATE_HistoryTable = "CREATE TABLE " + HistoryTable +
            " (" + HistoryId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + UniqueKey + " TEXT NOT NULL,"
            + OrderId + " TEXT NOT NULL,"
            + ItemId + " TEXT NOT NULL,"
            + ItemName + " TEXT NOT NULL,"
            + Price + " TEXT,"
            + Quantity + " TEXT,"
            + DateTime + " TEXT,"
            + CancelOrder + " TEXT,"
            + CreatedTime + " DATETIME DEFAULT (datetime('now','localtime')) NOT NULL )";

    private static final String DELETE_HistoryTable = "DROP TABLE IF EXISTS "+HistoryTable;


    // table order
    private static final String CREATE_OrderTable = "CREATE TABLE " + OrderTable +
            " (" + OrdId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + UniqueKey + " TEXT NOT NULL,"
            + OrderId + " TEXT NOT NULL,"
            + TotalPrice + " TEXT,"
            + AmountPayable + " TEXT,"
            + TotalItem + " TEXT,"
            + Discount + " TEXT,"
            + User + " TEXT,"
            + DateTime + " TEXT,"
            + CancelOrder + " TEXT,"
            + CreatedTime + " DATETIME DEFAULT (datetime('now','localtime')) NOT NULL )";

    private static final String DELETE_OrderTable = "DROP TABLE IF EXISTS "+OrderTable;


    // table item
    private static final String CREATE_ItemTable = "CREATE TABLE " + ItemTable +
            " (" + ItemId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ItemName + " TEXT NOT NULL,"
            + CreatedTime + " DATETIME DEFAULT (datetime('now','localtime')) NOT NULL )";

    private static final String DELETE_ItemTable = "DROP TABLE IF EXISTS "+ItemTable;


    // table stock
    private static final String CREATE_StockTable = "CREATE TABLE " + StockTable +
            " (" + StockId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + StockQuantity + " TEXT NOT NULL,"
            + StockPrice + " TEXT NOT NULL,"
            + Barcode + " TEXT NOT NULL,"
            + ItemId + " TEXT NOT NULL,"
            + ItemName + " TEXT NOT NULL,"
            + CreatedTime + " DATETIME DEFAULT (datetime('now','localtime')) NOT NULL )";

    private static final String DELETE_StockTable = "DROP TABLE IF EXISTS "+StockTable;


    // table staff
    private static final String CREATE_StaffTable = "CREATE TABLE " + StaffTable +
            " (" + StaffId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + StaffName + " TEXT NOT NULL,"
            + StaffPin + " TEXT NOT NULL,"
            + StaffType + " TEXT NOT NULL,"
            + StaffPhone + " TEXT,"
            + StaffEmail + " TEXT,"
            + CreatedTime + " DATETIME DEFAULT (datetime('now','localtime')) NOT NULL )";

    private static final String DELETE_StaffTable = "DROP TABLE IF EXISTS "+StaffTable;



    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL(CREATE_HistoryTable);
            db.execSQL(CREATE_OrderTable);
            db.execSQL(CREATE_ItemTable);
            db.execSQL(CREATE_StockTable);
            db.execSQL(CREATE_StaffTable);
            Log.e("DATABASE", "TABLE CREATED "+DATABASE_VERSION);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            Log.e("table was not CREATED",""+e.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(DELETE_HistoryTable);
            db.execSQL(DELETE_OrderTable);
            db.execSQL(DELETE_ItemTable);
            db.execSQL(DELETE_StockTable);
            db.execSQL(DELETE_StaffTable);
            onCreate(db);
            Log.e("DATABASE","UPGRADED "+DATABASE_VERSION);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            Log.e("table was not upgraded",""+e.toString());
        }
    }

    //It is a method to View Database.
    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);
        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);
            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });
            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {
                alc.set(0,c);
                c.moveToFirst();
                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.e("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.e("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }


    }
}
