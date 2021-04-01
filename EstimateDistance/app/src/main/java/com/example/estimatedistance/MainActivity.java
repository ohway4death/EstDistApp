package com.example.estimatedistance;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.widget.TextView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.Locale;


public class MainActivity extends AppCompatActivity
        implements SensorEventListener, View.OnClickListener{

    private SensorManager sensorManager;
    private Sensor accel;
    private TextView textViewTitle, textViewAx, textViewAy, textViewAz;
    private TextView textViewPeriod, textViewSpeed, textViewDistance;

    private LineChart mChart;
    private String[] labels = new String[]{
            "accelerationX",
            "speedX",
            "distanceX"};
    private int[] colors = new int[]{
            Color.BLUE,
            Color.GRAY,
            Color.MAGENTA};

    private boolean lineardata = true;
    private TextView textViewFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //アプリを起動した際に実行される部分
        //Activityが生成される
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 縦画面
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mChart = findViewById(R.id.chart);
        // インスタンス生成
        mChart.setData(new LineData());
        // no description text
        mChart.getDescription().setEnabled(false);
        // Grid背景色
        mChart.setDrawGridBackground(true);
        // 右側の目盛り
        mChart.getAxisRight().setEnabled(false);

        Button buttonStart = findViewById(R.id.button_start);
        buttonStart.setOnClickListener(this);

        Button buttonStop = findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(this);

        Button buttonChange = findViewById(R.id.button_change);
        buttonChange.setOnClickListener(this);

        Button buttonReset= findViewById(R.id.button_reset);
        buttonReset.setOnClickListener(this);


        //センサーサービスへの参照を取得する，SensorManagerクラスのインスタンスを作成する．
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        textViewTitle =findViewById(R.id.title_accel);
        textViewAx = findViewById(R.id.text_view_Ax);
        textViewAy = findViewById(R.id.text_view_Ay);
        textViewAz = findViewById(R.id.text_view_Az);

        textViewDistance = findViewById(R.id.distance);
        textViewSpeed = findViewById(R.id.speed);
        textViewPeriod = findViewById(R.id.period);

        textViewFilter = findViewById(R.id.filter);
        textViewFilter.setText("フィルタあり");

        //SystemClock.elapsedRealtimeNanos()はスマホが起動してからの時間を表示する，単位はナノ秒
        //period.setText((SystemClock.elapsedRealtimeNanos()/1e9)+"");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_start:
                sensorManager.registerListener(this, accel,
                        SensorManager.SENSOR_DELAY_NORMAL);
                break;
            case R.id.button_stop:
                sensorManager.unregisterListener(this);
                break;
            case R.id.button_change:
                sensorAx = 0;
                sensorAy = 0;
                sensorAz = 0;
                period = 0;
                speed = 0;
                distance = 0;
                if(lineardata){
                    lineardata = false;
                }
                else{
                    lineardata = true;
                }
                break;

            case R.id.button_reset:
                sensorAx = 0;
                sensorAy = 0;
                sensorAz = 0;
                period = 0;
                speed = 0;
                distance = 0;
                break;
        }
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

        float alpha = 0.1f;
        float[] linear_acceleration = new float[3];

        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                //ローパスフィルタ使用
                sensorAx = alpha * sensorAx_bef + (1-alpha) * event.values[0];
                sensorAy = alpha * sensorAy_bef + (1-alpha) * event.values[1];
                sensorAz = alpha * sensorAz_bef + (1-alpha) * event.values[2];

                /*
                sensorAx = event.values[0];
                sensorAy = event.values[1];
                sensorAz = event.values[2];
                */

                //（生データ）ー（処理後データ）＝ノイズ部分
                linear_acceleration[0] = event.values[0] - sensorAx;
                linear_acceleration[1] = event.values[1] - sensorAy;
                linear_acceleration[2] = event.values[2] - sensorAz;

                float values_filter[] = calculateDistance(sensorAx, sensorAy, sensorAz, event.timestamp);
                float values_origin[] = calculateDistance(event.values[0], event.values[1], event.values[2], event.timestamp);

                if(!lineardata){
                    //画面に表示
                    textViewPeriod.setText(String.valueOf(values_origin[3]));
                    textViewSpeed.setText(String.valueOf(values_origin[1]));
                    textViewDistance.setText(String.valueOf(values_origin[2]));
                    textViewAx.setText(String.format("%.3f",event.values[0]));
                    textViewAy.setText(String.format("%.3f",event.values[1]));
                    textViewAz.setText(String.format("%.3f",event.values[2]));
                    textViewTitle.setText("フィルタなし");
                }
                else {
                    //画面に表示
                    textViewPeriod.setText(String.valueOf(values_filter[3]));
                    textViewSpeed.setText(String.valueOf(values_filter[1]));
                    textViewDistance.setText(String.valueOf(values_filter[2]));
                    textViewAx.setText(String.format("%.3f",sensorAx));
                    textViewAy.setText(String.format("%.3f",sensorAy));
                    textViewAz.setText(String.format("%.3f",sensorAz));
                    textViewTitle.setText("フィルタあり");
                }



                LineData data = mChart.getLineData();

                if(data != null){
                    for(int i = 0; i < 3; i++){
                        ILineDataSet set3 = data.getDataSetByIndex(i);
                        if(set3 == null){
                            LineDataSet set = new LineDataSet(null, labels[i]);
                            set.setLineWidth(2.0f);
                            set.setColor(colors[i]);
                            // liner line
                            set.setDrawCircles(false);
                            // no values on the chart
                            set.setDrawValues(false);
                            set3 = set;
                            data.addDataSet(set3);
                        }

                        // data update
                        if(!lineardata){
                            data.addEntry(new Entry(set3.getEntryCount(), values_origin[i]), i);
                            textViewFilter.setText("処理前の加速度を使用");
                        }
                        else{
                            data.addEntry(new Entry(set3.getEntryCount(), values_filter[i]), i);
                            textViewFilter.setText("処理後の加速度を使用");
                        }

                        data.notifyDataChanged();
                    }

                    mChart.notifyDataSetChanged(); // 表示の更新のために変更を通知する
                    mChart.setVisibleXRangeMaximum(50); // 表示の幅を決定する
                    mChart.moveViewToX(data.getEntryCount()); // 最新のデータまで表示を移動させる
                }
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

    private long before_time = (long) (SystemClock.elapsedRealtimeNanos()/Math.pow(10,9));
    private float before_accX = 0;
    private float before_accY = 0;
    private float before_accZ = 0;
    private float before_accel_3ax = 0;
    private float before_speed = 0;
    private float accel_3ax = 0;
    private float speed = 0;
    private float distance = 0;
    private float period = 0;

    public float[] calculateDistance(float accX, float accY, float accZ, long time){
        //センサの取得間隔
        period = (float) ((time - before_time) / Math.pow(10,9));
        //小数点第2位で四捨五入
        //period = ((float)Math.round(period*10))/10;


        //小数点第2位で切り捨て
        BigDecimal bd1 = new BigDecimal(String.valueOf(before_accX));
        BigDecimal bd2 = new BigDecimal(String.valueOf(accX));
        BigDecimal bd1_after = bd1.setScale(1,RoundingMode.DOWN);
        BigDecimal bd2_after = bd2.setScale(1,RoundingMode.DOWN);

        //速度計算
        speed = ((float)bd1_after.doubleValue() + (float)bd2_after.doubleValue()) * period / 2;
        //speed = (before_accX + accX) * period / 2;
        //小数点第2位で四捨五入
        //speed = ((float)Math.round(speed*10))/10;

        //距離計算
        distance += (before_speed + speed) * period / 2;
        //小数点第2位で四捨五入
        //distance = ((float)Math.round(distance*10))/10;


        //値の更新
        before_time = time;
        before_speed = speed;
        before_accX = accX;

        float values[] = {accX, speed, distance, period};

        return values;
    }


}