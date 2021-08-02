package com.example.MadgwickGravityFile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.util.Log;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor accel,mag,gyro;
    private TextView textViewTitle, textViewAx, textViewAy, textViewAz;
    private TextView textViewMx, textViewMy, textViewMz;
    private TextView textViewGx, textViewGy, textViewGz;
    private TextView textViewRoll, textViewPitch, textViewYaw;
    private TextView textViewAxAft, textViewAyAft,textViewAzAft;
    private TextView textViewAxGlo, textViewAyGlo,textViewAzGlo;

    private long time;
    private float q0,q1,q2,q3;

    private File file;
    private FileWriter fw;
    private PrintWriter pw;
    private Context context;

    private int recordFrag = 0;

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

        textViewRoll = findViewById(R.id.text_view_roll);
        textViewPitch = findViewById(R.id.text_view_pitch);
        textViewYaw = findViewById(R.id.text_view_yaw);

        textViewAxAft = findViewById(R.id.text_view_Ax_aft);
        textViewAyAft = findViewById(R.id.text_view_Ay_aft);
        textViewAzAft = findViewById(R.id.text_view_Az_aft);

        textViewAxGlo = findViewById(R.id.text_view_Ax_global);
        textViewAyGlo = findViewById(R.id.text_view_Ay_global);
        textViewAzGlo = findViewById(R.id.text_view_Az_global);

        Button startButton = findViewById(R.id.recordStartButton);
        Button stopButton = findViewById(R.id.recordStopButton);

        //SystemClock.elapsedRealtimeNanos()はスマホが起動してからの時間を表示する，単位はナノ秒
        //period.setText((SystemClock.elapsedRealtimeNanos()/1e9)+"")

        time = SystemClock.elapsedRealtimeNanos();

        //初期方位推定値（サンプルプログラムを参考にしている）
        q0 = 1;
        q1 = 0;
        q2 = 0;
        q3 = 0;

        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                recordFrag = 1;

                context = getApplicationContext();
                String fileName = "Fastest_10min_desk_gravModify.csv";
                file = new File(context.getFilesDir(), fileName);
                try{
                    //引数はファイル名と書き込まれたデータを追加するかどうか決める、trueならファイルの最後に追記していく。
                    fw = new FileWriter(file, true);
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
                    pw.print("gyroX");
                    pw.print(",");
                    pw.print("gyroY");
                    pw.print(",");
                    pw.print("gyroZ");
                    pw.print(",");
                    pw.print("magnetX");
                    pw.print(",");
                    pw.print("magnetY");
                    pw.print(",");
                    pw.print("magnetZ");
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

            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordFrag = 0;
                Toast toastStop = Toast.makeText(getApplicationContext(), "Stop Record", Toast.LENGTH_SHORT);
                toastStop.show();
            }
        });

        //Toast.makeText(this, "Here is OnCreate", Toast.LENGTH_LONG).show();
        //Log.i("test", "test");
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
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
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

                textViewAx.setText(String.format("%.3f",sensorAccel[0]));
                textViewAy.setText(String.format("%.3f",sensorAccel[1]));
                textViewAz.setText(String.format("%.3f",sensorAccel[2]));

                break;

            case Sensor.TYPE_MAGNETIC_FIELD:

                sensorMagnet[0] = event.values[0];
                sensorMagnet[1] = event.values[1];
                sensorMagnet[2] = event.values[2];

                textViewMx.setText(String.format("%.3f",sensorMagnet[0]));
                textViewMy.setText(String.format("%.3f",sensorMagnet[1]));
                textViewMz.setText(String.format("%.3f",sensorMagnet[2]));

                break;

            case Sensor.TYPE_GYROSCOPE:

                sensorGyro[0] = event.values[0];
                sensorGyro[1] = event.values[1];
                sensorGyro[2] = event.values[2];

                textViewGx.setText(String.format("%.3f",sensorGyro[0]));
                textViewGy.setText(String.format("%.3f",sensorGyro[1]));
                textViewGz.setText(String.format("%.3f",sensorGyro[2]));

                break;
        }


        float dif = diff_time(event.timestamp, time);
        float[] rpy = madgwickFilter(sensorAccel, sensorGyro, sensorMagnet, 1.0F / dif);

        //textViewRoll.setText(String.valueOf(rpy[0]));
        //textViewPitch.setText(String.valueOf(rpy[1]));
        //textViewYaw.setText(String.valueOf(rpy[2]));

        //textViewPeriod.setText(String.valueOf(dif));
        //textViewSpeed.setText(String.valueOf(event.timestamp/1000000000F));
        //textViewDistance.setText(String.valueOf(time/1000000000F));

        double ax_after = sensorAccel[0] + 9.80665f * Math.sin(Math.toRadians(rpy[1]));
        double ay_after = sensorAccel[1] - 9.80665f * Math.cos(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0]));
        double az_after = sensorAccel[2] - 9.80665f * Math.cos(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0]));

        //textViewAxAft.setText(String.valueOf(ax_after));
        //textViewAyAft.setText(String.valueOf(ay_after));
        //textViewAzAft.setText(String.valueOf(az_after));

        double ax_global = Math.cos(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[1])) * (double) sensorAccel[0] +
                          (Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) - Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * (double)sensorAccel[1] +
                          (Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) + Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[0]))) * (double)sensorAccel[2];

        double ay_global = Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[1])) * (double)sensorAccel[0] +
                          (Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) + Math.cos(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * (double)sensorAccel[1] +
                          (Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) - Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[0]))) * (double)sensorAccel[2];


        double az_global = -Math.sin(Math.toRadians(rpy[1])) * (double)sensorAccel[0] +
                            Math.cos(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) * (double)sensorAccel[1] +
                            Math.cos(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) * (double)sensorAccel[2];

        /*
        double ax_global = Math.cos(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[1])) * ax_before +
                (Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) - Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * ay_before +
                (Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) + Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * az_before;

        double ay_global = Math.sin(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[1])) * ax_before +
                (Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) + Math.cos(Math.toRadians(rpy[2])) * Math.cos(Math.toRadians(rpy[0]))) * ay_before +
                (Math.sin(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) - Math.cos(Math.toRadians(rpy[2])) * Math.sin(Math.toRadians(rpy[0]))) * az_before;

        double az_global = -Math.sin(Math.toRadians(rpy[1])) * ax_before +
                Math.cos(Math.toRadians(rpy[1])) * Math.sin(Math.toRadians(rpy[0])) * ay_before +
                Math.cos(Math.toRadians(rpy[1])) * Math.cos(Math.toRadians(rpy[0])) * az_before;
         */

        //textViewAxGlo.setText(String.valueOf(ax_global));
        //textViewAyGlo.setText(String.valueOf(ay_global));
        //textViewAzGlo.setText(String.valueOf(az_global));


        if(recordFrag == 1){
            pw.print(event.timestamp + ",");
            pw.print(sensorAccel[0] + ",");
            pw.print(sensorAccel[1] + ",");
            pw.print(sensorAccel[2] + ",");
            pw.print(sensorGyro[0] + ",");
            pw.print(sensorGyro[1] + ",");
            pw.print(sensorGyro[2] + ",");
            pw.print(sensorMagnet[0] + ",");
            pw.print(sensorMagnet[1] + ",");
            pw.print(sensorMagnet[2] + ",");
            pw.print(ax_after + ",");
            pw.print(ay_after + ",");
            pw.print(az_after + ",");
            pw.print(ax_global + ",");
            pw.print(ay_global + ",");
            pw.print(az_global);
            pw.println();
        };

        time = event.timestamp;

        /*
        long timeStamp = event.timestamp;
        double dif = (double)(timeStamp - time) / 1000000000;
        float[] rpy = madgwickFilter(sensorAccel, sensorGyro, sensorMagnet, (float) (1.0d / dif));
        textViewRoll.setText(String.format("%.3f", rpy[0]));
        textViewPitch.setText(String.format("%.3f", rpy[1]));
        textViewYaw.setText(String.format("%.3f", rpy[2]));
        textViewPeriod.setText(String.valueOf(dif));
        textViewSpeed.setText(String.valueOf(timeStamp));
        textViewDistance.setText(String.valueOf(time));
        time = timeStamp;
        */
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //センサー精度の変更を行うときに利用するメソッド
    }

    public float diff_time(long time, long bef_time){
        return (time - bef_time) / 1000000000F;
    }

/*
    public float[] updateIMU(float[] accel,float[] gyro, float sampleFreq){
        float recipNorm;
        float s0, s1, s2, s3;
        float qDot1, qDot2, qDot3, qDot4;
        float _2q0, _2q1, _2q2, _2q3, _4q0, _4q1, _4q2 ,_8q1, _8q2, q0q0, q1q1, q2q2, q3q3;

        float ax = accel[0];
        float ay = accel[1];
        float az = accel[2];

        float gx = gyro[0];
        float gy = gyro[1];
        float gz = gyro[2];

        //ロール、ピッチ、ヨー角
        float roll, pitch, yaw;
        roll = 0;
        pitch = 0;
        yaw = 0;

        //論文と同じ値に設定している
        float beta = 0.41f;

        float invSampleFreq = 1.0f / sampleFreq;

        // Rate of change of quaternion from gyroscope
        qDot1 = 0.5f * (-q1 * gx - q2 * gy - q3 * gz);
        qDot2 = 0.5f * (q0 * gx + q2 * gz - q3 * gy);
        qDot3 = 0.5f * (q0 * gy - q1 * gz + q3 * gx);
        qDot4 = 0.5f * (q0 * gz + q1 * gy - q2 * gx);

        // Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
        if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {

            // Normalise accelerometer measurement
            recipNorm = invSqrt(ax * ax + ay * ay + az * az);
            ax *= recipNorm;
            ay *= recipNorm;
            az *= recipNorm;

            // Auxiliary variables to avoid repeated arithmetic
            _2q0 = 2.0f * q0;
            _2q1 = 2.0f * q1;
            _2q2 = 2.0f * q2;
            _2q3 = 2.0f * q3;
            _4q0 = 4.0f * q0;
            _4q1 = 4.0f * q1;
            _4q2 = 4.0f * q2;
            _8q1 = 8.0f * q1;
            _8q2 = 8.0f * q2;
            q0q0 = q0 * q0;
            q1q1 = q1 * q1;
            q2q2 = q2 * q2;
            q3q3 = q3 * q3;

            // Gradient decent algorithm corrective step
            s0 = _4q0 * q2q2 + _2q2 * ax + _4q0 * q1q1 - _2q1 * ay;
            s1 = _4q1 * q3q3 - _2q3 * ax + 4.0f * q0q0 * q1 - _2q0 * ay - _4q1 + _8q1 * q1q1 + _8q1 * q2q2 + _4q1 * az;
            s2 = 4.0f * q0q0 * q2 + _2q0 * ax + _4q2 * q3q3 - _2q3 * ay - _4q2 + _8q2 * q1q1 + _8q2 * q2q2 + _4q2 * az;
            s3 = 4.0f * q1q1 * q3 - _2q1 * ax + 4.0f * q2q2 * q3 - _2q2 * ay;
            recipNorm = invSqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3); // normalise step magnitude
            s0 *= recipNorm;
            s1 *= recipNorm;
            s2 *= recipNorm;
            s3 *= recipNorm;

            // Apply feedback step
            qDot1 -= beta * s0;
            qDot2 -= beta * s1;
            qDot3 -= beta * s2;
            qDot4 -= beta * s3;
        }

        // Integrate rate of change of quaternion to yield quaternion
        q0 += qDot1 * invSampleFreq;
        q1 += qDot2 * invSampleFreq;
        q2 += qDot3 * invSampleFreq;
        q3 += qDot4 * invSampleFreq;

        // Normalise quaternion
        recipNorm = invSqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 *= recipNorm;
        q1 *= recipNorm;
        q2 *= recipNorm;
        q3 *= recipNorm;

        //サンプルプログラム通り
        roll = (float) Math.toDegrees(Math.atan2(q0*q1 + q2*q3, 0.5f - q1*q1 - q2*q2));
        pitch = (float) Math.toDegrees(Math.asin (-2.0f * (q1*q3 - q0*q2)));
        yaw = (float) Math.toDegrees(Math.atan2(q1*q2 + q0*q3, 0.5f - q2*q2 - q3*q3)) + 180.0f;

        float angle[] = {roll, pitch, yaw};

        return angle;

    }


    public float[] madgwickFilter2(float accel[], float gyro[], float mag[], float sampleFreq){
        float recipNorm;
        float s0, s1, s2, s3;
        float qDot1, qDot2, qDot3, qDot4;
        float hx, hy;
        float _2q0mx, _2q0my, _2q0mz, _2q1mx, _2bx, _2bz, _4bx, _4bz, _2q0, _2q1, _2q2, _2q3, _2q0q2, _2q2q3, q0q0, q0q1, q0q2, q0q3, q1q1, q1q2, q1q3, q2q2, q2q3, q3q3;

        //ロール、ピッチ、ヨー角
        float roll, pitch, yaw;

        //論文と同じ値に設定している
        float beta = 0.41f;

        float invSampleFreq = 1.0f / sampleFreq;

        float ax = accel[0];
        float ay = accel[1];
        float az = accel[2];

        float gx = gyro[0];
        float gy = gyro[1];
        float gz = gyro[2];

        //地磁気代入
        float mx = mag[0];
        float my = mag[1];
        float mz = mag[2];


        // Use IMU algorithm if magnetometer measurement invalid (avoids NaN in magnetometer normalisation)
        if((mx == 0.0f) && (my == 0.0f) && (mz == 0.0f)) {
            return updateIMU(accel, gyro, sampleFreq);
        }


        // Rate of change of quaternion from gyroscope
        qDot1 = 0.5f * (-q1 * gx - q2 * gy - q3 * gz);
        qDot2 = 0.5f * (q0 * gx + q2 * gz - q3 * gy);
        qDot3 = 0.5f * (q0 * gy - q1 * gz + q3 * gx);
        qDot4 = 0.5f * (q0 * gz + q1 * gy - q2 * gx);

        // Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
        if(!((ax == 0.0f) && (ay == 0.0f) && (az == 0.0f))) {

            // Normalise accelerometer measurement
            recipNorm = invSqrt(ax * ax + ay * ay + az * az);
            ax *= recipNorm;
            ay *= recipNorm;
            az *= recipNorm;

            // Normalise magnetometer measurement
            recipNorm = invSqrt(mx * mx + my * my + mz * mz);
            mx *= recipNorm;
            my *= recipNorm;
            mz *= recipNorm;

            // Auxiliary variables to avoid repeated arithmetic
            _2q0mx = 2.0f * q0 * mx;
            _2q0my = 2.0f * q0 * my;
            _2q0mz = 2.0f * q0 * mz;
            _2q1mx = 2.0f * q1 * mx;
            _2q0 = 2.0f * q0;
            _2q1 = 2.0f * q1;
            _2q2 = 2.0f * q2;
            _2q3 = 2.0f * q3;
            _2q0q2 = 2.0f * q0 * q2;
            _2q2q3 = 2.0f * q2 * q3;
            q0q0 = q0 * q0;
            q0q1 = q0 * q1;
            q0q2 = q0 * q2;
            q0q3 = q0 * q3;
            q1q1 = q1 * q1;
            q1q2 = q1 * q2;
            q1q3 = q1 * q3;
            q2q2 = q2 * q2;
            q2q3 = q2 * q3;
            q3q3 = q3 * q3;

            // Reference direction of Earth's magnetic field
            hx = mx * q0q0 - _2q0my * q3 + _2q0mz * q2 + mx * q1q1 + _2q1 * my * q2 + _2q1 * mz * q3 - mx * q2q2 - mx * q3q3;
            hy = _2q0mx * q3 + my * q0q0 - _2q0mz * q1 + _2q1mx * q2 - my * q1q1 + my * q2q2 + _2q2 * mz * q3 - my * q3q3;
            _2bx = (float) Math.sqrt(hx * hx + hy * hy);
            _2bz = -_2q0mx * q2 + _2q0my * q1 + mz * q0q0 + _2q1mx * q3 - mz * q1q1 + _2q2 * my * q3 - mz * q2q2 + mz * q3q3;
            _4bx = 2.0f * _2bx;
            _4bz = 2.0f * _2bz;

            // Gradient decent algorithm corrective step
            s0 = -_2q2 * (2.0f * q1q3 - _2q0q2 - ax) + _2q1 * (2.0f * q0q1 + _2q2q3 - ay) - _2bz * q2 * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (-_2bx * q3 + _2bz * q1) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + _2bx * q2 * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
            s1 = _2q3 * (2.0f * q1q3 - _2q0q2 - ax) + _2q0 * (2.0f * q0q1 + _2q2q3 - ay) - 4.0f * q1 * (1 - 2.0f * q1q1 - 2.0f * q2q2 - az) + _2bz * q3 * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (_2bx * q2 + _2bz * q0) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + (_2bx * q3 - _4bz * q1) * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
            s2 = -_2q0 * (2.0f * q1q3 - _2q0q2 - ax) + _2q3 * (2.0f * q0q1 + _2q2q3 - ay) - 4.0f * q2 * (1 - 2.0f * q1q1 - 2.0f * q2q2 - az) + (-_4bx * q2 - _2bz * q0) * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (_2bx * q1 + _2bz * q3) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + (_2bx * q0 - _4bz * q2) * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
            s3 = _2q1 * (2.0f * q1q3 - _2q0q2 - ax) + _2q2 * (2.0f * q0q1 + _2q2q3 - ay) + (-_4bx * q3 + _2bz * q1) * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (-_2bx * q0 + _2bz * q2) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + _2bx * q1 * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
            recipNorm = invSqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3); // normalise step magnitude
            s0 *= recipNorm;
            s1 *= recipNorm;
            s2 *= recipNorm;
            s3 *= recipNorm;

            // Apply feedback step
            qDot1 -= beta * s0;
            qDot2 -= beta * s1;
            qDot3 -= beta * s2;
            qDot4 -= beta * s3;
        }

        // Integrate rate of change of quaternion to yield quaternion
        q0 += qDot1 * invSampleFreq;
        q1 += qDot2 * invSampleFreq;
        q2 += qDot3 * invSampleFreq;
        q3 += qDot4 * invSampleFreq;

        // Normalise quaternion
        recipNorm = invSqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 *= recipNorm;
        q1 *= recipNorm;
        q2 *= recipNorm;
        q3 *= recipNorm;

        //サンプルプログラム通り
        roll = (float) Math.toDegrees(Math.atan2(q0*q1 + q2*q3, 0.5f - q1*q1 - q2*q2));
        pitch = (float) Math.toDegrees(Math.asin (-2.0f * (q1*q3 - q0*q2)));
        yaw = (float) Math.toDegrees(Math.atan2(q1*q2 + q0*q3, 0.5f - q2*q2 - q3*q3)) + 180.0f;

        //角度を返す
        float angle[] = {roll, pitch, yaw};

        return angle;

    }

 */

    public float[] madgwickFilter(float accel[], float gyro[], float mag[], float sampleFreq){
        float ax, ay, az,
                gx, gy, gz,
                mx, my, mz;

        float s0, s1, s2,s3;
        float _2q0mx, _2q0my, _2q0mz, _2q1mx,
                _2bx, _2bz, _4bx, _4bz,
                _2q0, _2q1, _2q2, _2q3,
                _2q0q2, _2q2q3,
                q0q0, q0q1, q0q2, q0q3, q1q1, q1q2, q1q3, q2q2, q2q3, q3q3;
        //正規化で使う変数
        float recipNorm;

        //世界座標系から見た地磁気
        float hx, hy, hz;

        //クォータニオンの変化率
        float qDot1, qDot2, qDot3, qDot4;

        //論文と同じ値に設定している
        float beta = 0.41f;

        //ロール、ピッチ、ヨー角
        float roll, pitch, yaw;

        //サンプリング周波数の逆数
        float invSampleFreq = 1.0f / sampleFreq;

        //加速度代入
        ax = accel[0];
        ay = accel[1];
        az = accel[2];
        //加速度正規化
        recipNorm = invSqrt(ax*ax + ay*ay + az*az);
        ax *= recipNorm;
        ay *= recipNorm;
        az *= recipNorm;

        //地磁気代入
        mx = mag[0];
        my = mag[1];
        mz = mag[2];
        //地磁気正規化
        recipNorm = invSqrt(mx*mx + my*my + mz*mz);
        mx *= recipNorm;
        my *= recipNorm;
        mz *= recipNorm;

        //ジャイロ代入(ジャイロの単位はrad/s)
        gx = gyro[0];
        gy = gyro[1];
        gz = gyro[2];

        //計算のための変数を用意
        _2q0mx = 2.0f * q0 * mx;
        _2q0my = 2.0f * q0 * my;
        _2q0mz = 2.0f * q0 * mz;
        _2q1mx = 2.0f * q1 * mx;
        _2q0 = 2.0f * q0;
        _2q1 = 2.0f * q1;
        _2q2 = 2.0f * q2;
        _2q3 = 2.0f * q3;
        _2q0q2 = 2.0f * q0 * q2;
        _2q2q3 = 2.0f * q2 * q3;
        q0q0 = q0 * q0;
        q0q1 = q0 * q1;
        q0q2 = q0 * q2;
        q0q3 = q0 * q3;
        q1q1 = q1 * q1;
        q1q2 = q1 * q2;
        q1q3 = q1 * q3;
        q2q2 = q2 * q2;
        q2q3 = q2 * q3;
        q3q3 = q3 * q3;

        //地磁気の基準方向を求める(45)(46)式より
        hx = mx * q0q0 - _2q0my * q3 + _2q0mz * q2 + mx * q1q1 + _2q1 * my * q2 + _2q1 * mz * q3 - mx * q2q2 - mx * q3q3;
        hy = _2q0mx * q3 + my * q0q0 - _2q0mz * q1 + _2q1mx * q2 - my * q1q1 + my * q2q2 + _2q2 * mz * q3 - my * q3q3;
        _2bx = (float) Math.sqrt(hx * hx + hy * hy);
        _2bz = -_2q0mx * q2 + _2q0my * q1 + mz * q0q0 + _2q1mx * q3 - mz * q1q1 + _2q2 * my * q3 - mz * q2q2 + mz * q3q3;
        _4bx = 2.0f * _2bx;
        _4bz = 2.0f * _2bz;

        //勾配降下法
        //∇fを求める(31)(32)式より
        s0 = -_2q2 * (2.0f * q1q3 - _2q0q2 - ax) + _2q1 * (2.0f * q0q1 + _2q2q3 - ay) - _2bz * q2 * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (-_2bx * q3 + _2bz * q1) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + _2bx * q2 * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
        s1 = _2q3 * (2.0f * q1q3 - _2q0q2 - ax) + _2q0 * (2.0f * q0q1 + _2q2q3 - ay) - 4.0f * q1 * (1 - 2.0f * q1q1 - 2.0f * q2q2 - az) + _2bz * q3 * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (_2bx * q2 + _2bz * q0) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + (_2bx * q3 - _4bz * q1) * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
        s2 = -_2q0 * (2.0f * q1q3 - _2q0q2 - ax) + _2q3 * (2.0f * q0q1 + _2q2q3 - ay) - 4.0f * q2 * (1 - 2.0f * q1q1 - 2.0f * q2q2 - az) + (-_4bx * q2 - _2bz * q0) * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (_2bx * q1 + _2bz * q3) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + (_2bx * q0 - _4bz * q2) * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
        s3 = _2q1 * (2.0f * q1q3 - _2q0q2 - ax) + _2q2 * (2.0f * q0q1 + _2q2q3 - ay) + (-_4bx * q3 + _2bz * q1) * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (-_2bx * q0 + _2bz * q2) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + _2bx * q1 * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
        //正規化
        recipNorm = invSqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3);
        s0 *= recipNorm;
        s1 *= recipNorm;
        s2 *= recipNorm;
        s3 *= recipNorm;

        //ジャイロから求めたクォータニオンの変化率　(11)式より
        qDot1 = 0.5f * (-q1 * gx - q2 * gy - q3 * gz);
        qDot2 = 0.5f * (q0 * gx + q2 * gz - q3 * gy);
        qDot3 = 0.5f * (q0 * gy - q1 * gz + q3 * gx);
        qDot4 = 0.5f * (q0 * gz + q1 * gy - q2 * gx);

        //変化率の更新(33)式？
        qDot1 -= beta * s0;
        qDot2 -= beta * s1;
        qDot3 -= beta * s2;
        qDot4 -= beta * s3;

        //クォータニオン変化率を積分してクォータニオンを求める
        q0 += qDot1 * invSampleFreq;
        q1 += qDot2 * invSampleFreq;
        q2 += qDot3 * invSampleFreq;
        q3 += qDot4 * invSampleFreq;

        //クォータニオンを正規化する
        recipNorm = invSqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 *= recipNorm;
        q1 *= recipNorm;
        q2 *= recipNorm;
        q3 *= recipNorm;

        //サンプルプログラム通り
        roll = (float) Math.toDegrees(Math.atan2(q0*q1 + q2*q3, 0.5f - q1*q1 - q2*q2));
        pitch = (float) Math.toDegrees(Math.asin (-2.0f * (q1*q3 - q0*q2)));
        yaw = (float) Math.toDegrees(Math.atan2(q1*q2 + q0*q3, 0.5f - q2*q2 - q3*q3)) + 180.0f;


        //論文の式の通り、サンプルや下の普通に変換した場合と値が違った、ピッチが間違っている感じ、ヨーは正負が逆転していた
        /*
        roll = (float) Math.toDegrees(Math.atan2(2 * (q2 * q3 - q0 * q1), 2 * (q0 * q0 + q3 * q3) - 1));
        pitch = (float) Math.toDegrees(-1.0f * Math.asin (2.0f * (q1 * q3 + q0 * q2)));
        yaw = (float) Math.toDegrees(Math.atan2(2.0f * (q1 * q2 - q0 * q3), 2.0f * (q0 * q0 + q1 * q1) - 1));
        */

        //https://www.kazetest.com/vcmemo/quaternion/quaternion.htm これを参考にしてクォータニオンからオイラー角に変換した、サンプルプログラムと同じ値を示している
        /*
        float roll2 = (float) Math.toDegrees(Math.atan2(2 * (q0*q1 + q2*q3), q0*q0 - q1*q1 - q2*q2 + q3*q3));
        float pitch2 = (float) Math.toDegrees(Math.asin(2 * (q0*q2 - q1*q3)));
        float yaw2 = (float) Math.toDegrees(Math.atan2(2 * (q0*q3 + q1*q2), q0*q0 + q1*q1 - q2*q2 - q3*q3));
        */

        //角度を返す
        float angle[] = {roll, pitch, yaw};

        return angle;

    }

    //平方根の逆数を求める関数
    public float invSqrt(float x) {
        int i = Float.floatToRawIntBits(x);
        float y = Float.intBitsToFloat(0x5f3759df - (i >> 1));
        return y * (1.5F - 0.5F * x * y * y);
    }


}