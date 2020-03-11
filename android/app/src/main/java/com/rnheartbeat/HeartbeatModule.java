package com.rnheartbeat;

import android.content.Intent;
import android.os.Bundle;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import javax.annotation.Nonnull;

public class HeartbeatModule extends ReactContextBaseJavaModule {

    public static final String REACT_CLASS = "Heartbeat";
    private static ReactApplicationContext reactContext;

    public HeartbeatModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Nonnull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactMethod
    public void startService(String websocketUrl) {
        Intent service = new Intent(this.reactContext, HeartbeartService.class);
        Bundle bundle = new Bundle();
        bundle.putString("websocketUrl", websocketUrl);
        service.putExtras(bundle);
        this.reactContext.startService(service);
    }

    @ReactMethod
    public void stopService() {
        this.reactContext.stopService(new Intent(this.reactContext, HeartbeartService.class));
    }
}
