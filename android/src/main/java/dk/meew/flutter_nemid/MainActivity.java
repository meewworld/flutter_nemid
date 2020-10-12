package dk.meew.flutter_nemid;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import dk.meew.flutter_nemid.communication.RestJsonHelper;
import dk.meew.flutter_nemid.communication.RetrofitHelper;
import dk.meew.flutter_nemid.communication.SPRestService;
import dk.meew.flutter_nemid.utilities.Base64;
import dk.meew.flutter_nemid.utilities.Logger;
import dk.meew.flutter_nemid.utilities.StringHelper;
import dk.meew.flutter_nemid.communication.ValidationResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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
    private static String currentActiveFlow = "oceslogin2";
    public static boolean loggedIn = false;
    public static String parameters = "";
    public static String flowResponse;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    public String signingEndpoint;
    public String validationEndpoint;

    //region Private View setup and utility methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        signingEndpoint = intent.getStringExtra("signingEndpoint");
        validationEndpoint = intent.getStringExtra("validationEndpoint");

        setupWidthAndHeight();

        setupDeviceSize();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            startFlow();
        }
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void startFlow() {
        Request request = new Request.Builder()
                .url(signingEndpoint)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Failure", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                parameters = response.body().string();
                ClientDimensions clientDimensions = getClientDimensions();
                // Start NemIDActivity
                Intent openWebViewIntent = getWebIntent(getFlowUrl(), clientDimensions);
                startActivityForResult(openWebViewIntent, FLOWREQUEST);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                validateResponse();
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void validateResponse(){
        JSONObject body = new JSONObject();
        try {
            body.put("response", flowResponse);
            final Request request = new Request.Builder()
                    .url(validationEndpoint)
                    .post(RequestBody.create(JSON, body.toString()))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                String result = "";

                @Override
                public void onFailure(Call call, IOException e) {
                    result = e.getMessage();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("result", result);
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    finish();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    Intent resultIntent = new Intent();
                    if (response.isSuccessful()) {
                        result = response.body().string();
                        setResult(Activity.RESULT_OK, resultIntent);
                    } else {
                        setResult(Activity.RESULT_CANCELED, resultIntent);
                    }

                    resultIntent.putExtra("result", result);
                    resultIntent.putExtra("status", response.code());
                    finish();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
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

        return "https://appletk.danid.dk/launcher/lmt/" + r;
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

    private boolean currentFlowIsLoginFlow(){
        return currentActiveFlow.contains("login");
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
