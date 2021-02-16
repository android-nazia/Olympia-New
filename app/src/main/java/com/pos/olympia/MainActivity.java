package com.pos.olympia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.pos.olympia.db.DatabaseHandler;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.content.Context;
import android.Manifest;

public class MainActivity extends AppCompatActivity implements Runnable {
    protected static final String TAG = "TAG";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    Button mScan, mPrint, mDisc;
    BluetoothAdapter mBluetoothAdapter;
    private UUID applicationUUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog mBluetoothConnectProgressDialog;
    private BluetoothSocket mBluetoothSocket;
    BluetoothDevice mBluetoothDevice;
    GlobalClass global;
    SQLiteDatabase db;
    DatabaseHandler handler;
    SharedPreference pref;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
        Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new DatabaseHandler(this);
        db = handler.getReadableDatabase();
        pref = new SharedPreference();

        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }

        Log.e("TAG","TAG1");
        global = (GlobalClass)getApplicationContext();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mScan = findViewById(R.id.Scan);
        mScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View mView) {
                Log.e("TAG","TAG2");
                if (mBluetoothAdapter == null) {
                    Toast.makeText(MainActivity.this, "Message1", Toast.LENGTH_SHORT).show();
                } else {

                    if (mBluetoothAdapter == null) {
                        Toast.makeText(MainActivity.this, "Message1", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!mBluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(
                                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent,
                                    REQUEST_ENABLE_BT);
                        } else {
                            ListPairedDevices();
                            Intent connectIntent = new Intent(MainActivity.this,
                                    DeviceListActivity.class);
                            startActivityForResult(connectIntent,
                                    REQUEST_CONNECT_DEVICE);
                        }
                    }
                }

                /*if(pref.getSharedPrefInt(MainActivity.this , pref.PREFS_WELCOME)==1){
                    Intent i = new Intent(MainActivity.this, PinVerification.class);
                    startActivity(i);
                }else{
                    Intent i = new Intent(MainActivity.this, WelcomeActivity.class);
                    startActivity(i);
                }*/

            }
        });

        mPrint = findViewById(R.id.mPrint);
        mPrint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View mView) {
                Log.e("TAG","TAG3");
                //printBill();
            }
        });

        mDisc = findViewById(R.id.dis);
        mDisc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View mView) {
                Log.e("TAG","TAG4");
                if (mBluetoothAdapter != null)
                    mBluetoothAdapter.disable();
            }
        });

        if(!hasPermissions(MainActivity.this, PERMISSIONS)){
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
    }
    }

    public void onActivityResult(int mRequestCode, int mResultCode,
                                 Intent mDataIntent) {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent);

        switch (mRequestCode) {
            case REQUEST_CONNECT_DEVICE:
                Log.e("TAG","TAG6");
                if (mResultCode == Activity.RESULT_OK) {
                    Bundle mExtra = mDataIntent.getExtras();
                    String mDeviceAddress = mExtra.getString("DeviceAddress");
                    global.setDeviceAddress(mDeviceAddress);
                    Log.e(TAG, "Coming incoming address " + mDeviceAddress);
                    mBluetoothDevice = mBluetoothAdapter
                            .getRemoteDevice(global.getDeviceAddress());
                    mBluetoothConnectProgressDialog = ProgressDialog.show(this,
                            "Connecting...", mBluetoothDevice.getName() + " : "
                                    + mBluetoothDevice.getAddress(), true, false);
                    Thread mBlutoothConnectThread = new Thread(this);
                    mBlutoothConnectThread.start();

                }
                break;

            case REQUEST_ENABLE_BT:
                Log.e("TAG","TAG7");
                if (mResultCode == Activity.RESULT_OK) {
                    ListPairedDevices();
                    Intent connectIntent = new Intent(MainActivity.this,
                            DeviceListActivity.class);
                    startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE);
                } else {
                    Toast.makeText(MainActivity.this,
                            "You must switch ON your Bluetooth to use the App", Toast.LENGTH_LONG).show();
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
                Log.v(TAG, "PairedDevices: " + mDevice.getName() + "  "
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
            Log.d(TAG, "CouldNotConnectToSocket", eConnectException);
            closeSocket(mBluetoothSocket);
            return;
        }
    }

    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            Log.e("TAG","TAG10");
            nOpenSocket.close();
            Log.d(TAG, "SocketClosed");
        } catch (IOException ex) {
            Log.d(TAG, "CouldNotCloseSocket");
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e("TAG","TAG11");
            mBluetoothConnectProgressDialog.dismiss();
            Toast.makeText(MainActivity.this, "Device Connected", Toast.LENGTH_SHORT).show();
            /*Intent i = new Intent(MainActivity.this, Container.class);
            startActivity(i);*/
            if(pref.getSharedPrefInt(MainActivity.this , pref.PREFS_WELCOME)==1){
                Intent i = new Intent(MainActivity.this, PinVerification.class);
                startActivity(i);
                finish();
            }else{
                Intent i = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(i);
                finish();
            }

        }
    };

    /*public static byte intToByteArray(int value) {
        Log.e("TAG","TAG12");
        byte[] b = ByteBuffer.allocate(4).putInt(value).array();

        for (int k = 0; k < b.length; k++) {
            System.out.println("Selva  [" + k + "] = " + "0x"
                    + UnicodeFormatter.byteToHex(b[k]));
        }

        return b[3];
    }*/

    /*public byte[] sel(int val) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putInt(val);
        buffer.flip();
        return buffer.array();
    }*/

    /*public void printBill(){
        Thread t = new Thread() {
            public void run() {
                try {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    mBluetoothDevice = mBluetoothAdapter
                            .getRemoteDevice(global.getDeviceAddress());
                    mBluetoothSocket = mBluetoothDevice
                            .createRfcommSocketToServiceRecord(applicationUUID);
                    mBluetoothAdapter.cancelDiscovery();
                    mBluetoothSocket.connect();


                    OutputStream os = mBluetoothSocket
                            .getOutputStream();
                    String BILL = "";

                    BILL = "   OLYMPIA TAILORS & DRESSES     \n"
                            + "Howrah Super Market,42,G T Road\n"
                            + "Room No 11,12 & 13 Howrah-711101\n";
                    BILL = BILL
                            + "--------------------------------\n";
                    BILL = BILL
                            +"Item               Qty      Rate";
                    BILL = BILL
                            + "--------------------------------\n";
                    //BILL = BILL + String.format("%1$-10s %2$10s %3$13s %4$10s", "Item", "Qty", "Rate", "Totel");
                    //BILL = BILL + "\n";
                    //BILL = BILL
                    //+ "-----------------------------------------------";
                    BILL = BILL + "\n " + String.format("%1$-10s %2$10s %3$11s %4$10s", "item-001", "5", "10", "50.00");
                    //BILL = BILL + "\n " + String.format("%1$-10s %2$10s %3$11s %4$10s", "item-002", "10", "5", "50.00");
                    //BILL = BILL + "\n " + String.format("%1$-10s %2$10s %3$11s %4$10s", "item-003", "20", "10", "200.00");
                    //BILL = BILL + "\n " + String.format("%1$-10s %2$10s %3$11s %4$10s", "item-004", "50", "10", "500.00");

                    BILL = BILL
                            + "\n--------------------------------";
                    BILL = BILL + "\n";

                    //BILL = BILL + "                   Total Qty:" + "      " + "85" + "\n";
                    BILL = BILL + "                   Total Value:" + "     " + "700.00" + "\n";

                    BILL = BILL
                            + "--------------------------------\n";
                    BILL = BILL + "\n";
                    os.write(BILL.getBytes());
                    //This is printer specific code you can comment ==== > Start

                    // Setting height
                    int gs = 29;
                    os.write(intToByteArray(gs));
                    int h = 104;
                    os.write(intToByteArray(h));
                    int n = 162;
                    os.write(intToByteArray(n));

                    // Setting Width
                    int gs_width = 29;
                    os.write(intToByteArray(gs_width));
                    int w = 119;
                    os.write(intToByteArray(w));
                    int n_width = 2;
                    os.write(intToByteArray(n_width));


                } catch (Exception e) {
                    Log.e("MainActivity", "Exe ", e);
                }
            }
        };
        t.start();
    }*/

    public static boolean hasPermissions(Context context, String... permissions) {
    if (context != null && permissions != null) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
    }
    return true;
    }

}
