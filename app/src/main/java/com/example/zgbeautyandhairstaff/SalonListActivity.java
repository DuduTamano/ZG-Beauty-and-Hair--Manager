package com.example.zgbeautyandhairstaff;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zgbeautyandhairstaff.Adapter.MySalonAdapter;
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Common.SpacesItemDecoration;
import com.example.zgbeautyandhairstaff.Interface.IBranchLoadListener;
import com.example.zgbeautyandhairstaff.Interface.IGetBarberListener;
import com.example.zgbeautyandhairstaff.Interface.IOnLoadCountSalon;
import com.example.zgbeautyandhairstaff.Interface.IUserLoginRememberListener;
import com.example.zgbeautyandhairstaff.Model.Barber;
import com.example.zgbeautyandhairstaff.Model.Salon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class SalonListActivity extends AppCompatActivity implements IOnLoadCountSalon, IBranchLoadListener, IGetBarberListener, IUserLoginRememberListener {

    @BindView(R.id.txt_salon_count)
    TextView txt_salon_count;

    @BindView(R.id.recycler_salon)
    RecyclerView recycler_salon;

    IOnLoadCountSalon iOnLoadCountSalon;
    IBranchLoadListener iBranchLoadListener;

    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon_list);
        ButterKnife.bind(this);
        initView();
        init();

        loadSalonBaseOnCity(Common.state_name);
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        iOnLoadCountSalon = this;
        iBranchLoadListener = this;

    }

    private void loadSalonBaseOnCity(String name) {
        dialog.show();
        FirebaseFirestore.getInstance().collection("AllSalon")
                .document(name)
                .collection("Branch")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        List<Salon> salons = new ArrayList<>();
                        iOnLoadCountSalon.onLoadCountSalonSuccess(task.getResult().size());
                        for (DocumentSnapshot salonSnapShot:task.getResult())
                        {
                            Salon salon = salonSnapShot.toObject(Salon.class);
                            salon.setSalonId(salonSnapShot.getId());
                            salons.add(salon);
                        }
                        iBranchLoadListener.onBranchLoadSuccess(salons);
                    }
                }).addOnFailureListener(e -> iBranchLoadListener.onBranchLoadFailed(e.getMessage()));
    }

    private void initView() {
        recycler_salon.setHasFixedSize(true);
        recycler_salon.setLayoutManager(new GridLayoutManager(this,2));
        recycler_salon.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onLoadCountSalonSuccess(int count) {
        txt_salon_count.setText(new StringBuilder(" ")
        .append("Z&G")
        .append(" "));
    }

    @Override
    public void onBranchLoadSuccess(List<Salon> branchList) {
        MySalonAdapter salonAdapter = new MySalonAdapter(this, branchList, this, this);
        recycler_salon.setAdapter(salonAdapter);

        dialog.dismiss();
    }

    @Override
    public void onBranchLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onGetBarberSuccess(Barber barber) {
        Common.currentBarber = barber;
        Paper.book().write(Common.BARBER_KEY,new Gson().toJson(barber));
    }

    @Override
    public void onUserLoginSuccess(String user) {
        //Save user
        Paper.init(this);
        Paper.book().write(Common.LOGGED_KEY,user);
        Paper.book().write(Common.STATE_KEY, Common.state_name);
        Paper.book().write(Common.SALON_KEY,new Gson().toJson(Common.selected_salon));
    }
}
