package com.recursivepenguin.bluetoothmanagerforglass.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.widget.CardScrollAdapter;
import com.movisens.smartgattlib.Characteristic;
import com.recursivepenguin.bluetoothmanagerforglass.R;

import java.util.List;

public class BleCharacteristicCardScrollAdapter extends CardScrollAdapter {
    List<BluetoothGattCharacteristic> mBleCharacteristics;

    Context context;

    public BleCharacteristicCardScrollAdapter(Context context, List<BluetoothGattCharacteristic> characteristics) {
        mBleCharacteristics = characteristics;
        this.context = context;
    }

    @Override
    public int getPosition(Object item) {
        return mBleCharacteristics.indexOf(item);
    }

    @Override
    public int getCount() {
        return mBleCharacteristics.size();
    }

    @Override
    public BluetoothGattCharacteristic getItem(int position) {
        return mBleCharacteristics.get(position);
    }

    class ViewHolder {
        ImageView icon;
        TextView name;
        TextView text;
        TextView raw;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.ble_chara_card, null);
            holder = new ViewHolder();

            holder.icon = (ImageView) convertView.findViewById(R.id.imageView);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            holder.raw = (TextView) convertView.findViewById(R.id.raw);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothGattCharacteristic characteristic = getItem(position);
        holder.name.setText(Characteristic.lookup(characteristic.getUuid(), characteristic.getUuid().toString()));

        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            String text = characteristic.getStringValue(0);
            holder.text.setText(text);

            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            holder.raw.setText(stringBuilder.toString());
        }



        return convertView;
    }
}
