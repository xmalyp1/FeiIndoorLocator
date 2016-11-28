package sk.stuba.fei.indoorlocator.utils;

import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Patrik on 28/11/2016.
 */

public class NullComparator {
    public static Comparator<ScanDataDTO> atEnd(final Comparator<ScanDataDTO> comparator) {
        return new Comparator<ScanDataDTO>() {

            public int compare(ScanDataDTO o1, ScanDataDTO o2) {
                if (o1.getLevel() == null && o2.getLevel() == null) {
                    return 0;
                }

                if (o1.getLevel() == null) {
                    return 1;
                }

                if (o2.getLevel() == null) {
                    return -1;
                }

                return comparator.compare(o1, o2);
            }
        };
    }

    public static Comparator<ScanDataDTO> atBeginning(final Comparator<ScanDataDTO> comparator) {
        return Collections.reverseOrder(atEnd(comparator));
    }
}
