package com.ril.dfuupgrade

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.clw.base.ui.adapter.BaseRecyclerViewAdapter
import com.ril.dfuupgrade.base.ui.BaseActivity
import com.ril.dfuupgrade.ui.OTAActivity
import com.ril.dfuupgrade.ui.adapter.BleDeviceAdapter
import com.ril.dfuupgrade.utils.BroadCast
import com.ril.dfuupgrade.widgets.CustomDialog
import com.ril.dfuupgrade.widgets.ProgressLoading
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.File
import java.util.ArrayList
import com.tbruyelle.rxpermissions2.RxPermissions

class MainActivity : BaseActivity() , SwipeRefreshLayout.OnRefreshListener{

    companion object {
        const val SCAN_PERIOD = 10000
        const val REQUEST_CODE_OPEN_GPS = 1
        const val REQUEST_CODE_PERMISSION_LOCATION = 2
        const val REQUEST_CODE_STORE = 3
        const val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"
        //const val EXTRAS_DEVICE_NAME = "DEVICE_NAME"
    }

    private lateinit var  mDeviceAdapter: BleDeviceAdapter

    private lateinit var progressLoading: ProgressLoading

    private lateinit var dialog:CustomDialog

    private var mBleDevice: BleDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initData()
    }

    private fun initView(){
        mSinglesRv.layoutManager = LinearLayoutManager(this)
        //刷新
        mSwipesLayout.setOnRefreshListener(this)
        mSwipesLayout.setColorSchemeResources(R.color.colorPrimary)
    }

    private fun initData(){

        progressLoading = ProgressLoading.create(this)

        requestPermissions()

        BleManager.getInstance().init(application)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(10, 5000)
            .setConnectOverTime(20000).operateTimeout = SCAN_PERIOD
        checkPermissions()

        createFile()


        mDeviceAdapter = BleDeviceAdapter(this)

        mSinglesRv.adapter = mDeviceAdapter


        //点击事件

        mDeviceAdapter.mDragClickListener = object :BleDeviceAdapter.OnDragClickListener<BleDevice>{
            override fun onDisConClick(item: BleDevice, pos: Int) {
                //如果是连接的情况
                if (BleManager.getInstance().isConnected(item)) {
                    BleManager.getInstance().cancelScan()
                    mDeviceAdapter.clear()
                    mDeviceAdapter.notifyDataSetChanged()
                    //移除设备
                    BleManager.getInstance().disconnect(item)
                    //重新刷新
                    checkPermissions()
                }
            }

            override fun onCancelClick(item: BleDevice, pos: Int) {
                Log.i("取消点击","Cancel")
            }

        }

       // startActivity<OTAActivity>(EXTRAS_DEVICE_ADDRESS to "qqqq")

        mDeviceAdapter.mConnectClickListener = object :BleDeviceAdapter.OnConnectClickListener<BleDevice>{
            override fun onConnectClick(item: BleDevice, position: Int) {
                //如果是连接的情况
                if (BleManager.getInstance().isConnected(item)) {
                    BleManager.getInstance().cancelScan()
                    mDeviceAdapter.clear()
                    mDeviceAdapter.notifyDataSetChanged()
                    //移除设备
                    BleManager.getInstance().disconnect(item)
                }

                //这里是要做处理的
                if (!BleManager.getInstance().isConnected(item)) {
                    BleManager.getInstance().cancelScan()
                    connect(item)
                }
            }
        }


        mDeviceAdapter.mItemClickListener = object : BleDeviceAdapter.OnItemClickListener<BleDevice>,
            BaseRecyclerViewAdapter.OnItemClickListener<BleDevice> {
            override fun onItemClick(item: BleDevice, position: Int) {
                val builder = CustomDialog.Builder(this@MainActivity)
                builder.setTitle("Connect")
                builder.setMessage("Device Connect")

                builder.setPositiveButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
//                    //如果是连接的情况
//                    if (BleManager.getInstance().isConnected(item)) {
//                        BleManager.getInstance().cancelScan()
//                        //移除设备
//                        BleManager.getInstance().disconnect(item)
//                        mDeviceAdapter.clear()
//                        mDeviceAdapter.notifyDataSetChanged()
//                    }
                    finish()
                }).setNegativeButton("Connect", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    //选择跳转
                    if (item.name.contains("DFU")) {
                        //如果是连接的情况
                        if (BleManager.getInstance().isConnected(item)) {
                            BleManager.getInstance().cancelScan()
                            //移除设备
                            BleManager.getInstance().disconnect(item)
                        }
                        startActivity<OTAActivity>(EXTRAS_DEVICE_ADDRESS to item.mac)
                        //val intent = Intent(this@NMainActivity, OTAActivity::class.java)
                        //intent.putExtra("deviceName", item.name)
                        //intent.putExtra("deviceMac", item.mac)
                        //startActivity(intent)
                    }
                })

                dialog = builder.create()
                dialog.show()
                dialog.setCanceledOnTouchOutside(true)
            }
        }
    }


    override fun onRefresh() {
        Handler().postDelayed({

            checkPermissions()
            mDeviceAdapter.clearList()
            setScanRule()
            startScan()
            // 停止刷新
            mSwipesLayout.isRefreshing = false
        }, 3000)
    }


    /**
     *  开启所有的权限
     */
    @SuppressLint("CheckResult")
    private fun requestPermissions() {
        val rxPermission = RxPermissions(this@MainActivity)
        rxPermission
            .requestEach(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH
            )
            .subscribe { permission ->
                when {
                    permission.granted -> // 用户已经同意该权限
                        Log.d("TAG", permission.name + " is granted.")
                    permission.shouldShowRequestPermissionRationale -> // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时。还会提示请求权限的对话框
                        Log.d("TAG", permission.name + " is denied. More info should be provided.")
                    else -> // 用户拒绝了该权限，而且选中『不再询问』
                        Log.d("TAG", permission.name + " is denied.")
                }
            }
    }


    /*
            检查权限是否存在
    */
    private fun checkPermissions() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show()
            return
        }

        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionDeniedList = ArrayList<String>()
        for (permission in permissions) {
            val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission)
            } else {
                permissionDeniedList.add(permission)
            }
        }

        if (!permissionDeniedList.isEmpty()) {
            val deniedPermissions = permissionDeniedList.toTypedArray()
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION)
        }
    }


    /**
     * 同意开启权限
     */
    private fun onPermissionGranted(permission: String) {
        when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.notifyTitle)
                    .setMessage(R.string.gpsNotifyMsg)
                    .setNegativeButton(R.string.cancel
                    ) { _, _ -> finish() }
                    .setPositiveButton(R.string.setting
                    ) { _, _ ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
                    }
                    .setCancelable(false)
                    .show()
            } else {
                setScanRule()
                startScan()
            }
        }
    }


    private fun checkGPSIsOpen(): Boolean {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    /**
     * Create File
     */
    private fun createFile() {
        try {
            if (isOk()) {
                val b = "/storage/emulated/0/DFU025_FILE"
                val file = File(b)
                if (!file.exists()) {
                    file.mkdirs()
                    Log.i("Create_file", "文件夹不存在创建文件夹")
                } else {
                    Log.i("Create_file", "文件夹存在不需要创建")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun isOk(): Boolean {
        val status = Environment.getExternalStorageState()
        return status == Environment.MEDIA_MOUNTED
    }


    private fun setScanRule() {
        val scanRuleConfig = BleScanRuleConfig.Builder()
            .setServiceUuids(null)      // 只扫描指定的服务的设备，可选
            .setDeviceName(true)   // 只扫描指定广播名的设备，可选
            .setDeviceMac(null)                  // 只扫描指定mac的设备，可选
            .setAutoConnect(false)      // 连接时的autoConnect参数，可选，默认false
            .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
            .build()
        BleManager.getInstance().initScanRule(scanRuleConfig)
    }


    private fun startScan() {
        BleManager.getInstance().scan(object : BleScanCallback() {
           override fun onScanStarted(success: Boolean) {
                mDeviceAdapter.clear()
                mDeviceAdapter.notifyDataSetChanged()
            }

            override fun onLeScan(bleDevice: BleDevice?) {
                super.onLeScan(bleDevice)
                Log.i("Begin...","Start")
            }

            override fun onScanning(bleDevice: BleDevice) {
                mDeviceAdapter.addDevice(bleDevice)
                mDeviceAdapter.notifyDataSetChanged()
            }

           override fun onScanFinished(scanResultList: List<BleDevice>) {
                //扫描所有的数据返回
            }
        })
    }


    private fun connect(bleDevice: BleDevice) {
        BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
           override fun onStartConnect() {
               //20秒后自动关闭
               progressLoading.showLoading(20, "Data Reading...", true)
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                toast("连接失败!")
            }

           override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt, status: Int) {
                if (bleDevice != null) {
                    progressLoading.hideLoading()

                    sendMtu(bleDevice)
                    mDeviceAdapter.addDevice(bleDevice)
                    mDeviceAdapter.notifyDataSetChanged()
                    mBleDevice = bleDevice
                }
            }

            override fun onDisConnected(isActiveDisConnected: Boolean, bleDevice: BleDevice?, gatt: BluetoothGatt, status: Int) {
                if (bleDevice != null) {
                    mDeviceAdapter.clear()
                    mDeviceAdapter.notifyDataSetChanged()
                    //发送断开的广播
                    val intent = Intent()
                    intent.action = BroadCast.ACTION_DISCONNECTED_AVAILABLE
                    intent.putExtra("disconnectData", bleDevice.mac)
                    //发送广播
                    sendBroadcast(intent)
                    toast(bleDevice.mac+" is DisConnected")
                }
            }
        })
    }



    private fun sendMtu(device: BleDevice?) {
        BleManager.getInstance().setMtu(device, 512, object : BleMtuChangedCallback() {
            override fun onSetMTUFailure(exception: BleException) {}
            override fun onMtuChanged(mtu: Int) {
                Log.i("Send mtu tag is ", "$mtu success!")
            }
        })
    }


    /**
     * 启动的时候要扫描蓝牙设备
     */
    override fun onResume() {
        super.onResume()
        mBleDevice = null
        //检查权限
        checkPermissions()
    }

    override fun onPause() {
        super.onPause()
        Log.i("OnPause","ok")
    }


    /**
     * 返回
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION_LOCATION -> if (grantResults.isNotEmpty()) {
                for (i in grantResults.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        onPermissionGranted(permissions[i])
                    }
                }
            }

            REQUEST_CODE_STORE -> if (grantResults.isNotEmpty()) {
                for (i in grantResults.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        onPermissionGranted(permissions[i])
                    }
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                setScanRule()
                startScan()
            }
        }
    }


   override fun onDestroy() {
        super.onDestroy()
        BleManager.getInstance().cancelScan()
        BleManager.getInstance().disconnectAllDevice()
        BleManager.getInstance().destroy()
    }


}
