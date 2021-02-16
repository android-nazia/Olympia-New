package com.pos.olympia.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.pos.olympia.Container;
import com.pos.olympia.DeviceListActivity;
import com.pos.olympia.GlobalClass;
import com.pos.olympia.UnicodeFormatter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnectivity implements Runnable{

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothDevice mBluetoothDevice;
    private ProgressDialog mBluetoothConnectProgressDialog;
    private BluetoothSocket mBluetoothSocket;
    private UUID applicationUUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Context context;
    GlobalClass global;

    public BluetoothConnectivity(Context contex,
                                 BluetoothAdapter mBluetoothAdapter) {
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.context = contex;
        global = (GlobalClass)context.getApplicationContext();

    }

    public void connectPrinter(){
        if (mBluetoothAdapter == null) {
            Toast.makeText(context, "Message1", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity)context).startActivityForResult(enableBtIntent,
                        REQUEST_ENABLE_BT);
            } else {
                ListPairedDevices();
                Intent connectIntent = new Intent(context,
                        DeviceListActivity.class);
                ((Activity)context).startActivityForResult(connectIntent,
                        REQUEST_CONNECT_DEVICE);
            }
        }
    }

    public void onActivityResult(int mRequestCode, int mResultCode,
                                 Intent mDataIntent) {
        onActivityResult(mRequestCode, mResultCode, mDataIntent);
        switch (mRequestCode) {
            case REQUEST_CONNECT_DEVICE:
                Log.e("TAG","TAG6");
                if (mResultCode == Activity.RESULT_OK) {
                    Bundle mExtra = mDataIntent.getExtras();
                    String mDeviceAddress = mExtra.getString("DeviceAddress");
                    global.setDeviceAddress(mDeviceAddress);
                    Log.e("TAG", "Coming incoming address " + mDeviceAddress);
                    mBluetoothDevice = mBluetoothAdapter
                            .getRemoteDevice(global.getDeviceAddress());
                    mBluetoothConnectProgressDialog = ProgressDialog.show(context,
                            "Connecting...", mBluetoothDevice.getName() + " : "
                                    + mBluetoothDevice.getAddress(), true, false);
                    Thread mBlutoothConnectThread = new Thread(this);
                    mBlutoothConnectThread.start();
                    // pairToDevice(mBluetoothDevice); This method is replaced by
                    // progress dialog with thread
                }
                break;

            case REQUEST_ENABLE_BT:
                Log.e("TAG","TAG7");
                if (mResultCode == Activity.RESULT_OK) {
                    ListPairedDevices();
                    Intent connectIntent = new Intent(context,
                            DeviceListActivity.class);
                    ((Activity)context).startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE);
                } else {
                    Toast.makeText(context, "Message", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void ListPairedDevices() {
        Log.e("TAG","TAG8");
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter
                .getBondedDevices();
        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice mDevice : mPairedDevices) {
                Log.e("TAG", "PairedDevices: " + mDevice.getName() + "  "
                        + mDevice.getAddress());
            }
        }
    }

    public void run() {
        try {
            Log.e("TAG","TAG9");
            mBluetoothSocket = mBluetoothDevice
                    .createRfcommSocketToServiceRecord(applicationUUID);
            global.setSockey(mBluetoothSocket);
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();
            mHandler.sendEmptyMessage(0);
        } catch (IOException eConnectException) {
            Log.e("TAG", "CouldNotConnectToSocket", eConnectException);
            closeSocket(mBluetoothSocket);
            return;
        }
    }

    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            Log.e("TAG","TAG10");
            nOpenSocket.close();
            Log.e("TAG", "SocketClosed");
        } catch (IOException ex) {
            Log.e("TAG", "CouldNotCloseSocket");
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e("TAG","TAG11");
            mBluetoothConnectProgressDialog.dismiss();
            Toast.makeText(context, "Device Connected", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(context, Container.class);
            context.startActivity(i);

        }
    };

    public static byte intToByteArray(int value) {
        byte[] b = ByteBuffer.allocate(4).putInt(value).array();

        for (int k = 0; k < b.length; k++) {
            System.out.println("Selva  [" + k + "] = " + "0x"
                    + UnicodeFormatter.byteToHex(b[k]));
        }

        return b[3];
    }

}
