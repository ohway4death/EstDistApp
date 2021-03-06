package com.example.MGHThreshold;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;


public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor accel,mag,gyro;
    private TextView textViewAx, textViewAy, textViewAz;
    private TextView textViewMx, textViewMy, textViewMz;
    private TextView textViewGx, textViewGy, textViewGz;
    private TextView textViewRoll, textViewPitch, textViewYaw;
    private TextView textViewAxAft, textViewAyAft,textViewAzAft;
    private TextView textViewAxGlo, textViewAyGlo,textViewAzGlo;
    private TextView textViewDist;

    private Orientation orientation = new Orientation();
    private Distance distance = new Distance();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private long time, startTime;

    private File file;
    private FileWriter fw;
    private PrintWriter pw;
    private Context context;

    private int recordFrag = 0;

    Queue<Double> queueAccel = new ArrayDeque<>();
    Queue<Double> queueVelocity = new ArrayDeque<>();

    private int calibrationFlag = 0;
    private float aveAx = 0;
    private float aveAy = 0;
    private float aveAz = 0;
    ArrayList<Float> accelXList = new ArrayList<>();
    ArrayList<Float> accelYList = new ArrayList<>();
    ArrayList<Float> accelZList = new ArrayList<>();

    ArrayList<Float> gyroXList = new ArrayList<>();
    ArrayList<Float> gyroYList = new ArrayList<>();
    ArrayList<Float> gyroZList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //???????????????????????????????????????????????????
        //Activity??????????????????
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //??????????????????????????????????????????????????????SensorManager????????????????????????????????????????????????
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        textViewAx = findViewById(R.id.text_view_Ax);
        textViewAy = findViewById(R.id.text_view_Ay);
        textViewAz = findViewById(R.id.text_view_Az);

        textViewMx = findViewById(R.id.text_view_Mx);
        textViewMy = findViewById(R.id.text_view_My);
        textViewMz = findViewById(R.id.text_view_Mz);

        textViewGx = findViewById(R.id.text_view_Gx);
        textViewGy = findViewById(R.id.text_view_Gy);
        textViewGz = findViewById(R.id.text_view_Gz);

        textViewRoll = findViewById(R.id.text_view_roll);
        textViewPitch = findViewById(R.id.text_view_pitch);
        textViewYaw = findViewById(R.id.text_view_yaw);

        textViewAxAft = findViewById(R.id.text_view_Ax_aft);
        textViewAyAft = findViewById(R.id.text_view_Ay_aft);
        textViewAzAft = findViewById(R.id.text_view_Az_aft);

        textViewAxGlo = findViewById(R.id.text_view_Ax_global);
        textViewAyGlo = findViewById(R.id.text_view_Ay_global);
        textViewAzGlo = findViewById(R.id.text_view_Az_global);

        textViewDist = findViewById(R.id.distance);

        //Button startButton = findViewById(R.id.recordStartButton);
        //Button stopButton = findViewById(R.id.recordStopButton);

        //SystemClock.elapsedRealtimeNanos()??????????????????????????????????????????????????????????????????????????????
        //period.setText((SystemClock.elapsedRealtimeNanos()/1e9)+"")
        time = SystemClock.elapsedRealtimeNanos();
        startTime = time;

        StartThread();

    }

    public void onClick(View view){
        switch(view.getId()){
            case R.id.recordStartButton:
                
                recordFrag = 1;

                sensorManager.registerListener(this, accel, 10000);
                sensorManager.registerListener(this, mag, 10000);
                sensorManager.registerListener(this, gyro, 10000);

                context = getApplicationContext();
                String fileName = "step5min.csv";
                file = new File(context.getFilesDir(), fileName);
                try{
                    //?????????????????????????????????????????????????????????????????????????????????????????????true???????????????????????????????????????????????????
                    fw = new FileWriter(file, true);
                    pw = new PrintWriter(new BufferedWriter(fw));

                    //???????????????????????????
                    pw.print("timeStamp");
                    pw.print(",");
                    pw.print("accelX");
                    pw.print(",");
                    pw.print("accelY");
                    pw.print(",");
                    pw.print("accelZ");
                    pw.print(",");
                    pw.print("magnetX");
                    pw.print(",");
                    pw.print("magnetY");
                    pw.print(",");
                    pw.print("magnetZ");
                    pw.print(",");
                    pw.print("gyroX");
                    pw.print(",");
                    pw.print("gyroY");
                    pw.print(",");
                    pw.print("gyroZ");
                    pw.print(",");
                    pw.print("fixedX");
                    pw.print(",");
                    pw.print("fixedY");
                    pw.print(",");
                    pw.print("fixedZ");
                    pw.print(",");
                    pw.print("globalX");
                    pw.print(",");
                    pw.print("globalY");
                    pw.print(",");
                    pw.print("globalZ");
                    pw.println();

                }catch (IOException e){
                    e.printStackTrace();
                }

                Toast toastStart = Toast.makeText(getApplicationContext(), "Start Record!!", Toast.LENGTH_SHORT);
                toastStart.show();
                break;

            case R.id.recordStopButton:
                recordFrag = 0;
                sensorManager.unregisterListener(this);
                Toast toastStop = Toast.makeText(getApplicationContext(), "Stop Record", Toast.LENGTH_SHORT);
                toastStop.show();
                break;

            case R.id.resetButton:
                queueAccel.clear();
                queueVelocity.clear();
                estDist = 0;

                Toast toastReset = Toast.makeText(getApplicationContext(), "Reset", Toast.LENGTH_SHORT);
                toastReset.show();
                break;
        }
    }

    @Override
    protected void onResume(){
        //Activity????????????????????????????????????????????????
        super.onResume();
        //getDefaultSensor????????????????????????????????????????????????????????????
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????Listener?????????
        //????????????????????????????????????????????????????????????????????????????????????????????????????????????https://akihito104.hatenablog.com/entry/2013/07/22/013000????????????
        //3???11?????????????????????????????????????????????0.2?????????????????????????????????0.2???????????????
        sensorManager.registerListener(this, accel, 10000);
        sensorManager.registerListener(this, mag, 10000);
        sensorManager.registerListener(this, gyro, 10000);
    }

    @Override
    protected void onPause() {
        //???????????????????????????????????????????????????????????????????????????
        super.onPause();
        //Listener?????????
        sensorManager.unregisterListener(this);
    }

    float sensorAx = 0;
    float sensorAy = 0;
    float sensorAz = 0;

    float sensorMx = 0;
    float sensorMy = 0;
    float sensorMz = 0;

    float sensorGx = 0;
    float sensorGy = 0;
    float sensorGz = 0;

    float[] sensorAccel = {sensorAx, sensorAy, sensorAz};
    float[] sensorMagnet = {sensorMx, sensorMy, sensorMz};
    float[] sensorGyro = {sensorGx, sensorGy, sensorGz};

    float[] sensorAccelBef = {0, 0, 0};
    float[] sensorGyroBef = {0, 0, 0};
    double axNotGravBef = 0;
    double ayNotGravBef = 0;
    double azNotGravBef = 0;
    long sampleTime;

    @Override
    public void onSensorChanged(SensorEvent event){
        //??????????????????????????????????????????????????????????????????
        //?????????????????????????????????
        //????????????????????????????????????????????????

        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:


                if (event.values[0] >= -0.3f && event.values[0] <= 0.3f){
                    sensorAccel[0] = 0;
                }else{
                    sensorAccel[0] = event.values[0];
                }

                if (event.values[1] >= -0.3f && event.values[1] <= 0.3f){
                    sensorAccel[1] = 0;
                }else{
                    sensorAccel[1] = event.values[1];
                }

                if (event.values[2] >= -0.3f && event.values[2] <= 0.3f){
                    sensorAccel[2] = 0;
                }else{
                    sensorAccel[2] = event.values[2];
                }

                sampleTime = event.timestamp;


                //sensorAccel[0] = 0.1F * sensorAccel[0] - 0.9F * sensorAccelBef[0];
                //sensorAccel[1] = 0.1F * sensorAccel[1] - 0.9F * sensorAccelBef[1];
                //sensorAccel[2] = 0.1F * sensorAccel[2] - 0.9F * sensorAccelBef[2];

                //sensorAccel[0] = event.values[0] - sensorAccel[0];
                //sensorAccel[1] = event.values[1] - sensorAccel[1];
                //sensorAccel[2] = event.values[2] - sensorAccel[2];

                sensorAccelBef[0] = sensorAccel[0];
                sensorAccelBef[1] = sensorAccel[1];
                sensorAccelBef[2] = sensorAccel[2];

                textViewAx.setText(String.valueOf(sensorAccel[0]));
                textViewAy.setText(String.valueOf(sensorAccel[1]));
                textViewAz.setText(String.valueOf(sensorAccel[2]));

                break;


            case Sensor.TYPE_MAGNETIC_FIELD:

                sensorMagnet[0] = event.values[0] ;
                sensorMagnet[1] = event.values[1] ;
                sensorMagnet[2] = event.values[2] ;
                sampleTime = event.timestamp;

                textViewMx.setText(String.valueOf(sensorMagnet[0]));
                textViewMy.setText(String.valueOf(sensorMagnet[1]));
                textViewMz.setText(String.valueOf(sensorMagnet[2]));

                break;


            case Sensor.TYPE_GYROSCOPE:

                if (event.values[0] >= -0.02 && event.values[0] <= 0.02){
                    sensorMagnet[0] = 0;
                }else{
                    sensorMagnet[0] = event.values[0];
                }

                if (event.values[1] >= -0.02 && event.values[1] <= 0.02){
                    sensorMagnet[1] = 0;
                }else{
                    sensorMagnet[1] = event.values[1];
                }

                if (event.values[2] >= -0.02 && event.values[2] <= 0.02){
                    sensorMagnet[2] = 0;
                }else{
                    sensorMagnet[2] = event.values[2];
                }

                //sensorGyro[0] = event.values[0];
                //sensorGyro[1] = event.values[1];
                //sensorGyro[2] = event.values[2];
                sampleTime = event.timestamp;
/*
                sensorGyro[0] = 0.9F * event.values[0] - 0.1F * sensorGyroBef[0];
                sensorGyro[1] = 0.9F * event.values[1] - 0.1F * sensorGyroBef[1];
                sensorGyro[2] = 0.9F * event.values[2] - 0.1F * sensorGyroBef[2];

                sensorGyroBef[0] = sensorGyro[0];
                sensorGyroBef[1] = sensorGyro[1];
                sensorGyroBef[2] = sensorGyro[2];
*/
                textViewGx.setText(String.valueOf(sensorGyro[0]));
                textViewGy.setText(String.valueOf(sensorGyro[1]));
                textViewGz.setText(String.valueOf(sensorGyro[2]));

                break;
        }

    }

    double ax_global, ay_global, az_global;

    double estDist = 0;

    private void StartThread(){
        runnable = new Runnable() {
            @Override
            public void run() {
                float dif = diff_time(sampleTime, time);
                //9????????????
                float rpy[] = orientation.madgwickFilter(sensorAccel, sensorGyro, sensorMagnet, 1.0F / dif);
                //???????????????
                //float rpy[] = orientation.madgwickFilterNoMag(sensorAccel, sensorGyro, 1.0F / dif);


                //textViewRoll.setText(String.valueOf(rpy[0]));
                //textViewPitch.setText(String.valueOf(rpy[1]));
                //textViewYaw.setText(String.valueOf(rpy[2]));

                double axNotGrav = sensorAccel[0] + 9.8f * Math.sin(Math.toRadians(rpy[1]));
                double ayNotGrav = sensorAccel[1] - 9.8f * Math.cos(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0]));
                double azNotGrav = sensorAccel[2] - 9.8f * Math.cos(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0]));

                ax_global = Math.cos(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[1])) * (double) sensorAccel[0] +
                        (Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) - Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * (double)sensorAccel[1] +
                        (Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) + Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[0]))) * (double)sensorAccel[2];

                ay_global = Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[1])) * (double)sensorAccel[0] +
                        (Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) + Math.cos(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * (double)sensorAccel[1] +
                        (Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) - Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[0]))) * (double)sensorAccel[2];

                az_global = -Math.sin(Math.toRadians(rpy[1])) * (double)sensorAccel[0] +
                        Math.cos(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) * (double)sensorAccel[1] +
                        Math.cos(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) * (double)sensorAccel[2];

                //?????????????????????????????????????????????????????????????????????????????????
/*
                ax_global = Math.cos(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[1])) * (double)axNotGrav +
                        (Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) - Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * (double)ayNotGrav +
                        (Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) + Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[0]))) * (double)azNotGrav;

                ay_global = Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[1])) * (double)axNotGrav +
                        (Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) + Math.cos(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * (double)ayNotGrav +
                        (Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) - Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[0]))) * (double)azNotGrav;

                az_global = -Math.sin(Math.toRadians(rpy[1])) * (double)axNotGrav +
                        Math.cos(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) * (double)ayNotGrav +
                        Math.cos(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) * (double)azNotGrav;
*/

                double accelGlobal [] = {ax_global, ay_global, az_global};

                queueAccel.add(ax_global);
                /*
                if(queueAccel.size() == 4){
                    queueVelocity.add(distance.simpson4point(queueAccel, dif));

                    if (queueVelocity.size() == 4){
                        estDist += distance.simpson4point(queueVelocity, dif);
                        for(int i=0;i<3;i++){
                            queueVelocity.remove();
                        }
                    }

                    for(int i=0;i<3;i++){
                        queueAccel.remove();
                    }
                }
                 */

                if(queueAccel.size() == 2){
                    queueVelocity.add(distance.daikei(queueAccel, dif));

                    if (queueVelocity.size() == 2){
                        estDist += distance.daikei(queueVelocity, dif);
                        queueVelocity.remove();
                    }

                    queueAccel.remove();

                }

                textViewDist.setText(String.valueOf(estDist));

                //textViewAxAft.setText(String.valueOf(Math.round(axNotGrav * 100d)/100d));
                //textViewAyAft.setText(String.valueOf(Math.round(ayNotGrav * 100d)/100d));
                //textViewAzAft.setText(String.valueOf(Math.round(azNotGrav * 100d)/100d));

                textViewAxGlo.setText(String.valueOf(Math.round(ax_global * 100d)/100d));
                textViewAyGlo.setText(String.valueOf(Math.round(ay_global * 100d)/100d));
                textViewAzGlo.setText(String.valueOf(Math.round(az_global * 100d)/100d));

                time = sampleTime;

                if(recordFrag == 1){
                    pw.print(time + ",");
                    pw.print(sensorAccel[0] + ",");
                    pw.print(sensorAccel[1] + ",");
                    pw.print(sensorAccel[2] + ",");
                    pw.print(sensorMagnet[0] + ",");
                    pw.print(sensorMagnet[1] + ",");
                    pw.print(sensorMagnet[2] + ",");
                    pw.print(sensorGyro[0] + ",");
                    pw.print(sensorGyro[1] + ",");
                    pw.print(sensorGyro[2] + ",");
                    pw.print(axNotGrav + ",");
                    pw.print(ayNotGrav + ",");
                    pw.print(azNotGrav + ",");
                    pw.print(ax_global + ",");
                    pw.print(ay_global + ",");
                    pw.print(az_global);
                    pw.println();
                };

                if((time - startTime)/1000000000L <= 60){
                    accelXList.add(sensorAccel[0]);
                    accelYList.add(sensorAccel[1]);
                    accelZList.add(sensorAccel[2]);
                    gyroXList.add(sensorGyro[0]);
                    gyroYList.add(sensorGyro[1]);
                    gyroZList.add(sensorGyro[2]);
                    
                }else if((time - startTime)/1000000000L > 60 && calibrationFlag == 0){
                    for(int i=0;i<accelXList.size();i++){
                        aveAx += accelXList.get(i);
                    }
                    for(int i=0;i<accelXList.size();i++){
                        aveAy += accelYList.get(i);
                    }
                    for(int i=0;i<accelXList.size();i++){
                        aveAz += accelZList.get(i);
                    }
                    aveAx /= accelXList.size();
                    aveAy /= accelYList.size();
                    aveAz /= accelZList.size();
                    calibrationFlag = 1;
                }

                handler.postDelayed(this, 30);

            }
        };
        handler.post(runnable);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //?????????????????????????????????????????????????????????????????????
    }

    public float diff_time(long time, long bef_time){
        return (time - bef_time) / 1000000000F;
    }

}