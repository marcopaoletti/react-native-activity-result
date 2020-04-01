// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.util.SparseArray;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import host.exp.expoview.Exponent;

public class ActivityResultModule extends ReactContextBaseJavaModule {

  final SparseArray<Promise> mPromises;

  private final host.exp.exponent.ActivityResultListener mActivityEventListener = new host.exp.exponent.ActivityResultListener() {
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
      Promise promise = mPromises.get(requestCode);
      if (promise != null) {
          Log.e("Ivan","Got " + resultCode + " " + data.getData().toString());

          String json;

          if (resultCode == Activity.RESULT_OK) {
              json = "{\"resultCode\":" + resultCode + ", \"data\":" + data.getData().toString() + "}";
          } else {
              json = "{\"resultCode\":" + resultCode + ", \"data\":}";
          }

          promise.resolve(json);
      }
     }
  };

  public ActivityResultModule(ReactApplicationContext reactContext) {
      super(reactContext);
      mPromises = new SparseArray<>();
      Exponent.getInstance().addActivityResultListener(mActivityEventListener);
  }

  @Override
  public String getName() {
      return "ActivityResult";
  }

  @Nullable
  @Override
  public Map<String, Object> getConstants() {
      HashMap<String, Object> constants = new HashMap<>();
      constants.put("OK", Activity.RESULT_OK);
      constants.put("CANCELED", Activity.RESULT_CANCELED);
      return constants;
  }

  @Override
  public void initialize() {
      super.initialize();
  }

  @Override
  public void onCatalystInstanceDestroy() {
      super.onCatalystInstanceDestroy();
  }

  @ReactMethod
  public void startActivity(String appPackage, String action, ReadableMap data) {
      Activity activity = getReactApplicationContext().getCurrentActivity();
      Intent launchIntent = new Intent();
      launchIntent.setComponent(new ComponentName(appPackage, action));

      launchIntent.putExtras(Arguments.toBundle(data));
      activity.startActivity(launchIntent);
  }

  @ReactMethod
  public void startActivityForResult(int requestCode, String appPackage, String action, ReadableMap data, Promise promise) {
      Activity activity = getReactApplicationContext().getCurrentActivity();

      Intent launchIntent = new Intent();
      launchIntent.setComponent(new ComponentName(appPackage, action));
      launchIntent.putExtras(Arguments.toBundle(data));
      activity.startActivityForResult(launchIntent, requestCode);


      mPromises.put(requestCode, promise);
  }

  @ReactMethod
  public void resolveActivity(String appPackage, String action, Promise promise) {
      Activity activity = getReactApplicationContext().getCurrentActivity();
      Intent launchIntent = new Intent();
      launchIntent.setComponent(new ComponentName(appPackage, action));

      if(activity.getPackageManager().resolveActivity(launchIntent, 0) != null) {
          promise.resolve("{\"success\":\"true\"}");
      }else{
          promise.resolve(null);
      }
  }

  @ReactMethod
  public void finish(int result, String action, ReadableMap map) {
      Activity activity = getReactApplicationContext().getCurrentActivity();
      Intent intent = new Intent(action);
      intent.putExtras(Arguments.toBundle(map));
      activity.setResult(result, intent);
      activity.finish();
  }

}