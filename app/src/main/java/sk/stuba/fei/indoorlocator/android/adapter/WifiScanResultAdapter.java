package sk.stuba.fei.indoorlocator.android.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sk.stuba.fei.indoorlocator.R;
import sk.stuba.fei.indoorlocator.database.entities.Wifi;
import sk.stuba.fei.indoorlocator.utils.ScanDataDTO;

/**
 * Created by Patrik on 12.10.2016.
 */

public class WifiScanResultAdapter extends ArrayAdapter<ScanDataDTO> {

    private Set<ScanDataDTO> selectedScanResults;
    private Set<String> wifiOnLocation;

    public WifiScanResultAdapter(Context context, int resource, int textViewResourceId, List<ScanDataDTO> objects,Set<String>bssids) {
        super(context, resource, textViewResourceId, objects);
        fillSelectedScanResults(objects);
        wifiOnLocation=bssids;
    }

    private void fillSelectedScanResults(List<ScanDataDTO> allScanResults){
        this.selectedScanResults=new HashSet<ScanDataDTO>();
        for(ScanDataDTO scanResult  : allScanResults){
            if(scanResult.getLevel() != null)
                selectedScanResults.add(scanResult);
        }
    }

    public List<ScanDataDTO> getSelectedScanResults(){
        return new ArrayList<>(selectedScanResults);
    }

    public void setWifiOnLocation(Set<String> wifiOnLocation){
        this.wifiOnLocation = wifiOnLocation;
    }

    public Set<String> getWifiOnLocation(){
        return wifiOnLocation;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row =li.inflate(R.layout.wifi_scan_row,parent,false);
        final ScanDataDTO result = getItem(position);
        TextView ssid = (TextView)row.findViewById(R.id.ssid);
        ssid.setText(result.getName());

        TextView mac = (TextView)row.findViewById(R.id.mac_adr);
        mac.setText(result.getMac());

        TextView freq = (TextView)row.findViewById(R.id.flag);

        if(!wifiOnLocation.contains(result.getMac())) {
            freq.setTextColor(Color.GREEN);
            freq.setText("[NEW]");
        }

        TextView level = (TextView)row.findViewById(R.id.level);
        if(result.getLevel() != null) {
            level.setText(Integer.toString(result.getLevel()));
        }else{
            freq.setTextColor(Color.RED);
            freq.setText("[N/A]");
        }
        final CheckBox check = (CheckBox)row.findViewById(R.id.wifiCheckBox);
        if(result.getLevel() == null) {
            check.setChecked(false);
            check.setClickable(false);
            check.setVisibility(View.INVISIBLE);
        }
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check.isChecked())
                    selectedScanResults.add(result);
                else
                    selectedScanResults.remove(result);
            }
        });
        return row;

    }

    @Override
    public void clear() {
        super.clear();
        selectedScanResults.clear();
    }
}
