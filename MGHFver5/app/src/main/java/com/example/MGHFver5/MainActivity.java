package com.example.MGHFver5;

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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;


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

    private final Orientation orientation = new Orientation();
    private final Integral integral = new Integral();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private long time, initTime;

    //betaの変化を見るための変数
    private long Start;

    private File file;
    private FileWriter fw;
    private PrintWriter pw;
    private Context context;

    private boolean recordFrag = false;

    final int queueSize = 2;

    FixedSizeQueue queueAccelX = new FixedSizeQueue(queueSize);
    FixedSizeQueue queueAccelY = new FixedSizeQueue(queueSize);
    FixedSizeQueue queueAccelZ = new FixedSizeQueue(queueSize);
    FixedSizeLongQueue queueAccTime = new FixedSizeLongQueue(queueSize);
    FixedSizeQueue queueVelocityX = new FixedSizeQueue(queueSize);
    FixedSizeQueue queueVelocityY = new FixedSizeQueue(queueSize);
    FixedSizeQueue queueVelocityZ = new FixedSizeQueue(queueSize);
    FixedSizeLongQueue queueVeloTime = new FixedSizeLongQueue(queueSize);
    private double distance=0;
    private boolean startFlag = false;
    private boolean resetFrag = false;
    private double saveVeloX=0;
    private double saveVeloY=0;
    private double saveVeloZ=0;
    private double saveDistX=0;
    private double saveDistY=0;
    private double saveDistZ=0;

    private final int calibrationFlag = 0;
    private final float aveAx = 0;
    private final float aveAy = 0;
    private final float aveAz = 0;
    ArrayList<Float> accelXList = new ArrayList<>();
    ArrayList<Float> accelYList = new ArrayList<>();
    ArrayList<Float> accelZList = new ArrayList<>();

    ArrayList<Float> gyroXList = new ArrayList<>();
    ArrayList<Float> gyroYList = new ArrayList<>();
    ArrayList<Float> gyroZList = new ArrayList<>();

    private Spinner fileSpinner;
    private Spinner methodSpinner;
    String[] fileSpinnerItems = {
            "case1.csv",
            "case2.csv",
            "case3.csv",
            "case4.csv",
            "case5.csv"
    };
    String[] methodSpinnerItems = {
            "trapezoidal",
            "simpson3",
            "simpson4",
            "boole",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //アプリを起動した際に実行される部分
        //Activityが生成される
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //センサーサービスへの参照を取得する，SensorManagerクラスのインスタンスを作成する．
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

        fileSpinner = findViewById(R.id.fileSpinner);
        ArrayAdapter<String> fileAdapter
                = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fileSpinnerItems);
        fileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fileSpinner.setAdapter(fileAdapter);

        methodSpinner = findViewById(R.id.methodSpinner);
        ArrayAdapter<String> methodAdapter
                = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, methodSpinnerItems);
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        methodSpinner.setAdapter(methodAdapter);

        /*
        methodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!methodSpinner.isFocusable()) {
                    methodSpinner.setFocusable(true);
                    return;
                }
                queueAccel.clear();
                queueTime.clear();
                queueVelocity.clear();
                queueDist.clear();

                //startFlag = true;

                saveVelo=0;
                saveDist=0;
                estDist=0;

                int num = 0;

                switch ((String) methodSpinner.getSelectedItem()){
                    case "trapezoidal" : num = 1; break;
                    case "simpson3": num = 2; break;
                    case "simpson4": num = 3; break;
                    case "boole": num = 4; break;
                }

                for(int j=0;j<num;j++){
                    queueAccel.add(0d);
                    queueVelocity.add(0d);
                    queueDist.add(0d);
                    queueTime.add(0L);
                }

                Toast toastReset = Toast.makeText(getApplicationContext(), "Reset", Toast.LENGTH_SHORT);
                toastReset.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
         */

        //Button startButton = findViewById(R.id.recordStartButton);
        //Button stopButton = findViewById(R.id.recordStopButton);
        //SystemClock.elapsedRealtimeNanos()はスマホが起動してからの時間を表示する，単位はナノ秒
        //period.setText((SystemClock.elapsedRealtimeNanos()/1e9)+"")

        time = SystemClock.elapsedRealtimeNanos();
        initTime = time;
        Start = time;


        resetVariable();

        //アプリ起動時からbetaによる各値の推移を取得する
        /*
        context = getApplicationContext();
        //String fileName = "testes.csv";
        file = new File(context.getFilesDir(), "distCheck.csv");
        try{
            //引数はファイル名と書き込まれたデータを追加するかどうか決める、trueならファイルの最後に追記していく。
            fw = new FileWriter(file);
            pw = new PrintWriter(new BufferedWriter(fw));

            //ヘッダーを作成する
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
            pw.print("roll");
            pw.print(",");
            pw.print("pitch");
            pw.print(",");
            pw.print("yaw");
            pw.print(",");
            pw.print("globalX");
            pw.print(",");
            pw.print("globalY");
            pw.print(",");
            pw.print("globalZ");
            pw.print(",");
            pw.print("velocityX");
            pw.print(",");
            pw.print("velocityY");
            pw.print(",");
            pw.print("velocityZ");
            pw.print(",");
            pw.print("distanceX");
            pw.print(",");
            pw.print("distanceY");
            pw.print(",");
            pw.print("distanceZ");
            pw.print(",");
            pw.print("distance");
            pw.println();


            pw.print(queueAccTime.longSearchLast() + ",");
            pw.print(sensorAccel[0] + ",");
            pw.print(sensorAccel[1] + ",");
            pw.print(sensorAccel[2] + ",");
            pw.print(sensorMagnet[0] + ",");
            pw.print(sensorMagnet[1] + ",");
            pw.print(sensorMagnet[2] + ",");
            pw.print(sensorGyro[0] + ",");
            pw.print(sensorGyro[1] + ",");
            pw.print(sensorGyro[2] + ",");
            pw.print(rpy[0] + ",");
            pw.print(rpy[1] + ",");
            pw.print(rpy[2] + ",");
            pw.print(ax_global + ",");
            pw.print(ay_global + ",");
            pw.print(az_global + ",");
            pw.print(queueVelocityX.searchLast() + ",");
            pw.print(queueVelocityY.searchLast() + ",");
            pw.print(queueVelocityZ.searchLast() + ",");
            pw.print(distance);
            pw.println();


        }catch (IOException e){
            e.printStackTrace();
        }
         */

        //ここまで↑

        //test
        //queueAccelX.add(0d);
        //queueAccelY.add(0d);
        //queueAccelZ.add(0d);

        //StartThread();

    }

    public void onClick(View view){
        switch(view.getId()){
            case R.id.recordStartButton:

                initTime = SystemClock.elapsedRealtimeNanos();

                //sampleTime-initTimeがマイナスになるのを防ぐため
                sensorManager.unregisterListener(this);
                sensorManager.registerListener(this, accel, 10000);
                sensorManager.registerListener(this, mag, 10000);
                sensorManager.registerListener(this, gyro, 10000);

                String choiceFileName = (String) fileSpinner.getSelectedItem();
                String choiceMethodName = (String) methodSpinner.getSelectedItem();

                recordFrag = true;
                startFlag = true;

                resetVariable();

                //ver5ではt=0での加速度は0ではなく実際に取得した値とする
                queueAccelX.add(ax_global);
                queueAccelY.add(ay_global);
                queueAccelZ.add(az_global);

                context = getApplicationContext();
                //String fileName = "testes.csv";
                file = new File(context.getFilesDir(), choiceMethodName+"_"+choiceFileName);
                try{
                    //引数はファイル名と書き込まれたデータを追加するかどうか決める、trueならファイルの最後に追記していく。
                    fw = new FileWriter(file);
                    pw = new PrintWriter(new BufferedWriter(fw));

                    //ヘッダーを作成する
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
                    pw.print("roll");
                    pw.print(",");
                    pw.print("pitch");
                    pw.print(",");
                    pw.print("yaw");
                    pw.print(",");
                    pw.print("NotGrabX");
                    pw.print(",");
                    pw.print("NotGrabY");
                    pw.print(",");
                    pw.print("NotGrabZ");
                    pw.print(",");
                    pw.print("globalX");
                    pw.print(",");
                    pw.print("globalY");
                    pw.print(",");
                    pw.print("globalZ");
                    pw.print(",");
                    pw.print("velocityX");
                    pw.print(",");
                    pw.print("velocityY");
                    pw.print(",");
                    pw.print("velocityZ");
                    pw.print(",");
                    pw.print("distanceX");
                    pw.print(",");
                    pw.print("distanceY");
                    pw.print(",");
                    pw.print("distanceZ");
                    pw.print(",");
                    pw.print("distance");
                    pw.println();

                    pw.print(queueAccTime.longSearchLast() + ",");
                    pw.print(sensorAccel[0] + ",");
                    pw.print(sensorAccel[1] + ",");
                    pw.print(sensorAccel[2] + ",");
                    pw.print(sensorMagnet[0] + ",");
                    pw.print(sensorMagnet[1] + ",");
                    pw.print(sensorMagnet[2] + ",");
                    pw.print(sensorGyro[0] + ",");
                    pw.print(sensorGyro[1] + ",");
                    pw.print(sensorGyro[2] + ",");
                    pw.print(rpy[0] + ",");
                    pw.print(rpy[1] + ",");
                    pw.print(rpy[2] + ",");
                    pw.print(axNotGrav + ",");
                    pw.print(ayNotGrav + ",");
                    pw.print(azNotGrav + ",");
                    pw.print(ax_global + ",");
                    pw.print(ay_global + ",");
                    pw.print(az_global + ",");
                    pw.print(queueVelocityX.searchLast() + ",");
                    pw.print(queueVelocityY.searchLast() + ",");
                    pw.print(queueVelocityZ.searchLast() + ",");
                    pw.print(saveDistX + ",");
                    pw.print(saveDistY + ",");
                    pw.print(saveDistZ + ",");
                    pw.print(distance);
                    pw.println();

                }catch (IOException e){
                    e.printStackTrace();
                }

                Toast toastStart = Toast.makeText(getApplicationContext(), "Start Record!!", Toast.LENGTH_SHORT);
                toastStart.show();


                break;

            case R.id.recordStopButton:
                recordFrag = false;
                sensorManager.unregisterListener(this);
                Toast toastStop = Toast.makeText(getApplicationContext(), "Stop Record", Toast.LENGTH_SHORT);
                toastStop.show();
                break;

            case R.id.resetButton:
                //sensorManager.registerListener(this, accel, 10000);
                //sensorManager.registerListener(this, mag, 10000);
                //sensorManager.registerListener(this, gyro, 10000);

                resetFrag = true;

                initTime = SystemClock.elapsedRealtimeNanos();

                sensorManager.unregisterListener(this);
                sensorManager.registerListener(this, accel, 10000);
                sensorManager.registerListener(this, mag, 10000);
                sensorManager.registerListener(this, gyro, 10000);

                startFlag = true;

                resetVariable();

                Toast toastReset = Toast.makeText(getApplicationContext(), "Reset", Toast.LENGTH_SHORT);
                toastReset.show();

                break;
        }
    }

    @Override
    protected void onResume(){
        //Activityが表示されたときに実行される部分
        super.onResume();
        //getDefaultSensorは加速度センサがあるかどうか判断している
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //イベント（ここでは加速度を計測したことを指す）が発生した際にイベントを受け取るListenerの登録
        //取得間隔はマイクロ秒単位で設定できるらしいが，加速度センサは無理らしい（https://akihito104.hatenablog.com/entry/2013/07/22/013000）を参照
        //3月11日時点で何度やっても取得時間が0.2秒より大きくできない，0.2秒が最大？
        sensorManager.registerListener(this, accel, 10000);
        sensorManager.registerListener(this, mag, 10000);
        sensorManager.registerListener(this, gyro, 10000);
    }

    @Override
    protected void onPause() {
        //別のアクティビティが開始されるときに実行される部分
        super.onPause();
        //Listenerの解除
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

    long sampleTimeAccel;
    long sampleTimeMagnet;
    long sampleTimeGyro;


    long sampleTime;

    float[] rpy = {0,0,0};
    float[] qX = {0,0,0,0};

    int a = 0;

    @Override
    public void onSensorChanged(SensorEvent event){
        //ここは頻繁に呼び出されるので処理は簡単にする
        //計算はここの外側で行う
        //センサーのイベントを処理する部分
        //Log.i("onSensorChangedTime",String.valueOf(SystemClock.elapsedRealtimeNanos()/1000000000d));

        /*
        if ((event.timestamp - Start)/1000000000f >= 180){
            recordFrag=false;
            sensorManager.unregisterListener(this);

        }
        */

        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                //Log.i("AccelTime",String.valueOf(event.timestamp/1000000000d));

                sensorAccel[0] = event.values[0];
                sensorAccel[1] = event.values[1];
                sensorAccel[2] = event.values[2];



                //float a = 0.76f;

                //sensorAccel[0] = a * sensorAccel[0] + (1-a) * event.values[0];
                //sensorAccel[1] = a * sensorAccel[1] + (1-a) * event.values[1];
                //sensorAccel[2] = a * sensorAccel[2] + (1-a) * event.values[2];

                //sensorAccelBef[0] = sensorAccel[0];
                //sensorAccelBef[1] = sensorAccel[1];
                //sensorAccelBef[2] = sensorAccel[2];

                sampleTimeAccel = event.timestamp;
                sampleTime = event.timestamp;


                textViewAx.setText(String.valueOf(sensorAccel[0]));
                textViewAy.setText(String.valueOf(sensorAccel[1]));
                textViewAz.setText(String.valueOf(sensorAccel[2]));

                float dif = diff_time(sampleTime-initTime, time-initTime);
                //9軸センサ
                rpy = orientation.madgwickFilter(sensorAccel, sensorGyro, sensorMagnet, 1.0F / dif);
                //qX = orientation.madgwickFilter(sensorAccel, sensorGyro, sensorMagnet, 1.0F / dif);

                //６軸センサ
                //rpy = orientation.madgwickFilterNoMag(sensorAccel, sensorGyro, 1.0F / dif);
                //qX = orientation.madgwickFilterNoMag(sensorAccel, sensorGyro, 1.0F / dif);

                axNotGrav = sensorAccel[0] + 9.80665f * Math.sin(Math.toRadians(rpy[1]));
                ayNotGrav = sensorAccel[1] - 9.80665f * Math.cos(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0]));
                azNotGrav = sensorAccel[2] - 9.80665f * Math.cos(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0]));

/*
                ax_global = Math.cos(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[1])) * (double) sensorAccel[0] +
                        (Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) - Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * (double)sensorAccel[1] +
                        (Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) + Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[0]))) * (double)sensorAccel[2];

                ay_global = Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[1])) * (double)sensorAccel[0] +
                        (Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) + Math.cos(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * (double)sensorAccel[1] +
                        (Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) - Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[0]))) * (double)sensorAccel[2];

                az_global = -Math.sin(Math.toRadians(rpy[1])) * (double)sensorAccel[0] +
                        Math.cos(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) * (double)sensorAccel[1] +
                        Math.cos(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) * (double)sensorAccel[2];
*/
                //以下は重力加速度を打ち消したものを世界座標系にしたもの
/*
                ax_global = Math.cos(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[1])) * axNotGrav +
                        (Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) - Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * ayNotGrav +
                        (Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) + Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[0]))) * azNotGrav;

                ay_global = Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[1])) * axNotGrav +
                        (Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) + Math.cos(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * ayNotGrav +
                        (Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) - Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[0]))) * azNotGrav;

                az_global = -Math.sin(Math.toRadians(rpy[1])) * axNotGrav +
                        Math.cos(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) * ayNotGrav +
                        Math.cos(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) * azNotGrav;
*/

                double cosRoll = Math.cos(Math.toRadians(-rpy[0]));
                double cosPitch = Math.cos(Math.toRadians(-rpy[1]));
                double cosYaw = Math.cos(Math.toRadians(-rpy[2]));
                double sinRoll = Math.sin(Math.toRadians(-rpy[0]));
                double sinPitch = Math.sin(Math.toRadians(-rpy[1]));
                double sinYaw = Math.sin(Math.toRadians(-rpy[2]));
/*
                ax_global = cosYaw* cosPitch * axNotGrav
                        + (cosYaw * sinPitch * sinRoll + sinYaw * cosRoll) * ayNotGrav
                        + (sinYaw * sinRoll - cosYaw * sinPitch * cosRoll) * azNotGrav;

                ay_global = -sinYaw * cosPitch * axNotGrav
                        + (cosYaw * cosRoll - sinYaw * sinPitch * sinRoll) * ayNotGrav
                        + (sinYaw * sinPitch * cosRoll + cosYaw * sinRoll) * azNotGrav;

                az_global = sinPitch * axNotGrav
                        - cosPitch * sinRoll * ayNotGrav
                        + cosPitch * cosRoll * azNotGrav;
*/

/*
                //X->Yの順番で"ベクトル"を回転させる
                ax_global = cosPitch * axNotGrav
                        + sinPitch * sinRoll * ayNotGrav
                        + sinPitch * cosRoll * azNotGrav;

                ay_global = cosRoll * ayNotGrav
                        - sinRoll * azNotGrav;

                az_global = -sinPitch * axNotGrav
                        + cosPitch * sinRoll * ayNotGrav
                        + cosPitch * cosRoll * azNotGrav;
*/

                //X->Yの順番"で座標系"を回転させる
                ax_global = cosPitch * axNotGrav
                        + sinPitch * sinRoll * ayNotGrav
                        - sinPitch * cosRoll * azNotGrav;

                ay_global = cosRoll * ayNotGrav
                        + sinRoll * azNotGrav;

                az_global = sinPitch * axNotGrav
                        - cosPitch * sinRoll * ayNotGrav
                        + cosPitch * cosRoll * azNotGrav;



                //クォータニオンを使ってセンサ座標系から世界座標系に変換する
/*
                double q0q0 = qX[0] * qX[0];
                double q0q1 = qX[0] * -qX[1];
                double q0q2 = qX[0] * -qX[2];
                double q0q3 = qX[0] * -qX[3];
                double q1q1 = -qX[1] * -qX[1];
                double q1q2 = -qX[1] * -qX[2];
                double q1q3 = -qX[1] * -qX[3];
                double q2q2 = -qX[2] * -qX[2];
                double q2q3 = -qX[2] * -qX[3];
                double q3q3 = -qX[3] * -qX[3];

                axNotGrav = sensorAccel[0] + 9.8f * 2 * (qX[1]*qX[3] - qX[0]*qX[2]);
                ayNotGrav = sensorAccel[1] - 9.8f * 2 * (qX[0]*qX[1] + qX[2]*qX[3]);
                azNotGrav = sensorAccel[2] - 9.8f * (qX[0]*qX[0] - qX[1]*qX[1] - qX[2]*qX[2] + qX[3]*qX[3]);

                //クォータニオンでセンサ座標系から世界座標系に座標変換をおこなう
                ax_global = (q0q0 + q1q1 - q2q2 - q3q3) * axNotGrav + 2d * (q1q2 + q0q3) * ayNotGrav + 2d * (q1q3-q0q2) * azNotGrav;
                ay_global = 2d * (q1q2 -q0q3) * axNotGrav + (q0q0 - q1q1 + q2q2 - q3q3) * ayNotGrav + 2d * (q2q3+q0q1) * azNotGrav;
                az_global = 2d * (q1q3+q0q2) * axNotGrav + 2d * (q2q3 - q0q1) * ayNotGrav + 2d * (q0q0 - q1q1 - q2q2 + q3q3) * azNotGrav;
*/
                CalcDist();

                textViewRoll.setText(String.valueOf(rpy[0]));
                textViewPitch.setText(String.valueOf(rpy[1]));
                textViewYaw.setText(String.valueOf(rpy[2]));

                textViewAxAft.setText(String.valueOf(Math.round(axNotGrav * 100d)/100d));
                textViewAyAft.setText(String.valueOf(Math.round(ayNotGrav * 100d)/100d));
                textViewAzAft.setText(String.valueOf(Math.round(azNotGrav * 100d)/100d));

                textViewAxGlo.setText(String.valueOf(Math.round(ax_global * 100d)/100d));
                textViewAyGlo.setText(String.valueOf(Math.round(ay_global * 100d)/100d));
                textViewAzGlo.setText(String.valueOf(Math.round(az_global * 100d)/100d));

                textViewDist.setText(String.valueOf(Math.round(distance * 1000d) / 1000d));

                time = sampleTime;

                if(recordFrag){
                    pw.print(queueAccTime.longSearchLast() + ",");
                    pw.print(sensorAccel[0] + ",");
                    pw.print(sensorAccel[1] + ",");
                    pw.print(sensorAccel[2] + ",");
                    pw.print(sensorMagnet[0] + ",");
                    pw.print(sensorMagnet[1] + ",");
                    pw.print(sensorMagnet[2] + ",");
                    pw.print(sensorGyro[0] + ",");
                    pw.print(sensorGyro[1] + ",");
                    pw.print(sensorGyro[2] + ",");
                    pw.print(rpy[0] + ",");
                    pw.print(rpy[1] + ",");
                    pw.print(rpy[2] + ",");
                    pw.print(axNotGrav + ",");
                    pw.print(ayNotGrav + ",");
                    pw.print(azNotGrav + ",");
                    pw.print(ax_global + ",");
                    pw.print(ay_global + ",");
                    pw.print(az_global + ",");
                    pw.print(queueVelocityX.searchLast() + ",");
                    pw.print(queueVelocityY.searchLast() + ",");
                    pw.print(queueVelocityZ.searchLast() + ",");
                    pw.print(saveDistX + ",");
                    pw.print(saveDistY + ",");
                    pw.print(saveDistZ + ",");
                    pw.print(distance);
                    pw.println();
                }

                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                //Log.i("MagnetTime",String.valueOf(event.timestamp/1000000000d));

                sensorMagnet[0] = event.values[0] ;
                sensorMagnet[1] = event.values[1] ;
                sensorMagnet[2] = event.values[2] ;

                sampleTimeMagnet = event.timestamp;
                sampleTime = event.timestamp;

                textViewMx.setText(String.valueOf(sensorMagnet[0]));
                textViewMy.setText(String.valueOf(sensorMagnet[1]));
                textViewMz.setText(String.valueOf(sensorMagnet[2]));

                break;

            case Sensor.TYPE_GYROSCOPE:
                //Log.i("GyroTime",String.valueOf(event.timestamp/1000000000d));

                sensorGyro[0] = event.values[0];
                sensorGyro[1] = event.values[1];
                sensorGyro[2] = event.values[2];

                sampleTimeGyro = event.timestamp;
                sampleTime = event.timestamp;

                textViewGx.setText(String.valueOf(sensorGyro[0]));
                textViewGy.setText(String.valueOf(sensorGyro[1]));
                textViewGz.setText(String.valueOf(sensorGyro[2]));

                break;
        }

    }

    private void CalcDist(){
        queueAccelX.add(ax_global);
        queueAccelY.add(ay_global);
        queueAccelZ.add(az_global);
        queueAccTime.add(sampleTime-initTime);

        switch (queueSize){
            case 2:
                //台形則
                if(queueAccelX.size()==2 && queueAccelY.size()==2 && queueAccelZ.size()==2){
                    saveVeloX = integral.daikei(queueAccelX, queueAccTime);
                    saveVeloY = integral.daikei(queueAccelY, queueAccTime);
                    saveVeloZ = integral.daikei(queueAccelZ, queueAccTime);
                    queueVelocityX.add(saveVeloX + queueVelocityX.searchLast());
                    queueVelocityY.add(saveVeloY + queueVelocityY.searchLast());
                    queueVelocityZ.add(saveVeloZ + queueVelocityZ.searchLast());
                    queueVeloTime.add(sampleTime - initTime);
                    queueAccelX.deleteHead(1);
                    queueAccelY.deleteHead(1);
                    queueAccelZ.deleteHead(1);
                    queueAccTime.deleteHead(1);
                }
                if (queueVelocityX.size()==2 && queueVelocityY.size()==2 && queueVelocityZ.size()==2){
                    saveDistX += integral.daikei(queueVelocityX,queueVeloTime);
                    saveDistY += integral.daikei(queueVelocityY,queueVeloTime);
                    saveDistZ += integral.daikei(queueVelocityZ,queueVeloTime);
                    distance = Math.sqrt(Math.pow(saveDistX, 2) + Math.pow(saveDistY, 2) + Math.pow(saveDistZ, 2));
                    queueVelocityX.deleteHead(1);
                    queueVelocityY.deleteHead(1);
                    queueVelocityZ.deleteHead(1);
                    queueVeloTime.deleteHead(1);
                }
                break;
            case 3:
                //3点のシンプソン(シンプソン則)
                if(queueAccelX.size()==3 && queueAccelY.size()==3 && queueAccelZ.size()==3){
                    saveVeloX = integral.simpson3point(queueAccelX, queueAccTime);
                    saveVeloY = integral.simpson3point(queueAccelY, queueAccTime);
                    saveVeloZ = integral.simpson3point(queueAccelZ, queueAccTime);
                    queueVelocityX.add(saveVeloX + queueVelocityX.searchLast());
                    queueVelocityY.add(saveVeloY + queueVelocityY.searchLast());
                    queueVelocityZ.add(saveVeloZ + queueVelocityZ.searchLast());
                    queueVeloTime.add(sampleTime - initTime);
                    queueAccelX.deleteHead(2);
                    queueAccelY.deleteHead(2);
                    queueAccelZ.deleteHead(2);
                    queueAccTime.deleteHead(2);

                    if (queueVelocityX.size()==3 && queueVelocityY.size()==3 && queueVelocityZ.size()==3){
                        saveDistX += integral.simpson3point(queueVelocityX,queueVeloTime);
                        saveDistY += integral.simpson3point(queueVelocityY,queueVeloTime);
                        saveDistZ += integral.simpson3point(queueVelocityZ,queueVeloTime);
                        distance = Math.sqrt(Math.pow(saveDistX, 2) + Math.pow(saveDistY, 2) + Math.pow(saveDistZ, 2));
                        queueVelocityX.deleteHead(2);
                        queueVelocityY.deleteHead(2);
                        queueVelocityZ.deleteHead(2);
                        queueVeloTime.deleteHead(2);
                    }
                }
                break;
            case 4:
                //4点のシンプソン（シンプソン3/8則）
                if(queueAccelX.size()==4 && queueAccelY.size()==4 && queueAccelZ.size()==4){
                    saveVeloX = integral.simpson4point(queueAccelX, queueAccTime);
                    saveVeloY = integral.simpson4point(queueAccelY, queueAccTime);
                    saveVeloZ = integral.simpson4point(queueAccelZ, queueAccTime);
                    queueVelocityX.add(saveVeloX + queueVelocityX.searchLast());
                    queueVelocityY.add(saveVeloY + queueVelocityY.searchLast());
                    queueVelocityZ.add(saveVeloZ + queueVelocityZ.searchLast());
                    queueVeloTime.add(sampleTime - initTime);
                    queueAccelX.deleteHead(3);
                    queueAccelY.deleteHead(3);
                    queueAccelZ.deleteHead(3);
                    queueAccTime.deleteHead(3);
                }
                if (queueVelocityX.size()==4 && queueVelocityY.size()==4 && queueVelocityZ.size()==4){
                    saveDistX += integral.simpson4point(queueVelocityX,queueVeloTime);
                    saveDistY += integral.simpson4point(queueVelocityY,queueVeloTime);
                    saveDistZ += integral.simpson4point(queueVelocityZ,queueVeloTime);
                    distance = Math.sqrt(Math.pow(saveDistX, 2) + Math.pow(saveDistY, 2) + Math.pow(saveDistZ, 2));
                    queueVelocityX.deleteHead(3);
                    queueVelocityY.deleteHead(3);
                    queueVelocityZ.deleteHead(3);
                    queueVeloTime.deleteHead(3);
                }
                break;
            case 5:
                /*
                //ブール則（5点）
                if(queueAccel.size()==5){
                    saveVelo = integral.bool(queueAccel, queueTime);
                    queueVelocity.add(saveVelo + queueVelocity.element());
                }
                if (queueVelocity.size()==5){
                    saveDist = integral.bool(queueVelocity,queueTime);
                    queueDist.add(saveDist + queueDist.element());
                }
                break;
                 */
        }

/*
        //台形則
        if(queueAccelX.size()==2 && queueAccelY.size()==2 && queueAccelZ.size()==3){
            saveVeloX = integral.daikei(queueAccelX, queueAccTime);
            saveVeloY = integral.daikei(queueAccelY, queueAccTime);
            saveVeloZ = integral.daikei(queueAccelZ, queueAccTime);
            queueVelocityX.add(saveVeloX + queueVelocityX.searchLast());
            queueVelocityY.add(saveVeloY + queueVelocityY.searchLast());
            queueVelocityZ.add(saveVeloZ + queueVelocityZ.searchLast());
            queueVeloTime.add(sampleTime - initTime);
            queueAccelX.deleteHead(1);
            queueAccelY.deleteHead(1);
            queueAccelZ.deleteHead(1);
            queueAccTime.deleteHead(1);
        }
        if (queueVelocityX.size()==2 && queueVelocityY.size()==2 && queueVelocityZ.size()==2){
            saveDistX += integral.daikei(queueVelocityX,queueVeloTime);
            saveDistY += integral.daikei(queueVelocityY,queueVeloTime);
            saveDistZ += integral.daikei(queueVelocityZ,queueVeloTime);
            distance = Math.sqrt(Math.pow(saveDistX, 2) + Math.pow(saveDistY, 2) + Math.pow(saveDistZ, 2));
            queueVelocityX.deleteHead(1);
            queueVelocityY.deleteHead(1);
            queueVelocityZ.deleteHead(1);
            queueVeloTime.deleteHead(1);
        }
*/
/*
        //3点のシンプソン(シンプソン則)
        if(queueAccelX.size()==3 && queueAccelY.size()==3 && queueAccelZ.size()==3){
            saveVeloX = integral.simpson3point(queueAccelX, queueAccTime);
            saveVeloY = integral.simpson3point(queueAccelY, queueAccTime);
            saveVeloZ = integral.simpson3point(queueAccelZ, queueAccTime);
            queueVelocityX.add(saveVeloX + queueVelocityX.searchLast());
            queueVelocityY.add(saveVeloY + queueVelocityY.searchLast());
            queueVelocityZ.add(saveVeloZ + queueVelocityZ.searchLast());
            queueVeloTime.add(sampleTime - initTime);
            queueAccelX.deleteHead(2);
            queueAccelY.deleteHead(2);
            queueAccelZ.deleteHead(2);
            queueAccTime.deleteHead(2);

            if (queueVelocityX.size()==3 && queueVelocityY.size()==3 && queueVelocityZ.size()==3){
                saveDistX += integral.simpson3point(queueVelocityX,queueVeloTime);
                saveDistY += integral.simpson3point(queueVelocityY,queueVeloTime);
                saveDistZ += integral.simpson3point(queueVelocityZ,queueVeloTime);
                distance = Math.sqrt(Math.pow(saveDistX, 2) + Math.pow(saveDistY, 2) + Math.pow(saveDistZ, 2));
                queueVelocityX.deleteHead(2);
                queueVelocityY.deleteHead(2);
                queueVelocityZ.deleteHead(2);
                queueVeloTime.deleteHead(2);
            }
        }
*/
/*
        //4点のシンプソン（シンプソン3/8則）
        if(queueAccelX.size()==4 && queueAccelY.size()==4 && queueAccelZ.size()==4){
            saveVeloX = integral.simpson4point(queueAccelX, queueAccTime);
            saveVeloY = integral.simpson4point(queueAccelY, queueAccTime);
            saveVeloZ = integral.simpson4point(queueAccelZ, queueAccTime);
            queueVelocityX.add(saveVeloX + queueVelocityX.searchLast());
            queueVelocityY.add(saveVeloY + queueVelocityY.searchLast());
            queueVelocityZ.add(saveVeloZ + queueVelocityZ.searchLast());
            queueVeloTime.add(sampleTime - initTime);
            queueAccelX.deleteHead(3);
            queueAccelY.deleteHead(3);
            queueAccelZ.deleteHead(3);
            queueAccTime.deleteHead(3);
        }
        if (queueVelocityX.size()==4 && queueVelocityY.size()==4 && queueVelocityZ.size()==4){
            saveDistX += integral.simpson4point(queueVelocityX,queueVeloTime);
            saveDistY += integral.simpson4point(queueVelocityY,queueVeloTime);
            saveDistZ += integral.simpson4point(queueVelocityZ,queueVeloTime);
            distance = Math.sqrt(Math.pow(saveDistX, 2) + Math.pow(saveDistY, 2) + Math.pow(saveDistZ, 2));
            queueVelocityX.deleteHead(3);
            queueVelocityY.deleteHead(3);
            queueVelocityZ.deleteHead(3);
            queueVeloTime.deleteHead(3);
        }
*/
/*
        //ブール則（5点）
        if(queueAccel.size()==5){
            saveVelo = integral.bool(queueAccel, queueTime);
            queueVelocity.add(saveVelo + queueVelocity.element());
        }
        if (queueVelocity.size()==5){
            saveDist = integral.bool(queueVelocity,queueTime);
            queueDist.add(saveDist + queueDist.element());
        }
*/
    }


    double ax_global, ay_global, az_global;
    double axNotGrav,ayNotGrav,azNotGrav;


    long timeCaliculateStart, timeCaliculateEnd;
/*
    private void StartThread(){
        runnable = new Runnable() {
            @Override
            public void run() {
                timeCaliculateStart = SystemClock.elapsedRealtimeNanos();

                textViewRoll.setText(String.valueOf(rpy[0]));
                textViewPitch.setText(String.valueOf(rpy[1]));
                textViewYaw.setText(String.valueOf(rpy[2]));

                //double accelGlobal [] = {ax_global, ay_global, az_global};

                //textViewAxAft.setText(String.valueOf(Math.round(axNotGrav * 100d)/100d));
                //textViewAyAft.setText(String.valueOf(Math.round(ayNotGrav * 100d)/100d));
                //textViewAzAft.setText(String.valueOf(Math.round(azNotGrav * 100d)/100d));

                textViewAxGlo.setText(String.valueOf(Math.round(ax_global * 100d)/100d));
                textViewAyGlo.setText(String.valueOf(Math.round(ay_global * 100d)/100d));
                textViewAzGlo.setText(String.valueOf(Math.round(az_global * 100d)/100d));

                textViewDist.setText(String.valueOf(Math.round(distance * 1000d) / 1000d));

                handler.postDelayed(this, 35);

            }
        };
        handler.post(runnable);
    }
*/

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //センサー精度の変更を行うときに利用するメソッド
    }

    public float diff_time(long time, long bef_time){
        return (time - bef_time) / 1000000000F;
    }

    public void resetVariable(){
        queueAccelX.clear();
        queueAccelY.clear();
        queueAccelZ.clear();
        queueAccTime.clear();
        queueVelocityX.velocityClear();
        queueVelocityY.velocityClear();
        queueVelocityZ.velocityClear();
        queueVeloTime.clear();
        distance=0;

        saveVeloX=0;
        saveVeloY=0;
        saveVeloZ=0;
        saveDistX=0;
        saveDistY=0;
        saveDistZ=0;
    }


}