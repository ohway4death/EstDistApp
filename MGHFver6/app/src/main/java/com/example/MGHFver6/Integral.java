package com.example.MGHFver6;

import java.util.Deque;

public class Integral {
    private double integralVel = 0;
    private double integralDis = 0;
    private double t = 0;
    private double v = 0;
    private double d = 0;


    //台形法
    public double daikei(Deque<Double> queue, Deque<Long> time) {
        Double[] array = queue.toArray(new Double[1]);
        Long[] timeArray = time.toArray(new Long[2]);
        float h = diff_time(timeArray[1], timeArray[0]);
        return (array[0] + array[1]) * h / 2d;
    }

    //３点を使うシンプソン法
    /*
    public double simpson3point(FixedSizeQueue queue, FixedSizeLongQueue time){
        Double[] array = queue.toArray(new Double[2]);
        Long[] timeArray = time.toArray(new Long[2]);
        float h = diff_time(timeArray[2], timeArray[0]);
        return (array[0] + 4*array[1] + array[2]) * h /6d;
    }
     */
    public double simpson3point(FixedSizeQueue queue, FixedSizeLongQueue time){
        float h = diff_time(time.queue[2], time.queue[0]);
        return (queue.queue[0] + 4*queue.queue[1] + queue.queue[2]) * h /6d;
    }

    //4点を使うシンプソン法
    /*
    public double simpson4point(Deque<Double> queue, Deque<Long> time){
        Double[] array = queue.toArray(new Double[3]);
        Long[] timeArray = time.toArray(new Long[3]);
        float h = diff_time(timeArray[3], timeArray[0]);
        return (array[0] + 3*array[1] + 3*array[2] + array[3]) * h / 8d;
    }
     */

    public double simpson4point(FixedSizeQueue queue, FixedSizeLongQueue time){
        float h = diff_time(time.queue[3], time.queue[0]);
        return (queue.queue[0] + 3*queue.queue[1] + 3*queue.queue[2] + queue.queue[3]) * h / 8d;
    }

    //5点を使うシンプソン法
    public double bool(Deque<Double> queue, Deque<Long> time){
        Double[] array = queue.toArray(new Double[4]);
        Long[] timeArray = time.toArray(new Long[4]);
        float h = diff_time(timeArray[4], timeArray[0]);
        return (7*array[0] + 32*array[1] + 12*array[2] + 32*array[3] + 7*array[4]) * h / 90d;
    }

    public float diff_time(long time, long bef_time){
        return (time - bef_time) / 1000000000F;
    }

}