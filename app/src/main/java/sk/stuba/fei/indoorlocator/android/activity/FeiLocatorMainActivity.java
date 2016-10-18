package sk.stuba.fei.indoorlocator.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import sk.stuba.fei.indoorlocator.R;

public class FeiLocatorMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fei_locator_main);
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
}
