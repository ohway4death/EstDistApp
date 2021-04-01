package com.example.myaccelarator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    private TextView textViewAx, textViewAy, textViewAz;
    private TextView textViewMx, textViewMy, textViewMz;
    private TextView textViewRx, textViewRy, textViewRz;
    private TextView textViewAxt, textViewAyt, textViewAzt;
    private TextView textViewMxt, textViewMyt, textViewMzt;
    private TextView textViewRxt, textViewRyt, textViewRzt;
    private TextView period;
    private TextView textViewDis;
    private long before, after;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //アプリを起動した際に実行される部分
        //Activityが生成される
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //センサーサービスへの参照を取得する，SensorManagerクラスのインスタンスを作成する．
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //
        textViewAx = findViewById(R.id.text_view_Ax);
        textViewAy = findViewById(R.id.text_view_Ay);
        textViewAz = findViewById(R.id.text_view_Az);

        textViewMx = findViewById(R.id.text_view_Mx);
        textViewMy = findViewById(R.id.text_view_My);
        textViewMz = findViewById(R.id.text_view_Mz);

        textViewRx = findViewById(R.id.text_view_Rx);
        textViewRy = findViewById(R.id.text_view_Ry);
        textViewRz = findViewById(R.id.text_view_Rz);

        textViewAxt = findViewById(R.id.text_view_Axt);
        textViewAyt = findViewById(R.id.text_view_Ayt);
        textViewAzt = findViewById(R.id.text_view_Azt);
/*
        textViewMxt = findViewById(R.id.text_view_Mxt);
        textViewMyt = findViewById(R.id.text_view_Myt);
        textViewMzt = findViewById(R.id.text_view_Mzt);

        textViewRxt = findViewById(R.id.text_view_Rxt);
        textViewRyt = findViewById(R.id.text_view_Ryt);
        textViewRzt = findViewById(R.id.text_view_Rzt);
*/
        textViewDis = findViewById(R.id.distance);
        period = findViewById(R.id.period);

        //SystemClock.elapsedRealtimeNanos()はスマホが起動してからの時間を表示する，単位はナノ秒
        period.setText((SystemClock.elapsedRealtimeNanos()/1e9)+"");
    }

    @Override
    protected void onResume(){
        //Activityが表示されたときに実行される部分
        super.onResume();
        //getDefaultSensorは加速度センサがあるかどうか判断している
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor rotation = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //イベント（ここでは加速度を計測したことを指す）が発生した際にイベントを受け取るListenerの登録
        //取得間隔はマイクロ秒単位で設定できるらしいが，加速度センサは無理らしい（https://akihito104.hatenablog.com/entry/2013/07/22/013000）を参照
        //3月11日時点で何度やっても取得時間が0.2秒より大きくできない，0.2秒が最大？
        sensorManager.registerListener(this, accel, (int) Math.pow(10, 5));
        sensorManager.registerListener(this, magnet, (int) Math.pow(10, 5));
        sensorManager.registerListener(this, rotation, (int) Math.pow(10, 5));
    }

    @Override
    protected void onPause() {
        //別のアクティビティが開始されるときに実行される部分
        super.onPause();
        //Listenerの解除
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        //ここは頻繁に呼び出されるので処理は簡単にする
        //計算はここの外側で行う
        //センサーのイベントを処理する部分
        float sensorAx, sensorAy, sensorAz;
        float sensorMx, sensorMy, sensorMz;
        float sensorRx, sensorRy, sensorRz;
        //int timeStamp;

        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                sensorAx = event.values[0];
                sensorAy = event.values[1];
                sensorAz = event.values[2];

                textViewAx.setText(String.format("%.2f",sensorAx));
                textViewAy.setText(String.format("%.2f",sensorAy));
                textViewAz.setText(String.format("%.2f",sensorAz));

                //calculateDistance(sensorAx,sensorAy,sensorAz, event.timestamp);
                calculateDistance(sensorAx, sensorAy, sensorAz, event.timestamp);
/*
                int sense_time = (int) (event.timestamp - before_time);
                before_time = (int) event.timestamp;


                textViewAxt.setText(sense_time + "");
                textViewAyt.setText(sense_time + "");
                textViewAzt.setText(sense_time + "");
*/

                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                sensorMx = event.values[0];
                sensorMy = event.values[1];
                sensorMz = event.values[2];

                textViewMx.setText(sensorMx + "");
                textViewMy.setText(sensorMy + "");
                textViewMz.setText(sensorMz + "");

                //calculateDistance(sensorMx,sensorMy,sensorMz, event.timestamp);

/*
                textViewMxt.setText(event.timestamp + "");
                textViewMyt.setText(event.timestamp + "");
                textViewMzt.setText(event.timestamp + "");

*/
               //calPeriod(event);
                //event.timestampはeventが呼び出されたときの時間を出す．単位はナノ秒，
                //eventが呼び出されたときの時間(SystemClock.elapsedRealtimeNanos())を表示している
                //そのため，呼び出されたときの時間間隔を取る際の初期値はSystemClock.elapsedRealtimeNanos()を基準にして計算したらおｋ
                //period.setText(event.timestamp + "");
                break;
            case Sensor.TYPE_GYROSCOPE:
                sensorRx = event.values[0];
                sensorRy = event.values[1];
                sensorRz = event.values[2];

                textViewRx.setText(sensorRx + "");
                textViewRy.setText(sensorRy + "");
                textViewRz.setText(sensorRz + "");

                //calculateDistance(sensorRx,sensorRy,sensorRz, event.timestamp);
/*
                textViewRxt.setText(event.timestamp + "");
                textViewRyt.setText(event.timestamp + "");
                textViewRzt.setText(event.timestamp + "");
 */
                break;
        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //センサー精度の変更を行うときに利用するメソッド
    }

    public long calPeriod(SensorEvent event){

        long period = after - before;
        long before = event.timestamp;
        return period;
    }

    private long before_time = (long) (SystemClock.elapsedRealtimeNanos()/Math.pow(10,9));
    private float before_accX = 0;
    private float before_accY = 0;
    private float before_accZ = 0;
    private float before_accel_3ax = 0;
    private float before_speed = 0;
    private float accel_3ax = 0;
    private float speed = 0;
    private float distance = 0;


    public void calculateDistance(float accX, float accY, float accZ, long time){
        //センサの取得間隔
        float dif_time = (float) ((time - before_time) / Math.pow(10,9));
        //3軸の加速度
        //long accel_3ax = (long) Math.sqrt(Math.pow(accX - before_accX, 2) + Math.pow(accY - before_accY, 2) + Math.pow(accZ - before_accZ, 2));
        //速度計算
        speed = (before_accX + accX) * dif_time / 2;
        //距離計算
        distance += (before_speed + speed) * dif_time / 2;

        //時間間隔のテストしたやつ
        //float test = (float) ((time - before_time)/Math.pow(10, 9));
        //画面に表示
        textViewDis.setText(dif_time + " / " + accX + " / " + before_accX + " / " + distance);

        //値の更新
        before_time = time;
        before_accel_3ax = accel_3ax;
        before_speed = speed;
        before_accX = accX;

    }

}