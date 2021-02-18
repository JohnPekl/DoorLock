package gist.mlv.doorlock;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.RouteInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import gist.mlv.doorlock.network.HttpRequest;

public class MainActivity extends Activity implements View.OnClickListener {
    /**
     * Called when the activity is first created.
     */
    private String TAG = "MainActivity";
    private String LANGUAGE_PREF = "LANGUAGE_PREF";

    private ImageButton mAddDevice;
    private Device mDevice;
    private ProgressBar mProgressScan;
    private boolean mScanning;
    private Handler mMainHandler;
    private TextView mEmptyDeviceTxt;
    private ListView mDeviceListView;
    private ArrayList<Device> mDeviceArrList;
    private ArrayAdapter<Device> mDeviceAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mAddDevice = findViewById(R.id.btn_add_device);
        mDeviceListView = findViewById(R.id.device_list);
        mEmptyDeviceTxt = findViewById(R.id.empty_device_list);
        mAddDevice.setOnClickListener(this);
        findViewById(R.id.imb_setting).setOnClickListener(this);
        mProgressScan = findViewById(R.id.pgb_scan_device);

        if (!requestPermission(Manifest.permission.CAMERA)) {
            int REQUEST_CODE = 1001;
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.INTERNET}, REQUEST_CODE);
        }

        getAllPreferense();
        registerForContextMenu(mDeviceListView);
        mMainHandler = new Handler();

        //scan devices
        mScanning = false;
        (new Thread() {
            @Override
            public void run() {
                scanLocalWifi(MainActivity.this);
            }
        }).start();
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
        mDeviceListView.setDividerHeight(2);

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

        prefs = getPreferences(Context.MODE_PRIVATE);
        String localeCode = prefs.getString(LANGUAGE_PREF, "en");
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(localeCode.toLowerCase()));
        res.updateConfiguration(conf, dm);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mScanning = true; // the activity is no longer visible, stop scanning
    }

    @Override
    public void onClick(View view) {
        Context context = MainActivity.this;
        switch (view.getId()) {
            case R.id.btn_add_device:
                mDevice = new Device("000-000-000" + Math.random(), "admin", "admin");
                showWIFIDialog();
                //connect2Wifi("mlv-door5g", "wlsfkaus");
                break;
            case R.id.imb_setting:
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view_setting = layoutInflater.inflate(R.layout.langguage_setting, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle(R.string.lang_ch);
                alertDialog.setCancelable(true);

                view_setting.findViewById(R.id.btn_lang_en).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setAppLocale("en");
                        alertDialog.dismiss();
                    }
                });
                view_setting.findViewById(R.id.btn_lang_vi).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setAppLocale("vi");
                        alertDialog.dismiss();
                    }
                });
                view_setting.findViewById(R.id.btn_lang_ko).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setAppLocale("ko");
                        alertDialog.dismiss();
                    }
                });
                alertDialog.setView(view_setting);
                alertDialog.show();
                break;
        }
    }

    private void setAppLocale(String localeCode) {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        Context context = MainActivity.this;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            LocaleList ll = conf.getLocales();
            for (int i = 0; i < ll.size(); i++) {
                if (!ll.get(i).getCountry().equals(localeCode)) {
                    conf.setLocale(new Locale(localeCode.toLowerCase()));
                }
            }
        } else {
            Locale locale = context.getResources().getConfiguration().locale;
            if (!locale.getCountry().equals(localeCode)) {
                conf.setLocale(new Locale(localeCode.toLowerCase()));
            }
        }
        SharedPreferences.Editor prefsEditor = getPreferences(Context.MODE_PRIVATE).edit();
        prefsEditor.putString(LANGUAGE_PREF, localeCode);
        prefsEditor.commit();

        res.updateConfiguration(conf, dm);
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_delete, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int position = info.position;
        final Context context = MainActivity.this;

        switch (item.getItemId()) {
            case R.id.menu_edit:
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View view = layoutInflater.inflate(R.layout.input_wifi_info, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle(R.string.wifi_title);
                alertDialog.setCancelable(false);

                final EditText edt_ssid = (EditText) view.findViewById(R.id.wifi_name);
                final EditText edt_password = (EditText) view.findViewById(R.id.wifi_pwd);
                final LinearLayout pwd_layout = (LinearLayout) view.findViewById(R.id.wifi_pwd_layout);
                final CheckBox cbx = (CheckBox) view.findViewById(R.id.wifi_cbx);
                final EditText edt_name = (EditText) view.findViewById(R.id.device_name);
                Button btn_wifiok = (Button) view.findViewById(R.id.wifi_ok);
                Button btn_wificancel = (Button) view.findViewById(R.id.wifi_cancel);

                final Device device = mDeviceArrList.get(position);
                edt_ssid.setText(device.getWifiSSID());
                edt_name.setText(device.getName());
                if (device.getWifiPassword().length() == 0) {
                    cbx.setChecked(true);
                    pwd_layout.setVisibility(View.GONE);
                } else {
                    edt_password.setText(device.getWifiPassword());
                }
                alertDialog.setView(view);
                alertDialog.show();
                btn_wificancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
                btn_wifiok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String device_name = edt_name.getText().toString();
                        String ssid = edt_ssid.getText().toString();
                        String pwd = edt_password.getText().toString();
                        alertDialog.dismiss();

                        device.setName(device_name);
                        device.setWifiSSID(ssid);
                        device.setWifiPassword(pwd);

                        mDeviceArrList.set(position, device);
                        mDeviceAdapter.notifyDataSetChanged();
                        device.savePreferences(context.getSharedPreferences(Device.PREFERENCE, Context.MODE_PRIVATE));
                        Toast.makeText(context, R.string.toast_update, Toast.LENGTH_SHORT).show();
                    }
                });
                cbx.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (cbx.isChecked()) {
                            pwd_layout.setVisibility(View.GONE);
                            edt_password.setText("");
                        } else {
                            pwd_layout.setVisibility(View.VISIBLE);
                        }
                    }
                });
                return true;
            case R.id.menu_delete:
                Device device_del = mDeviceArrList.get(position);
                SharedPreferences.Editor editor = context.getSharedPreferences(Device.PREFERENCE, Context.MODE_PRIVATE).edit();
                editor.remove(device_del.getID()).apply();
                mDeviceArrList.remove(device_del);
                mDeviceAdapter.notifyDataSetChanged();
                Toast.makeText(context, R.string.toast_delete, Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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

    private void scanLocalWifi(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = cm.getAllNetworks();
        String gateway = "";
        String myIp = "";
        for (Network network : networks) {
            LinkProperties linkProperties = cm.getLinkProperties(network);
            List<LinkAddress> linkAddresses = linkProperties.getLinkAddresses();
            List<RouteInfo> routes = linkProperties.getRoutes();
            for (LinkAddress addr : linkAddresses) {
                if (addr.getAddress() instanceof Inet4Address) {
                    //prefixLen = addr.getPrefixLength();
                    myIp = addr.getAddress().getHostAddress();
                }
            }
            for (RouteInfo route : routes) {
                if (route.getGateway() instanceof Inet4Address && !route.getGateway().isAnyLocalAddress()) {
                    gateway = route.getGateway().getHostAddress();
                }
            }
        }

        HttpRequest http = new HttpRequest();
        String result = "";
        String prefix = myIp.substring(0, myIp.lastIndexOf(".") + 1);

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mProgressScan.setVisibility(View.VISIBLE);
            }
        });

        for (int i = 0; i <= 255; i++) {
            if(mScanning){
                break;
            }
            String nextIp = prefix + i;
            if (nextIp.equals(myIp) || nextIp.equals(gateway)) {
                continue;
            }
            //
            String getDevice = "http://" + nextIp + ":8555/get_dev";
            result = http.checkDevice(getDevice);

            boolean alreadyStored = false;
            for (int idx = 0; idx < mDeviceArrList.size(); idx++) {
                if (mDeviceArrList.get(idx).getID().equals(result)) {
                    alreadyStored = true;
                    break; // ignore if it is already stored
                }
            }
            if (alreadyStored) {
                continue;
            }

            if (!result.equals("")) {
                mDevice = new Device(result, "", "");
                mDevice.setIpAdress(nextIp);
                mDevice.setName(context.getString(R.string.hint_device_name));
                mDeviceArrList.add(mDevice);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mDeviceListView.getVisibility() == View.GONE) {
                            mEmptyDeviceTxt.setVisibility(View.GONE);
                            mDeviceListView.setVisibility(View.VISIBLE);
                        }
                        mDeviceAdapter.notifyDataSetChanged();
                    }
                });
                mDevice.savePreferences(context.getSharedPreferences(Device.PREFERENCE, Context.MODE_PRIVATE));
            }
            final int progress = i;
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mProgressScan.setProgress((int) ((progress + 1) * 100 / 255));
                    if (mProgressScan.getProgress() == mProgressScan.getMax()) {
                        mProgressScan.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}