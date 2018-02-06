package com.cyrille.energytest;



import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.security.Policy;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button startButton, stopButton;
    TextView runtimeText;
    private CameraManager cameraManager;
    private String cameraId;
    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    Policy.Parameters params;
    private MediaPlayer mediaPlayer;
    private CountDownTimer countDownTimer;
    private  Integer nextswitchTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        runtimeText = (TextView) findViewById(R.id.tvRunTime);
        // flash switch button
        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);
        isFlashOn = false;
        nextswitchTime = 10;

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);


        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Sorry, your device doesn't support flash light!");
            alert.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alert.show();
            return;
        }

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

       /* // get the camera
        getCamera();

        // displaying button image
        toggleButtonImage();


        // Switch button click event to toggle flash on/off
*/

    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_button:
                if (!isFlashOn) {
                    turnOnLight();
                    isFlashOn = true;
                    Log.i("turnOnLight ", "turnOnLight button clicked ");

                    countDownTimer =new CountDownTimer(30000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            runtimeText.setText("Seconds remaining: " + millisUntilFinished / 1000);

                            Integer switchTime = ((int)millisUntilFinished / 1000) % nextswitchTime;
                            if ( switchTime == 0){
                                /*
                               Starts with 10 seconds off: After the first 10 seconds on, it Switch next after 8 seconds, while been off
                                 */
                                if (nextswitchTime == 8){
                                    nextswitchTime = 10;
                                } else {
                                    nextswitchTime = 8;
                                }

                                Log.i("CountdownTimer ", "Hey am " + millisUntilFinished + " __ "+ (int)millisUntilFinished / 1000 );
                                try {
                                    if (isFlashOn) {
                                        turnOffLight();
                                        isFlashOn = false;
                                    } else {
                                        turnOnLight();
                                        isFlashOn = true;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        public void onFinish() {
                            runtimeText.setText("Done!");
                            turnOffLight();
                            countDownTimer.cancel();
                            countDownTimer.onFinish();
                            isFlashOn = false;
                        }
                    };
                    countDownTimer.start();

                } else {

                }

                break;

            case R.id.stop_button:
                try {
                    if (isFlashOn) {
                        turnOffLight();
                        countDownTimer.cancel();
                        countDownTimer.onFinish();
                        isFlashOn = false;
                    } else {
                        //turnOnLight();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // turnOffLight();
                // isFlashOn = false;
                Log.i("turnOffLight ", "turnOffLight button clicked ");
                break;


        }
        /*if (isFlashOn) {
            // turn off flash
            turnOffLight();
        } else {
            // turn on flash
            turnOnLight();
        }*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFlashOn) {
            turnOffLight();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (isFlashOn) {
            turnOffLight();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (isFlashOn) {
            turnOnLight();
        }
    }

    /**
     * Method for turning light ON
     */
    public void turnOnLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, true);
                playOnOffSound();
                //ivOnOFF.setImageResource(R.drawable.on);
                //startButton.setBackgroundColor(78);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for turning light OFF
     */
    public void turnOffLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, false);
                playOnOffSound();
                //ivOnOFF.setImageResource(R.drawable.off);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playOnOffSound() {
        //mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.flash_sound);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mediaPlayer.start();
    }



    /*// Get the camera
    private void getCamera() {
        if (camera == null) {
            try {
                camera = android.hardware.Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
            }
        }
    }


    // Turning On flash
    private void turnOnFlash() {
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
            // play sound
            playSound();

            params = camera.getParameters();
            params.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = true;

            // changing button/switch image
            toggleButtonImage();
        }

    }


    // Turning Off flash
    private void turnOffFlash() {
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
            // play sound
            playSound();

            params = camera.getParameters();
            params.setFlashMode(Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashOn = false;

            // changing button/switch image
            toggleButtonImage();
        }
    }


    // Playing sound
    // will play button toggle sound on flash on / off
    private void playSound() {
        if (isFlashOn) {
            mp = MediaPlayer.create(MainActivity.this, R.raw.light_switch_off);
        } else {
            mp = MediaPlayer.create(MainActivity.this, R.raw.light_switch_on);
        }
        mp.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
            }
        });
        mp.start();
    }

    *//*
     * Toggle switch button images
     * changing image states to on / off
     * *//*
    private void toggleButtonImage() {
        if (isFlashOn) {
            btnSwitch.setImageResource(R.drawable.btn_switch_on);
        } else {
            btnSwitch.setImageResource(R.drawable.btn_switch_off);
        }
    }
*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }


    @Override
    protected void onStart() {
        super.onStart();

        // on starting the app get the camera params
        // getCamera();
    }


}