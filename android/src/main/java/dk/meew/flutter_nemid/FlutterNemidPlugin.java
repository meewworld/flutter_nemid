package dk.meew.flutter_nemid;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class FlutterNemidPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {

  private MethodChannel channel;
  private Activity activity;
  private ActivityPluginBinding activityPluginBinding;
  Result mResult;
  private String signingEndpoint, validationEndpoint;
  private static int REQUEST_CODE = 1337;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_nemid");
    channel.setMethodCallHandler(this);
  }

  public void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_nemid");
    channel.setMethodCallHandler(new FlutterNemidPlugin());
    activity = registrar.activity();
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("setupBackendEndpoints")) {
      if(call.hasArgument("signingEndpoint") && call.hasArgument("validationEndpoint")){
        signingEndpoint = call.argument("signingEndpoint");
        validationEndpoint = call.argument("validationEndpoint");
        result.success("ok");
      } else {
        result.error("PARAMETERS NOT FOUND", "Please pass parameters for the backend endpoints.", null);
      }
    } else if (call.method.equals("startNemIDLogin")) {
      mResult = result;
      Intent intent = new Intent(activity, MainActivity.class);
      intent.putExtra("signingEndpoint", signingEndpoint);
      intent.putExtra("validationEndpoint", validationEndpoint);
      activity.startActivityForResult(intent, REQUEST_CODE);
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
    binding.addActivityResultListener(this);
    activityPluginBinding = binding;
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {}

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {}

  @Override
  public void onDetachedFromActivity() {
    activity = null;
    activityPluginBinding.removeActivityResultListener(this);
    activityPluginBinding = null;
  }

  /**
   * @param requestCode
   * @param resultCode
   * @param data
   * @return true if the result has been handled.
   */
  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE) {
      try {
        JSONObject response = new JSONObject();
        if(data != null) {
          if(data.getExtras().containsKey("result")) {
            String result = data.getStringExtra("result");
            int status = data.getIntExtra("status", 503);
            response.put("result", result);
            response.put("status", status);
            if (resultCode == Activity.RESULT_OK) {
              mResult.success(response.toString());
            } else {
              mResult.error("" + status, result, response);
            }
          } else {
            mResult.error("503", "Unknown error", "");
          }
        } else {
          mResult.error("503", "Login was canceled", "");
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return true;
  }
}
