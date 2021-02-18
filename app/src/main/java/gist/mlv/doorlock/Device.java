package gist.mlv.doorlock;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class Device {
    private String ID;
    private String Name;
    private String UserName;
    private String Password;
    private String WifiSSID;
    private String WifiPassword;
    private String IpAdress;
    private String StreamingKey;

    public static String PREFERENCE = "MLV_Prefers";

    public Device(String id, String user, String password) {
        UserName = user;
        ID = id;
        Password = password;
    }

    public Device() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getWifiSSID() {
        return WifiSSID;
    }

    public void setWifiSSID(String wifiSSID) {
        WifiSSID = wifiSSID;
    }

    public String getWifiPassword() {
        return WifiPassword;
    }

    public void setWifiPassword(String wifiPassword) {
        WifiPassword = wifiPassword;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getIpAdress() {
        return IpAdress;
    }

    public void setIpAdress(String ipAdress) {
        IpAdress = ipAdress;
    }

    public String getStreamingKey() {
        return StreamingKey;
    }

    public void setStreamingKey(String streamingKey) {
        StreamingKey = streamingKey;
    }

    /*https://code.tutsplus.com/tutorials/storing-data-securely-on-android--cms-30558
     * Since 6.0 Marshmallow, full-disk encryption is enabled by default, for devices with the capability.
     * Files and SharedPreferences that are saved by the app are automatically set with the MODE_PRIVATE constant.
     * This means the data can be accessed only by your own app. */
    public void savePreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Device.PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(this);
        prefsEditor.putString(ID, json);
        prefsEditor.commit();
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;
        if (object != null && object instanceof Device) {
            isEqual = (this.ID.equals(((Device) object).getID()));
        }
        return isEqual;
    }

    public String getUrlLocal() {
        return "http://" + IpAdress + ":8555";
    }

    public String getUrlMLV() {
        return "http://mlv.co.kr/ict/showvid.php?devid=%27" + ID + "%27&key=%" + StreamingKey + "%27";
    }

    public String getUrlcheckDevice() {
        return "http://" + IpAdress + ":8555/get_dev";
    }

    public String getUrlChangeWIFI() {
        return "http://" + IpAdress + ":8555/set_wifi?ssid=" + WifiSSID + "&password=" + WifiPassword;
    }
}
