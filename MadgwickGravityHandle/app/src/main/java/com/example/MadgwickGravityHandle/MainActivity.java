package com.example.MadgwickGravityHandle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor accel,mag,gyro;
    private TextView textViewTitle, textViewAx, textViewAy, textViewAz;
    private TextView textViewMx, textViewMy, textViewMz;
    private TextView textViewGx, textViewGy, textViewGz;
    private TextView textViewPeriod, textViewSpeed, textViewDistance;
    private TextView textViewRoll, textViewPitch, textViewYaw;
    private TextView textViewAxAft, textViewAyAft,textViewAzAft;
    private TextView textViewAxGlo, textViewAyGlo,textViewAzGlo;
    private Orientation orientation = new Orientation();
    private Distance distance = new Distance();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private long time;
    //private float q0,q1,q2,q3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //アプリを起動した際に実行される部分
        //Activityが生成される
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //センサーサービスへの参照を取得する，SensorManagerクラスのインスタンスを作成する．
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        textViewTitle =findViewById(R.id.title_accel);
        textViewAx = findViewById(R.id.text_view_Ax);
        textViewAy = findViewById(R.id.text_view_Ay);
        textViewAz = findViewById(R.id.text_view_Az);

        textViewMx = findViewById(R.id.text_view_Mx);
        textViewMy = findViewById(R.id.text_view_My);
        textViewMz = findViewById(R.id.text_view_Mz);

        textViewGx = findViewById(R.id.text_view_Gx);
        textViewGy = findViewById(R.id.text_view_Gy);
        textViewGz = findViewById(R.id.text_view_Gz);

        textViewDistance = findViewById(R.id.distance);
        textViewSpeed = findViewById(R.id.speed);
        textViewPeriod = findViewById(R.id.period);

        textViewRoll = findViewById(R.id.text_view_roll);
        textViewPitch = findViewById(R.id.text_view_pitch);
        textViewYaw = findViewById(R.id.text_view_yaw);

        textViewAxAft = findViewById(R.id.text_view_Ax_aft);
        textViewAyAft = findViewById(R.id.text_view_Ay_aft);
        textViewAzAft = findViewById(R.id.text_view_Az_aft);

        textViewAxGlo = findViewById(R.id.text_view_Ax_global);
        textViewAyGlo = findViewById(R.id.text_view_Ay_global);
        textViewAzGlo = findViewById(R.id.text_view_Az_global);


        //SystemClock.elapsedRealtimeNanos()はスマホが起動してからの時間を表示する，単位はナノ秒
        //period.setText((SystemClock.elapsedRealtimeNanos()/1e9)+"")
        time = SystemClock.elapsedRealtimeNanos();

        StartThread();




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

    float[] sensorAccelBef = {0, 0, 0};
    long sampleTime;

    @Override
    public void onSensorChanged(SensorEvent event){
        //ここは頻繁に呼び出されるので処理は簡単にする
        //計算はここの外側で行う
        //センサーのイベントを処理する部分

        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:

                sensorAccel[0] = event.values[0];
                sensorAccel[1] = event.values[1];
                sensorAccel[2] = event.values[2];
                sampleTime = event.timestamp;

                textViewAx.setText(String.format("%.3f",sensorAccel[0]));
                textViewAy.setText(String.format("%.3f",sensorAccel[1]));
                textViewAz.setText(String.format("%.3f",sensorAccel[2]));


                break;

            case Sensor.TYPE_MAGNETIC_FIELD:

                sensorMagnet[0] = event.values[0];
                sensorMagnet[1] = event.values[1];
                sensorMagnet[2] = event.values[2];
                sampleTime = event.timestamp;

                textViewMx.setText(String.format("%.3f",sensorMagnet[0]));
                textViewMy.setText(String.format("%.3f",sensorMagnet[1]));
                textViewMz.setText(String.format("%.3f",sensorMagnet[2]));

                break;

            case Sensor.TYPE_GYROSCOPE:

                sensorGyro[0] = event.values[0];
                sensorGyro[1] = event.values[1];
                sensorGyro[2] = event.values[2];
                sampleTime = event.timestamp;

                textViewGx.setText(String.format("%.3f",sensorGyro[0]));
                textViewGy.setText(String.format("%.3f",sensorGyro[1]));
                textViewGz.setText(String.format("%.3f",sensorGyro[2]));

                break;
        }

    }

    double ax_global, ay_global, az_global;
    double axNotGrav,ayNotGrav,azNotGrav;
    double estDist = 0;

    private void StartThread(){
        runnable = new Runnable() {
            @Override
            public void run() {
                
                float dif = diff_time(sampleTime, time);
                //9軸センサ
                float rpy[] = orientation.madgwickFilter(sensorAccel, sensorGyro, sensorMagnet, 1.0F / dif);
                //６軸センサ
                //float rpy[] = orientation.madgwickFilterNoMag(sensorAccel, sensorGyro, 1.0F / dif);

                textViewRoll.setText(String.valueOf(rpy[0]));
                textViewPitch.setText(String.valueOf(rpy[1]));
                textViewYaw.setText(String.valueOf(rpy[2]));

                ax_global = Math.cos(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[1])) * (double) sensorAccel[0] +
                        (Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) - Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * (double)sensorAccel[1] +
                        (Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) + Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[0]))) * (double)sensorAccel[2];

                ay_global = Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[1])) * (double)sensorAccel[0] +
                        (Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) + Math.cos(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * (double)sensorAccel[1] +
                        (Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) - Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[0]))) * (double)sensorAccel[2];

                az_global = -Math.sin(Math.toRadians(rpy[1])) * (double)sensorAccel[0] +
                        Math.cos(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) * (double)sensorAccel[1] +
                        Math.cos(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) * (double)sensorAccel[2];
                double accelGlobal [] = {ax_global, ay_global, az_global};

                sensorAccelBef = sensorAccel;

                axNotGrav = sensorAccel[0] + 9.8f * Math.sin(Math.toRadians(rpy[1]));
                ayNotGrav = sensorAccel[1] - 9.8f * Math.cos(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0]));
                azNotGrav = sensorAccel[2] - 9.8f * Math.cos(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0]));

                textViewAxAft.setText(String.valueOf(Math.round(axNotGrav * 100d)/100d));
                textViewAyAft.setText(String.valueOf(Math.round(ayNotGrav * 100d)/100d));
                textViewAzAft.setText(String.valueOf(Math.round(azNotGrav * 100d)/100d));

                textViewAxGlo.setText(String.valueOf(Math.round(ax_global * 100d)/100d));
                textViewAyGlo.setText(String.valueOf(Math.round(ay_global * 100d)/100d));
                textViewAzGlo.setText(String.valueOf(Math.round(az_global * 100d)/100d));

                estDist = distance.rungeKutta(accelGlobal, dif);

                time = sampleTime;

                handler.postDelayed(this, 30);

            }
        };
        handler.post(runnable);
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //センサー精度の変更を行うときに利用するメソッド
    }

    public float diff_time(long time, long bef_time){
        return (time - bef_time) / 1000000000F;
    }

}