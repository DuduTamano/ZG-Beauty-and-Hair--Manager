package com.example.zgbeautyandhairstaff.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zgbeautyandhairstaff.BookingActivity;
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Model.BookingInformation;
import com.example.zgbeautyandhairstaff.R;
import com.example.zgbeautyandhairstaff.Retrofit.IFCMService;
import com.example.zgbeautyandhairstaff.Retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;

public class MyBookingAdapter extends RecyclerView.Adapter<MyBookingAdapter.MyViewHolder> {
    private Context context;
    private List<BookingInformation> bookingInformationList;
    private SimpleDateFormat simpleDateFormat;
    private IFCMService ifcmService;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    public MyBookingAdapter(Context context, List<BookingInformation> bookingInformationList) {
        this.context = context;
        this.bookingInformationList = bookingInformationList;
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy  HH:mm");

    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_bookings_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ifcmService = RetrofitClient.getInstance().create(IFCMService.class);

        //.replaceAll("[+]972", "0")
        Picasso.get().load(bookingInformationList.get(position).getCustomerImage()).into(holder.userImage);

        holder.txt_customer_name.setText(bookingInformationList.get(position).getCustomerName());
        holder.txt_customer_phone.setText(bookingInformationList.get(position).getCustomerPhone().replaceAll("[+]972", "0"));
        holder.txt_salon_name.setText(bookingInformationList.get(position).getSalonName());
        holder.txt_time.setText(new StringBuilder().append(bookingInformationList.get(position).getBookingDate())
                .append(" בשעה: ").append(bookingInformationList.get(position).getBookingTime()));

        Common.setSpanStringColor("מועד קביעת התור: ", simpleDateFormat.format(new Date(bookingInformationList.get(position).getTimestamp().toDate().toString())),
                holder.txt_booking_time, Color.parseColor("#333639"));

        holder.txt_order_status.setText(new StringBuilder("").append(Common.convertBookingStatusToString(bookingInformationList.get(position).getBookingStatus())));
    }

    @Override
    public int getItemCount() {
        return bookingInformationList  == null ? 0 :bookingInformationList.size();
    }

    public BookingInformation getItemPosition(int pos) {
        return bookingInformationList.get(pos);
    }

    public void removeItem(int pos) {
        notifyItemRemoved(pos);
        bookingInformationList.clear();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.txt_salon_name)
        TextView txt_salon_name;

        @BindView(R.id.txt_order_status)
        TextView txt_order_status;

        @BindView(R.id.txt_booking_time)
        TextView txt_booking_time;

        @BindView(R.id.txt_customer_name)
        TextView txt_customer_name;

        @BindView(R.id.txt_customer_phone)
        TextView txt_customer_phone;

        @BindView(R.id.txt_time)
        TextView txt_time;

        @BindView(R.id.userImage)
        ImageView userImage;

        Unbinder unbinder;

        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            unbinder = ButterKnife.bind(this, itemView);

        }
    }
}
