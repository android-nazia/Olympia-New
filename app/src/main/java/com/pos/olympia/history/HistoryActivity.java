package com.pos.olympia.history;

import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.olympia.Dividers.ReceiptDividerDecoration;
import com.pos.olympia.Listeners.ReceiptClickListener;
import com.pos.olympia.R;
import com.pos.olympia.db.DatabaseHandler;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersTouchListener;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    String from;
    ImageView img_back;
    TextView tvCategoryHeader;
    RecyclerView recyclerView;
    Cursor cursor_ord, cursor_his;
    DatabaseHandler handler;
    SQLiteDatabase db;
    ArrayList<HashMap<String, String>> arrayList_order;
    Fragment fragment=null;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_history);

        from=getIntent().getStringExtra("from");
        arrayList_order = new ArrayList<>();
        handler = new DatabaseHandler(this);
        db = handler.getWritableDatabase();
        setViews();

    }

    private void setViews(){
        img_back = findViewById(R.id.img_back);
        tvCategoryHeader = findViewById(R.id.tvCategoryHeader);
        tvCategoryHeader.setText("Sales History");
        recyclerView = findViewById(R.id.recyclerview);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        fetchOrderTable();


    }

    private void fetchOrderTable(){
        String query_doc = "SELECT * FROM OrderTable ORDER BY CreatedTime DESC";//
        cursor_ord = db.rawQuery(query_doc,null);
        cursor_ord.moveToFirst();
        //String catShow = cursor_doc.getString(0) ;

        if(cursor_ord.getCount()>0) {
            //tv_nodata.setVisibility(View.GONE);
            for (int i = 0; i < cursor_ord.getCount(); i++) {

                HashMap<String, String> map = new HashMap<>();
                map.put("OrdId", cursor_ord.getString(0));
                map.put("UniqueKey", cursor_ord.getString(1));
                map.put("OrderId", cursor_ord.getString(2));
                map.put("TotalPrice", cursor_ord.getString(3));
                map.put("Price", cursor_ord.getString(10)+"#"+cursor_ord.getString(4));
                map.put("AmountPayable", cursor_ord.getString(4));
                map.put("TotalItem", cursor_ord.getString(5));
                map.put("Discount", cursor_ord.getString(6));
                map.put("User", cursor_ord.getString(7));
                map.put("CancelOrder", cursor_ord.getString(9));
                map.put("CreatedTime", cursor_ord.getString(10));
                map.put("Date", cursor_ord.getString(10));
                map.put("Time", timeConversion(cursor_ord.getString(10)));

                arrayList_order.add(map);

                cursor_ord.moveToNext();
            }

        }else{
            //tv_nodata.setVisibility(View.VISIBLE);
        }

        if(arrayList_order.size()>0){
            populateRV();
        }else{
            Toast.makeText(HistoryActivity.this, "History Empty" , Toast.LENGTH_SHORT).show();
        }

    }


    private void populateRV(){
        final ReceiptRVadapter adapter = new ReceiptRVadapter();
        adapter.arr_addAll(arrayList_order);
        recyclerView.setAdapter(adapter);

        // Set layout manager
        int orientation = getLayoutManagerOrientation(getResources().getConfiguration().orientation);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(HistoryActivity.this, orientation,false);
        recyclerView.setLayoutManager(layoutManager);

        // Add the sticky headers decoration
        final StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(adapter);
        recyclerView.addItemDecoration(headersDecor);

        // Add decoration for dividers between list items
        recyclerView.addItemDecoration(new ReceiptDividerDecoration(HistoryActivity.this));

        StickyRecyclerHeadersTouchListener touchListener =
                new StickyRecyclerHeadersTouchListener(recyclerView, headersDecor);

        recyclerView.addOnItemTouchListener(touchListener);
        recyclerView.addOnItemTouchListener(new ReceiptClickListener(HistoryActivity.this,
                new ReceiptClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        String uniqueKey = arrayList_order.get(position).get("UniqueKey");
                        ArrayList<HashMap<String , String >> arr = fetchHistoryTable(uniqueKey);

                        clearBackStack();
                        fragment=new FragmentReceiptDetails();
                        fragmentManager=getSupportFragmentManager();
                        fragmentTransaction=fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame,fragment);
                        fragmentTransaction.addToBackStack(null);
                        Bundle bundle = new Bundle();
                        bundle.putInt("position", position);
                        bundle.putSerializable("arr_ord",arrayList_order.get(position));
                        bundle.putSerializable("arr_his",arr);
                        fragment.setArguments(bundle);
                        fragmentTransaction.commit();
                    }
                }));

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                headersDecor.invalidateHeaders();
            }
        });
    }


    private class ReceiptRVadapter extends com.pos.olympia.Adapters.ReceiptRVadapter
            implements StickyRecyclerHeadersAdapter<ReceiptRVadapter.HeaderHolder> {

        @Override
        public long getHeaderId(int position) {
            String[] namesList = getItemDate(position).split(" ");
            String first = namesList[0];
            Log.e("TAG" , "getHeaderId ----------------------------------------------------");
            Log.e("TAG", "part1 "+namesList[0]);
            Log.e("TAG", "part2 "+namesList[1]);
            Log.e("TAG" , "---------------------------------------------------------------");

            String amt = getOnlyAmount(position);
            Log.e("TAG", "part3 "+amt);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

            long diff = 0;
            try {
                diff = printDifference(dateFormat.parse(first));
            } catch (ParseException e) {
                e.printStackTrace();
            }


            return diff;
        }

        public long printDifference(Date endDate) {
            //milliseconds
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date startDate = null;
            try {
                startDate = dateFormat.parse("2018-01-01");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long different = endDate.getTime() - startDate.getTime();

            return different;

        }

        @Override
        public ReceiptRVadapter.HeaderHolder onCreateHeaderViewHolder(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_header_layout, parent, false);
            Log.e("TAG" , "onCreateHeaderViewHolder----------------------------------------");
            return new ReceiptRVadapter.HeaderHolder(view) {
            };
        }

        @Override
        public void onBindHeaderViewHolder(ReceiptRVadapter.HeaderHolder holder, int position) {
            //holder.date.setText(String.valueOf(getItemAmount(position)).charAt(0));
            Log.e("TAG" , "onBindHeaderViewHolder-----------------------------------------");
            String s = String.valueOf(getItemAmount(position));
            String[] namesList = s.split("#");
            Log.e("TAG", "part1 "+namesList[0]);
            Log.e("TAG", "part2 "+namesList[1]);
            Log.e("TAG" , "---------------------------------------------------------------");
            //Log.e("TAG" , "tot amnt --"+getTotalAmountPerDay());


            holder.date.setText(parseDate(namesList[0]));

            int val =getTotalAmount(position,parseDate(namesList[0]));

            holder.receipt.setText("Total: Rs. "+val);
            //holder.receipt.setText("");
        }

        private int getRandomColor() {
            SecureRandom rgen = new SecureRandom();
            return Color.HSVToColor(150, new float[]{
                    rgen.nextInt(359), 1, 1
            });
        }

        public class HeaderHolder extends RecyclerView.ViewHolder {
            TextView date;
            TextView receipt;

            public HeaderHolder(View itemView) {
                super(itemView);
                date= itemView.findViewById(R.id.date);
                receipt= itemView.findViewById(R.id.receipt);
            }
        }
    }


    private ArrayList<HashMap<String, String>> fetchHistoryTable(String uniqueKey){
        ArrayList<HashMap<String, String>> arrayList_history = new ArrayList<>();
        String query_doc = "SELECT * FROM HistoryTable where UniqueKey like '"+uniqueKey+"'";
        // ORDER BY CreatedTime DESC
        cursor_his = db.rawQuery(query_doc,null);
        cursor_his.moveToFirst();
        //String catShow = cursor_doc.getString(0) ;

        if(cursor_his.getCount()>0) {
            //tv_nodata.setVisibility(View.GONE);
            for (int i = 0; i < cursor_his.getCount(); i++) {

                HashMap<String, String> map = new HashMap<>();
                map.put("HistoryId", cursor_his.getString(0));
                map.put("UniqueKey", cursor_his.getString(1));
                map.put("OrderId", cursor_his.getString(2));
                map.put("ItemId", cursor_his.getString(3));
                map.put("ItemName", cursor_his.getString(4));
                map.put("Price", cursor_his.getString(5));
                map.put("Quantity", cursor_his.getString(6));
                map.put("DateTime", cursor_his.getString(7));
                map.put("CancelOrder", cursor_his.getString(8));
                map.put("CreatedTime", dateConversion(cursor_his.getString(9)));

                arrayList_history.add(map);

                cursor_his.moveToNext();
            }

        }else{
            //tv_nodata.setVisibility(View.VISIBLE);
        }

        return arrayList_history;
    }


    private int getLayoutManagerOrientation(int activityOrientation) {
        if (activityOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            return LinearLayoutManager.VERTICAL;
        } else {
            return LinearLayoutManager.HORIZONTAL;
        }
    }

    private void clearBackStack() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }



    public String timeConversion(String date){
        SimpleDateFormat spf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date newDate= null;
        try {
            newDate = spf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        spf= new SimpleDateFormat("hh:mm aaa", Locale.ENGLISH);
        date = spf.format(newDate);
        return date;
    }


    public String parseDate(String time) {
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
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

    public String dateConversion(String date){
        SimpleDateFormat spf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date newDate= null;
        try {
            newDate = spf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        spf= new SimpleDateFormat("EEE, MMM dd, yy", Locale.ENGLISH);
        date = spf.format(newDate);
        return date;
    }

}
