package com.example.MadgwickGravityHandleFile;

import java.util.Queue;

public class Distance {
    private double integralVel = 0;
    private double integralDis = 0;
    private double t = 0;
    private double v = 0;
    private double d = 0;


    //台形法
    public double daikei(Queue<Double> queue, float h) {
        Double[] array = queue.toArray(new Double[1]);
        return (array[0] + array[1]) * h / 2d;
    }

    //３点を使うシンプソン法
    public double simpson3point(Queue<Double> queue, float h){
        Double[] array = queue.toArray(new Double[2]);
        return (array[0] + 4*array[1] + array[2]) * h /6d;
    }

    //4点を使うシンプソン法
    public double simpson4point(Queue<Double> queue, float h){
        Double[] array = queue.toArray(new Double[3]);
        return (array[0] + 3*array[1] + 3*array[2] + array[3]) * h / 8d;
    }

}