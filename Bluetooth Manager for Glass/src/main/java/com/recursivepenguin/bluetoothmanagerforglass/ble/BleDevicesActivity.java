package com.recursivepenguin.bluetoothmanagerforglass.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardScrollView;
import com.recursivepenguin.bluetoothmanagerforglass.R;

import java.util.ArrayList;
import java.util.List;

public class BleDevicesActivity extends Activity implements AdapterView.OnItemClickListener, BluetoothAdapter.LeScanCallback {

    BluetoothAdapter mBluetoothAdapter;
    CardScrollView mCardScrollView;
    BleDeviceCardScrollAdapter adapter;
    BleDevice mSelectedDevice;
    List<BleDevice> mDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBluetoothAdapter.startLeScan(this);

        mCardScrollView = new CardScrollView(this);
        mCardScrollView.activate();
        mCardScrollView.setOnItemClickListener(this);
        mCardScrollView.setHorizontalScrollBarEnabled(true);
        setContentView(mCardScrollView);

        mDevices = new ArrayList<>();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBluetoothAdapter.stopLeScan(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ble, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.connect:
                Intent intent = new Intent(this, BleServicesActivity.class);
                intent.putExtra(BleServicesActivity.EXTRA_DEVICE_ADDRESS, mSelectedDevice.getAddress());
                startActivity(intent);
                mBluetoothAdapter.stopLeScan(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.TAP);
        mSelectedDevice = adapter.getItem(position);
        openOptionsMenu();
    }


    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] bytes) {

        if (device.getName() != null) {

            BleDevice bleDevice = new BleDevice(device, rssi);

            if (!mDevices.contains(bleDevice)) {
                mDevices.add(bleDevice);
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audio.playSoundEffect(Sounds.SUCCESS);
            } else {
                int index = mDevices.indexOf(bleDevice);
                mDevices.get(index).setRssi(rssi);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter = new BleDeviceCardScrollAdapter(BleDevicesActivity.this, mDevices);
                    mCardScrollView.setAdapter(adapter);
                }
            });

        }
    }

}
