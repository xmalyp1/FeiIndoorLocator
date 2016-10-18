package sk.stuba.fei.indoorlocator.database.entities;

import android.database.Cursor;

import java.io.Serializable;

/**
 * Created by Patrik on 13.10.2016.
 */

public class Location extends AbstractEntity{


    public static final class Field{
        public static final String ID = "_id";
        public static final String BLOCK = "block";
        public static final String FLOOR = "floor";
        public static final String LAST_SCAN = "last_scan";
    }

    private Long id;
    private Character block;
    private Integer floor;
    private String lastScan;


    public Location() {

    }

    public Location(Character block,Integer floor){
        this.block = block;
        this.floor = floor;

    }

    public Character getBlock() {
        return block;
    }

    public void setBlock(Character block) {
        this.block = block;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getLastScan() {
        return lastScan;
    }

    public void setLastScan(String lastScan) {
        this.lastScan = lastScan;
    }
}
