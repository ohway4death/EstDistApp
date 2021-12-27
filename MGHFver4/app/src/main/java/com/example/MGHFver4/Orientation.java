package com.example.MGHFver4;

class Orientation {
    //初期方位推定値（サンプルプログラムを参考にしている）
    float q0 = 1;
    float q1 = 0;
    float q2 = 0;
    float q3 = 0;

   //論文と同じ値に設定している
   float beta = 0.41f;

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

        //ロール、ピッチ、ヨー角
        float roll, pitch, yaw;

        //サンプリング周波数の逆数
        float invSampleFreq = 1.0f / sampleFreq;

       //地磁気代入
       mx = mag[0];
       my = mag[1];
       mz = mag[2];

       if((mx==0.0f) && (my==0.0f) && (mz==0.0f)){
           return madgwickFilterNoMag(accel, gyro, sampleFreq);
       }

        //加速度代入
       ax = accel[0];
       ay = accel[1];
       az = accel[2];

       //ジャイロ代入(ジャイロの単位はrad/s)
       gx = gyro[0];
       gy = gyro[1];
       gz = gyro[2];

       //ジャイロから求めたクォータニオンの変化率　(11)式より
       qDot1 = 0.5f * (-q1 * gx - q2 * gy - q3 * gz);
       qDot2 = 0.5f * (q0 * gx + q2 * gz - q3 * gy);
       qDot3 = 0.5f * (q0 * gy - q1 * gz + q3 * gx);
       qDot4 = 0.5f * (q0 * gz + q1 * gy - q2 * gx);

       if(!((ax==0.0f) && (ay==0.0f) && (az==0.0f))){
           //加速度正規化
           recipNorm = invSqrt(ax*ax + ay*ay + az*az);
           ax *= recipNorm;
           ay *= recipNorm;
           az *= recipNorm;

           //地磁気正規化
           recipNorm = invSqrt(mx*mx + my*my + mz*mz);
           mx *= recipNorm;
           my *= recipNorm;
           mz *= recipNorm;

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

           //変化率の更新(33)式？
           qDot1 -= beta * s0;
           qDot2 -= beta * s1;
           qDot3 -= beta * s2;
           qDot4 -= beta * s3;
       }

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

    public float[] madgwickFilterNoMag (float accel[], float gyro[], float sampleFreq){
        float recipNorm;
        float s0,s1,s2,s3;
        float qDot0,qDot1,qDot2,qDot3;

        float ax = accel[0];
        float ay = accel[1];
        float az = accel[2];
        float gx = gyro[0];
        float gy = gyro[1];
        float gz = gyro[2];

       float invSampleFreq = 1.0f / sampleFreq;

        qDot0 = 0.5f * (-q1 * gx - q2 * gy - q3 * gz);
        qDot1 = 0.5f * (q0 * gx + q2 * gz - q3 * gy);
        qDot2 = 0.5f * (q0 * gy - q1 * gz + q3 * gx);
        qDot3 = 0.5f * (q0 * gz + q1 * gy - q2 * gx);

       if(!((ax==0.0f) && (ay==0.0f) && (az==0.0f))){
           recipNorm = invSqrt(ax * ax + ay * ay + az * az);
           ax *= recipNorm;
           ay *= recipNorm;
           az *= recipNorm;

           float _2q0 = 2f * q0;
           float _2q1 = 2f * q1;
           float _2q2 = 2f * q2;
           float _2q3 = 2f * q3;
           float _4q0 = 4f * q0;
           float _4q1 = 4f * q1;
           float _4q2 = 4f * q2;
           float _8q1 = 8f * q1;
           float _8q2 = 8f * q2;
           float q0q0 = q0 * q0;
           float q1q1 = q1 * q1;
           float q2q2 = q2 * q2;
           float q3q3 = q3 * q3;

           // Gradient decent algorithm corrective step
           s0 = _4q0 * q2q2 + _2q2 * ax + _4q0 * q1q1 - _2q1 * ay;
           s1 = _4q1 * q3q3 - _2q3 * ax + 4f * q0q0 * q1 - _2q0 * ay - _4q1 + _8q1
                   * q1q1 + _8q1 * q2q2 + _4q1 * az;
           s2 = 4f * q0q0 * q2 + _2q0 * ax + _4q2 * q3q3 - _2q3 * ay - _4q2 + _8q2
                   * q1q1 + _8q2 * q2q2 + _4q2 * az;
           s3 = 4f * q1q1 * q3 - _2q1 * ax + 4f * q2q2 * q3 - _2q2 * ay;
           recipNorm = invSqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3);
           s0 *= recipNorm;
           s1 *= recipNorm;
           s2 *= recipNorm;
           s3 *= recipNorm;

           // Compute rate of change of quaternion
           qDot0 -= beta * s0;
           qDot1 -= beta * s1;
           qDot2 -= beta * s2;
           qDot3 -= beta * s3;

       }

       // Integrate to yield quaternion
       q0 += qDot0 * invSampleFreq;
       q1 += qDot1 * invSampleFreq;
       q2 += qDot2 * invSampleFreq;
       q3 += qDot3 * invSampleFreq;
       recipNorm = invSqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);

       // quaternion
       q0 *= recipNorm;
       q1 *= recipNorm;
       q2 *= recipNorm;
       q3 *= recipNorm;

       //サンプルプログラム通り
       float roll = (float) Math.toDegrees(Math.atan2(q0*q1 + q2*q3, 0.5f - q1*q1 - q2*q2));
       float pitch = (float) Math.toDegrees(Math.asin (-2.0f * (q1*q3 - q0*q2)));
       float yaw = (float) Math.toDegrees(Math.atan2(q1*q2 + q0*q3, 0.5f - q2*q2 - q3*q3)) + 180.0f;

       //角度を返す
       float angle[] = {roll, pitch, yaw};

       return angle;

    }

    //平方根の逆数を求める関数
    private float invSqrt(float x) {
        int i = Float.floatToRawIntBits(x);
        float y = Float.intBitsToFloat(0x5f3759df - (i >> 1));
        return y * (1.5F - 0.5F * x * y * y);


    }

}
