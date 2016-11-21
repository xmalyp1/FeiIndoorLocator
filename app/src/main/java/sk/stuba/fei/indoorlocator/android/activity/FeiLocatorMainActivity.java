package sk.stuba.fei.indoorlocator.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

import sk.stuba.fei.indoorlocator.R;
import sk.stuba.fei.indoorlocator.database.DatabaseHelper;
import sk.stuba.fei.indoorlocator.database.DatabaseManager;
import sk.stuba.fei.indoorlocator.database.exception.DatabaseException;
import sk.stuba.fei.indoorlocator.utils.PermissionManager;

public class FeiLocatorMainActivity extends Activity {

    private static final int FILE_CHOOSER = 3333;

    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fei_locator_main);
        dbManager = new DatabaseManager(new DatabaseHelper(getApplicationContext()));
        dbManager.open();
        Button scanButton = (Button)this.findViewById(R.id.btn_open_scan_process);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(FeiLocatorMainActivity.this, LocationsActivity.class);
                FeiLocatorMainActivity.this.startActivity(myIntent);
            }
        });

        Button whereAmI=(Button)this.findViewById(R.id.btn_open_check_process);
        whereAmI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(PermissionManager.hasPermissions(FeiLocatorMainActivity.this, PermissionManager.PERMISSIONS_GROUP_LOCATION)) {
                    Intent myIntent = new Intent(FeiLocatorMainActivity.this, DetectionActivity.class);
                    FeiLocatorMainActivity.this.startActivity(myIntent);
                } else {
                    ActivityCompat.requestPermissions(FeiLocatorMainActivity.this, PermissionManager.PERMISSIONS_GROUP_LOCATION, PermissionManager.PERMISSION_REQUEST_LOCATION);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.data_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.import_option:
                importDB();
                return true;
            case R.id.export_option:
                exportDB();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void exportDB() {
        try {
            if(PermissionManager.hasPermissions(FeiLocatorMainActivity.this, PermissionManager.PERMISSIONS_GROUP_STORAGE))
                dbManager.exportDB();
            else
                ActivityCompat.requestPermissions(FeiLocatorMainActivity.this, PermissionManager.PERMISSIONS_GROUP_STORAGE, PermissionManager.PERMISSION_REQUEST_STORAGE);

            Toast.makeText(FeiLocatorMainActivity.this,"Database was successfully exported.", Toast.LENGTH_SHORT).show();
        } catch (DatabaseException | IOException e) {
            e.printStackTrace();
            Log.e("FEI_DB_EXPORT_ERROR",e.getMessage());
            Toast.makeText(FeiLocatorMainActivity.this,"Database was not successfully exported.", Toast.LENGTH_SHORT).show();
        }

    }

    public void importDB() {

        if(PermissionManager.hasPermissions(FeiLocatorMainActivity.this, PermissionManager.PERMISSIONS_GROUP_STORAGE)) {
            Intent intent = new Intent()
                    .setType("text/comma-separated-values")
                    .setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Select a csv file"), FILE_CHOOSER);
        } else
            ActivityCompat.requestPermissions(FeiLocatorMainActivity.this, PermissionManager.PERMISSIONS_GROUP_STORAGE, PermissionManager.PERMISSION_REQUEST_STORAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == FILE_CHOOSER && resultCode == RESULT_OK) {
            Uri selectedfileUri = data.getData();

            InputStream selectedFileIS = null;
            try {
                selectedFileIS = FeiLocatorMainActivity.this.getContentResolver().openInputStream(selectedfileUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try {
                dbManager.ImportDB(selectedFileIS);
                Toast.makeText(FeiLocatorMainActivity.this,"Database was successfully imported.", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(FeiLocatorMainActivity.this,"Database was not successfully imported.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PermissionManager.PERMISSION_REQUEST_LOCATION) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent myIntent = new Intent(FeiLocatorMainActivity.this, DetectionActivity.class);
                FeiLocatorMainActivity.this.startActivity(myIntent);
            } else {
                Toast.makeText(FeiLocatorMainActivity.this,"You do not have needed permissions.", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == PermissionManager.PERMISSION_REQUEST_STORAGE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                importDB();
            } else {
                Toast.makeText(FeiLocatorMainActivity.this,"You do not have needed permissions.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
