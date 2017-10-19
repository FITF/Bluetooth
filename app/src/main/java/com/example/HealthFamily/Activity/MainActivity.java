package com.example.HealthFamily.Activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.HealthFamily.R;
import com.example.HealthFamily.bluetooth.DeviceControlActivity;
import com.example.HealthFamily.healthSqlitepal.SqlHelp;


import java.util.ArrayList;
import java.util.List;

public  class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";      //定义一个标签，用于控制台方便输出

    ToggleButton tb_on_off;
    Button btn_searchDev;
    ListView lv_bleList;


    //蓝牙适配器对象
    private BluetoothAdapter mBluetoothAdapter;
    private DeviceListAdapter mDevListAdapter;
    private static final int REQUEST_ENABLE_BT = 1;   //接收EN
    private static final int Permission_location = 0x01;
    private static final long SCAN_PERIOD = 10000;// 扫描周期
    private boolean mScanning;// 扫描
    private Handler mHandler;    //传递句柄

    //数据库对象



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();

        if(!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this,"不支持蓝牙",Toast.LENGTH_SHORT).show();
            finish();    //关闭这个活动。
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);   //获得蓝牙服务
        mBluetoothAdapter = bluetoothManager.getAdapter();   //获得适配器
        if(mBluetoothAdapter == null){
            Toast.makeText(this,"此设备不支持蓝牙4.0",Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        init_Bluetooth();   //蓝牙点击事件分配初始化
        SqlHelp mSqlHelp = new SqlHelp();
        mSqlHelp.init_databases();   //数据库的初始化
    }

    /**
     * @Title: initParameter
     * @Description: 初始化参数
     * @param
     * @return void
     * @throws
     */
    private void init_Bluetooth(){
        tb_on_off = (ToggleButton) findViewById(R.id.tb_on_off);
        btn_searchDev = (Button) findViewById(R.id.btn_searchDev);
        lv_bleList = (ListView) findViewById(R.id.lv_bleList);

        btn_searchDev.setOnClickListener(this);
        mDevListAdapter = new DeviceListAdapter();
        lv_bleList.setAdapter(mDevListAdapter);

        if(mBluetoothAdapter.isEnabled()){
            tb_on_off.setChecked(true);
            Log.d("TAG","打开蓝牙");
        }else {
            tb_on_off.setChecked(false);
            Log.d("TAG","关闭蓝牙");
        }
        tb_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    Intent enableBtIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    mBluetoothAdapter.disable();
                }
            }
        });
        //蓝牙设备列表
        lv_bleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (mDevListAdapter.getCount() > 0) {
                    BluetoothDevice device = (BluetoothDevice) mDevListAdapter.getItem(position);
                    Intent intent = new Intent(MainActivity.this,
                            DeviceControlActivity.class);
                    //Bundle相当于Map类,即一个映射,用Bundle绑定数据,便于数据处理。
                    Bundle bundle = new Bundle();
                    bundle.putString("BLEDevName", device.getName());
                    bundle.putString("BLEDevAddress", device.getAddress());
                    intent.putExtras(bundle);
                    MainActivity.this.startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_searchDev:

                //安卓6.0蓝牙扫描需要位置权限,位置权限属于危险权限，以下代码声明
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //请求权限
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            Permission_location);
                } else {
                     scanLeDevice(true);
                }

                break;

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults){
        switch (requestCode){
            case Permission_location:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scanLeDevice(true);
                }
            }
        }
    }
    private void scanLeDevice(final boolean enable){
        if(enable){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            },SCAN_PERIOD);     //SCAN_PERIOD开头定义的变量，大概摸清了全部大写的就是这个定义的常量。
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);       //开始扫描
            Log.d(TAG,"开始扫描");
        }else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
    //搜索到设备后调用此接口
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device,int rssi,
                             byte[] scanRecord){
         runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 mDevListAdapter.addDevice(device);
                 mDevListAdapter.notifyDataSetChanged();
             }
         });
        }
    };
    @Override
    protected void onResume() {
        super.onResume();

    }
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }


    /**设备列表适配器必须要有这些项去重写
     *
     *
     */
    class DeviceListAdapter extends BaseAdapter{

        private List<BluetoothDevice> mBleArray;
        private ViewHolder viewHolder;

        public DeviceListAdapter() {
            mBleArray = new ArrayList<BluetoothDevice>();}

        public void addDevice(BluetoothDevice device) {
            if (!mBleArray.contains(device)) {
                mBleArray.add(device);
            }
        }
        @Override
        public int getCount() {
            return mBleArray.size();
        }
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return mBleArray.get(position);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(
                        R.layout.item_list, null);
                viewHolder = new ViewHolder();
                viewHolder.tv_devName = (TextView) convertView
                        .findViewById(R.id.tv_devName);
                viewHolder.tv_devAddress = (TextView) convertView
                        .findViewById(R.id.tv_devAddress);
                convertView.setTag(viewHolder);
            } else {
                convertView.getTag();
            }

            // add-Parameters
            BluetoothDevice device = mBleArray.get(position);
            String devName = device.getName();
            if (devName != null && devName.length() > 0) {
                viewHolder.tv_devName.setText(devName);
            } else {
                viewHolder.tv_devName.setText("unknown-device");
            }
            viewHolder.tv_devAddress.setText(device.getAddress());

            return convertView;
        }
    }

     //  列表适配器里面的一个小项
     //避免了就是每次在getVIew的时候，都需要重新的findViewById，
    class ViewHolder {
        TextView tv_devName, tv_devAddress;
    }
}




