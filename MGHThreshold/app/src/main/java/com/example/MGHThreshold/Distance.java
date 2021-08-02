package com.example.MGHThreshold;

import java.util.Queue;

public class Distance {
    private double integralVel = 0;
    private double integralDis = 0;
    private double t = 0;
    private double v = 0;
    private double d = 0;

    //4次のルンゲクッタで距離を求める
    public double rungeKutta(double[] accel, float time){
        //float ax = accel[0];
        double ax = accel[0];

        double k1V = ax;
        double k2V = (v + time/2d * k1V) / (t + time/2d);
        double k3V = (v + time/2d * k1V) / (t + time/2d);
        double k4V = (v * k3V) / (t + time);
        double v2  = v + (k1V + 2 * k2V + 2 * k3V + k4V) * time / 6d;

        double k1D = v2;
        double k2D = (d + time/2d * k1V) / (t + time/2d);
        double k3D = (d + time/2d * k1V) / (t + time/2d);
        double k4D = (d * k3V) / (t + time);
        double d2  =  d + (k1D + 2 * k2D + 2 * k3D + k4D) * time / 6d;

        integralDis += d2;

        return integralDis;

    }

    public double simpson4point(Queue<Double> queue, float h){
        Double[] array = queue.toArray(new Double[3]);
        return (array[0] + 3*array[1] + 3*array[2] + array[3]) * h / 8d;
    }

    public double daikei(Queue<Double> queue, float h) {
        Double[] array = queue.toArray(new Double[1]);
        return (array[0] + array[1]) * h / 2d;
    }

    public double simpson3point(Queue<Double> queue, float h){
        Double[] array = queue.toArray(new Double[2]);
        return (array[0] + 4*array[1] + array[2]) * h /6d;
    }
}