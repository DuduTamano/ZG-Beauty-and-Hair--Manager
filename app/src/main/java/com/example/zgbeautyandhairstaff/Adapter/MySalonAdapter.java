package com.example.zgbeautyandhairstaff.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Common.CustomLoginDialog;
import com.example.zgbeautyandhairstaff.Interface.IDialogClickListener;
import com.example.zgbeautyandhairstaff.Interface.IGetBarberListener;
import com.example.zgbeautyandhairstaff.Interface.IRecyclerItemSelectedListener;
import com.example.zgbeautyandhairstaff.Interface.IUserLoginRememberListener;
import com.example.zgbeautyandhairstaff.Model.Barber;
import com.example.zgbeautyandhairstaff.Model.Salon;
import com.example.zgbeautyandhairstaff.R;
import com.example.zgbeautyandhairstaff.StaffHomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MySalonAdapter extends RecyclerView.Adapter<MySalonAdapter.MyViewHolder> implements IDialogClickListener {

    Context context;
    List<Salon> salonList;
    List<CardView> cardViewList;

    IUserLoginRememberListener iUserLoginRememberListener;
    IGetBarberListener iGetBarberListener;

    public MySalonAdapter(Context context, List<Salon> salonList, IUserLoginRememberListener iUserLoginRememberListener, IGetBarberListener iGetBarberListener) {
        this.context = context;
        this.salonList = salonList;
        cardViewList = new ArrayList<>();
        this.iUserLoginRememberListener = iUserLoginRememberListener;
        this.iGetBarberListener = iGetBarberListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_salon,viewGroup,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {
        myViewHolder.txt_salon_name.setText(salonList.get(i).getName());
        myViewHolder.txt_salon_address.setText(salonList.get(i).getAddress());
        if (!cardViewList.contains(myViewHolder.card_salon))
            cardViewList.add(myViewHolder.card_salon);


        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int pos) {
                Common.selected_salon = salonList.get(pos);
                showLoginDialog();
            }
        });
    }

    private void showLoginDialog() {
        CustomLoginDialog.getInstance()
                .showLoginDialog("כניסת מנהל",
                        "כניסה",
                        "ביטול",
                        context,
                        this);
    }

    @Override
    public int getItemCount() {
        return salonList.size();
    }

    @Override
    public void onClickPositiveButton(DialogInterface dialogInterface, String userName, String password) {
        //Show loading dialog
        final AlertDialog loading = new SpotsDialog.Builder().setCancelable(false).setContext(context).build();

        loading.show();

        ///AllSalon/ראשון לציון/Branch/qxhrdAtz2HdDLqTxsWZe/Barbers/CZr1rU4rjFcgSf7rU1RM
        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Barbers")
                .whereEqualTo("username",userName)
                .whereEqualTo("password",password)
                .limit(1)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

                        loading.dismiss();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            if (task.getResult().size() > 0)
                            {
                                dialogInterface.dismiss();
                                loading.dismiss();

                                iUserLoginRememberListener.onUserLoginSuccess(userName);

                                //create barber
                                Barber barber = new Barber();
                                for (DocumentSnapshot barberSnapShot:task.getResult())
                                {
                                    barber = barberSnapShot.toObject(Barber.class);
                                    barber.setBarberId(barberSnapShot.getId());
                                }

                                iGetBarberListener.onGetBarberSuccess(barber);

                                //we will navigate staff home and clear all previous activity
                                Intent staffHome = new Intent(context, StaffHomeActivity.class);
                                staffHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                staffHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(staffHome);
                            }
                            else
                            {
                                loading.dismiss();

                                Toast toast = new Toast(context);
                                toast.setDuration(Toast.LENGTH_LONG);
                                View toastView = ((Activity)context).getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) ((Activity)context).findViewById(R.id.root_layout));
                                TextView textView = toastView.findViewById(R.id.txt_message);
                                toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                textView.setText("שם המשתמש או הסיסמה לא נכונים");
                                toast.setView(toastView);
                                toast.show();

                            }
                        }
                    }
                });
    }

    @Override
    public void onClickNegativeButton(DialogInterface dialogInterface) {
        dialogInterface.dismiss();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_salon_name,txt_salon_address;
        CardView card_salon;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            card_salon = (CardView)itemView.findViewById(R.id.card_salon);
            txt_salon_address = (TextView)itemView.findViewById(R.id.txt_salon_address);
            txt_salon_name = (TextView)itemView.findViewById(R.id.txt_salon_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelected(v,getAdapterPosition());
        }
    }
}
