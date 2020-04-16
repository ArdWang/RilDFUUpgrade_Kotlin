package com.ril.dfuupgrade.utils

object GattAttributes {
    //温度类型
    val TEMPERATURE_TYPE = "00002a1d-0000-1000-8000-00805f9b34fb"
    //usual
    val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
    //通过OTA发送数据到设备 更新设备的功能
    val BW_PROJECT_OTA_DATA = "4E8A02FE-BB42-452D-B573-E0645F03C230"

    //DFU UPGRADE
    val BW_DFU_UPGRADE = "33F1899C-B6C5-4E6A-9238-14EA25BF4A77"

}
