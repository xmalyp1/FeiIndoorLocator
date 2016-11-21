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
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import sk.stuba.fei.indoorlocator.utils.PermissionManager;
import sk.stuba.fei.indoorlocator.utils.ScanDataDTO;
import sk.stuba.fei.indoorlocator.utils.ScanResultMapper;

public class WifiSearchActivity extends Activity {

    private static final int SCAN_PROCESS_SECONDS = 30;

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

                if(PermissionManager.hasPermissions(WifiSearchActivity.this, PermissionManager.PERMISSIONS_GROUP_LOCATION)) {
                    registerReceiver(wifiBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                    wifiResults.clear();
                    wifi.startScan();
                    final ProgressDialog progDailog = ProgressDialog.show(WifiSearchActivity.this,
                            "Scanning wifi networks",
                            "Please wait....", true);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            List<ScanDataDTO> data = null;
                            try {
                                data = getDataToDisplay(ScanResultMapper.mapScanResults(wifiResults), getBSIDOnLocation(selectedLocation));
                            } catch (DatabaseException e) {
                                data = ScanResultMapper.mapScanResults(wifiResults);
                            }
                            wifiScanResultAdapter = new WifiScanResultAdapter(getApplicationContext(), R.layout.wifi_scan_row, R.id.wifi_list, data, getBSIDOnLocation(selectedLocation));
                            scanResultListView = (ListView) findViewById(R.id.wifi_list);
                            scanResultListView.setAdapter(wifiScanResultAdapter);
                            unregisterReceiver(wifiBroadcastReceiver);
                            progDailog.dismiss();
                            if (wifiResults == null || wifiResults.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "No results found!", Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                saveFormLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    }, 1000 * SCAN_PROCESS_SECONDS);
                } else {
                    ActivityCompat.requestPermissions(WifiSearchActivity.this, PermissionManager.PERMISSIONS_GROUP_LOCATION, PermissionManager.PERMISSION_REQUEST_LOCATION);
                }
            }
        });

        Button save = (Button)this.findViewById(R.id.btn_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveMeasurement();
                    Toast.makeText(getApplicationContext(), "Measurement was saved sucessfully!", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(WifiSearchActivity.this,FeiLocatorMainActivity.class);
                    WifiSearchActivity.this.startActivity(i);
                } catch (DatabaseException e) {
                    Toast.makeText(getApplicationContext(), "Unable to save the measurement!", Toast.LENGTH_LONG).show();
                    Log.i("FEI","Unable to save measurement");
                    e.printStackTrace();
                }
            }
        });
        if(selectedLocation == null)
            save.setClickable(false);
        else{
            scanBtn.setText("Scan for location "+ selectedLocation.getBlock()+" "+selectedLocation.getFloor());
        }
    }

    @Override
    protected void onPause() {
        try {
            unregisterReceiver(wifiBroadcastReceiver);
        }catch(IllegalArgumentException e){}
        if(wifiScanResultAdapter != null)
            wifiScanResultAdapter.clear();
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

        for(ScanDataDTO result : wifiScanResultAdapter.getSelectedScanResults()){
            Wifi wifi = wifiDAO.findWifiByMac(result.getMac());
            if(wifi == null){
                wifi = new Wifi(result.getName(),result.getMac());
                Long l = wifiDAO.createEntity(new Wifi(result.getName(),result.getMac()));
                wifi.setId(l);
            }
            measurementDAO.createEntity(new Measurement(result.getLevel(),selectedLocation.getId(),wifi.getId()));
            Log.i("FEI_SAVE","Saving data: "+result.toString());
        }

        locationDAO.updateLastScanForLocation(selectedLocation);
    }

    private Set<String> getBSIDOnLocation(Location loc){
        MeasurementDAO measurementDAO = new MeasurementDAO(databaseManager);
        WifiDAO wifiDAO = new WifiDAO(databaseManager);
        List<Measurement> measurementOnLocation = measurementDAO.findMeasurementsForLocation(loc);
        Set<String> wifiOnLocation = new HashSet<String>();
        for(Measurement mes : measurementOnLocation){
            try {
                Wifi w =wifiDAO.findWifiByID(mes.getWifiId());
                if(w != null)
                    wifiOnLocation.add(w.getMac());
            } catch (DatabaseException e) {
            }
        }
        return wifiOnLocation;
    }

    private List<ScanDataDTO> getDataToDisplay(List<ScanDataDTO> availableData,Set<String> bssids) throws DatabaseException {
        WifiDAO wifiDAO = new WifiDAO(databaseManager);
        List<ScanDataDTO> result = new ArrayList<>(availableData);
        boolean found;
        for(String bssid : bssids) {
            found=false;
            for(ScanDataDTO data : availableData){
                if(bssid.equals(data.getMac())) {
                    found =true;
                    break;
                }
            }
            if(found)
                continue;
            Wifi w = wifiDAO.findWifiByMac(bssid);
            if(w!=null)
                result.add(new ScanDataDTO(w.getSsid(),w.getMac(),null));
        }
        return result;
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult> scan =wifi.getScanResults();

                List<ScanResult> finalResult = new ArrayList<>();
                for(ScanResult result : scan){
                    Log.i("FEI",result.toString());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PermissionManager.PERMISSION_REQUEST_LOCATION) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent myIntent = new Intent(WifiSearchActivity.this, DetectionActivity.class);
                WifiSearchActivity.this.startActivity(myIntent);
            } else {
                Toast.makeText(WifiSearchActivity.this,"You do not have needed permissions.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

