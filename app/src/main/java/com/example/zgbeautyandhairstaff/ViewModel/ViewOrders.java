package com.example.zgbeautyandhairstaff.ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.zgbeautyandhairstaff.Interface.IOrderCallbackListener;
import com.example.zgbeautyandhairstaff.Model.Order;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ViewOrders extends ViewModel implements IOrderCallbackListener {
    private MutableLiveData<List<Order>> orderModelMutableLiveData;
    private MutableLiveData<String> messageError;

    private IOrderCallbackListener listener;

    public ViewOrders() {
        orderModelMutableLiveData = new MutableLiveData<>();
        messageError = new MutableLiveData<>();
        listener = this;

    }


    public MutableLiveData<List<Order>> getOrderModelMutableLiveData() {
       // LoadUser();

        loadOrderByStatus(0);

        return orderModelMutableLiveData;
    }

    public void loadOrderByStatus(int status) {

        List<Order> orderList = new ArrayList<>();

        FirebaseFirestore.getInstance()
                .collection("Users")
                .get().addOnCompleteListener( new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot snapshot : task.getResult()) {

                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document( snapshot.getId())
                                .collection("Orders")
                                .whereEqualTo("orderStatus", status)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        List<DocumentSnapshot> snapshotList= queryDocumentSnapshots.getDocuments();
                                        for (DocumentSnapshot orderSnapshot : snapshotList) {
                                            Order orderModel = orderSnapshot.toObject(Order.class);
                                            orderModel.setKey(orderSnapshot.getId());
                                            orderList.add(orderModel);
                                        }

                                        listener.onOrderLoadSuccess(orderList);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                listener.onOrderLoadFailed(e.getMessage());
                            }
                        });
                    }

                }
            }
        } );
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onOrderLoadSuccess(List<Order> orderList) {
        if (orderList.size() > 0)
        {
            Collections.sort(orderList, (order, t1) -> {
                if (order.getCreateDate() < t1.getCreateDate())
                    return -1;
                return order.getCreateDate() == t1.getCreateDate() ? 0:1;
            });
        }

        orderModelMutableLiveData.setValue(orderList);
    }

    @Override
    public void onOrderLoadFailed(String message) {
        messageError.setValue(message);
    }
}
