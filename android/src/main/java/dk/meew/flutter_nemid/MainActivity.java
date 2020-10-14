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
    private static String SPBACKENDURL = "https://applet.danid.dk";
    public static String NIDBACKENDURL = "https://applet.danid.dk";
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

                //parameters = response.body().string();
                parameters = "{\"CLIENTFLOW\":\"OCESLOGIN2\",\"ENABLE_AWAITING_APP_APPROVAL_EVENT\":\"true\",\"SP_CERT\":\"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\",\"TIMESTAMP\":\"2020-10-14 16:16:36+0000\",\"DIGEST_SIGNATURE\":\"RGFZjxoqLwNhGwGUv4xEvCKtq+sP2qTSeioCyPwAVIeL92M8sGvpN4/Pbq6PfLK2FV8nm0TkkA08FcohSvCsdvPWS0MnDsXzyaZ0hwv3w5TOozMdZM1ATwsnyot4cUmQ8BUZhQpajz9Q37JYvWXp4iqtf2JcPhQNu+JxBcIe93jjb0IpPrpLS16GQ+GiP4UyIYC/NufUQlnCGha48F5/MvblTlptJFGzJK0Ihhtq3b9X3cCaT9mH1lfuYGroKwjhCB8cHEtTUfLISSKasixRTVttuFOZ3vGzCDkKZIYBCQVqjv2J8baGFXebh76DF1RazoHmSmj7dPNQpYUCfhYNSA==\",\"PARAMS_DIGEST\":\"kQkV64j+InTjmpFUjcCbhlSlzHoaLW1gRmtENQYOBZw=\"}";

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

        return "https://applet.danid.dk/launcher/lmt/" + r;
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
