package sk.stuba.fei.indoorlocator.utils;

/**
 * Created by Patrik on 7.11.2016.
 */

public class ScanDataDTO {
    private String name;
    private String mac;
    private Integer level;

    public ScanDataDTO(String name,String mac, Integer level){
        this.name = name;
        this.mac = mac;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "ScanDataDTO{" +
                "name='" + name + '\'' +
                ", mac='" + mac + '\'' +
                ", level=" + level +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScanDataDTO)) return false;

        ScanDataDTO that = (ScanDataDTO) o;

        if (!name.equals(that.name)) return false;
        if (!mac.equals(that.mac)) return false;
        return level != null ? level.equals(that.level) : that.level == null;

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + mac.hashCode();
        result = 31 * result + (level != null ? level.hashCode() : 0);
        return result;
    }
}
