package sk.stuba.fei.indoorlocator.android.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sk.stuba.fei.indoorlocator.R;
import sk.stuba.fei.indoorlocator.database.entities.Wifi;
import sk.stuba.fei.indoorlocator.utils.NullComparator;
import sk.stuba.fei.indoorlocator.utils.ScanDataDTO;

/**
 * Created by Patrik on 12.10.2016.
 */

public class WifiScanResultAdapter extends BaseAdapter {

    private static final int SENSITIVITY_VALUE = 5;
    private Context context;
    private List<ScanDataDTO> data;
    private Set<ScanDataDTO> selectedScanResults;
    private Set<String> wifiOnLocation;

    public WifiScanResultAdapter(Context context, int resource, int textViewResourceId, List<ScanDataDTO> objects,Set<String>bssids) {
        super();
        this.context = context;
        this.data=objects;
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

    public List<ScanDataDTO> getScanData(){
        return data;
    }

    public void setDataForUI(List<ScanDataDTO> scannedData){

        for(ScanDataDTO sdata : scannedData){
            ScanDataDTO scanDataDTO = getScanDataDTO(sdata.getMac());
            if(scanDataDTO == null) {
                data.add(sdata);
                if(sdata.getLevel() != null)
                    selectedScanResults.add(sdata);

                continue;
            }

            if(scanDataDTO.getLevel() == null) {
                scanDataDTO.setLevel(sdata.getLevel());
                selectedScanResults.add(scanDataDTO);
            }
            else if (Math.abs(scanDataDTO.getLevel()-sdata.getLevel()) > SENSITIVITY_VALUE){
                scanDataDTO.setLevel(sdata.getLevel());
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.i("RESULT_SORT","Sorting results....");
            Collections.sort(data, Comparator.nullsLast(Comparator.comparingInt(ScanDataDTO::getLevel)));
        }else{
            Log.i("RESULT_SORT","Sorting results....");

            Collections.sort(data, NullComparator.atEnd(new Comparator<ScanDataDTO>() {
                @Override
                public int compare(ScanDataDTO o1, ScanDataDTO o2) {
                    return o1.getLevel() - o2.getLevel();
                }
            }));
        }

    }

    private ScanDataDTO getScanDataDTO(String mac){
        for(ScanDataDTO sd:data){
            if(mac.equals(sd.getMac()))
                return sd;
        }
        return null;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public ScanDataDTO getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.indexOf(position);
    }

    public void setData(List<ScanDataDTO> scanDataDTOs){
        data.clear();
        data.addAll(scanDataDTOs);
    }

    @NonNull
    @Override
    public View getView(int position, View row, ViewGroup parent) {
        Log.i("getView","Calling get view");

        final ViewHolder holder;
        final ScanDataDTO result = getItem(position);
        if(row == null) {

            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = li.inflate(R.layout.wifi_scan_row, parent, false);
            holder = new ViewHolder();
            holder.mac = (TextView) row.findViewById(R.id.mac_adr);
            holder.checkBox = (CheckBox) row.findViewById(R.id.wifiCheckBox);
            holder.ssid =  (TextView)row.findViewById(R.id.ssid);
            holder.level = (ImageView)row.findViewById(R.id.level);
            holder.flag = (TextView) row.findViewById(R.id.flag);
            row.setTag(holder);
        }else{
            holder = (ViewHolder) row.getTag();
        }

        holder.ssid.setText(result.getName());

        holder.mac.setText(result.getMac());
        holder.flag.setText("");


        if(!wifiOnLocation.contains(result.getMac())) {
            holder.flag.setTextColor(Color.GREEN);
            holder.flag.setText("[NEW]");
        }

        if(result.getLevel() != null) {
            int level = WifiManager.calculateSignalLevel(result.getLevel(),4)+1;
            switch (level){
                case 1:
                    holder.level.setImageResource(R.drawable.ic_signal_wifi_1_bar_black_24dp);
                    break;

                case 2:
                    holder.level.setImageResource(R.drawable.ic_signal_wifi_2_bar_black_24dp);
                    break;

                case 3:
                    holder.level.setImageResource(R.drawable.ic_signal_wifi_3_bar_black_24dp);
                    break;

                case 4:
                    holder.level.setImageResource(R.drawable.ic_signal_wifi_4_bar_black_24dp);
                    break;

                default:
                    holder.level.setImageResource(R.drawable.ic_signal_wifi_0_bar_black_24dp);
                    break;

            }
        }else{
            holder.level.setImageResource(R.drawable.ic_signal_wifi_0_bar_black_24dp);
            holder.flag.setTextColor(Color.RED);
            holder.flag.setText("[N/A]");
        }
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)  {
                if(isChecked)
                    selectedScanResults.add(result);
                else
                    selectedScanResults.remove(result);
            }
        });

        if(result.getLevel() == null) {
            holder.checkBox.setChecked(false);
            holder.checkBox.setClickable(false);
        }else{
            holder.checkBox.setChecked(selectedScanResults.contains(result));
            holder.checkBox.setClickable(true);
            holder.checkBox.setVisibility(View.VISIBLE);
        }
        return row;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    public void clear() {
        data.clear();
        selectedScanResults.clear();
    }

    private class ViewHolder {
        protected TextView mac;
        protected TextView ssid;
        protected ImageView level;
        protected CheckBox checkBox;
        protected TextView flag;
    }

}
