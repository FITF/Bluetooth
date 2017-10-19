package com.example.HealthFamily.healthSqlitepal;

import org.litepal.crud.DataSupport;

/**
 * Created by someone on 2017/10/12.
 *
 * 数据库方法
 */

public class HealthData extends DataSupport{

        private int id;
        //private  String userName;
        private  int highPressure;
        private  int lowPressure;
        private  int  heartbeat;
        private  String mtime;
        public void setId(int id){
            this.id = id;
        }

  //      public void setuserName(String userName){
  //          this.userName = userName;
  //      }


    public int getId() {
        return id;
    }

    public int getHighPressure() {
        return highPressure;
    }

    public void setHighPressure(int highPressure) {
        this.highPressure = highPressure;
    }

    public int getLowPressure() {
        return lowPressure;
    }

    public void setLowpressure(int lowPressure) {
        this.lowPressure = lowPressure;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public String getMtime() {
        return mtime;
    }

    public void setMtime(String mtime) {
        this.mtime = mtime;
    }
}
