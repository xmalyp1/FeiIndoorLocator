package sk.stuba.fei.indoorlocator.database;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sk.stuba.fei.indoorlocator.database.dao.LocationDAO;
import sk.stuba.fei.indoorlocator.database.dao.MeasurementDAO;
import sk.stuba.fei.indoorlocator.database.dao.WifiDAO;
import sk.stuba.fei.indoorlocator.database.entities.Location;
import sk.stuba.fei.indoorlocator.database.entities.Measurement;
import sk.stuba.fei.indoorlocator.database.entities.Wifi;
import sk.stuba.fei.indoorlocator.database.exception.DatabaseException;
import sk.stuba.fei.indoorlocator.utils.ScanDataDTO;

/**
 * Created by Martin on 25.10.2016.
 */

public class DatabaseUtils {

    private static final String TAG = "DatabaseUtils";
    private static final String CSV_DATA_SEPARATOR = ",";

    public static List<String> getCSVRecords(DatabaseManager dm) throws DatabaseException {
        List<String> records = new ArrayList<>();

        MeasurementDAO measurementDAO = new MeasurementDAO(dm);
        LocationDAO locationDAO = new LocationDAO(dm);
        WifiDAO wifiDAO = new WifiDAO(dm);

        List<Measurement> measurements = measurementDAO.getAllMeasurements();

        for(Measurement measurement : measurements) {
            StringBuilder sb = new StringBuilder();
            Wifi wifi = wifiDAO.findWifiByID(measurement.getWifiId());
            Location location = locationDAO.getLocationByID(measurement.getBlockId());

            sb.append(location.getBlock().toString() + CSV_DATA_SEPARATOR);
            sb.append(location.getFloor().toString() + CSV_DATA_SEPARATOR);
            sb.append(wifi.getMac() + CSV_DATA_SEPARATOR);
            sb.append(wifi.getSsid() + CSV_DATA_SEPARATOR);
            sb.append(measurement.getLevel() + System.lineSeparator());

            records.add(sb.toString());
        }

        return records;
    }

    public static void processCSVLine(DatabaseManager dm, String line) {
        String[] dataArray = line.split(CSV_DATA_SEPARATOR);
        Date d = new Date();
        if(dataArray.length == 5) {
            MeasurementDAO measurementDAO = new MeasurementDAO(dm);
            LocationDAO locationDAO = new LocationDAO(dm);
            WifiDAO wifiDAO = new WifiDAO(dm);

            Location location = new Location(dataArray[0].charAt(0), Integer.parseInt(dataArray[1]));
            Long locationId = locationDAO.createEntity(location);
            location.setId(locationId);
            locationDAO.updateLastScanForLocation(location);
            Wifi wifi = new Wifi(dataArray[3], dataArray[2]);
            Long wifiId = wifiDAO.createEntity(wifi);

            Measurement measurement = new Measurement(Integer.parseInt(dataArray[4]),locationId,wifiId);
            Long measurementId = measurementDAO.createEntity(measurement);

        } else {
            Log.e(TAG, "There must be 5 params: " + dataArray.toString());
        }

    }


    public static List<ScanDataDTO> getScanDataForLocation(Location location, DatabaseManager dbm){

        if(dbm == null || location == null || location.getId() == null) {
            Log.w("FEI_ScanDataForLocation","Unable to perform the operation. Retrieving empty list.");
            return new ArrayList<>();
        }
        Set<ScanDataDTO> results = new HashSet<>();
        MeasurementDAO measurementDAO = new MeasurementDAO(dbm);
        WifiDAO wifiDAO = new WifiDAO(dbm);

        List<Measurement> measurements = measurementDAO.findMeasurementsForLocation(location);
        for(Measurement measurement : measurements){
            try {
                Wifi wifi = wifiDAO.findWifiByID(measurement.getWifiId());
                results.add(new ScanDataDTO(wifi.getSsid(),wifi.getMac(),null));
            } catch (DatabaseException e) {
                Log.e("FEI_DB_EXCEPTION",e.getMessage());
            }
        }
        return new ArrayList<>(results);
    }
}
