package sk.stuba.fei.indoorlocator.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import sk.stuba.fei.indoorlocator.database.DatabaseHelper;
import sk.stuba.fei.indoorlocator.database.DatabaseManager;
import sk.stuba.fei.indoorlocator.database.entities.Wifi;
import sk.stuba.fei.indoorlocator.database.exception.DatabaseException;

/**
 * Created by Patrik on 13.10.2016.
 */

public class WifiDAO extends  AbstractDAO<Wifi>{

    public WifiDAO(DatabaseManager dbHelper){
        super(dbHelper);
    }

    @Override
    public Long createEntity(Wifi wifi) {
        ContentValues values = new ContentValues();
        values.put(Wifi.Field.MAC_ADR,wifi.getMac());
        values.put(Wifi.Field.SSID,wifi.getSsid());
        Long l =getDatabase().insert(getDatabaseHelper().WIFI_TABLE,null,values);
        return l;
    }

    @Override
    public List<Wifi> getEntityFromCursor(Cursor cursor) {
        List<Wifi> result = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            Wifi wifi = new Wifi();
            wifi.setId(cursor.getLong(0));
            wifi.setMac(cursor.getString(1));
            wifi.setSsid(cursor.getString(2));
            wifi.setOnBlock(cursor.getString(3));
            result.add(wifi);
            cursor.moveToNext();
        }
        cursor.close();
        return result;
    }

    public Wifi findWifiByMac(String bsid) throws DatabaseException {
        Cursor cursor = getDatabase().query(getDatabaseHelper().WIFI_TABLE,null,Wifi.Field.MAC_ADR+"=?",new String[]{bsid},null,null,null);
        List<Wifi> result = getEntityFromCursor(cursor);
        if(result.size()>1)
            throw new DatabaseException("Non unique mac adress : "+bsid);

        cursor.close();
        return result.isEmpty() ? null : result.get(0);
    }
}
