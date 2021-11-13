package com.example.zgbeautyandhairstaff.ViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.zgbeautyandhairstaff.Interface.IBookingLoadListener;
import com.example.zgbeautyandhairstaff.Model.BookingInformation;
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
import java.util.List;


public class ViewBooking extends ViewModel implements IBookingLoadListener {
    private MutableLiveData<List<BookingInformation>> bookingModelMutableLiveData;
    private MutableLiveData<String> messageError;

    private IBookingLoadListener listener;

    public ViewBooking() {
        bookingModelMutableLiveData = new MutableLiveData<>();
        messageError = new MutableLiveData<>();
        listener = this;
    }

    public MutableLiveData<List<BookingInformation>> getBookingModelMutableLiveData() {

        loadBookingByStatus(0);

        return bookingModelMutableLiveData;
    }

    public void loadBookingByStatus(int status) {
        List<BookingInformation> bookingInformationList = new ArrayList<>();

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
                                .collection("Booking")
                                .whereEqualTo("bookingStatus", status)
                              //  .orderBy("timestamp", Query.Direction.DESCENDING)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        List<DocumentSnapshot> snapshotList= queryDocumentSnapshots.getDocuments();
                                        for (DocumentSnapshot bookingSnapshot : snapshotList) {
                                            BookingInformation bookingModel = bookingSnapshot.toObject(BookingInformation.class);
                                            bookingModel.setBookingId(bookingSnapshot.getId());
                                            bookingInformationList.add(bookingModel);
                                        }

                                        listener.onBookingLoadSuccess(bookingInformationList);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                listener.onBookingLoadFailed(e.getMessage());
                            }
                        });
                    }

                }
            }
        } );
    }

    @Override
    public void onBookingLoadSuccess(List<BookingInformation> bookingInformationList) {

        bookingModelMutableLiveData.setValue(bookingInformationList);
    }

    @Override
    public void onBookingLoadFailed(String message) {
        messageError.setValue(message);
    }
}
