package com.example.MGHFver7;

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
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SensorEventListener, OnClickListener{

    private SensorManager sensorManager;
    private Sensor accel,mag,gyro;
    private TextView textViewAx, textViewAy, textViewAz;
    private TextView textViewMx, textViewMy, textViewMz;
    private TextView textViewGx, textViewGy, textViewGz;
    private TextView textViewQx, textViewQy, textViewQz, textViewQw, textViewQ;
    private TextView textViewAxAft, textViewAyAft,textViewAzAft;
    private TextView textViewAxGlo, textViewAyGlo,textViewAzGlo;
    private TextView textViewDist;
    private Button recordStartButton, recordStopButton, resetButton;

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
    private double saveVeloX=0;
    private double saveVeloY=0;
    private double saveVeloZ=0;
    private double saveDistX=0;
    private double saveDistY=0;
    private double saveDistZ=0;
    private double[] accel_global;

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
            "daikei",
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

        textViewQw = findViewById(R.id.text_view_qW);
        textViewQx = findViewById(R.id.text_view_qX);
        textViewQy = findViewById(R.id.text_view_qY);
        textViewQz = findViewById(R.id.text_view_qZ);
        textViewQ = findViewById(R.id.text_view_Q);

        textViewAxAft = findViewById(R.id.text_view_Ax_aft);
        textViewAyAft = findViewById(R.id.text_view_Ay_aft);
        textViewAzAft = findViewById(R.id.text_view_Az_aft);

        textViewAxGlo = findViewById(R.id.text_view_Ax_global);
        textViewAyGlo = findViewById(R.id.text_view_Ay_global);
        textViewAzGlo = findViewById(R.id.text_view_Az_global);

        textViewDist = findViewById(R.id.distance);

        recordStartButton = (Button)findViewById(R.id.recordStartButton);
        recordStopButton = (Button)findViewById(R.id.recordStopButton);
        resetButton = (Button)findViewById(R.id.resetButton);
        recordStartButton.setOnClickListener(this);
        recordStopButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);

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



        time = SystemClock.elapsedRealtimeNanos();
        initTime = time;
        Start = time;

        resetVariable();

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
                    String header = "timeStamp" + "," + "accelX" + "," + "accelY" + "," + "accelZ"
                            + "," + "magnetX" + "," + "magnetY" + "," + "magnetZ"
                            + "," + "gyroX" + "," + "gyroY" + "," + "gyroZ"
                            + "," + "qw" + "," + "qx" + "," + "qy" + "," + "qz"
                            + "," + "NotGravX" + "," + "NotGravY" + "," + "NotGravZ"
                            + "," + "globalX" + "," + "globalY" + "," + "globalZ"
                            + "," + "velocityX" + "," + "velocityY" + "," + "velocityZ"
                            + "," + "distanceX" + "," + "distanceY" + "," + "distanceZ" + "," + "distance";
                    pw.println(header);

                    String data = queueAccTime.longSearchLast() + ","
                            + sensorAccel[0] + "," + sensorAccel[1] + "," + sensorAccel[2] + ","
                            + sensorMagnet[0] + "," + sensorMagnet[1] + "," + sensorMagnet[2] + ","
                            + sensorGyro[0] + "," + sensorGyro[1] + "," + sensorGyro[2] + ","
                            + qX[0] + "," + qX[1] + "," + qX[2] + "," + qX[3] + ","
                            + axNotGrav + "," + ayNotGrav + "," + azNotGrav + ","
                            + ax_global + "," + ay_global + "," + az_global + ","
                            + queueVelocityX.searchLast() + "," + queueVelocityY.searchLast() + "," + queueVelocityZ.searchLast() + ","
                            + saveDistX + "," + saveDistY + "," + saveDistZ + "," + distance;

                    pw.println(data);

                }catch (IOException e){
                    e.printStackTrace();
                }

                Toast toastStart = Toast.makeText(getApplicationContext(), "Start Record!!", Toast.LENGTH_SHORT);
                toastStart.show();


                break;

            case R.id.recordStopButton:

                recordFrag = false;
                pw.close();
                //sensorManager.unregisterListener(this);
                Toast toastStop = Toast.makeText(getApplicationContext(), "Stop Record", Toast.LENGTH_SHORT);
                toastStop.show();

                break;

            case R.id.resetButton:
                initTime = SystemClock.elapsedRealtimeNanos();

                sensorManager.unregisterListener(this);
                sensorManager.registerListener(this, accel, 10000);
                sensorManager.registerListener(this, mag, 10000);
                sensorManager.registerListener(this, gyro, 10000);

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
                //rpy = orientation.madgwickFilter(sensorAccel, sensorGyro, sensorMagnet, 1.0F / dif);
                //qX = orientation.madgwickFilter(sensorAccel, sensorGyro, sensorMagnet, 1.0F / dif);

                //６軸センサ
                //rpy = orientation.madgwickFilterNoMag(sensorAccel, sensorGyro, 1.0F / dif);
                qX = orientation.madgwickFilterNoMag(sensorAccel, sensorGyro, 1.0F / dif);

                //端末座標系を世界座標系に変換→Z軸から重力加速度を減算
                double[] quaternion_inv = new double[]{qX[0], -qX[1], -qX[2], -qX[3]};
                double[] quaternion = new double[]{qX[0], qX[1], qX[2], qX[3]};
                double[] left = QuaternionMultiplication(quaternion, new double[]{0, sensorAccel[0], sensorAccel[1], sensorAccel[2]});
                accel_global = QuaternionMultiplication(left, quaternion_inv);

                ax_global = accel_global[1] ;
                ay_global = accel_global[2] ;
                az_global = accel_global[3] - 9.80665;

                //left = QuaternionMultiplication(quaternion_inv , new double[]{0d, 0d, 0d, -9.80665d});
                //double[] gravity_body = QuaternionMultiplication(left, quaternion);

                //axNotGrav = sensorAccel[0] + gravity_body[1];
                //ayNotGrav = sensorAccel[1] + gravity_body[2];
                //azNotGrav = sensorAccel[2] + gravity_body[3];



                //クォータニオンでセンサ座標系から世界座標系に座標変換をおこなう
                /*
                ax_global = (q0q0 + q1q1 - q2q2 - q3q3) * axNotGrav + 2d * (q1q2 + q0q3) * ayNotGrav + 2d * (q1q3 - q0q2) * azNotGrav;
                ay_global = 2d * (q1q2 - q0q3) * axNotGrav + (q0q0 - q1q1 + q2q2 - q3q3) * ayNotGrav + 2d * (q2q3 + q0q1) * azNotGrav;
                az_global = 2d * (q1q3 + q0q2) * axNotGrav + 2d * (q2q3 - q0q1) * ayNotGrav + 2d * (q0q0 - q1q1 - q2q2 + q3q3) * azNotGrav;
                */
                CalcDist();

                time = sampleTime;

                if(recordFrag){
                    String data = queueAccTime.longSearchLast() + ","
                            + sensorAccel[0] + "," + sensorAccel[1] + "," + sensorAccel[2] + ","
                            + sensorMagnet[0] + "," + sensorMagnet[1] + "," + sensorMagnet[2] + ","
                            + sensorGyro[0] + "," + sensorGyro[1] + "," + sensorGyro[2] + ","
                            + qX[0] + "," + qX[1] + "," + qX[2] + "," + qX[3] + ","
                            + axNotGrav + "," + ayNotGrav + "," + azNotGrav + ","
                            + ax_global + "," + ay_global + "," + az_global + ","
                            + queueVelocityX.searchLast() + "," + queueVelocityY.searchLast() + "," + queueVelocityZ.searchLast() + ","
                            + saveDistX + "," + saveDistY + "," + saveDistZ + "," + distance;

                    pw.println(data);
                }

                textViewQw.setText(String.valueOf(Math.round(qX[0] * 1000d)/1000d));
                textViewQx.setText(String.valueOf(Math.round(qX[1] * 1000d)/1000d));
                textViewQy.setText(String.valueOf(Math.round(qX[2] * 1000d)/1000d));
                textViewQz.setText(String.valueOf(Math.round(qX[3] * 1000d)/1000d));
                textViewQ.setText(String.valueOf(Math.round((Math.sqrt(qX[0]*qX[0] + qX[1]*qX[1] + qX[2]*qX[2] + qX[3]*qX[3])) * 1000d)/1000d));


                textViewAxAft.setText(String.valueOf(Math.round(axNotGrav * 100d)/100d));
                textViewAyAft.setText(String.valueOf(Math.round(ayNotGrav * 100d)/100d));
                textViewAzAft.setText(String.valueOf(Math.round(azNotGrav * 100d)/100d));

                textViewAxGlo.setText(String.valueOf(Math.round(ax_global * 100d)/100d));
                textViewAyGlo.setText(String.valueOf(Math.round(ay_global * 100d)/100d));
                textViewAzGlo.setText(String.valueOf(Math.round(az_global * 100d)/100d));

                textViewDist.setText(String.valueOf(Math.round(distance * 1000d) / 1000d));

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
                    //distance = Math.sqrt(Math.pow(saveDistX, 2) + Math.pow(saveDistY, 2) + Math.pow(saveDistZ, 2));
                    distance = Math.sqrt(Math.pow(saveDistX, 2) + Math.pow(saveDistY, 2));
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
                        //distance = Math.sqrt(Math.pow(saveDistX, 2) + Math.pow(saveDistY, 2) + Math.pow(saveDistZ, 2));
                        distance = Math.sqrt(Math.pow(saveDistX, 2) + Math.pow(saveDistY, 2));
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
                    //distance = Math.sqrt(Math.pow(saveDistX, 2) + Math.pow(saveDistY, 2) + Math.pow(saveDistZ, 2));
                    distance = Math.sqrt(Math.pow(saveDistX, 2) + Math.pow(saveDistY, 2));
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

    }


    double ax_global;
    double ay_global;
    double az_global;
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

        //Orientation.q0 = 1;
        //Orientation.q1 = 0;
        //Orientation.q2 = 0;
        //Orientation.q3 = 0;

        //qX[0]=1;
        //qX[1]=0;
        //qX[2]=0;
        //qX[3]=0;
    }

    public double[] QuaternionMultiplication(double[] left, double[] right) {
        double d0, d1, d2, d3;

        d0 = left[0] * right[0];
        d1 = -left[1] * right[1];
        d2 = -left[2] * right[2];
        d3 = -left[3] * right[3];
        double w = d0 + d1 + d2 + d3;

        d0 = left[0] * right[1];
        d1 = left[1] * right[0];
        d2 = left[2] * right[3];
        d3 = -left[3] * right[2];
        double x = d0 + d1 + d2 + d3;

        d0 = left[0] * right[2];
        d1 = -left[1] * right[3];
        d2 = left[2] * right[0];
        d3 = left[3] * right[1];
        double y = d0 + d1 + d2 + d3;

        d0 = left[0] * right[3];
        d1 = left[1] * right[2];
        d2 = -left[2] * right[1];
        d3 = left[3] * right[0];
        double z = d0 + d1 + d2 + d3;

        double[] result = {w,x,y,z};
        return result;
    }

}