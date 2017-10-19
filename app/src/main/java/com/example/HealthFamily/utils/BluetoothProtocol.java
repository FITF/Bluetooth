package com.example.HealthFamily.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by someone on 2017/10/12.
 *
 */

public class BluetoothProtocol {
    private String version;    //版本号  1
    private String senderName;   //设别发送者
    private int highPressure;
    private int lowPressure;
    private int heartbeat;
    private int commandNo;        //命令

    //根据协议字符串分割初始化
    public BluetoothProtocol(String protocolString){
        String[] args = protocolString.split(":");   //用分号分割字符串
        version = args[0];   //版本号
        senderName = args[1];	//发送者昵称
        highPressure=Integer.parseInt(args[2]);
        lowPressure=Integer.parseInt(args[3]);
        heartbeat=Integer.parseInt(args[4]);
        commandNo = Integer.parseInt(args[5]); //其实就相当与把字符型数据转换成整型
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

    public void setLowPressure(int lowPressure) {
        this.lowPressure = lowPressure;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public int getCommandNo() {
        return commandNo;
    }

    public void setCommandNo(int commandNo) {
        this.commandNo = commandNo;
    }

    public String getProtocolString(){
        StringBuffer sb = new StringBuffer();

        sb.append(version);
        sb.append(":");
        sb.append(senderName);
        sb.append(":");
        sb.append(commandNo);



        return sb.toString();
    }
    public String getMinutes(){
        Date nowDate = new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String time=sdf.format(nowDate);
        return time;
    }

}
