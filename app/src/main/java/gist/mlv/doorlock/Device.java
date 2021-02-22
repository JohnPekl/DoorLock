package gist.mlv.doorlock;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class Device {
    private String id;
    private String name;
    private String userName;
    private String password;
    private String wifiSSID;
    private String wifiPassword;
    private String ipAdress;
    private String streamingKey;
    private boolean isLocalOnline;

    public static String PREFERENCE = "MLV_Prefers";

    public Device(String id, String user, String password) {
        userName = user;
        this.id = id;
        this.password = password;
    }

    public Device() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getWifiSSID() {
        return wifiSSID;
    }

    public void setWifiSSID(String wifiSSID) {
        this.wifiSSID = wifiSSID;
    }

    public String getWifiPassword() {
        return wifiPassword;
    }

    public void setWifiPassword(String wifiPassword) {
        this.wifiPassword = wifiPassword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAdress() {
        return ipAdress;
    }

    public void setIpAdress(String ipAdress) {
        this.ipAdress = ipAdress;
    }

    public String getStreamingKey() {
        return streamingKey;
    }

    public void setStreamingKey(String streamingKey) {
        this.streamingKey = streamingKey;
    }

    public boolean getLocalOnline() {
        return isLocalOnline;
    }

    public void setLocalOnline(boolean online) {
        isLocalOnline = online;
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
        prefsEditor.putString(id, json);
        prefsEditor.commit();
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;
        if (object != null && object instanceof Device) {
            isEqual = (this.id.equals(((Device) object).getId()));
        }
        return isEqual;
    }

    public String getUrlLocal() {
        return "http://" + ipAdress + ":8555";
    }

    public String getUrlMLV() {
        return "http://mlv.co.kr/ict/showvid.php?devid='" + id + "'&key='" + streamingKey + "'";
    }

    public String getUrlCheckDevice() {
        return "http://" + ipAdress + ":8555/get_dev";
    }

    public String getUrlChangeWIFI() {
        return "http://" + ipAdress + ":8555/set_wifi?ssid=" + wifiSSID + "&password=" + wifiPassword;
    }
}
