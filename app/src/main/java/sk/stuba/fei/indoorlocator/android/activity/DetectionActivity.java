package sk.stuba.fei.indoorlocator.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import sk.stuba.fei.indoorlocator.R;
import sk.stuba.fei.indoorlocator.database.DatabaseHelper;
import sk.stuba.fei.indoorlocator.database.DatabaseManager;
import sk.stuba.fei.indoorlocator.database.entities.Location;
import sk.stuba.fei.indoorlocator.database.exception.DatabaseException;
import sk.stuba.fei.indoorlocator.locator.FeiIndoorLocator;

public class DetectionActivity extends Activity {

    private WifiManager wifi;
    private FeiLocatorReceiver feiLocatorReceiver;
    private ProgressDialog progDialog;
    private FeiIndoorLocator feiLocator;
    private TextView locationText;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);
        locationText = (TextView)findViewById(R.id.locationText);
        locationText.setText("?");
        databaseManager = new DatabaseManager(new DatabaseHelper(getApplicationContext()));
        databaseManager.open();
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        feiLocatorReceiver = new FeiLocatorReceiver();

        //THE FLOOR LOCATOR MUST BE SET! If the floor locator is null than the default strategy is used.
        feiLocator = new FeiIndoorLocator();
        registerReceiver(feiLocatorReceiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        startDetection(null);
    }

    public void startDetection(View v) {
        if (!wifi.isWifiEnabled())
        {
            Toast.makeText(getApplicationContext(), "Wifi is disabled! Making it enabled.", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        wifi.startScan();
        progDialog = ProgressDialog.show(DetectionActivity.this,"Localization in progress!",
                "Please wait....", true);
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

    private class FeiLocatorReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = wifi.getScanResults();
            Log.i("FEI",String.valueOf(results.size()));
            try {
                Location location = feiLocator.getActualLocation(wifi.getScanResults(),databaseManager);
                locationText.setText(formatLocationForUI(location));
            } catch (DatabaseException e) {
                locationText.setText(formatLocationForUI(null));
        }finally {
                DetectionActivity.this.runOnUiThread(new Runnable() {
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
