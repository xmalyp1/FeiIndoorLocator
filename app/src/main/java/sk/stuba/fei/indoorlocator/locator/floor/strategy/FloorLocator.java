package sk.stuba.fei.indoorlocator.locator.floor.strategy;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by Patrik on 11.10.2016.
 */

public interface FloorLocator {

    public Integer getFloorInfo(List<ScanResult> scanResultList);

}
