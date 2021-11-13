package com.example.zgbeautyandhairstaff.Retrofit;

import com.example.zgbeautyandhairstaff.Model.FCMResponse;
import com.example.zgbeautyandhairstaff.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key =AAAAGzwKNI8:APA91bG_4ontKWQFF4r9VeMUZ9nBcit9J0sonbPYC8y9GYLFXDP2h9Ae_NTJtIfepyyXyOBGjH_7cJtIT2px9t-InfG0a7kd9rgz_9yjRvAdcj-xMH_HLGQjdrJKoTCSAurKf9ToYgFL"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
