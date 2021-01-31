package gist.mlv.doorlock;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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

import com.blikoon.qrcodescanner.QrCodeActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import info.whitebyte.hotspotmanager.ClientScanResult;
import info.whitebyte.hotspotmanager.FinishScanListener;
import info.whitebyte.hotspotmanager.WifiApManager;

public class MainActivity extends Activity implements View.OnClickListener {
    /**
     * Called when the activity is first created.
     */
    private String TAG = "MainActivity";

    private Button mAddDevice;
    private WifiApManager mWifiApManager;
    private Device mDevice;
    private TextView mEmptyDeviceTxt;
    private ListView mDeviceListView;
    private ArrayList<Device> mDeviceArrList;
    private ArrayAdapter<Device> mDeviceAdapter;
    private static final int REQUEST_CODE_QR_SCAN = 101;

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
                            Manifest.permission.VIBRATE, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.INTERNET},
                    REQUEST_CODE);
        }

        mWifiApManager = new WifiApManager(getApplicationContext());
        if (!Settings.System.canWrite(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), (MainActivity.this).getString(R.string.wifi_allow_setting), Toast.LENGTH_LONG).show();
        }
        mWifiApManager.showWritePermissionSettings(false);

        getAllPreferense();
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
        mDeviceAdapter = new DeviceAdapter(MainActivity.this, mDeviceArrList);
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
                Intent i = new Intent(MainActivity.this, QrCodeActivity.class);
                startActivityForResult(i, REQUEST_CODE_QR_SCAN);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            if (data == null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
            if (result != null) {
            }
            return;

        }
        if (requestCode == REQUEST_CODE_QR_SCAN) {
            if (data == null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
            String[] arr = result.split(",");
            //String mlv_device_id = arr[0]; String mlv_user = arr[1]; String mlv_pwd = arr[2];

            mDevice = new Device(arr[0], arr[1], arr[2]);
            showWIFIDialog();
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
                //alertDialog.dismiss();

                mDevice.setName(device_name);
                mDevice.setWifiSSID(ssid);
                mDevice.setWifiPassword(pwd);
                mDeviceArrList.add(mDevice);
                mDeviceAdapter.notifyDataSetChanged();
                mDevice.savePreferences(getSharedPreferences(Device.PREFERENCE, Context.MODE_PRIVATE));
                if(mDeviceListView.getVisibility() == View.GONE){
                    mDeviceListView.setVisibility(View.VISIBLE);
                    mEmptyDeviceTxt.setVisibility(View.GONE);
                }

                //checkSSIDPassword(view, alertDialog, ssid, pwd);
                createHotSpot();
                alertDialog.dismiss();
                Toast.makeText(MainActivity.this, R.string.toast_save, Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.setView(view);
        alertDialog.show();
    }

    private void checkSSIDPassword(View v, final AlertDialog dialog, final String ssid, final String pwd) {
        final Context context = MainActivity.this;

        final ProgressDialog progressBar = new ProgressDialog(v.getContext());
        progressBar.setCancelable(false);
        progressBar.setMessage(context.getString(R.string.wifi_check));
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();

        final Handler handler = new Handler();

        final Thread wifi_check = new Thread() {
            @Override
            public void run() {
                WifiConfiguration wifiConfig = new WifiConfiguration();

                wifiConfig.SSID = String.format("\"%s\"", ssid);
                wifiConfig.preSharedKey = String.format("\"%s\"", pwd);

                WifiManager wifiManager = (WifiManager) context.getSystemService(android.content.Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);
                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                boolean result = wifiManager.enableNetwork(netId, true);
                //boolean result = wifiManager.reconnect();

                if (result) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.dismiss();
                            dialog.dismiss();
                            Toast.makeText(context, context.getString(R.string.wifi_check_ok), Toast.LENGTH_SHORT).show();
                        }
                    });
                    createHotSpot();
                }
            }
        };
        wifi_check.start();

        if (wifi_check.isAlive()) {
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    wifi_check.interrupt();
                    progressBar.dismiss();
                    Toast.makeText(context, context.getString(R.string.wifi_check_no), Toast.LENGTH_SHORT).show();
                }
            }, 3000);
        }
    }

    private void createHotSpot() {
        Thread hotspotThread = new Thread() {
            @Override
            public void run() {
                mWifiApManager.setWifiApEnabled(null, false); // turn off previously started hotspot

                String name = "mlv-door5gkkkk";
                String passPhrase = "wlsfkaus";
                WifiConfiguration wifiConfig = new WifiConfiguration();

                wifiConfig.SSID = name;
                // must be 8 or more in length
                wifiConfig.preSharedKey = passPhrase;

                wifiConfig.hiddenSSID = false;

                wifiConfig.status = WifiConfiguration.Status.ENABLED;
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

                mWifiApManager.setWifiApEnabled(wifiConfig, true);
            }
        };
        hotspotThread.start();
        Context context = MainActivity.this;
        Toast.makeText(context, context.getString(R.string.hotspot_start), Toast.LENGTH_SHORT).show();

        final ProgressDialog progressBar = new ProgressDialog(context);
        progressBar.setCancelable(false);
        progressBar.setMessage(context.getString(R.string.hotspot_scan));
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();

        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                mWifiApManager.getClientList(false, new FinishScanListener() {
                    @Override
                    public void onFinishScan(final ArrayList<ClientScanResult> clients) {
                        for (ClientScanResult clientScanResult : clients) {
                            String ip = clientScanResult.getIpAddr();
                            String device = clientScanResult.getDevice();
                            String mac = clientScanResult.getHWAddr();
                            boolean state = clientScanResult.isReachable();
                        }

                        //String result = httprequest("http://172.26.19.213:8555/?check_dev=000-000-000");
                    }
                });
            }
        }, 3000);
    }

}


