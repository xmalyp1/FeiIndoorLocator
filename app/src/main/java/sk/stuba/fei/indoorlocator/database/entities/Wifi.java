package sk.stuba.fei.indoorlocator.database.entities;

import android.database.Cursor;

/**
 * Created by Patrik on 13.10.2016.
 */

public class Wifi extends AbstractEntity{

    public Wifi() {

    }

    public static final class Field{
        public static final String ID = "_id";
        public static final String SSID = "ssid";
        public static final String MAC_ADR = "bsid";
        public static final String ONLY_ON_BLOCK = "only_on_block";
    }

    private Long id;
    private String ssid;
    private String mac;
    private String onBlock;

    public Wifi(String ssid,String mac){
        this.ssid = ssid;
        this.mac = mac;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getOnBlock() {
        return onBlock;
    }

    public void setOnBlock(String onBlock) {
        this.onBlock = onBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Wifi wifi = (Wifi) o;

        if (id != null ? !id.equals(wifi.id) : wifi.id != null) return false;
        if (ssid != null ? !ssid.equals(wifi.ssid) : wifi.ssid != null) return false;
        if (mac != null ? !mac.equals(wifi.mac) : wifi.mac != null) return false;
        return onBlock != null ? onBlock.equals(wifi.onBlock) : wifi.onBlock == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (ssid != null ? ssid.hashCode() : 0);
        result = 31 * result + (mac != null ? mac.hashCode() : 0);
        result = 31 * result + (onBlock != null ? onBlock.hashCode() : 0);
        return result;
    }
}
