package com.example.fingerprint;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.futronictech.FPScan;
import com.futronictech.MyBitmapFile;
import com.futronictech.Scanner;
import com.futronictech.UsbDeviceDataExchangeImpl;
import com.futronictech.ftrWsqAndroidHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * FutronicFingerprintScannerPlugin
 */
public class FutronicFingerprintScannerPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.RequestPermissionsResultListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    public static boolean mStop = false;
    public static boolean mFrame = true;
    public static boolean mLFD = false;
    public static boolean mInvertImage = false;
    public static boolean mNFIQ = false;

    public static final int MESSAGE_SHOW_MSG = 1;
    public static final int MESSAGE_SHOW_SCANNER_INFO = 2;
    public static final int MESSAGE_SHOW_IMAGE = 3;
    public static final int MESSAGE_ERROR = 4;
    public static final int MESSAGE_TRACE = 5;

    public static byte[] mImageFP = null;
    public static Object mSyncObj = new Object();

    public static int mImageWidth = 0;
    public static int mImageHeight = 0;
    private static int[] mPixels = null;
    private static Bitmap mBitmapFP = null;
    private static Canvas mCanvas = null;
    private static Paint mPaint = null;
    public static boolean mUsbHostMode = true;

    private MethodChannel channel;

    private boolean isScanButton = false;
    private FPScan mFPScan = null;
    private UsbDeviceDataExchangeImpl usb_host_ctx = null;
    private File SyncDir = null;

    private Context context;
    private Activity activity;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "futronic_fingerprint_scanner");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
        flutterPluginBinding
                .getPlatformViewRegistry()
                .registerViewFactory("imageView", new FingerprintViewFactory());
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

        if (call.method.equals("scan")) {
            result.success(scanButton());
        } else if (call.method.equals("stop")) {
            result.success(stopButton());
        } else if (call.method.equals("setCheckFrame")) {
            if (mStop) return;
            mFrame = Boolean.TRUE.equals(call.argument("value"));
        } else if (call.method.equals("setCheckLFD")) {
            if (mStop) return;
            mLFD = Boolean.TRUE.equals(call.argument("value"));
        } else if (call.method.equals("setCheckInvert")) {
            if (mStop) return;
            mInvertImage = Boolean.TRUE.equals(call.argument("value"));
        } else if (call.method.equals("setCheckUSB")) {
            if (mStop) return;
            mUsbHostMode = Boolean.TRUE.equals(call.argument("value"));
        } else if (call.method.equals("setCheckNFIQ")) {
            if (mStop) return;
            mNFIQ = Boolean.TRUE.equals(call.argument("value"));

            // isChecked
        } else if (call.method.equals("isFrameChecked")) {
            result.success(mFrame);
        } else if (call.method.equals("isLFDChecked")) {
            result.success(mLFD);
        } else if (call.method.equals("isInvertChecked")) {
            result.success(mInvertImage);
        } else if (call.method.equals("isUSBChecked")) {
            result.success(mUsbHostMode);
        } else if (call.method.equals("isNFIQChecked")) {
            result.success(mNFIQ);

        } else if (call.method.equals("getScannerSize")) {
            result.success("{\"width\":" + mImageWidth + ",\"height\":" + mImageHeight + "}");

        } else if (call.method.equals("getFingerprintImageBytes")) {
            result.success(mImageFP);

        } else if (call.method.equals("saveImage")) {
            if (isStoragePermissionGranted()) {
                SaveImageByFileFormat(Objects.requireNonNull(call.argument("fileFormat")), call.argument("filePath") + ((String) call.argument("fileName")));
                result.success(true);
                return;
            }
            result.success(false);

        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    public static void InitFingerPictureParameters(int wight, int height) {
        mImageWidth = wight;
        mImageHeight = height;

        mImageFP = new byte[mImageWidth * mImageHeight];
        mPixels = new int[mImageWidth * mImageHeight];

        mBitmapFP = Bitmap.createBitmap(wight, height, Bitmap.Config.RGB_565);

        mCanvas = new Canvas(mBitmapFP);
        mPaint = new Paint();

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        mPaint.setColorFilter(f);
    }

    private void init() {
        usb_host_ctx = new UsbDeviceDataExchangeImpl(context, mHandler);
        mFrame = true;
        mUsbHostMode = true;
        mLFD = mInvertImage = false;
        SyncDir = context.getExternalFilesDir(null);

        if (!isStoragePermissionGranted()) {
            isScanButton = false;
        }

    }

    private boolean StartScan() {
        mFPScan = new FPScan(usb_host_ctx, SyncDir, mHandler);
        mFPScan.start();
        return true;
    }

    private boolean scanButton() {
        boolean isSuccessful = true;
        if (mFPScan != null) {
            mStop = true;
            mFPScan.stop();

        }
        mStop = false;
        if (mUsbHostMode) {
            usb_host_ctx.CloseDevice();
            if (usb_host_ctx.OpenDevice(0, true)) {
                if (StartScan()) {
                   /* mButtonScan.setEnabled(false);
                    mButtonSave.setEnabled(false);
                    mCheckUsbHostMode.setEnabled(false);
                    mButtonStop.setEnabled(true);*/
                }
            } else {
                if (!usb_host_ctx.IsPendingOpen()) {
                    isSuccessful = false;
                    channel.invokeMethod("message", "Can not start scan operation.\\nCan't open scanner device");
                }
            }
        } else {
            if (StartScan()) {
               /* mButtonScan.setEnabled(false);
                mButtonSave.setEnabled(false);
                mCheckUsbHostMode.setEnabled(false);
                mButtonStop.setEnabled(true);*/
            }
        }
        return isSuccessful;
    }

    private boolean stopButton() {
        mStop = true;
        if (mFPScan != null) {
            mFPScan.stop();
            mFPScan = null;

        }
        /*mButtonScan.setEnabled(true);
        mButtonSave.setEnabled(true);
        mCheckUsbHostMode.setEnabled(true);
        mButtonStop.setEnabled(false);*/

        return true;
    }

    private static void ShowBitmap() {
        Log.d("ShowBitmap", "ShowBitmap");
        for (int i = 0; i < mImageWidth * mImageHeight; i++) {
            mPixels[i] = Color.rgb(mImageFP[i], mImageFP[i], mImageFP[i]);
        }

        mCanvas.drawBitmap(mPixels, 0, mImageWidth, 0, 0, mImageWidth, mImageHeight, false, mPaint);

        // mFingerImage.setImageBitmap(mBitmapFP);
        // mFingerImage.invalidate();
        for (int i = 0; i < FingerprintViewFactory.fingerprintViews.size(); i++) {
            final FingerprintView fingerprintView = FingerprintViewFactory.fingerprintViews.get(i);
            final ImageView imageView = (ImageView) fingerprintView.getView();
            imageView.setImageBitmap(mBitmapFP);
            imageView.invalidate();
        }
        synchronized (mSyncObj) {
            mSyncObj.notifyAll();
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW_MSG:
                case MESSAGE_SHOW_SCANNER_INFO:
                    String showMsg = (String) msg.obj;
                    channel.invokeMethod("message", showMsg);
                    //mMessage.setText(showMsg);
                    break;
                case MESSAGE_SHOW_IMAGE:
                    ShowBitmap();
                    break;
                case MESSAGE_ERROR:
                    //mFPScan = null;
                    isScanButton = true;
                   /* mButtonScan.setEnabled(true);
                    mCheckUsbHostMode.setEnabled(true);
                    mButtonStop.setEnabled(false);*/
                    break;
                case UsbDeviceDataExchangeImpl.MESSAGE_ALLOW_DEVICE:
                    if (usb_host_ctx.ValidateContext()) {
                        if (StartScan()) {
                            /*mButtonScan.setEnabled(false);
                            mButtonSave.setEnabled(false);
                            mCheckUsbHostMode.setEnabled(false);
                            mButtonStop.setEnabled(true);*/
                        }
                    } else
                        //mMessage.setText("Can't open scanner device");
                        break;
                case UsbDeviceDataExchangeImpl.MESSAGE_DENY_DEVICE:
                    channel.invokeMethod("message", "User deny scanner device");
                    break;
            }
        }
    };

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addRequestPermissionsResultListener(this);
        init();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
        mStop = true;
        if (mFPScan != null) {
            mFPScan.stop();
            mFPScan = null;
        }
        usb_host_ctx.CloseDevice();
        usb_host_ctx.Destroy();
        usb_host_ctx = null;

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addRequestPermissionsResultListener(this);
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //Log.v(TAG,"Permission is granted");
                return true;
            } else {
                //Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            //Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    private void SaveImageByFileFormat(String fileFormat, String fileName) {
        if (fileFormat.compareTo("WSQ") == 0)    //save wsq file
        {
            Scanner devScan = new Scanner();
            boolean bRet;
            if (mUsbHostMode)
                bRet = devScan.OpenDeviceOnInterfaceUsbHost(usb_host_ctx);
            else
                bRet = devScan.OpenDevice();
            if (!bRet) {
                channel.invokeMethod("message", devScan.GetErrorMessage());
                return;
            }
            byte[] wsqImg = new byte[mImageWidth * mImageHeight];
            long hDevice = devScan.GetDeviceHandle();
            ftrWsqAndroidHelper wsqHelper = new ftrWsqAndroidHelper();
            if (wsqHelper.ConvertRawToWsq(hDevice, mImageWidth, mImageHeight, 2.25f, mImageFP, wsqImg)) {
                File file = new File(fileName);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    out.write(wsqImg, 0, wsqHelper.mWSQ_size);    // save the wsq_size bytes data to file
                    out.close();
                    channel.invokeMethod("message", "Image is saved as " + fileName);
                } catch (Exception e) {
                    channel.invokeMethod("message", "Exception in saving file");
                }
            } else
                channel.invokeMethod("message", "Failed to convert the image!");
            if (mUsbHostMode)
                devScan.CloseDeviceUsbHost();
            else
                devScan.CloseDevice();
            return;
        }
        // 0 - save bitmap file
        File file = new File(fileName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            //mBitmapFP.compress(Bitmap.CompressFormat.PNG, 90, out);
            MyBitmapFile fileBMP = new MyBitmapFile(mImageWidth, mImageHeight, mImageFP);
            out.write(fileBMP.toBytes());
            out.close();
            channel.invokeMethod("message", "Image is saved as " + fileName);
        } catch (Exception e) {
            channel.invokeMethod("message", "Exception in saving file");
        }
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            //Log.v("FtrScanDemoUsbHost","Permission: "+permissions[0]+ "was "+grantResults[0]);
            //Log.v("FtrScanDemoUsbHost","Permission: "+permissions[1]+ "was "+grantResults[1]);
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //resume tasks needing this permission
                isScanButton = true;
            }
            return true;
        }
        return false;
    }

    /*public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_FILE_FORMAT:
                if (resultCode == Activity.RESULT_OK) {
                    // Get the file format
                    String[] extraString = data.getExtras().getStringArray(SelectFileFormatActivity.EXTRA_FILE_FORMAT);
                    String fileFormat = extraString[0];
                    String fileName = extraString[1];
                    SaveImageByFileFormat(fileFormat, fileName);
                }
                else
                    mMessage.setText("Cancelled!");
                break;
        }
    }*/
}