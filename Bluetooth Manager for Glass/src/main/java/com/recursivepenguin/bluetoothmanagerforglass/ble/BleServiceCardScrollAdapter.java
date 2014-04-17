package com.recursivepenguin.bluetoothmanagerforglass.ble;

import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.widget.CardScrollAdapter;
import com.recursivepenguin.bluetoothmanagerforglass.R;

import java.util.List;

public class BleServiceCardScrollAdapter extends CardScrollAdapter {
    List<BluetoothGattService> mBleServices;

    Context context;

    public BleServiceCardScrollAdapter(Context context, List<BluetoothGattService> devices) {
        mBleServices = devices;
        this.context = context;
    }

    @Override
    public int getPosition(Object item) {
        return mBleServices.indexOf(item);
    }

    @Override
    public int getCount() {
        return mBleServices.size();
    }

    @Override
    public BluetoothGattService getItem(int position) {
        return mBleServices.get(position);
    }

    class ViewHolder {
        ImageView icon;
        TextView name;
        TextView type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.ble_card, null);
            holder = new ViewHolder();

            holder.icon = (ImageView) convertView.findViewById(R.id.imageView);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.type = (TextView) convertView.findViewById(R.id.address);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothGattService service = getItem(position);
        holder.name.setText(service.getUuid().toString());
        holder.type.setText(service.getType());

        return convertView;
    }
}
