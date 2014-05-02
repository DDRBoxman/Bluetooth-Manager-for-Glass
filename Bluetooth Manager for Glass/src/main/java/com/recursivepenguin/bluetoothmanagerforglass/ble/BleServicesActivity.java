package com.recursivepenguin.bluetoothmanagerforglass.ble;

import android.app.Activity;
import android.bluetooth.*;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardScrollView;
import com.recursivepenguin.bluetoothmanagerforglass.R;

import java.util.ArrayList;
import java.util.List;

public class BleServicesActivity extends Activity implements AdapterView.OnItemClickListener {

    public static final String EXTRA_DEVICE_ADDRESS = "EXTRA_DEVICE_ADDRESS";
    private static final String TAG = BleServicesActivity.class.getSimpleName();

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private int mConnectionState = STATE_DISCONNECTED;

    BluetoothAdapter mBluetoothAdapter;
    CardScrollView mCardScrollView;
    BleServiceCardScrollAdapter adapter;
    List<BluetoothGattService> mServices;
    private String mBleDeviceAddress;
    private BluetoothGattService mSelectedService;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private BleCharacteristicCardScrollAdapter charAdapter;
    private BluetoothGattCharacteristic mSelectedCharacteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBluetoothManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mBleDeviceAddress = getIntent().getStringExtra(EXTRA_DEVICE_ADDRESS);
        connect(mBleDeviceAddress, mBroadcastGattCallback);

        mCardScrollView = new CardScrollView(this);
        mCardScrollView.activate();
        mCardScrollView.setOnItemClickListener(this);
        mCardScrollView.setHorizontalScrollBarEnabled(true);
        setContentView(mCardScrollView);

        mServices = new ArrayList<>();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect(mBleDeviceAddress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.service, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.read_value).setVisible(charAdapter != null);
        menu.findItem(R.id.show_values).setVisible(charAdapter == null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.show_values:
                showValuesForSelectedService();
                mCardScrollView.setSelection(0);
                return true;
            case R.id.read_value:
                readValueForSelectedCharacteristic();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void readValueForSelectedCharacteristic() {
        mBluetoothGatt.readCharacteristic(mSelectedCharacteristic);
    }

    private void showValuesForSelectedService() {
        charAdapter = new BleCharacteristicCardScrollAdapter(this, mSelectedService.getCharacteristics());
        mCardScrollView.setAdapter(charAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (charAdapter == null) {
            onServiceClick(position);
        } else {
            onCharacteristicClick(position);
        }
    }

    private void onServiceClick(int position) {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.TAP);
        mSelectedService = adapter.getItem(position);
        openOptionsMenu();
    }

    private void onCharacteristicClick(int position) {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.TAP);
        mSelectedCharacteristic = charAdapter.getItem(position);
        openOptionsMenu();
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address      The device type of the destination device.
     * @param gattCallback
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address, BluetoothGattCallback gattCallback) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified type.");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        // Previously connected device.  Try to reconnect.
        if (mBleDeviceAddress != null && address.equals(mBleDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                Log.d(TAG, "connecting");
                return true;
            } else {
                Log.d(TAG, "connection failed");
                return false;
            }
        }

        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, gattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBleDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;

        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect(String address) {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "disconnect: BluetoothAdapter not initialized");
            return;
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        int connectionState = mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT);

        if (mBluetoothGatt != null) {
            Log.i(TAG, "disconnect");
            if (connectionState != BluetoothProfile.STATE_DISCONNECTED) {
                mBluetoothGatt.disconnect();
            } else {
                Log.w(TAG, "Attempt to disconnect in state: " + connectionState);
            }
            close();
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    private final BluetoothGattCallback mBroadcastGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;

                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                close();
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
            } else {
                Log.e(TAG, "New state not processed: " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mServices.clear();
                mServices.addAll(mBluetoothGatt.getServices());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new BleServiceCardScrollAdapter(BleServicesActivity.this, mServices);
                        mCardScrollView.setAdapter(adapter);
                    }
                });

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG, "value received from" + characteristic.getUuid() + ": " + characteristic.getValue());
            Log.d(TAG, "status " + status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                showValuesForSelectedService();
                            }
                        }
                );

            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "value written to " + characteristic.getUuid() + " status: " + status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "changed value received from " + characteristic.getUuid() + ": " + characteristic.getValue());
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "remote rssi " + rssi + " status " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // TODO
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, "onDescriptorWrite");
        }
    };

}
