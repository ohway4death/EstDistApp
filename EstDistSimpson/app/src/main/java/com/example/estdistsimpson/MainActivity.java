package com.example.estdistsimpson;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;

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


}