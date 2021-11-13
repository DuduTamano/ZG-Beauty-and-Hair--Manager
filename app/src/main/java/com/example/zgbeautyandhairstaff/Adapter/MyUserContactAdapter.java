package com.example.zgbeautyandhairstaff.Adapter;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Model.Category;
import com.example.zgbeautyandhairstaff.Model.UserContact;
import com.example.zgbeautyandhairstaff.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyUserContactAdapter extends RecyclerView.Adapter<MyUserContactAdapter.MyViewHolder> {
    Context context;
    List<UserContact> userContactList;
    List<UserContact> filterUserContact;
    private SimpleDateFormat simpleDateFormat;

    public MyUserContactAdapter(Context context, List<UserContact> userContactList) {
        this.context = context;
        this.userContactList = userContactList;
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        filterUserContact = new ArrayList<>();
        filterUserContact.addAll(userContactList);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_user_contact, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Picasso.get().load(userContactList.get(position).getImage() ).into(holder.userImage);

        holder.txt_salon_name.setText(Common.selected_salon.getName());

        holder.txt_customer_address.setText(userContactList.get(position).getAddress());

        holder.txt_customer_name.setText(userContactList.get(position).getFirstName() +" "+ userContactList.get(position).getLastName());

        holder.txt_customer_phone.setText(userContactList.get(position).getPhoneNumber().replaceAll("[+]972", "0"));

        holder.txt_customer_created.setText(simpleDateFormat.format(new Date(userContactList.get(position).getTimestamp().toDate().toString())));
    }

    @Override
    public int getItemCount() {
        return userContactList.size();
    }

    public UserContact getItemPosition(int pos) {
        return userContactList.get(pos);
    }

    public void removeItem(int pos) {
        notifyItemRemoved(pos);
        userContactList.clear();
    }

    public void filter(String text) {
        int loginItUd = text.length();
        if (loginItUd == 0) {
            userContactList.clear();
            userContactList.addAll(filterUserContact);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                List<UserContact> collection = userContactList.stream()
                        .filter(i -> (i.getFirstName() + i.getLastName() + i.getPhoneNumber().replaceAll("[+]972", "0")).toLowerCase().contains(text.toLowerCase()))
                        .collect(Collectors.toList());
                userContactList.clear();
                userContactList.addAll(collection);
            } else {
                for (UserContact contact : userContactList) {
                    if ((contact.getFirstName() + contact.getLastName() + contact.getPhoneNumber().replaceAll("[+]972", "0")).toLowerCase().contains(text.toLowerCase())) {
                        userContactList.add(contact);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        Unbinder unbinder;

        @BindView(R.id.txt_salon_name)
        TextView txt_salon_name;

        @BindView(R.id.txt_customer_name)
        TextView txt_customer_name;

        @BindView(R.id.txt_customer_phone)
        TextView txt_customer_phone;

        @BindView(R.id.txt_customer_address)
        TextView txt_customer_address;

        @BindView(R.id.txt_customer_created)
        TextView txt_customer_created;

        @BindView(R.id.userImage)
        ImageView userImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
