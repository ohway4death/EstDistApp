package com.example.MGHSampleCalc;

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
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Queue;
import java.util.StringTokenizer;


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

    Deque<Double> queueAccel = new ArrayDeque<>();
    Deque<Double> queueVelocity = new ArrayDeque<>();
    Deque<Double> queueDist = new ArrayDeque<>();
    Deque<Long> queueTime = new ArrayDeque<>();
    private boolean startFlag = true;
    private double saveVelo=0;
    private double saveDist=0;



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

    private ArrayList<Float> timeList;
    private ArrayList<Double> accelList;

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

        timeList = new ArrayList<>();
        accelList = new ArrayList<>();
        //queueVelocity.add(0d);
        float beforeTime=0;

        readCSV();

        for (int i=0; i<timeList.size() && i<accelList.size(); i++){

            /*
            if(queueAccel.size() == 2){
                saveVelo += distance.daikei(queueAccel, time-beforeTime);
                queueVelocity.add(saveVelo);
                queueAccel.remove();

                if (queueVelocity.size() == 2){
                    saveDist =  distance.daikei(queueVelocity, time-beforeTime);
                    estDist += saveDist;
                    queueVelocity.remove();
                }
            }
            */

            /*
            if(queueAccel.size() == 4){
                saveVelo += distance.simpson4point(queueAccel,  time-timeList.get(i-3));
                queueVelocity.add(saveVelo);

                if (queueVelocity.size() == 4){
                    saveDist += distance.simpson4point(queueVelocity,  time-timeList.get(i-9));
                    estDist = saveDist;
                    for(int j=0;j<3;j++){
                        queueVelocity.remove();
                    }
                }
                for(int j=0;j<3;j++){
                    queueAccel.remove();
                }
            }
             */

            //???????????????????????????
            if (startFlag){
                queueAccel.add(0d);
                queueAccel.add(0d);
                queueAccel.add(0d);
                queueAccel.add(0d);

                queueVelocity.add(0d);
                queueVelocity.add(0d);
                queueVelocity.add(0d);
                queueVelocity.add(0d);

                queueDist.add(0d);
                queueDist.add(0d);
                queueDist.add(0d);
                queueDist.add(0d);

                queueTime.add(0L);
                queueTime.add(0L);
                queueTime.add(0L);
                queueTime.add(0L);

                startFlag=false;
            }

            long testTime = (long) (timeList.get(i) * 1000000000F);

            queueTime.add(testTime);
            queueAccel.add(accelList.get(i));
/*
            //?????????
            if(queueAccel.size()==2){
                saveVelo = distance.daikei(queueAccel, queueTime);
                queueVelocity.add(saveVelo + queueVelocity.element());
            }
            if (queueVelocity.size()==2){
                saveDist = distance.daikei(queueVelocity,queueTime);
                queueDist.add(saveDist + queueDist.element());
            }
*/
/*
            //3?????????????????????
            if(queueAccel.size()==3){
                saveVelo = distance.simpson3point(queueAccel, queueTime);
                queueVelocity.add(saveVelo + queueVelocity.element());
            }
            if (queueVelocity.size()==3){
                saveDist = distance.simpson3point(queueVelocity,queueTime);
                queueDist.add(saveDist + queueDist.element());
            }
*/
/*
                //4???????????????????????????????????????3/8??????
                if(queueAccel.size()==4){
                    saveVelo = distance.simpson4point(queueAccel, queueTime);
                    queueVelocity.add(saveVelo + queueVelocity.element());
                }
                if (queueVelocity.size()==4){
                    saveDist = distance.simpson4point(queueVelocity,queueTime);
                    queueDist.add(saveDist + queueDist.element());
                }
*/

                //???????????????5??????
                if(queueAccel.size()==5){
                    saveVelo = distance.bool(queueAccel, queueTime);
                    queueVelocity.add(saveVelo + queueVelocity.element());
                }
                if (queueVelocity.size()==5){
                    saveDist = distance.bool(queueVelocity,queueTime);
                    queueDist.add(saveDist + queueDist.element());
                }


            queueAccel.remove();
            queueTime.remove();
            queueDist.remove();
            queueVelocity.remove();

            beforeTime = time;

            Toast toastFinish = Toast.makeText(getApplicationContext(), "Finish caliculate", Toast.LENGTH_SHORT);
            toastFinish.show();

        }

        textViewDist.setText(String.valueOf(queueDist.getLast()));

    //StartThread();

    }

    public void onClick(View view){
        switch(view.getId()){
            case R.id.recordStartButton:
                
                //recordFrag = 1;

                sensorManager.registerListener(this, accel, 10000);
                sensorManager.registerListener(this, mag, 10000);
                sensorManager.registerListener(this, gyro, 10000);

                context = getApplicationContext();
                String fileName = "stepHallwayOneway.csv";
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
                    pw.print(",");
                    pw.print("velocity");
                    pw.print(",");
                    pw.print("distance");
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
    long sampleTimeAccel;
    long sampleTimeMagnet;
    long sampleTimeGyro;
    long sampleTimeAccelBefore = 0;
    long sampleTimeMagnetBefore = 0;
    long sampleTimeGyroBefore = 0;

    @Override
    public void onSensorChanged(SensorEvent event){
        //??????????????????????????????????????????????????????????????????
        //?????????????????????????????????
        //????????????????????????????????????????????????

        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:


                sensorAccel[0] = event.values[0];
                sensorAccel[1] = event.values[1];
                sensorAccel[2] = event.values[2];
                sampleTimeAccel = event.timestamp;


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
                sampleTimeMagnet = event.timestamp;

                textViewMx.setText(String.valueOf(sensorMagnet[0]));
                textViewMy.setText(String.valueOf(sensorMagnet[1]));
                textViewMz.setText(String.valueOf(sensorMagnet[2]));

                break;

            case Sensor.TYPE_GYROSCOPE:

                sensorGyro[0] = event.values[0];
                sensorGyro[1] = event.values[1];
                sensorGyro[2] = event.values[2];
                sampleTimeGyro = event.timestamp;
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
                float difAccel = diff_time(sampleTimeAccel, sampleTimeAccelBefore);
                float difMagnet = diff_time(sampleTimeMagnet, sampleTimeMagnetBefore);
                float difGyro= diff_time(sampleTimeGyro, sampleTimeGyroBefore);
                float dif = (difAccel + difMagnet + difGyro) / 3f;
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

                //double accelGlobal [] = {ax_global, ay_global, az_global};

                //textViewAxAft.setText(String.valueOf(Math.round(axNotGrav * 100d)/100d));
                //textViewAyAft.setText(String.valueOf(Math.round(ayNotGrav * 100d)/100d));
                //textViewAzAft.setText(String.valueOf(Math.round(azNotGrav * 100d)/100d));

                textViewAxGlo.setText(String.valueOf(Math.round(ax_global * 100d)/100d));
                textViewAyGlo.setText(String.valueOf(Math.round(ay_global * 100d)/100d));
                textViewAzGlo.setText(String.valueOf(Math.round(az_global * 100d)/100d));

                sampleTimeAccelBefore = sampleTimeAccel;
                sampleTimeMagnetBefore = sampleTimeMagnet;
                sampleTimeGyroBefore = sampleTimeGyro;

                //queueAccel.add(ay_global);



                /*
                if (accelTimeSaveFlag){
                    startAccelTime = sampleTimeAccel;
                    accelTimeSaveFlag = false;
                }
                if (velocityTimeSaveFlag){
                    startVelocityTime = sampleTimeAccel;
                    velocityTimeSaveFlag = false;
                }

                 */

                /*
                //4?????????????????????
                if(queueAccel.size() == 4){
                    queueVelocity.add(distance.simpson4point(queueAccel, diff_time(sampleTimeAccel, startAccelTime)));

                    if (queueVelocity.size() == 4){
                        estDist += distance.simpson4point(queueVelocity, diff_time(sampleTimeAccel, startVelocityTime));
                        for(int i=0;i<3;i++){
                            queueVelocity.remove();
                        }
                        startVelocityTime = sampleTimeAccel;
                    }

                    for(int i=0;i<3;i++){
                        queueAccel.remove();
                    }
                    startAccelTime = sampleTimeAccel;
                }

                //3?????????????????????
                if(queueAccel.size() == 3){
                    queueVelocity.add(distance.simpson3point(queueAccel, diff_time(sampleTimeAccel, startAccelTime)));

                    if (queueVelocity.size() == 3){
                        estDist += distance.simpson3point(queueVelocity, diff_time(sampleTimeAccel, startVelocityTime));
                        for(int i=0;i<2;i++){
                            queueVelocity.remove();
                        }
                        startVelocityTime = sampleTimeAccel;
                    }

                    for(int i=0;i<2;i++){
                        queueAccel.remove();
                    }
                    startAccelTime = sampleTimeAccel;
                }

                //??????????????????
                if(queueAccel.size() == 2){
                    saveVelo += distance.daikei(queueAccel, diff_time(sampleTimeAccel, startAccelTime));
                    queueVelocity.add(saveVelo);
                    queueAccel.remove();
                    startAccelTime = sampleTimeAccel;

                    if (queueVelocity.size() == 2){
                        saveDist =  distance.daikei(queueVelocity, diff_time(sampleTimeAccel, startVelocityTime));
                        estDist += saveDist;
                        queueVelocity.remove();
                        startVelocityTime = sampleTimeAccel;
                    }
                }

                 */


/*
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
                    pw.print(az_global + ",");
                    pw.print(saveVelo + ",");
                    pw.print(saveDist);
                    pw.println();
                }


                textViewDist.setText(String.valueOf(estDist));


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
                */

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



    public void readCSV(){
        try {
            InputStream inputStream =
                    getResources().getAssets().open("accelData2.csv");

            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream);

            BufferedReader bufferReader =
                    new BufferedReader(inputStreamReader);

            String line = "";

            while((line = bufferReader.readLine()) != null){
                StringTokenizer stringTokenizer =
                        new StringTokenizer(line,",");

                timeList.add(Float.parseFloat(stringTokenizer.nextToken()));
                accelList.add(Double.parseDouble(stringTokenizer.nextToken()));

            }
            bufferReader.close();

        } catch (IOException e){
            e.printStackTrace();
        }
    }

}