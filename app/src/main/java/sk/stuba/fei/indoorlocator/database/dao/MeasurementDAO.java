package sk.stuba.fei.indoorlocator.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import sk.stuba.fei.indoorlocator.database.DatabaseHelper;
import sk.stuba.fei.indoorlocator.database.DatabaseManager;
import sk.stuba.fei.indoorlocator.database.entities.Location;
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

    @Override
    public Long exists(ContentValues contentValues) {
        //ToDo
        return null;
    }

    public List<Measurement> findMeasurementsForWifi(Wifi wifi){
        Cursor cursor = getDatabase().query(getDatabaseHelper().MEASUREMENT_TABLE,null,Measurement.Field.WIFI_ID+"=?",new String[]{wifi.getId().toString()},null,null,null);
        List<Measurement> result = getEntityFromCursor(cursor);
        cursor.close();
        return result;
    }

    public List<Measurement> findMeasurementsForLocation(Location loc){
        Cursor cursor = getDatabase().query(getDatabaseHelper().MEASUREMENT_TABLE,null,Measurement.Field.BLOCK_ID+"=?",new String[]{loc.getId().toString()},null,null,null);
        List<Measurement> result = getEntityFromCursor(cursor);
        cursor.close();
        return result;
    }

    public List<Measurement> getAllMeasurements() {
        Cursor cursor = getDatabase().query(getDatabaseHelper().MEASUREMENT_TABLE,null,null,null,null,null,null);
        List<Measurement> result = getEntityFromCursor(cursor);
        cursor.close();
        return result;
    }

    public List<Measurement> findMeasurementsForLoc(Location loc){
        String rawQuery = "SELECT * FROM " + getDatabaseHelper().MEASUREMENT_TABLE + " INNER JOIN " + getDatabaseHelper().LOCATION_TABLE + " ON " + getDatabaseHelper().MEASUREMENT_TABLE + "." + Measurement.Field.BLOCK_ID + " = " + getDatabaseHelper().LOCATION_TABLE + "." + Location.Field.ID + " WHERE " + getDatabaseHelper().LOCATION_TABLE + "."+Location.Field.BLOCK + " = ? AND " + getDatabaseHelper().LOCATION_TABLE + "."+Location.Field.FLOOR + " = ? AND " + getDatabaseHelper().MEASUREMENT_TABLE + "."+Measurement.Field.LEVEL + " < ?";
        Cursor c = getDatabase().rawQuery(rawQuery, new String[]{loc.getBlock().toString(),loc.getFloor().toString(),String.valueOf("-85")} );
        c.moveToFirst();
        List<Measurement> result=getEntityFromCursor(c);
        c.close();
        return result;
    }

    public List<Measurement> findMeasurementsForBlockAndWifi(Location loc,Wifi wifi){
        String rawQuery = "SELECT * FROM " + getDatabaseHelper().MEASUREMENT_TABLE + " INNER JOIN " + getDatabaseHelper().LOCATION_TABLE + " ON " + getDatabaseHelper().MEASUREMENT_TABLE + "." + Measurement.Field.BLOCK_ID + " = " + getDatabaseHelper().LOCATION_TABLE + "." + Location.Field.ID + " WHERE " + getDatabaseHelper().LOCATION_TABLE + "."+Location.Field.BLOCK + " = ? AND " +getDatabaseHelper().MEASUREMENT_TABLE + "."+Measurement.Field.WIFI_ID + " = ?" ;
        Cursor c = getDatabase().rawQuery(rawQuery, new String[]{loc.getBlock().toString(),wifi.getId().toString()} );
        c.moveToFirst();
        List<Measurement> result=getEntityFromCursor(c);
        c.close();
        return result;
    }
}
