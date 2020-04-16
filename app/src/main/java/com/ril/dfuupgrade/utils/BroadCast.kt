package com.ril.dfuupgrade.utils

object BroadCast {

    val EXTRA_CHARACTERISTIC_ERROR_MESSAGE = "com.dt.EXTRA_CHARACTERISTIC_ERROR_MESSAGE"
    val ACTION_GATT_CHARACTERISTIC_ERROR = "com.dt.action.ACTION_GATT_CHARACTERISTIC_ERROR"
    val ACTION_GATT_CONNECTED = "com.dt.action.ACTION_GATT_CONNECTED"   //连接
    val ACTION_GATT_DISCONNECTED = "com.dt.action.ACTION_GATT_DISCONNECTED"  //断开链接
    val ACTION_GATT_SERVICES_DISCOVERED = "com.dt.action.ACTION_GATT_SERVICES_DISCOVERED" //发现设备
    val ACTION_DATA_AVAILABLE = "com.dt.action.ACTION_DATA_AVAILABLE"

    //自定义的
    val ACTION_OTA_DATA_AVAILABLE = "com.dt.bluetooth.le.ACTION_OTA_DATA_AVAILABLE"

    //开关广播
    val ACTION_SWITCH_DATA_AVAILABLE = "com.dt.action.ACTION_SWITCH_DATA_AVAILABLE"

    //断开连接的时候
    val ACTION_DISCONNECTED_AVAILABLE = "com.dt.action.ACTION_DISCONNECTED_AVAILABLE"


    //温度数据的传输
    val EXTRA_TEMP_DATA = "com.bw.bwk.action.EXTRA_TEMP_DATA"

    //开关
    val EXTRA_TEMP_SWCHIN = "com.bw.bwk.action.EXTRA_TEMP_SWCHIN"

    //单位数据
    val EXTRA_TEMP_UNITC = "com.bw.bwk.action.EXTRA_TEMP_UNITC"

    //CR清除数据
    val EXTRA_TEMP_CLEAR = "com.bw.bwk.action.EXTRA_TEMP_CLEAR"

    //数据接受成功
    val ACTION_DATA_RECIVER_AVAILABLE = "com.dt.action.ACTION_DATA_RECIVER_AVAILABLE"

    //多台
    val ACTION_ALL_DATA_AVAILABLE = "com.dt.action.ACTION_ALL_DATA_AVAILABLE"


    //OTA的
    val ACTION_GATT_CONNECTING = "com.dt.bluetooth.le.ACTION_GATT_CONNECTING"

    val ACTION_GATT_DISCONNECTED_CAROUSEL = "com.dt.bluetooth.le.ACTION_GATT_DISCONNECTED_CAROUSEL"
    val ACTION_GATT_DISCONNECTED_OTA = "com.dt.bluetooth.le.ACTION_GATT_DISCONNECTED_OTA"
    val ACTION_GATT_CONNECT_OTA = "com.dt.bluetooth.le.ACTION_GATT_CONNECT_OTA"
    val ACTION_GATT_SERVICES_DISCOVERED_OTA = "com.dt.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED_OTA"
    val ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL = "com.dt.bluetooth.le.ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL"

    //系统的
    //系统的
    val ACTION_WRITE_SUCCESS = "android.bluetooth.device.action.ACTION_WRITE_SUCCESS"
    val ACTION_WRITE_FAILED = "android.bluetooth.device.action.ACTION_WRITE_FAILED"
    val ACTION_PAIR_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST"
    val ACTION_WRITE_COMPLETED = "android.bluetooth.device.action.ACTION_WRITE_COMPLETED"


}




