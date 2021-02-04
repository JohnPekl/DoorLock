package gist.mlv.doorlock;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import gist.mlv.doorlock.network.HttpRequest;

public class MainActivity extends Activity implements View.OnClickListener {
    /**
     * Called when the activity is first created.
     */
    private String TAG = "MainActivity";

    private Button mAddDevice;
    private Device mDevice;
    private Handler mMainHandler;
    private TextView mEmptyDeviceTxt;
    private ListView mDeviceListView;
    private ArrayList<Device> mDeviceArrList;
    private ArrayAdapter<Device> mDeviceAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mAddDevice = (Button) findViewById(R.id.btn_add_device);
        mDeviceListView = findViewById(R.id.device_list);
        mEmptyDeviceTxt = findViewById(R.id.empty_device_list);
        mAddDevice.setOnClickListener(this);

        if (!requestPermission(Manifest.permission.CAMERA)) {
            int REQUEST_CODE = 1001;
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.INTERNET}, REQUEST_CODE);
        }

        getAllPreferense();
        mMainHandler = new Handler();
    }

    private boolean requestPermission(String permission) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    private void getAllPreferense() {
        SharedPreferences prefs = getSharedPreferences(Device.PREFERENCE, Context.MODE_PRIVATE);
        mDeviceArrList = new ArrayList<Device>();
        mDeviceAdapter = new DeviceAdapter(this, MainActivity.this, mDeviceArrList);
        mDeviceListView.setAdapter(mDeviceAdapter);

        Map<String, ?> allEntries = prefs.getAll();
        if (allEntries.size() > 0) {
            mEmptyDeviceTxt.setVisibility(View.GONE);
            mDeviceListView.setVisibility(View.VISIBLE);
        }
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Gson gson = new Gson();
            String json = prefs.getString(entry.getKey(), "");
            Device obj = gson.fromJson(json, Device.class);
            mDeviceArrList.add(obj);
            mDeviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_device:
                mDevice = new Device("000-000-000", "admin", "admin");
                showWIFIDialog();
                //connect2Wifi("mlv-door5g", "wlsfkaus");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    /*https://code.tutsplus.com/tutorials/storing-data-securely-on-android--cms-30558
     * Since 6.0 Marshmallow, full-disk encryption is enabled by default, for devices with the capability.
     * Files and SharedPreferences that are saved by the app are automatically set with the MODE_PRIVATE constant.
     * This means the data can be accessed only by your own app. */
    private void showWIFIDialog() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = layoutInflater.inflate(R.layout.input_wifi_info, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(R.string.wifi_title);
        alertDialog.setCancelable(false);

        final EditText ssid_edt = (EditText) view.findViewById(R.id.wifi_name);
        final EditText pwd_edt = (EditText) view.findViewById(R.id.wifi_pwd);
        final LinearLayout pwd_layout = (LinearLayout) view.findViewById(R.id.wifi_pwd_layout);
        final CheckBox cbx = (CheckBox) view.findViewById(R.id.wifi_cbx);
        final EditText name_edt = (EditText) view.findViewById(R.id.device_name);
        Button wifi_ok = (Button) view.findViewById(R.id.wifi_ok);
        Button wifi_cancel = (Button) view.findViewById(R.id.wifi_cancel);

        cbx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cbx.isChecked()) {
                    pwd_layout.setVisibility(View.GONE);
                    pwd_edt.setText("");
                } else {
                    pwd_layout.setVisibility(View.VISIBLE);
                }
            }
        });
        wifi_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        wifi_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String device_name = name_edt.getText().toString();
                String ssid = ssid_edt.getText().toString();
                String pwd = pwd_edt.getText().toString();

                if (device_name.length() == 0 || ssid.length() == 0) {
                    Toast.makeText(MainActivity.this, R.string.toast_name_ssid, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (device_name.length() > 10) {
                    Toast.makeText(MainActivity.this, R.string.toast_len, Toast.LENGTH_SHORT).show();
                    return;
                }

                mDevice.setName(device_name);
                mDevice.setWifiSSID(ssid);
                mDevice.setWifiPassword(pwd);
                if (mDeviceArrList.contains(mDevice)) { // checking whether device is added or not
                    Toast.makeText(MainActivity.this, R.string.toast_exist, Toast.LENGTH_SHORT).show();
                    return;
                }
                mDeviceArrList.add(mDevice);
                mDeviceAdapter.notifyDataSetChanged();
                mDevice.savePreferences(getSharedPreferences(Device.PREFERENCE, Context.MODE_PRIVATE));
                if (mDeviceListView.getVisibility() == View.GONE) {
                    mDeviceListView.setVisibility(View.VISIBLE);
                    mEmptyDeviceTxt.setVisibility(View.GONE);
                }

                final String ssid_t = ssid;
                final String password_t = pwd;
                final Context context_t = MainActivity.this;
                final ProgressDialog progressBar = new ProgressDialog(context_t);
                progressBar.setCancelable(false);//you can cancel it by pressing back button
                progressBar.setMessage(getString(R.string.progress_connect_wifi));
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.setProgress(0);//initially progress is 0
                progressBar.setMax(100);//sets the maximum value 100
                progressBar.show();//displays the progress bar

                (new Thread() {
                    @Override
                    public void run() {
                        connect2Wifi(context_t, ssid_t, password_t);
                        try {
                            sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        scanLocalWifi(context_t, progressBar);
                    }
                }).start();
                alertDialog.dismiss();
                Toast.makeText(MainActivity.this, R.string.toast_save, Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.setView(view);
        alertDialog.show();
    }

    private void connect2Wifi(Context context, String ssid, String pwd) {
        WifiConfiguration wifiConfig = new WifiConfiguration();

        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", pwd);

        WifiManager wifiManager = (WifiManager) context.getSystemService(android.content.Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        boolean result = wifiManager.enableNetwork(netId, true);
        wifiManager.saveConfiguration();
        //boolean result = wifiManager.reconnect();
    }

    private void scanLocalWifi(final Context context, final ProgressDialog progressBar) {
        WifiManager wifii = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo d = wifii.getDhcpInfo();

        String s_dns1 = "DNS 1: " + intToIp(d.dns1);
        String s_dns2 = "DNS 2: " + intToIp(d.dns2);
        String s_gateway = "Default Gateway: " + intToIp(d.gateway);
        String s_ipAddress = "IP Address: " + intToIp(d.ipAddress);
        String s_leaseDuration = "Lease Time: " + intToIp(d.leaseDuration);
        String s_netmask = "Subnet Mask: " + intToIp(d.netmask);
        String s_serverAddress = "Server IP: " + intToIp(d.serverAddress);
        //dispaly them
        Log.d(TAG, "Network Info\n" + s_dns1 + "\n" + s_dns2 + "\n" + s_gateway + "\n" +
                s_ipAddress + "\n" + s_leaseDuration + "\n" + s_netmask + "\n" + s_serverAddress);

        int start = d.gateway & d.netmask << 1;
        int end = d.gateway | ~d.netmask;

        HttpRequest http = new HttpRequest();
        boolean result = true; // true for test
        displayProgressUI(context, progressBar, context.getString(R.string.progress_scan));
        for (int i = 0; i < 255; i++) {
            int last = (((start >> 24) + i) << 24);
            int first = ((start << 8) >> 8);
            int next = last | first;
            if (next == d.gateway || next == d.ipAddress) { // ignore itself and gateway
                continue;
            }
            //
            final String nextIp = intToIp(next);
            displayProgressUI(context, progressBar, context.getString(R.string.progress_scan) + "\n" + nextIp);
            String checkDevice = "http://" + nextIp + ":8555/?check_dev=" + mDevice.getID();
            checkDevice = "http://172.126.19.213:8555/?check_dev=000-000-000";
            String changeWifi = "http://" + nextIp + ":8555/set_wifi?ssid=" + mDevice.getWifiSSID() + "&password=" + mDevice.getWifiPassword();
            changeWifi = "http://172.126.19.213:8555/set_wifi?ssid=mlv-306&password=wlsfkaus";
            String[] params = new String[]{checkDevice, changeWifi};
            result = http.configDevice(params);
            if (result) {
                mDevice.setIpAdress("172.26.19.213"); //nextIp
                mDevice.savePreferences(context.getSharedPreferences(Device.PREFERENCE, Context.MODE_PRIVATE));
                makeToastUI(context, R.string.toast_config);
                break;
            }
        }
        displayProgressUI(context, progressBar, "");
        if (!result){
            makeToastUI(context, R.string.toast_config_error);
        }
    }

    private void makeToastUI(final Context context, final int s){
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProgressUI(final Context context, final ProgressDialog progressBar, final String s){
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if(s.equals("")){
                    progressBar.dismiss();
                } else {
                    progressBar.setMessage(s);
                }
            }
        });
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 24) & 0xFF);
    }
}