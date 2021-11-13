package com.example.zgbeautyandhairstaff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zgbeautyandhairstaff.Adapter.MyTimeSlotAdapter;
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Common.SpacesItemDecoration;
import com.example.zgbeautyandhairstaff.Interface.ITimeSlotLoadListener;
import com.example.zgbeautyandhairstaff.Model.BookingInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.model.CalendarItemStyle;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarPredicate;
import dmax.dialog.SpotsDialog;

public class CalendarActivity extends AppCompatActivity implements ITimeSlotLoadListener {

    DocumentReference barberDoc;
    ITimeSlotLoadListener iTimeSlotLoadListener;
    android.app.AlertDialog dialog;

    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;

    @BindView(R.id.txt_close_barber)
    TextView txt_close_barber;

    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;

    CollectionReference currentBookDateCollection;

    EventListener<QuerySnapshot> bookingEvent;

    ListenerRegistration bookingRealtimeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        ButterKnife.bind(this);

        txt_close_barber = findViewById(R.id.txt_close_barber);

        init();
        initView();
    }

    private void initView() {

        //copy from z&g beauty and hair app (client app)
        dialog  = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();

        Calendar date = Calendar.getInstance();

        date.add(Calendar.DATE,0);
        loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                Common.simpleDateFormat.format(date.getTime()));

        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3);
        recycler_time_slot.setLayoutManager(gridLayoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE,0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE,30);

        HorizontalDates disableDate = new HorizontalDates(Calendar.SATURDAY,Calendar.FRIDAY);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate,endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .configure()
                .textSize(20f, 50f, 20f)
                .formatTopText("yyyy")
                .formatMiddleText("MMM"+ " " +"dd")
                .end()
                .disableDates(disableDate)
                .build();


        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis())
                {
                    Common.bookingDate = date;
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                            Common.simpleDateFormat.format(date.getTime()));
                }

            }
        });

    }

    private void loadAvailableTimeSlotOfBarber(final String barberId, final String bookDate) {
        dialog.show();

        //Get Information of this barber
        barberDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                DocumentSnapshot documentSnapshot = task.getResult();

                //if barber available
                if (documentSnapshot.exists())
                {
                    //Get information of booking
                    //If not created , return empty

                    CollectionReference date = FirebaseFirestore.getInstance()
                            .collection("AllSalon")
                            .document(Common.state_name)
                            .collection("Branch")
                            .document(Common.selected_salon.getSalonId())
                            .collection("Barbers")
                            .document(barberId)
                            .collection(bookDate);

                    date.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();

                                //if don't have any appointment
                                if (querySnapshot.isEmpty())
                                    iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                                else {
                                    //If have appointment
                                    List<BookingInformation> timeSlots = new ArrayList<>();
                                    for (QueryDocumentSnapshot document : task.getResult())
                                        timeSlots.add(document.toObject(BookingInformation.class));
                                    iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                }
                            }
                        }
                    }).addOnFailureListener(e -> iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage()));
                }
            }
        });
    }

    private void init() {
        iTimeSlotLoadListener = this;
        initBookingRealtimeUpdate();
    }

    private void initBookingRealtimeUpdate() {
        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document("ראשון לציון")
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId());

        //Get current date
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        bookingEvent = (queryDocumentSnapshots, e) -> {
            //if have any new booking, update adapter
            loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                    Common.simpleDateFormat.format(date.getTime()));
        };
        currentBookDateCollection = barberDoc.collection(Common.simpleDateFormat.format(date.getTime()));
        bookingRealtimeListener = currentBookDateCollection.addSnapshotListener(bookingEvent);
    }

    @Override
    public void onTimeSlotLoadSuccess(List<BookingInformation> timeSlotList) {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this,timeSlotList);
        recycler_time_slot.setAdapter(adapter);
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(this, ""+message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this);
        recycler_time_slot.setAdapter(adapter);

        dialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBookingRealtimeUpdate();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (bookingRealtimeListener != null) {
            bookingRealtimeListener.remove();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (bookingRealtimeListener != null) {
            bookingRealtimeListener.remove();
        }
        super.onDestroy();
    }

    public void back(View view) {
        startActivity(new Intent(CalendarActivity.this, BookingActivity.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public class HorizontalDates implements HorizontalCalendarPredicate {
        private int friday;
        private int saturday;


        public HorizontalDates(int saturday, int friday) {
            this.friday = friday;
            this.saturday = saturday;
        }

        @Override
        public boolean test(Calendar selDate) {
            int dayOfWek = selDate.get(Calendar.DAY_OF_WEEK);
            Calendar c = Calendar.getInstance();
            DateFormat df = new SimpleDateFormat("EEE dd/MM/yyyy");
            String closeBarber = "מספרה סגורה\n אין תורים פנויים";


            if (dayOfWek == friday || dayOfWek == saturday)
            {
                recycler_time_slot.setVisibility(View.GONE);
                txt_close_barber.setText(new StringBuilder().append(closeBarber));
                txt_close_barber.setVisibility(View.VISIBLE);

            } else {
                recycler_time_slot.setVisibility(View.VISIBLE);
            }
            return (dayOfWek==friday || dayOfWek==saturday);
        }

        @Override
        public CalendarItemStyle style() {
            return null;
        }
    }

}