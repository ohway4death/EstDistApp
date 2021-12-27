package com.example.MGHFver4;

import android.util.Log;

import java.util.Arrays;

public class FixedSizeQueue{
    Double[] queue;
    int size = 2;

    /*コンストラクタ*/
    public FixedSizeQueue(int n){
        this.size = n;
        this.queue = new Double[this.size];
        /*
        //ver3での初期化
        for(int i=0;i<this.size;i++){
            this.queue[i] = 0d;
        }
        */
        //ver4での初期化
        this.queue[0] = 0d;
    }

    void add(double num){
        if (this.queue[this.size-1] != null){
            System.arraycopy(this.queue, 1, this.queue, 0, this.size - 1);
            this.queue[this.size - 1] = num;
        }else{
            int i;
            for (i=0;i<this.size;i++){
                if (this.queue[i]==null){
                    this.queue[i] = num;
                    break;
                }
            }
            //Log.d("err", "add_error");
        }
    }

    double getFirst(){
        return this.queue[0];
    }

    double getLast(){
        return this.queue[this.size - 1];
    }

    double selectData(int num){ return this.queue[num-1]; }

    int size(){
        int i;
        int j=0;
        for (i=0;i<this.size;i++){
            if (this.queue[i] != null){
                j++;
            }
        }
        return j;
    }

    void clear(){
        /*
        for(int i=0;i<this.size;i++){
            this.queue[i] = 0d;
        }
        */
        this.queue = new Double[this.size];
        this.queue[0] = 0D;
    }

    void deleteHead(int count){
        this.queue = Arrays.copyOfRange(this.queue, count, count+this.size);
        //System.arraycopy(this.queue,count,newQueue,0,this.queue.length-count);
    }

    double searchLast(){
        int i;
        for (i=this.size-1;i>=0;i--){
            if (this.queue[i] != null){
                break;
            }
        }
        return this.queue[i];
    }

}
