package sk.stuba.fei.indoorlocator.database;

import android.app.Activity;
import android.app.Application;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Patrik on 14.10.2016.
 */

public class DatabaseManager {
    private SQLiteDatabase database;
    private DatabaseHelper databaseHelper;

    public DatabaseManager(DatabaseHelper dbHelper){
        databaseHelper=dbHelper;
    }

    public SQLiteDatabase getDatabase(){
        return database;
    }

    public DatabaseHelper getDatabaseHelper(){
        return databaseHelper;
    }

    public void open() throws SQLException {
        database = databaseHelper.getWritableDatabase();
    }

    public void close() {
        databaseHelper.close();
    }

}
