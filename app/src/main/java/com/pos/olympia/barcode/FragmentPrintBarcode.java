package com.pos.olympia.barcode;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.pos.olympia.GlobalClass;
import com.pos.olympia.R;
import com.pos.olympia.btsdk.BluetoothService;
import com.pos.olympia.btsdk.PrintPic;
import com.pos.olympia.db.DatabaseHandler;

import com.pos.olympia.model.ModelStock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import cn.refactor.lib.colordialog.ColorDialog;


public class FragmentPrintBarcode extends Fragment implements View.OnClickListener {

    Spinner spinner_item;
    Toolbar toolbar;
    String selected_name="", selected_id="", selected_price="", selected_barcode="";
    EditText edt_number;
    Button btn_print;
    TextView tvCategoryHeader, edt_price, tv_code;
    Cursor cursor_ord;
    DatabaseHandler handler;
    SQLiteDatabase db;
    ArrayList<ModelStock> arrayList;
    CustomAdapter myAdapter;
    ImageView img;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    BluetoothDevice mBluetoothDevice;
    GlobalClass global;
    Bitmap bitmap, bitmap_print;
    BluetoothService mService = null;
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    switch (msg.arg1) {
                        case 0:
                        case 1:
                            //Log.d("蓝牙调试", "等待连接.....");
                            return;
                        case 2:
                            //Log.d("蓝牙调试", "正在连接.....");
                            return;
                        case 3:
                            /*Toast.makeText(FragmentPrintBarcode.this.getActivity(),
                                    "Connect successful", Toast.LENGTH_SHORT).show();
                            FragmentPrintBarcode.this.btnClose.setEnabled(true);
                            FragmentPrintBarcode.this.btnSend.setEnabled(true);
                            FragmentPrintBarcode.this.qrCodeBtnSend.setEnabled(true);
                            FragmentPrintBarcode.this.btnSendDraw.setEnabled(true);*/
                            return;
                        default:
                            return;
                    }
                case 5:
                    Toast.makeText(FragmentPrintBarcode.this.getActivity(),
                            "Device connection was lost", Toast.LENGTH_SHORT).show();
                    /*FragmentPrintBarcode.this.btnClose.setEnabled(false);
                    FragmentPrintBarcode.this.btnSend.setEnabled(false);
                    FragmentPrintBarcode.this.qrCodeBtnSend.setEnabled(false);
                    FragmentPrintBarcode.this.btnSendDraw.setEnabled(false);*/
                    return;
                case 6:
                    Toast.makeText(FragmentPrintBarcode.this.getActivity(),
                            "Unable to connect device", Toast.LENGTH_SHORT).show();
                    return;
                default:
                    return;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new DatabaseHandler(getActivity());
        db = handler.getWritableDatabase();
        toolbar = getActivity().findViewById(R.id.toolbar);
        arrayList = new ArrayList<>();
        this.mService = new BluetoothService(getActivity(), this.mHandler);
        if (!this.mService.isAvailable()) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        global = (GlobalClass)getActivity().getApplicationContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.frag_print_barcode,container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        setViews(view);
        return view;
    }

    private void setViews(View view){
        spinner_item = view.findViewById(R.id.spinner_item);
        edt_price = view.findViewById(R.id.edt_price);
        edt_number = view.findViewById(R.id.edt_number);
        btn_print = view.findViewById(R.id.btn_print);
        img = view.findViewById(R.id.img);
        tv_code = view.findViewById(R.id.tv_code);
        tvCategoryHeader = toolbar.findViewById(R.id.tvCategoryHeader);
        tvCategoryHeader.setText("Print Barcode");
        setListeners();
    }

    private void setListeners(){
        btn_print.setOnClickListener(this);
        fetchStockTable();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_print:
                generateBarcode(selected_barcode);
                tv_code.setText(selected_barcode);
                validation();
                break;
        }
    }

    private void generateBarcode(String code){
        try {
            bitmap = encodeAsBitmap(code, BarcodeFormat.CODE_128, 600, 300);
            bitmap_print = encodeAsBitmap(code, BarcodeFormat.CODE_128, 40, 20);
            img.setImageBitmap(bitmap);

            saveImage();


        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void saveImage(){
        // Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
                FileOutputStream out = new FileOutputStream(photoFile);
                bitmap_print.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();
            } catch (Exception ex) {}

            /*if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                        "com.pos.olympia.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }*/
        //}
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }



    private void validation(){
        if(selected_id.length()<1){
            Toast.makeText(getActivity(),"Select Item" , Toast.LENGTH_SHORT).show();
        }else if(edt_number.getText().length()<1){
            edt_number.setError("Enter total numbers of barcodes to be printed");
            edt_number.requestFocus();
        }else{
            ColorDialog dialog = new ColorDialog(getActivity());
            dialog.setTitle("Print Barcode ?");
            dialog.setAnimationEnable(true);
            dialog.setAnimationIn(getInAnimationTest(getActivity()));
            dialog.setAnimationOut(getOutAnimationTest(getActivity()));
            dialog.setContentImage(getResources().getDrawable(R.drawable.ic_print));
            dialog.setPositiveListener("Print", new ColorDialog.OnPositiveListener() {
                @Override
                public void onClick(ColorDialog dialog) {

                    //printImage();

                    //printBarcode(selected_barcode);
                    byte[] cmd = {27, 90, 0, 2, 7, 23, 0};
                    String msg2 = FragmentPrintBarcode.this.getResources()
                            .getString(R.string.app_name);
                    if (msg2.length() > 0) {
                        FragmentPrintBarcode.this.mService.write(cmd);
                        FragmentPrintBarcode.this.mService.sendMessage(msg2, "GBK");
                        return;
                    }
                }
            }).setNegativeListener(getString(R.string.cancel), new ColorDialog.OnNegativeListener() {
                @Override
                public void onClick(ColorDialog dialog) {
                    dialog.dismiss();
                }
            }).show();
        }
    }

    public void printBarcode(final String str){
        Thread t = new Thread() {
            public void run() {
                try {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    mBluetoothDevice = mBluetoothAdapter
                            .getRemoteDevice(global.getDeviceAddress());
                    mBluetoothSocket = global.getSockey();
                    mBluetoothAdapter.cancelDiscovery();

                    OutputStream os = mBluetoothSocket.getOutputStream();
                    //String BILL = str;
                    //os.write(BILL.getBytes());
                    //This is printer specific code you can comment ==== > Start

                    // Setting height
                    /*int gs = 29;
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
                    os.write(intToByteArray(n_width));*/

                    printPhoto(R.mipmap.no_image_square, os);

                } catch (Exception e) {
                    Log.e("MainActivity", "Exe ", e);
                }
            }
        };
        t.start();

    }

    public void printPhoto(int img, OutputStream os) {
        try {
                /*byte[] command = Utils.decodeBitmap(bitmap_print);
                os.write(PrinterCommands.ESC_ALIGN_CENTER);
                os.write(PrinterCommands.SELECT_BIT_IMAGE_MODE);
                os.write(command);*/

          /*  byte[] cmd = {27, 90, 0, 2, 7, 23, 0};
            String msg2 = FragmentPrintBarcode.this.getResources()
                    .getString(R.string.app_name);
            if (msg2.length() > 0) {
                FragmentPrintBarcode.this.mService.write(cmd);
                FragmentPrintBarcode.this.mService.sendMessage(msg2, "GBK");
                return;
            }*/


        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    /*private void printBill(final String code){
        Thread t = new Thread() {
            public void run() {
                try {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    mBluetoothDevice = mBluetoothAdapter
                            .getRemoteDevice(global.getDeviceAddress());
                    mBluetoothSocket = global.getSockey();
                    mBluetoothAdapter.cancelDiscovery();

                    OutputStream os = mBluetoothSocket.getOutputStream();
                    String BILL = code;
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


    private void fetchStockTable(){
        arrayList.clear();
        String query_doc = "SELECT * FROM StockTable";//
        cursor_ord = db.rawQuery(query_doc,null);
        cursor_ord.moveToFirst();
        if(cursor_ord.getCount()>0) {
            for (int i = 0; i < cursor_ord.getCount(); i++) {
                ModelStock model = new ModelStock();
                model.setStockId(cursor_ord.getString(0));
                model.setStockQuantity(cursor_ord.getInt(1));
                model.setStockPrice(cursor_ord.getInt(2));
                model.setBarcode(cursor_ord.getString(3));
                model.setItemId(cursor_ord.getString(4));
                model.setItemName(cursor_ord.getString(5));
                arrayList.add(model);
                cursor_ord.moveToNext();
            }
            Collections.reverse(arrayList);
            ModelStock model0 = new ModelStock();
            model0.setStockId("0");
            model0.setStockQuantity(0);
            model0.setStockPrice(0);
            model0.setBarcode("0");
            model0.setItemId("0");
            model0.setItemName("Select Item");
            arrayList.add(model0);
        }else{
            //tv_nodata.setVisibility(View.VISIBLE);
        }
        if(arrayList.size()>0){
            populateSpinner();
        }else{
            Toast.makeText(getActivity(), "Please Add Items First" , Toast.LENGTH_SHORT).show();
        }
    }

    private void populateSpinner(){
        myAdapter = new CustomAdapter(getActivity(), R.layout.row_spinner_item, arrayList);
        myAdapter.setDropDownViewResource(R.layout.row_spinner_item);
        spinner_item.setAdapter(myAdapter);
        spinner_item.setSelection(myAdapter.getCount());
        spinner_item.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                selected_name = arrayList.get(position).getItemName().trim();
                selected_id = arrayList.get(position).getItemId();
                selected_price = String.valueOf(arrayList.get(position).getStockPrice());
                selected_barcode = arrayList.get(position).getBarcode();
                edt_price.setText(selected_price);
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }




    public static AnimationSet getInAnimationTest(Context context) {
        AnimationSet out = new AnimationSet(context, null);
        AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
        alpha.setDuration(150);
        ScaleAnimation scale = new ScaleAnimation(0.6f, 1.0f, 0.6f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(150);
        out.addAnimation(alpha);
        out.addAnimation(scale);
        return out;
    }

    public static AnimationSet getOutAnimationTest(Context context) {
        AnimationSet out = new AnimationSet(context, null);
        AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.0f);
        alpha.setDuration(150);
        ScaleAnimation scale = new ScaleAnimation(1.0f, 0.6f, 1.0f, 0.6f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(150);
        out.addAnimation(alpha);
        out.addAnimation(scale);
        return out;
    }

    public class CustomAdapter extends ArrayAdapter<ModelStock>  {
        LayoutInflater flater;
        ArrayList<ModelStock> list;
        Context c;
        public CustomAdapter(Context context, int resourceId,
                             ArrayList<ModelStock> objects) {
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

        @Override
        public int getCount() {
            // don't display last item. It is used as hint.
            int count = super.getCount();
            return count > 0 ? count - 1 : count;
        }

        private View rowview(View convertView , int position){
            ModelStock rowItem = getItem(position);
            CustomAdapter.viewHolder holder ;
            View rowview = convertView;
            if (rowview==null) {
                holder = new CustomAdapter.viewHolder();
                flater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowview = flater.inflate(R.layout.row_spinner_item, null, false);
                holder.txtTitle = rowview.findViewById(R.id.tv_item);
                rowview.setTag(holder);
            }else{
                holder = (CustomAdapter.viewHolder) rowview.getTag();
            }
            holder.txtTitle.setText(rowItem.getItemName());

            return rowview;
        }
        private class viewHolder{
            TextView txtTitle;
        }


    }


    private Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width,
                                  int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }


    public void onResume() {
        super.onResume();
        if (!this.mService.isBTopen()) {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 2);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mService != null) {
            this.mService.stop();
        }
        this.mService = null;
    }

   /* public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == -1) {
                    this.con_dev = this.mService.getDevByMac(data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS));
                    this.mService.connect(this.con_dev);
                    return;
                }
                return;
            case 2:
                if (resultCode == -1) {
                    Toast.makeText(getActivity(), "Bluetooth open successful", 1).show();
                    return;
                }
                return;
            default:
                return;
        }
    }*/

    /* access modifiers changed from: private */
    @SuppressLint({"SdCardPath"})
    public void printImage() {
        PrintPic pg = new PrintPic();
        pg.initCanvas(576);
        pg.initPaint();
        pg.drawImage(0.0f, 0.0f, "/mnt/sdcard/icon.jpg");
        byte[] sendData = pg.printDraw();
        this.mService.write(sendData);
        Log.d("fgg", ""+sendData.length);
    }


}

