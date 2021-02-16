package com.pos.olympia.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pos.olympia.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;



/**
 * Adapter holding a list of animal names of type String. Note that each item must be unique.
 */
public abstract class ReceiptRVadapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<ReceiptRVadapter.ItemHolder> {

  private ArrayList<HashMap<String, String>> arr_items = new ArrayList<>();
  private ArrayList<ArrayList<HashMap<String, String>>> arr_total = new ArrayList<>();


  public ReceiptRVadapter() {
    setHasStableIds(true);
  }

  @Override
  public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_layout, parent, false);
    return new ReceiptRVadapter.ItemHolder(view) {};
  }

  @Override
  public void onBindViewHolder(ReceiptRVadapter.ItemHolder holder, int position) {
    holder.amount.setText(getOnlyAmount(position));
    holder.time.setText(getItemTime(position));

    if(arr_items.get(position).get("CancelOrder").equals("yes")){
      holder.item_cancel.setText("[Cancelled]");
    }else{
      holder.item_cancel.setText("");
    }
  }

  public void arr_addAllC(Collection<? extends HashMap<String, String>> collection) {
    if (collection != null) {
      arr_items.addAll(collection);
      notifyDataSetChanged();
    }
  }

  public void arr_addAll(ArrayList<HashMap<String, String>> items) {
    arr_addAllC(items);


   /* arrayList_order_short.clear();
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
    }*/
  }

  public void clear() {
    arr_items.clear();
    notifyDataSetChanged();
  }

  public void remove(String object) {
    arr_items.remove(object);
    notifyDataSetChanged();
  }

  public String getItemAmount(int position) {
    return arr_items.get(position).get("Price");
  }

  public int getTotalAmount(int position,String d) {
    int total=0;


    for(int i=0;i<arr_items.size();i++){
      String s = parseDate(arr_items.get(i).get("CreatedTime"));
      String cancel = arr_items.get(i).get("CancelOrder");

      if (s.equals(d) && cancel.equals("no")) {

        float val = Float.parseFloat(arr_items.get(i).get("AmountPayable"));
        total = (int) (total+val);

      }
    }



    return total;
  }


  public String getOnlyAmount(int position) {
    String s = arr_items.get(position).get("Price");
    String[] namesList = s.split("#");
    //holder.date.setText(namesList[0]);
    //namesList[0] -- date
    //namesList[1] -- price
    return namesList[1];
  }



  public String getItemTime(int position) {
    return arr_items.get(position).get("Time");
  }


  public String getItemDate(int position) {
    return arr_items.get(position).get("Date");
  }

  /*public String getItemDateFormated(int position) {
    return arr_items.get(position).get("Ddate");
  }*/

  @Override
  public long getItemId(int position) {
    return getItemAmount(position).hashCode();
  }

  @Override
  public int getItemCount() {
    return arr_items.size();
  }

  public class ItemHolder extends RecyclerView.ViewHolder {
    TextView amount, item_cancel, time;

    public ItemHolder(View itemView) {
      super(itemView);
      amount= itemView.findViewById(R.id.item_amount);
      time= itemView.findViewById(R.id.item_time);
      item_cancel= itemView.findViewById(R.id.item_cancel);
    }

  }

  public String parseDate(String time) {
    String inputPattern = "yyyy-MM-dd HH:mm:ss";
    String outputPattern = "dd MMM, yyyy";
    SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
    SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

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
