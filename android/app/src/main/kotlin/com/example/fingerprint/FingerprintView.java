package com.example.fingerprint;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import io.flutter.plugin.platform.PlatformView;

public class FingerprintView implements PlatformView {
    @NonNull
    private final ImageView imageView;

    FingerprintView(@NonNull Context context, int id, @Nullable Map<String, Object> creationParams) {
        imageView = new ImageView(context);

    }

    @NonNull
    @Override
    public View getView() {
        return imageView;
    }

    @Override
    public void dispose() {
        FingerprintViewFactory.fingerprintViews.remove(this);
    }
}