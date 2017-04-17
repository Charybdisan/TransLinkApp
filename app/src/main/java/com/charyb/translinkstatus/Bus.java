/**
 * TransLink Bus App
 * Bus.java
 * Purpose: Allows sorting by number of minutes and stores time and text concerning Bus information
 * @author Taylor Lino
 * @version 1.0 2/17/2017.
 */


package com.charyb.translinkstatus;

import android.widget.FrameLayout;
import android.widget.TextView;


public class Bus implements Comparable {
    FrameLayout frame;

    TextView leftSide;
    TextView rightSide;

    String time;

    int minutes = 0;

    public void processTime(){
        if(!time.equals("")){
            minutes = Integer.parseInt(time);
        }
    }

    @Override
    public int compareTo(Object o) {
        if(this.minutes > ((Bus)o).minutes){
            return 1;
        }
        else return -1;
    }
}
