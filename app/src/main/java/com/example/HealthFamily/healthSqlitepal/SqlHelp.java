package com.example.HealthFamily.healthSqlitepal;

import android.util.Log;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import com.example.HealthFamily.utils.BluetoothProtocol;

import java.util.List;

import static org.litepal.LitePalBase.TAG;

/**
 * Created by someone on 2017/10/12.
 * 关于数据库的操作封装都在这个文件中实现
 */

public class SqlHelp {


    public void Sqlset(BluetoothProtocol mBluetoothValue){
        HealthData healthData = new HealthData();//新建了表
        //数据写入数据库的表中
        healthData.setHeartbeat(mBluetoothValue.getHeartbeat());
        healthData.setHighPressure(mBluetoothValue.getHighPressure());
        healthData.setLowpressure(mBluetoothValue.getLowPressure());
        healthData.setMtime(mBluetoothValue.getMinutes());
        healthData.save();
        Log.d(TAG,"写入数据进入数据库");
        Log.d(TAG,"心跳" + mBluetoothValue.getHeartbeat());

        Log.d(TAG,"当前时间：" + mBluetoothValue.getMinutes());
        HealthData firstdata = DataSupport.findFirst(HealthData.class);
        Log.d(TAG,"保存的时间:"+firstdata.getMtime());

    }
    public void init_databases() {
        LitePal.getDatabase(); //创建数据库  已经判断是否为空了，再次创建不会占用太多资源
    }
}
