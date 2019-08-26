package com.example.demoapplication;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.dft.onyx.FingerprintTemplate;
import com.dft.onyxcamera.config.Onyx;
import com.dft.onyxcamera.ui.OnyxFragment;
import com.dft.onyxcamera.ui.OnyxFragmentFactory;

public class HelperActivity extends AppCompatActivity {
    private Onyx configuredOnyx;
    private ImageView mFingerprintView;
    private Animation mFadeIn;
    private Animation mFadeOut;
    private OnyxFragment mFragment;
    private FingerprintTemplate mCurrentTemplate = null;
    private double mCurrentFocusQuality = 0.0;
    private FingerprintTemplate mEnrolledTemplate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper);
        getSupportActionBar().hide();
        //setUp();
    }

    private void setUp() {
        mFragment = new OnyxFragmentFactory().getOnyxFragment(this);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.fragment_content, mFragment);
        ft.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Setting the activity being used to run Onyx here so that it can be finished from
        // the SuccessCallback in OnyxSetupActivity
        MainApplication.setActivityForRunningOnyx1(this);

        // Get the configured Onyx that was returned from the OnyxCallback
        configuredOnyx = MainApplication.getConfiguredOnyx();

        // Creates Onyx in this activity
        //configuredOnyx.create(this);

        //setUp();
        configuredOnyx.create(this);

        /*CaptureConfiguration captureConfig = new CaptureConfigurationBuilder()
                .setProcessedBitmapCallback(mProcessedCallback)
                .setWsqCallback(mWsqCallback)
                .setFingerprintTemplateCallback(mTemplateCallback)
                .setShouldInvert(true)
                .setFlip(CaptureConfiguration.Flip.VERTICAL)
                .buildCaptureConfiguration();
        mFragment.setCaptureConfiguration(captureConfig);
        mFragment.setErrorCallback(mErrorCallback);
        mFragment.startOneShotAutoCapture();*/

        // Make Onyx start the capture process
        // Important: configuredOnyx.capture() must occur after configuredOnyx.create() has been called
        if (!configuredOnyx.getOnyxConfig().isManualCapture()) {
            // Start the capture with auto capture process
            configuredOnyx.capture();

            createFadeInAnimation();
            createFadeOutAnimation();

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mFingerprintView = new ImageView(this);
            this.addContentView(mFingerprintView, layoutParams);
        }

    }

    private void createFadeOutAnimation() {
        mFadeOut = new AlphaAnimation(1.0f, 0.0f);
        mFadeOut.setDuration(500);
        mFadeOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                mFingerprintView.setVisibility(View.INVISIBLE);
                if (mEnrolledTemplate == null) {
                    // createEnrollQuestionDialog();
                } else {
                    mFragment.startOneShotAutoCapture();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });
    }

    private void createFadeInAnimation() {
        mFadeIn = new AlphaAnimation(0.0f, 1.0f);
        mFadeIn.setDuration(500);
        mFadeIn.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                new CountDownTimer(1000, 1000) {

                    @Override
                    public void onFinish() {
                        mFingerprintView.startAnimation(mFadeOut);
                    }

                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                }.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
                mFingerprintView.setVisibility(View.VISIBLE);
            }
        });
    }
}
