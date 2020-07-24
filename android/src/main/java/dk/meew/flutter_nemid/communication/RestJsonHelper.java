package dk.meew.flutter_nemid.communication;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class RestJsonHelper {

    public enum SignTextFormat {
        HTML, XML, PLAIN, PDF
    }

    public static String getLogonRequest(String issuer, String language, String requestType, String rememberUseridToken, boolean suppressPushToDevice, boolean useAppSwitch) {
        try {
            JSONObject request = new JSONObject();
            request.put("issuer", issuer);
            request.put("requestType", requestType);
            request.put("useJson",true);
            request.put("rememberuseridtoken", (null != rememberUseridToken) ? rememberUseridToken : "");
            request.put("suppressPushToDevice", suppressPushToDevice);
            request.put("enableAwaitingAppApprovalEvent", useAppSwitch);
            setLanguage(language, request);
            return request.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getSignRequest(String issuer, String language, String requestType, String encodedSignText, String encodedSignTextStylesheet, SignTextFormat signTextFormat, String allowStepUp, String rememberUseridToken, boolean suppressPushToDevice, boolean useAppSwitch) {
        try {
            JSONObject request = new JSONObject();
            request.put("issuer", issuer);
            request.put("requestType", requestType);
            request.put("useJson",true);
            request.put("signTextFormat", signTextString.get(signTextFormat));
            request.put("signText", encodedSignText);
            if (encodedSignTextStylesheet != null) {
                request.put("signTransformation", encodedSignTextStylesheet);
                request.put("transformationId", 10);
            }
            setLanguage(language, request);
            if (allowStepUp != null) {
                request.put("allow_stepup", allowStepUp);
            }
            request.put("rememberuseridtoken", (null != rememberUseridToken) ? rememberUseridToken : "");
            request.put("suppressPushToDevice", suppressPushToDevice);
            request.put("enableAwaitingAppApprovalEvent", useAppSwitch);
            return request.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setLanguage(String language, JSONObject request) throws JSONException {
        if (language != null && !language.trim().equals("")) {
            request.put("language", language);
        }
    }

    private static final Map<SignTextFormat, String> signTextString = new HashMap<SignTextFormat, String>() {{
        put(SignTextFormat.HTML, "html");
        put(SignTextFormat.XML, "XML");
        put(SignTextFormat.PLAIN, "TEXT");
        put(SignTextFormat.PDF, "pdf");
    }};
}
