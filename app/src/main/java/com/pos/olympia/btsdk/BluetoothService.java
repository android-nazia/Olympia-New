package com.pos.olympia.btsdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class BluetoothService {

    /* renamed from: D */
    private static final boolean f6D = true;
    public static final int MESSAGE_CONNECTION_LOST = 5;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_UNABLE_CONNECT = 6;
    public static final int MESSAGE_WRITE = 3;
    /* access modifiers changed from: private */
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NAME = "BTPrinter";
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_NONE = 0;
    private static final String TAG = "BluetoothService";
    private AcceptThread mAcceptThread;
    /* access modifiers changed from: private */
    public final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    /* access modifiers changed from: private */
    public ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public int mState = 0;

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = BluetoothService.this.mAdapter.listenUsingRfcommWithServiceRecord(BluetoothService.NAME, BluetoothService.MY_UUID);
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "listen() failed", e);
            }
            this.mmServerSocket = tmp;
        }

        public void run() {
            Log.d("AcceptThread线程运行", "正在运行......");
            Log.d(BluetoothService.TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            while (BluetoothService.this.mState != 3) {
                try {
                    BluetoothSocket socket = this.mmServerSocket.accept();
                    if (socket != null) {
                        synchronized (BluetoothService.this) {
                            switch (BluetoothService.this.mState) {
                                case 0:
                                case 3:
                                    try {
                                        socket.close();
                                        break;
                                    } catch (IOException e) {
                                        Log.e(BluetoothService.TAG, "Could not close unwanted socket", e);
                                        break;
                                    }
                                case 1:
                                case 2:
                                    BluetoothService.this.connected(socket, socket.getRemoteDevice());
                                    break;
                            }
                        }
                    }
                } catch (IOException e2) {
                    Log.e(BluetoothService.TAG, "accept() failed", e2);
                }
            }
            Log.i(BluetoothService.TAG, "END mAcceptThread");
            return;
        }

        public void cancel() {
            Log.d(BluetoothService.TAG, "cancel " + this);
            try {
                this.mmServerSocket.close();
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "close() of server failed", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(BluetoothService.MY_UUID);
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "create() failed", e);
            }
            this.mmSocket = tmp;
        }

        public void run() {
            Log.i(BluetoothService.TAG, "BEGIN mConnectThread");
            setName("ConnectThread");
            BluetoothService.this.mAdapter.cancelDiscovery();
            try {
                this.mmSocket.connect();
                synchronized (BluetoothService.this) {
                    BluetoothService.this.mConnectThread = null;
                }
                BluetoothService.this.connected(this.mmSocket, this.mmDevice);
            } catch (IOException e) {
                BluetoothService.this.connectionFailed();
                try {
                    this.mmSocket.close();
                } catch (IOException e2) {
                    Log.e(BluetoothService.TAG, "unable to close() socket during connection failure", e2);
                }
                BluetoothService.this.start();
            }
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(BluetoothService.TAG, "create ConnectedThread");
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "temp sockets not created", e);
            }
            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
        }

        public void run() {
            Log.d("ConnectedThread线程运行", "正在运行......");
            Log.i(BluetoothService.TAG, "BEGIN mConnectedThread");
            while (true) {
                try {
                    byte[] buffer = new byte[256];
                    int bytes = this.mmInStream.read(buffer);
                    if (bytes <= 0) {
                        break;
                    }
                    BluetoothService.this.mHandler.obtainMessage(2, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(BluetoothService.TAG, "disconnected", e);
                    BluetoothService.this.connectionLost();
                    if (BluetoothService.this.mState != 0) {
                        BluetoothService.this.start();
                        return;
                    }
                    return;
                }
            }
            Log.e(BluetoothService.TAG, "disconnected");
            BluetoothService.this.connectionLost();
            if (BluetoothService.this.mState != 0) {
                Log.e(BluetoothService.TAG, "disconnected");
                BluetoothService.this.start();
            }
        }

        public void write(byte[] buffer) {
            try {
                this.mmOutStream.write(buffer);
                BluetoothService.this.mHandler.obtainMessage(3, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "close() of connect socket failed", e);
            }
        }
    }

    public BluetoothService(Context context, Handler handler) {
        this.mHandler = handler;
    }

    public synchronized boolean isAvailable() {
        boolean z;
        if (this.mAdapter == null) {
            z = false;
        } else {
            z = true;
        }
        return z;
    }

    public synchronized boolean isBTopen() {
        boolean z;
        if (!this.mAdapter.isEnabled()) {
            z = false;
        } else {
            z = true;
        }
        return z;
    }

    public synchronized BluetoothDevice getDevByMac(String mac) {
        return this.mAdapter.getRemoteDevice(mac);
    }

    public synchronized BluetoothDevice getDevByName(String name) {
        BluetoothDevice tem_dev;
        tem_dev = null;
        Set<BluetoothDevice> pairedDevices = getPairedDev();
        if (pairedDevices.size() > 0) {
            Iterator it = pairedDevices.iterator();
            while (true) {
                if (it.hasNext()) {
                    BluetoothDevice device = (BluetoothDevice) it.next();
                    if (device.getName().indexOf(name) != -1) {
                        tem_dev = device;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return tem_dev;
    }

    public synchronized void sendMessage(String message, String charset) {
        byte[] send;
        if (message.length() > 0) {
            try {
                send = message.getBytes(charset);
            } catch (UnsupportedEncodingException e) {
                send = message.getBytes();
                Log.d("蓝牙调试", "转码出错");
            }
            for (int loop = 0; loop < send.length; loop++) {
                //Log.d("蓝牙调试", send[loop]);
            }
            write(send);
            byte[] tail = new byte[3];
            tail[0] = 10;
            tail[1] = 13;
            write(tail);
        }
        return;
    }

    public synchronized Set<BluetoothDevice> getPairedDev() {
        return this.mAdapter.getBondedDevices();
    }

    public synchronized boolean cancelDiscovery() {
        return this.mAdapter.cancelDiscovery();
    }

    public synchronized boolean isDiscovering() {
        return this.mAdapter.isDiscovering();
    }

    public synchronized boolean startDiscovery() {
        return this.mAdapter.startDiscovery();
    }

    private synchronized void setState(int state) {
        this.mState = state;
        this.mHandler.obtainMessage(1, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return this.mState;
    }

    public synchronized void start() {
        Log.d(TAG, "start");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread == null) {
            this.mAcceptThread = new AcceptThread();
            this.mAcceptThread.start();
        }
        setState(1);
    }

    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
        if (this.mState == 2 && this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        this.mConnectThread = new ConnectThread(device);
        this.mConnectThread.start();
        setState(2);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }
        this.mConnectedThread = new ConnectedThread(socket);
        this.mConnectedThread.start();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(4));
        setState(3);
    }

    public synchronized void stop() {
        Log.d(TAG, "stop");
        setState(0);
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }
    }

    public void write(byte[] out) {
        synchronized (this) {
            if (this.mState == 3) {
                ConnectedThread r = this.mConnectedThread;
                r.write(out);
            }
        }
    }

    /* access modifiers changed from: private */
    public void connectionFailed() {
        setState(1);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(6));
    }

    /* access modifiers changed from: private */
    public void connectionLost() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5));
    }
}
