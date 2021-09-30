package com.example.MGHFver3;

import android.util.Log;

public class FixedSizeLongQueue extends FixedSizeQueue{
    long[] queue;
    int size = 2;

    /*コンストラクタ*/
    public FixedSizeLongQueue(int n){
        super(2);
        this.size = n;
        this.queue = new long[this.size];
        for(int i=0;i<this.size;i++){
            this.queue[i] = 0L;
        }
    }

    void add(long num){
        if (this.queue.length == this.size){
            System.arraycopy(this.queue, 1, this.queue, 0, this.size - 1);
            this.queue[this.size - 1] = num;
        }else{
            Log.d("err", "add_error");
        }
    }

    void clear(){
        for(int i=0;i<this.size;i++){
            this.queue[i] = 0L;
        }
    }


}
