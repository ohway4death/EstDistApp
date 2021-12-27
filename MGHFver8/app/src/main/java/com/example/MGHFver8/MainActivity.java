package com.example.MGHFver8;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
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
    private TextView textViewStdVarX, textViewStdVarY, textViewMeanX, textViewMeanY;
    private TextView textViewAxGlo, textViewAyGlo,textViewAzGlo;
    private TextView textViewDist;
    private TextView textViewBeta;
    private Button recordStartButton, recordStopButton, resetButton;

    private final Orientation orientation = new Orientation();
    private final Integral integral = new Integral();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private long time, initTime;

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
    FixedSizeQueue queueTheresholdX = new FixedSizeQueue(25);
    FixedSizeQueue queueTheresholdY = new FixedSizeQueue(25);
    FixedSizeQueue queueTheresholdZ = new FixedSizeQueue(25);
    FixedSizeQueue queueMeanX = new FixedSizeQueue(5);
    FixedSizeQueue queueMeanY = new FixedSizeQueue(5);
    FixedSizeQueue queueMeanZ = new FixedSizeQueue(5);
    private double distance=0;
    private double saveVeloX=0;
    private double saveVeloY=0;
    private double saveVeloZ=0;
    private double saveDistX=0;
    private double saveDistY=0;
    private double saveDistZ=0;
    private double[] accel_global;
    private FixedSizeQueue averageQueueX = new FixedSizeQueue(100);
    private FixedSizeQueue averageQueueY = new FixedSizeQueue(100);
    private FixedSizeQueue averageQueueZ = new FixedSizeQueue(100);
    private double gravity = 9.80665;

    private final float aveAx = 0;
    private final float aveAy = 0;
    private final float aveAz = 0;
    ArrayList<Float> accelXList = new ArrayList<>();
    ArrayList<Float> accelYList = new ArrayList<>();
    ArrayList<Float> accelZList = new ArrayList<>();

    ArrayList<Float> gyroXList = new ArrayList<>();
    ArrayList<Float> gyroYList = new ArrayList<>();
    ArrayList<Float> gyroZList = new ArrayList<>();

    private double[] iir = {0,0,0,0,0,0};
    final double fc = 15f;
    FixedSizeQueue iirInQueueAccelX = new FixedSizeQueue(3);
    FixedSizeQueue iirInQueueAccelY = new FixedSizeQueue(3);
    FixedSizeQueue iirInQueueAccelZ = new FixedSizeQueue(3);

    FixedSizeQueue iirOutQueueAccelX = new FixedSizeQueue(3);
    FixedSizeQueue iirOutQueueAccelY = new FixedSizeQueue(3);
    FixedSizeQueue iirOutQueueAccelZ = new FixedSizeQueue(3);


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

    /*
    Float[] methodSpinnerItems = {
            0.01f,
            0.02f,
            0.03f,
            0.04f,
            0.05f
    };
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //アプリを起動した際に実行される部分
        //Activityが生成される
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //センサーサービスへの参照を取得する，SensorManagerクラスのインスタンスを作成する．
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        setText();

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
        calibrationStartTime = time;
        //フィルタの係数を計算
        iir = IIR(fc);
        iirOutQueueAccelX.allZero();
        iirOutQueueAccelY.allZero();
        iirOutQueueAccelZ.allZero();
        iirInQueueAccelX.allZero();
        iirInQueueAccelY.allZero();
        iirInQueueAccelZ.allZero();

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
                file = new File(context.getFilesDir(), choiceMethodName + "_" + choiceFileName);
                try{
                    //引数はファイル名と書き込まれたデータを追加するかどうか決める、trueならファイルの最後に追記していく。
                    fw = new FileWriter(file);
                    pw = new PrintWriter(new BufferedWriter(fw));

                    //ヘッダーを作成する
                    String header = "timeStamp" + "," + "accelX" + "," + "accelY" + "," + "accelZ"
                            + "," + "magnetX" + "," + "magnetY" + "," + "magnetZ"
                            + "," + "gyroX" + "," + "gyroY" + "," + "gyroZ"
                            + "," + "qw" + "," + "qx" + "," + "qy" + "," + "qz"
                            + "," + "GravityGlobalX" + "," + "GravityGlobalY" + "," + "GravityGlobalZ" + "," + "Gravity"
                            + "," + "NotGravityGlobalX" + "," + "NotGravityGlobalY" + "," + "NotGravityGlobalZ"
                            + "," + "velocityX" + "," + "velocityY" + "," + "velocityZ"
                            + "," + "distanceX" + "," + "distanceY" + "," + "distanceZ" + "," + "distance";
                    pw.println(header);

                    String data = queueAccTime.longSearchLast() + ","
                            + sensorAccel[0] + "," + sensorAccel[1] + "," + sensorAccel[2] + ","
                            + sensorMagnet[0] + "," + sensorMagnet[1] + "," + sensorMagnet[2] + ","
                            + sensorGyro[0] + "," + sensorGyro[1] + "," + sensorGyro[2] + ","
                            + qX[0] + "," + qX[1] + "," + qX[2] + "," + qX[3] + ","
                            + accel_global[0] + "," + accel_global[1] + "," + accel_global[2] + "," + gravity + ","
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

        //textViewVendor.setText(accel.getVendor() + accel.getMaximumRange() );
    }

    @Override
    protected void onPause() {
        //別のアクティビティが開始されるときに実行される部分
        super.onPause();
        //Listenerの解除
        sensorManager.unregisterListener(this);
    }

    float[] sensorAccel = {0, 0, 0};
    float[] sensorMagnet = {0, 0, 0};
    float[] sensorGyro = {0, 0, 0};

    float[] sensorAccelBef = {0, 0, 0};

    long sampleTimeAccel;
    long sampleTimeMagnet;
    long sampleTimeGyro;


    long sampleTime;

    float[] rpy = {0,0,0};
    float[] qX = {0,0,0,0};

    int a = 0;

    float[] LPF = {0,0,0};
    float[] accelAftFilter = {0, 0, 0};

    private int count = 0;
    private float gyroX_ave = 0;
    private float gyroY_ave = 0;
    private float gyroZ_ave = 0;
    private float gyroX_max = 0;
    private float gyroY_max = 0;
    private float gyroZ_max = 0;
    private float calibrationStartTime;
    private boolean calibrationFlag = true;

    double ax_global;
    double ay_global;
    double az_global;

    @Override
    public void onSensorChanged(SensorEvent event){
        //ここは頻繁に呼び出されるので処理は簡単にする
        //計算はここの外側で行う
        //センサーのイベントを処理する部分

        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:

                textViewAx.setText(String.valueOf(event.values[0]));
                textViewAy.setText(String.valueOf(event.values[1]));
                textViewAz.setText(String.valueOf(event.values[2]));

                sensorAccel[0] = event.values[0];
                sensorAccel[1] = event.values[1];
                sensorAccel[2] = event.values[2];

                /*
                //iirフィルタ
                iirInQueueAccelX.add(event.values[0]);
                iirInQueueAccelY.add(event.values[1]);
                iirInQueueAccelZ.add(event.values[2]);
                sensorAccel[0] = (float) (iirInQueueAccelX.queue[2] * iir[0] + iirInQueueAccelX.queue[1] * iir[1] + iirInQueueAccelX.queue[0] * iir[2]
                        - iirOutQueueAccelX.queue[2] * iir[4] - iirOutQueueAccelX.queue[1] * iir[5]);
                sensorAccel[1] = (float) (iirInQueueAccelY.queue[2] * iir[0] + iirInQueueAccelY.queue[1] * iir[1] + iirInQueueAccelY.queue[0] * iir[2]
                        - iirOutQueueAccelY.queue[2] * iir[4] - iirOutQueueAccelY.queue[1] * iir[5]);
                sensorAccel[2] = (float) (iirInQueueAccelZ.queue[2] * iir[0] + iirInQueueAccelZ.queue[1] * iir[1] + iirInQueueAccelZ.queue[0] * iir[2]
                        - iirOutQueueAccelZ.queue[2] * iir[4] - iirOutQueueAccelZ.queue[1] * iir[5]);
                iirOutQueueAccelX.add(sensorAccel[0]);
                iirOutQueueAccelY.add(sensorAccel[1]);
                iirOutQueueAccelZ.add(sensorAccel[2]);
                */

                sampleTimeAccel = event.timestamp;
                sampleTime = event.timestamp;

                float dif = diff_time(sampleTime-initTime, time-initTime);

                if (!calibrationFlag){
                    //9軸センサ
                    //rpy = orientation.madgwickFilter(sensorAccel, sensorGyro, sensorMagnet, 1.0F / dif);
                    //qX = orientation.madgwickFilter(sensorAccel, sensorGyro, sensorMagnet, 1.0F / dif);

                    //６軸センサ
                    //rpy = orientation.madgwickFilterNoMag(sensorAccel, sensorGyro, 1.0F / dif);
                    qX = orientation.madgwickFilterNoMag(sensorAccel, sensorGyro, 1.0F / dif);

                    //LowPassFilter
                    //float a = 0.76f;
                    //sensorAccel[0] = a * sensorAccel[0] + (1-a) * event.values[0];
                    //sensorAccel[1] = a * sensorAccel[1] + (1-a) * event.values[1];
                    //sensorAccel[2] = a * sensorAccel[2] + (1-a) * event.values[2];

                    //LPF+HPF
                    /*
                    float a = 0.97f;
                    LPF[0] = a * LPF[0] + (1-a) * event.values[0];
                    LPF[1] = a * LPF[1] + (1-a) * event.values[1];
                    LPF[2] = a * LPF[2] + (1-a) * event.values[2];
                    accelAftFilter[0] = event.values[0] - LPF[0];
                    accelAftFilter[1] = event.values[1] - LPF[1];
                    accelAftFilter[2] = event.values[2] - LPF[2];
                    */

                    //端末座標系を世界座標系に変換→Z軸から重力加速度を減算
                    double[] quaternion_inv = new double[]{qX[0], -qX[1], -qX[2], -qX[3]};
                    double[] quaternion = new double[]{qX[0], qX[1], qX[2], qX[3]};
                    //double[] left = QuaternionMultiplication(quaternion, new double[]{0, accelAftFilter[0], accelAftFilter[1], accelAftFilter[2]});
                    //座標系を回転させる（共役）＋逆回転させるため（共役），左からクォータニオンをそのままかけている．
                    double[] left = QuaternionMultiplication(quaternion, new double[]{0, sensorAccel[0], sensorAccel[1], sensorAccel[2]});
                    accel_global = QuaternionMultiplication(left, quaternion_inv);

                    //移動平均フィルタ
                    /*
                    queueMeanX.add(accel_global[1]);
                    queueMeanY.add(accel_global[2]);
                    queueMeanZ.add(accel_global[3]);
                    if (queueMeanX.size()>=5 && queueMeanY.size()>=5 && queueMeanZ.size()>=5){
                        double[] ans  = MeanFilter();
                        ax_global = ans[0] ;
                        ay_global = ans[1] ;
                        az_global = ans[2] ;
                    }
                    */

                    //静止状態のときは加速度を0にする。

                    queueTheresholdX.add(accel_global[1]);
                    queueTheresholdY.add(accel_global[2]);
                    queueTheresholdZ.add(accel_global[3]);
                    if (queueTheresholdX.size()>=25 || queueTheresholdY.size()>=25 || queueTheresholdZ.size()>=25){
                        AdjustGravity4();
                        CalcDist();
                    }

/*
                    ax_global = accel_global[1] ;
                    ay_global = accel_global[2] ;
                    az_global = accel_global[3] - gravity;

                    CalcDist();
*/


                }

                time = sampleTime;

                if(recordFrag){
                    String data = queueAccTime.longSearchLast() + ","
                            + sensorAccel[0] + "," + sensorAccel[1] + "," + sensorAccel[2] + ","
                            + sensorMagnet[0] + "," + sensorMagnet[1] + "," + sensorMagnet[2] + ","
                            + sensorGyro[0] + "," + sensorGyro[1] + "," + sensorGyro[2] + ","
                            + qX[0] + "," + qX[1] + "," + qX[2] + "," + qX[3] + ","
                            + accel_global[1] + "," + accel_global[2] + "," + accel_global[3] + "," + gravity + ","
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

                textViewStdVarX.setText(String.valueOf(Math.round(std_varX * 1000d)/1000d));
                textViewStdVarY.setText(String.valueOf(Math.round(std_varY * 1000d)/1000d));
                textViewMeanX.setText(String.valueOf(Math.round(aveX * 1000d)/1000d));
                textViewMeanY.setText(String.valueOf(Math.round(aveY * 1000d)/1000d));

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

                //textViewMx.setText(String.valueOf(sensorMagnet[0]));
                //textViewMy.setText(String.valueOf(sensorMagnet[1]));
                //textViewMz.setText(String.valueOf(sensorMagnet[2]));

                break;

            case Sensor.TYPE_GYROSCOPE:
                //Log.i("GyroTime",String.valueOf(event.timestamp/1000000000d));

                sensorGyro[0] = event.values[0];
                sensorGyro[1] = event.values[1];
                sensorGyro[2] = event.values[2];

                if((sampleTimeGyro - calibrationStartTime)/1000000000f < 60){
                    count++;

                    gyroX_ave += sensorGyro[0];
                    gyroY_ave += sensorGyro[1];
                    gyroZ_ave += sensorGyro[2];

                    gyroX_ave /= count;
                    gyroY_ave /= count;
                    gyroZ_ave /= count;

                    gyroX_max = Math.max(gyroX_max, Math.abs(sensorGyro[0]));
                    gyroY_max = Math.max(gyroY_max, Math.abs(sensorGyro[1]));
                    gyroZ_max = Math.max(gyroZ_max, Math.abs(sensorGyro[2]));

                    setContentView(R.layout.calibration);

                }else if((sampleTimeGyro - calibrationStartTime)/1000000000f > 60 && calibrationFlag){
                    Orientation.beta = (float) (Math.max(gyroX_max-gyroX_ave, Math.max(gyroY_max-gyroY_ave, gyroZ_max-gyroZ_ave)) * Math.sqrt(3f/4f));
                    calibrationFlag = false;
                    setContentView(R.layout.activity_main);
                    setText();
                    textViewBeta.setText("beta = " + Orientation.beta);
                }

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
                    if(MotionFlag){
                        queueVelocityX.add(saveVeloX + queueVelocityX.searchLast());
                        queueVelocityY.add(saveVeloY + queueVelocityY.searchLast());
                        queueVelocityZ.add(saveVeloZ + queueVelocityZ.searchLast());
                    }else{
                        queueVelocityX.add(0);
                        queueVelocityY.add(0);
                        queueVelocityZ.add(0);
                    }
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
                        distance = Math.sqrt(Math.pow(saveDistX, 2) + Math.pow(saveDistY, 2) + Math.pow(saveDistZ, 2));
                        //distance = Math.sqrt(Math.pow(saveDistX, 2) + Math.pow(saveDistY, 2));
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

    //1秒間の加速度の平均値の大きさが0.1未満なら重力加速度を更新
    public void AdjustGravity(){
        averageQueueX.add(accel_global[1]);
        averageQueueY.add(accel_global[2]);
        averageQueueZ.add(accel_global[3]);
        if (averageQueueX.size() == 100 && averageQueueY.size() == 100){
            double averageX = averageQueueX.average();
            double averageY = averageQueueY.average();
            double averageZ = averageQueueZ.average();

            if (Math.abs(averageX) < 0.1 && Math.abs(averageY) < 0.1){
                gravity = averageZ;
            }
        }
    }

    //直近の加速度の大きさが0.1未満なら重力加速度を更新
    public void AdjustGravity2(){
        if (Math.abs(accel_global[1]) < 0.1 && Math.abs(accel_global[2]) < 0.1){
            gravity = accel_global[3];
        }
    }

    //直近のZ軸方向の加速度の大きさが0.1未満なら重力加速度を更新
    public void AdjustGravity3(){
        if (Math.abs(accel_global[3] - 9.80665) < 0.1){
            gravity = accel_global[3];
        }
    }

    private double std_varX = 0;
    private double std_varY = 0;

    private double aveX = 0;
    private double aveY = 0;
    private double aveZ = 0;

    private boolean MotionFlag = true;
    //0.25秒間の標準偏差が0.05より小さく、平均が0.1よりも小さければ静止状態
    public void AdjustGravity4(){
        std_varX = queueTheresholdX.std_var();
        std_varY = queueTheresholdY.std_var();
        //double std_varZ = queueTheresholdZ.std_var();

        aveX = queueTheresholdX.average();
        aveY = queueTheresholdY.average();
        aveZ = queueTheresholdZ.average();

        if (std_varX<0.2 && std_varY<0.2 &&
                Math.abs(aveX)<0.2 && Math.abs(aveY)<0.2){
            gravity = aveZ;
            ax_global = 0 ;
            ay_global = 0 ;
            az_global = 0 ;
            MotionFlag = false;
            textViewDist.setBackgroundColor(Color.BLUE);
        }else{
            ax_global = accel_global[1] ;
            ay_global = accel_global[2] ;
            az_global = accel_global[3] - gravity;
            MotionFlag = true;
            textViewDist.setBackgroundColor(Color.RED);
        }

        if (std_varX<0.2){
            textViewStdVarX.setBackgroundColor(Color.BLUE);
        }else{
            textViewStdVarY.setBackgroundColor(Color.RED);
        }
        if (std_varY<0.2){
            textViewStdVarY.setBackgroundColor(Color.BLUE);
        }else {
            textViewStdVarX.setBackgroundColor(Color.RED);
        }
        if (Math.abs(aveX)<0.2){
            textViewMeanX.setBackgroundColor(Color.BLUE);
        }else{
            textViewMeanX.setBackgroundColor(Color.RED);
        }
        if (Math.abs(aveY)<0.2){
            textViewMeanY.setBackgroundColor(Color.BLUE);
        }else{
            textViewMeanY.setBackgroundColor(Color.RED);
        }

    }

    //移動平均フィルタ
    public double[] MeanFilter(){
        double aveX = queueMeanX.average();
        double aveY = queueMeanY.average();
        double aveZ = queueMeanZ.average();

        return new double[]{aveX, aveY, aveZ};
    }

    public double[] IIR(double fc){
        double[] a = {0,0,0,0,0,0};
        double denom = 1 + (2 * Math.sqrt(2) * Math.PI * fc) + 4 * Math.pow(Math.PI, 2) * Math.pow(fc, 2);
        a[0] = (4 * Math.pow(Math.PI, 2) * Math.pow(fc, 2)) / denom; //a0
        a[1] = (8 * Math.pow(Math.PI, 2) * Math.pow(fc, 2)) / denom; //a1
        a[2] = (4 * Math.pow(Math.PI, 2) * Math.pow(fc, 2)) / denom; //a2
        a[3] = 1.0; //b0
        a[4] = (8 * Math.pow(Math.PI, 2) * Math.pow(fc, 2) - 2) / denom; //b1
        a[5] = (1 - (2 * Math.sqrt(2) * Math.PI * fc) + 4 * Math.pow(Math.PI, 2) * Math.pow(fc, 2)) / denom; //b2

        return a;
    }

    public void setText(){
        textViewAx = findViewById(R.id.text_view_Ax);
        textViewAy = findViewById(R.id.text_view_Ay);
        textViewAz = findViewById(R.id.text_view_Az);

        textViewGx = findViewById(R.id.text_view_Gx);
        textViewGy = findViewById(R.id.text_view_Gy);
        textViewGz = findViewById(R.id.text_view_Gz);

        textViewQw = findViewById(R.id.text_view_qW);
        textViewQx = findViewById(R.id.text_view_qX);
        textViewQy = findViewById(R.id.text_view_qY);
        textViewQz = findViewById(R.id.text_view_qZ);
        textViewQ = findViewById(R.id.text_view_Q);

        textViewStdVarX = findViewById(R.id.text_view_std_varX);
        textViewMeanX = findViewById(R.id.text_view_meanX);
        textViewStdVarY = findViewById(R.id.text_view_std_varY);
        textViewMeanY = findViewById(R.id.text_view_meanY);

        textViewAxGlo = findViewById(R.id.text_view_Ax_global);
        textViewAyGlo = findViewById(R.id.text_view_Ay_global);
        textViewAzGlo = findViewById(R.id.text_view_Az_global);

        textViewDist = findViewById(R.id.distance);
        textViewBeta = findViewById(R.id.text_view_beta);
    }

}