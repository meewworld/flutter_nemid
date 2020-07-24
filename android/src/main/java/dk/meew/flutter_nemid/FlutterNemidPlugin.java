package dk.meew.flutter_nemid;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Intent;
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
    if (call.method.equals("startNemIDLogin")) {
      mResult = result;
      activity.startActivityForResult(new Intent(activity, MainActivity.class), 1);
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
  public boolean onActivityResult(int requestCode, int resultCode, Intent data){
    if (resultCode == Activity.RESULT_OK) {
      boolean returnValue = data.getBooleanExtra("logged_in", false);
      mResult.success(returnValue);
    } else if (resultCode == Activity.RESULT_CANCELED) {
      mResult.success(false);
    } else {
      mResult.success(false);
    }
    return true;
  }
}
