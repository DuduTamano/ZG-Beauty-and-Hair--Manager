package com.example.zgbeautyandhairstaff;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zgbeautyandhairstaff.Adapter.MyBookingAdapter;
import com.example.zgbeautyandhairstaff.Common.BottomSheetBookingFragment;
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Common.MySwipeHelper;
import com.example.zgbeautyandhairstaff.Model.BookingInformation;
import com.example.zgbeautyandhairstaff.Model.EventBus.ChangeBookingClick;
import com.example.zgbeautyandhairstaff.Model.EventBus.LoadBookingEvent;
import com.example.zgbeautyandhairstaff.Model.FCMSendData;
import com.example.zgbeautyandhairstaff.Model.MyToken;
import com.example.zgbeautyandhairstaff.Retrofit.IFCMService;
import com.example.zgbeautyandhairstaff.Retrofit.RetrofitClient;
import com.example.zgbeautyandhairstaff.ViewModel.ViewBooking;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class BookingActivity extends AppCompatActivity {
    @BindView(R.id.recycler_bookings)
    RecyclerView recycler_bookings;

    ImageView img_booking;

    ImageView action_filter;

    @BindView(R.id.txt_booking_filter)
    TextView txt_booking_filter;

    MyBookingAdapter adapter;

    private IFCMService ifcmService;

    Unbinder unbinder;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    LayoutAnimationController layoutAnimationController;

    private ViewBooking viewBooking;

    android.app.AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        unbinder = ButterKnife.bind(this);

        dialog = new SpotsDialog.Builder().setContext(this)
                .setCancelable(false).build();

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        viewBooking = new ViewModelProvider(this).get(ViewBooking.class);

        img_booking = findViewById(R.id.img_booking);

        action_filter = findViewById(R.id.action_filter);

        initView();

        loadUserId();

        img_booking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BookingActivity.this, CalendarActivity.class));
            }
        });

        viewBooking.getBookingModelMutableLiveData().observe(this, new Observer<List<BookingInformation>>() {
            @Override
            public void onChanged(List<BookingInformation> bookingInformationList) {
                if (bookingInformationList != null) {
                    adapter = new MyBookingAdapter(BookingActivity.this, bookingInformationList);
                    recycler_bookings.setAdapter(adapter);
                    recycler_bookings.setLayoutAnimation(layoutAnimationController);

                    BookingActivity.this.updateTextCounter();
                }
            }
        });

        action_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenFilter();
            }
        });

    }

    private void OpenFilter() {
        BottomSheetBookingFragment bottomSheetBookingFragment = BottomSheetBookingFragment.getInstance();
        bottomSheetBookingFragment.show(this.getSupportFragmentManager(), "Booking filter");
    }

    private void loadUserId() {
        FirebaseFirestore.getInstance().collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot userSnapshot : task.getResult()) {

                                loadBookingId(userSnapshot.getId());

                            }
                        }
                    }
                });
    }

    private void loadBookingId(String userPhone) {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userPhone)
                .collection("Booking")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot bookingSnapshot:task.getResult()) {

                                BookingInformation bookings = bookingSnapshot.toObject(BookingInformation.class);

                                Common.currentBookingInformation = bookings;

                                bookings.setBookingId(bookingSnapshot.getId());

                            }
                        }
                    }
                });
    }

    private void initView() {

        ifcmService = RetrofitClient.getInstance().create(IFCMService.class);

        recycler_bookings.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_bookings.setLayoutManager(layoutManager);
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_item_from_left);

        //Slide options
        DisplayMetrics displayMetrics = new DisplayMetrics();
        BookingActivity.this.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        MySwipeHelper mySwipeHelper = new MySwipeHelper(this, recycler_bookings, width/6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {

                buf.add(new MyButton(BookingActivity.this, "עדכון תור", 30, 0, Color.parseColor("#000000"),
                        pos -> {
                            showEditDialog(adapter.getItemPosition(pos), pos);
                        }));
            }
        };
    }

    private void showEditDialog(BookingInformation bookingInformationModel, int pos) {
        View layout_dialog;
        AlertDialog.Builder builder;

        //waiting to confirm
        if (bookingInformationModel.getBookingStatus() == 0)
        {
            layout_dialog = LayoutInflater.from(BookingActivity.this)
                    .inflate(R.layout.layout_dialog_booking, null);

            builder = new AlertDialog.Builder(this)
                    .setView(layout_dialog);
        }

        else
            layout_dialog = LayoutInflater.from(BookingActivity.this)
                    .inflate(R.layout.layout_booking_deleted, null);

        builder = new AlertDialog.Builder(this)
                .setView(layout_dialog);

        //View
        TextView txt_salon_name = (TextView)layout_dialog.findViewById(R.id.txt_salon_name);
        TextView txt_customer_name = (TextView)layout_dialog.findViewById(R.id.txt_customer_name);
        TextView txt_customer_phone = (TextView)layout_dialog.findViewById(R.id.txt_customer_phone);
        TextView txt_time = (TextView)layout_dialog.findViewById(R.id.txt_time);
        ImageView userImage = (ImageView) layout_dialog.findViewById(R.id.userImage);


        Button btn_confirm_booking = (Button)layout_dialog.findViewById(R.id.btn_confirm_booking);
        Button btn_delete_booking = (Button)layout_dialog.findViewById(R.id.btn_delete_booking);

        TextView txt_status = (TextView)layout_dialog.findViewById(R.id.txt_status);

        //Set data
        txt_status.setText(new StringBuilder("סטטוס הזמנה: ")
                .append(Common.convertBookingStatusToString(bookingInformationModel.getBookingStatus())));

        txt_salon_name.setText(bookingInformationModel.getSalonName());
        txt_customer_name.setText(bookingInformationModel.getCustomerName());
        txt_customer_phone.setText(bookingInformationModel.getCustomerPhone().replaceAll("[+]972", "0"));
        txt_time.setText(new StringBuilder().append(bookingInformationModel.getBookingDate())
                .append(" בשעה: ").append(bookingInformationModel.getBookingTime()));

        Picasso.get().load(bookingInformationModel.getCustomerImage()).into(userImage);


        //Create Dialog
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.show();

        //Custom dialog
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        if (bookingInformationModel.getBookingStatus() == 0)
        {
            btn_confirm_booking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UpdateBookingStatusBarber(pos, bookingInformationModel, 1);
                    UpdateBookingStatusUser(pos, bookingInformationModel,1);
                    ConfirmBookingBarber(pos, bookingInformationModel);
                    ConfirmBookingUser(bookingInformationModel, pos);
                    SendNotification(pos, bookingInformationModel);
                }
            });


            btn_delete_booking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Show dialog confirm
                    androidx.appcompat.app.AlertDialog.Builder confirmDialog = new androidx.appcompat.app.AlertDialog.Builder(BookingActivity.this)
                            .setCancelable(false)
                            .setTitle("היי!")
                            .setMessage("האם למחוק את התור של "+" " + bookingInformationModel.getCustomerName())
                            .setNegativeButton("ביטול", (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            }).setPositiveButton("מאשרת", (dialogInterface, i) -> {
                                deleteBookingFromBarber(bookingInformationModel, pos);
                                SendNotificationDelete(pos, bookingInformationModel);

                            });
                    confirmDialog.show();
                }
            });

        }

        else if (bookingInformationModel.getBookingStatus() == 1)
        {

            btn_delete_booking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Show dialog confirm
                    androidx.appcompat.app.AlertDialog.Builder confirmDialog = new androidx.appcompat.app.AlertDialog.Builder(BookingActivity.this)
                            .setCancelable(false)
                            .setTitle("היי!")
                            .setMessage("האם למחוק את התור של "+" " + bookingInformationModel.getCustomerName())
                            .setNegativeButton("ביטול", (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            }).setPositiveButton("מאשרת", (dialogInterface, i) -> {
                                deleteBookingFromBarber(bookingInformationModel, pos);
                                SendNotificationDelete(pos, bookingInformationModel);
                            });
                    confirmDialog.show();
                }
            });

        }

    }

    private void deleteBookingFromBarber(BookingInformation bookingInformationModel, int pos) {
        //we need load Common.currentBooking because we need some data from BookingInformation
        if (bookingInformationModel != null)
        {
            dialog.show();

            //get booking information in barber project
            DocumentReference barberBookingInfo = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.state_name)
                    .collection("Branch")
                    .document(bookingInformationModel.getSalonId())
                    .collection("Barbers")
                    .document(bookingInformationModel.getBarberId())
                    .collection(bookingInformationModel.getBookingDate())
                    .document(bookingInformationModel.getSlot().toString());

            //When we document, just delete it
            barberBookingInfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(BookingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //After delete on barber done
                    //we will start delete from User

                    deleteBookingFromUser(bookingInformationModel, pos);
                }
            });

        }
        else
        {
            Toast toast = new Toast(BookingActivity.this);
            toast.setDuration(Toast.LENGTH_LONG);
            View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
            TextView textView = toastView.findViewById(R.id.txt_message);
            toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
            textView.setText("Current Booking must be null");
            toast.setView(toastView);
            toast.show();
        }
    }

    private void deleteBookingFromUser(BookingInformation bookingInformationModel, int pos) {
        DocumentReference bookingDelete = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(bookingInformationModel.getCustomerPhone())
                .collection("Booking")
                .document(bookingInformationModel.getBookingId());

        bookingDelete.delete().addOnFailureListener( new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText( BookingActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
            }
        }).addOnSuccessListener(aVoid -> {

            Paper.init(BookingActivity.this);
            if(Paper.book().read(Common.EVENT_URI_CACHE) != null)
            {
                String eventString = Paper.book().read(Common.EVENT_URI_CACHE).toString();
                Uri eventUri = null;
                if (eventString != null && !TextUtils.isEmpty(eventString))
                    eventUri = Uri.parse(eventString);
                if (eventUri != null)
                    BookingActivity.this.getContentResolver().delete(eventUri, null, null);
            }

            Toast toast = new Toast(BookingActivity.this);
            toast.setDuration(Toast.LENGTH_LONG);
            View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
            TextView textView = toastView.findViewById(R.id.txt_message);
            toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
            textView.setText("תור נמחק בהצלחה!");
            toast.setView(toastView);
            toast.show();

            resetStaticData();
            finish();

        });
    }

    private void UpdateBookingStatusBarber(int pos, BookingInformation bookingInformationModel, int status) {

        DocumentReference bookingUpdate = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(bookingInformationModel.getSalonId())
                .collection("Barbers")
                .document(bookingInformationModel.getBarberId())
                .collection(bookingInformationModel.getBookingDate())
                .document(bookingInformationModel.getSlot().toString());

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("bookingStatus", status);

        bookingUpdate.update(updateData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            adapter.removeItem(pos);
                            adapter.notifyItemRemoved(pos);

                            updateTextCounter();

                        }
                    }
                });
    }

    private void UpdateBookingStatusUser(int pos, BookingInformation bookingInformationModel, int status) {
        DocumentReference bookingUpdate = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(bookingInformationModel.getCustomerPhone())
                .collection("Booking")
                .document(bookingInformationModel.getBookingId());

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("bookingStatus", status);

        bookingUpdate.update(updateData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            adapter.removeItem(pos);
                            adapter.notifyItemRemoved(pos);

                            updateTextCounter();

                            resetStaticData();
                            finish();

                        }
                    }
                });
    }

    private void ConfirmBookingBarber(int pos, BookingInformation bookingInformationModel) {
        //Update bookingInformation, set done = true
        DocumentReference bookingSet = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(bookingInformationModel.getSalonId())
                .collection("Barbers")
                .document(bookingInformationModel.getBarberId())
                .collection(bookingInformationModel.getBookingDate())
                .document(bookingInformationModel.getSlot().toString());

        bookingSet.get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        if(task.getResult().exists())
                        {

                            //Update
                            Map<String, Object> dataUpdate = new HashMap<>();
                            dataUpdate.put("done", true);
                            bookingSet.update(dataUpdate)
                                    .addOnFailureListener(e -> {
                                        dialog.dismiss();
                                        Toast.makeText(BookingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        dialog.dismiss();

                                    }
                                }
                            });
                        }
                    }
                }).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(BookingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void ConfirmBookingUser(BookingInformation bookingInformationModel, int pos) {
        DocumentReference userBookingInfo = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(bookingInformationModel.getCustomerPhone())
                .collection("Booking")
                .document(bookingInformationModel.getBookingId());

        userBookingInfo.get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        //Update
                        Map<String, Object> dataUpdate = new HashMap<>();
                        dataUpdate.put("done", true);
                        userBookingInfo.update(dataUpdate)
                                .addOnFailureListener(e -> {
                                    dialog.dismiss();
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    dialog.dismiss();

                                }
                            }
                        });
                    }
                }).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void SendNotification(int pos, BookingInformation bookingInformationModel) {
        android.app.AlertDialog dialog = new SpotsDialog.Builder().setContext(BookingActivity.this).setCancelable(false).build();
        dialog.show();

        FirebaseFirestore.getInstance()
                .collection("Z&G Tokens")
                .whereEqualTo("userPhone", bookingInformationModel.getCustomerPhone())
                .limit(1)
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful() && task1.getResult().size() > 0) {

                        MyToken myToken = new MyToken();
                        for (DocumentSnapshot tokenSnapshot : task1.getResult()) {
                            myToken = tokenSnapshot.toObject(MyToken.class);
                        }

                        Map<String, String> notiData = new HashMap<>();


                        notiData.put(Common.TITLE_KEY, "היי " + Common.currentBookingInformation.getCustomerName());
                        notiData.put(Common.CONTENT_KEY,"התור שלך אושר בהצלחה");

                        //create notification to send
                        FCMSendData sendData = new FCMSendData(myToken.getToken(), notiData);
                        sendData.setTo(myToken.getToken());
                        sendData.setData(notiData);

                        compositeDisposable.add(ifcmService.sendNotification(sendData)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(fcmResponse -> {
                                    dialog.dismiss();

                                    if (fcmResponse.getSuccess() == 1) {
                                        Toast toast = new Toast(BookingActivity.this);
                                        toast.setDuration(Toast.LENGTH_LONG);
                                        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                                        TextView textView = toastView.findViewById(R.id.txt_message);
                                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                        textView.setText("תור עודכן בהצלחה");
                                        toast.setView(toastView);
                                        toast.show();

                                    } else {
                                        Toast toast = new Toast(BookingActivity.this);
                                        toast.setDuration(Toast.LENGTH_LONG);
                                        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                                        TextView textView = toastView.findViewById(R.id.txt_message);
                                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                        textView.setText("תור עודכן בהצלחה, אבל הודעה לא נשלחה");
                                        toast.setView(toastView);
                                        toast.show();

                                    }

                                }, throwable -> {
                                    dialog.dismiss();
                                    Toast toast = new Toast(BookingActivity.this);
                                    toast.setDuration(Toast.LENGTH_LONG);
                                    View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                                    TextView textView = toastView.findViewById(R.id.txt_message);
                                    toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                    textView.setText("Fail");
                                    toast.setView(toastView);
                                    toast.show();
                                }));
                    }
                    else
                    {
                        dialog.dismiss();
                        Toast toast = new Toast(BookingActivity.this);
                        toast.setDuration(Toast.LENGTH_LONG);
                        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                        TextView textView = toastView.findViewById(R.id.txt_message);
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        textView.setText("Token not found");
                        toast.setView(toastView);
                        toast.show();
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast toast = new Toast(BookingActivity.this);
                toast.setDuration(Toast.LENGTH_LONG);
                View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                TextView textView = toastView.findViewById(R.id.txt_message);
                toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                textView.setText("Fail");
                toast.setView(toastView);
                toast.show();
            }
        });

        adapter.removeItem(pos);
        adapter.notifyItemRemoved(pos);

        updateTextCounter();

        resetStaticData();
        finish();
    }

    private void SendNotificationDelete(int pos, BookingInformation bookingInformationModel) {
        android.app.AlertDialog dialog = new SpotsDialog.Builder().setContext(BookingActivity.this).setCancelable(false).build();
        dialog.show();

        FirebaseFirestore.getInstance()
                .collection("Z&G Tokens")
                .whereEqualTo("userPhone", bookingInformationModel.getCustomerPhone())
                .limit(1)
                .get()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful() && task1.getResult().size() > 0) {

                        MyToken myToken = new MyToken();
                        for (DocumentSnapshot tokenSnapshot : task1.getResult()) {
                            myToken = tokenSnapshot.toObject(MyToken.class);
                        }

                        Map<String, String> notiData = new HashMap<>();


                        notiData.put(Common.TITLE_KEY, "היי " + Common.currentBookingInformation.getCustomerName());
                        notiData.put(Common.CONTENT_KEY,"התור שלך נמחק");

                        //create notification to send
                        FCMSendData sendData = new FCMSendData(myToken.getToken(), notiData);
                        sendData.setTo(myToken.getToken());
                        sendData.setData(notiData);

                        compositeDisposable.add(ifcmService.sendNotification(sendData)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(fcmResponse -> {
                                    dialog.dismiss();

                                    if (fcmResponse.getSuccess() == 1) {
                                        Toast toast = new Toast(BookingActivity.this);
                                        toast.setDuration(Toast.LENGTH_LONG);
                                        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                                        TextView textView = toastView.findViewById(R.id.txt_message);
                                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                        textView.setText("תור עודכן בהצלחה");
                                        toast.setView(toastView);
                                        toast.show();

                                    } else {
                                        Toast toast = new Toast(BookingActivity.this);
                                        toast.setDuration(Toast.LENGTH_LONG);
                                        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                                        TextView textView = toastView.findViewById(R.id.txt_message);
                                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                        textView.setText("תור עודכן בהצלחה, אבל הודעה לא נשלחה");
                                        toast.setView(toastView);
                                        toast.show();

                                    }

                                }, throwable -> {
                                    dialog.dismiss();
                                    Toast toast = new Toast(BookingActivity.this);
                                    toast.setDuration(Toast.LENGTH_LONG);
                                    View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                                    TextView textView = toastView.findViewById(R.id.txt_message);
                                    toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                    textView.setText("Fail");
                                    toast.setView(toastView);
                                    toast.show();
                                }));
                    }
                    else
                    {
                        dialog.dismiss();
                        Toast toast = new Toast(BookingActivity.this);
                        toast.setDuration(Toast.LENGTH_LONG);
                        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                        TextView textView = toastView.findViewById(R.id.txt_message);
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        textView.setText("Token not found");
                        toast.setView(toastView);
                        toast.show();
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast toast = new Toast(BookingActivity.this);
                toast.setDuration(Toast.LENGTH_LONG);
                View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                TextView textView = toastView.findViewById(R.id.txt_message);
                toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                textView.setText("Fail");
                toast.setView(toastView);
                toast.show();
            }
        });
    }

    private void resetStaticData() {
        Intent intent = new Intent(BookingActivity.this, BookingActivity.class);
        startActivity(intent);
    }

    private void updateTextCounter() {
        txt_booking_filter.setText(new StringBuilder("רשימת כל התורים (")
                .append(adapter.getItemCount())
                .append(")"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.booking_filter_memu, menu);

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(LoadBookingEvent.class))
            EventBus.getDefault().removeStickyEvent(LoadBookingEvent.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeBookingClick(true));
        super.onDestroy();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoadBookingEvent(LoadBookingEvent event)
    {
        viewBooking.loadBookingByStatus(event.getStatus());
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            super.onBackPressed(); //replaced
        }
    }

    public void back(View view) {
        startActivity(new Intent(BookingActivity.this, StaffHomeActivity.class));
    }
}