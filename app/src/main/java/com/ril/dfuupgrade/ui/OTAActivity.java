package com.ril.dfuupgrade.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.ril.dfuupgrade.R;
import com.ril.dfuupgrade.dfu.DFU025Listener;
import com.ril.dfuupgrade.dfu.DFU025Upgrade;
import com.ril.dfuupgrade.utils.BroadCast;
import com.ril.dfuupgrade.utils.Constants;
import com.ril.dfuupgrade.utils.GattAttributes;
import com.ril.dfuupgrade.widgets.TextProgressBar;


import java.util.ArrayList;
import java.util.List;

public class OTAActivity extends AppCompatActivity implements View.OnClickListener{
    /**
     * 静态的非变量成员
     */
    public static final int mApplicationUpgrade = 101;
    public static final int mApplicationAndStackCombined = 201;
    public static final int mApplicationAndStackSeparate = 301;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    //public BluetoothService bluetoothService;

    private Button mOTAUpdate;
    //OTA
    public static BluetoothGattCharacteristic mSendOTACharacteristic;

    //获取得到的设备地址和设备名称
    private String deviceAddre;

    //private YModem yModem;

    private DFU025Upgrade dfuUpgrade;

    private String mCurrentFilePath;

    private String mCurrentFileName;

    private boolean sendData;

    private TextProgressBar mUpgradeBar;

    private TextView mUpgradeFilename;

    private Button mOTAStop;

    private static final String TAG = "OTAActivity";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota);

        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if(mCurrentFileName!=null) {
            mUpgradeFilename.setText(mCurrentFileName);
        }else{
            mUpgradeFilename.setText("选择需要升级的文件");
        }
    }

    /**
     * 发送服务广播
     * @return
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        return intentFilter;
    }


    private void initView() {
        mOTAUpdate = findViewById(R.id.mOTAUpdate);
        mUpgradeBar = findViewById(R.id.mUpgradeBar);
        mUpgradeFilename = findViewById(R.id.mUpgradeFilename);
        mOTAStop = findViewById(R.id.mOTAStop);

        mOTAUpdate.setOnClickListener(this);
        mOTAStop.setOnClickListener(this);

        //startTransmission();
    }

    private void initData(){
        deviceAddre = getIntent().getStringExtra(OTAActivity.EXTRAS_DEVICE_ADDRESS);
        //创建service
        //Intent gattServiceIntent = new Intent(this, BluetoothService.class);
        //bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 蓝牙连接
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //断开的时候作出来判断
        }
        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {

        }
    };



    public void onDataReceivedFromBLE(byte[] data) {
        sendData = true;
    }

    public static byte[] strToByteArray(String str) {
        if (str == null) {
            return null;
        }
        byte[] byteArray = str.getBytes();
        return byteArray;
    }

    private void startTransmission(){
        //String md5 = MD5Util.MD5(mCurrentFilePath);
        dfuUpgrade = new DFU025Upgrade.Builder()
                .with(this)
                .filePath(mCurrentFilePath)
                .fileName(mCurrentFileName)
                .sendSize(256)
                .checkMd5("")
                .callback(new DFU025Listener() {
                              @Override
                              public void onDataSend(byte[] data) {
                                    Log.i("data is:",data.toString());
                              }

                              @Override
                              public void onProgress(int currentSent, int total) {

                              }

                              @Override
                              public void onSuccess() {

                              }

                              @Override
                              public void onFailed(String reason) {

                              }
                          }).build();

                          //dfuUpgrade.start();
                         // dfuUpgrade.testFile(mCurrentFileName);
    }

    public static String bytesToHexFun(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for(byte b : bytes) { // 使用String的format方法进行转换
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }
        return buf.toString();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.mOTAUpdate:
                if(mCurrentFileName==null&&mCurrentFilePath==null){
                    mOTAUpdate.setText("点我升级");
                    Intent ApplicationAndStackCombined = new Intent(OTAActivity.this, FileListActivity.class);
                    ApplicationAndStackCombined.putExtra("FilesName", "Files");
                    ApplicationAndStackCombined.putExtra(Constants.REQ_FILE_COUNT, mApplicationAndStackCombined);
                    startActivityForResult(ApplicationAndStackCombined, mApplicationAndStackCombined);

                }else {
                    mOTAUpdate.setVisibility(View.GONE);
                    mOTAStop.setVisibility(View.VISIBLE);
                    //boolean isSuccess = bluetoothService.writeCharacteristic(OTAActivity.mSendOTACharacteristic,"0x05");


                    startTransmission();
                    dfuUpgrade.sendData();
                }
                break;

            case R.id.mOTAStop:


                //finish();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            ArrayList<String> selectedFiles = data.
                    getStringArrayListExtra(Constants.ARRAYLIST_SELECTED_FILE_NAMES);
            ArrayList<String> selectedFilesPaths = data.
                    getStringArrayListExtra(Constants.ARRAYLIST_SELECTED_FILE_PATHS);
            if (requestCode == mApplicationUpgrade) {
                mCurrentFileName = selectedFiles.get(0);
                mCurrentFilePath = selectedFilesPaths.get(0);
            } else if (requestCode == mApplicationAndStackCombined) {
                mCurrentFileName = selectedFiles.get(0);
                mCurrentFilePath = selectedFilesPaths.get(0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dfuUpgrade.stop();
        //unregisterReceiver(mGattUpdateReceiver);
        //断开服务连接
        unbindService(mServiceConnection);

    }


}
