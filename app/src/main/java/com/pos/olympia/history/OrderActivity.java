package com.pos.olympia.history;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.pos.olympia.R;

public class OrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        setViews();
    }

    private void setViews(){

        setListeners();
    }

    private void setListeners(){

    }




}
