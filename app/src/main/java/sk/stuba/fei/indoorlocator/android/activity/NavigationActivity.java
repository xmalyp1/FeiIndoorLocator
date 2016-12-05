package sk.stuba.fei.indoorlocator.android.activity;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import sk.stuba.fei.indoorlocator.R;
import sk.stuba.fei.indoorlocator.android.adapter.NavigationAdapter;
import sk.stuba.fei.indoorlocator.database.DatabaseHelper;
import sk.stuba.fei.indoorlocator.database.DatabaseManager;
import sk.stuba.fei.indoorlocator.database.entities.Location;
import sk.stuba.fei.indoorlocator.database.exception.DatabaseException;
import sk.stuba.fei.indoorlocator.locator.FeiIndoorLocator;

public class NavigationActivity extends ListActivity {

    private WifiManager wifi;
    private FeiLocatorReceiver feiLocatorReceiver;
    private ProgressDialog progDialog;
    private FeiIndoorLocator feiLocator;
    private TextView locationText;
    private EditText wantedLocation;
    private List<String> steps;
    private DatabaseManager databaseManager;
    private NavigationAdapter navigationAdapater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        locationText = (TextView)findViewById(R.id.locationText2);
        locationText.setText("?");
        databaseManager = new DatabaseManager(new DatabaseHelper(getApplicationContext()));
        databaseManager.open();
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        feiLocatorReceiver = new FeiLocatorReceiver();

        //THE FLOOR LOCATOR MUST BE SET! If the floor locator is null than the default strategy is used.
        feiLocator = new FeiIndoorLocator();
        registerReceiver(feiLocatorReceiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        startDetection(null);

        wantedLocation=(EditText)findViewById(R.id.input_location);
        wantedLocation.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after){}

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String text = wantedLocation.getText().toString();
                int textlength = wantedLocation.getText().length();

                if(textlength>0) {
                    if (!Character.isLetter(text.charAt(0))) {
                        wantedLocation.setText("");
                        textlength = 0;
                    }

                    if (textlength >0) {
                        if(textlength>3){
                            if(text.substring(1,4).compareTo(" - ")!=0){
                                String a=text.toUpperCase().substring(0,1)+ " - ";
                                wantedLocation.setText(a);
                                wantedLocation.setSelection(wantedLocation.getText().length());
                            }
                        }
                        else{
                            String a=text.toUpperCase().substring(0,1)+ " - ";
                            wantedLocation.setText(a);
                            wantedLocation.setSelection(wantedLocation.getText().length());
                        }
                    }

                    if (textlength==5 && !Character.isDigit(text.charAt(4))){
                        wantedLocation.setText(text.substring(0,textlength-1));
                        wantedLocation.setSelection(wantedLocation.getText().length());
                    }
                    if (textlength==6 && !Character.isDigit(text.charAt(5))){
                        wantedLocation.setText(text.substring(0,textlength-1));
                        wantedLocation.setSelection(wantedLocation.getText().length());
                    }
                    if (textlength==7 && !Character.isDigit(text.charAt(6))){
                        wantedLocation.setText(text.substring(0,textlength-1));
                        wantedLocation.setSelection(wantedLocation.getText().length());
                    }
                    if (textlength>7){
                        wantedLocation.setText(text.substring(0,textlength-1));
                        wantedLocation.setSelection(wantedLocation.getText().length());
                    }
                }
            }});
    }

    public void startDetection(View v) {
        if (!wifi.isWifiEnabled())
        {
            Toast.makeText(getApplicationContext(), "Wifi is disabled! Making it enabled.", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        wifi.startScan();
        progDialog = ProgressDialog.show(NavigationActivity.this,"Localization in progress!",
                "Please wait....", true);

        if(v!=null){

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        databaseManager.open();
        registerReceiver(feiLocatorReceiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // progDialog.show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseManager.close();
        try {
            unregisterReceiver(feiLocatorReceiver);
        }catch(IllegalArgumentException e){}

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progDialog != null && progDialog.isShowing())
                {
                    progDialog.dismiss();
                }
            }
        });
    }

    private String formatLocationForUI(Location location){
        if (location == null)
            return "?";

        return location.getBlock().toString()+" - "+location.getFloor().toString();
    }

    private String getFloorFromFormatLocation(String location){
        if (location.compareTo("") == 0)
            return null;

        String floor= location.split(" - ")[1];
        return floor;
    }

    private String getBlockFromFormatLocation(String location){
        if (location.compareTo("") == 0)
            return null;

        String block= location.split(" - ")[0];
        return block;
    }

    public void startNavigation(View v) {
        steps=new ArrayList<String>();
        String wantedLocationText=wantedLocation.getText().toString();
        String actualLocationText= locationText.getText().toString();
        if(actualLocationText.compareTo("?") != 0) {
            if (wantedLocationText.compareTo("") != 0 && checkWantedText(wantedLocationText)) {

                Character wBlock = wantedLocationText.toUpperCase().charAt(0);
                Character aBlock = getBlockFromFormatLocation(actualLocationText).toUpperCase().charAt(0);
                int wFloor = Integer.parseInt(wantedLocationText.substring(4,5));
                int aFloor = Integer.parseInt(getFloorFromFormatLocation(actualLocationText));

                if (wBlock==aBlock) {
                    if(wFloor>aFloor){
                        if((wFloor - aFloor)<2) {
                            steps.add("Go up " + (wFloor - aFloor) + " floor.");
                        }
                        else{
                            steps.add("Go up " + (wFloor - aFloor) + " floors.");
                        }
                    }
                    if(wFloor<aFloor){
                        if((aFloor - wFloor)<2) {
                            steps.add("Go down " + (aFloor - wFloor) + " floor.");
                        }
                        else{
                            steps.add("Go down " + (aFloor - wFloor) + " floors.");
                        }
                    }
                    if(wFloor==aFloor){
                        steps.add("You have reached your destination :)");
                    }
                }
                else {

                    if(aFloor==1){
                        steps.add("Go down "+(aFloor)+" floor (on ground floor).");
                    }
                    else if(aFloor>1){
                        steps.add("Go down "+(aFloor)+" floors (on ground floor).");
                    }

                    if(wBlock<aBlock){
                        steps.add("Turn left. ");
                    }
                    if(wBlock>aBlock){
                        steps.add("Turn right.");
                    }

                    steps.add("Go straight until you reach "+wBlock.toString()+" - 0.");

                    if(wFloor==1){
                        steps.add("Go up "+(wFloor)+" floor.");
                    }
                    else if(wFloor>1){
                        steps.add("Go up "+(wFloor)+" floors.");
                    }

                    if(wFloor==aFloor && aBlock==wBlock){
                        steps.add("You have reached your destination :)");
                    }
                }

                navigationAdapater = new NavigationAdapter(getApplicationContext(),steps);
                setListAdapter(navigationAdapater);

            } else {
                Toast.makeText(getApplicationContext(), "Fill wanted location!", Toast.LENGTH_SHORT).show();
            }


        }
        else {
            Toast.makeText(getApplicationContext(), "Unknown actual position!", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkWantedText(String s){
        if(s.matches("[a-zA-Z]{1} - [0-9]{3}")){
            return true;
        }
        else return false;

    }

    private class FeiLocatorReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = wifi.getScanResults();
            Log.i("FEI",String.valueOf(results.size()));
            try {
                Location location = feiLocator.getActualLocation(wifi.getScanResults(),databaseManager);
                locationText.setText(formatLocationForUI(location));
                View v = View.inflate(context, R.layout.activity_navigation, null);
                startNavigation(v);

            } catch (DatabaseException e) {
                locationText.setText(formatLocationForUI(null));
            }finally {
                NavigationActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(progDialog != null && progDialog.isShowing())
                        {
                            progDialog.dismiss();
                        }
                    }
                });
            }
        }

    }
}
