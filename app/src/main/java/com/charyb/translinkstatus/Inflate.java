/**
 * TransLink Bus App
 * Inflate.java
 * Purpose: Inflates xml files and extracts data from buses/saved bus favorite XML layouts
 * @author Taylor Lino
 * @version 1.0 2/17/2017.
 */

package com.charyb.translinkstatus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class Inflate {
    private LayoutInflater inflater;
    private AppCompatActivity activity;

    public Inflate(AppCompatActivity activity){
        this.activity = activity;
        inflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Bus with view layout data to be displayed and sorted on Main Activity
     * @param left Left text
     * @param right Right text
     * @return A new bus
     */
    public Bus newBus(String left, String right){
        FrameLayout frame = (FrameLayout)inflater.inflate(R.layout.bus, null);
        LinearLayout lin = (LinearLayout)frame.getChildAt(1);
        TextView leftSide = (TextView)lin.getChildAt(1);
        leftSide.setText(left);
        TextView rightSide = (TextView)lin.getChildAt(2);
        rightSide.setText(right);
        Bus bus = new Bus();
        bus.frame = frame;
        bus.leftSide = leftSide;
        bus.rightSide = rightSide;
        return bus;
    }

    /**
     * A saved bus favorite with view layout data to be displayed on Main 2 Activity
     * @param icon
     * @param busName
     * @param busStop
     * @return
     */
    public LinearLayout newSavedBus(String busName, String busStop){
        final LinearLayout base = (LinearLayout)inflater.inflate(R.layout.bus_saved, null);
        LinearLayout lin = (LinearLayout)base.getChildAt(0);
        EditText busNameText = (EditText)lin.getChildAt(0);
        busNameText.setText(busName);

        FrameLayout frame = (FrameLayout)lin.getChildAt(1);
        LinearLayout linFrame = (LinearLayout)frame.getChildAt(1);
        final TextView busStopText = (TextView)linFrame.getChildAt(1);
        busStopText.setText(busStop);

        busNameText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                Main2Activity activ = (Main2Activity) activity;
                try {
                    activ.saveBuses();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        frame.setOnClickListener(new FrameLayout.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent i=new Intent(view.getContext(), MainActivity.class);
                i.putExtra("stop", busStopText.getText().toString());
                activity.startActivity(i);
                activity.finish();
            }
        });

        final LinearLayout busList = (LinearLayout)activity.findViewById(R.id.savedBusList);
        Button delButton = (Button)lin.getChildAt(2);
        delButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {

                busList.removeView(base);

                Main2Activity activ = (Main2Activity) activity;
                try {
                    activ.saveBuses();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

        return base;
    }


}
