package com.example.demoapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dft.onyxcamera.config.OnyxConfiguration;
import com.dft.onyxcamera.config.OnyxConfigurationBuilder;
import com.dft.onyxcamera.config.OnyxError;
import com.dft.onyxcamera.config.OnyxResult;
import com.dft.onyxcamera.ui.reticles.Reticle;
import com.example.demoapplication.databinding.ActivityMainBinding;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;

public class MainActivity extends AppCompatActivity implements ProviderInstaller.ProviderInstallListener {
    private static final String TAG = MainActivity.class.getName();
    ActivityMainBinding mainBinding;

    private static final int ONYX_REQUEST_CODE = 1337;
    MainApplication application = new MainApplication();

    private Activity activity;
    private AlertDialog alertDialog;

    //layout components
   /* private ImageView fingerprintView;
    private Button startOnyxButton;
    private AlertDialog alertDialog;
    private TextView livenessResultTextView;
    private TextView nfiqScoreTextView;*/

    private OnyxConfiguration.SuccessCallback successCallback;
    private OnyxConfiguration.ErrorCallback errorCallback;
    private OnyxConfiguration.OnyxCallback onyxCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProviderInstaller.installIfNeededAsync(this, this); // This is needed in order for SSL to work on Android 5.1 devices and lower
        //FileUtil fileUtil = new FileUtil();
        //fileUtil.getWriteExternalStoragePermission(this); // This is for file writing permission on SDK >= 23
        //setupUI();
        activity = this;
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setupCallbacks();
        setUp();
    }

    private void setUp() {
        mainBinding.startOnyxButton.setEnabled(false);
        mainBinding.refreshConfigButton.setOnClickListener(v -> {
            setupOnyx(activity);
            mainBinding.startOnyxButton.setEnabled(false);
        });
        mainBinding.startOnyxButton.setOnClickListener(v -> {
            MainApplication.setOnyxResult(null);
            startActivityForResult(new Intent(activity, OnyxActivity.class), ONYX_REQUEST_CODE);
        });
    }

    private void setupOnyx(Activity activity) {
        OnyxConfigurationBuilder onyxConfigurationBuilder = new OnyxConfigurationBuilder()
                .setActivity(activity)
                .setLicenseKey(getResources().getString(R.string.onyx_license))
                .setReturnRawImage(true) //return raw image
                .setReturnProcessedImage(false) //return processed image
                .setReturnEnhancedImage(false) //return enhanced image
                .setReturnWSQ(false) //return wsq
                .setReturnFingerprintTemplate(true) //show fingerprint template
                .setThresholdProcessedImage(false)
                .setShowLoadingSpinner(true)
                .setUseOnyxLive(true)
                .setUseFlash(false)
                .setImageRotation(90)
                .setReticleOrientation(Reticle.Orientation.LEFT) //Reticle.Orientation.LEFT : Reticle.Orientation.RIGHT;
                .setCropSize(512,512)
                .setCropFactor(1.0f)
                .setUseFourFingerReticle(true)
                .setLayoutPreference(OnyxConfiguration.LayoutPreference.FULL)
                .setSuccessCallback(successCallback)
                .setErrorCallback(errorCallback)
                .setOnyxCallback(onyxCallback);

        // Reticle Angle overrides Reticle Orientation so have to set this separately

       /* if (getReticleAngle(this) != null) {
            onyxConfigurationBuilder.setReticleAngle(getReticleAngle(this));
        }*/

        onyxConfigurationBuilder.setReticleAngle(2.0f); //float value
        onyxConfigurationBuilder.setUseManualCapture(false);
        // Finally, build the OnyxConfiguration
        onyxConfigurationBuilder.buildOnyxConfiguration();
    }

    private void setupCallbacks() {
        successCallback = onyxResult -> {
            MainApplication.setOnyxResult(onyxResult);
            finishActivityForRunningOnyx();
        };

        errorCallback = onyxError -> {
            Log.e("OnyxError", onyxError.getErrorMessage());
            application.setOnyxError(onyxError);
            showAlertDialog(onyxError);
            finishActivityForRunningOnyx();
        };

        onyxCallback = configuredOnyx -> {
            application.setConfiguredOnyx(configuredOnyx);
            activity.runOnUiThread(() -> mainBinding.startOnyxButton.setEnabled(true));
        };
    }

    private void finishActivityForRunningOnyx() {
        if (MainApplication.getActivityForRunningOnyx() != null) {
            MainApplication.getActivityForRunningOnyx().finish();
        }
    }

    private void showAlertDialog(OnyxError onyxError) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setTitle("Onyx Error");
        alertDialogBuilder.setMessage(onyxError.getErrorMessage());
        alertDialogBuilder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> alertDialog.dismiss());
        alertDialogBuilder.setOnCancelListener(dialogInterface -> alertDialog.dismiss());

        activity.runOnUiThread(() -> {
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MainApplication.getOnyxResult() != null) {
            displayResults(MainApplication.getOnyxResult());
        }
    }

    private void displayResults(OnyxResult onyxResult) {

    }

    @Override
    public void onProviderInstalled() {

    }

    private static final int ERROR_DIALOG_REQUEST_CODE = 11111;

    @Override
    public void onProviderInstallFailed(int errorCode, Intent intent) {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        if (availability.isUserResolvableError(errorCode)) {
            // Recoverable error. Show a dialog prompting the user to
            // install/update/enable Google Play services.
            availability.showErrorDialogFragment(
                    this,
                    errorCode,
                    ERROR_DIALOG_REQUEST_CODE,
                    dialog -> {
                        // The user chose not to take the recovery action
                        onProviderInstallerNotAvailable();
                    });
        } else {
            // Google Play services is not available.
            onProviderInstallerNotAvailable();
        }
    }

    private void onProviderInstallerNotAvailable() {
        // This is reached if the provider cannot be updated for some reason.
        // App should consider all HTTP communication to be vulnerable, and take
        // appropriate action.
        Log.i("OnyxSetupActivity", "ProviderInstaller not available, device cannot make secure network calls.");
    }
}
