package com.pos.olympia;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

import com.pos.olympia.model.ModelStaff;
import com.pos.olympia.model.ModelStock;

import java.util.ArrayList;

public class GlobalClass extends Application {

    ArrayList<ModelStock> global_arr_cart;
    String deviceAddress;
    BluetoothSocket sockey;
    float discount = 10.0f;
    ModelStaff global_staff;




    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public BluetoothSocket getSockey() {
        return sockey;
    }

    public void setSockey(BluetoothSocket sockey) {
        this.sockey = sockey;
    }

    public float getDiscount() {
        return discount;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
    }

    public ArrayList<ModelStock> getGlobal_arr_cart() {
        return global_arr_cart;
    }

    public void setGlobal_arr_cart(ArrayList<ModelStock> global_arr_cart) {
        this.global_arr_cart = global_arr_cart;
    }

    public ModelStaff getGlobal_staff() {
        return global_staff;
    }

    public void setGlobal_staff(ModelStaff global_staff) {
        this.global_staff = global_staff;
    }


}
