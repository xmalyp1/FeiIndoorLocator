package sk.stuba.fei.indoorlocator.utils;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Patrik on 7.11.2016.
 */

public class ScanResultMapper {

    public static List<ScanDataDTO> mapScanResults(List<ScanResult> wifiResults){
        List<ScanDataDTO> result = new ArrayList<>();
        for(ScanResult scan : wifiResults){
            result.add(new ScanDataDTO(scan.SSID,scan.BSSID,scan.level));
        }
        return result;
    }
}
