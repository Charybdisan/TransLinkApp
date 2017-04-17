/**
 * TransLink Bus App
 * Main2Activity.java
 * Purpose: Storing bus stop favorites and quick-launching
 * @author Taylor Lino
 * @version 1.0 2/17/2017.
 */

package com.charyb.translinkstatus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {

    public final String PREFS_NAME = "PREFERENCES";

    EditText editStop;
    LinearLayout busList;
    private Inflate inflater;
    public final String FILE_BUSES = "buses";

    class SavedBus{
        String name = "";
        String stop = "";
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        inflater = new Inflate(this);

        editStop = (EditText) findViewById(R.id.busStop);
        busList = (LinearLayout) findViewById(R.id.savedBusList);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //Check when the BACK button is pressed
        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        //Check when the ADD button is pressed
        Button addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                String busStopStr = editStop.getText().toString();
                if(!busStopStr.isEmpty()) {
                    editStop.setText("");
                    final LinearLayout savedBus = inflater.newSavedBus("", busStopStr);
                    busList.addView(savedBus);

                    try {
                        saveBuses();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }
        });

        //Load the buses if they exist
        loadBuses();

    }

    /**
     * Go back to the Main activity
     */
    private void goBack(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Load favorite bus stops from shared preferences
     */
    public void loadBuses(){

        ArrayList<SavedBus> savedArrayList = null;

        Gson gson = new Gson();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String json = settings.getString("SavedBuses", "");

        //Extract ArrayList of SavedBus from shared preferences via Json string
        savedArrayList = gson.fromJson(json, new TypeToken<ArrayList<SavedBus>>() {}.getType());

        if(savedArrayList != null) {
            for (SavedBus bus : savedArrayList) {

                final LinearLayout savedBus = inflater.newSavedBus(bus.name, bus.stop);
                busList.addView(savedBus);
            }
        }



    }

    /**
     * Save list of bus stop favorites to shared preferences
     * @throws IOException
     */
    public void saveBuses() throws IOException {
        ArrayList<SavedBus> savedBuses = new ArrayList<SavedBus>();

        final int childcount = busList.getChildCount();
        for (int i = 0; i < childcount; i++) {
            LinearLayout v = (LinearLayout)busList.getChildAt(i);
            LinearLayout ll = (LinearLayout)v.getChildAt(0);
            SavedBus bus = new SavedBus();
            //Get name
            EditText etxt = (EditText)ll.getChildAt(0);
            bus.name = etxt.getText().toString();
            //Get bus stop
            FrameLayout fl = (FrameLayout)ll.getChildAt(1);
            LinearLayout lfl = (LinearLayout)fl.getChildAt(1);
            TextView tlfl = (TextView)lfl.getChildAt(1);
            bus.stop = tlfl.getText().toString();

            savedBuses.add(bus);
        }

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor prefsEditor = settings.edit();
        Gson gson = new Gson();
        String json = gson.toJson(savedBuses);
        prefsEditor.putString("SavedBuses", json);
        prefsEditor.commit();

    }
}
