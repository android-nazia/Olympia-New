package com.pos.olympia;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mukesh.OtpView;
import com.pos.olympia.db.DatabaseHandler;
import com.pos.olympia.model.ModelStaff;

import cn.refactor.lib.colordialog.PromptDialog;

/**
 * Created by ANDROID on 12/15/2017.
 */

public class PinVerification extends AppCompatActivity implements View.OnClickListener{
    OtpView otp_view;
    ImageButton img_go;
    DatabaseHandler handler;
    SQLiteDatabase db;
    GlobalClass global;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pin_verification);
        handler = new DatabaseHandler(this);
        db = handler.getWritableDatabase();
        global = (GlobalClass)getApplicationContext();
        setViews();
    }

    private void setViews(){
        otp_view = findViewById(R.id.otp_view);
        img_go = findViewById(R.id.img_go);
        setListener();
    }

    private void setListener(){
        img_go.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_go:
                if(otp_view.getText().toString().isEmpty()){
                    Toast.makeText(PinVerification.this, "Enter Pin", Toast.LENGTH_SHORT).show();
                }else{
                    checkStaffExistence(otp_view.getText().toString());
                }
                break;
        }
    }


    private void checkStaffExistence(String pin){
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
            global.setGlobal_staff(model);
            startActivity(new Intent(PinVerification.this, Container.class));
            finish();
        }else{
            Toast.makeText(PinVerification.this, "Staff not found" , Toast.LENGTH_SHORT).show();
            otp_view.setText("");
        }
    }


}
