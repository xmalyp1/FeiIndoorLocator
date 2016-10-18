package sk.stuba.fei.indoorlocator.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sk.stuba.fei.indoorlocator.database.DatabaseHelper;
import sk.stuba.fei.indoorlocator.database.DatabaseManager;
import sk.stuba.fei.indoorlocator.database.entities.Location;
import sk.stuba.fei.indoorlocator.database.entities.Wifi;
import sk.stuba.fei.indoorlocator.database.exception.DatabaseException;

/**
 * Created by Patrik on 13.10.2016.
 */

public class LocationDAO extends AbstractDAO<Location> {

    public LocationDAO(DatabaseManager dbHelper){
        super(dbHelper);
    }

    @Override
    public Long createEntity(Location location) {
        ContentValues values = new ContentValues();
        values.put(Location.Field.BLOCK,location.getBlock().toString());
        values.put(Location.Field.FLOOR,location.getFloor().toString());
        Long id = getDatabase().insert(getDatabaseHelper().LOCATION_TABLE,null,values);
        return id;
    }

    @Override
    public List<Location> getEntityFromCursor(Cursor cursor) {
        List<Location> result = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            Location location = new Location();
            location.setId(cursor.getLong(0));
            location.setBlock(Character.valueOf(cursor.getString(1).charAt(0)));
            location.setFloor(cursor.getInt(2));
            location.setLastScan(cursor.getString(3));
            result.add(location);
            cursor.moveToNext();
        }
        cursor.close();
        return result;
    }

    public Location getLocation(Character block,Integer floor) throws DatabaseException {
        Cursor c = getDatabase().query(getDatabaseHelper().LOCATION_TABLE,null,Location.Field.BLOCK+"=? AND "+Location.Field.FLOOR+"=?",new String[]{block.toString(),floor.toString()},null,null,null);
        List<Location> result = getEntityFromCursor(c);
        if(result.size()>1)
            throw new DatabaseException("Non unique location : ["+block +" "+floor+"]");

        return result.isEmpty() ? null : result.get(0);
    }

    public void populateLocation(){

        populateBlock('A',0,6);
        populateBlock('B',0,6);
        populateBlock('C',0,6);
        populateBlock('D',0,6);
        populateBlock('E',-1,6);


    }

    private void populateBlock(Character block,int from,int to){
        for (int i = from; i<= to ; i++){
            createEntity(new Location(block,i));
        }
    }

    public List<Location> getAllLocations(){
        Cursor c = getDatabase().query(getDatabaseHelper().LOCATION_TABLE,null,null,null,null,null,Location.Field.BLOCK);
        return getEntityFromCursor(c);
    }

    public void updateLastScanForLocation(Location loc){
        ContentValues values = new ContentValues();
        values.put(Location.Field.LAST_SCAN,DatabaseHelper.getDate(new Date()));
        getDatabase().update(DatabaseHelper.LOCATION_TABLE,values,Location.Field.ID+"=?",new String[]{loc.getId().toString()});
    }
}

