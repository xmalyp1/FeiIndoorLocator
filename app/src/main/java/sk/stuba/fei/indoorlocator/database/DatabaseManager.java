package sk.stuba.fei.indoorlocator.database;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import sk.stuba.fei.indoorlocator.database.exception.DatabaseException;

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

    public void exportDB() throws DatabaseException, IOException {
        if(database == null) {
            open();
        } else {
            List<String> records = DatabaseUtils.getCSVRecords(this);
            File outFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ExportDB-" + System.currentTimeMillis() +".csv");

            OutputStreamWriter fileOutputStream = new OutputStreamWriter(new FileOutputStream(outFile));

            for(String record : records) {
                fileOutputStream.write(record);
            }

            fileOutputStream.close();
        }
    }

    public void ImportDB(File csvFile) throws IOException {
        if(database == null) {
            open();
        } else {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));

            String line;
            while((line = br.readLine()) != null) {
                DatabaseUtils.processCSVLine(this, line);
            }
        }
    }
}
