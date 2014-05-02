package com.recursivepenguin.bluetoothmanagerforglass.ble;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.widget.CardScrollAdapter;
import com.recursivepenguin.bluetoothmanagerforglass.R;

import java.util.List;

public class BleDeviceCardScrollAdapter extends CardScrollAdapter {
    List<BleDevice> mBleDevices;

    Context context;

    public BleDeviceCardScrollAdapter(Context context, List<BleDevice> devices) {
        mBleDevices = devices;
        this.context = context;
    }

    public void addDevice(BleDevice device) {
        mBleDevices.add(device);
    }

    @Override
    public int getPosition(Object item) {
        return mBleDevices.indexOf(item);
    }

    @Override
    public int getCount() {
        return mBleDevices.size();
    }

    @Override
    public BleDevice getItem(int position) {
        return mBleDevices.get(position);
    }

    class ViewHolder {
        ImageView icon;
        TextView name;
        TextView address;
        TextView rssi;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.ble_card, null);
            holder = new ViewHolder();

            holder.icon = (ImageView) convertView.findViewById(R.id.imageView);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.address = (TextView) convertView.findViewById(R.id.address);
            holder.rssi = (TextView) convertView.findViewById(R.id.rssi);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        BleDevice device = getItem(position);
        holder.name.setText(device.getName());
        holder.address.setText(device.getAddress());
        holder.rssi.setText(context.getString(R.string.rssi_value, device.getRssi()));

        return convertView;
    }
}
