package com.example.zgbeautyandhairstaff.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Interface.IOnShoppingItemSelected;
import com.example.zgbeautyandhairstaff.Interface.IRecyclerItemSelectedListener;
import com.example.zgbeautyandhairstaff.Model.CartItem;
import com.example.zgbeautyandhairstaff.Model.ProductItem;
import com.example.zgbeautyandhairstaff.Model.ShoppingItem;
import com.example.zgbeautyandhairstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyShoppingItemAdapter extends RecyclerView.Adapter<MyShoppingItemAdapter.MyViewHolder> {

    Context context;
    List<ShoppingItem> shoppingItemList;
    IOnShoppingItemSelected iOnShoppingItemSelected;
    String[] itemList;
    ArrayAdapter<String> adapter;
    FirebaseFirestore firebaseFirestore;
    ProductItem productItem = new ProductItem();


    public MyShoppingItemAdapter(Context context, List<ShoppingItem> shoppingItemList, IOnShoppingItemSelected iOnShoppingItemSelected) {
        this.context = context;
        this.shoppingItemList = shoppingItemList;
        this.iOnShoppingItemSelected = iOnShoppingItemSelected;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_shopping_item,viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, @SuppressLint("RecyclerView") int i) {
        Picasso.get().load( shoppingItemList.get( i ).getImage() ).into( myViewHolder.img_shopping_item );
        myViewHolder.txt_item_shopping_name.setText( Common.formatItemShoppingName( shoppingItemList.get( i ).getName() ) );
        myViewHolder.txt_item_shopping_price.setText( new StringBuilder( " ₪" ).append( shoppingItemList.get( i ).getPrice() ) );

        myViewHolder.itemView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(shoppingItemList.get(i));
            }
        } );
    }

    private void showDialog(ShoppingItem shoppingItem) {
        View layout_dialog = LayoutInflater.from(context).inflate(R.layout.layout_dialog_product_details, null);
        Dialog builder = new Dialog(context, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        builder.setContentView(layout_dialog);

        //product details
        Spinner spinner = (Spinner) layout_dialog.findViewById(R.id.spinner);
        LinearLayout spinnerLinear = (LinearLayout) layout_dialog.findViewById(R.id.spinnerLinear);
        Button txt_add_to_cart = (Button) layout_dialog.findViewById(R.id.txt_add_to_cart);
        Button txt_add_to_cart2 = (Button) layout_dialog.findViewById(R.id.txt_add_to_cart2);
        TextView txt_name_shopping_item = (TextView) layout_dialog.findViewById(R.id.txt_name_shopping_item);
        TextView txt_price_shopping_item = (TextView) layout_dialog.findViewById(R.id.txt_price_shopping_item);
        ImageView img_shopping_item = (ImageView) layout_dialog.findViewById(R.id.img_shopping_item);
        TextView description = (TextView) layout_dialog.findViewById(R.id.description);
        ImageView back_pressed = (ImageView) layout_dialog.findViewById(R.id.back_pressed);


        txt_name_shopping_item.setText(shoppingItem.getName());
        txt_price_shopping_item.setText(new StringBuilder("₪").append(shoppingItem.getPrice()));
        description.setText(shoppingItem.getDec());
        Glide.with(context).load(shoppingItem.getImage()).into(img_shopping_item);

        loadId(txt_add_to_cart, txt_add_to_cart2, txt_price_shopping_item, spinner, spinnerLinear, shoppingItem);

        //Show dialog
        builder.create();
        builder.show();

        back_pressed.setOnClickListener(v -> {
            builder.dismiss();
        });

    }

    private void loadId(Button txt_add_to_cart, Button txt_add_to_cart2, TextView txt_price_shopping_item, Spinner spinner, LinearLayout spinnerLinear, ShoppingItem shoppingItem) {
        FirebaseFirestore.getInstance().collection("Shopping")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        String shoppingID = documentSnapshot.getId();

                        String[] NotHari = {" LA BEAUTE מסכת", "שמן אורגני", "סרום לשיער", "ספריי La Beaute", "BIOTOP", "ווקס", "SOVONCARE"};

                        String[] Hair = {"פרונטל 360", "גלי סגור", "קלוז'ר", "גלי פתוח"};

                        firebaseFirestore.collection("Shopping").document(shoppingID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (shoppingID.equals(documentSnapshot.getId())) {

                                        for (int i=0; i<= NotHari.length-1; i++) {
                                            if (shoppingItem.getName().equals(NotHari[i])) {

                                                spinnerLinear.setVisibility(View.GONE);
                                                txt_price_shopping_item.setText(new StringBuilder("₪").append(shoppingItem.getPrice()));
                                                txt_add_to_cart.setVisibility(View.GONE);
                                                addToCart2(shoppingItem, txt_add_to_cart2);
                                                break;
                                            }
                                        }


                                        for (int i=0; i<= Hair.length-1; i++) {
                                            if (shoppingItem.getName().equals(Hair[i])) {

                                                spinnerLinear.setVisibility(View.VISIBLE);
                                                txt_add_to_cart2.setVisibility(View.GONE);
                                                callSizeSpinner(txt_price_shopping_item, spinner);
                                                addToCart(shoppingItem, txt_add_to_cart, productItem);
                                                break;
                                            }
                                        }

                                    }
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast toast = new Toast(context);
                                toast.setDuration(Toast.LENGTH_LONG);
                                View toastView = ((Activity)context).getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) ((Activity)context).findViewById(R.id.root_layout));
                                TextView textView = toastView.findViewById(R.id.txt_message);
                                toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                                textView.setText("Error : " + e.toString());
                                toast.setView(toastView);
                                toast.show();

                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return shoppingItemList.size();
    }

    public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
        this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
    }

    IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txt_item_shopping_name,txt_item_shopping_price;

        ImageView img_shopping_item;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img_shopping_item = (ImageView)itemView.findViewById(R.id.img_shopping_item);
            txt_item_shopping_name = (TextView)itemView.findViewById(R.id.txt_name_shopping_item);
            txt_item_shopping_price = (TextView)itemView.findViewById(R.id.txt_price_shopping_item);


            firebaseFirestore = FirebaseFirestore.getInstance();

        }
    }

    private void callSizeSpinner(TextView txt_price_shopping_item, Spinner spinner) {
        itemList = new String[] {"12", "14", "16", "18", "20", "22", "24"};

        adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, itemList);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0)
                {
                    productItem.setSize_price(200);
                }
                else if (position == 1)
                {
                    productItem.setSize_price(300);
                }
                else if (position == 2)
                {
                    productItem.setSize_price(400);
                }
                else if (position == 3)
                {
                    productItem.setSize_price(500);
                }
                else if (position == 4)
                {
                    productItem.setSize_price(600);
                }
                else if (position == 5)
                {
                    productItem.setSize_price(700);
                }
                else if (position == 6)
                {
                    productItem.setSize_price(800);
                }

                txt_price_shopping_item.setText(new StringBuilder("₪").append(productItem.getSize_price()));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void addToCart2(ShoppingItem shoppingItem, Button txt_add_to_cart2) {
        txt_add_to_cart2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartItem cartItem = new CartItem();
                cartItem.setProductId(shoppingItem.getId());
                cartItem.setProductName(shoppingItem.getName());
                cartItem.setProductImage(shoppingItem.getImage());
                cartItem.setProductQuantity (1);
                cartItem.setProductPrice(shoppingItem.getPrice());
                cartItem.setUserPhone(Common.currentBookingInformation.getCustomerPhone());

                iOnShoppingItemSelected.onShoppingItemSelected(shoppingItem, productItem);
            }
        });
    }

    private void addToCart(ShoppingItem shoppingItem, Button txt_add_to_cart, ProductItem productItem) {
        txt_add_to_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create cart Item
                CartItem cartItem = new CartItem ();
                cartItem.setProductId(shoppingItem.getId());
                cartItem.setProductName(shoppingItem.getName());
                cartItem.setProductImage(shoppingItem.getImage());
                cartItem.setProductQuantity (1);
                cartItem.setProductPrice(productItem.getSize_price() );
                cartItem.setUserPhone(Common.currentBookingInformation.getCustomerPhone());

                // Insert to db
                iOnShoppingItemSelected.onShoppingItemSelected(shoppingItem, MyShoppingItemAdapter.this.productItem);
            }
        });
    }
}
