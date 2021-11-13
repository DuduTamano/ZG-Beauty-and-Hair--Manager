package com.example.zgbeautyandhairstaff.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.example.zgbeautyandhairstaff.Model.Category;
import com.example.zgbeautyandhairstaff.ProductListActivity;
import com.example.zgbeautyandhairstaff.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCategoriesAdapter extends RecyclerView.Adapter<MyCategoriesAdapter.MyViewHolder> {

    Context context;
    List<Category> categoryList;

    public MyCategoriesAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_category_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Glide.with(context).load(categoryList.get(position).getImage())
                .into(holder.category_image);
        holder.category_name.setText(new StringBuilder(categoryList.get(position).getName()));


        holder.itemView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent( v.getContext(), ProductListActivity.class );

                Common.categorySelected = categoryList.get(position);
                Common.categorySelected.setItems(categoryList.get(position).getItems());


                v.getContext().startActivity( intent );
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList  == null ? 0 :categoryList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;

        @BindView(R.id.img_category)
        ImageView category_image;

        @BindView(R.id.text_category)
        TextView category_name;

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

    @Override
    public int getItemViewType(int position) {
        if (categoryList.size() == 1)
            return Common.DEFAULT_COLUMN_COUNT;

        else {
            if (categoryList.size() % 2 == 0)
                return Common.DEFAULT_COLUMN_COUNT;

            else
                return (position > 1 && position == categoryList.size() -1) ? Common.FULL_WIDTH_COLUMN : Common.DEFAULT_COLUMN_COUNT;
        }

    }
}
