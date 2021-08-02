package com.example.CheckSamplingTime;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor accel,mag,gyro;
    private TextView textViewTitle, textViewAx, textViewAy, textViewAz;
    private TextView textViewMx, textViewMy, textViewMz;
    private TextView textViewGx, textViewGy, textViewGz;
    private TextView textViewPeriod, textViewSpeed, textViewDistance;
    private TextView textViewRoll, textViewPitch, textViewYaw;
    private long time;

    private File fileA, fileM, fileG;
    private FileWriter fwA, fwM, fwG;
    private PrintWriter pwA, pwM, pwG;

    private File file;
    private FileWriter fw;
    private PrintWriter pw;
    private Context context;


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

        //SystemClock.elapsedRealtimeNanos()はスマホが起動してからの時間を表示する，単位はナノ秒
        //period.setText((SystemClock.elapsedRealtimeNanos()/1e9)+"");

        time = SystemClock.elapsedRealtimeNanos();

        context = getApplicationContext();
        /*
        String fileNameA = "CheckSamplingTimeZeroAccel.csv";
        String fileNameM = "CheckSamplingTimeZeroMagnet.csv";
        String fileNameG = "CheckSamplingTimeZeroGyro.csv";
        fileA = new File(context.getFilesDir(), fileNameA);
        fileM = new File(context.getFilesDir(), fileNameM);
        fileG = new File(context.getFilesDir(), fileNameG);
        */
        String fileName = "CheckSamplingTimeFastestOneSensor.csv";
        file = new File(context.getFilesDir(), fileName);
        try{
        /*
            //引数はファイル名と書き込まれたデータを追加するかどうか決める、trueならファイルの最後に追記していく。
            fwA = new FileWriter(fileA, true);
            pwA = new PrintWriter(new BufferedWriter(fwA));

            //ヘッダーを作成する
            pwA.print("timeStamp");
            pwA.print(",");
            pwA.print("X");
            pwA.print(",");
            pwA.print("Y");
            pwA.print(",");
            pwA.print("Z");
            pwA.println();

            fwM = new FileWriter(fileM, true);
            pwM = new PrintWriter(new BufferedWriter(fwM));

            //ヘッダーを作成する
            pwM.print("timeStamp");
            pwM.print(",");
            pwM.print("X");
            pwM.print(",");
            pwM.print("Y");
            pwM.print(",");
            pwM.print("Z");
            pwM.println();

            fwG = new FileWriter(fileG, true);
            pwG = new PrintWriter(new BufferedWriter(fwG));

            //ヘッダーを作成する
            pwG.print("timeStamp");
            pwG.print(",");
            pwG.print("X");
            pwG.print(",");
            pwG.print("Y");
            pwG.print(",");
            pwG.print("Z");
            pwG.println();
            */

            //引数はファイル名と書き込まれたデータを追加するかどうか決める、trueならファイルの最後に追記していく。
            fw = new FileWriter(file, true);
            pw = new PrintWriter(new BufferedWriter(fw));

            //ヘッダーを作成する
            pw.print("SensorType");
            pw.print(",");
            pw.print("timeStamp");
            pw.print(",");
            pw.print("X");
            pw.print(",");
            pw.print("Y");
            pw.print(",");
            pw.print("Z");
            pw.println();

        }catch (IOException e){
            e.printStackTrace();
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
        sensorManager.registerListener(this, accel,0);
        sensorManager.registerListener(this, mag, 0);
        sensorManager.registerListener(this, gyro, 0);

    }

    @Override
    protected void onPause() {
        //別のアクティビティが開始されるときに実行される部分
        super.onPause();
        //Listenerの解除
        sensorManager.unregisterListener(this);

        //ファイルを閉じる
        /*
        pwA.close();
        pwM.close();
        pwG.close();
         */
        pw.close();
        //Toast.makeText(context, "file close", Toast.LENGTH_LONG).show();
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

                pw.print("ACCEL");
                pw.print(",");

                break;

            case Sensor.TYPE_MAGNETIC_FIELD:

                sensorMagnet[0] = event.values[0];
                sensorMagnet[1] = event.values[1];
                sensorMagnet[2] = event.values[2];

                textViewMx.setText(String.format("%.3f",sensorMagnet[0]));
                textViewMy.setText(String.format("%.3f",sensorMagnet[1]));
                textViewMz.setText(String.format("%.3f",sensorMagnet[2]));

                pw.print("MAGNET");
                pw.print(",");

                break;

            case Sensor.TYPE_GYROSCOPE:

                sensorGyro[0] = event.values[0];
                sensorGyro[1] = event.values[1];
                sensorGyro[2] = event.values[2];

                textViewGx.setText(String.format("%.3f",sensorGyro[0]));
                textViewGy.setText(String.format("%.3f",sensorGyro[1]));
                textViewGz.setText(String.format("%.3f",sensorGyro[2]));

                pw.print("GYRO");
                pw.print(",");

                break;
        }

        pw.print(event.timestamp);
        pw.print(",");
        pw.print(event.values[0]);
        pw.print(",");
        pw.print(event.values[1]);
        pw.print(",");
        pw.print(event.values[2]);
        pw.println();

/*
        pw.print(event.timestamp);
        pw.print(",");
        pw.print(sensorAccel[0]);
        pw.print(",");
        pw.print(sensorAccel[1]);
        pw.print(",");
        pw.print(sensorAccel[2]);
        pw.print(",");
        pw.print(sensorMagnet[0]);
        pw.print(",");
        pw.print(sensorMagnet[1]);
        pw.print(",");
        pw.print(sensorMagnet[2]);
        pw.print(",");
        pw.print(sensorGyro[0]);
        pw.print(",");
        pw.print(sensorGyro[1]);
        pw.print(",");
        pw.print(sensorGyro[2]);
        pw.println();
 */


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //センサー精度の変更を行うときに利用するメソッド
    }

}