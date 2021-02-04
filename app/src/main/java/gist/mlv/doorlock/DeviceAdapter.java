package gist.mlv.doorlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DeviceAdapter extends ArrayAdapter<Device> implements View.OnClickListener {
    private ArrayList<Device> mArrayDevice;
    private Activity mMainActivity;
    public DeviceAdapter(Activity activity, Context context, ArrayList<Device> devices) {
        super(context, 0, devices);
        mArrayDevice = devices;
        mMainActivity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Device device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_list, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.lv_device_name);
        Button btnEdit = (Button) convertView.findViewById(R.id.lv_btn_edit);
        Button btnConnect = (Button) convertView.findViewById(R.id.lv_btn_connect);
        Button btnDelete = (Button) convertView.findViewById(R.id.lv_btn_delete);
        // Populate the data into the template view using the data object
        tvName.setText(device.getName());
        btnEdit.setOnClickListener(this);
        btnConnect.setOnClickListener(this);
        btnDelete.setOnClickListener(this);

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public void onClick(View viewItem) {
        View parentRow = (View) viewItem.getParent();
        ListView listView = (ListView) parentRow.getParent();
        final int position = listView.getPositionForView(parentRow);
        final Context context = getContext();

        switch (viewItem.getId()) {
            case R.id.lv_btn_edit:
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View view = layoutInflater.inflate(R.layout.input_wifi_info, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle(R.string.wifi_title);
                alertDialog.setCancelable(false);

                final EditText ssid_edt = (EditText) view.findViewById(R.id.wifi_name);
                final EditText pwd_edt = (EditText) view.findViewById(R.id.wifi_pwd);
                final LinearLayout pwd_layout = (LinearLayout) view.findViewById(R.id.wifi_pwd_layout);
                final CheckBox cbx = (CheckBox) view.findViewById(R.id.wifi_cbx);
                final EditText name_edt = (EditText) view.findViewById(R.id.device_name);
                Button wifi_ok = (Button) view.findViewById(R.id.wifi_ok);
                Button wifi_cancel = (Button) view.findViewById(R.id.wifi_cancel);

                final Device device = getItem(position);
                ssid_edt.setText(device.getWifiSSID());
                name_edt.setText(device.getName());
                if(device.getWifiPassword().length() == 0){
                    cbx.setChecked(true);
                    pwd_layout.setVisibility(View.GONE);
                } else{
                    pwd_edt.setText(device.getWifiPassword());
                }
                alertDialog.setView(view);
                alertDialog.show();
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
                        alertDialog.dismiss();

                        device.setName(device_name);
                        device.setWifiSSID(ssid);
                        device.setWifiPassword(pwd);

                        mArrayDevice.set(position, device);
                        notifyDataSetChanged();
                        device.savePreferences(context.getSharedPreferences(Device.PREFERENCE, Context.MODE_PRIVATE));
                        Toast.makeText(context, R.string.toast_update, Toast.LENGTH_SHORT).show();
                    }
                });
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
                break;
            case R.id.lv_btn_connect:
                Device connect = getItem(position);
                Intent i = new Intent(getContext(), DeviceWebView.class);
                i.putExtra("EXTRA_SESSION_ID", connect.connectLocal());
                mMainActivity.startActivity(i);
                break;
            case R.id.lv_btn_delete:
                Device device_del = getItem(position);
                SharedPreferences.Editor editor = context.getSharedPreferences(Device.PREFERENCE, Context.MODE_PRIVATE).edit();
                editor.remove(device_del.getID()).apply();
                remove(device_del);
                notifyDataSetChanged();
                Toast.makeText(context, R.string.toast_delete, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
