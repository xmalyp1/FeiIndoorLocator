package sk.stuba.fei.indoorlocator.locator;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

            if(wifi != null) {
                List<Measurement> measurements = measurementDAO.findMeasurementsForWifi(wifi);
                int diff = Integer.MAX_VALUE;
                Measurement selected = null;
                for (Measurement actualMeasurement : measurements) {
                    if (Math.abs(actualMeasurement.getLevel() - scan.level) < diff) {
                        diff = Math.abs(actualMeasurement.getLevel() - scan.level);
                        selected = actualMeasurement;
                    }
                }
                if (selected != null) {
                    return locationDAO.getLocationByID(selected.getBlockId());
                }
            }
        }

        return null;
    }

    @Nullable
    private Location getFloorLocation(List<ScanResult> results, DatabaseManager manager, Location actualLocation) throws DatabaseException{
        MeasurementDAO measurementDAO = new MeasurementDAO(manager);
        LocationDAO locationDAO = new LocationDAO(manager);
        WifiDAO wifiDAO = new WifiDAO(manager);

        //Listy
        List<ScanResult> scanResultWifiInDatabase = new ArrayList<ScanResult>();
        List<Measurement> allMeasurements=new ArrayList<Measurement>();
        List<Measurement> minimalCountOfMeasurements = new ArrayList<Measurement>();

        //Hash
        Map<Location, Double> countAllLocation = new HashMap<>();
        Map<Location, Double> minimalLocation = new HashMap<>();
        Map<Location, Double> countApproximatelyLocation = new HashMap<>();
        Map<Location, Double> countApproximatelyAllLocation = new HashMap<>();
        Map<Location, Double> relevantHashMap = new HashMap<>();

        // Integer
        int actualCountOfMeasurement = Integer.MAX_VALUE;
        int countOfMeasurement = 0;

        for(ScanResult scan : results) {
            Wifi wifi = wifiDAO.findWifiByMac(scan.BSSID);

            if (wifi != null){
                scanResultWifiInDatabase.add(scan);

                List<Measurement> measurements = measurementDAO.findMeasurementsForBlockAndWifi(actualLocation, wifi);
                countOfMeasurement = measurements.size();

                for(int i = 0; i <= 3 && countApproximatelyLocation.size() < 2; i++) {
                    countApproximatelyLocation.clear();
                    for (Measurement measurement : measurements) {
                        if (((measurement.getLevel() - scan.level) <= i) && ((measurement.getLevel() - scan.level) >= -i)) {
                            Location actualLoc = locationDAO.getLocationByID(measurement.getBlockId());
                            if (!countApproximatelyLocation.containsKey(actualLoc)) {
                                countApproximatelyLocation.put(actualLoc, 1.0);
                            }
                            else {
                                countApproximatelyLocation.put(actualLoc,
                                        countApproximatelyLocation.get(actualLoc) + 1.0);
                            }
                        }
                    }
                }

                getRelevantHashMap(countApproximatelyLocation, countApproximatelyAllLocation);

                if (countOfMeasurement != 0) {
                    allMeasurements.addAll(measurements);
                }

                if (countOfMeasurement != 0 && actualCountOfMeasurement > countOfMeasurement) {
                    actualCountOfMeasurement = countOfMeasurement;

                    minimalCountOfMeasurements.clear();
                    minimalCountOfMeasurements.addAll(measurements);
                }
                else if(countOfMeasurement != 0 && actualCountOfMeasurement == countOfMeasurement) {
                    minimalCountOfMeasurements.addAll(measurements);
                }
            }
        }
        if(allMeasurements.size() > 0) {
            for (Measurement actMeasurement : allMeasurements) {
                Location l = locationDAO.getLocationByID(actMeasurement.getBlockId());
                if (!countAllLocation.containsKey(l)) {
                    countAllLocation.put(l, 1.0);
                } else {
                    countAllLocation.put(l, countAllLocation.get(l) + 1.0);
                }
            }

            for (Measurement actMeasurement : minimalCountOfMeasurements) {
                Location l = locationDAO.getLocationByID(actMeasurement.getBlockId());
                if (!minimalLocation.containsKey(l)) {
                    minimalLocation.put(l, 1.0);
                } else {
                    minimalLocation.put(l, minimalLocation.get(l) + 1.0);
                }

            }


            countAllLocation = MapUtil.sortByValue(countAllLocation);


            Location firstLocation = countAllLocation.entrySet().iterator().next().getKey();

            Location firstApproximatelyLocation = null;


            if(!countApproximatelyAllLocation.isEmpty()){
                countApproximatelyAllLocation = MapUtil.sortByValue(countApproximatelyAllLocation);
                countApproximatelyAllLocation = relevantScoring(countApproximatelyAllLocation, countApproximatelyAllLocation.entrySet().iterator().next().getValue() > 5.0 ? 1.3 : 2.3);
                getRelevantHashMap(countApproximatelyAllLocation, relevantHashMap);

                firstApproximatelyLocation = countApproximatelyAllLocation.entrySet().iterator().next().getKey();
            }
            else if(!countApproximatelyLocation.isEmpty() && countApproximatelyAllLocation.isEmpty()){
                countApproximatelyLocation = MapUtil.sortByValue(countApproximatelyLocation);
                countApproximatelyLocation = relevantScoring(countApproximatelyLocation, countApproximatelyLocation.entrySet().iterator().next().getValue() > 5.0 ? 1.3 : 2.3);
                getRelevantHashMap(countApproximatelyLocation, relevantHashMap);

                firstApproximatelyLocation = countApproximatelyLocation.entrySet().iterator().next().getKey();
            }


            //Set differrent relevantScoring if data is incorect
            countAllLocation = relevantScoring(countAllLocation, 1.3);
            getRelevantHashMap(countAllLocation, relevantHashMap);
            minimalLocation = relevantScoring(minimalLocation, minimalLocation.size() > 1 ? 1.0 : 0.5);
            getRelevantHashMap(minimalLocation, relevantHashMap);

            //Sort relevat scoring bestAdept is first location from hashmap
            relevantHashMap = MapUtil.sortByValue(relevantHashMap);

            //this is old logic
//            Location bestAdept = null, optionalAdept = null, firstAdept = null;
//
//            for (Location loc : countAllLocation.keySet()) {
//                if (minimalLocation.containsKey(loc) && minimalCountOfMeasurements.size() > 1 && loc.getId().equals(countApproximatelyLocationLongId)) {
//                    bestAdept = loc;
//                } else if (minimalCountOfMeasurements.size() <= 1 && loc.getId().equals(countApproximatelyLocationLongId)) {
//                    optionalAdept = loc;
//                }
//            }
//
//            if ((minimalLocation.containsKey(firstLocation) && minimalCountOfMeasurements.size() > 1
//                    && (countApproximatelyLocation.containsKey(firstLocation.getId()) && firstAdept == null))) {
//                firstAdept = firstLocation;
//            } else {
//                firstAdept = minimalLocation.entrySet().iterator().next().getKey();
//            }
//
//
//            if (optionalAdept != null) {
//                firstAdept = optionalAdept;
//            }
//            if (bestAdept != null) {
//                firstAdept = bestAdept;
//            }
            return relevantHashMap.entrySet().iterator().next().getKey();
        }

        return null;
    }

    private void getRelevantHashMap(Map<Location, Double>  hashMapToRelevant, Map<Location, Double>  relevantHashMap){
        for (Location loc : hashMapToRelevant.keySet()) {
            if (!relevantHashMap.containsKey(loc)) {
                relevantHashMap.put(loc, hashMapToRelevant.get(loc));
            }
            else {
                relevantHashMap.put(loc,
                        relevantHashMap.get(loc) + hashMapToRelevant.get(loc));
            }
        }
    }

    private Map<Location, Double> relevantScoring(Map<Location, Double>  hashMapToScoring, Double relevancia){

        Map<Location, Double> scoringHashMap = new HashMap<>();
        boolean isFirst = true;
        Double theHighestNumber = 0.0;
        Integer maxThreeFloor = 0;

        Iterator iterator = hashMapToScoring.entrySet().iterator();

        while(iterator.hasNext() && maxThreeFloor < 3) {

            Map.Entry map = (Map.Entry) iterator.next();

            if(isFirst){
                theHighestNumber = (Double) map.getValue();
                isFirst = false;
            }

            if((theHighestNumber - (Double) map.getValue()) < 13){
                scoringHashMap.put((Location) map.getKey(), ((((Double) map.getValue())/ theHighestNumber) * relevancia));
            }

            maxThreeFloor++;
        }

        return scoringHashMap;
    }


    public Location getActualLocation(List<ScanResult> scanResults,DatabaseManager dbManager) throws DatabaseException {
        LocationDAO locationDAO = new LocationDAO(dbManager);
        if(floorLocator == null){

            Location actualLocation =  getActualLocationGreedy(scanResults,dbManager);

            if(actualLocation != null ){
                if(actualLocation.getBlock().equals('A') || actualLocation.getBlock().equals('B') || actualLocation.getBlock().equals('C') || actualLocation.getBlock().equals('D')  || actualLocation.getBlock().equals('E')){
                    return getFloorLocation(scanResults,dbManager, actualLocation);
                }
            }

            return getActualLocationGreedy(scanResults,dbManager);
        }else {
            Character block = blockLocator.getActualBlock(scanResults);
            Integer floor = floorLocator.getFloorInfo(scanResults);
            return locationDAO.getLocation(block,floor);
        }
    }

}

class MapUtil
{
    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
}
