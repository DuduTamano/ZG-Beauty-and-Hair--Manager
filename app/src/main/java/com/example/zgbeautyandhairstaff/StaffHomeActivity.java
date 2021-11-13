package com.example.zgbeautyandhairstaff;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zgbeautyandhairstaff.Adapter.HomeSliderAdapter;
import com.example.zgbeautyandhairstaff.Adapter.LookbookAdapter;
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Interface.IBannerLoadListener;
import com.example.zgbeautyandhairstaff.Interface.ILookbookLoadListener;
import com.example.zgbeautyandhairstaff.Interface.INotificationCountListener;
import com.example.zgbeautyandhairstaff.Model.Banner;
import com.example.zgbeautyandhairstaff.Model.EventBus.CategoryClick;
import com.example.zgbeautyandhairstaff.Model.EventBus.ChangeMenuClick;
import com.example.zgbeautyandhairstaff.Model.EventBus.ToastEvent;
import com.example.zgbeautyandhairstaff.Model.FCMSendData;
import com.example.zgbeautyandhairstaff.Model.News;
import com.example.zgbeautyandhairstaff.Retrofit.IFCMService;
import com.example.zgbeautyandhairstaff.Retrofit.RetrofitClient;
import com.example.zgbeautyandhairstaff.Service.PicassoImageLoadingService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ss.com.bannerslider.Slider;

public class StaffHomeActivity extends AppCompatActivity implements IBannerLoadListener, INotificationCountListener, ILookbookLoadListener {

    private static final int PICK_IMAGE_REQUEST = 7171;

    StorageReference storageReference;
    FirebaseFirestore firebaseFirestore;

    //Firebase
    CollectionReference bannerRef,lookbookRef;

    //Interface
    IBannerLoadListener iBannerLoadListener;

    ILookbookLoadListener iLookbookLoadListener;

    @BindView(R.id.banner_slider)
    Slider banner_slider;

    @BindView(R.id.recycler_look_book)
    RecyclerView recycler_look_book;

    @BindView(R.id.booking)
    CardView booking;

    @BindView(R.id.categories)
    CardView categories;

    @BindView(R.id.orders)
    CardView orders;

    @BindView(R.id.news)
    CardView news;

    @BindView(R.id.users_contact)
    CardView users_contact;

    CircleImageView circleLogo;

    TextView txt_notification_badge;

    ImageView action_new_notification;

    CollectionReference notificationCollection;

    INotificationCountListener iNotificationCountListener;

    EventListener<QuerySnapshot> notificationEvent;

    ListenerRegistration notificationListener;

    Uri imgUri = null;

    ImageView img_upload;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    IFCMService ifcmService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home );

        ButterKnife.bind(this);

        booking = findViewById(R.id.booking);
        categories = findViewById(R.id.categories);
        orders = findViewById(R.id.orders);
        circleLogo = findViewById(R.id.circleLogo);
        news = findViewById(R.id.news);
        users_contact = findViewById(R.id.users_contact);

        txt_notification_badge = findViewById(R.id.notification_badge);
        action_new_notification = findViewById(R.id.action_new_notification);

        storageReference = FirebaseStorage.getInstance().getReference("NewsImage");
        firebaseFirestore = FirebaseFirestore.getInstance();

        bannerRef = FirebaseFirestore.getInstance().collection("Banner");
        lookbookRef = FirebaseFirestore.getInstance().collection("Lookbook");

        ifcmService = RetrofitClient.getInstance().create(IFCMService.class);

        users_contact.setOnClickListener(v ->
                startActivity(new Intent(StaffHomeActivity.this, UsersContactActivity.class)));

        booking.setOnClickListener(v ->
                startActivity(new Intent(StaffHomeActivity.this, BookingActivity.class)));

        categories.setOnClickListener(v ->
                startActivity(new Intent(StaffHomeActivity.this, CategoryActivity.class)));

        orders.setOnClickListener(v ->
                startActivity(new Intent(StaffHomeActivity.this, OrdersActivity.class)));

        circleLogo.setOnClickListener(v ->
                logOut());

        action_new_notification.setOnClickListener(v -> {
            startActivity(new Intent(StaffHomeActivity.this, NotificationActivity.class));
            txt_notification_badge.setText("");
        });

        news.setOnClickListener(v ->
                showNewsDialog());

        init();

        loadNotification();
    }

    @SuppressLint("ResourceType")
    private void showNewsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle("הודעת מערכת");
        builder.setMessage("שליחת מבצעים לכל הלקוחות");
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_news, null);

        //View
        EditText edt_title = (EditText) itemView.findViewById(R.id.edt_title);
        EditText edt_content = (EditText) itemView.findViewById(R.id.edt_content);
        EditText edt_link = (EditText) itemView.findViewById(R.id.edt_link);
        img_upload = (ImageView) itemView.findViewById(R.id.img_upload);
        RadioButton rdi_none = (RadioButton) itemView.findViewById(R.id.rdi_none);
        RadioButton rdi_link = (RadioButton) itemView.findViewById(R.id.rdi_link);
        RadioButton rdi_upload = (RadioButton) itemView.findViewById(R.id.rdi_image);

        Calendar bookingDate = Calendar.getInstance();
        Calendar bookingDateWithoutHouse = Calendar.getInstance();
        bookingDateWithoutHouse.setTimeInMillis(bookingDate.getTimeInMillis());

        //Create Timestamp object and apply to BookingInformation
        Timestamp timestamp = new Timestamp(bookingDateWithoutHouse.getTime());

        //Event
        rdi_none.setOnClickListener(View -> {
            edt_link.setVisibility(View.GONE);
            img_upload.setVisibility(View.GONE);
        });

        rdi_link.setOnClickListener(View -> {
            edt_link.setVisibility(View.VISIBLE);
            img_upload.setVisibility(View.GONE);
        });

        rdi_upload.setOnClickListener(View -> {
            edt_link.setVisibility(View.GONE);
            img_upload.setVisibility(View.VISIBLE);
        });

        img_upload.setOnClickListener(View -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "בחרי תמונה"), PICK_IMAGE_REQUEST);
        });

        builder.setView(itemView);
        builder.setNegativeButton("ביטול", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });

        builder.setPositiveButton("שלח", (dialogInterface, i) -> {
            if (rdi_none.isChecked())
            {
                sendNews(edt_title.getText().toString(), edt_content.getText().toString());
            }
            else if (rdi_link.isChecked())
            {
                sendNews(edt_title.getText().toString(), edt_content.getText().toString(), edt_link.getText().toString());
            }
            else if (rdi_upload.isChecked())
            {
                if (imgUri != null)
                {
                    AlertDialog dialog = new AlertDialog.Builder(this).setMessage("מעדכן...").create();
                    dialog.show();

                    String file_name = UUID.randomUUID().toString();

                    StorageReference newsImages = storageReference.child("news/" + file_name);

                    newsImages.putFile(imgUri)
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }).addOnSuccessListener(taskSnapshot -> {
                        dialog.dismiss();
                        newsImages.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                sendNews(edt_title.getText().toString(), edt_content.getText().toString(), uri.toString());
                              /* Map<String, String> news = new HashMap<>();
                                news.put("Image", imgUri.toString());
                                news.get(timestamp); */

                                News news = new News();

                                news.setImage(imgUri.toString());
                                news.setCONTENT_KEY(edt_content.getText().toString());
                                news.setTITLE_KEY(edt_title.getText().toString());
                                news.setTimestamp(timestamp);

                                firebaseFirestore.collection("News").document()
                                        .set(news)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful())
                                            {
                                                dialog.dismiss();

                                                Toast mToast = new Toast(StaffHomeActivity.this);
                                                mToast.setDuration(Toast.LENGTH_LONG);
                                                View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                                                TextView textView = toastView.findViewById(R.id.txt_message);
                                                mToast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                                textView.setText("עודכן");
                                                mToast.setView(toastView);
                                                mToast.show();

                                            }
                                            else
                                            {
                                                Toast.makeText(StaffHomeActivity.this, "Error"+task.getException(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        });
                    }).addOnProgressListener(taskSnapshot -> {
                        double progress = Math.round(100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        dialog.setMessage(new StringBuilder("מעדכן: ").append(progress).append("%"));
                    });

                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        Button negativeButton = dialog.getButton(dialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor( Color.GRAY);
        Button positiveButton = dialog.getButton(dialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(Color.RED);

    }

    private void sendNews(String title, String content, String url) {
        Map<String, String> notificationData = new HashMap<String, String>();
        notificationData.put(Common.TITLE_KEY, title);
        notificationData.put(Common.CONTENT_KEY, content);
        notificationData.put(Common.IS_SEND_IMAGE, "true");
        notificationData.put(Common.IMAGE_URL, url);

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(), notificationData);

        fcmSendData.setTo(Common.getNewsTopic());
        fcmSendData.setData(notificationData);

        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("המתיני בבקשה...").create();
        dialog.show();

        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                    dialog.dismiss();
                    if (fcmResponse.getMessage_id() != 0){
                        Toast toast = new Toast(StaffHomeActivity.this);
                        toast.setDuration(Toast.LENGTH_LONG);
                        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                        TextView textView = toastView.findViewById(R.id.txt_message);
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        textView.setText("נשלח");
                        toast.setView(toastView);
                        toast.show();
                    }
                    else {
                        Toast toast = new Toast(StaffHomeActivity.this);
                        toast.setDuration(Toast.LENGTH_LONG);
                        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                        TextView textView = toastView.findViewById(R.id.txt_message);
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        textView.setText("לא נשלח");
                        toast.setView(toastView);
                        toast.show();
                    }

                }, throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void sendNews(String title, String content) {
        Map<String, String> notificationData = new HashMap<>();
        notificationData.put(Common.TITLE_KEY, title);
        notificationData.put(Common.CONTENT_KEY, content);
        notificationData.put(Common.IS_SEND_IMAGE, "false");

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(), notificationData);

        fcmSendData.setTo(Common.getNewsTopic());
        fcmSendData.setData(notificationData);

        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Waiting...").create();
        dialog.show();

        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                    dialog.dismiss();
                    if (fcmResponse.getMessage_id() != 0) {
                        Toast toast = new Toast(StaffHomeActivity.this);
                        toast.setDuration(Toast.LENGTH_LONG);
                        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                        TextView textView = toastView.findViewById(R.id.txt_message);
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        textView.setText("נשלח");
                        toast.setView(toastView);
                        toast.show();
                    }

                    else {
                        Toast toast = new Toast(StaffHomeActivity.this);
                        toast.setDuration(Toast.LENGTH_LONG);
                        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                        TextView textView = toastView.findViewById(R.id.txt_message);
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        textView.setText("לא נשלח");
                        toast.setView(toastView);
                        toast.show();
                    }

                }, throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void loadNotification() {
        notificationCollection.whereEqualTo("read", false)
                .get()
                .addOnFailureListener(e -> Toast.makeText(StaffHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        iNotificationCountListener.onNotificationCountSuccess(task.getResult().size());
                    }
                });
    }

    @Override
    public void onNotificationCountSuccess(int count) {
        if (count == 0)
            txt_notification_badge.setVisibility(View.INVISIBLE);
        else
        {
            txt_notification_badge.setVisibility(View.VISIBLE);
            if (count <= 9)
                txt_notification_badge.setText(String.valueOf(count));
            else
                txt_notification_badge.setText("9+");
        }
    }

    private void logOut() {
        //just delete all remember key and start main activity
        Paper.init(this);
        Paper.book().delete(Common.SALON_KEY);
        Paper.book().delete(Common.BARBER_KEY);
        Paper.book().delete(Common.STATE_KEY);
        Paper.book().delete(Common.LOGGED_KEY);

        new AlertDialog.Builder(this)
                .setMessage("האם את רוצה להתנתק ?")
                .setCancelable(false)
                .setPositiveButton("כן", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent mainActivity = new Intent(StaffHomeActivity.this, MainActivity.class);
                        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainActivity);
                        finish();
                    }
                }).setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("האם את רוצה להתנתק?")
                .setCancelable(false)
                .setPositiveButton("כן", (dialogInterface, i) ->
                        Toast.makeText(StaffHomeActivity.this, "היציאה בצד שמאל קליק על הלוגו", Toast.LENGTH_SHORT).show())
                .setNegativeButton("ביטול", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    private void initNotificationRealtimeUpdate() {
        notificationCollection = FirebaseFirestore.getInstance()
                .collection("Notifications");

        notificationEvent = (queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots.size() > 0)
                loadNotification();
        };

        //only listen and count all notification unread
        notificationListener = notificationCollection.whereEqualTo("read", false)
                .addSnapshotListener(notificationEvent);
    }

    private void init() {
        iNotificationCountListener = this;
        initNotificationRealtimeUpdate();

        // Init
        Slider.init(new PicassoImageLoadingService());
        iLookbookLoadListener = this;
        iBannerLoadListener = this;

        loadBanner();
        loadLookBook();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initNotificationRealtimeUpdate();
    }

    @Override
    protected void onStop() {
        if (notificationListener != null) {
            notificationListener.remove();
        }
        compositeDisposable.clear();
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (notificationListener != null) {
            notificationListener.remove();
        }

        super.onDestroy();
    }

    private void loadBanner(){
        bannerRef.get()
                .addOnCompleteListener(task -> {
                    List<Banner> banners = new ArrayList<>();
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot bannerSnapShot:task.getResult())
                        {
                            Banner banner = bannerSnapShot.toObject(Banner.class);
                            banners.add(banner);
                        }
                        iBannerLoadListener.onBannerLoadSuccess(banners);
                    }
                }).addOnFailureListener(e -> iBannerLoadListener.onBannerLoadFailed(e.getMessage()));
    }

    private void loadLookBook() {
        lookbookRef.get()
                .addOnCompleteListener(task -> {
                    List<Banner> lookbooks = new ArrayList<>();
                    if (task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot bannerSnapShot:task.getResult())
                        {
                            Banner banner = bannerSnapShot.toObject(Banner.class);
                            lookbooks.add(banner);
                        }
                        iLookbookLoadListener.onLookbookSuccess(lookbooks);
                    }
                }).addOnFailureListener(e ->
                iLookbookLoadListener.onLookbookFailed(e.getMessage()));
    }

    @Override
    public void onBannerLoadSuccess(List<Banner> banners) {
        banner_slider.setAdapter(new HomeSliderAdapter(banners));
    }

    @Override
    public void onBannerLoadFailed(String message) {
        Toast.makeText(StaffHomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLookbookSuccess(List<Banner> banners) {
        recycler_look_book.setHasFixedSize(true);
        recycler_look_book.setLayoutManager(new LinearLayoutManager(this));
        recycler_look_book.setAdapter(new LookbookAdapter(this, banners));
    }

    @Override
    public void onLookbookFailed(String message) {
        Toast.makeText(StaffHomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if (data != null && data.getData() != null)
            {
                imgUri = data.getData();
                img_upload.setImageURI(imgUri);
            }
        }
    }

}