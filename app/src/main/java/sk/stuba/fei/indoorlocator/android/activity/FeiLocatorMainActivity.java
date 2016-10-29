package sk.stuba.fei.indoorlocator.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import java.io.IOException;

import sk.stuba.fei.indoorlocator.R;
import sk.stuba.fei.indoorlocator.database.DatabaseHelper;
import sk.stuba.fei.indoorlocator.database.DatabaseManager;
import sk.stuba.fei.indoorlocator.database.exception.DatabaseException;

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
                Toast.makeText(getApplicationContext(),"The functionality is not implemented!",Toast.LENGTH_LONG).show();
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
            dbManager.exportDB();

            Toast.makeText(FeiLocatorMainActivity.this,"Database was successfully exported.", Toast.LENGTH_SHORT).show();
        } catch (DatabaseException | IOException e) {
            e.printStackTrace();
            Log.e("FEI_DB_EXPORT_ERROR",e.getMessage());
            Toast.makeText(FeiLocatorMainActivity.this,"Database was not successfully exported.", Toast.LENGTH_SHORT).show();
        }

    }

    public void importDB() {

        Intent intent = new Intent()
                .setType("text/comma-separated-values")
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
                Toast.makeText(FeiLocatorMainActivity.this,"Database was successfully imported.", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(FeiLocatorMainActivity.this,"Database was not successfully imported.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
