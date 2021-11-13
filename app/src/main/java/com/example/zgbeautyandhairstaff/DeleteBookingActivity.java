package com.example.zgbeautyandhairstaff;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Interface.IBookingInfoLoadListener;
import com.example.zgbeautyandhairstaff.Interface.IBookingInformationChangeListener;
import com.example.zgbeautyandhairstaff.Model.BookingDelete;
import com.example.zgbeautyandhairstaff.Model.BookingInformation;
import com.example.zgbeautyandhairstaff.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;


import static com.example.zgbeautyandhairstaff.Common.Common.currentBookingInformation;


public class DeleteBookingActivity extends AppCompatActivity implements IBookingInfoLoadListener {
    Unbinder unbinder;
    AlertDialog dialog;

    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;

    @BindView(R.id.txt_customer_phone)
    TextView txt_customer_phone;

    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;

    @BindView(R.id.txt_time)
    TextView txt_time;

    Button btn_delete_booking;

    @BindView(R.id.card_booking_info)
    CardView card_booking_info;

    public void deleteBooking(View view) {
        //Show dialog confirm
        androidx.appcompat.app.AlertDialog.Builder confirmDialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("היי!")
                .setMessage("האם למחוק את התור של "+" " + currentBookingInformation.getCustomerName())
                .setNegativeButton("ביטול", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }).setPositiveButton("מאשרת", (dialogInterface, i) -> {
                    deleteBookingFromBarber(true);
                });
        confirmDialog.show();
    }

    private void deleteBookingFromBarber(boolean isChange) {
        //we need load Common.currentBooking because we need some data from BookingInformation
        if (currentBookingInformation != null)
        {
            dialog.show();

            //get booking information in barber project
            DocumentReference barberBookingInfo = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(currentBookingInformation.getCityBook())
                    .collection("Branch")
                    .document(currentBookingInformation.getSalonId())
                    .collection("Barbers")
                    .document(currentBookingInformation.getBarberId())
                    .collection(currentBookingInformation.getBookingDate())
                    .document(String.valueOf(currentBookingInformation.getSlot()));

            //When we document, just delete it
            barberBookingInfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(DeleteBookingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //After delete on barber done
                    //we will start delete from User
                    deleteBookingFromUser(isChange, currentBookingInformation.getBookingTime());
                }
            });

        }
        else
        {
            Toast toast = new Toast(DeleteBookingActivity.this);
            toast.setDuration(Toast.LENGTH_LONG);
            View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
            TextView textView = toastView.findViewById(R.id.txt_message);
            toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
            textView.setText("Current Booking must be null");
            toast.setView(toastView);
            toast.show();
        }
    }

    private void deleteBookingFromUser(boolean isChange, String bookingDate) {
        if (!TextUtils.isEmpty(Common.currentBookingId.getBookingId()))
        {

            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(Common.currentUser.getPhoneNumber())
                    .collection("Booking")
                    //.document(currentBookingId.getBookingId());
                    .whereEqualTo("bookingDate", bookingDate)
                    .get().addOnCompleteListener( new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot deleteSnapshot = task.getResult().getDocuments().get(0);

                        String Id = deleteSnapshot.getId();

                        DocumentReference userBookingInfo = FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(Common.currentUser.getPhoneNumber())
                                .collection("Booking")
                                .document(Id);

                        userBookingInfo.delete().addOnFailureListener( new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText( DeleteBookingActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
                            }
                        }).addOnSuccessListener(aVoid -> {

                            Paper.init(DeleteBookingActivity.this);
                            if(Paper.book().read(Common.EVENT_URI_CACHE) != null)
                            {
                                String eventString = Paper.book().read(Common.EVENT_URI_CACHE).toString();
                                Uri eventUri = null;
                                if (eventString != null && !TextUtils.isEmpty(eventString))
                                    eventUri = Uri.parse(eventString);
                                if (eventUri != null)
                                    DeleteBookingActivity.this.getContentResolver().delete(eventUri, null, null);
                            }

                            Toast toast = new Toast(DeleteBookingActivity.this);
                            toast.setDuration(Toast.LENGTH_LONG);
                            View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                            TextView textView = toastView.findViewById(R.id.txt_message);
                            toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                            textView.setText("תור נמחק בהצלחה!");
                            toast.setView(toastView);
                            toast.show();

                            loadUserBooking();

                            Intent intent = new Intent(DeleteBookingActivity.this, BookingActivity.class);
                            startActivity(intent);

                            dialog.dismiss();
                        });

                    }
                }
            });
        }
        else
            {
                dialog.dismiss();

                Toast toast = new Toast(DeleteBookingActivity.this);
                toast.setDuration(Toast.LENGTH_LONG);
                View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                TextView textView = toastView.findViewById(R.id.txt_message);
                toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                textView.setText("Booking information empty");
                toast.setView(toastView);
                toast.show();
            }
    }

    IBookingInfoLoadListener iBookingInfoLoadListener;

    IBookingInformationChangeListener iBookingInformationChangeListener;

    ListenerRegistration userBookingListener = null;
    EventListener<QuerySnapshot> userBookingEvent = null;

    @Override
    public void onResume() {
        super.onResume();
        loadUserBooking();
        loadBookingId();
    }

    private void loadBookingId() {
        FirebaseFirestore.getInstance().collection("Users")
                .document(currentBookingInformation.getCustomerPhone())
                .collection("Booking")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot bookingSnapshot:task.getResult()) {

                                BookingDelete booking = bookingSnapshot.toObject(BookingDelete.class);

                                Common.currentBookingId = booking;

                                booking.setBookingId(bookingSnapshot.getId());

                            }
                        }
                    }
                });
    }

    private void loadUserId() {
        FirebaseFirestore.getInstance().collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot userSnapshot : task.getResult()) {
                                User user = userSnapshot.toObject(User.class);
                                Common.currentUser = user;
                                user.setId(userSnapshot.getId());

                            }
                        }
                    }
                });
    }

    private void loadUserBooking() {
        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentBookingInformation.getCustomerPhone())
                .collection("Booking");

        //Get Current date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,0);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);

        Timestamp toDayTimeStamp = new Timestamp(calendar.getTime());

        //Select booking information from Firebase with done=false and timestamp greater today
        userBooking
                .whereGreaterThanOrEqualTo("timestamp",toDayTimeStamp)
                .whereEqualTo("done",false)
                .limit(1) //only take 1
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful())
                    {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult())
                            {
                                BookingInformation bookingInformation = queryDocumentSnapshot.toObject(BookingInformation.class);
                                iBookingInfoLoadListener.onBookingInfoLoadSuccess(bookingInformation, queryDocumentSnapshot.getId());

                                //Exit loop as soon as
                                break;
                            }
                        }
                        else
                            iBookingInfoLoadListener.onBookingInfoLoadEmpty();
                    }
                }).addOnFailureListener(e ->
                iBookingInfoLoadListener.onBookingInfoLoadFailed(e.getMessage()

                ));

        //Here, after userBooking has been assign data (collections)
        //we will make realtime listen here
        //if userBookingEvent already init
        if (userBookingEvent != null)
        {
            if (userBookingListener == null) //only add if userBookingListener ==null
            {
                //That mean we just add 1 time
                userBookingListener = userBooking.addSnapshotListener(userBookingEvent);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_delete_booking);

        View itemView = LayoutInflater.from(this).inflate(R.layout.activity_delete_booking, null);

        iBookingInfoLoadListener = this;

        btn_delete_booking = findViewById(R.id.btn_delete_booking);
        txt_time = findViewById(R.id.txt_time);
        txt_customer_name = findViewById(R.id.txt_customer_name);
        txt_customer_phone = findViewById(R.id.txt_customer_phone);
        txt_salon_name = findViewById(R.id.txt_salon_name);

        unbinder = ButterKnife.bind(this, itemView);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        txt_salon_name.setText(currentBookingInformation.getSalonName());
        txt_customer_name.setText(currentBookingInformation.getCustomerName());
        txt_customer_phone.setText(currentBookingInformation.getCustomerPhone());
        txt_time.setText(Common.convertTimeSlotToString(currentBookingInformation.getSlot().intValue()));

        loadUserId();

        builder.setView(itemView);
        builder.setCancelable(true);
        builder.show().getWindow().setBackgroundDrawable(new ColorDrawable(0));

        //builder.show();

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
            loadUserBooking();
            initRealtimeUserBooking();  //Need declare above loadUserbooking
        }
    }

    private void initRealtimeUserBooking() {
        if(userBookingEvent == null) //we only init event is null
        {
            userBookingEvent = new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    //In this event, when it fired, we will call loadUserBooking again
                    //to reload all booking information
                    loadUserBooking();
                }
            };
        }
    }

    @Override
    public void onDestroy() {
        if(userBookingListener != null)
            userBookingListener.remove();
        super.onDestroy();
    }

    @Override
    public void onBookingInfoLoadEmpty() {
        card_booking_info.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBookingInfoLoadSuccess(BookingInformation bookingInformation, String bookingId) {
        Common.currentBookingInformation = bookingInformation;

    }

    @Override
    public void onBookingInfoLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void back(View view) {
        startActivity(new Intent(DeleteBookingActivity.this, StaffHomeActivity.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}