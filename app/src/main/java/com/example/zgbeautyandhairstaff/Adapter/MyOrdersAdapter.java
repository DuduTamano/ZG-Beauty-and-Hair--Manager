package com.example.zgbeautyandhairstaff.Adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Interface.IRecyclerItemSelectedListener;
import com.example.zgbeautyandhairstaff.Model.CartItem;
import com.example.zgbeautyandhairstaff.Model.Order;
import com.example.zgbeautyandhairstaff.R;
import com.example.zgbeautyandhairstaff.Retrofit.IFCMService;
import com.example.zgbeautyandhairstaff.Retrofit.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;

public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.MyViewHolder> {
    private Context context;
    private List<Order> orderList;
    private SimpleDateFormat simpleDateFormat;
    AlertDialog dialog;
    private IFCMService ifcmService;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Calendar calendar;


    public MyOrdersAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        calendar = Calendar.getInstance();
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_orders_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ifcmService = RetrofitClient.getInstance().create(IFCMService.class);

        dialog = new SpotsDialog.Builder().setContext(context)
                .setCancelable(false).build();

        calendar.setTimeInMillis(orderList.get(position).getCreateDate());
        Date date = new Date(orderList.get(position).getCreateDate());

        holder.txt_order_number.setText(orderList.get(position).getKey());

       // holder.txt_order_time.setText(new StringBuilder());

        Common.setSpanStringColor(" ", simpleDateFormat.format(new Date(orderList.get(position).getTimestamp().toDate().toString())),
                holder.txt_order_time, Color.parseColor("#333639"));

        holder.txt_order_status.setText(Common.convertStatusToString(orderList.get(position).getOrderStatus()));

        holder.txt_customer_name.setText(orderList.get(position).getUserName());


                holder.txt_order_number.setText(orderList.get(position).getOrderNumber());

        holder.txt_order_items.setText(orderList.get(position).getCartItemList() == null ? "0":
                String.valueOf(orderList.get(position).getCartItemList().size()));

        holder.setRecyclerItemSelectedListener((view, pos) ->

                showDialog(orderList.get(pos).getCartItemList())
        );
    }

    private void showDialog(List<CartItem> cartItemList) {
        View layout_dialog = LayoutInflater.from(context).inflate(R.layout.layout_dialog_order_detail, null);

        Dialog builder = new Dialog(context, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        builder.setContentView(layout_dialog);

        Button btn_ok = (Button) layout_dialog.findViewById(R.id.btn_ok);

        RecyclerView recycler_order_detail = (RecyclerView) layout_dialog.findViewById(R.id.recycler_order_detail);
        recycler_order_detail.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recycler_order_detail.setLayoutManager(layoutManager);

        MyOrderDetailAdapter myOrderDetailAdapter = new MyOrderDetailAdapter(context, cartItemList);
        recycler_order_detail.setAdapter(myOrderDetailAdapter);


        //Show dialog
        builder.create();
        builder.show();

        btn_ok.setOnClickListener( v -> builder.dismiss() );

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public Order getItemPosition(int pos) {
        return orderList.get(pos);
    }

    public void removeItem(int pos) {
        notifyItemRemoved(pos);
        orderList.clear();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txt_order_status)
        TextView txt_order_status;

        @BindView(R.id.txt_customer_name)
        TextView txt_customer_name;

        @BindView(R.id.txt_order_number)
        TextView txt_order_number;

        @BindView(R.id.txt_order_time)
        TextView txt_order_time;

        @BindView(R.id.txt_order_items)
        TextView txt_order_items;

        Unbinder unbinder;

        IRecyclerItemSelectedListener recyclerItemSelectedListener;

        public void setRecyclerItemSelectedListener(IRecyclerItemSelectedListener recyclerItemSelectedListener) {
            this.recyclerItemSelectedListener = recyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            recyclerItemSelectedListener.onItemSelected(view, getAdapterPosition());
        }
    }
}
