package com.example.HealthFamily.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.HealthFamily.R;
import com.example.HealthFamily.utils.DataProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TR on 2017/7/27.
 */

public class DeviceControlActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = DeviceControlActivity.class.getSimpleName();

    public BluetoothLEService mBluetoothLEService;
    private Bundle data;
    private String bleDevName;
    private String bLEDevAddress;
    private Handler mHandler;
    public boolean connect;
    private ProgressDialog progressDialog;
    public BluetoothGattCharacteristic mNotifyCharacteristic;

    EditText et_writeContent;
    TextView tv_devName, tv_receiveData;
    Button btn_sendMsg;
    public ArrayList<BluetoothGattCharacteristic> characteristics;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_communication);
        getIntentData();
        init();
    }

    private Handler catHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tv_receiveData.setText(tv_receiveData.getText().toString() + "\n"
                    + "设备连接成功" + "\n" + "设备地址" + bLEDevAddress + "\n");
        }
    };

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLEService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLEService = ((BluetoothLEService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLEService.initBluetoothParam()) {
                Log.e(TAG, "未能成功初始化蓝牙");
                finish();
            }
            if (mBluetoothLEService == null) {
                finish();
            }
        }
    };
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)) {
                // mConnected = true;
                // updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                System.out.println("action = " + action);
            } else if (BluetoothLEService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                // mConnected = false;
                // updateConnectionState(R.string.disconnected);
                // invalidateOptionsMenu();
                // clearUI();
            } else if (BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
                // displayGattServices(mBluetoothLeService
                // .getSupportedGattServices());
            } else if (BluetoothLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                Bundle extras = intent.getExtras();
                String data = extras.getString(BluetoothLEService.EXTRA_DATA);
                DataProtocol mDataProtocol=new DataProtocol();
                mDataProtocol.DataDeal(data);                       //进行数据处理
                tv_receiveData.setText(tv_receiveData.getText().toString() + data);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void init() {
        mHandler = new Handler();
        tv_devName = (TextView) findViewById(R.id.tv_devName);
        tv_devName.setText(bleDevName);
        tv_receiveData = (TextView) findViewById(R.id.tv_receiveData);
        et_writeContent = (EditText) findViewById(R.id.et_writeContent);
        btn_sendMsg = (Button) findViewById(R.id.btn_sendMsg);
        btn_sendMsg.setOnClickListener(this);
        Intent intent = new Intent(this, BluetoothLEService.class);
		/*parameter1:intent,parameter2:ServiceConnection对象,BIND_AUTO_CREATE means automatic binding*/
        bindService(intent, conn, BIND_AUTO_CREATE);
        CatConResult result = new CatConResult();
        result.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        //进度对话框
        progressDialog = ProgressDialog.show(this, "连接血压计",
                "连接中，请稍等！");
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mBluetoothLEService != null) {
                    //连接到选定的蓝牙设备
                    connect = mBluetoothLEService.connect(bLEDevAddress);
                    if (connect) {
                        NotifyThread thread = new NotifyThread();
                        thread.execute();
                    }
                }
            }
        }, 2000);
    }

    class NotifyThread extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                Thread.sleep(2000);
                characteristics = getCharacteristic();
                setNotifyReceive(characteristics);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            if (connect) {
                //进度条消失
                progressDialog.dismiss();
            }
            super.onPostExecute(result);
        }
    }
    //获取特征值
    public ArrayList<BluetoothGattCharacteristic> getCharacteristic() {
        ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
        List<BluetoothGattService> services = mBluetoothLEService.getServices();
        for (int i = 0; i < services.size(); i++) {
            BluetoothGattService gattService = services.get(i);
            List<BluetoothGattCharacteristic> characteristics = gattService
                    .getCharacteristics();
            for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristics) {
                charas.add(bluetoothGattCharacteristic);
            }
        }
        return charas;
    }

    public void setNotifyReceive(
            ArrayList<BluetoothGattCharacteristic> characteristics) {
        if (characteristics != null && characteristics.size() > 0) {
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                int flage = characteristic.getProperties();
                if ((flage | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    // If there is an active notification on a characteristic,
                    // clear
                    // it first so it doesn't update the data field on the user
                    // interface.
                    //  if (mNotifyCharacteristic != null) {
                    //     mBluetoothLEService.setCharacteristicNotification(
                    //            mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                    // }
                    mBluetoothLEService.readCharacteristic(characteristic);
                }
                if ((flage | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    mNotifyCharacteristic = characteristic;
                    mBluetoothLEService.setCharacteristicNotification(
                            mNotifyCharacteristic, true);
                }
            }
        }
    }

    class CatConResult extends Thread {
        private boolean isRun = true;

        @Override
        public void run() {
            super.run();
            while (isRun) {
                if (connect) {
                    catHandler.sendEmptyMessage(0);
                    isRun = false;
                }
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothLEService.disconnect();
        mBluetoothLEService.close();
        unbindService(conn);
        mBluetoothLEService = null;
    }

    private void getIntentData() {
        //得到MainActivity里的蓝牙名称及设备
        Intent intent = this.getIntent();
        data = intent.getExtras();
        bleDevName = data.getString("BLEDevName");
        bLEDevAddress = data.getString("BLEDevAddress");
    }

    @Override
    public void onClick(View v) {

        String sendStr = et_writeContent.getText().toString();
        if (characteristics == null) {
            Toast.makeText(mBluetoothLEService, "未获取到特征值，请重试！", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                mBluetoothLEService.writeCharacteristic(characteristic, sendStr);

            }
        }
    }

}

