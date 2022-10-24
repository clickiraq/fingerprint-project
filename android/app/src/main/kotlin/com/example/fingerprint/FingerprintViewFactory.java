package com.example.fingerprint;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Map;

import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class FingerprintViewFactory extends PlatformViewFactory {

    static ArrayList<FingerprintView> fingerprintViews = new ArrayList<>();

    FingerprintViewFactory() {
        super(StandardMessageCodec.INSTANCE);
    }

    @NonNull
    @Override
    public PlatformView create(@NonNull Context context, int id, @Nullable Object args) {
        final Map<String, Object> creationParams = (Map<String, Object>) args;
        final FingerprintView fingerprintView = new FingerprintView(context, id, creationParams);
        fingerprintViews.add(fingerprintView);

        return fingerprintView;
    }
}