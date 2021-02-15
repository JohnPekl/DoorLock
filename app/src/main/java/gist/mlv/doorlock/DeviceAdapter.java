package gist.mlv.doorlock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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
            convertView.setLongClickable(true);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.lv_device_name);
        Button btnConnect = (Button) convertView.findViewById(R.id.lv_btn_connect);
        Button btnStream = (Button) convertView.findViewById(R.id.lv_btn_stream);
        // Populate the data into the template view using the data object
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
                iConnect.putExtra("EXTRA_SESSION_ID", connect.connectLocal());
                mMainActivity.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                mMainActivity.startActivity(iConnect);
                break;
            case R.id.lv_btn_stream:
                Device stream = getItem(position);
                Intent iStream = new Intent(getContext(), StreamingWebView.class);
                iStream.putExtra("EXTRA_SESSION_ID", stream.connectLocal());
                mMainActivity.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                mMainActivity.startActivity(iStream);
                break;
        }
    }
}
