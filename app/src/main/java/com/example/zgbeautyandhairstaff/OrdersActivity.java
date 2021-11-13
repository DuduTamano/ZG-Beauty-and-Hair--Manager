package com.example.zgbeautyandhairstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zgbeautyandhairstaff.Adapter.MyOrdersAdapter;
import com.example.zgbeautyandhairstaff.Common.BottomSheetOrderFragment;
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Common.MySwipeHelper;
import com.example.zgbeautyandhairstaff.Model.EventBus.ChangeMenuClick;
import com.example.zgbeautyandhairstaff.Model.EventBus.LoadOrderEvent;
import com.example.zgbeautyandhairstaff.Model.FCMSendData;
import com.example.zgbeautyandhairstaff.Model.MyToken;
import com.example.zgbeautyandhairstaff.Model.Order;
import com.example.zgbeautyandhairstaff.Retrofit.IFCMService;
import com.example.zgbeautyandhairstaff.Retrofit.RetrofitClient;
import com.example.zgbeautyandhairstaff.ViewModel.ViewOrders;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrdersActivity extends AppCompatActivity {
    @BindView(R.id.recycler_orders)
    RecyclerView recycler_orders;

    @BindView(R.id.txt_order_filter)
    TextView txt_order_filter;

    Unbinder unbinder;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private IFCMService ifcmService;

    LayoutAnimationController layoutAnimationController;

    MyOrdersAdapter adapter;

    private ViewOrders viewOrders;

    android.app.AlertDialog dialog;

    ImageView action_filter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        dialog = new SpotsDialog.Builder().setContext(this)
                .setCancelable(false).build();

        action_filter = findViewById(R.id.action_filter);

        viewOrders = new ViewModelProvider(this).get(ViewOrders.class);

        unbinder = ButterKnife.bind(this);


        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        loadUserId();

        initView();

        viewOrders.getMessageError().observe(this, s -> {
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        });

        viewOrders.getOrderModelMutableLiveData().observe(this, new Observer<List<Order>>() {
            @Override
            public void onChanged(List<Order> orderModels) {
                if (orderModels != null) {
                    adapter = new MyOrdersAdapter(OrdersActivity.this, orderModels);
                    recycler_orders.setAdapter(adapter);
                    recycler_orders.setLayoutAnimation(layoutAnimationController);

                    OrdersActivity.this.updateTextCounter();
                }
            }
        });

        action_filter.setOnClickListener(v -> OpenFilter());

    }

    private void OpenFilter() {
        BottomSheetOrderFragment bottomSheetOrderFragment = BottomSheetOrderFragment.getInstance();
        bottomSheetOrderFragment.show(this.getSupportFragmentManager(), "Order filter");
    }

    private void loadUserId() {
        FirebaseFirestore.getInstance().collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot userSnapshot : task.getResult()) {

                                loadOrderId(userSnapshot.getId());

                            }
                        }
                    }
                });
    }

    private void loadOrderId(String userPhone) {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userPhone)
                .collection("Orders")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot orderSnapshot:task.getResult()) {

                                Order order = orderSnapshot.toObject(Order.class);

                                Common.currentOrder = order;

                                order.setKey(orderSnapshot.getId());

                            }
                        }
                    }
                });
    }

    private void initView() {

        ifcmService = RetrofitClient.getInstance().create(IFCMService.class);

        recycler_orders.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_orders.setLayoutManager(layoutManager);
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_item_from_left);

        //Slide options
        DisplayMetrics displayMetrics = new DisplayMetrics();
        OrdersActivity.this.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        MySwipeHelper mySwipeHelper = new MySwipeHelper(this, recycler_orders, width/6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {

                buf.add(new MyButton(OrdersActivity.this, "חיוג ללקוחה", 40, 0, Color.parseColor("#1b1b1b"),
                        pos -> {

                            Dexter.withActivity(OrdersActivity.this)
                                    .withPermission(Manifest.permission.CALL_PHONE)
                                    .withListener(new PermissionListener() {
                                        @Override
                                        public void onPermissionGranted(PermissionGrantedResponse response) {
                                            Order order = adapter.getItemPosition(pos);
                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_DIAL);
                                            intent.setData(Uri.parse(new StringBuilder("tel: ")
                                            .append(order.getUserPhone()).toString()));
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onPermissionDenied(PermissionDeniedResponse response) {

                                            Toast toast = new Toast(OrdersActivity.this);
                                            toast.setDuration(Toast.LENGTH_LONG);
                                            View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                                            TextView textView = toastView.findViewById(R.id.txt_message);
                                            toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                            textView.setText("You must accept"+response.getPermissionName());
                                            toast.setView(toastView);
                                            toast.show();
                                        }

                                        @Override
                                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                                        }
                                    }).check();

                        }));

                buf.add(new MyButton(OrdersActivity.this, "מחיקת הזמנה", 40, 0, Color.parseColor("#484848"),
                        pos -> {

                            AlertDialog.Builder builder = new AlertDialog.Builder(OrdersActivity.this)
                                    .setTitle("מחיקה")
                                    .setMessage("האם את בטוחה שאת רוצה למחוק את ההזמנה?")
                                    .setNegativeButton("ביטול", (dialog, i) -> dialog.dismiss())
                                    .setPositiveButton("מחיקה", (dialog, which) -> {

                                        FirebaseFirestore.getInstance()
                                                .collection("Users")
                                                .get().addOnCompleteListener( new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                                for (DocumentSnapshot snapshot : task.getResult()) {
                                                    FirebaseFirestore.getInstance()
                                                            .collection("Users")
                                                            .document(snapshot.getId())
                                                            .collection("Orders")
                                                            .document(Common.currentOrder.getKey())
                                                            .delete()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {

                                                                        deleteOrder(pos);

                                                                        dialog.dismiss();

                                                                        Toast toast = new Toast(OrdersActivity.this);
                                                                        toast.setDuration(Toast.LENGTH_LONG);
                                                                        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                                                                        TextView textView = toastView.findViewById(R.id.txt_message);
                                                                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                                                        textView.setText("Order has been delete!");
                                                                        toast.setView(toastView);
                                                                        toast.show();
                                                                    }
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                            Toast toast = new Toast(OrdersActivity.this);
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
                                            }
                                        });

                                    });

                            //create dialog
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            Button negativeButton = dialog.getButton(dialog.BUTTON_NEGATIVE);
                            negativeButton.setTextColor(Color.GRAY);
                            Button positiveButton = dialog.getButton(dialog.BUTTON_POSITIVE);
                            positiveButton.setTextColor(Color.RED);


                        }));

                buf.add(new MyButton(OrdersActivity.this, "עדכון", 40, 0, Color.parseColor("#6d6d6d"),
                        pos -> {
                                showEditDialog(adapter.getItemPosition(pos), pos);
                        }));
            }
        };

    }

    private void showEditDialog(Order orderModel, int pos) {
        View layout_dialog;
        AlertDialog.Builder builder;

        //Placed
        if (orderModel.getOrderStatus() == 0)
        {
            layout_dialog = LayoutInflater.from(OrdersActivity.this)
                    .inflate(R.layout.layout_dialog_shipping, null);
            builder = new AlertDialog.Builder(this)
                    .setView(layout_dialog);
        }

        //Cancelled
        else if (orderModel.getOrderStatus() == -1)
        {
            layout_dialog = LayoutInflater.from(OrdersActivity.this)
                    .inflate(R.layout.layout_dialog_cancelled, null);
            builder = new AlertDialog.Builder(this)
                    .setView(layout_dialog);
        }

        //Shipped
        else
        {
            layout_dialog = LayoutInflater.from(OrdersActivity.this)
                    .inflate(R.layout.layout_dialog_shipped, null);
            builder = new AlertDialog.Builder(this)
                    .setView(layout_dialog);
        }

        //View
        Button btn_ok = (Button)layout_dialog.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button)layout_dialog.findViewById(R.id.btn_cancel);

        RadioButton rdi_shipping = (RadioButton)layout_dialog.findViewById(R.id.rdi_shipping);
        RadioButton rdi_shipped = (RadioButton)layout_dialog.findViewById(R.id.rdi_shipped);
        RadioButton rdi_cancelled = (RadioButton)layout_dialog.findViewById(R.id.rdi_cancelled);
        RadioButton rdi_delete = (RadioButton)layout_dialog.findViewById(R.id.rdi_delete);
        RadioButton rdi_restore_placed = (RadioButton)layout_dialog.findViewById(R.id.rdi_restore_placed);

        TextView txt_status = (TextView)layout_dialog.findViewById(R.id.txt_status);

        //Set data
        txt_status.setText(new StringBuilder("סטטוס הזמנה(")
                .append(Common.convertStatusToString(orderModel.getOrderStatus())));


        //Create Dialog
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.show();

        //Custom dialog
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        btn_cancel.setOnClickListener(v ->
                dialog.dismiss());

        btn_ok.setOnClickListener(v -> {
            dialog.dismiss();
            if (rdi_cancelled != null && rdi_cancelled.isChecked())
                updateOrder(pos, orderModel,  -1, Common.currentOrder.getUserPhone());

            if (rdi_shipping != null && rdi_shipping.isChecked())
                updateOrder(pos, orderModel, 1, Common.currentOrder.getUserPhone());

            if (rdi_shipped != null && rdi_shipped.isChecked())
                updateOrder(pos, orderModel, 2, Common.currentOrder.getUserPhone());

            if (rdi_restore_placed != null && rdi_restore_placed.isChecked())
                updateOrder(pos, orderModel, 0, Common.currentOrder.getUserPhone());

            else if (rdi_delete!= null && rdi_delete.isChecked())
                deleteOrder(pos);
        });

    }

    private void updateOrder(int pos, Order orderModel, int status, String customerPhone) {
        if (!TextUtils.isEmpty(orderModel.getKey())) {

            FirebaseFirestore.getInstance()
                    .collection( "Users" )
                    .get().addOnCompleteListener( new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                    for (DocumentSnapshot snapshot : task.getResult()) {

                        DocumentReference orderUpdate = FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(snapshot.getId())
                                .collection("Orders")
                                .document(orderModel.getKey());

                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("orderStatus", status);

                        orderUpdate.update(updateData)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                            android.app.AlertDialog dialog = new SpotsDialog.Builder().setContext(OrdersActivity.this).setCancelable(false).build();
                                            dialog.show();

                                            FirebaseFirestore.getInstance()
                                                    .collection("Z&G Tokens")
                                                    .whereEqualTo("userPhone", customerPhone)
                                                    .limit(1)
                                                    .get()
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful() && task1.getResult().size() > 0) {

                                                            MyToken myToken = new MyToken();
                                                            for (DocumentSnapshot tokenSnapshot : task1.getResult()) {
                                                                myToken = tokenSnapshot.toObject(MyToken.class);
                                                            }

                                                            Map<String, String> notiData = new HashMap<>();

                                                            notiData.put(Common.TITLE_KEY, "ההזמנה שלך");
                                                            notiData.put(Common.CONTENT_KEY, new StringBuilder("מס. הזמנה ")
                                                                    .append(orderModel.getOrderNumber())
                                                                    .append(" "+ Common.convertStatusToString(status)).toString());

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
                                                                            Toast toast = new Toast(OrdersActivity.this);
                                                                            toast.setDuration(Toast.LENGTH_LONG);
                                                                            View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                                                                            TextView textView = toastView.findViewById(R.id.txt_message);
                                                                            toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                                                            textView.setText("Update order success");
                                                                            toast.setView(toastView);
                                                                            toast.show();

                                                                            adapter.notifyItemRemoved(pos);
                                                                            adapter.removeItem(pos);

                                                                            updateTextCounter();

                                                                        } else {
                                                                            Toast toast = new Toast(OrdersActivity.this);
                                                                            toast.setDuration(Toast.LENGTH_LONG);
                                                                            View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                                                                            TextView textView = toastView.findViewById(R.id.txt_message);
                                                                            toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                                                            textView.setText("Update order success but failed to send noti");
                                                                            toast.setView(toastView);
                                                                            toast.show();
                                                                        }

                                                                    }, throwable -> {
                                                                        dialog.dismiss();

                                                                        Toast toast = new Toast(OrdersActivity.this);
                                                                        toast.setDuration(Toast.LENGTH_LONG);
                                                                        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                                                                        TextView textView = toastView.findViewById(R.id.txt_message);
                                                                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                                                        textView.setText("fail");
                                                                        toast.setView(toastView);
                                                                        toast.show();
                                                                    }));
                                                        }
                                                        else
                                                        {
                                                            dialog.dismiss();

                                                            Toast toast = new Toast(OrdersActivity.this);
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
                                                    Toast.makeText(OrdersActivity.this, "fail", Toast.LENGTH_SHORT).show();

                                                    Toast toast = new Toast(OrdersActivity.this);
                                                    toast.setDuration(Toast.LENGTH_LONG);
                                                    View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                                                    TextView textView = toastView.findViewById(R.id.txt_message);
                                                    toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                                    textView.setText("fail");
                                                    toast.setView(toastView);
                                                    toast.show();
                                                }
                                            });


                                        }
                                    }
                                });


                    }
                }
            } );
        }
        else {
            Toast.makeText( this, "fail", Toast.LENGTH_SHORT ).show();
        }
    }

    private void deleteOrder(int pos) {
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .get().addOnCompleteListener( new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot snapshot : task.getResult()) {

                            FirebaseFirestore.getInstance()
                                    .collection("Users")
                                    .document(snapshot.getId())
                                    .collection("Orders")
                                    .document(Common.currentOrder.getKey())
                                    .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    adapter.notifyItemRemoved(pos);
                                    adapter.removeItem(pos);

                                    updateTextCounter();

                                    Toast toast = new Toast(OrdersActivity.this);
                                    toast.setDuration(Toast.LENGTH_LONG);
                                    View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                                    TextView textView = toastView.findViewById(R.id.txt_message);
                                    toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                    textView.setText("Order has been delete!");
                                    toast.setView(toastView);
                                    toast.show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast toast = new Toast(OrdersActivity.this);
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

                    }
                }
            } );
    }

    private void updateTextCounter() {
        txt_order_filter.setText(new StringBuilder("מספר הזמנות (")
                .append(adapter.getItemCount())
                .append(")"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.order_filter_memu, menu);

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
        if (EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent.class))
            EventBus.getDefault().removeStickyEvent(LoadOrderEvent.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoadOrderEvent(LoadOrderEvent event)
    {
        viewOrders.loadOrderByStatus(event.getStatus());
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
        startActivity(new Intent(OrdersActivity.this, StaffHomeActivity.class));
    }
}