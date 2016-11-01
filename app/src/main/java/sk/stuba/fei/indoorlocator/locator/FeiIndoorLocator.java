package sk.stuba.fei.indoorlocator.locator;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sk.stuba.fei.indoorlocator.database.DatabaseManager;
import sk.stuba.fei.indoorlocator.database.dao.LocationDAO;
import sk.stuba.fei.indoorlocator.database.dao.MeasurementDAO;
import sk.stuba.fei.indoorlocator.database.dao.WifiDAO;
import sk.stuba.fei.indoorlocator.database.entities.Location;
import sk.stuba.fei.indoorlocator.database.entities.Measurement;
import sk.stuba.fei.indoorlocator.database.entities.Wifi;
import sk.stuba.fei.indoorlocator.database.exception.DatabaseException;
import sk.stuba.fei.indoorlocator.locator.block.BlockLocator;
import sk.stuba.fei.indoorlocator.locator.floor.strategy.FloorLocator;

/**
 * Created by Patrik on 11.10.2016.
 */

public class FeiIndoorLocator {
    private FloorLocator floorLocator;
    private BlockLocator blockLocator;

    public FeiIndoorLocator(FloorLocator floorLocator){
        this.blockLocator = new BlockLocator();
        this.floorLocator = floorLocator;
    }

    public FeiIndoorLocator(){
        this.blockLocator = new BlockLocator();
    }

    public void setFloorLocator(FloorLocator floorLocator){
        this.floorLocator=floorLocator;
    }

    /**
     * Based on greedy approach find the local maximum and made a decision...
     * @param results scan results
     * @param manager DB connection
     * @return the actual location or null
     * @throws DatabaseException
     */
    @Nullable
    private Location getActualLocationGreedy(List<ScanResult> results, DatabaseManager manager) throws DatabaseException {
        LocationDAO locationDAO = new LocationDAO(manager);
        MeasurementDAO measurementDAO = new MeasurementDAO(manager);
        WifiDAO wifiDAO = new WifiDAO(manager);


        if(results == null || results.isEmpty())
            return null;

        Collections.sort(results,new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult o1, ScanResult o2) {
                return o1.level - o2.level;
            }
        });

        for(ScanResult scan : results) {
            Wifi wifi = wifiDAO.findWifiByMac(scan.BSSID);
            if(wifi == null)
                break;

            List<Measurement> measurements = measurementDAO.findMeasurementsForWifi(wifi);
            int diff = Integer.MAX_VALUE;
            Measurement selected = null;
            for(Measurement actualMeasurement : measurements){
                if(Math.abs(actualMeasurement.getLevel()-scan.level) < diff){
                    diff = Math.abs(actualMeasurement.getLevel()-scan.level);
                    selected = actualMeasurement;
                }
            }
            if(selected != null){
              return locationDAO.getLocationByID(selected.getBlockId());

            }
        }

        return null;
    }

    public Location getActualLocation(List<ScanResult> scanResults,DatabaseManager dbManager) throws DatabaseException {
        LocationDAO locationDAO = new LocationDAO(dbManager);
        if(floorLocator == null){
            return getActualLocationGreedy(scanResults,dbManager);
        }else {
            Character block = blockLocator.getActualBlock(scanResults);
            Integer floor = floorLocator.getFloorInfo(scanResults);
            return locationDAO.getLocation(block,floor);
        }
    }

}
