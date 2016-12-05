package sk.stuba.fei.indoorlocator.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import sk.stuba.fei.indoorlocator.R;
import sk.stuba.fei.indoorlocator.database.entities.Location;

/**
 * Created by Patrik on 18.10.2016.
 */

public class NavigationAdapter extends BaseAdapter {

    List<String> navigationList;
    Context mContext;

    public NavigationAdapter(Context context, List<String> navigationList){
        super();
        this.mContext=context;
        this.navigationList = navigationList;
    }

    public void clear(){
        navigationList.clear();
    }

    public void addAll(List<String> navg){
        navigationList.clear();
        navigationList.addAll(navg);
    }

    @Override
    public int getCount() {
        return navigationList.size();
    }

    @Override
    public Object getItem(int i) {
        return navigationList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return navigationList.get(i).hashCode();
    }

    public long getId(String s) {
        for (int i = 0; i < navigationList.size(); i++) {
            String p = navigationList.get(i);
            if (s.compareTo(p)==0)
            {
                return i;
            }
        }

        return -1;

    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String step = navigationList.get(i);
        convertView = li.inflate(R.layout.navigation_row,parent,false);
        TextView stepText = (TextView) convertView.findViewById(R.id.step);
        TextView orderText = (TextView) convertView.findViewById(R.id.order);

        orderText.setText((getId(step)+1)+")");
        stepText.setText(step);

        return convertView;
    }


}
