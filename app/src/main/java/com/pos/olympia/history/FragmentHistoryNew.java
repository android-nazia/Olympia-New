package com.pos.olympia.history;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pos.olympia.R;
import com.pos.olympia.db.DatabaseHandler;

public class FragmentHistoryNew extends Fragment implements View.OnClickListener {
    TextView tvCategoryHeader;
    Toolbar toolbar;
    Button btn_1, btn_2, btn_3;
    DatabaseHandler handler;
    SQLiteDatabase db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar = getActivity().findViewById(R.id.toolbar);
        handler = new DatabaseHandler(getActivity());
        db = handler.getWritableDatabase();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate( R.layout.frag_history_new,container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        setViews(view);
        return view;
    }

    private void setViews(View view){

        btn_1 = view.findViewById(R.id.btn_1);
        btn_2 = view.findViewById(R.id.btn_2);
        btn_3 = view.findViewById(R.id.btn_3);
        btn_1.setOnClickListener(this);
        btn_2.setOnClickListener(this);
        btn_3.setOnClickListener(this);


        tvCategoryHeader = toolbar.findViewById(R.id.tvCategoryHeader);
        tvCategoryHeader.setText("History");
        /*img_cart = toolbar.findViewById(R.id.img_cart);
        img_scan = toolbar.findViewById(R.id.img_scan);
        noti_count = toolbar.findViewById(R.id.noti_count);
        img_cart.setVisibility(View.GONE);
        img_scan.setVisibility(View.GONE);
        noti_count.setVisibility(View.GONE);
        recyclerView = view.findViewById(R.id.recyclerview);
        mLayoutmanager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutmanager);
        arrayList_order = new ArrayList<>();
        arrayList_order_short = new ArrayList<>();
        arrayList = new ArrayList<>();

        fetchOrderTable();*/
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.btn_1:
                Intent i1 = new Intent(getActivity(), HistoryActivity.class);
                i1.putExtra("from","1");
                startActivity(i1);
                break;

            case R.id.btn_2:
                Intent i2 = new Intent(getActivity(), HistoryActivity.class);
                i2.putExtra("from","2");
                startActivity(i2);
                break;

            case R.id.btn_3:
                Intent i3 = new Intent(getActivity(), HistoryActivity.class);
                i3.putExtra("from","3");
                startActivity(i3);
                break;
        }
    }

    /*private void fetchOrderTable(){
        String query_doc = "SELECT * FROM OrderTable ORDER BY CreatedTime DESC";//
        cursor_ord = db.rawQuery(query_doc,null);
        cursor_ord.moveToFirst();
        if(cursor_ord.getCount()>0) {
            for (int i = 0; i < cursor_ord.getCount(); i++) {
                HashMap<String, String> map = new HashMap<>();
                map.put("OrdId", cursor_ord.getString(0));
                map.put("UniqueKey", cursor_ord.getString(1));
                map.put("OrderId", cursor_ord.getString(2));
                map.put("TotalPrice", cursor_ord.getString(3));
                map.put("AmountPayable", cursor_ord.getString(4));
                map.put("TotalItem", cursor_ord.getString(5));
                map.put("Discount", cursor_ord.getString(6));
                map.put("User", cursor_ord.getString(7));
                map.put("DateTime", cursor_ord.getString(8));
                map.put("CreatedTime", cursor_ord.getString(9));
                map.put("Time", parseDate(cursor_ord.getString(9)));
                arrayList_order.add(map);
                cursor_ord.moveToNext();
            }

            arrayList_order_short.clear();
            arrayList_order_short.add(arrayList_order.get(0));
            arrayList.add(arrayList_order);

            for (int i = 0; i < arrayList_order.size(); i++) {
                int u =0;
                for (int f = 0; f < arrayList_order_short.size(); f++) {
                    if(arrayList_order.get(i).get("DateTime")
                            .equals(arrayList_order_short.get(f).get("DateTime"))) {
                        u=0;
                    }else{
                        u=1;
                    }
                }
                if(u==1){
                    arrayList_order_short.add(arrayList_order.get(i));
                }
            }
        }else{
            //tv_nodata.setVisibility(View.VISIBLE);
        }
        if(arrayList_order_short.size()>0){
            populateRV();
        }else{
            Toast.makeText(getActivity(), "History Empty" , Toast.LENGTH_SHORT).show();
        }

    }

    private void populateRV(){
        AdapterHistory mAdapter = new AdapterHistory(getContext(),arrayList_order,
                arrayList_order_short);
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mAdapter.setOnClick(FragmentHistoryNew.this);
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


    @Override
    public void OnItemClick(View v, int position) {

    }*/
}
