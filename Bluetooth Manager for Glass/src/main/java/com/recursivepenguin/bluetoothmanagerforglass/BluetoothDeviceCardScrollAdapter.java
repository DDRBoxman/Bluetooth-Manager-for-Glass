package com.recursivepenguin.bluetoothmanagerforglass;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.widget.CardScrollAdapter;

import java.util.List;

public class BluetoothDeviceCardScrollAdapter extends CardScrollAdapter {
    List<BluetoothDevice> mBondedDevices;

    Context context;

    public BluetoothDeviceCardScrollAdapter(Context context, List<BluetoothDevice> devices) {
        mBondedDevices = devices;
        this.context = context;
    }

    public void addDevice(BluetoothDevice device) {
        mBondedDevices.add(device);
    }

    @Override
    public int getPosition(Object item) {
        return mBondedDevices.indexOf(item);
    }

    @Override
    public int getCount() {
        return mBondedDevices.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return mBondedDevices.get(position);
    }

    class ViewHolder {
        ImageView icon;
        TextView name;
        TextView address;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.device_card, null);
            holder = new ViewHolder();

            holder.icon = (ImageView) convertView.findViewById(R.id.imageView);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.address = (TextView) convertView.findViewById(R.id.address);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device = getItem(position);
        holder.name.setText(device.getName());
        holder.address.setText(device.getAddress());

        return convertView;
    }
}
