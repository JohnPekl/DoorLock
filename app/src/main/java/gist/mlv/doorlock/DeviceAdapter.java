package gist.mlv.doorlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import gist.mlv.doorlock.network.HttpRequest;

public class DeviceAdapter extends ArrayAdapter<Device> implements View.OnClickListener {
    private ArrayList<Device> mArrayDevice;
    private Activity mMainActivity;
    private Handler mMainHandler;
    public DeviceAdapter(Activity activity, Handler handler, ArrayList<Device> devices) {
        super(activity, 0, devices);
        mArrayDevice = devices;
        mMainActivity = activity;
        mMainHandler = handler;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Device device = mArrayDevice.get(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_list, parent, false);
            convertView.setLongClickable(true);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.lv_device_name);
        Button btnConnect = (Button) convertView.findViewById(R.id.lv_btn_connect);
        Button btnStream = (Button) convertView.findViewById(R.id.lv_btn_stream);
        // Populate the data into the template view using the data object
        if (device.getUserName().equals("")){
            btnStream.setEnabled(false);
        }
        tvName.setText(device.getName());
        btnConnect.setOnClickListener(this);
        btnStream.setOnClickListener(this);

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
            case R.id.lv_btn_connect:
                Device connect = getItem(position);
                Intent iConnect = new Intent(getContext(), DeviceWebView.class);
                mMainActivity.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                if (connect.getUserName().equals("")){
                    checkUserPassword(connect, iConnect);
                } else {
                    iConnect.putExtra(DeviceWebView.INTENT_EXTRA, (new Gson().toJson(connect)));
                    mMainActivity.startActivity(iConnect);
                }
                break;
            case R.id.lv_btn_stream:
                final Device stream = getItem(position);
                final Intent iStream = new Intent(getContext(), StreamingWebView.class);

                if(stream.getStreamingKey().equals("")) {
                    LayoutInflater layoutInflater = (LayoutInflater) mMainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view_streamingkey = layoutInflater.inflate(R.layout.streaming_key, null);
                    final AlertDialog alertDialog = new AlertDialog.Builder(mMainActivity).create();
                    alertDialog.setCancelable(false);
                    view_streamingkey.findViewById(R.id.streaming_key_cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });
                    final EditText edt_key = (EditText) view_streamingkey.findViewById(R.id.streaming_key);
                    view_streamingkey.findViewById(R.id.streaming_key_ok).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String key = edt_key.getText().toString();
                            stream.setStreamingKey(key);
                            stream.savePreferences(mMainActivity);
                            alertDialog.dismiss();

                            iStream.putExtra(StreamingWebView.INTENT_EXTRA, stream.getUrlMLV());
                            mMainActivity.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                            mMainActivity.startActivity(iStream);
                        }
                    });
                    alertDialog.setView(view_streamingkey);
                    alertDialog.show();
                } else {
                    iStream.putExtra(StreamingWebView.INTENT_EXTRA, stream.getUrlMLV());
                    mMainActivity.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    mMainActivity.startActivity(iStream);
                }
                break;
        }
    }

    private void checkUserPassword(final Device d, final Intent iConnect){
        LayoutInflater layoutInflater = (LayoutInflater) mMainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view_login = layoutInflater.inflate(R.layout.username_password, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(mMainActivity).create();
        alertDialog.setTitle(R.string.user_pwd);
        alertDialog.setCancelable(false);
        view_login.findViewById(R.id.login_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        final EditText edt_username = (EditText) view_login.findViewById(R.id.login_username);
        final EditText edt_password = (EditText) view_login.findViewById(R.id.login_password);
        view_login.findViewById(R.id.login_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final HttpRequest http = new HttpRequest();
                (new Thread(){
                    @Override
                    public void run() {
                        String username = edt_username.getText().toString();
                        String password = edt_password.getText().toString();
                        boolean check = http.checkUserPassword(d.getUrlLocal(), username, password);
                        if (check){
                            alertDialog.dismiss();
                            d.setUserName(username);
                            d.setPassword(password);
                            d.savePreferences(mMainActivity);

                            iConnect.putExtra(DeviceWebView.INTENT_EXTRA, (new Gson().toJson(d)));
                            mMainActivity.startActivity(iConnect);
                        } else {
                            mMainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mMainActivity, R.string.login_incorrect, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        alertDialog.setView(view_login);
        alertDialog.show();
    }
}
