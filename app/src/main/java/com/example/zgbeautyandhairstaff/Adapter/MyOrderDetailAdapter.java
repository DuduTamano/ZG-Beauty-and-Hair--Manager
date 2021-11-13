package com.example.zgbeautyandhairstaff.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zgbeautyandhairstaff.Model.CartItem;
import com.example.zgbeautyandhairstaff.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderDetailAdapter extends RecyclerView.Adapter<MyOrderDetailAdapter.MyViewHolder> {

    Context context;
    List<CartItem> cartItemList;

    public MyOrderDetailAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_order_detail_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(cartItemList.get(position).getProductImage())
                .into(holder.cart_img);

        holder.txt_item_quantity.setText(new StringBuilder("כמות: ").append(cartItemList.get(position).getProductQuantity()));

        holder.txt_item_name.setText(new StringBuilder().append(cartItemList.get(position).getProductName()));

        holder.txt_item_price.setText(new StringBuilder("₪").append(cartItemList.get(position).getProductPrice()));

    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cart_img)
        ImageView cart_img;

        @BindView(R.id.txt_item_name)
        TextView txt_item_name;

        @BindView(R.id.txt_item_quantity)
        TextView txt_item_quantity;

        @BindView(R.id.txt_item_price)
        TextView txt_item_price;

        private Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
