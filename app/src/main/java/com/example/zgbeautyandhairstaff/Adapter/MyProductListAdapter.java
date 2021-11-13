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
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Interface.IRecyclerClickListener;
import com.example.zgbeautyandhairstaff.Model.ShoppingItem;
import com.example.zgbeautyandhairstaff.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyProductListAdapter extends RecyclerView.Adapter<MyProductListAdapter.MyViewHolder> {

    Context context;
    List<ShoppingItem> shoppingItemList;

    public MyProductListAdapter(Context context, List<ShoppingItem> shoppingItemList) {
        this.context = context;
        this.shoppingItemList = shoppingItemList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_product_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ShoppingItem shoppingItem = shoppingItemList.get(position);


        Glide.with(context).load(shoppingItem.getImage())
                .into(holder.img_product_image);

        holder.txt_product_name.setText(new StringBuilder("").append(shoppingItem.getName()));

        holder.txt_product_price.setText(new StringBuilder("₪").append(shoppingItem.getPrice()));

        //Event
        holder.setListener((view, pos) -> {
            Common.selectedItem = shoppingItemList.get(pos);
            Common.selectedItem.setId(shoppingItemList.get(pos).getId());

        });
    }

    @Override
    public int getItemCount() {
        return shoppingItemList == null ? 0 :shoppingItemList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;

        @BindView(R.id.img_product_image)
        ImageView img_product_image;

        @BindView(R.id.txt_product_name)
        TextView txt_product_name;

        @BindView(R.id.txt_product_price)
        TextView txt_product_price;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            listener.onItemClickListener(view, getAdapterPosition());
        }
    }
}
