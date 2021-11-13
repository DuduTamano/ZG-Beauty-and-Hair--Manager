package com.example.zgbeautyandhairstaff.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.DeleteBookingActivity;
import com.example.zgbeautyandhairstaff.DoneServiceActivity;
import com.example.zgbeautyandhairstaff.Interface.IRecyclerItemSelectedListener;
import com.example.zgbeautyandhairstaff.Model.BookingInformation;
import com.example.zgbeautyandhairstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<BookingInformation> timeSlotList;
    List<CardView> cardViewList;
    Dialog dialog;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        cardViewList = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<BookingInformation> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        cardViewList = new ArrayList<>();
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_time_slot,viewGroup,false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {
        myViewHolder.txt_time_slot.setText(new StringBuilder(Common.convertTimeSlotToString(i)).toString());
        if (timeSlotList.size() == 0) {

            myViewHolder.txt_time_slot_description.setText("פנוי");
            myViewHolder.txt_time_slot_description.setTextColor(context.getResources().getColor(android.R.color.black, null));
            myViewHolder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black, null));
            myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.white, null));


            //Add Event nothing
            myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                @Override
                public void onItemSelected(View view, int position) {
                    //Fix crush if we add this function
                }
            });
        }
        else
        {
            for (BookingInformation slotValue:timeSlotList) {
                int slot = Integer.parseInt(slotValue.getSlot().toString());
                if (slot == i) {

                    myViewHolder.card_time_slot.setTag(Common.DISABLE_TAG);
                    if (!slotValue.isDone()) {

                        myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                        myViewHolder.txt_time_slot_description.setText("מלא");
                        myViewHolder.txt_time_slot_description.setTextColor(context.getResources()
                                .getColor(android.R.color.white));
                        myViewHolder.txt_time_slot.setTextColor(context.getResources()
                                .getColor(android.R.color.white));

                        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelected(View view, int position) {
                                //only add for gray time slot
                                //here we will get booking information and store in Common.currentBookingInformation
                                //after that, start DoneServicesActivity

                                FirebaseFirestore.getInstance()
                                        .collection("AllSalon")
                                        .document(Common.state_name)
                                        .collection("Branch")
                                        .document(Common.selected_salon.getSalonId())
                                        .collection("Barbers")
                                        .document(Common.currentBarber.getBarberId())
                                        .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                                        .document(slotValue.getSlot().toString())
                                        .get()
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful())
                                        {
                                            if (task.getResult().exists())
                                            {
                                                Common.currentBookingInformation = task.getResult().toObject(BookingInformation.class);
                                                Common.currentBookingInformation.setBookingId(task.getResult().getId());
                                                context.startActivity(new Intent(context, DoneServiceActivity.class));
                                            }
                                        }
                                    }
                                });

                            }

                        });

                    }
                    else
                    {
                        myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                        myViewHolder.txt_time_slot_description.setText("תור נקבע");
                        myViewHolder.txt_time_slot_description.setTextColor(context.getResources()
                                .getColor(android.R.color.white));
                        myViewHolder.txt_time_slot.setTextColor(context.getResources()
                                .getColor(android.R.color.white));

                        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelected(View view, int position) {
                                //add here to fix crash
                                // Intent intent = new Intent (view.getContext(), DeleteBookingActivity.class);

                                //   view.getContext().startActivity(intent);

                                //only add for gray time slot
                                //here we will get booking information and store in Common.currentBookingInformation
                                //after that, start DoneServicesActivity

                                FirebaseFirestore.getInstance()
                                        .collection("AllSalon")
                                        .document(Common.state_name)
                                        .collection("Branch")
                                        .document(Common.selected_salon.getSalonId())
                                        .collection("Barbers")
                                        .document(Common.currentBarber.getBarberId())
                                        .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                                        .document(slotValue.getSlot().toString())
                                        .get()
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful())
                                        {
                                            if (task.getResult().exists())
                                            {
                                                Intent intent = new Intent (context, DeleteBookingActivity.class);
                                                Common.currentBookingInformation = task.getResult().toObject(BookingInformation.class);

                                                context.startActivity(intent);
                                            }
                                        }
                                    }
                                });

                            }
                        });

                    }
                }
                else
                {
                    //Fix crash
                    if (myViewHolder.getiRecyclerItemSelectedListener() == null)
                    {
                        //we only add event for view holder with is not implement click
                        //because if we don't put this if condition
                        //all time slot with slot value higher current time slot will be override event

                        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelected(View view, int postion) {

                            }
                        });
                    }
                }
            }
        }

        //Add only all available time slot card
        if (!cardViewList.contains(myViewHolder.card_time_slot)) {
            cardViewList.add(myViewHolder.card_time_slot);
        }
    }


    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_time_slot,txt_time_slot_description;
        CardView card_time_slot;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public IRecyclerItemSelectedListener getiRecyclerItemSelectedListener() {
            return iRecyclerItemSelectedListener;
        }

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            card_time_slot = itemView.findViewById(R.id.card_time_slot);
            txt_time_slot = (TextView)itemView.findViewById(R.id.txt_time_slot);
            txt_time_slot_description = (TextView)itemView.findViewById(R.id.txt_time_slot_description);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelected(view,getAdapterPosition());
        }
    }

}
