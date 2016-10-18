package sk.stuba.fei.indoorlocator.android.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import sk.stuba.fei.indoorlocator.R;

/**
 * Created by Patrik on 12.10.2016.
 */

public class WifiScanResultAdapter extends ArrayAdapter<ScanResult> {

    public WifiScanResultAdapter(Context context, int resource, int textViewResourceId, List<ScanResult> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row =li.inflate(R.layout.wifi_scan_row,parent,false);
        ScanResult result = getItem(position);
        TextView ssid = (TextView)row.findViewById(R.id.ssid);
        ssid.setText(result.SSID);

        TextView mac = (TextView)row.findViewById(R.id.mac_adr);
        mac.setText(result.BSSID);

        TextView freq = (TextView)row.findViewById(R.id.frequency);
        freq.setText(Integer.toString(result.frequency));

        TextView level = (TextView)row.findViewById(R.id.level);
        level.setText(Integer.toString(result.level));

        return row;

    }
}
