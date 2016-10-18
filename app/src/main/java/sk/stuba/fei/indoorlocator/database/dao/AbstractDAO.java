package sk.stuba.fei.indoorlocator.database.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import sk.stuba.fei.indoorlocator.database.DatabaseHelper;
import sk.stuba.fei.indoorlocator.database.DatabaseManager;
import sk.stuba.fei.indoorlocator.database.entities.AbstractEntity;

/**
 * Created by Patrik on 13.10.2016.
 */

public abstract class AbstractDAO<T extends AbstractEntity> {


    private DatabaseManager manager;
    private T template;

    public AbstractDAO(DatabaseManager manager){
        this.manager=manager;
    }

    public SQLiteDatabase getDatabase(){
        return manager.getDatabase();
    }

    public DatabaseHelper getDatabaseHelper(){
        return manager.getDatabaseHelper();
    }

    public abstract Long createEntity(T t);

    public abstract List<T> getEntityFromCursor(Cursor cursor);
}
