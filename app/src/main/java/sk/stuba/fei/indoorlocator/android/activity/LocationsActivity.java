package sk.stuba.fei.indoorlocator.android.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import sk.stuba.fei.indoorlocator.R;
import sk.stuba.fei.indoorlocator.android.adapter.LocationsAdapter;
import sk.stuba.fei.indoorlocator.database.DatabaseHelper;
import sk.stuba.fei.indoorlocator.database.DatabaseManager;
import sk.stuba.fei.indoorlocator.database.dao.LocationDAO;
import sk.stuba.fei.indoorlocator.database.entities.Location;

public class LocationsActivity extends ListActivity {

    private DatabaseManager dbManager;
    private LocationDAO locationDAO;
    private List<Location> locationList;
    private LocationsAdapter locationsAdapater;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        dbManager = new DatabaseManager(new DatabaseHelper(getApplicationContext()));
        dbManager.open();
        locationDAO = new LocationDAO(dbManager);
        locationList = locationDAO.getAllLocations();
        locationsAdapater = new LocationsAdapter(getApplicationContext(),locationList);
        setListAdapter(locationsAdapater);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(LocationsActivity.this,WifiSearchActivity.class);
        i.putExtra("LOCATION",locationList.get(position));
        this.startActivity(i);
    }
}
