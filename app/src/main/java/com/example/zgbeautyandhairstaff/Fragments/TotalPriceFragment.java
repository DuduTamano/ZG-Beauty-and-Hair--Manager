package com.example.zgbeautyandhairstaff.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zgbeautyandhairstaff.Adapter.MyConfirmShoppingItemAdapter;
import com.example.zgbeautyandhairstaff.Adapter.MyOrdersAdapter;
import com.example.zgbeautyandhairstaff.BookingActivity;
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Model.BarberServices;
import com.example.zgbeautyandhairstaff.Model.BookingInformation;
import com.example.zgbeautyandhairstaff.Model.CartItem;
import com.example.zgbeautyandhairstaff.Model.EventBus.DismissFromBottomSheetEvent;
import com.example.zgbeautyandhairstaff.Model.FCMSendData;
import com.example.zgbeautyandhairstaff.Model.MyToken;
import com.example.zgbeautyandhairstaff.R;
import com.example.zgbeautyandhairstaff.Retrofit.IFCMService;
import com.example.zgbeautyandhairstaff.Retrofit.RetrofitClient;
import com.example.zgbeautyandhairstaff.ViewModel.ViewOrders;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

@SuppressLint("ValidFragment")
public class TotalPriceFragment extends BottomSheetDialogFragment {
    Unbinder unbinder;

    MyOrdersAdapter adapter;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @BindView(R.id.chip_group_services)
    ChipGroup chip_group_service;

    @BindView(R.id.recycler_view_shopping)
    RecyclerView recycler_view_shopping;

    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;

    @BindView(R.id.userImage)
    ImageView userImage;

    @BindView(R.id.txt_barber_name)
    TextView txt_barber_name;

    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;

    @BindView(R.id.txt_customer_phone)
    TextView txt_customer_phone;

    @BindView(R.id.txt_time)
    TextView txt_time;

    @BindView(R.id.txt_total_price)
    TextView txt_total_price;

    @BindView(R.id.btn_confirm)
    Button btn_confirm;

    HashSet<BarberServices> servicesAdded;

    //Interface
    IFCMService ifcmService;

    AlertDialog dialog;

    String image_url;

    @BindView(R.id.linear_view_shopping)
    CardView linear_view_shopping;

    MyConfirmShoppingItemAdapter myConfirmShoppingItemAdapter;

    private static TotalPriceFragment instance;

    public static TotalPriceFragment getInstance() {
        return instance == null?new TotalPriceFragment():instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_total_price, container, false);

        unbinder = ButterKnife.bind(this, itemView);

        init();

        initView();

        getBundle(getArguments());

        setInformation();

        return itemView;

    }

    private void setInformation() {
        txt_salon_name.setText(Common.selected_salon.getName());
        txt_barber_name.setText(Common.currentBarber.getName());
        txt_time.setText(new StringBuilder(" בתאריך ").append(Common.currentBookingInformation.getBookingDate())
        .append(" בשעה: ").append(Common.convertTimeSlotToString(Common.currentBookingInformation.getSlot().intValue())));
        txt_customer_name.setText(Common.currentBookingInformation.getCustomerName());
        txt_customer_phone.setText(Common.currentBookingInformation.getCustomerPhone().replaceAll("[+]972", "0"));

        Picasso.get().load(Common.currentBookingInformation.getCustomerImage()).into(userImage);

        if(servicesAdded.size() > 0)
        {
            //add To chip group
            int i = 0;
            for(BarberServices services: servicesAdded)
            {
                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.chip_item, null);
                chip.setText(services.getName());
                chip.setTag(i);
                chip.setOnCloseIconClickListener( view -> {
                    servicesAdded.remove((int)view.getTag());
                    chip_group_service.removeView(view);

                    calculatePrice();
                });
                chip_group_service.addView(chip);
                i++;
            }
        }

        if (Common.currentBookingInformation.getCartItemList() != null)
        {
            if (Common.currentBookingInformation.getCartItemList().size() > 0)
            {
                myConfirmShoppingItemAdapter = new MyConfirmShoppingItemAdapter(getContext(), Common.currentBookingInformation.getCartItemList());
                recycler_view_shopping.setAdapter(myConfirmShoppingItemAdapter);
            }

            calculatePrice();
        }
    }

    private double calculatePrice() {
        double price = Common.DEFAULT_PRICE;
        for(BarberServices services:servicesAdded)
            price+= services.getPrice();

        if(Common.currentBookingInformation.getCartItemList() != null)
        {
            for(CartItem cartItem:Common.currentBookingInformation.getCartItemList())
                price += (cartItem.getProductPrice()*cartItem.getProductQuantity());
        }

        txt_total_price.setText(new StringBuilder(Common.MONEY_SIGN)
                .append(price));

        return price;
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(getContext())
                .setCancelable(false).build();
        ifcmService = RetrofitClient.getInstance().create(IFCMService.class);
    }

    private void initView() {
        recycler_view_shopping.setHasFixedSize(true);
        recycler_view_shopping.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        if (Common.currentBookingInformation.getCartItemList().size() ==0 ) {
            linear_view_shopping.setVisibility(View.GONE);
        }
        else {
            linear_view_shopping.setVisibility(View.VISIBLE);
        }


        btn_confirm.setOnClickListener(view -> {
            dialog.show();

            ConfirmBookingBarber();
            UpdateBookingStatusBarber(1);

        });
    }

    private void ConfirmBookingBarber() {
        //Update bookingInformation, set done = true
        DocumentReference bookingSet = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId())
                .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                .document(Common.currentBookingInformation.getBookingId());

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
                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        dialog.dismiss();

                                        ConfirmBookingUser();

                                        sendNotificationUpdateToUser(Common.currentBookingInformation.getCustomerPhone());

                                    }
                                }
                            });
                        }
                    }
                }).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void ConfirmBookingUser() {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(Common.currentBookingInformation.getCustomerPhone())
                .collection("Booking")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        for (DocumentSnapshot snapshot : task.getResult()) {
                            DocumentReference userBookingInfo =FirebaseFirestore.getInstance()
                                    .collection("Users")
                                    .document(Common.currentBookingInformation.getCustomerPhone())
                                    .collection("Booking")
                                    .document(snapshot.getId());

                            userBookingInfo.get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful())
                                            {
                                                //Update
                                                Map<String, Object> dataUpdate = new HashMap<>();
                                                dataUpdate.put("done", true);
                                                userBookingInfo.update(dataUpdate)
                                                        .addOnFailureListener(e -> {
                                                            dialog.dismiss();
                                                            Toast.makeText(TotalPriceFragment.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                    });

                        }

                    }
                }).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(TotalPriceFragment.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void UpdateBookingStatusBarber(int status) {

        //Update bookingInformation, status to 1
        DocumentReference bookingSet = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId())
                .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                .document(Common.currentBookingInformation.getBookingId());

        bookingSet.get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {
                        if(task.getResult().exists())
                        {
                            //Update
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("bookingStatus", status);
                            bookingSet.update(updateData)
                                    .addOnFailureListener(e -> {
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        dialog.dismiss();

                                        UpdateBookingStatusUser(1);

                                    }
                                }
                            });
                        }
                    }
                }).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void UpdateBookingStatusUser(int status) {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(Common.currentBookingInformation.getCustomerPhone())
                .collection("Booking")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot snapshot : task.getResult()) {
                                DocumentReference bookingSet = FirebaseFirestore.getInstance()
                                        .collection("Users")
                                        .document(Common.currentBookingInformation.getCustomerPhone())
                                        .collection("Booking")
                                        .document(snapshot.getId());

                                bookingSet.get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    //Update
                                                    Map<String, Object> updateData = new HashMap<>();
                                                    updateData.put("bookingStatus", status);
                                                    bookingSet.update(updateData)
                                                            .addOnFailureListener(e -> {
                                                                dialog.dismiss();
                                                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                        });

                            }

                        }
                    }
                }).addOnFailureListener(e -> {
            dialog.dismiss();
            Toast.makeText(TotalPriceFragment.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void sendNotificationUpdateToUser(String customerPhone) {
        //Get Token of user first
        FirebaseFirestore.getInstance()
                .collection("Z&G Tokens")
                .whereEqualTo("userPhone", customerPhone)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().size() > 0) {

                            MyToken myToken = new MyToken();
                            for (DocumentSnapshot tokenSnapShot : task.getResult()) {
                                myToken = tokenSnapShot.toObject(MyToken.class);
                            }

                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put("update_done", "true");

                            dataSend.put(Common.TITLE_KEY, "היי " + Common.currentBookingInformation.getCustomerName());
                            dataSend.put(Common.CONTENT_KEY,"התור שלך אושר בהצלחה");

                            //create notification to send
                            FCMSendData fcmSendData = new FCMSendData(myToken.getToken(), dataSend);

                            fcmSendData.setTo(myToken.getToken());
                            fcmSendData.setData(dataSend);

                            //Information need for Rating
                            dataSend.put(Common.RATING_STATE_KEY, Common.state_name);
                            dataSend.put(Common.RATING_SALON_ID, Common.selected_salon.getSalonId());
                            dataSend.put(Common.RATING_SALON_NAME, Common.selected_salon.getName());
                            dataSend.put(Common.RATING_BARBER_ID, Common.currentBarber.getBarberId());


                            compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(fcmResponse -> {
                                        dialog.dismiss();
                                        TotalPriceFragment.this.dismiss();
                                        //Post an event
                                        EventBus.getDefault().postSticky(new DismissFromBottomSheetEvent(true));

                                        if (fcmResponse != null)
                                            Toast.makeText( getContext(), "אישור נשלח ללקוחה", Toast.LENGTH_SHORT ).show();

                                        else
                                            Toast.makeText(getContext(), "לא נשלח", Toast.LENGTH_SHORT).show();


                                    }, throwable -> {
                                        Looper.prepare();
                                        Toast.makeText(TotalPriceFragment.this.getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        Looper.loop();
                                    }));
                        }
                    }
                });
    }

    private void getBundle(Bundle arguments) {
        this.servicesAdded = new Gson()
                .fromJson(arguments.getString(Common.SERVICES_ADDED),
                        new TypeToken<HashSet<BarberServices>>(){}.getType());

        image_url = arguments.getString(Common.IMAGE_DOWNLOADABLE_URL);

    }

}
