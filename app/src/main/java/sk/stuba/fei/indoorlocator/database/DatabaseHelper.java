package sk.stuba.fei.indoorlocator.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import sk.stuba.fei.indoorlocator.database.dao.LocationDAO;
import sk.stuba.fei.indoorlocator.database.dao.WifiDAO;
import sk.stuba.fei.indoorlocator.database.entities.Location;
import sk.stuba.fei.indoorlocator.database.entities.Measurement;
import sk.stuba.fei.indoorlocator.database.entities.Wifi;

/**
 * Created by Patrik on 11.10.2016.
 */

public class DatabaseHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "fei_indoor_data";
    private static final int DB_VERSION = 4;

    public static final String WIFI_TABLE = "wifi";
    public static final String MEASUREMENT_TABLE = "measurement";
    public static final String LOCATION_TABLE = "location";

    private static final String CREATE_WIFI_TABLE = "create table "
            + WIFI_TABLE + "( " + Wifi.Field.ID
            + " integer primary key autoincrement, " + Wifi.Field.MAC_ADR
            + " text not null, "+ Wifi.Field.SSID +" text not null, "+Wifi.Field.ONLY_ON_BLOCK+" text default null);";

    private static final String CREATE_MEASUREMENT_TABLE = "create table "
            + MEASUREMENT_TABLE + "( " + Measurement.Field.ID
            + " integer primary key autoincrement, "+ Measurement.Field.LEVEL +" integer not null, "+Measurement.Field.BLOCK_ID+" integer not null, "+Measurement.Field.WIFI_ID+" integer not null);";

    private static final String CREATE_LOCATION_TABLE = "create table "
            + LOCATION_TABLE + "( " + Location.Field.ID
            + " integer primary key autoincrement, " + Location.Field.BLOCK
            + " text not null, "+ Location.Field.FLOOR +" integer not null,"+ Location.Field.LAST_SCAN +" text);";


    public DatabaseHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_WIFI_TABLE);
        db.execSQL(CREATE_LOCATION_TABLE);
        db.execSQL(CREATE_MEASUREMENT_TABLE);
        Log.i("FEI_Database","Database was created...");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("FEI_Database",
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + MEASUREMENT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + WIFI_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE);

        onCreate(db);
    }

    public static String getDate(Date d){
        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return iso8601Format.format(d);
    }

}
