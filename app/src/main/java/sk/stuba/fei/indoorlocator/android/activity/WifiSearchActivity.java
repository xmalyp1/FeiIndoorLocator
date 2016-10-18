package sk.stuba.fei.indoorlocator.android.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sk.stuba.fei.indoorlocator.R;
import sk.stuba.fei.indoorlocator.android.adapter.WifiScanResultAdapter;
import sk.stuba.fei.indoorlocator.database.DatabaseHelper;
import sk.stuba.fei.indoorlocator.database.DatabaseManager;
import sk.stuba.fei.indoorlocator.database.dao.LocationDAO;
import sk.stuba.fei.indoorlocator.database.dao.MeasurementDAO;
import sk.stuba.fei.indoorlocator.database.dao.WifiDAO;
import sk.stuba.fei.indoorlocator.database.entities.Location;
import sk.stuba.fei.indoorlocator.database.entities.Measurement;
import sk.stuba.fei.indoorlocator.database.entities.Wifi;
import sk.stuba.fei.indoorlocator.database.exception.DatabaseException;

public class WifiSearchActivity extends Activity {

    private static final int SCAN_PROCESS_SECONDS = 30;
    private static final String[] BLOCKS= {"A","B","C","D","E"};
    private static final String[] FLOORS= {"-1","0","1","2","3","4","5","6"};

    private WifiManager wifi;
    private List<ScanResult> wifiResults;
    private WifiBroadcastReceiver wifiBroadcastReceiver;
    private WifiScanResultAdapter wifiScanResultAdapter;
    private ListView scanResultListView;
    private RelativeLayout saveFormLayout;
    private DatabaseManager databaseManager;
    private Location selectedLocation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_search);
        selectedLocation = (Location)getIntent().getSerializableExtra("LOCATION");
        databaseManager = new DatabaseManager(new DatabaseHelper(getApplicationContext()));
        databaseManager.open();
        //new LocationDAO(databaseHelper).populateLocation();

        saveFormLayout = (RelativeLayout) this.findViewById(R.id.save_layout_container);
        saveFormLayout.setVisibility(View.INVISIBLE);
        wifiResults = new ArrayList<ScanResult>();
        wifiBroadcastReceiver = new WifiBroadcastReceiver();
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // askForWifiPermission();
        if (wifi.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "Wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }

        Button scanBtn = (Button)this.findViewById(R.id.refresh);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerReceiver(wifiBroadcastReceiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiResults.clear();
                wifi.startScan();
                final ProgressDialog progDailog = ProgressDialog.show(WifiSearchActivity.this,
                        "Scanning wifi networks",
                        "Please wait....", true);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        wifiScanResultAdapter = new WifiScanResultAdapter(getApplicationContext() ,R.layout.wifi_scan_row,R.id.wifi_list,wifiResults);
                        scanResultListView = (ListView)findViewById(R.id.wifi_list);
                        scanResultListView.setAdapter(wifiScanResultAdapter);
                        unregisterReceiver(wifiBroadcastReceiver);
                        progDailog.dismiss();
                        if(wifiResults == null || wifiResults.isEmpty()){
                            Toast.makeText(getApplicationContext(), "No results found!", Toast.LENGTH_LONG).show();
                            return;
                        }else {
                            saveFormLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }, 1000 * SCAN_PROCESS_SECONDS);
            }
        });

        Button save = (Button)this.findViewById(R.id.btn_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveMeasurement();
                    Toast.makeText(getApplicationContext(), "Measurement was saved sucessfully!", Toast.LENGTH_LONG).show();

                } catch (DatabaseException e) {
                    Toast.makeText(getApplicationContext(), "Unable to save the measurement!", Toast.LENGTH_LONG).show();
                    Log.i("FEI","Unable to save measurement");
                    e.printStackTrace();
                }
            }
        });
        if(selectedLocation == null)
            save.setClickable(false);
    }

    @Override
    protected void onPause() {
        try {
            unregisterReceiver(wifiBroadcastReceiver);
        }catch(IllegalArgumentException e){}

        databaseManager.close();
        super.onPause();
    }

    @Override
    protected void onResume() {
        databaseManager.open();
        registerReceiver(wifiBroadcastReceiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }




    private void saveMeasurement() throws DatabaseException {

        LocationDAO locationDAO= new LocationDAO(databaseManager);
        WifiDAO wifiDAO = new WifiDAO(databaseManager);
        MeasurementDAO measurementDAO = new MeasurementDAO(databaseManager);

        for(ScanResult result : wifiResults){
            Wifi wifi = wifiDAO.findWifiByMac(result.BSSID);
            if(wifi == null){
                wifi = new Wifi(result.SSID,result.BSSID);
                Long l = wifiDAO.createEntity(new Wifi(result.SSID,result.BSSID));
                wifi.setId(l);
            }
            measurementDAO.createEntity(new Measurement(result.level,selectedLocation.getId(),wifi.getId()));
        }

        locationDAO.updateLastScanForLocation(selectedLocation);
    }


    private class WifiBroadcastReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult> scan =wifi.getScanResults();

                List<ScanResult> finalResult = new ArrayList<>();
                for(ScanResult result : scan){
                    Log.i("FEI",scan.toString());
                    ScanResult found = getScanResult(result.BSSID);
                    if(found != null){
                        if(result.level < found.level)
                            finalResult.add(result);
                        else
                            finalResult.add(found);
                    }else {
                        finalResult.add(result);
                    }
                }
                wifiResults = finalResult;
            }

        private ScanResult getScanResult(String mac){
            for(ScanResult result:wifiResults){
                if(result.BSSID.equals(mac)){
                    return result;
                }
            }
            return null;
        }
    }

    }

