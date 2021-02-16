package com.pos.olympia.staff;

import android.app.Activity;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.pos.olympia.R;
import com.pos.olympia.db.DatabaseHandler;
import com.pos.olympia.model.ModelStaff;
import java.util.ArrayList;
import java.util.HashMap;
import cn.refactor.lib.colordialog.PromptDialog;

public class FragmentStaff extends Fragment implements View.OnClickListener, AdapterStaffs.OnItemClickListener {
    EditText edt_name, edt_pin, edt_mobile;
    Spinner spinner_item;
    Button btn_add_item;
    String cashier, admin;
    DatabaseHandler handler;
    SQLiteDatabase db;
    ArrayList<HashMap<String, String>> arrayList;
    Toolbar toolbar;
    TextView tvCategoryHeader;
    ImageView img_cart, img_scan;
    TextView noti_count;
    RecyclerView rc;
    ArrayList<ModelStaff> arrayList_staff;
    AdapterStaffs adapter;
    StaffAdapter staffAdapter;
    AdminAdapter adminAdapter;
    String cashier_arr[], admin_arr[];

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new DatabaseHandler(getActivity());
        db = handler.getWritableDatabase();
        toolbar = getActivity().findViewById(R.id.toolbar);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.fragment_staff,container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        setViews(view);
        return view;
    }



    private void setViews(View view){
        arrayList = new ArrayList<>();
        arrayList_staff = new ArrayList<>();
        spinner_item = view.findViewById(R.id.spinner_item);
        edt_name = view.findViewById(R.id.edt_name);
        edt_pin = view.findViewById(R.id.edt_pin);
        btn_add_item = view.findViewById(R.id.btn_add_item);
        edt_mobile = view.findViewById(R.id.edt_mobile);
        tvCategoryHeader = toolbar.findViewById(R.id.tvCategoryHeader);
        tvCategoryHeader.setText("Staffs");
        img_cart = toolbar.findViewById(R.id.img_cart);
        img_scan = toolbar.findViewById(R.id.img_scan);
        noti_count = toolbar.findViewById(R.id.noti_count);
        img_cart.setVisibility(View.GONE);
        img_scan.setVisibility(View.GONE);
        noti_count.setVisibility(View.GONE);
        rc = view.findViewById(R.id.rc);
        rc.addItemDecoration(new DividerItemDecoration(rc.getContext(),
                DividerItemDecoration.VERTICAL));
        rc.setLayoutManager(new LinearLayoutManager(getActivity()));
        setListeners();
        populateStaffSpinner();

    }

    private void setListeners(){
        btn_add_item.setOnClickListener(this);

        fetchStaffs("fetch");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_add_item:
                validation();
                break;
        }
    }

    private void validation(){
        if(edt_name.getText().length()<1){
            edt_name.setError("Enter Name");
            edt_name.requestFocus();
        }else if(edt_pin.getText().length()!=4){
            edt_pin.setError("Create 4-digits Pin");
            edt_pin.requestFocus();
        }else if(cashier.length()<1){
            Toast.makeText(getActivity(),"Select Type" , Toast.LENGTH_SHORT).show();
        }else if(edt_mobile.getText().length()>0 && edt_mobile.getText().length()!=10){
            edt_mobile.setError("Enter 10-digits mobile number");
            edt_mobile.requestFocus();
        }else{
            String name =edt_name.getText().toString();
            String pin =edt_pin.getText().toString();
            String mob =edt_mobile.getText().toString();
            checkStaffExistence(name, pin, mob);
        }
    }

    private void populateStaffSpinner(){
        cashier_arr = new String[]{"Cashier"};
        staffAdapter = new StaffAdapter(getActivity(), R.layout.row_spinner_item, cashier_arr);
        staffAdapter.setDropDownViewResource(R.layout.row_spinner_item);
        spinner_item.setAdapter(staffAdapter);
        spinner_item.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                cashier = cashier_arr[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    public class StaffAdapter extends ArrayAdapter<String> {
        LayoutInflater flater;
        String[] list;
        Context c;
        public StaffAdapter(Context context, int resourceId,String[] objects) {
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
        private View rowview(View convertView , int position){
            String rowItem = getItem(position);
            StaffAdapter.viewHolder holder ;
            View rowview = convertView;
            if (rowview==null) {
                holder = new StaffAdapter.viewHolder();
                flater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowview = flater.inflate(R.layout.row_spinner_item, null, false);
                holder.txtTitle = rowview.findViewById(R.id.tv_item);
                rowview.setTag(holder);
            }else{
                holder = (StaffAdapter.viewHolder) rowview.getTag();
            }
            holder.txtTitle.setText(rowItem);
            return rowview;
        }
        private class viewHolder{
            TextView txtTitle;
        }
    }

    public class AdminAdapter extends ArrayAdapter<String> {
        LayoutInflater flater;
        String[] list;
        Context c;
        public AdminAdapter(Context context, int resourceId,String[] objects) {
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
        private View rowview(View convertView , int position){
            String rowItem = getItem(position);
            AdminAdapter.viewHolder holder ;
            View rowview = convertView;
            if (rowview==null) {
                holder = new AdminAdapter.viewHolder();
                flater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowview = flater.inflate(R.layout.row_spinner_item, null, false);
                holder.txtTitle = rowview.findViewById(R.id.tv_item);
                rowview.setTag(holder);
            }else{
                holder = (AdminAdapter.viewHolder) rowview.getTag();
            }
            holder.txtTitle.setText(rowItem);
            return rowview;
        }
        private class viewHolder{
            TextView txtTitle;
        }
    }


    private void checkStaffExistence(String name, String pin, String mob){
        String query_doc = "SELECT * FROM StaffTable where StaffName = '" + name + "'";
        Cursor cursor_staff = db.rawQuery(query_doc,null);
        cursor_staff.moveToFirst();
        if(cursor_staff.getCount()>0) {
            Toast.makeText(getActivity(), "This name already exist." , Toast.LENGTH_SHORT).show();
        }else{
            query_doc = "SELECT * FROM StaffTable where StaffPin = '" + pin + "'";
            cursor_staff = db.rawQuery(query_doc,null);
            cursor_staff.moveToFirst();
            if(cursor_staff.getCount()>0) {
                Toast.makeText(getActivity(), "This pin already exist." , Toast.LENGTH_SHORT).show();
            }else{
                insertIntoStaff(name, pin,mob, cashier);
            }
        }
    }

    private void insertIntoStaff(String name, String pin, String mobile, String staff){
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.StaffName,name);
        values.put(DatabaseHandler.StaffPin,pin);
        values.put(DatabaseHandler.StaffType,staff);
        values.put(DatabaseHandler.StaffPhone,mobile);
        db.insert(DatabaseHandler.StaffTable,null,values);
        Log.e("TAG" , "--data inserted in Staff");
        new PromptDialog(getActivity())
                .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                .setAnimationEnable(true)
                .setTitleText("Success")
                .setContentText("Staff Added Successfully")
                .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                    @Override
                    public void onClick(PromptDialog dialog) {
                        dialog.dismiss();
                        edt_name.setText("");
                        edt_pin.setText("");
                        edt_mobile.setText("");
                    }
                }).show();
        fetchStaffs("refresh");
    }

    private void fetchStaffs(String message){
        arrayList_staff.clear();
        String query_doc = "SELECT * FROM StaffTable ";
        Cursor cursor = db.rawQuery(query_doc,null);
        cursor.moveToFirst();
        if(cursor.getCount()>0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                ModelStaff model = new ModelStaff();
                model.setStaffId(cursor.getString(0));
                model.setStaffName(cursor.getString(1));
                model.setStaffPin(cursor.getString(2));
                model.setStaffType(cursor.getString(3));
                model.setStaffPhone(cursor.getString(4));
                model.setStaffEmail(cursor.getString(5));
                arrayList_staff.add(model);
                cursor.moveToNext();
            }
        }
        if(cursor.getCount()>0) {
            populateRc();
        }else{
            if(message.equalsIgnoreCase("fetch")) {
                Toast.makeText(getActivity(), "No Staffs Found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void populateRc(){
        adapter = new AdapterStaffs(getActivity(), arrayList_staff,this);
        rc.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, final ModelStaff item) {
        switch (view.getId()){
            case R.id.img_delete:
                new PromptDialog(getActivity())
                        .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                        .setAnimationEnable(true)
                        .setTitleText("Delete Staff")
                        .setContentText("Are you sure you want to delete this Staff?")
                        .setPositiveListener("Yes", new PromptDialog.OnPositiveListener() {
                            @Override
                            public void onClick(PromptDialog dialog) {
                                dialog.dismiss();
                                deleteItem(item.getStaffId());
                            }
                        }).show();
                break;

            case R.id.img_edit:
                if(item.getStaffType().equalsIgnoreCase("admin")){
                    alertEditAdmin(item);
                }else{
                    alertEditStaff(item);
                }
                break;
        }
    }

    private void deleteItem(String id){
        String query_doc = "Delete from StaffTable where StaffId = " + id ;
        Cursor cursor = db.rawQuery(query_doc,null);
        cursor.moveToFirst();
        fetchStaffs("refresh");
        Toast.makeText(getActivity(), "Staff Deleted" , Toast.LENGTH_SHORT).show();
        edt_name.setText("");
        edt_pin.setText("");
        edt_mobile.setText("");
    }

    private void alertEditStaff(final ModelStaff item){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alert_edit_staff, null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);
        final EditText edt_name = dialogView.findViewById(R.id.edt_name);
        edt_name.setText(item.getStaffName());
        edt_name.setSelection(item.getStaffName().length());
        final EditText edt_pin = dialogView.findViewById(R.id.edt_pin);
        edt_pin.setText(item.getStaffPin());
        edt_pin.setSelection(item.getStaffPin().length());
        final EditText edt_mobile = dialogView.findViewById(R.id.edt_mobile);
        edt_mobile.setText(item.getStaffPhone());
        edt_mobile.setSelection(item.getStaffPhone().length());
        Button btn_update = dialogView.findViewById(R.id.btn_update);
        Spinner spinner = dialogView.findViewById(R.id.spinner_item);
        spinner.setAdapter(staffAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                cashier = cashier_arr[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        Button btn_close = dialogView.findViewById(R.id.btn_close);
        final AlertDialog alertDialog = dialogBuilder.create();
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                updateItems(item.getStaffId(), edt_name.getText().toString(),edt_pin.getText().toString() ,
                        cashier,edt_mobile.getText().toString(), "");
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


    private void alertEditAdmin(final ModelStaff item){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alert_edit_admin, null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);
        final EditText edt_name = dialogView.findViewById(R.id.edt_name);
        edt_name.setText(item.getStaffName());
        edt_name.setSelection(item.getStaffName().length());
        final EditText edt_pin = dialogView.findViewById(R.id.edt_pin);
        edt_pin.setText(item.getStaffPin());
        edt_pin.setSelection(item.getStaffPin().length());
        final EditText edt_mobile = dialogView.findViewById(R.id.edt_mobile);
        edt_mobile.setText(item.getStaffPhone());
        edt_mobile.setSelection(item.getStaffPhone().length());
        final EditText edt_email = dialogView.findViewById(R.id.edt_email);
        edt_email.setText(item.getStaffEmail());
        edt_email.setSelection(item.getStaffEmail().length());
        Button btn_update = dialogView.findViewById(R.id.btn_update);
        Spinner spinner = dialogView.findViewById(R.id.spinner_item);
        admin_arr = new String[]{"Admin"};
        adminAdapter = new AdminAdapter(getActivity(), R.layout.row_spinner_item, admin_arr);
        adminAdapter.setDropDownViewResource(R.layout.row_spinner_item);
        spinner.setAdapter(adminAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                admin = admin_arr[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        Button btn_close = dialogView.findViewById(R.id.btn_close);
        final AlertDialog alertDialog = dialogBuilder.create();
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                updateItems(item.getStaffId(), edt_name.getText().toString(),edt_pin.getText().toString() ,
                        admin,edt_mobile.getText().toString() , edt_email.getText().toString());
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


    private void updateItems(String id, String name , String pin , String type , String mobile,String email){
        //update in item table
        ContentValues values1 = new ContentValues();
        values1.put(DatabaseHandler.StaffName,name);
        values1.put(DatabaseHandler.StaffPin,pin);
        values1.put(DatabaseHandler.StaffType,type);
        values1.put(DatabaseHandler.StaffPhone,mobile);
        values1.put(DatabaseHandler.StaffEmail,email);
        db.update(DatabaseHandler.StaffTable,values1, DatabaseHandler.StaffId+" = "+id, null);

        fetchStaffs("refresh");
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        Toast.makeText(getActivity(), "Staff Updated" , Toast.LENGTH_SHORT).show();
    }



}
