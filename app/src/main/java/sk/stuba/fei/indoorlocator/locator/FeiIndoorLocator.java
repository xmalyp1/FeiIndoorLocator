package sk.stuba.fei.indoorlocator.locator;

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
}
