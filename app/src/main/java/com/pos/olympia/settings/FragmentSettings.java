package com.pos.olympia.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;
import com.pos.olympia.GlobalClass;
import com.pos.olympia.R;
import com.pos.olympia.SharedPreference;
import com.pos.olympia.cart.CartActivity;
import com.pos.olympia.db.AndroidDatabaseManager;
import com.pos.olympia.db.DatabaseHandler;
import com.pos.olympia.model.ModelStaff;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import cn.refactor.lib.colordialog.PromptDialog;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import static com.pos.olympia.db.DatabaseHandler.ItemTable;
import static com.pos.olympia.db.DatabaseHandler.StaffTable;
import static com.pos.olympia.db.DatabaseHandler.StockTable;

public class FragmentSettings extends Fragment implements View.OnClickListener , OnOtpCompletionListener{
    TextView tvCategoryHeader;
    OtpView otpview;
    Toolbar toolbar;
    Button btn_delete_history, btn_db, btn_email_history;
    Cursor cursor_history, cursor_stock, cursor_order, cursor_item, cursor_staff;
    DatabaseHandler handler;
    SQLiteDatabase db;
    android.widget.ImageView img_cart, img_scan;
    TextView noti_count;
    SharedPreference pref;
    ProgressBar pb;
    RadioButton rb_small, rb_big;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new DatabaseHandler(getActivity());
        db = handler.getWritableDatabase();
        pref = new SharedPreference();
        toolbar = getActivity().findViewById(R.id.toolbar);

    }


    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate( R.layout.frag_settings,container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        alertSecurityCode();

        setViews(view);
        return view;
    }

    private void setViews(View view){
        tvCategoryHeader = toolbar.findViewById(R.id.tvCategoryHeader);
        tvCategoryHeader.setText("Settings");
        img_cart = toolbar.findViewById(R.id.img_cart);
        img_scan = toolbar.findViewById(R.id.img_scan);
        noti_count = toolbar.findViewById(R.id.noti_count);

        img_cart.setVisibility(View.GONE);
        img_scan.setVisibility(View.GONE);
        noti_count.setVisibility(View.GONE);
        btn_delete_history = view.findViewById(R.id.btn_delete_history);
        btn_db = view.findViewById(R.id.btn_db);
        btn_email_history= view.findViewById(R.id.btn_email_history);
        pb = view.findViewById(R.id.pb);
        pb.setVisibility(View.GONE);
        rb_small = view.findViewById(R.id.rb_small);
        rb_big = view.findViewById(R.id.rb_big);

        String billSize = pref.getSharedPrefString(getActivity(), pref.PREFS_bill);

        if(billSize==null || billSize.isEmpty() || billSize.equalsIgnoreCase("small")) {
            rb_small.setChecked(true);
            rb_big.setChecked(false);
        }else{
            rb_small.setChecked(false);
            rb_big.setChecked(true);        }

        setListeners();
    }


    private void setListeners(){
        btn_delete_history.setOnClickListener(this);
        btn_db.setOnClickListener(this);
        btn_email_history.setOnClickListener(this);
        rb_small.setOnClickListener(this);
        rb_big.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.btn_delete_history:
                new PromptDialog(getActivity())
                        .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                        .setAnimationEnable(true)
                        .setTitleText("Delete Database")
                        .setContentText("Are you sure you want to delete all the data of your App ?")
                        .setPositiveListener("Delete", new PromptDialog.OnPositiveListener() {
                            @Override
                            public void onClick(PromptDialog dialog) {
                                dialog.dismiss();
                                try {
                                    //db.execSQL("delete from "+HistoryTable);
                                    //db.execSQL("delete from "+OrderTable);
                                    db.execSQL("delete from "+ItemTable);
                                    db.execSQL("delete from "+StockTable);
                                    db.execSQL("delete from "+StaffTable);
                                    pref.clearSharedPreference(getActivity());
                                } catch (SQLException e) {
                                    // TODO Auto-generated catch block
                                    Log.e("table was not upgraded",""+e.toString());
                                }

                                Toast.makeText(getActivity(), "Deleted" , Toast.LENGTH_SHORT).show();
                                getActivity().onBackPressed();
                            }
                        }).show();
                break;

            case R.id.btn_db:
                Intent dbmanager = new Intent(getActivity(),AndroidDatabaseManager.class);
                startActivity(dbmanager);
                break;

            case R.id.btn_email_history:
                AlertDialog.Builder builderr = new AlertDialog.Builder(getActivity());
                builderr.setMessage("Sales History, Items and Stocks data will be sent and saved to your email ?");
                builderr.setCancelable(true);
                builderr.setPositiveButton(
                        "Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                createExcelForHistory();
                            }
                        });
                builderr.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert1 = builderr.create();
                alert1.show();
                break;


            case R.id.rb_small:
                rb_small.setChecked(true);
                rb_big.setChecked(false);
                pref.setSharedPrefString(getActivity(), pref.PREFS_bill, "small");
                break;

            case R.id.rb_big:
                rb_small.setChecked(false);
                rb_big.setChecked(true);
                pref.setSharedPrefString(getActivity(), pref.PREFS_bill, "big");
                break;

        }
    }

    private void alertSecurityCode(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alert_security_code, null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);
        otpview = dialogView.findViewById(R.id.otp_view);
        ImageButton img_go = dialogView.findViewById(R.id.img_go);
        final AlertDialog alertDialog = dialogBuilder.create();
        img_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //alertDialog.dismiss();
                if(otpview.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(), "Enter Pin", Toast.LENGTH_SHORT).show();
                }else{
                    checkStaffExistence(otpview.getText().toString(), alertDialog);
                }
            }
        });

        alertDialog.show();
    }

    private void checkStaffExistence(String pin, AlertDialog dialogBuilder){
        String query_doc = "SELECT * FROM StaffTable where StaffPin = '" + pin + "'";
        Cursor cursor_staff = db.rawQuery(query_doc,null);
        cursor_staff.moveToFirst();
        if(cursor_staff.getCount()>0) {
            ModelStaff model = new ModelStaff();
            model.setStaffId(cursor_staff.getString(0));
            model.setStaffName(cursor_staff.getString(1));
            model.setStaffPin(cursor_staff.getString(2));
            model.setStaffType(cursor_staff.getString(3));
            model.setStaffPhone(cursor_staff.getString(4));
            model.setStaffEmail(cursor_staff.getString(5));
            cursor_staff.moveToNext();

            if(model.getStaffType().equalsIgnoreCase("admin")){
                dialogBuilder.dismiss();
            }else{
                Toast.makeText(getActivity(), "Wrong Pin" , Toast.LENGTH_SHORT).show();
                otpview.setText("");
            }

        }else{
            Toast.makeText(getActivity(), "Admin not found" , Toast.LENGTH_SHORT).show();
            otpview.setText("");
        }
    }

    @Override
    public void onOtpCompleted(String s) {
        Log.e("onOtpCompleted=>", otpview.getText().toString());
        Log.e("onOtpCompleted=>s", s);
    }

    private void createExcelForHistory(){
        final Cursor cursor = historyTable();

        File sd = Environment.getExternalStorageDirectory();
        String csvFile = "OlympiaSales.xls";

        File directory = new File(sd.getAbsolutePath());
        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        try {

            //file path
            File file = new File(directory, csvFile);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook;
            workbook = Workbook.createWorkbook(file, wbSettings);
            //Excel sheet name. 0 represents first sheet
            WritableSheet sheet = workbook.createSheet("OlympiaSales", 0);
            // column and row
            sheet.addCell(new Label(0, 0, "OrderId"));
            sheet.addCell(new Label(1, 0, "ItemName"));
            sheet.addCell(new Label(2, 0, "Price"));
            sheet.addCell(new Label(3, 0, "Quantity"));
            sheet.addCell(new Label(4, 0, "CreatedTime"));

            if (cursor.moveToFirst()) {
                do {
                    String OrderId = cursor.getString(cursor.getColumnIndex("OrderId"));
                    String Item = cursor.getString(cursor.getColumnIndex("ItemName"));
                    String Price = cursor.getString(cursor.getColumnIndex("Price"));
                    String Quantity = cursor.getString(cursor.getColumnIndex("Quantity"));
                    String CreatedTime = cursor.getString(cursor.getColumnIndex("CreatedTime"));

                    int i = cursor.getPosition() + 1;
                    sheet.addCell(new Label(0, i, OrderId));
                    sheet.addCell(new Label(1, i, Item));
                    sheet.addCell(new Label(2, i, Price));
                    sheet.addCell(new Label(3, i, Quantity));
                    sheet.addCell(new Label(4, i, CreatedTime));
                } while (cursor.moveToNext());
            }

            //closing cursor
            cursor.close();
            workbook.write();
            workbook.close();


            //sendMail(file);
            createExcelForStock(file);
            //Toast.makeText(getActivity(), "Saved and Emailed", Toast.LENGTH_SHORT).show();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private Cursor historyTable(){
        ArrayList<HashMap<String, String>> arrayList_history = new ArrayList<>();
        String query_doc = "SELECT * FROM HistoryTable ORDER BY CreatedTime DESC";
        cursor_history = db.rawQuery(query_doc,null);
        cursor_history.moveToFirst();


        if(cursor_history.getCount()>0) {

            for (int i = 0; i < cursor_history.getCount(); i++) {

                HashMap<String, String> map = new HashMap<>();
                map.put("HistoryId", cursor_history.getString(0));
                map.put("UniqueKey", cursor_history.getString(1));
                map.put("OrderId", cursor_history.getString(2));
                map.put("ItemId", cursor_history.getString(3));
                map.put("ItemName", cursor_history.getString(4));
                map.put("Price", cursor_history.getString(5));
                map.put("Quantity", cursor_history.getString(6));
                map.put("DateTime", cursor_history.getString(7));
                map.put("CancelOrder", cursor_history.getString(8));
                map.put("CreatedTime", dateConversion(cursor_history.getString(9)));

                arrayList_history.add(map);

                cursor_history.moveToNext();
            }

        }else{
            //tv_nodata.setVisibility(View.VISIBLE);
        }

        return cursor_history;
    }

    private void createExcelForStock(File f1){
        final Cursor cursor = stockTable();

        File sd = Environment.getExternalStorageDirectory();
        String csvFile = "OlympiaStock.xls";

        File directory = new File(sd.getAbsolutePath());
        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        try {

            //file path
            File file = new File(directory, csvFile);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook;
            workbook = Workbook.createWorkbook(file, wbSettings);
            //Excel sheet name. 0 represents first sheet
            WritableSheet sheet = workbook.createSheet("OlympiaStock", 0);
            // column and row
            sheet.addCell(new Label(0, 0, "StockId"));
            sheet.addCell(new Label(1, 0, "StockQuantity"));
            sheet.addCell(new Label(2, 0, "StockPrice"));
            sheet.addCell(new Label(3, 0, "Barcode"));
            sheet.addCell(new Label(4, 0, "ItemId"));
            sheet.addCell(new Label(5, 0, "ItemName"));
            sheet.addCell(new Label(6, 0, "CreatedTime"));

            if (cursor.moveToFirst()) {
                do {
                    String StockId = cursor.getString(cursor.getColumnIndex("StockId"));
                    String StockQuantity = cursor.getString(cursor.getColumnIndex("StockQuantity"));
                    String StockPrice = cursor.getString(cursor.getColumnIndex("StockPrice"));
                    String Barcode = cursor.getString(cursor.getColumnIndex("Barcode"));
                    String ItemId = cursor.getString(cursor.getColumnIndex("ItemId"));
                    String ItemName = cursor.getString(cursor.getColumnIndex("ItemName"));
                    String CreatedTime = cursor.getString(cursor.getColumnIndex("CreatedTime"));

                    int i = cursor.getPosition() + 1;
                    sheet.addCell(new Label(0, i, StockId));
                    sheet.addCell(new Label(1, i, StockQuantity));
                    sheet.addCell(new Label(2, i, StockPrice));
                    sheet.addCell(new Label(3, i, Barcode));
                    sheet.addCell(new Label(4, i, ItemId));
                    sheet.addCell(new Label(5, i, ItemName));
                    sheet.addCell(new Label(6, i, CreatedTime));
                } while (cursor.moveToNext());
            }

            //closing cursor
            cursor.close();
            workbook.write();
            workbook.close();


            createExcelForOrders(f1, file);

            Toast.makeText(getActivity(),
                    "Saved and Emailed", Toast.LENGTH_SHORT).show();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private Cursor stockTable(){
        ArrayList<HashMap<String, String>> arrayList_stock = new ArrayList<>();
        String query_doc = "SELECT * FROM StockTable ORDER BY CreatedTime DESC";
        cursor_stock = db.rawQuery(query_doc,null);
        cursor_stock.moveToFirst();
        if(cursor_stock.getCount()>0) {
            for (int i = 0; i < cursor_stock.getCount(); i++) {
                HashMap<String, String> map = new HashMap<>();
                map.put("StockId", cursor_stock.getString(0));
                map.put("StockQuantity", cursor_stock.getString(1));
                map.put("StockPrice", cursor_stock.getString(2));
                map.put("Barcode", cursor_stock.getString(3));
                map.put("ItemId", cursor_stock.getString(4));
                map.put("ItemName", cursor_stock.getString(5));
                map.put("CreatedTime", dateConversion(cursor_stock.getString(6)));
                arrayList_stock.add(map);
                cursor_stock.moveToNext();
            }
        }else{
            //tv_nodata.setVisibility(View.VISIBLE);
        }
        return cursor_stock;
    }


    private void createExcelForOrders(File f1, File f2){
        final Cursor cursor = orderTable();

        File sd = Environment.getExternalStorageDirectory();
        String csvFile = "OlympiaOrder.xls";

        File directory = new File(sd.getAbsolutePath());
        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        try {

            //file path
            File file = new File(directory, csvFile);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook;
            workbook = Workbook.createWorkbook(file, wbSettings);
            //Excel sheet name. 0 represents first sheet
            WritableSheet sheet = workbook.createSheet("OlympiaOrder", 0);
            // column and row
            sheet.addCell(new Label(0, 0, "OrdId"));
            sheet.addCell(new Label(1, 0, "UniqueKey"));
            sheet.addCell(new Label(2, 0, "OrderId"));
            sheet.addCell(new Label(3, 0, "TotalPrice"));
            sheet.addCell(new Label(4, 0, "AmountPayable"));
            sheet.addCell(new Label(5, 0, "TotalItem"));
            sheet.addCell(new Label(6, 0, "Discount"));
            sheet.addCell(new Label(7, 0, "User"));
            sheet.addCell(new Label(8, 0, "DateTime"));
            sheet.addCell(new Label(9, 0, "CancelOrder"));
            sheet.addCell(new Label(10, 0, "CreatedTime"));

            if (cursor.moveToFirst()) {
                do {
                    String OrdId = cursor.getString(cursor.getColumnIndex("OrdId"));
                    String UniqueKey = cursor.getString(cursor.getColumnIndex("UniqueKey"));
                    String OrderId = cursor.getString(cursor.getColumnIndex("OrderId"));
                    String TotalPrice = cursor.getString(cursor.getColumnIndex("TotalPrice"));
                    String AmountPayable = cursor.getString(cursor.getColumnIndex("AmountPayable"));
                    String TotalItem = cursor.getString(cursor.getColumnIndex("TotalItem"));
                    String Discount = cursor.getString(cursor.getColumnIndex("Discount"));
                    String User = cursor.getString(cursor.getColumnIndex("User"));
                    String DateTime = cursor.getString(cursor.getColumnIndex("DateTime"));
                    String CancelOrder = cursor.getString(cursor.getColumnIndex("CancelOrder"));
                    String CreatedTime = cursor.getString(cursor.getColumnIndex("CreatedTime"));

                    int i = cursor.getPosition() + 1;
                    sheet.addCell(new Label(0, i, OrdId));
                    sheet.addCell(new Label(1, i, UniqueKey));
                    sheet.addCell(new Label(2, i, OrderId));
                    sheet.addCell(new Label(3, i, TotalPrice));
                    sheet.addCell(new Label(4, i, AmountPayable));
                    sheet.addCell(new Label(5, i, TotalItem));
                    sheet.addCell(new Label(6, i, Discount));
                    sheet.addCell(new Label(7, i, User));
                    sheet.addCell(new Label(8, i, DateTime));
                    sheet.addCell(new Label(9, i, CancelOrder));
                    sheet.addCell(new Label(10, i, CreatedTime));
                } while (cursor.moveToNext());
            }

            //closing cursor
            cursor.close();
            workbook.write();
            workbook.close();
            createExcelForItems(f1,f2, file);
            Toast.makeText(getActivity(),
                    "Saved and Emailed", Toast.LENGTH_SHORT).show();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private Cursor orderTable(){
        ArrayList<HashMap<String, String>> arrayList_order = new ArrayList<>();
        String query_doc = "SELECT * FROM OrderTable ORDER BY CreatedTime DESC";
        cursor_order = db.rawQuery(query_doc,null);
        cursor_order.moveToFirst();
        if(cursor_order.getCount()>0) {
            for (int i = 0; i < cursor_order.getCount(); i++) {
                HashMap<String, String> map = new HashMap<>();
                map.put("OrdId", cursor_order.getString(0));
                map.put("UniqueKey", cursor_order.getString(1));
                map.put("OrderId", cursor_order.getString(2));
                map.put("TotalPrice", cursor_order.getString(3));
                map.put("AmountPayable", cursor_order.getString(4));
                map.put("TotalItem", cursor_order.getString(5));
                map.put("Discount", cursor_order.getString(6));
                map.put("User", cursor_order.getString(7));
                map.put("DateTime", cursor_order.getString(8));
                map.put("CancelOrder", cursor_order.getString(9));
                map.put("CreatedTime", dateConversion(cursor_order.getString(10)));
                arrayList_order.add(map);
                cursor_order.moveToNext();
            }
        }else{
            //tv_nodata.setVisibility(View.VISIBLE);
        }
        return cursor_order;
    }


    private void createExcelForItems(File f1, File f2, File f3){
        final Cursor cursor = itemTable();

        File sd = Environment.getExternalStorageDirectory();
        String csvFile = "OlympiaItem.xls";

        File directory = new File(sd.getAbsolutePath());
        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        try {

            //file path
            File file = new File(directory, csvFile);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook;
            workbook = Workbook.createWorkbook(file, wbSettings);
            //Excel sheet name. 0 represents first sheet
            WritableSheet sheet = workbook.createSheet("OlympiaItem", 0);
            // column and row
            sheet.addCell(new Label(0, 0, "ItemId"));
            sheet.addCell(new Label(1, 0, "ItemName"));
            sheet.addCell(new Label(2, 0, "CreatedTime"));

            if (cursor.moveToFirst()) {
                do {
                    String ItemId = cursor.getString(cursor.getColumnIndex("ItemId"));
                    String ItemName = cursor.getString(cursor.getColumnIndex("ItemName"));
                    String CreatedTime = cursor.getString(cursor.getColumnIndex("CreatedTime"));

                    int i = cursor.getPosition() + 1;
                    sheet.addCell(new Label(0, i, ItemId));
                    sheet.addCell(new Label(1, i, ItemName));
                    sheet.addCell(new Label(2, i, CreatedTime));
                } while (cursor.moveToNext());
            }

            //closing cursor
            cursor.close();
            workbook.write();
            workbook.close();
            createExcelForStaff(f1,f2, f3,file);
            Toast.makeText(getActivity(),
                    "Saved and Emailed", Toast.LENGTH_SHORT).show();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private Cursor itemTable(){
        ArrayList<HashMap<String, String>> arrayList_item = new ArrayList<>();
        String query_doc = "SELECT * FROM ItemTable ORDER BY CreatedTime DESC";
        cursor_item = db.rawQuery(query_doc,null);
        cursor_item.moveToFirst();
        if(cursor_item.getCount()>0) {
            for (int i = 0; i < cursor_item.getCount(); i++) {
                HashMap<String, String> map = new HashMap<>();
                map.put("ItemId", cursor_item.getString(0));
                map.put("ItemName", cursor_item.getString(1));
                map.put("CreatedTime", dateConversion(cursor_item.getString(2)));
                arrayList_item.add(map);
                cursor_item.moveToNext();
            }
        }else{
            //tv_nodata.setVisibility(View.VISIBLE);
        }
        return cursor_item;
    }

    private void createExcelForStaff(File f1, File f2, File f3, File f4){
        final Cursor cursor = staffTable();

        File sd = Environment.getExternalStorageDirectory();
        String csvFile = "OlympiaStaff.xls";

        File directory = new File(sd.getAbsolutePath());
        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        try {

            //file path
            File file = new File(directory, csvFile);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook;
            workbook = Workbook.createWorkbook(file, wbSettings);
            //Excel sheet name. 0 represents first sheet
            WritableSheet sheet = workbook.createSheet("OlympiaStaff", 0);
            // column and row
            sheet.addCell(new Label(0, 0, "StaffId"));
            sheet.addCell(new Label(1, 0, "StaffName"));
            sheet.addCell(new Label(2, 0, "StaffPin"));
            sheet.addCell(new Label(3, 0, "StaffType"));
            sheet.addCell(new Label(4, 0, "StaffPhone"));
            sheet.addCell(new Label(5, 0, "StaffEmail"));
            sheet.addCell(new Label(6, 0, "CreatedTime"));

            if (cursor.moveToFirst()) {
                do {
                    String StaffId = cursor.getString(cursor.getColumnIndex("StaffId"));
                    String StaffName = cursor.getString(cursor.getColumnIndex("StaffName"));
                    String StaffPin = cursor.getString(cursor.getColumnIndex("StaffPin"));
                    String StaffType = cursor.getString(cursor.getColumnIndex("StaffType"));
                    String StaffPhone = cursor.getString(cursor.getColumnIndex("StaffPhone"));
                    String StaffEmail = cursor.getString(cursor.getColumnIndex("StaffEmail"));
                    String CreatedTime = cursor.getString(cursor.getColumnIndex("CreatedTime"));

                    int i = cursor.getPosition() + 1;
                    sheet.addCell(new Label(0, i, StaffId));
                    sheet.addCell(new Label(1, i, StaffName));
                    sheet.addCell(new Label(2, i, StaffPin));
                    sheet.addCell(new Label(3, i, StaffType));
                    sheet.addCell(new Label(4, i, StaffPhone));
                    sheet.addCell(new Label(5, i, StaffEmail));
                    sheet.addCell(new Label(6, i, CreatedTime));
                } while (cursor.moveToNext());
            }

            //closing cursor
            cursor.close();
            workbook.write();
            workbook.close();
            sendMail(f1,f2, f3,f4,file);
            Toast.makeText(getActivity(),
                    "Saved and Emailed", Toast.LENGTH_SHORT).show();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private Cursor staffTable(){
        ArrayList<HashMap<String, String>> arrayList_staff = new ArrayList<>();
        String query_doc = "SELECT * FROM StaffTable ORDER BY CreatedTime DESC";
        cursor_staff = db.rawQuery(query_doc,null);
        cursor_staff.moveToFirst();
        if(cursor_staff.getCount()>0) {
            for (int i = 0; i < cursor_staff.getCount(); i++) {
                HashMap<String, String> map = new HashMap<>();
                map.put("StaffId", cursor_staff.getString(0));
                map.put("StaffName", cursor_staff.getString(1));
                map.put("StaffPin", cursor_staff.getString(2));
                map.put("StaffType", cursor_staff.getString(3));
                map.put("StaffPhone", cursor_staff.getString(4));
                map.put("StaffEmail", cursor_staff.getString(5));
                map.put("CreatedTime", dateConversion(cursor_staff.getString(6)));
                arrayList_staff.add(map);
                cursor_staff.moveToNext();
            }
        }else{
            //tv_nodata.setVisibility(View.VISIBLE);
        }
        return cursor_staff;
    }


    public void sendMail(File f1, File f2, File f3, File f4, File f5)  {
        //olympiareadymade@gmail.com
        Uri contentUri1 = Uri.fromFile(f1);
        Uri contentUri2 = Uri.fromFile(f2);
        Uri contentUri3 = Uri.fromFile(f3);
        Uri contentUri4 = Uri.fromFile(f4);
        Uri contentUri5 = Uri.fromFile(f5);
        Log.e("TAG" ,"path "+contentUri1.toString());
        Log.e("TAG" ,"path "+contentUri2.toString());
        Log.e("TAG" ,"path "+contentUri3.toString());
        Log.e("TAG" ,"path "+contentUri4.toString());
        Log.e("TAG" ,"path "+contentUri5.toString());
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        String[] strTo = {"olympiareadymade@gmail.com", "naziahassan66@gmail.com"};
        intent.putExtra(Intent.EXTRA_EMAIL, strTo);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Olympia App Record");
        intent.putExtra(Intent.EXTRA_TEXT, "Olympia Readymade Garments All Data Till"+ getTodaysDate());
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(contentUri1);
        uris.add(contentUri2);
        uris.add(contentUri3);
        uris.add(contentUri4);
        uris.add(contentUri5);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.setType("message/rfc822");
        intent.setPackage("com.google.android.gm");
        startActivity(intent);
    }

    private String getTodaysDate(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
        String formattedDate = df.format(c);
        return formattedDate;
    }


    public String dateConversion(String date){
        SimpleDateFormat spf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date newDate= null;
        try {
            newDate = spf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        spf= new SimpleDateFormat("EEE, MMM dd, yy", Locale.ENGLISH);
        date = spf.format(newDate);
        return date;
    }

}

