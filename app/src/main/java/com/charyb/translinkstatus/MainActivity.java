/**
 * TransLink Bus App
 * MainActivity.java
 * Purpose: Displays the main activity (Incoming buses and times)
 * Test Bus Stop: http://api.translink.ca/rttiapi/v1/buses?apikey=(API-KEY-HERE)
 * References: https://developer.translink.ca/ServicesRtti/ApiReference#Buses
 *
 * @author Taylor Lino
 * @version 1.0 2/17/2017.
 */

package com.charyb.translinkstatus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.LinearLayout;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.appindexing.AppIndex;

import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.util.Collections;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeContainer;
    private ScrollView scroll;
    private LinearLayout ll;

    public final String API_KEY = "INSERT_API_KEY_HERE";
    public final String PREFS_NAME = "PREFERENCES";

    private ArrayList<Bus> buses = new ArrayList<Bus>();
    private Inflate inflater;

    private boolean fetching = false;
    private GoogleApiClient client;

    @Override
    public void onBackPressed() {

    }

    /**
     * Creation of the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inflater = new Inflate(this);

        //Check when EditText is being typed in to stop refresh
        EditText editStop = (EditText) findViewById(R.id.busStop);
        editStop.setOnKeyListener(new EditText.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                stopFetching();
                return false;
            }
        });

        //Save the current bus stop for future use after typing
        editStop.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                System.out.println("NEW STR: " + editable.toString());
                if(!editable.toString().isEmpty()) {
                    //Save the new bus stop to preferences for automatic loading
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("busStopNumber", editable.toString());
                    editor.apply();
                }

            }
        });

        //Check when the SAVED BUSES button is pressed
        Button savedBusesBut = (Button) findViewById(R.id.savedBuses);
        savedBusesBut.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), Main2Activity.class);
                view.getContext().startActivity(intent);
                finish();
            }
        });

        //Check when the CHECK button is pressed to initiate refreshing
        Button checkBut = (Button) findViewById(R.id.checkButton);
        checkBut.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchTransLinkInfo();
            }
        });

        //Check when we swipe down to start refreshing via swipe-down
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //Swipe Refresh Listener
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTransLinkInfo();
            }
        });


        scroll = (ScrollView) findViewById(R.id.scroll);
        ll = (LinearLayout) scroll.getChildAt(0);

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        //Don't get from settings by default
        boolean getFromSettings = false;
        Bundle extras = getIntent().getExtras();
        //If we have a bus stop set in settings, extract it
        //and set it to the current bus stop we want
        //--
        //May also be called due to the other activity
        //requesting it
        if (extras != null) {
            String value = extras.getString("stop");
            if(value == null) getFromSettings = true;
            else if(value.isEmpty()) getFromSettings = true;
            else {
                editStop.setText(value);
                fetchTransLinkInfo();
            }
        }
        else{
            getFromSettings = true;
        }
        if(getFromSettings){
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            String stopPref = settings.getString("busStopNumber","NONE");
            if(!stopPref.equals("NONE")){
                editStop.setText(stopPref);
                getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                );

                fetchTransLinkInfo();
            }

        }

    }

    /**
     * Fetch the incoming buses for the current bus stop
     */
    public void fetchTransLinkInfo() {
        if (!fetching) {
            fetching = true;
            swipeContainer.setRefreshing(true);
            EditText editStop = (EditText) findViewById(R.id.busStop);
            editStop.clearFocus();

            //Close keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editStop.getWindowToken(), 0);

            String editStopStr = editStop.getText().toString();
            int stop = -1;
            if (!editStopStr.isEmpty()) stop = Integer.parseInt(editStopStr);

            //=============================================================
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "http://api.translink.ca/rttiapi/v1/stops/" + stop + "/estimates?apikey=" + API_KEY;
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            buses.clear();

                            try {
                                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                                        .parse(new InputSource(new StringReader(response)));
                                NodeList busNodes = doc.getElementsByTagName("Schedule");

                                for (int i = 0; i < busNodes.getLength(); i++) {
                                    Node curBus = busNodes.item(i);
                                    NodeList curBusChilds = curBus.getChildNodes();

                                    String busDest = "", busTime = "";

                                    for (int j = 0; j < curBusChilds.getLength(); j++) {
                                        Node curNode = curBusChilds.item(j);
                                        if (curNode.getNodeName().equals("Destination")) {

                                            busDest = curNode.getTextContent();
                                        } else if (curNode.getNodeName().equals("ExpectedCountdown")) {
                                            busTime = curNode.getTextContent();
                                        }
                                    }
                                    System.out.println("Destination: " + busDest + " Time: " + busTime);
                                    Bus bus = inflater.newBus(busDest, busTime + " minutes");
                                    bus.time = busTime;
                                    bus.processTime();

                                    if (bus.minutes >= 0)
                                        buses.add(bus);

                                }


                            } catch (SAXException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ParserConfigurationException e) {
                                e.printStackTrace();
                            }

                            processBuses();
                            stopFetching();

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            stopFetching();
                            buses.clear();
                            ll.removeAllViews();
                        }
                    }
            );
            queue.add(request);

            //=============================================================


            /// /Log.println(Log.INFO, "Stop:", Integer.toString(stop));
            //swipeContainer.setRefreshing(false);
        }

    }

    /**
     * Process buses by clearing previous ones and sorting them by closest times
     */
    public void processBuses() {
        //Clear previous ones
        ll.removeAllViews();

        //Sort buses
        Collections.sort(buses);
        //Process new buses
        for (Bus bus : buses) {
            ll.addView(bus.frame);
        }
    }

    /**
     * Cancel fetching of buses / refreshing
     */
    public void stopFetching() {
        fetching = false;
        swipeContainer.setRefreshing(false);
    }

}
