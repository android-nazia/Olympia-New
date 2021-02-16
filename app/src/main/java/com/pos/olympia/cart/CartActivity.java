package com.pos.olympia.cart;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.pos.olympia.GlobalClass;
import com.pos.olympia.PrinterCommands;
import com.pos.olympia.R;
import com.pos.olympia.SharedPreference;
import com.pos.olympia.Utils;
import com.pos.olympia.db.DatabaseHandler;
import com.pos.olympia.model.ModelStock;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import static com.pos.olympia.bluetooth.BluetoothConnectivity.intToByteArray;

public class CartActivity extends AppCompatActivity implements View.OnClickListener{

    GlobalClass global;
    RecyclerView recyclerview;
    ArrayList<ModelStock> arrayList;
    ArrayList<ModelStock> arrayList_stock;
    Cursor cursor_stock , cursor_cashier;
    TextView tv_pretotal, tv_total;
    float total_amount, rate, amount_payble;
    Button btn_print;
    ImageView img_back;
    EditText edt_gst;
    String TAG = "TAG", billBig="", billSmall="";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    Button mScan, mPrint, mDisc;
    BluetoothAdapter mBluetoothAdapter;
    final String uniqueId = UUID.randomUUID().toString();
    private ProgressDialog mBluetoothConnectProgressDialog;
    private BluetoothSocket mBluetoothSocket;
    BluetoothDevice mBluetoothDevice;
    DatabaseHandler handler;
    SQLiteDatabase db;
    int total_item=0;
    final String order_id = getOrderId();
    int total_quantity ;
    SharedPreference pref;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_cart);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        setViews();
        handler = new DatabaseHandler(this);
        db = handler.getWritableDatabase();
        global = (GlobalClass)getApplicationContext();
        pref = new SharedPreference();

        if (global.getDiscount() != 0) {
            edt_gst.setText(global.getDiscount()+"");
        }
        arrayList = global.getGlobal_arr_cart();
        CartAdapter adapter = new CartAdapter();
        recyclerview.setAdapter(adapter);



        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();



        for(int i=0;i<arrayList.size();i++){
            int q = arrayList.get(i).getStockQuantity();
            float f = arrayList.get(i).getStockPrice();
            total_amount =total_amount+(q*f);

            total_item = total_item+q;


            total_quantity =  total_quantity + arrayList.get(i).getStockQuantity();
        }





        rate = global.getDiscount();
        Log.e("TAG", "rate "+rate);
        if(rate>0.0) {
            float total = total_amount;
            tv_pretotal.setText(total_amount+"");
            total = total - (total*rate)/100;
            tv_total.setText(""+total);

        }else{
            tv_pretotal.setText(total_amount+"");
            tv_total.setText(""+total_amount);
        }

        billBig = getItemsBig();
        billSmall = getItemsSmall();

       // order_id = getOrderId();

    }

    private void setViews(){
        tv_total = findViewById(R.id.tv_total);
        tv_pretotal = findViewById(R.id.tv_pretotal);
        btn_print = findViewById(R.id.btn_print);
        img_back = findViewById(R.id.img_back);
        edt_gst = findViewById(R.id.edt_gst);
        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.addItemDecoration(new DividerItemDecoration(recyclerview.getContext(),
                DividerItemDecoration.VERTICAL));
        recyclerview.setLayoutManager(new LinearLayoutManager(this));


        setLlisterner();
    }

    private void setLlisterner(){
        btn_print.setOnClickListener(this);
        img_back.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onTextChange();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_print:
                Log.e("TAG", "bill\n"+logBillSmall());
                printBill();
                addIntoDB();
                break;

            case R.id.img_back:
                finish();
                break;
        }
    }


    public class CartAdapter extends RecyclerView.Adapter<CartAdapter.Viewholder> {
        @NonNull
        @Override
        public CartAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.row_cart, parent, false);
            return new CartAdapter.Viewholder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CartAdapter.Viewholder holder, final int position) {
            holder.tv_qty.setText(arrayList.get(position).getStockQuantity()+"");
            holder.tv_price.setText(arrayList.get(position).getStockPrice()+"");
            holder.tv_item.setText(arrayList.get(position).getItemName());

            int qty = arrayList.get(position).getStockQuantity();
            float pri = arrayList.get(position).getStockPrice();
            float value = qty * pri;
            holder.tv_amount.setText(value+"");

            holder.img_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(arrayList.size()>0){
                        arrayList.remove(position);
                        notifyDataSetChanged();
                        global.setGlobal_arr_cart(arrayList);


                        if(arrayList.size()<1){
                            global.setGlobal_arr_cart(null);
                            finish();
                        }

                        total_amount=0.0f;
                        for(int i=0;i<arrayList.size();i++){
                            int q = arrayList.get(i).getStockQuantity();
                            float f = arrayList.get(i).getStockPrice();
                            total_amount =total_amount+(q*f);

                            total_item = total_item+q;
                        }


                        rate = global.getDiscount();
                        Log.e("TAG", "rate "+rate);
                        if(rate>0.0) {
                            float total = total_amount;
                            tv_pretotal.setText(total_amount+"");
                            total = total - (total*rate)/100;
                            tv_total.setText(""+total);
                        }else{
                            tv_pretotal.setText(total_amount+"");
                            tv_total.setText(""+total_amount);
                        }

                        billBig = getItemsBig();
                        billSmall = getItemsSmall();


                    }
                }
            });



        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        class Viewholder extends RecyclerView.ViewHolder {
            TextView tv_item, tv_qty, tv_price, tv_amount;
            ImageView img_delete;

            public Viewholder(View itemView) {
                super(itemView);
                tv_item = itemView.findViewById(R.id.tv_item);
                tv_qty = itemView.findViewById(R.id.tv_qty);
                tv_price = itemView.findViewById(R.id.tv_price);
                img_delete = itemView.findViewById(R.id.img_delete);
                tv_amount = itemView.findViewById(R.id.tv_amount);
            }
        }

        public String parseDate(String time) {
            String inputPattern = "yyyy-MM-dd";
            String outputPattern = "dd MMM, yyyy";
            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.ENGLISH);
            Date date = null;
            String str = null;
            try {
                date = inputFormat.parse(time);
                str = outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return str;
        }

    }

    private String getTodaysDate(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String formattedDate = df.format(c);
        return formattedDate;
    }

    private String getTodaysTime(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        String formattedDate = df.format(c);
        return formattedDate;
    }

    private String getOrderId(){
        Calendar c = Calendar.getInstance();
        int Hr24=c.get(Calendar.HOUR_OF_DAY);
        int Min=c.get(Calendar.MINUTE);
        int ss=c.get(Calendar.SECOND);

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        String formattedDat = df.format(date);

        formattedDat = formattedDat+"_"+Hr24+":"+Min+":"+ss;

        return formattedDat;
    }

    private String getItemsBig(){
        String items=null;
        for(int i=0;i<arrayList.size();i++){
            if(i==0) {
                int q = arrayList.get(i).getStockQuantity();
                String I = arrayList.get(i).getItemName();
                float p = arrayList.get(i).getStockPrice();
                String pp = arrayList.get(i).getStockPrice()+"";
                float r = q*p;
                String rr = r+"";
                //String first =  q+ " " +I+ " " +p;
                int item_space = 0;
                if(q<10){
                    item_space = 23 - I.length();
                }else{
                    item_space = 22 - I.length();
                }

                int rate_space = 10 - rr.length();
                int qty_space = 11 - pp.length();
                //Log.e("TAG" , "length"+i+" "+first.length()+" "+ space1 );
                items=itemSpaces(I,item_space)+" "+q+" "+priceSpaces(pp,qty_space)+" "+priceSpaces(rr,rate_space)+"\n";
            }else{
                int q = arrayList.get(i).getStockQuantity();
                String I = arrayList.get(i).getItemName();
                float p = arrayList.get(i).getStockPrice();
                String pp = arrayList.get(i).getStockPrice()+"";
                float r = q*p;
                String rr = r+"";
                //String second= q+ " " +I+ " " +p;
                int item_space = 0;
                if(q<10){
                    item_space = 23 - I.length();
                }else{
                    item_space = 22 - I.length();
                }
                int rate_space = 10 - rr.length();
                int qty_space = 11 - pp.length();
                items=items+itemSpaces(I,item_space)+" "+q+" "+priceSpaces(pp,qty_space)+" "+priceSpaces(rr,rate_space)+ "\n";
            }
        }
        return  items;
    }

    private String getItemsSmall(){
        String items=null;
        for(int i=0;i<arrayList.size();i++){
            if(i==0) {
                int q = arrayList.get(i).getStockQuantity();
                String I = arrayList.get(i).getItemName();
                float p = arrayList.get(i).getStockPrice();
                String pp = arrayList.get(i).getStockPrice()+"";
                float r = q*p;
                String rr = r+"";
                //String first =  q+ " " +I+ " " +p;
                int item_space = 0;
                if(q<10){
                    item_space = 12 - I.length();
                }else{
                    item_space = 11 - I.length();
                }

                int rate_space = 9 - rr.length();
                int qty_space = 6 - pp.length();
                //Log.e("TAG" , "length"+i+" "+first.length()+" "+ space1 );
                items=itemSpaces(I,item_space)+" "+q+" "+priceSpaces(pp,qty_space)+" "+priceSpaces(rr,rate_space)+"\n";
            }else{
                int q = arrayList.get(i).getStockQuantity();
                String I = arrayList.get(i).getItemName();
                float p = arrayList.get(i).getStockPrice();
                String pp = arrayList.get(i).getStockPrice()+"";
                float r = q*p;
                String rr = r+"";
                //String second= q+ " " +I+ " " +p;
                int item_space = 0;
                if(q<10){
                    item_space = 12 - I.length();
                }else{
                    item_space = 11 - I.length();
                }
                int rate_space = 9 - rr.length();
                int qty_space = 6 - pp.length();
                items=items+itemSpaces(I,item_space)+" "+q+" "+priceSpaces(pp,qty_space)+" "+priceSpaces(rr,rate_space)+ "\n";
            }
        }
        return  items;
    }


    private String getTotalBig(){
        /*String t = "TOTAL " + total_quantity;
        int space = 10 - t.length();
        t = itemSpaces("TOTAL " , space)+total_amount;
        return  t;*/
        String t = "TOTAL                   "+total_quantity+"                "+ total_amount;
        return  t;
    }

    private String getTotalSmall(){
        /*String t = "TOTAL " + total_quantity;
        int space = 10 - t.length();
        t = itemSpaces("TOTAL " , space)+total_amount;
        return  t;*/
        String t = "TOTAL        "+total_quantity+"          "+ total_amount;
        return  t;
    }



    private String getDiscountBig(){
        float f = 0.0f;
        rate = global.getDiscount();
        Log.e("TAG", "rate "+rate);
        if(rate>0.0) {
            float total = total_amount;
            f = (total*rate)/100;

        }else{
            f = 0.0f;
        }

        String t = "ITEM DISCOUNT @ " + edt_gst.getText().toString()+"% "+"-"+f;
        String first_part = "ITEM DISCOUNT @ " + edt_gst.getText().toString()+"% ";
        int space = 48 - t.length();
        t = itemSpaces(first_part , space)+"-"+f;
        return  t;
    }

    private String getDiscountSmall(){
        float f = 0.0f;
        rate = global.getDiscount();
        Log.e("TAG", "rate "+rate);
        if(rate>0.0) {
            float total = total_amount;
            f = (total*rate)/100;

        }else{
            f = 0.0f;
        }

        String t = "ITEM DISCOUNT @ " + edt_gst.getText().toString()+"% "+"-"+f;
        String first_part = "ITEM DISCOUNT @ " + edt_gst.getText().toString()+"% ";
        int space = 32 - t.length();
        t = itemSpaces(first_part , space)+"-"+f;
        return  t;
    }

    private String getDiscountedValueBig(){
        float net_value = 0.0f;
        rate = global.getDiscount();
        Log.e("TAG", "rate "+rate);
        if(rate>0.0) {
            float total = total_amount;
            total = total - (total*rate)/100;
            net_value = total;
        }else{
            net_value = total_amount;
        }
        amount_payble = net_value;
        String t = "NET AMOUNT " + net_value;
        int space = 48 - t.length();
        t = itemSpaces("NET AMOUNT " , space)+net_value;
        return  t;
    }

    private String getDiscountedValueSmall(){
        float net_value = 0.0f;
        rate = global.getDiscount();
        Log.e("TAG", "rate "+rate);
        if(rate>0.0) {
            float total = total_amount;
            total = total - (total*rate)/100;
            net_value = total;
        }else{
            net_value = total_amount;
        }
        amount_payble = net_value;
        String t = "NET AMOUNT " + net_value;
        int space = 32 - t.length();
        t = itemSpaces("NET AMOUNT " , space)+net_value;
        return  t;
    }

    private String itemSpaces(String name, int spacess) {
        for(int i=0;i<spacess;i++){
            name= name +" ";
        }
        return name;
    }

    private String priceSpaces(String name, int spacess) {
        for(int i=0;i<spacess;i++){
            name= " "+name;
        }
        return name;
    }

    private void printLogo( OutputStream os , int img) {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), img);
            if(bmp!=null){
                byte[] command = Utils.decodeBitmap(bmp);
                os.write(PrinterCommands.ESC_ALIGN_CENTER);
                printText(os, command);
            }else{
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }

    }

    private void printThankYou( OutputStream os , int img) {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), img);
            if(bmp!=null){
                byte[] command = Utils.decodeBitmap(bmp);
                os.write(PrinterCommands.ESC_ALIGN_CENTER);
                printText(os, command);
                printNewLine(os);
                printNewLine(os);
                printNewLine(os);
            }else{
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }

    }

    private String newShopHeading() {
        String heading;

        heading = "\n        OLYMPIA\n"
                + "KIDS & MENS FASHION WEAR\n\n";

        return heading;

    }

    private String newShopAddress() {
        String address
                = "************************************************\n"
                + "      89, Jolapara Masjid lane, Tikiapara,\n"
                + "        Near Noori Masjid, Howrah 711101\n\n"
                + "GSTIN    :   19AEJPA372191ZA\n"
                + "Mobile   :   9836306644\n"
                + "C/M NO   :   "+"Oly_"+order_id+"\n"
                + "Date     :   "+getTodaysDate()+"            Time : "+getTodaysTime()+"\n"
                + "Cashier  :   "+global.getGlobal_staff().getStaffName()+"\n"
                + "************************************************\n\n"
                + "                  FIXED PRICE\n";


        return address;
    }

    private String oldShopHeading() {
        String heading;

        heading = "\n        OLYMPIA\n"
                + "     READYMADE GARMENTS\n\n";

        heading = heading
                + "***********************\n\n\n";

        return heading;

    }

    private String oldShopAddress() {
        String address
                = "************************************************\n"
                + "          42, G T Road, Howrah-711 101\n"
                + "              GSTIN 19AEJPA372191ZA\n"
                + "               Mobile - 9836306644\n"
                + "         C/M NO."+"Oly_"+order_id+"\n"
                + "               Date - "+getTodaysDate()+"\n"
                + "               Time - "+getTodaysTime()+"\n"
                + "************************************************\n\n\n"
                + "                  FIXED PRICE\n";
        return address;
    }


    private String logBillBig(){
        String BILL = newShopAddress();

        BILL = BILL
                + "------------------------------------------------\n";
        BILL = BILL
                + "PRODUCT               QNTY       RATE     AMOUNT\n";
        BILL = BILL
                + "------------------------------------------------\n";

        BILL = BILL + billBig;
        BILL = BILL
                + "------------------------------------------------";
        BILL = BILL + "\n";
        BILL = BILL + getTotalBig() + "\n";
        BILL = BILL
                + "------------------------------------------------";
        BILL = BILL + "\n";
        BILL = BILL + getDiscountBig() + "\n";
        BILL = BILL
                + "------------------------------------------------";
        BILL = BILL + "\n";
        BILL = BILL + getDiscountedValueBig() + "\n\n";
        BILL = BILL
                + "************************************************\n\n";
        BILL = BILL + "1)Exchange within 3 days, between(1pm to 3pm)\n";
        BILL = BILL + "come with Bill & Bar-code. Exchange only ONCE.\n";
        BILL = BILL + "2)No exchange on Saturday, Sunday & on Calendar\n";
        BILL = BILL + "Holidays\n";
        BILL = BILL + "3)No Guarantee for COLOUR and WORK\n";
        BILL = BILL + "4)No Money Refund\n";
        BILL = BILL + "5)Open on Thursday\n";
        BILL = BILL + "6)No exchange, no refund for Offer Items.\n\n";
        BILL = BILL
                + "************************************************\n";
        BILL = BILL + "        Thank You For Visiting Olympia\n";
        BILL = BILL + "           We hope to see you again " + "\n\n";




        return BILL;
    }


    private String logBillSmall(){
        String BILL ;

        BILL =    "   OLYMPIA READYMADE GARMENTS\n"
                + "  42, G T Road, Howrah-711 101\n"
                + "      GSTIN 19AEJPA372191ZA\n"
                + "       Mobile - 9836306644\n"
                + " C/M NO."+"Oly_"+order_id+"\n"
                + "       Date - "+getTodaysDate()+"\n"
                + "       Time - "+getTodaysTime()+"\n\n\n"
                + "          FIXED PRICE\n";
        BILL = BILL
                + "--------------------------------\n";
        BILL = BILL
                + "PRODUCT     QNTY  RATE    AMOUNT\n";
        BILL = BILL
                + "--------------------------------\n";

        BILL = BILL + billSmall;
        BILL = BILL
                + "--------------------------------";
        BILL = BILL + "\n";
        BILL = BILL + getTotalSmall() + "\n";
        BILL = BILL
                + "--------------------------------";
        BILL = BILL + "\n";
        BILL = BILL + getDiscountSmall() + "\n";
        BILL = BILL
                + "--------------------------------";
        BILL = BILL + "\n";
        BILL = BILL + getDiscountedValueSmall() + "\n";
        BILL = BILL
                + "--------------------------------\n\n";
        BILL = BILL + "1)Exchange within 3 days between\n";
        BILL = BILL + "  (1pm to 3pm) come with Bill &\n";
        BILL = BILL + "  Bar-code. Exchange only ONCE.\n";
        BILL = BILL + "2)No exchange on Saturday/Sunday\n";
        BILL = BILL + "  & on Calendar Holidays\n";
        BILL = BILL + "3)No Guarantee for COLOUR and\n";
        BILL = BILL + "  WORK\n";
        BILL = BILL + "4)No Money Refund\n";
        BILL = BILL + "5)Open on Thursday\n";
        BILL = BILL + "6)No exchange, no refund for\n";
        BILL = BILL + "  Offer Items.\n";
        BILL = BILL
                + "--------------------------------\n";
        BILL = BILL + "Thank you for choosing Olympia\n";
        BILL = BILL + "   We hope to see you again " + "\n\n\n\n\n";




        return BILL;
    }



    private void printBill(){
        Thread t = new Thread() {
            public void run() {
                try {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    mBluetoothDevice = mBluetoothAdapter
                            .getRemoteDevice(global.getDeviceAddress());
                    mBluetoothSocket = global.getSockey();
                    mBluetoothAdapter.cancelDiscovery();

                    OutputStream os = mBluetoothSocket
                            .getOutputStream();

                    String BILL ;

                    String billSize = pref.getSharedPrefString(CartActivity.this, pref.PREFS_bill);

                    if(billSize==null || billSize.isEmpty() || billSize.equalsIgnoreCase("small")) {
                        BILL = logBillSmall();
                        os.write(PrinterCommands.ESC_ALIGN_LEFT);
                        os.write(BILL.getBytes());
                    }else{
                        BILL = logBillBig();

                        //print logo
                        printLogo(os, R.drawable.logo1);

                        //print heading
                        String headingContent = newShopHeading();
                        byte[]  formatHeading = new byte[]{0x1B,0x21,0x20};
                        os.write(PrinterCommands.ESC_ALIGN_LEFT);
                        os.write(formatHeading);
                        os.write(headingContent.getBytes(),0,headingContent.getBytes().length);

                        //print contents
                        //bold
                        byte[] format = new byte[]{0x1B,0x21,0x08};
                        os.write(format);
                        os.write(BILL.getBytes(),0,BILL.getBytes().length);

                        //print thank you
                        printThankYou(os, R.drawable.thank_you);
                    }

                } catch (Exception e) {
                    Log.e("MainActivity", "Exe ", e);
                }
            }
        };
        t.start();
    }





    private void onTextChange(){
        edt_gst.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0) {
                    float total = total_amount;
                    tv_pretotal.setText(total_amount+"");
                    rate = Float.parseFloat(edt_gst.getText().toString());
                    global.setDiscount(rate);
                    total = total - (total*rate)/100;
                    tv_total.setText(""+total);
                }else{
                    tv_pretotal.setText(total_amount+"");
                    tv_total.setText(""+total_amount);
                }
            }
        });


    }

    public void addIntoDB(){
        for (int i=0;i<arrayList.size();i++){
            insertIntoHistory(i);
        }

        insertIntoOrder();
    }

    private void insertIntoHistory(int position){
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.UniqueKey,uniqueId);
        values.put(DatabaseHandler.OrderId,"Oly_"+order_id);
        values.put(DatabaseHandler.ItemId,arrayList.get(position).getItemId());
        values.put(DatabaseHandler.ItemName,arrayList.get(position).getItemName());
        values.put(DatabaseHandler.Price,arrayList.get(position).getStockPrice());
        values.put(DatabaseHandler.Quantity,arrayList.get(position).getStockQuantity());
        values.put(DatabaseHandler.DateTime,getTodaysDate());
        values.put(DatabaseHandler.CancelOrder,"no");
        db.insert(DatabaseHandler.HistoryTable,null,values);
        Log.e("TAG" , "--data inserted in History");


        substractItemFromStock(arrayList.get(position).getStockQuantity(), arrayList.get(position).getItemId());

    }

    private void insertIntoOrder(){
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.UniqueKey,uniqueId);
        values.put(DatabaseHandler.OrderId,"Oly_"+order_id);
        values.put(DatabaseHandler.TotalPrice,tv_pretotal.getText().toString());
        values.put(DatabaseHandler.AmountPayable,tv_total.getText().toString());
        values.put(DatabaseHandler.TotalItem,total_item);
        values.put(DatabaseHandler.Discount,edt_gst.getText().toString());
        values.put(DatabaseHandler.User,global.getGlobal_staff().getStaffName());
        values.put(DatabaseHandler.DateTime,getTodaysDate());
        values.put(DatabaseHandler.CancelOrder,"no");
        db.insert(DatabaseHandler.OrderTable,null,values);
        Log.e("TAG" , "--data inserted in Order");

        global.getGlobal_arr_cart().clear();
        finish();

    }


    private void substractItemFromStock(int qty, String item_id){
        int initial_qty = fetchStockTable(item_id);
        int left_qty = initial_qty - qty;
        String str = String.valueOf(left_qty);
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.StockQuantity,str);
        db.update(DatabaseHandler.StockTable,values, DatabaseHandler.ItemId+"="+item_id, null);
        Log.e("TAG" , "--data substracted in Stock");
    }


    private int fetchStockTable(String item_id){
        String query_doc = "SELECT * FROM StockTable where ItemId = '" + item_id + "'";
        cursor_stock = db.rawQuery(query_doc,null);
        cursor_stock.moveToFirst();
        if(cursor_stock.getCount()>0) {
            Log.e("TAG", "initial_qty "+cursor_stock.getInt(1)+"");
            return cursor_stock.getInt(1);
        }
        return 0;
    }

    //print photo
    private void printText( OutputStream os, byte[] msg) {
        try {
            // Print normal text
            printNewLine(os);
            os.write(msg);
            printNewLine(os);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //print new line
    private void printNewLine(OutputStream os) {
        try {
            os.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
