package com.example.zgbeautyandhairstaff;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.example.zgbeautyandhairstaff.Common.Common;
import com.example.zgbeautyandhairstaff.Model.CartItem;
import com.example.zgbeautyandhairstaff.Fragments.ShoppingFragment;
import com.example.zgbeautyandhairstaff.Fragments.TotalPriceFragment;
import com.example.zgbeautyandhairstaff.Interface.IBarberServicesLoadListener;
import com.example.zgbeautyandhairstaff.Interface.IOnShoppingItemSelected;
import com.example.zgbeautyandhairstaff.Model.BarberServices;
import com.example.zgbeautyandhairstaff.Model.EventBus.DismissFromBottomSheetEvent;
import com.example.zgbeautyandhairstaff.Model.Order;
import com.example.zgbeautyandhairstaff.Model.ProductItem;
import com.example.zgbeautyandhairstaff.Model.ShoppingItem;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

public class DoneServiceActivity extends AppCompatActivity implements IBarberServicesLoadListener, IOnShoppingItemSelected {

    private static final int MY_CAMERA_REQUEST_CODE = 1000;
    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;

    @BindView(R.id.txt_customer_phone)
    TextView txt_customer_phone;

    @BindView(R.id.chip_group_services)
    ChipGroup chip_group_services;

    @BindView(R.id.chip_group_shopping)
    ChipGroup chip_group_shopping;

    @BindView(R.id.edt_services)
    AppCompatAutoCompleteTextView edt_services;

    @BindView(R.id.img_customer_hair)
    ImageView img_customer_hair;

    @BindView(R.id.userImage)
    ImageView userImage;

    @BindView(R.id.call_customer)
    ImageView call_customer;

    @BindView(R.id.add_shopping)
    Button add_shopping;

    @BindView(R.id.btn_finish)
    Button btn_finish;

    @BindView(R.id.rdi_no_picture)
    RadioButton rdi_no_picture;

    @BindView(R.id.rdi_picture)
    RadioButton rdi_picture;

    AlertDialog dialog;

    IBarberServicesLoadListener iBarberServicesLoadListener;

    HashSet<BarberServices> serviceAdded = new HashSet<>();

    LayoutInflater inflater;

    Uri fileUri;

    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done_service);

        ButterKnife.bind(this);

        call_customer = findViewById(R.id.call_customer);

        call_customer.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallCustomer();
            }
        } );

        init();

        initView();

        setCustomerInformation();

        loadBarberServices();
    }

    private void CallCustomer() {
        Dexter.withActivity(DoneServiceActivity.this)
                .withPermission( Manifest.permission.CALL_PHONE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse(new StringBuilder("tel: ")
                                .append(Common.currentBookingInformation.getCustomerPhone()).toString()));
                        startActivity(intent);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                        Toast toast = new Toast(DoneServiceActivity.this);
                        toast.setDuration(Toast.LENGTH_LONG);
                        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                        TextView textView = toastView.findViewById(R.id.txt_message);
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        textView.setText("You must accept" + response.getPermissionName());
                        toast.setView(toastView);
                        toast.show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
    }

    private void initView() {

        rdi_picture.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
            {
                img_customer_hair.setVisibility(View.VISIBLE);
                btn_finish.setEnabled(false);
            }
        });

        rdi_no_picture.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
            {
                img_customer_hair.setVisibility(View.GONE);
                btn_finish.setEnabled(true);
            }
        });

        btn_finish.setOnClickListener(view -> {
            if (rdi_no_picture.isChecked())
            {
                dialog.dismiss();

                TotalPriceFragment fragment = TotalPriceFragment.getInstance();
                Bundle bundle = new Bundle();
                bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(serviceAdded));
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), "מחיר");
            }
            else
            {
                uploadPicture(fileUri);
            }
        });

        img_customer_hair.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            fileUri = getOutputMediaFileUri();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, MY_CAMERA_REQUEST_CODE);
        });

        add_shopping.setOnClickListener(view -> {
            ShoppingFragment shoppingFragment = ShoppingFragment.getInstance(DoneServiceActivity.this);
            shoppingFragment.show(getSupportFragmentManager(), "Shopping");
        });
    }

    private void uploadPicture(Uri fileUri) {
        if(fileUri != null)
        {
            dialog.show();

            String fileName = Common.getFileName(getContentResolver(), fileUri);
            String path = new StringBuilder("Customer_Pictures/")
                    .append(fileName)
                    .toString();
            storageReference = FirebaseStorage.getInstance().getReference(path);

            UploadTask uploadTask = storageReference.putFile(fileUri);

            //Tao task
            Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {

                        Toast toast = new Toast(DoneServiceActivity.this);
                        toast.setDuration(Toast.LENGTH_LONG);
                        View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                        TextView textView = toastView.findViewById(R.id.txt_message);
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        textView.setText("Failed to upload");
                        toast.setView(toastView);
                        toast.show();
                    }

                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        String url = task.getResult().toString()
                                .substring(0, task.getResult().toString().indexOf("&token"));

                        Log.d("DOWNLOADABLE_LINK", url);
                        dialog.dismiss();

                        TotalPriceFragment fragment = TotalPriceFragment.getInstance();
                        Bundle bundle = new Bundle();
                        bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(serviceAdded));
                        bundle.putString(Common.IMAGE_DOWNLOADABLE_URL, url);
                        fragment.setArguments(bundle);
                        fragment.show(DoneServiceActivity.this.getSupportFragmentManager(), "מחיר");
                    }
                }
            }).addOnFailureListener(e -> {
                dialog.dismiss();
                Toast.makeText(DoneServiceActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
        else
        {

            Toast toast = new Toast(DoneServiceActivity.this);
            toast.setDuration(Toast.LENGTH_LONG);
            View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
            TextView textView = toastView.findViewById(R.id.txt_message);
            toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
            textView.setText("Image is empty");
            toast.setView(toastView);
            toast.show();
        }
    }

    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Z&G Staff App");
        if (!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdir())
            return null;
        }

        String time_stamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File mediaFile = new File(mediaStorageDir.getPath()+File.separator+"IMG_"
                +time_stamp+"_"+new Random().nextInt()+".jpg");

        return mediaFile;
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this)
                .setCancelable(false)
                .build();

        inflater = LayoutInflater.from(this);

        iBarberServicesLoadListener = this;
    }

    private void loadBarberServices() {
        dialog.show();

        ///AllSalon/ראשון לציון/Branch/qxhrdAtz2HdDLqTxsWZe/Services
        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Services")
                .get()
                .addOnFailureListener(e -> iBarberServicesLoadListener.onBarberServicesLoadFailed(e.getMessage())).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        List<BarberServices> barberServices = new ArrayList<>();
                        for (DocumentSnapshot barberSnapShot: task.getResult())
                        {
                            BarberServices services = barberSnapShot.toObject(BarberServices.class);
                            barberServices.add(services);
                        }
                        iBarberServicesLoadListener.onBarberServicesLoadSuccess(barberServices);
                    }
                });
    }

    private void setCustomerInformation() {
        txt_customer_name.setText(Common.currentBookingInformation.getCustomerName());
        txt_customer_phone.setText(Common.currentBookingInformation.getCustomerPhone().replaceAll("[+]972", "0"));
        Picasso.get().load(Common.currentBookingInformation.getCustomerImage()).into(userImage);

    }

    @Override
    public void onBarberServicesLoadSuccess(List<BarberServices> barberServicesList) {
        List<String> nameServices = new ArrayList<>();

        //Sort alpha-bet
        Collections.sort(barberServicesList, new Comparator<BarberServices>() {
            @Override
            public int compare(BarberServices barberServices, BarberServices t1) {
                return barberServices.getName().compareTo(t1.getName());
            }
        });

        //add all name of services after sort
        for (BarberServices barberServices:barberServicesList)
            nameServices.add(barberServices.getName());

        //create Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, nameServices);
        //will start working from first character
        edt_services.setThreshold(1);
        edt_services.setAdapter(adapter);
        edt_services.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //add to Chip Group
                int index = nameServices.indexOf(edt_services.getText().toString().trim());

                if (!serviceAdded.contains(barberServicesList.get(index))) {
                    //we don't want to have duplicate service in list so we use hashSet
                    serviceAdded.add(barberServicesList.get(index));
                    final Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
                    item.setText(edt_services.getText().toString());
                    item.setTag(index);
                    edt_services.setText("");

                    item.setOnCloseIconClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            chip_group_services.removeView(view);
                            serviceAdded.remove((int) item.getTag());
                        }
                    });
                    chip_group_services.addView(item);
                } else {
                    edt_services.setText("");
                }
            }
        });

        loadExtraItems();
    }

    @Override
    public void onBarberServicesLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem, ProductItem productItem) {
        //here we will create an list to hold shopping item
        //shoppingItems.add(shoppingItem);

        //Create new Cart item
        CartItem cartItem = new CartItem();
        cartItem.setProductId(shoppingItem.getId());
        cartItem.setProductImage(shoppingItem.getImage());
        cartItem.setProductName(shoppingItem.getName());


        cartItem.setProductQuantity(1);
        cartItem.setUserPhone(Common.currentBookingInformation.getCustomerPhone());

        if (shoppingItem.getPrice() > 0)
        {

            cartItem.setProductPrice(shoppingItem.getPrice());
        }

        if (productItem.getSize_price() > 0)
        {

            cartItem.setProductPrice(productItem.getSize_price());
        }

        //if user submit with empty cart
        if(Common.currentBookingInformation.getCartItemList() == null)
        {
            Common.currentBookingInformation.setCartItemList(new ArrayList<CartItem>());
        }

        //we will use this flag to update cart item quantity increase by 1
        boolean flag = false;

        //if already have item with same name in cart
        for (int i = 0; i< Common.currentBookingInformation.getCartItemList().size(); i++)
        {
            if(Common.currentBookingInformation.getCartItemList().get(i).getProductName()
                    .equals(shoppingItem.getName()))
            {
                //enable flag
                flag = true;
                CartItem itemUpdate = Common.currentBookingInformation.getCartItemList().get(i);
                itemUpdate.setProductQuantity(itemUpdate.getProductQuantity()+1);
                //update list
                Common.currentBookingInformation.getCartItemList().set(i,itemUpdate);

                Toast toast = new Toast(DoneServiceActivity.this);
                toast.setDuration(Toast.LENGTH_LONG);
                View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
                TextView textView = toastView.findViewById(R.id.txt_message);
                toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                textView.setText("פריט נוסף בהצלחה");
                toast.setView(toastView);
                toast.show();
            }
        }

        //if flag = false
        if(!flag)
        {
            Common.currentBookingInformation.getCartItemList().add(cartItem);

            Toast toast = new Toast(DoneServiceActivity.this);
            toast.setDuration(Toast.LENGTH_LONG);
            View toastView = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.root_layout));
            TextView textView = toastView.findViewById(R.id.txt_message);
            toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
            textView.setText("פריט נוסף בהצלחה");
            toast.setView(toastView);
            toast.show();

            final Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
            item.setText(cartItem.getProductName());
            item.setTag(Common.currentBookingInformation.getCartItemList().indexOf(cartItem));

            item.setOnCloseIconClickListener(view -> {
                chip_group_shopping.removeView(view);
                Common.currentBookingInformation.getCartItemList().remove((int)item.getTag());
            });

            chip_group_shopping.addView(item);
        }
        else //flag = true, item update
        {
            chip_group_shopping.removeAllViews();

            loadExtraItems();
        }

    }

    private void loadExtraItems() {
        if(Common.currentBookingInformation.getCartItemList() != null)
        {
            for(CartItem cartItem: Common.currentBookingInformation.getCartItemList())
            {
                final Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
                item.setText(new StringBuilder(cartItem.getProductName()).append(" x ").append(cartItem.getProductQuantity()));
                item.setTag(Common.currentBookingInformation.getCartItemList().indexOf(cartItem));

                item.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        chip_group_shopping.removeView(view);
                        Common.currentBookingInformation.getCartItemList().remove((int)item.getTag());
                    }
                });

                chip_group_shopping.addView(item);
            }
        }
        dialog.dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MY_CAMERA_REQUEST_CODE)
        {
            if(resultCode == RESULT_OK)
            {
                Bitmap bitmap = null;
                ExifInterface ei = null;
                try{
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
                    ei = new ExifInterface(getContentResolver().openInputStream(fileUri));

                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                    Bitmap rotateBitmap = null;
                    switch (orientation)
                    {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotateBitmap = rotateImage(bitmap,90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotateBitmap = rotateImage(bitmap,180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotateBitmap = rotateImage(bitmap,270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:

                        default:
                            rotateBitmap = bitmap;
                            break;
                    }
                    img_customer_hair.setImageBitmap(rotateBitmap);
                    btn_finish.setEnabled(true);
                }
                catch (FileNotFoundException e){
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap rotateImage(Bitmap bitmap, int i) {
        Matrix matrix = new Matrix();
        matrix.postRotate(i);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //EventBus
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void dismissDialog(DismissFromBottomSheetEvent event)
    {
        if(event.isButtonClick())
        {
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
