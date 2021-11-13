package com.example.zgbeautyandhairstaff.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.zgbeautyandhairstaff.Model.Barber;
import com.example.zgbeautyandhairstaff.Model.BookingDelete;
import com.example.zgbeautyandhairstaff.Model.BookingInformation;
import com.example.zgbeautyandhairstaff.Model.Category;
import com.example.zgbeautyandhairstaff.Model.MyToken;
import com.example.zgbeautyandhairstaff.Model.Order;
import com.example.zgbeautyandhairstaff.Model.Salon;
import com.example.zgbeautyandhairstaff.Model.ShoppingItem;
import com.example.zgbeautyandhairstaff.Model.User;
import com.example.zgbeautyandhairstaff.Model.UserContact;
import com.example.zgbeautyandhairstaff.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.paperdb.Paper;

public class Common {
    public static final Object DISABLE_TAG = "DISABLE";
    public static final int TIME_SLOT_TOTAL = 9;
    public static final String LOGGED_KEY = "LOGGED";
    public static final String STATE_KEY = "STATE";
    public static final String SALON_KEY = "SALON";
    public static final String BARBER_KEY = "BARBER";
    public static final String TITLE_KEY = "title";
    public static final String CONTENT_KEY = "content";
    public static final int MAX_NOTIFICATION_PER_LOAD = 10;
    public static final String SERVICES_ADDED = "SERVICES_ADDED";
    //Default without no services extra item extra
    public static final double DEFAULT_PRICE = 0;
    public static final String MONEY_SIGN = "₪";
    public static final String IMAGE_DOWNLOADABLE_URL = "DOWNLOADABLE_URL";
    public static final String RATING_STATE_KEY = "RATING_STATE_KEY";
    public static final String RATING_SALON_ID = "RATING_SALON_ID";
    public static final String RATING_SALON_NAME = "RATING_SALON_NAME";
    public static final String RATING_BARBER_ID = "RATING_BARBER_ID";
    public static final String EVENT_URI_CACHE = "URI_EVENT_SAVE";
    public static final String IS_SEND_IMAGE = "IS_SEND_IMAGE";
    public static final String IMAGE_URL = "IMAGE_URL";
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static String state_name="";
    public static Barber currentBarber;
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    public static Calendar bookingDate=Calendar.getInstance();
    public static Salon selected_salon;

    public static BookingInformation currentBookingInformation;
    public static BookingDelete currentBookingId;
    public static Order currentOrder ;
    public static User currentUser;
    public static Category categorySelected;
    public static ShoppingItem selectedItem;
    public static UserContact currentUserContact;


    public static String convertTimeSlotToString(int slot) {
        switch (slot)
        {
            case 0:
                return "10:00-11:00";
            case 1:
                return "11:00-12:00";
            case 2:
                return "12:00-13:00";
            case 3:
                return "13:00-14:00";
            case 4:
                return "14:00-15:00";
            case 5:
                return "15:00-16:00";
            case 6:
                return "16:00-17:00";
            case 7:
                return "17:00-18:00";
            case 8:
                return "18:00-19:00";
            default:
                return "סגור";
        }
    }

    public static String convertTimeStampToStringKey(Timestamp timestamp) {
        Date date = timestamp.toDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return simpleDateFormat.format(date);
    }

    public static String formatItemShoppingName(String name) {
        return name.length() > 13 ? new StringBuilder(name.substring(0,10)).append("...").toString():name;
    }

    public static void showNotification(Context context, int notification_id, String title, String content, Intent intent) {

        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(
                    context,
                    notification_id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

        String NOTIFICATION_CHANNEL_ID = "Z&G Beauty and Hair";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Z&G Beauty and Hair", NotificationManager.IMPORTANCE_DEFAULT);

            //configure the notification channel
            notificationChannel.setDescription("Z&G Beauty and Hair App");
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.zglogo)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.noti_logo));

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }
        Notification notification = builder.build();

        notificationManager.notify(notification_id, notification);
    }

    public static String getFileName(ContentResolver contentResolver, Uri fileUri) {
        String result = null;
        if (fileUri.getScheme().equals("content"))
        {
            Cursor cursor = contentResolver.query(fileUri, null, null, null, null);

            try
            {
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }finally {
                cursor.close();
            }
        }
        if (result == null)
        {
            result = fileUri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1)
                result = result.substring(cut+1);
        }
        return result;
    }

    public static void setSpanStringColor(String order_date, String name, TextView txt_order_time, int color) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(order_date);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(styleSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        txt_order_time.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus)
        {
            case 0:
                return "הזמנה במספרה";
            case 1:
                return "הזמנה נשלחה";
            case 2:
                return "הזמנה הגיעה ליעד";
            case -1:
                return "הזמנה בוטלה";
            default:
                return "Error";
        }
    }

    public static String convertBookingStatusToString(int BookingStatus) {
        switch (BookingStatus)
        {
            case 0:
                return "ממתין לאישור";
            case 1:
                return "תור אושר";
            default:
                return "Error";
        }
    }

    public static String getNewsTopic() {
        return new StringBuilder("/topics/news").toString();
    }


    public enum TOKEN_TYPE{
        CLIENT,
        BARBER,
        MANAGER
    }

    public static void updateToken(Context context, String token) {
        //First, we need check if user still login
        //Because, we need store token belonging user
        //so, we need user store data
        Paper.init(context);
        String user = Paper.book().read(Common.LOGGED_KEY);
        if (user != null)
        {
            if (!TextUtils.isEmpty(user))
            {
                MyToken myToken = new MyToken();
                myToken.setToken(token);
                myToken.setTokenType(TOKEN_TYPE.BARBER); // run from barber staff
                myToken.setUserPhone(user);

                //submit on fire base
                FirebaseFirestore.getInstance()
                        .collection("Z&G Tokens")
                        .document(user)
                        .set(myToken)
                        .addOnCompleteListener( new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        } );
            }
        }
    }

}
