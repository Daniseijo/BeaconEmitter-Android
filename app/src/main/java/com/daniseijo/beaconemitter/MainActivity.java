package com.daniseijo.beaconemitter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter mBluetoothAdapter;
    private AdvertiseData mAdvertiseData;
    private AdvertiseSettings mAdvertiseSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothManager btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        if (btManager != null) {
            mBluetoothAdapter = btManager.getAdapter();
            if (mBluetoothAdapter != null) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    bluetoothSetupDone();
                }
            } else  {
                Toast toast = Toast.makeText(getApplicationContext(), "Failed to get Bluetooth Adapter", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Failed to get Bluetooth Manager", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    bluetoothSetupDone();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "User did not enable Bluetooth or an error occurred", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void bluetoothSetupDone() {
        if(!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Device does not support Bluetooth LE", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        BluetoothLeAdvertiser mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        Log.d("CREATED", "new BeaconTransmitter constructed.  mbluetoothLeAdvertiser is " +  mBluetoothLeAdvertiser);

        setAdvertiseData();
        setAdvertiseSettings();

        mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSettings, mAdvertiseData, mAdvertiseCallback);
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Toast toast = Toast.makeText(getApplicationContext(), "Advertising", Toast.LENGTH_SHORT);
            toast.show();
        }

        @Override
        public void onStartFailure(int errorCode) {
            Toast toast = Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT);
            toast.show();
            Log.d("ERROR", "Failed with code" + errorCode);
        }
    };

    private void setAdvertiseData() {
        AdvertiseData.Builder mBuilder = new AdvertiseData.Builder();
        ByteBuffer mManufacturerData = ByteBuffer.allocate(23);

        byte[] uuid = getIdAsByte(UUID.fromString("0018B4CC-1937-4981-B893-9D7191B22E35"));
        mManufacturerData.put(0, (byte)0x02); // Beacon Identifier
        mManufacturerData.put(1, (byte)0x15); // Beacon Identifier
        for (int i=2; i<=17; i++) {
            mManufacturerData.put(i, uuid[i-2]); // adding the UUID
        }
        mManufacturerData.put(18, (byte)0x00); // first byte of Major
        mManufacturerData.put(19, (byte)0x01); // second byte of Major
        mManufacturerData.put(20, (byte)0x00); // first minor
        mManufacturerData.put(21, (byte)0x01); // second minor
        mManufacturerData.put(22, (byte)0xB5); // txPower
        mBuilder.addManufacturerData(0x004C, mManufacturerData.array()); // using Apple's company ID
        mAdvertiseData = mBuilder.build();
    }

    private void setAdvertiseSettings() {
        AdvertiseSettings.Builder mBuilder = new AdvertiseSettings.Builder();
        mBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        mBuilder.setConnectable(false);
        mBuilder.setTimeout(0);
        mBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);
        mAdvertiseSettings = mBuilder.build();
    }

    private static byte[] getIdAsByte(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
