package sk.stuba.fei.indoorlocator.database.entities;

import android.database.Cursor;

/**
 * Created by Patrik on 13.10.2016.
 */

public class Measurement extends AbstractEntity{

    public static final class Field {
        public static final String ID = "_id";
        public static final String LEVEL = "level";
        public static final String BLOCK_ID = "block_id";
        public static final String WIFI_ID = "wifi_id";
    }

    private Long id;
    private Integer level;
    private Long blockId;
    private Long wifiId;

    public Measurement(){}

    public Measurement (Integer level,Long block,Long wifiId){
        this.level=level;
        this.blockId = block;
        this.wifiId = wifiId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getWifiId() {
        return wifiId;
    }

    public void setWifiId(Long wifiId) {
        this.wifiId = wifiId;
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }



}
