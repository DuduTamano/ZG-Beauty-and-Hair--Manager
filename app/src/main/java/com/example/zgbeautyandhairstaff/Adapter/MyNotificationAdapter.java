package com.example.zgbeautyandhairstaff.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zgbeautyandhairstaff.BookingActivity;
import com.example.zgbeautyandhairstaff.Common.MyDiffCallBack;
import com.example.zgbeautyandhairstaff.Model.MyNotification;
import com.example.zgbeautyandhairstaff.OrdersActivity;
import com.example.zgbeautyandhairstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyNotificationAdapter extends RecyclerView.Adapter<MyNotificationAdapter.MyViewHolder> {
    Context context;
    List<MyNotification> myNotificationList;
    private SimpleDateFormat simpleDateFormat1;
    private SimpleDateFormat simpleDateFormat2;
    FirebaseFirestore firebaseFirestore;

    public MyNotificationAdapter(Context context, List<MyNotification> myNotificationList) {
        this.context = context;
        this.myNotificationList = myNotificationList;
        simpleDateFormat1 = new SimpleDateFormat("HH:mm:ss");
        simpleDateFormat2 = new SimpleDateFormat("dd-MM-yyyy");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_notification_item,viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, @SuppressLint("RecyclerView") int i) {

        myViewHolder.txt_notification_title.setText(myNotificationList.get(i).getTitle());
        myViewHolder.txt_notification_content.setText(myNotificationList.get(i).getContent());

        myViewHolder.txt_notification_time.setText(new StringBuilder().append("בתאריך: ").append(simpleDateFormat2.format(new Date(myNotificationList.get(i).getServerTimestamp().toDate().toString())))
        .append(" בשעה: ").append(simpleDateFormat1.format(new Date(myNotificationList.get(i).getServerTimestamp().toDate().toString()))));

        Picasso.get().load(myNotificationList.get(i).getImage()).into(myViewHolder.userImage);
    }

    @Override
    public int getItemCount() {
        return myNotificationList.size();
    }

    public void updateList(List<MyNotification> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MyDiffCallBack(this.myNotificationList,newList));
        myNotificationList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder  {
        @BindView(R.id.txt_notification_title)
        TextView txt_notification_title;

        @BindView(R.id.txt_notification_content)
        TextView txt_notification_content;

        @BindView(R.id.txt_notification_time)
        TextView txt_notification_time;

        @BindView(R.id.userImage)
        ImageView userImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

            firebaseFirestore = FirebaseFirestore.getInstance();
        }
    }
}