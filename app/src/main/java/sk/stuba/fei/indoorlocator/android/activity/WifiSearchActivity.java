package sk.stuba.fei.indoorlocator.android.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
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
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sk.stuba.fei.indoorlocator.R;
import sk.stuba.fei.indoorlocator.android.adapter.WifiScanResultAdapter;
import sk.stuba.fei.indoorlocator.database.DatabaseHelper;
import sk.stuba.fei.indoorlocator.database.DatabaseManager;
import sk.stuba.fei.indoorlocator.database.DatabaseUtils;
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
    private WifiBroadcastReceiver wifiBroadcastReceiver;
    private WifiScanResultAdapter wifiScanResultAdapter;
    private ListView scanResultListView;
    private DatabaseManager databaseManager;
    private Location selectedLocation;
    private TextView progressSelection;
    private Button save;
    private int progressCounter = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_search);
        selectedLocation = (Location)getIntent().getSerializableExtra("LOCATION");
        databaseManager = new DatabaseManager(new DatabaseHelper(getApplicationContext()));
        databaseManager.open();
        wifiBroadcastReceiver = new WifiBroadcastReceiver();
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        final LocationManager manager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) )
            Toast.makeText(WifiSearchActivity.this, "GPS is disabled! The functionality may not work correctly.", Toast.LENGTH_LONG).show();

        // askForWifiPermission();
        if (wifi.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "Enabling wifi option...", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        wifiScanResultAdapter = new WifiScanResultAdapter(getApplicationContext(), R.layout.wifi_scan_row, R.id.wifi_list, DatabaseUtils.getScanDataForLocation(selectedLocation,databaseManager), getBSIDOnLocation(selectedLocation));
        scanResultListView = (ListView) findViewById(R.id.wifi_list);
        scanResultListView.setAdapter(wifiScanResultAdapter);

        if(PermissionManager.hasPermissions(WifiSearchActivity.this, PermissionManager.PERMISSIONS_GROUP_LOCATION)) {

            registerReceiver(wifiBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifi.startScan();
            progressSelection = (TextView)this.findViewById(R.id.scanProgress);
            progressSelection.setText("Scanning networks..");
            Toast.makeText(getApplicationContext(), "Starting to scan the networks...", Toast.LENGTH_LONG).show();
            Log.i("FEI_SCAN","Starting to scan...");
        }else{
            ActivityCompat.requestPermissions(WifiSearchActivity.this, PermissionManager.PERMISSIONS_GROUP_LOCATION, PermissionManager.PERMISSION_REQUEST_LOCATION);
        }



        save = (Button)this.findViewById(R.id.saveBtn);
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
        if(wifiScanResultAdapter != null)
            wifiScanResultAdapter.setData(DatabaseUtils.getScanDataForLocation(selectedLocation,databaseManager));
        wifiScanResultAdapter.notifyDataSetChanged();
        registerReceiver(wifiBroadcastReceiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifi.startScan();
        super.onResume();
    }




    private void saveMeasurement() throws DatabaseException {

        LocationDAO locationDAO= new LocationDAO(databaseManager);
        WifiDAO wifiDAO = new WifiDAO(databaseManager);
        MeasurementDAO measurementDAO = new MeasurementDAO(databaseManager);

        for(ScanDataDTO result : wifiScanResultAdapter.getSelectedScanResults()){
            Log.i("FEI_SAVE_MEAUSREMENT","Saving: "+result.toString());
            Wifi wifi = wifiDAO.findWifiByMac(result.getMac());
            if(wifi == null){
                wifi = new Wifi(result.getName(),result.getMac());
                Long l = wifiDAO.createEntity(new Wifi(result.getName(),result.getMac()));
                wifi.setId(l);
            }
            measurementDAO.createEntity(new Measurement(result.getLevel(),selectedLocation.getId(),wifi.getId()));
        }

        locationDAO.updateLastScanForLocation(selectedLocation);
    }

    private Set<String> getBSIDOnLocation(Location loc){
        if(loc == null)
            return new HashSet<>();

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
                Log.i("FEI","Data received...");
                wifiScanResultAdapter.setDataForUI(ScanResultMapper.mapScanResults(wifi.getScanResults()));
                wifiScanResultAdapter.notifyDataSetChanged();
                if(progressCounter % 3 == 0)
                    progressSelection.setText("Scanning networks");
                else{
                    progressSelection.setText(progressSelection.getText()+".");
                }
                progressCounter++;


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
                //TODO maybe redirect to the main activity....
            }
        }
    }

}

