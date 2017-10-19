package com.example.HealthFamily.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

/**
 * Created by someone on 2017/10/8.
 */

public class BluetoothLEService extends Service {
    private final static String TAG = BluetoothLEService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;


    private static final int STATE_DISCONNECTED = 0;  //未连接
    private static final int STATE_CONNECTING = 1;    //连接中
    private static final int STATE_CONNECTED = 2;     //已连接

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";



    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    // BLE API定义的各种回调方法

    /*连接后会回调BluetoothGattCallback接口，包括连接设备，
    * 往设备里写数据及设备发出通知等都会回调该接口
    * 其中比较重要的是BluetoothGatt*/
    private final BluetoothGattCallback mGattCallback;

    {
        mGattCallback = new BluetoothGattCallback() {
            @Override//当连接上设备或者失去连接时会回调此函数
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                String intentAction;
                if (newState == BluetoothProfile.STATE_CONNECTED) {//连接成功
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    broadcastUpdate(intentAction);
                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "Attempting to start service discovery:"
                            //连接成功后启动服务发现
                            + mBluetoothGatt.discoverServices());
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//连接失败
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.");
                    broadcastUpdate(intentAction);
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }

            @Override//当向设备Descriptor中写数据,会回调此函数
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                System.out.println("onDescriptorWriteonDescriptorWrite = " + status
                        + ", descriptor =" + descriptor.getUuid().toString());
            }

            @Override//数据返回的回调（此处接收BLE设备返回数据）
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {
                Log.d(TAG, "数据接收回调成功");
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                if (characteristic.getValue() != null) {
                    Log.d(TAG, "获得的值：" + characteristic.getStringValue(0));
                }
                System.out.println("--------onCharacteristicChanged-----");
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                System.out.println("rssi = " + rssi);
            }

            //当向Characteristic写数据时会回调该函数
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic, int status) {
                System.out.println("--------write success----- status:" + status);
                Log.e(TAG, "onCharWrite " + gatt.getDevice().getName()
                        + " write "
                        + characteristic.getUuid().toString()
                        + " -> "
                        + new String(characteristic.getValue()));

            }
        };
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.
        // Data parsing is carried out as per profile specifications.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            String value = characteristic.getStringValue(0);
            intent.putExtra(EXTRA_DATA, value);
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(
                        data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n"
                        + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }
    public boolean initBluetoothParam() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Toast.makeText(this, "bluetooth初始化失败", Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "不能获得bluetoothAdapter", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        return true;
    }
    //连接是通过获取到的mac地址去进行连接操作
    public boolean connect(String address) {
        if (mBluetoothAdapter == null || address == null) {
            return false;
        }
        //先前连接的设备，尝试重新连接
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                Log.d(TAG, "mBluetoothAdapter："+mBluetoothAdapter +"  address:"+address);
                return false;
            }
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.v("device of null", "device of null");
            return false;
        }
        //三个参数，第一个参数是上下文对象，第二个参数是是否自动连接，第三个参数就是上面的回调方法。
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);//该函数才是真正去进行连接
        mConnectionState = STATE_CONNECTING;
        return true;
    }
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "蓝牙适配器未能初始化！");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    public void writeCharacteristic(BluetoothGattCharacteristic mWriteCaracteristic,String data){
        if(UUID_HEART_RATE_MEASUREMENT.equals(mWriteCaracteristic.getUuid())){

            mBluetoothGatt.setCharacteristicNotification(mWriteCaracteristic,true);

            byte[] values = data.getBytes();
            mWriteCaracteristic.setValue(values);
            //向蓝牙模块中写入数据
            mBluetoothGatt.writeCharacteristic(mWriteCaracteristic);
        }
    }
    public void setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        // BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
        // UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        // descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        // mBluetoothGatt.writeDescriptor(descriptor);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic
                    .getDescriptor(UUID
                            .fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor
                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }
    public class LocalBinder extends Binder {
        BluetoothLEService getService() {
            return BluetoothLEService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public List<BluetoothGattService> getServices() {
        if (mBluetoothGatt == null)
            return null;
        return mBluetoothGatt.getServices();
    }

    public BluetoothGattService getservice(UUID uuid) {
        BluetoothGattService service = null;
        if (mBluetoothGatt != null) {
            service = mBluetoothGatt.getService(uuid);
        }
        return service;
    }


}
