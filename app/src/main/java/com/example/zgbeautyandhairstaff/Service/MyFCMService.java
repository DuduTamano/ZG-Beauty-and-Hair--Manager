package com.example.zgbeautyandhairstaff.Service;

import com.example.zgbeautyandhairstaff.Common.Common;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;


public class MyFCMService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> dateRecv = remoteMessage.getData();

        if (remoteMessage.getData() != null) {
            Common.showNotification(this,
                    new Random().nextInt(),
                    remoteMessage.getData().get(Common.TITLE_KEY),
                    remoteMessage.getData().get(Common.CONTENT_KEY),
                    null);
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Common.updateToken(this, token);
    }
}
