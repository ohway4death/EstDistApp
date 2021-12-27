package com.example.MGHFver5;

import java.util.Arrays;

public class FixedSizeLongQueue extends FixedSizeQueue{
    Long[] queue;
    int size = 2;

    /*コンストラクタ*/
    public FixedSizeLongQueue(int n){
        super(2);
        this.size = n;
        this.queue = new Long[this.size];

        /*
        //ver3での初期化
        for(int i=0;i<this.size;i++){
            this.queue[i] = 0L;
        }
        */
        //ver4での初期化
        this.queue[0] = 0L;
    }

    void add(long num){
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

    void clear(){
        /*
        for(int i=0;i<this.size;i++){
            this.queue[i] = 0L;
        }
        */
        this.queue = new Long[this.size];
        this.queue[0] = 0L;
    }

    long longSearchLast(){
        int i;
        long ans = 0L;
        for (i=this.size-1;i>-1;i--){
            if (this.queue[i] != null){
                ans = this.queue[i];
                break;
            }
        }
        return ans;
    }

    void deleteHead(int count){
        this.queue = Arrays.copyOfRange(this.queue, count, count+this.size);
        //System.arraycopy(this.queue,count,newQueue,0,this.queue.length-count);
    }

}
