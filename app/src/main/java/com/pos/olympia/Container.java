package com.pos.olympia;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import com.pos.olympia.barcode.FragmentPrintBarcode;
import com.pos.olympia.db.DatabaseHandler;
import com.pos.olympia.history.FragmentHistory;
import com.pos.olympia.history.FragmentHistoryNew;
import com.pos.olympia.home.FragmentHome;
import com.pos.olympia.items.FragmentItems;
import com.pos.olympia.model.ModelStaff;
import com.pos.olympia.settings.FragmentSettings;
import com.pos.olympia.staff.FragmentStaff;
import com.pos.olympia.stock.FragmentStock;

public class Container extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    int fragment_count=0;
    Fragment newFragment;
    FragmentTransaction transaction ;
    GlobalClass global;
    DatabaseHandler handler;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        global = (GlobalClass)getApplicationContext();
        handler = new DatabaseHandler(this);
        db = handler.getWritableDatabase();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        transaction = getSupportFragmentManager().beginTransaction();
        fragment_count = 0;
        newFragment =new FragmentHome();
        transaction.replace(R.id.content_frame, newFragment);
        //transaction.addToBackStack(null);
        transaction.commit();


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View v = navigationView.getHeaderView(0);

        TextView tv_name = v.findViewById(R.id.tv_name);
        TextView tv_email = v.findViewById(R.id.tv_email);
        TextView tv_cashier = v.findViewById(R.id.tv_cashier);

        fetchAdmin(tv_name, tv_email);
        tv_cashier.setText("Cashier Logged In :\n"+global.getGlobal_staff().getStaffName());

        Menu nav_menu = navigationView.getMenu();
        if(!global.getGlobal_staff().getStaffType().equalsIgnoreCase("admin")){
            nav_menu.findItem(R.id.nav_item).setVisible(false);
            nav_menu.findItem(R.id.nav_stock).setVisible(false);
            nav_menu.findItem(R.id.nav_staff).setVisible(false);
            nav_menu.findItem(R.id.nav_barcode).setVisible(false);
            nav_menu.findItem(R.id.nav_settings).setVisible(false);
        }

        MenuItem register = nav_menu.findItem(R.id.nav_barcode);
        register.setVisible(false);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
           // super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        transaction = getSupportFragmentManager().beginTransaction();
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            if(fragment_count!=0) {
                fragment_count = 0;
                clearBackStack();
                newFragment = new FragmentHome();
                transaction.replace(R.id.content_frame, newFragment);
                transaction.commit();
            }
        } else if (id == R.id.nav_item) {
            if(fragment_count!=1) {
                fragment_count = 1;
                clearBackStack();
                newFragment = new FragmentItems();
                transaction.replace(R.id.content_frame, newFragment);
                transaction.commit();
            }
        } else if (id == R.id.nav_stock) {
            if(fragment_count!=2) {
                fragment_count = 2;
                clearBackStack();
                newFragment = new FragmentStock();
                transaction.replace(R.id.content_frame, newFragment);
                transaction.commit();
            }
        }else if (id == R.id.nav_staff) {
            if(fragment_count!=3) {
                fragment_count = 3;
                clearBackStack();
                newFragment = new FragmentStaff();
                transaction.replace(R.id.content_frame, newFragment);
                transaction.commit();
            }
        } else if (id == R.id.nav_history) {
            if(fragment_count!=4) {
                fragment_count = 4;
                clearBackStack();
                newFragment = new FragmentHistory();
                //newFragment = new FragmentHistoryNew();
                transaction.replace(R.id.content_frame, newFragment);
                transaction.commit();
            }
        } else if (id == R.id.nav_barcode) {
            if(fragment_count!=5) {
                fragment_count = 5;
                clearBackStack();
                newFragment = new FragmentPrintBarcode();
                transaction.replace(R.id.content_frame, newFragment);
                transaction.commit();
            }
        } else if (id == R.id.nav_settings) {
            if(fragment_count!=6) {
                fragment_count = 6;
                clearBackStack();
                newFragment = new FragmentSettings();
                transaction.replace(R.id.content_frame, newFragment);
                transaction.commit();
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void clearBackStack() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void fetchAdmin(TextView tv_name, TextView tv_email){
        ModelStaff model = new ModelStaff();
        String query_doc = "SELECT * FROM StaffTable where StaffType = '" + "Admin" + "'";
        Cursor cursor = db.rawQuery(query_doc,null);
        cursor.moveToFirst();
        if(cursor.getCount()>0) {
            model.setStaffId(cursor.getString(0));
            model.setStaffName(cursor.getString(1));
            model.setStaffPin(cursor.getString(2));
            model.setStaffType(cursor.getString(3));
            model.setStaffPhone(cursor.getString(4));
            model.setStaffEmail(cursor.getString(5));
            cursor.moveToNext();
        }
        if(cursor.getCount()>0) {
            tv_name.setText(model.getStaffName());
            tv_email.setText(model.getStaffEmail());
        }else{

        }
    }

}
