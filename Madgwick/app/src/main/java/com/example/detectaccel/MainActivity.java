package com.example.detectaccel;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor accel;
    private TextView textViewTitle, textViewAx, textViewAy, textViewAz;
    private TextView textViewPeriod, textViewSpeed, textViewDistance;

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

        textViewDistance = findViewById(R.id.distance);
        textViewSpeed = findViewById(R.id.speed);
        textViewPeriod = findViewById(R.id.period);

        //SystemClock.elapsedRealtimeNanos()はスマホが起動してからの時間を表示する，単位はナノ秒
        //period.setText((SystemClock.elapsedRealtimeNanos()/1e9)+"");
    }

    @Override
    protected void onResume(){
        //Activityが表示されたときに実行される部分
        super.onResume();
        //getDefaultSensorは加速度センサがあるかどうか判断している
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //イベント（ここでは加速度を計測したことを指す）が発生した際にイベントを受け取るListenerの登録
        //取得間隔はマイクロ秒単位で設定できるらしいが，加速度センサは無理らしい（https://akihito104.hatenablog.com/entry/2013/07/22/013000）を参照
        //3月11日時点で何度やっても取得時間が0.2秒より大きくできない，0.2秒が最大？
        sensorManager.registerListener(this, accel, (int) Math.pow(10, 5));
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
    float sensorAx_bef = 0;
    float sensorAy_bef = 0;
    float sensorAz_bef = 0;


    @Override
    public void onSensorChanged(SensorEvent event){
        //ここは頻繁に呼び出されるので処理は簡単にする
        //計算はここの外側で行う
        //センサーのイベントを処理する部分


        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:

                sensorAx = event.values[0];
                sensorAy = event.values[1];
                sensorAz = event.values[2];
/*

                float values_filter[] = calculateDistance(sensorAx, sensorAy, sensorAz, event.timestamp);
                float values_origin[] = calculateDistance(event.values[0], event.values[1], event.values[2], event.timestamp);
*/
                textViewAx.setText(String.format("%.3f",sensorAx));
                textViewAy.setText(String.format("%.3f",sensorAy));
                textViewAz.setText(String.format("%.3f",sensorAz));

                sensorAx_bef = sensorAx;
                sensorAy_bef = sensorAy;
                sensorAz_bef = sensorAz;

                break;
        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //センサー精度の変更を行うときに利用するメソッド
    }

    public void madgwickFilter(float accel[], float gyro[], float mag[]){
        //変数の宣言
        //クォータニオン
        float q0, q1, q2, q3,
                ax, ay, az,
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


        //初期方位推定値（サンプルプログラムを参考にしている）
        q0 = 1;
        q1 = 0;
        q2 = 0;
        q3 = 0;

        //加速度代入
        ax = accel[0];
        ay = accel[1];
        az = accel[2];
        //加速度正規化
        recipNorm = 1.0f / (float) (Math.sqrt(ax*ax + ay*ay + az*az));
        ax *= recipNorm;
        ay *= recipNorm;
        az *= recipNorm;

        //地磁気代入
        mx = mag[0];
        my = mag[1];
        mz = mag[2];
        //地磁気正規化
        recipNorm = 1.0f / (float) (Math.sqrt(mx*mx + my*my + mz*mz));
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

        //地磁気の基準方向を求める
        hx = mx * q0q0 - _2q0my * q3 + _2q0mz * q2 + mx * q1q1 + _2q1 * my * q2 + _2q1 * mz * q3 - mx * q2q2 - mx * q3q3;
        hy = _2q0mx * q3 + my * q0q0 - _2q0mz * q1 + _2q1mx * q2 - my * q1q1 + my * q2q2 + _2q2 * mz * q3 - my * q3q3;
        _2bx = (float) Math.sqrt(hx * hx + hy * hy);
        _2bz = -_2q0mx * q2 + _2q0my * q1 + mz * q0q0 + _2q1mx * q3 - mz * q1q1 + _2q2 * my * q3 - mz * q2q2 + mz * q3q3;
        _4bx = 2.0f * _2bx;
        _4bz = 2.0f * _2bz;

        //勾配降下法
        //∇fを求める
        s0 = -_2q2 * (2.0f * q1q3 - _2q0q2 - ax) + _2q1 * (2.0f * q0q1 + _2q2q3 - ay) - _2bz * q2 * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (-_2bx * q3 + _2bz * q1) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + _2bx * q2 * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
        s1 = _2q3 * (2.0f * q1q3 - _2q0q2 - ax) + _2q0 * (2.0f * q0q1 + _2q2q3 - ay) - 4.0f * q1 * (1 - 2.0f * q1q1 - 2.0f * q2q2 - az) + _2bz * q3 * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (_2bx * q2 + _2bz * q0) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + (_2bx * q3 - _4bz * q1) * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
        s2 = -_2q0 * (2.0f * q1q3 - _2q0q2 - ax) + _2q3 * (2.0f * q0q1 + _2q2q3 - ay) - 4.0f * q2 * (1 - 2.0f * q1q1 - 2.0f * q2q2 - az) + (-_4bx * q2 - _2bz * q0) * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (_2bx * q1 + _2bz * q3) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + (_2bx * q0 - _4bz * q2) * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
        s3 = _2q1 * (2.0f * q1q3 - _2q0q2 - ax) + _2q2 * (2.0f * q0q1 + _2q2q3 - ay) + (-_4bx * q3 + _2bz * q1) * (_2bx * (0.5f - q2q2 - q3q3) + _2bz * (q1q3 - q0q2) - mx) + (-_2bx * q0 + _2bz * q2) * (_2bx * (q1q2 - q0q3) + _2bz * (q0q1 + q2q3) - my) + _2bx * q1 * (_2bx * (q0q2 + q1q3) + _2bz * (0.5f - q1q1 - q2q2) - mz);
        //正規化
        recipNorm = 1.0f / (float) Math.sqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3);
        s0 *= recipNorm;
        s1 *= recipNorm;
        s2 *= recipNorm;
        s3 *= recipNorm;





        //J_Tg(se_q^_est_t-1)



        float ax = accel[0];
        float ay = accel[1];
        float az = accel[2];



    }


}