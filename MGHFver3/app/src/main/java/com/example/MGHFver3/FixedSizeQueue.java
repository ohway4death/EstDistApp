package com.example.MGHFver3;

import android.util.Log;

import java.util.Queue;

public class FixedSizeQueue{
    double[] queue;
    int size = 2;

    /*コンストラクタ*/
    public FixedSizeQueue(int n){
        this.size = n;
        this.queue = new double[this.size];
        for(int i=0;i<this.size;i++){
            this.queue[i] = 0;
        }
    }

    void add(double num){
        if (this.queue.length == this.size){
            System.arraycopy(this.queue, 1, this.queue, 0, this.size - 1);
            this.queue[this.size - 1] = num;
        }else{
            Log.d("err", "add_error");
        }
    }

    double getFirst(){
        return this.queue[0];
    }

    double getLast(){
        return this.queue[this.size - 1];
    }


}
