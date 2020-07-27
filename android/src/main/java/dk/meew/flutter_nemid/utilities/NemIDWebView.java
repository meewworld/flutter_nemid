package dk.meew.flutter_nemid.utilities;

import android.content.Context;
import android.os.Build;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class NemIDWebView extends WebView {

    Context context;
    private int inputType = 0;

    public NemIDWebView(Context context) {
        super(context);
        init(context);
    }

    public NemIDWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NemIDWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        addJavascriptInterface(new CustomJavaScriptInterfaceWebview(), "NemIDWebViewJSI");
    }

    // In principle any device with API level 21 v.5.0.1 and lower should have the backspace fix, as the problem has only been observed on API levels up to 21, v.5.0.1
    private boolean shouldApplyBackspaceFix() {
        return (Build.VERSION.SDK_INT < 22 && !Build.VERSION.RELEASE.equals("5.0.2"));
    }

    //The numeric keyboard fix does not go well with the low API level HTC devices
    private boolean shouldApplyBackspaceFixAndNumericKeyboardFor4DigitFix() {
        return shouldApplyBackspaceFix() && !Build.MANUFACTURER.toLowerCase().contains("htc");
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {

        if (shouldApplyBackspaceFix()) {
            InputConnectionForBackspaceIssues baseInputConnection = new InputConnectionForBackspaceIssues(this, false);

            outAttrs.actionLabel = null;

            if (shouldApplyBackspaceFixAndNumericKeyboardFor4DigitFix()) {
                if (inputType != 0) {
                    outAttrs.inputType = inputType;
                    inputType = 0;
                }
            } else {
                outAttrs.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
            }
            outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;

            return baseInputConnection;

        } else {
            InputConnection conn = super.onCreateInputConnection(outAttrs);
            if (inputType != 0) {
                outAttrs.inputType = inputType;
                inputType = 0;
            }
            return conn;
        }
    }

    private class CustomJavaScriptInterfaceWebview {
        @JavascriptInterface
        @SuppressWarnings("unused") // setKeyboardType is called from JavaScript
        public void setKeyboardType(String data) {
            boolean is4Digit = false;
            boolean isAlpha = false;
            try {
                String b64DecodedString = new String(Base64.decode(data), "UTF-8");
                is4Digit = b64DecodedString.toUpperCase().contains("FOURDIGIT");
                isAlpha = b64DecodedString.toUpperCase().contains("ALPHANUMERIC");
            } catch (Exception e) {
                Log.e("Decode error", e.getMessage());
            }

            if (is4Digit) {
                inputType = InputType.TYPE_CLASS_PHONE;
            } else if (isAlpha) {
                inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
            }
        }
    }
}
