package com.example.lowpassfilter;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;



public class MainActivity extends AppCompatActivity
        implements SensorEventListener, View.OnClickListener{

    private SensorManager sensorManager;
    private Sensor accel;
    private TextView  textViewAxBef, textViewAyBef, textViewAzBef;
    private TextView textViewAxAft, textViewAyAft, textViewAzAft;

    private LineChart mChart;
    private String[] labels = new String[]{
            "accelerationX",
            "accelerationY",
            "accelerationZ"};
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
        textViewAxBef = findViewById(R.id.text_view_Ax_bef);
        textViewAyBef = findViewById(R.id.text_view_Ay_bef);
        textViewAzBef = findViewById(R.id.text_view_Az_bef);

        textViewAxAft = findViewById(R.id.text_view_Ax_aft);
        textViewAyAft = findViewById(R.id.text_view_Ay_aft);
        textViewAzAft = findViewById(R.id.text_view_Az_aft);

        textViewFilter = findViewById(R.id.filter);
        textViewFilter.setText("フィルタあり");

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
                if(lineardata){
                    lineardata = false;
                }
                else{
                    lineardata = true;
                }
                break;

            case R.id.button_reset:
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

    float sensorAx_bef = 0;
    float sensorAy_bef = 0;
    float sensorAz_bef = 0;

    @Override
    public void onSensorChanged(SensorEvent event){
        //ここは頻繁に呼び出されるので処理は簡単にする
        //計算はここの外側で行う
        //センサーのイベントを処理する部分

        float alpha = 0.1f;
        float[] values_filter = new float[3];

        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                //ローパスフィルタ使用
                values_filter[0] = alpha * sensorAx_bef + (1-alpha) * event.values[0];
                values_filter[1] = alpha * sensorAy_bef + (1-alpha) * event.values[1];
                values_filter[2] = alpha * sensorAz_bef + (1-alpha) * event.values[2];

                /*
                sensorAx = event.values[0];
                sensorAy = event.values[1];
                sensorAz = event.values[2];
                */

                //（生データ）ー（処理後データ）＝ノイズ部分
                //linear_acceleration[0] = event.values[0] - sensorAx;
                //linear_acceleration[1] = event.values[1] - sensorAy;
                //linear_acceleration[2] = event.values[2] - sensorAz;
                textViewAxBef.setText(String.valueOf(event.values[0]));
                textViewAyBef.setText(String.valueOf(event.values[1]));
                textViewAzBef.setText(String.valueOf(event.values[2]));
                textViewAxAft.setText(String.valueOf(values_filter[0]));
                textViewAyAft.setText(String.valueOf(values_filter[1]));
                textViewAzAft.setText(String.valueOf(values_filter[2]));

                if(!lineardata){
                    //画面に表示
                    textViewFilter.setText("フィルタなし");
                }
                else {
                    //画面に表示
                    textViewFilter.setText("フィルタなし");
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
                            data.addEntry(new Entry(set3.getEntryCount(), event.values[i]), i);
                            textViewFilter.setText("フィルタなし");
                        }
                        else{
                            data.addEntry(new Entry(set3.getEntryCount(), values_filter[i]), i);
                            textViewFilter.setText("フィルタあり");
                        }

                        data.notifyDataChanged();
                    }

                    mChart.notifyDataSetChanged(); // 表示の更新のために変更を通知する
                    mChart.setVisibleXRangeMaximum(50); // 表示の幅を決定する
                    mChart.moveViewToX(data.getEntryCount()); // 最新のデータまで表示を移動させる
                }

                sensorAx_bef = values_filter[0];
                sensorAy_bef = values_filter[1];
                sensorAz_bef = values_filter[2];

                break;
        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //センサー精度の変更を行うときに利用するメソッド
    }

}