package dk.meew.flutter_nemid.communication;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface SPRestService {

    @FormUrlEncoded
    @POST("/developers/mobile/mobileAppParameterGeneratorForJS.jsp")
    Call<String> signParameters(@Field("data") String data);

    @FormUrlEncoded
    @POST("/developers/mobile/samlReceiverJS.jsp")
    Call<String> validateLoginResult(@Field("response") String data);

    @FormUrlEncoded
    @POST("/developers/mobile/signReceiverJS.jsp")
    Call<String> validateSignResult(@Field("response") String data);

    @POST("/developers/mobile/logOutJS.jsp")
    Call<String> logout(@Body String body);
}
