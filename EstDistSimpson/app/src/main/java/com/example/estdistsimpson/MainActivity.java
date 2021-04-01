package com.example.estdistsimpson;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor accel;
    private TextView textViewTitle, textViewAx, textViewAy, textViewAz;
    private TextView textViewCount, textViewQueue, textViewDistance;

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
        textViewQueue = findViewById(R.id.queue);
        textViewCount = findViewById(R.id.count);

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
    Queue<Float> queueAx = new ArrayBlockingQueue<>(3);
    Queue<Float> queueAy = new ArrayBlockingQueue<>(3);
    Queue<Float> queueAz = new ArrayBlockingQueue<>(3);
    float time_bef = 0;
    float period = 0;


    int count_acc = 0;
    int count_spd = 0;


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

                //キューがいっぱいなら先頭のやつ1つ消す
                if (queueAx.size() == 3 && queueAy.size() == 3 && queueAz.size() == 3 ){
                    queueAx.poll();
                    queueAy.poll();
                    queueAz.poll();
                }
                //キューに代入
                queueAx.offer(sensorAx);
                queueAy.offer(sensorAy);
                queueAz.offer(sensorAz);
                //最初に計測した奴を代入しておく
                if (count_acc== 1){
                    time_bef = event.timestamp;
                }
                //キューに3つの要素があり，カウントが奇数の時，シンプソンをつかう
                if (queueAx.size() == 3 && count_acc%2 == 1){
                    Float[] array = new Float[3];
                    queueAx.toArray(array);
                    period = event.timestamp - time_bef;
                    estDist(array, period);
                }

                break;
        }

        textViewQueue.setText(String.valueOf(queueAx));
        textViewCount.setText(String.valueOf(count_acc));
        count_acc++;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //センサー精度の変更を行うときに利用するメソッド
    }

    Queue<Float> queueVx = new ArrayBlockingQueue<>(3);

    public float simpson(Float[] str, float diff){
        return (str[0] + str[2] + 4 * str[1])*diff/6;
    }

    public void estDist(Float[] accel, float diff){
        if (queueVx.size() == 3 ){
            queueAx.poll();
        }
        queueVx.offer(simpson(accel, diff));

        //キューに3つの要素があり，カウントが奇数の時，シンプソンをつかう
        if (queueVx.size() == 3 && count_spd%2 == 1){
            Float[] array_vx = new Float[3];
            queueVx.toArray(array_vx);
            period = event.timestamp - time_bef;
            estDistSimpson(array, period);
        }


    }


}