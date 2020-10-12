package dk.meew.flutter_nemid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import dk.meew.flutter_nemid.print.PrintUtils;
import dk.meew.flutter_nemid.utilities.Base64;
import dk.meew.flutter_nemid.utilities.Logger;
import dk.meew.flutter_nemid.utilities.NemIDWebView;
import dk.meew.flutter_nemid.utilities.StringHelper;

public class NemIDActivity extends Activity {

    private static final String LOGTAG = "NemID - NemIDActivity";
    private NemIDWebView jsWebView = null;
    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = null;
    public static String printHtml;

    private class NemIDWebViewClientOverrideUrlLoading extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(!url.contains(MainActivity.NIDBACKENDURL)) {
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
            return true;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url){
            warnClearText(url);
            return null;
        }
    }

    private class NemIDWebViewClientBelowApi21 extends NemIDWebViewClientOverrideUrlLoading {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url){
            warnClearText(url);
            if(!url.contains(MainActivity.NIDBACKENDURL) && url.contains("https://")) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
            return null;
        }
    }
    //endregion

    // Show warning if clear text traffic
    private void warnClearText(final String url) {
        if (url.contains("http") && !url.contains("https")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Warning - content was not loaded over a secure connection: " + url,
                            Toast.LENGTH_SHORT);
                    toast.show();
                    MainActivity.flowResponse = "Non-HTTPS resources loaded. Flow was cancelled.";
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }
            });
        }
    }

    //region Webview handling and communication
    @Override
    public void onBackPressed() {
        Intent data_carry = new Intent();
        MainActivity.flowResponse = "Back pressed. Flow cancelled.";
        setResult(Activity.RESULT_CANCELED, data_carry);
        destroyWebView();
        finish();
        super.onBackPressed();
    }

    private void destroyWebView(){
        try {
            jsWebView.stopLoading();
            ViewGroup parent = (ViewGroup) jsWebView.getParent();
            parent.removeView(jsWebView);
            jsWebView.destroy();
            jsWebView = null;
        } catch (Exception e) {
            Logger.e(LOGTAG, "clear webView fail", e);
        }
    }

    /**
     * Custom javascript interface, used for communicating with and retrieving flow responses from the NemID JS Client
     */
    private class CustomJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused") // getResponse is called from JavaScript
        public void getResponse(final String response) {
            MainActivity.flowResponse = response;
            setResult(Activity.RESULT_OK);
            // Destroy the webview on the UI thread
            jsWebView.post(new Runnable() {
                @Override
                public void run() {
                    destroyWebView();
                }
            });
            // Navigate somewhere else given the result, result right now just carried back to initial activity for tester to view
            finish();
        }

        @JavascriptInterface
        @SuppressWarnings("unused") // requestPrint is called from JavaScript
        public void requestPrint(final String signHtml) {
            String decodedSigningHtml = StringHelper.toUtf8String(Base64.decode(signHtml));
            printHtml = decodedSigningHtml;

            jsWebView.post(new Runnable() {
                @Override
                public void run() {
                    doWebViewPrint();
                }
            });
        }

        /**
         * Shows the keyboard upon request from the NemID JS Client, this is necessary for the
         * keyboard to pop up on the very first page rendered in the WebView, since by default
         * on Android the keyboard is not popped on the very first page rendered in the WebView
         * up even though focus is requested by the field
         */
        @JavascriptInterface
        @SuppressWarnings("unused") // requestKeyboard is called from JavaScript
        public void requestKeyboard(){
            showKeyboard();
        }

        @SuppressWarnings("unused")
        @JavascriptInterface
        public void performAppSwitch() {
            Toast.makeText(NemIDActivity.this, "Ready to perform App Switch", Toast.LENGTH_LONG).show();
        }
    }

    public void showKeyboard(){
        Logger.d(LOGTAG, "Did show keyboard");
        InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mgr.restartInput(jsWebView);
        if (globalLayoutListener != null) {
            jsWebView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        }
        mgr.showSoftInput(jsWebView, InputMethodManager.SHOW_IMPLICIT);
    }
    //endregion

    //region Print related functionality
    void doWebViewPrint(){
        WebView printWebView = new WebView(this);
        printWebView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view,
                                                    String url)
            {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                createWebPrintJob(view);
            }
        });

        printWebView.loadDataWithBaseURL(null, NemIDActivity.printHtml,
                "text/HTML", "UTF-8", null);
    }


    private void createWebPrintJob(WebView webView) {
        if (Build.VERSION.SDK_INT<19){
            // Printing is not implemented prior to API level 19
        } else {
            PrintUtils printUtils = new PrintUtils();
            printUtils.createWebPrintJob(webView, this);
        }
    }
    //endregion

    //region Webview creation
    @SuppressLint({"NewApi", "SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!MainActivity.isLargeDevice) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Intent openWebViewIntent = getIntent();

        Boolean isLargeDeviceLogin = openWebViewIntent.getBooleanExtra(MainActivity.LARGE_DEVICE_LOGIN, false);
        if (isLargeDeviceLogin) {
            setContentView(R.layout.activity_nemid_tablet);
        } else {
            setContentView(R.layout.activity_nemid);
        }

        // Handle url
        String url = openWebViewIntent.getStringExtra(MainActivity.STARTFLOWURL);

        jsWebView = (NemIDWebView) findViewById(R.id.jsWebView);

        WebSettings settings = jsWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(false);


        // Performance improvements
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        CookieManager.getInstance().setAcceptCookie(true); // Set this after WebView init but before load
        if(Build.VERSION.SDK_INT>=21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(jsWebView, true);
        }

        // Enable local storage
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSaveFormData(false);

        // Disable pinch zoom and zoom controls
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // Disabling access to files and other content
        settings.setAllowFileAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setAllowContentAccess(false);

        jsWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        jsWebView.setOverScrollMode(WebView.OVER_SCROLL_ALWAYS);
        jsWebView.setScrollbarFadingEnabled(false);
        jsWebView.setVerticalScrollBarEnabled(true);

        // Avoid long click selection of UI elements
        jsWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        //Add custom client to override loading help-links in WebView (load in browser instead)
        if(Build.VERSION.SDK_INT<21) { //Only shouldInterceptRequest works in lower version than 21
            jsWebView.setWebViewClient(new NemIDWebViewClientBelowApi21());
        }
        else{
            jsWebView.setWebViewClient(new NemIDWebViewClientOverrideUrlLoading());
        }
        jsWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String msg = consoleMessage.message();
                if (null != msg && msg.toLowerCase().startsWith("uncaught")) {
                    boolean b = super.onConsoleMessage(consoleMessage);

                    MainActivity.flowResponse = msg;

                    setResult(Activity.RESULT_CANCELED);

                    // Destroy the webview on the UI thread
                    jsWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            destroyWebView();
                        }
                    });
                    // Navigate somewhere else given the result, result right now just carried back to initial activity for tester to view
                    finish();

                    return b;
                } else {
                    return super.onConsoleMessage(consoleMessage);
                }
            }
        });
        // Start up WebView with JavaScript enabled
        jsWebView.addJavascriptInterface(new CustomJavaScriptInterface(), "NemIDActivityJSI");

        //Create html
        String width = openWebViewIntent.getStringExtra(MainActivity.WIDTH_PARAMETER);
        String height = openWebViewIntent.getStringExtra(MainActivity.HEIGHT_PARAMETER);
        openWebViewIntent.getStringExtra(MainActivity.HEIGHT_PARAMETER);
        String clientLoadHtml = getHTML(MainActivity.parameters, url, width, height);

        //Load the WebView
        jsWebView.loadDataWithBaseURL(url, clientLoadHtml, "text/html", "utf-8", "");

        //Ensure that the keyboard is not covering the input fields
        globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (jsWebView != null && jsWebView.getRootView() != null) {
                    int heightDiff = jsWebView.getRootView().getHeight() - jsWebView.getHeight();
                    if (heightDiff > 300) { //Assume that its a keyboard if difference is greater than 300 px
                        jsWebView.scrollTo(0, jsWebView.getRootView().getHeight());
                    }
                }
            }
        };
        jsWebView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }
    //endregion

    //region HTML generation methods for loading NemId client in NemIdActivity
    public static String getJavascriptSrc(String parameters, String url, String width, String height) {

        String js = "function onNemIDMessage(e) { "
                + "var event = e || event; "
                + "var win = document.getElementById(\"nemid_iframe\").contentWindow, postMessage = {}, message; "
                + "message = JSON.parse(event.data); "
                + "origin = event.origin; "
                + "if (origin !== \"%s\") { "
                + "   return; "
                + "} "
                + "if (message.command === \"SendParameters\") { "
                + " postMessage.command = \"parameters\"; "
                + " postMessage.content = \'%s\'; "
                + " win.postMessage(JSON.stringify(postMessage), \"*\"); "
                + " } "
                + " if (message.command === \"changeResponseAndSubmit\") { "
                + "window.globalContent = message.content; "
                + "giveResponseToAndroid();"
                + "} "
                + " if (message.command === \"AwaitingAppApproval\") { "
                + "NemIDActivityJSI.performAppSwitch();"
                + "} "
                + " if (message.command === \"RequestKeyboard\") { "
                + "window.globalContent = message.content; "
                + "NemIDWebViewJSI.setKeyboardType(window.globalContent);"
                + "NemIDActivityJSI.requestKeyboard();"
                + "} "
                + " if (message.command === \"RememberMeUnchecked\") { "
                + "window.globalContent = message.content; "
                + "NemIDActivityJSI.requestKeyboard();"
                + "} "
                + " if (message.command === \"RequestPrint\") { "
                + "window.globalContent = message.content; "
                + "NemIDActivityJSI.requestPrint(window.globalContent);"
                + "} "
                + " if (message.command === \"changeResponseAndSubmitSign\") { "
                + "window.globalContent = message.content; "
                + "giveResponseToAndroid();"
                + "} } "
                + "if (window.addEventListener) { window.addEventListener(\"message\", onNemIDMessage); }else if (window.attachEvent) { "
                + "window.attachEvent(\"onmessage\", onNemIDMessage); } "
                + "function giveResponseToAndroid() { NemIDActivityJSI.getResponse(window.globalContent); } "
                + "function getContent() { "
                + "return window.globalContent; }";

        js = String.format(js, url, parameters);
        return js;
    }

    public String getHTML(String parameters, String url, String width, String height) {

        String js = getJavascriptSrc(parameters, MainActivity.NIDBACKENDURL, width, height);

        // iframe will use relative scaling and later the webview is sized for
        // pixel width/height
        String widthS = "" + width + "px"; // could also be "100%"
        String heightS = "" + height + "px";

        String scrolling = "no";

        // the header - meta tags are for development
        return String
                .format("<html>"
                        + "<body style=\"text-align:center;padding:0;margin:0;overflow:hidden\">"
                        + "<iframe id=\"nemid_iframe\" name=\"nemid_iframe\" scrolling=\"%s\" frameborder=\"0\" style=\"padding:0;margin:0; background-color: #ffffff; width:%s;height:%s;border:0\" src=\"%s\"></iframe>"
                        + "<script>%s</script>" + "</body></html>", scrolling, widthS, heightS, url, js);
    }
    //endregion
}
