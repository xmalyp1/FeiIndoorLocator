package sk.stuba.fei.indoorlocator.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sk.stuba.fei.indoorlocator.R;
import sk.stuba.fei.indoorlocator.database.entities.Location;

import static java.security.AccessController.getContext;

/**
 * Created by Patrik on 18.10.2016.
 */

public class LocationsAdapter extends BaseAdapter {

    List<Location> locationList;
    Context mContext;

    public LocationsAdapter(Context context,List<Location> locationList){
        super();
        this.mContext=context;
        this.locationList = locationList;
    }

    @Override
    public int getCount() {
        return locationList.size();
    }

    @Override
    public Object getItem(int position) {
        return locationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return locationList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Location location = locationList.get(position);
        convertView = li.inflate(R.layout.location_row,parent,false);
        TextView block = (TextView) convertView.findViewById(R.id.block_id);
        TextView floor = (TextView) convertView.findViewById(R.id.floor_id);
        TextView lastUpdate = (TextView) convertView.findViewById(R.id.update_date);

        block.setText(location.getBlock().toString());
        floor.setText(location.getFloor().toString());
        lastUpdate.setText(location.getLastScan() == null ? "Not scanned" : location.getLastScan());

        return convertView;
    }
}
