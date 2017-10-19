package com.example.HealthFamily.utils;

import android.util.Log;

import com.example.HealthFamily.healthSqlitepal.SqlHelp;

import static com.example.HealthFamily.utils.UsedConst.DATASUCCEED;
import static org.litepal.LitePalBase.TAG;

/**
 * Created by someone on 2017/10/12.
 */

public class DataProtocol {

    private BluetoothProtocol mBluetoothProtocol;
    public void DataDeal(String data){
        mBluetoothProtocol= new BluetoothProtocol(data);
        int commandNo = mBluetoothProtocol.getCommandNo();
        Log.d(TAG,"命令"+commandNo);
        switch (commandNo ) {
            case DATASUCCEED :{//接收到数据正确的命令开始处理数据库。然后展示数据
                SqlHelp mSqlhelp= new SqlHelp();
                mSqlhelp.Sqlset(mBluetoothProtocol);      //数据的存储。
                Log.d(TAG,"数据存储成功");
                //接下来数据的展示
            }break;
        }
    }
}
