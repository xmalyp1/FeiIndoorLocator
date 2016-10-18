package sk.stuba.fei.indoorlocator.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import sk.stuba.fei.indoorlocator.database.DatabaseHelper;
import sk.stuba.fei.indoorlocator.database.DatabaseManager;
import sk.stuba.fei.indoorlocator.database.entities.Measurement;
import sk.stuba.fei.indoorlocator.database.entities.Wifi;

/**
 * Created by Patrik on 13.10.2016.
 */

public class MeasurementDAO extends  AbstractDAO<Measurement> {

    public MeasurementDAO (DatabaseManager dbHelper){
        super(dbHelper);
    }

    @Override
    public Long createEntity(Measurement measurement) {
        ContentValues values = new ContentValues();
        values.put(Measurement.Field.LEVEL,measurement.getLevel());
        values.put(Measurement.Field.BLOCK_ID,measurement.getBlockId());
        values.put(Measurement.Field.WIFI_ID,measurement.getWifiId());
        Long l =getDatabase().insert(getDatabaseHelper().MEASUREMENT_TABLE,null,values);
        return l;
    }

    @Override
    public List<Measurement> getEntityFromCursor(Cursor cursor) {
        List<Measurement> result = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            Measurement measurement = new Measurement();
            measurement.setId(cursor.getLong(0));
            measurement.setLevel(cursor.getInt(1));
            measurement.setBlockId(cursor.getLong(2));
            measurement.setWifiId(cursor.getLong(3));
            result.add(measurement);
            cursor.moveToNext();
        }
        cursor.close();
        return result;
    }

    public List<Measurement> findMeasurementsForWifi(Wifi wifi){
        Cursor cursor = getDatabase().query(getDatabaseHelper().MEASUREMENT_TABLE,null,Measurement.Field.WIFI_ID+"=?",new String[]{wifi.getId().toString()},null,null,null);
        List<Measurement> result = getEntityFromCursor(cursor);
        cursor.close();
        return result;
    }
}
