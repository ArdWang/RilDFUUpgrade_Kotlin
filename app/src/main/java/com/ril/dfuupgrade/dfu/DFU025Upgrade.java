package com.ril.dfuupgrade.dfu;

import android.content.Context;
import android.util.Log;



public class DFU025Upgrade implements FileStreamThread.DataRaderListener{

    private DFU025Listener listener;

    private Context context;

    private String filePath;

    private String fileName;

    //需要时候增加进去
    private String fileMd5String;

    //标志位置
    private int index_pack;
    private int index_pack_cache;

    //状态标注
    private static final String STATE_C = "C";
    private static final String STATE_A = "A";
    private static final String STATE_E = "E";
    private static final String STATE_OK = "OK";

    //每次需要发送数据包的大小
    private int sendSize;

    //发送完成每包间隔时间
    private static final int SLEEP_TIME = 50;

    private byte[] currSending = null;

    private TimeOutHelper timerHelper = new TimeOutHelper();

    private int packageErrorTimes = 0;

    private int bytesSent = 0;

    private static final int MAX_PACKAGE_SEND_ERROR_TIMES = 6;

    //the timeout interval for a single package
    private static final int PACKAGE_TIME_OUT = 6000;

    private FileStreamThread streamThread;



    /**
     * Construct of the YModemBLE,you may don't need the fileMD5 checking,remove it
     * YMODESMLE的构造，您可能不需要FLIMD5检查，删除它
     *
     * @param filePath       absolute path of the file
     * @param fileName file name for sending to the terminal
     * @param fileMd5String  md5 for terminal checking after transmission finished 传输结束后的终端检查MD5
     */

    private DFU025Upgrade(Context context, String filePath,String fileName,String fileMd5String, int sendSize, DFU025Listener listener){
        this.context = context;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileMd5String = fileMd5String;
        this.sendSize = sendSize;
        this.listener = listener;
    }


    /**
     * 生产的时候需要这么写
     */
    private void start(){
        index_pack = -1;
        index_pack_cache = -2;
        sendData();
    }


    /**
     * 生产的时候把这个加进去
     */
    public void stop(){
        index_pack = -1;
        index_pack_cache = -2;

        bytesSent = 0;
        currSending = null;
        packageErrorTimes = 0;
        if (streamThread != null) {
            streamThread.release();
        }
        timerHelper.stopTimer();
        timerHelper.unRegisterListener();
    }


    /**
     * 发送数据开始启动
     */
    public void sendData() {
        streamThread = new FileStreamThread(context, filePath, sendSize,this);
        String ok = getFileSize()+"";
        Lg.i("file size is"+ok);
        streamThread.start();
        Lg.f("StartData!!!");
    }


    private void startSendFileData() {
        //CURR_STEP = STEP_FILE_BODY;
        Lg.f("startSendFileData");
        streamThread.start();
    }


    /**
     * 接收蓝牙传递过来的数据 外部调用
     * @param respData
     */
    public void onReceiveBleData(byte[] respData) {
        timerHelper.stopTimer();
        if (respData != null && respData.length > 0) {
            Lg.f("Device received " + respData.length + " bytes.");
             String reciveData= DFU025Util.byteArrayToHexString(respData);

             //握手成功可以发送数据
            if(reciveData.equals(STATE_C)){
                //启动开始发送数据
                start();
                handleFileBody(respData);
            }else if(reciveData.equals(STATE_A)){
                handleFileBody(respData);
            }else if(reciveData.equals(STATE_E)){
                handlePackageFail("Recive Error!");
            }else if(reciveData.equals(STATE_OK)){
                packageErrorTimes = 0;
                //发送已经成功，完全结束
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        }
    }


    private void handleFileBody(byte[] value) {
        Lg.f("Device received " + value.length + " bytes.");
        packageErrorTimes = 0;
        bytesSent += currSending.length;
        try {
            if (listener != null) {
                listener.onProgress(bytesSent, streamThread.getFileByteSize());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        streamThread.keepReading();
    }


    /**
     * Get File Size
     */
    private long getFileSize(){
        long size = 0;
        if(filePath!=null){
           size = FileSizeUtil.getAutoFileOrFilesLongSize(filePath);
        }
        return size;
    }


    @Override
    public void onDataReady(byte[] data) {
        String s = DFU025Util.byteArrayToHexString(data);
        Log.i("TAG====is byte[]:",s);
        sendPackageData(data);
    }

    @Override
    public void onFinish() {
        Lg.i("sendEND");
    }

    //The timeout listener
    private TimeOutHelper.ITimeOut timeoutListener = new TimeOutHelper.ITimeOut() {
        @Override
        public void onTimeOut() {
            Lg.f("------ time out ------");
            if (currSending != null) {
                handlePackageFail("package timeout...");
            }
        }
    };

    //Handle a failed package data ,resend it up to MAX_PACKAGE_SEND_ERROR_TIMES times.
    //处理失败的包数据
    //If still failed, then the transmission failed.
    private void handlePackageFail(String reason) {
        packageErrorTimes++;
        Lg.f("Fail:" + reason + " for " + packageErrorTimes + " times");
        stop();
        if (listener != null) {
            listener.onFailed(reason);
        }
    }

    private void sendPackageData(byte[] packageData) {
        if (listener != null && packageData != null) {
            currSending = packageData;
            //Start the timer, it will be cancelled when reponse received,
            // or trigger the timeout and resend the current package data
            //启动计时器，当收到回复时将被取消，
            //或触发超时并重新发送当前包数据
            timerHelper.startTimer(timeoutListener, PACKAGE_TIME_OUT);
            listener.onDataSend(packageData);
        }
    }


    public static class Builder{

        private Context context;
        private String filePath;
        private String fileName;
        private String fileMd5String;
        private DFU025Listener listener;
        private int sendSize;

        public Builder with(Context context) {
            this.context = context;
            return this;
        }

        public Builder sendSize(int sendSize){
            this.sendSize = sendSize;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }


        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder checkMd5(String fileMd5String) {
            this.fileMd5String = fileMd5String;
            return this;
        }

        public Builder callback(DFU025Listener listener) {
            this.listener = listener;
            return this;
        }

        public DFU025Upgrade build() {
            return new DFU025Upgrade(context, filePath, fileName, fileMd5String, sendSize, listener);
        }

    }

}
