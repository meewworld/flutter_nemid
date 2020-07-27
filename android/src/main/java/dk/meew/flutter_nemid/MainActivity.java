package dk.meew.flutter_nemid;

import java.util.Map;
import java.util.Random;
import androidx.annotation.NonNull;
import dk.meew.flutter_nemid.communication.RestJsonHelper;
import dk.meew.flutter_nemid.communication.RetrofitHelper;
import dk.meew.flutter_nemid.communication.SPRestService;
import dk.meew.flutter_nemid.utilities.Base64;
import dk.meew.flutter_nemid.utilities.Logger;
import dk.meew.flutter_nemid.utilities.StringHelper;
import dk.meew.flutter_nemid.communication.ValidationResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class MainActivity extends Activity {
    private static final String LOGTAG = "NemID - MainActivity";
    public final static String STARTFLOWURL = "dk.danid.android.testjavascript.MainActivity.startflowurl";
    private static String WIDTH = "";
    private static String HEIGHT = "";
    public final static String LARGE_DEVICE_LOGIN = "dk.danid.android.testjavascript.MainActivity.largedevicelogin";
    public static boolean isLargeDevice = false;
    public final static String WIDTH_PARAMETER = "dk.danid.android.testjavascript.MainActivity.width_parameter";
    public final static String HEIGHT_PARAMETER = "dk.danid.android.testjavascript.MainActivity.height_parameter";
    private final static int FLOWREQUEST = 0x1234;
    private static String SPBACKENDURL = "https://appletk.danid.dk";
    public static String NIDBACKENDURL = "https://appletk.danid.dk";
    private static String currentActiveFlow = "";
    public static boolean loggedIn = false;
    public static String parameters = "";
    public static String flowResponse;

    //region Private View setup and utility methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWidthAndHeight();

        setupDeviceSize();
        startFlow("oceslogin2");
    }

    private void setupWidthAndHeight() {
        try {
            // wrapped for safety on older devices or future API changes
            DisplayMetrics deviceDisplayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(deviceDisplayMetrics);

            // get the width and height
            float density = deviceDisplayMetrics.scaledDensity;
            WIDTH = Integer.toString((int) Math.floor(deviceDisplayMetrics.widthPixels / density));
            HEIGHT = Integer.toString((int) Math.floor((deviceDisplayMetrics.heightPixels - getStatusBarHeight()) / density));
            Logger.d(LOGTAG, "WIDTH: " + WIDTH + ", HEIGHT: " + HEIGHT);
        } catch (Exception e) {
            Logger.e(LOGTAG, "Error getting device metrics", e);
        }
    }

    private void setupDeviceSize() {
        int layoutMask = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
        isLargeDevice = (layoutMask == Configuration.SCREENLAYOUT_SIZE_XLARGE);
        if (!isLargeDevice) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    //region General view manipulation and interaction methods
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void startFlow(final String flowtype) {
        currentActiveFlow = flowtype;

        final String requestObj;
        requestObj = RestJsonHelper.getLogonRequest(
                "1",
                "DA",
                flowtype,
                "",
                false,
                false);
        signParameters(flowtype, requestObj);
    }

    private void signParameters(final String flowType, final String requestObj) {
        Retrofit retrofit = RetrofitHelper.getRetrofitForBaseUrl(getBackendUrl());
        SPRestService spRestService = retrofit.create(SPRestService.class);
        final Call<String> parameterCall = spRestService.signParameters(requestObj);

        parameterCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    parameters = response.body();
                    ClientDimensions clientDimensions = getClientDimensions();
                    // Start NemIDActivity
                    Intent openWebViewIntent = getWebIntent(getFlowUrl(), clientDimensions);
                    startActivityForResult(openWebViewIntent, FLOWREQUEST);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Logger.e(LOGTAG, "Failed to request flow: " + flowType);
            }
        });
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (currentFlowIsBankFlow() && currentFlowIsLoginFlow()) {
                validateLoginResponseAtSpBackend();
            } else if (currentFlowIsOcesFlow() || currentFlowIsSignFlow()) {
                validateSignResponseAtSpBackend();
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("logged_in", loggedIn);
            setResult(Activity.RESULT_CANCELED, resultIntent);
            finish();
        } else {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("logged_in", loggedIn);
            resultIntent.putExtra("error", resultCode);
            setResult(Activity.RESULT_CANCELED, resultIntent);
            finish();
        }
    }

    private String processValidationResult(Map<String, String> validationResult) {
        if (validationResult != null && validationResult.containsKey("VALIDATION_RESULT")) {
            String responseText = "";
            if (validationResult.get("VALIDATION_RESULT").equalsIgnoreCase("OK")) {
                loggedIn = true;
                responseText = "Flow success. Response validated.";
            } else if (validationResult.get("VALIDATION_RESULT").equalsIgnoreCase("FAILED VALIDATION")) {
                responseText = "Response Signature not valid.";
            } else if (validationResult.get("VALIDATION_RESULT").equalsIgnoreCase("FAILED SYSTEM EXCEPTION")) {
                responseText = "Could not validate response.";
            }
            return responseText;
        } else {
            Logger.e(LOGTAG, "Empty Validation Result Received!");
            return "Empty Validation Result Received!";
        }
    }

    private void validateSignResponseAtSpBackend() {
        Retrofit retrofit = RetrofitHelper.getRetrofitForBaseUrl(getBackendUrl());
        SPRestService spRestService = retrofit.create(SPRestService.class);

        String encodedResponse = Base64.encode(StringHelper.toUtf8Bytes(flowResponse));

        Call<String> verificationCall = spRestService.validateSignResult(encodedResponse);
        performVerificationCall(verificationCall);
    }

    private void validateLoginResponseAtSpBackend() {
        Retrofit retrofit = RetrofitHelper.getRetrofitForBaseUrl(getBackendUrl());
        SPRestService spRestService = retrofit.create(SPRestService.class);

        String encodedResponse = Base64.encode(StringHelper.toUtf8Bytes(flowResponse));

        Call<String> verificationCall = spRestService.validateLoginResult(encodedResponse);
        performVerificationCall(verificationCall);
    }

    private void performVerificationCall(Call<String> verificationCall) {
        verificationCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Map<String, String> validationResult;

                Intent resultIntent = new Intent();
                if (response.isSuccessful()) {
                    validationResult = ValidationResponse.parse(response.body());
                    processValidationResult(validationResult);
                    setResult(Activity.RESULT_OK, resultIntent);
                } else {
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                }

                resultIntent.putExtra("logged_in", loggedIn);
                finish();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Logger.e(LOGTAG, "Failed to receive response at sp backend.");
            }
        });
    }
    //endregion

    //region Helper methods
    private ClientDimensions getClientDimensions() {

        String width = WIDTH;
        String height = HEIGHT;

        // On a larger device show login in smaller centered view
        Boolean isLargeDeviceLogin = false;
        if ((currentFlowIsLoginFlow()) && isLargeDevice) {
            isLargeDeviceLogin = true;
            width = "320";
            height = "460";
        }

        return new ClientDimensions(width,height,isLargeDeviceLogin);
    }

    private String getFlowUrl() {
        Random rand = new Random();
        int r = Math.abs(rand.nextInt());

        return NIDBACKENDURL + "/launcher/lmt/" + r;
    }

    @NonNull
    private Intent getWebIntent(String url, ClientDimensions clientDimensions) {

        Intent openWebViewIntent = new Intent(this, NemIDActivity.class);
        openWebViewIntent.putExtra(STARTFLOWURL, url);
        openWebViewIntent.putExtra(LARGE_DEVICE_LOGIN, clientDimensions.isLargeDeviceLogin);
        openWebViewIntent.putExtra(WIDTH_PARAMETER,clientDimensions.width);
        openWebViewIntent.putExtra(HEIGHT_PARAMETER, clientDimensions.height);
        return openWebViewIntent;
    }

    public static String getBackendUrl() {
        return SPBACKENDURL;
    }

    private boolean currentFlowIsOcesFlow(){
        return currentActiveFlow.contains("oces");
    }
    private boolean currentFlowIsBankFlow(){
        return currentActiveFlow.contains("bank");
    }
    private boolean currentFlowIsLoginFlow(){
        return currentActiveFlow.contains("login");
    }
    private boolean currentFlowIsSignFlow(){
        return currentActiveFlow.contains("sign");
    }
    //endregion

    //region Parameter containers for ease of handling of parameters for communication with SP backend and creation of html for loading client
    private class ClientDimensions {
        String width;
        String height;
        boolean isLargeDeviceLogin;

        ClientDimensions(String width, String height, boolean isLargeDeviceLogin) {
            this.width = width;
            this.height = height;
            this.isLargeDeviceLogin = isLargeDeviceLogin;
        }
    }
    //endregion
}
