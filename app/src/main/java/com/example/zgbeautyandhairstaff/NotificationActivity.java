package com.example.zgbeautyandhairstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.zgbeautyandhairstaff.Adapter.MyNotificationAdapter;
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Interface.INotificationCountListener;
import com.example.zgbeautyandhairstaff.Interface.INotificationLoadListener;
import com.example.zgbeautyandhairstaff.Model.MyNotification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationActivity extends AppCompatActivity implements INotificationLoadListener {
    @BindView(R.id.recycler_notification)
    RecyclerView recycler_notification;

    CollectionReference notificationCollection;

    INotificationLoadListener iNotificationLoadListener;

    int total_item = 0,last_visible_item;
    boolean isLoading = false, isMaxData = false;
    DocumentSnapshot finalDoc;
    MyNotificationAdapter adapter;
    List<MyNotification> firstList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ButterKnife.bind(this);
        init();
        initView();
        loadNotification(null);
    }

    private void initView() {
        recycler_notification.setHasFixedSize(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_notification.setLayoutManager(layoutManager);

        recycler_notification.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                total_item = layoutManager.getItemCount();
                last_visible_item = layoutManager.findLastVisibleItemPosition();
                if(!isLoading &&
                        total_item <= (last_visible_item + Common.MAX_NOTIFICATION_PER_LOAD)){


                    loadNotification(finalDoc);
                    isLoading = true;
                }
            }
        });
    }

    private void loadNotification(DocumentSnapshot lastDoc) {

        notificationCollection = FirebaseFirestore.getInstance()
                .collection("Notifications");

        if (lastDoc == null){
            notificationCollection.orderBy("serverTimestamp", Query.Direction.DESCENDING)
                    .limit(Common.MAX_NOTIFICATION_PER_LOAD)
                    .get()
                    .addOnFailureListener(e ->
                            iNotificationLoadListener.onNotificationLoadFailed(e.getMessage()))
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            List<MyNotification> myNotifications = new ArrayList<>();
                            DocumentSnapshot finalDoc = null;
                            for (DocumentSnapshot notiSnapShot:task.getResult()){

                                MyNotification myNotification = notiSnapShot.toObject(MyNotification.class);
                                myNotifications.add(myNotification);
                                finalDoc = notiSnapShot;

                            }
                            iNotificationLoadListener.onNotificationLoadSuccess(myNotifications, finalDoc);
                        }
                    });
        }
        else
        {
            if(!isMaxData){

                notificationCollection.orderBy("serverTimestamp", Query.Direction.DESCENDING)
                        .startAfter(lastDoc)
                        .limit(Common.MAX_NOTIFICATION_PER_LOAD)
                        .get()
                        .addOnFailureListener(e -> iNotificationLoadListener.onNotificationLoadFailed(e.getMessage()))
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                List<MyNotification> myNotifications = new ArrayList<>();
                                DocumentSnapshot finalDoc = null;
                                for (DocumentSnapshot notiSnapShot:task.getResult()){

                                    MyNotification myNotification = notiSnapShot.toObject(MyNotification.class);
                                    myNotifications.add(myNotification);
                                    finalDoc = notiSnapShot;

                                }
                                iNotificationLoadListener.onNotificationLoadSuccess(myNotifications, finalDoc);
                            }
                        });
            }
        }

    }

    private void init() {
        iNotificationLoadListener = this;

    }

    @Override
    public void onNotificationLoadSuccess(List<MyNotification> myNotificationList, DocumentSnapshot lastDocument) {
        if (lastDocument != null)
        {
            if (lastDocument.equals(finalDoc))
                isMaxData=true;
            else
            {
                finalDoc = lastDocument;
                isMaxData = false;
            }

            if (adapter == null && firstList.size() == 0)
            {
                adapter = new MyNotificationAdapter(this,myNotificationList);
                firstList = myNotificationList;
            }
            else
            {
                if (!myNotificationList.equals(firstList))
                    adapter.updateList(myNotificationList);
            }

            recycler_notification.setAdapter(adapter);
        }

    }

    @Override
    public void onNotificationLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }

    public void back(View view) {
        startActivity(new Intent(NotificationActivity.this, StaffHomeActivity.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
