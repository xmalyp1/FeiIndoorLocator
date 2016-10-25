package sk.stuba.fei.indoorlocator.android.activity;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import sk.stuba.fei.indoorlocator.Helper.FileManager;
import sk.stuba.fei.indoorlocator.R;
import sk.stuba.fei.indoorlocator.android.adapter.LocationsAdapter;
import sk.stuba.fei.indoorlocator.database.DatabaseHelper;
import sk.stuba.fei.indoorlocator.database.DatabaseManager;
import sk.stuba.fei.indoorlocator.database.dao.LocationDAO;
import sk.stuba.fei.indoorlocator.database.entities.Location;
import sk.stuba.fei.indoorlocator.database.exception.DatabaseException;

public class LocationsActivity extends ListActivity {

    private static final int FILE_CHOOSER = 3333;

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

        Button btn = (Button)findViewById(R.id.btn_open_add_dialog);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeDialog();
            }
        });

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(LocationsActivity.this,WifiSearchActivity.class);
        i.putExtra("LOCATION",locationList.get(position));
        this.startActivity(i);
    }

    private void initializeDialog(){
        final Dialog dialog = new Dialog(LocationsActivity.this);
        dialog.setContentView(R.layout.add_location_dialog);
        dialog.setTitle("Add new location");

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_add_location);

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    int floor = Integer.valueOf(((EditText)dialog.findViewById(R.id.input_floor)).getText().toString());
                    String block =((EditText)dialog.findViewById(R.id.input_block)).getText().toString();
                    if(block.length()>1 || block.isEmpty())
                        throw new Exception("Block identifier should be only one character");
                    locationDAO.createEntity(new Location(block.toUpperCase().charAt(0),floor));
                }catch(Exception e){
                    Toast.makeText(LocationsActivity.this,"Unable to create a new location.", Toast.LENGTH_SHORT).show();
                    return;
                }

                locationsAdapater.addAll(locationDAO.getAllLocations());
                LocationsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        locationsAdapater.notifyDataSetChanged();
                    }
                });
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void exportDB(View view) {
        String dbName = dbManager.getDatabaseHelper().getDatabaseName();

        try {
            dbManager.exportDB();

            Toast.makeText(LocationsActivity.this,"Database was successfully exported.", Toast.LENGTH_SHORT).show();
        } catch (DatabaseException | IOException e) {
            e.printStackTrace();

            Toast.makeText(LocationsActivity.this,"Database was not successfully exported.", Toast.LENGTH_SHORT).show();
        }

    }

    public void importDB(View view) {

        Intent intent = new Intent()
        .setType("text/csv")
        .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a csv file"), FILE_CHOOSER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == FILE_CHOOSER && resultCode == RESULT_OK) {
            Uri selectedfileUri = data.getData();
            File selectedFile = new File(selectedfileUri.getPath());
            try {
                dbManager.ImportDB(selectedFile);
                Toast.makeText(LocationsActivity.this,"Database was successfully imported.", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(LocationsActivity.this,"Database was not successfully imported.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
