package gist.mlv.doorlock;

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

    public static String PREFERENCE = "MLV_Prefers";

    public Device(String id, String user, String password){
        UserName = user;
        ID = id;
        Password = password;
    }
    public Device(){ }

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

    public void savePreferences(SharedPreferences prefs){
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

    public String connectLocal(){
        //return "http://" + IpAdress + ":8555";
        return "http://172.26.19.213:8555";
    }

    public String connectMLV(){
        return "http://" + IpAdress + ":8555";
    }
}
