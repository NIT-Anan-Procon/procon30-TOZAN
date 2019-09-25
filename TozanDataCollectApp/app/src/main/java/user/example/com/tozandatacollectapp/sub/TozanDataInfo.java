package user.example.com.tozandatacollectapp.sub;

public final class TozanDataInfo {

    private String dataName, dataPath, intStorage;

    public TozanDataInfo(String dataName, String dataPath, String intStorage){
        this.dataName = dataName;
        this.dataPath = dataPath;
        this.intStorage = intStorage;
    }

    public String getDataName() {
        return dataName;
    }

    public String getDataPath() {
        return dataPath;
    }

    public String getIntStorage() {
        return intStorage;
    }
}
