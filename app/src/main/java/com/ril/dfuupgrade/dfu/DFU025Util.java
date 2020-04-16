package com.ril.dfuupgrade.dfu;


import android.content.Context;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DFU025Util {

    private static CRC16 crc16 = new CRC16();

    private static final byte CPMEOF = 0x1A;/* Fill the last package if not long enough */

    /**
     * Get InputStream from Assets, you can customize it from the other sources
     *
     * @param fileAbsolutePath absolute path of the file in asstes
     */
    public static InputStream getInputStream(Context context, String fileAbsolutePath) throws IOException {
        return new InputStreamSource().getStream(context, fileAbsolutePath);
    }


    /**
     * 十六进制字符串转byte[]
     *
     * @param hex
     *            十六进制字符串
     * @return byte[]
     */
    public static byte[] hexStr2Byte(String hex) {
        if (hex == null) {
            return new byte[] {};
        }

        // 奇数位补0
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }

        int length = hex.length();
        ByteBuffer buffer = ByteBuffer.allocate(length / 2);
        for (int i = 0; i < length; i++) {
            String hexStr = hex.charAt(i) + "";
            i++;
            hexStr += hex.charAt(i);
            byte b = (byte) Integer.parseInt(hexStr, 16);
            buffer.put(b);
        }
        return buffer.array();
    }

    /**
     * byte[]转十六进制字符串
     *
     * @param array
     *            byte[]
     * @return 十六进制字符串
     */
    public static String byteArrayToHexString(byte[] array) {
        if (array == null) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            buffer.append(byteToHex(array[i]));
        }
        return buffer.toString();
    }

    /**
     * byte转十六进制字符
     *
     * @param b
     *            byte
     * @return 十六进制字符
     */
    public static String byteToHex(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        return hex.toUpperCase(Locale.getDefault());
    }


    /**
     * int转byte数组
     * @param num
     * @return
     */
    public static byte[] intToByte(int num) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) ((num >> 24) & 0xff);
        bytes[2] = (byte) ((num >> 16) & 0xff);
        bytes[1] = (byte) ((num >> 8) & 0xff);
        bytes[0] = (byte) (num & 0xff);
        return bytes;
    }

    /**
     * 对象转数组
     * @param obj
     * @return
     */
    private byte[] toByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray ();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    /**
     * java对象序列化成字节数组
     *
     * @param object
     * @return
     */
    public static byte[] toBytes(Object object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } finally {
            try {
                oos.close();
            } catch (Exception e) {
            }
        }
    }


    /**
     * Get a encapsulated package data block
     * 获取封装的包数据块
     *
     * @param block      byte data array
     * @param dataLength the actual content length in the block without 0 filled in it.
     * @return a encapsulated package data block
     */
    public static byte[] getDataPackage(byte[] block, int dataLength) throws IOException {
        //每次传输 64个字节的数据
        //byte[] header = getDataHeader(sequence, block.length == 64);
        //The last package, fill CPMEOF if the dataLength is not sufficient
        if (dataLength < block.length) {
            int startFil = dataLength;
            while (startFil < block.length) {
                block[startFil] = CPMEOF;
                startFil++;
            }
        }

        //We should use short size when writing into the data package as it only needs 2 bytes
        short crc = (short) crc16.calcCRC(block);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeShort(crc);
        dos.close();
        byte[] crcBytes = baos.toByteArray();
        return concat(block,crcBytes);
    }

    //System.arraycopy()方法
    private static byte[] concat(byte[] bt1, byte[] bt2){
        byte[] bt3 = new byte[bt1.length+bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }



    /**
       * splitAry方法<br>
       * @param ary 要分割的数组
       * @param subSize 分割的块大小
       * @return subAry
       */
    public static Object[] splitAry(String filePath, int subSize) throws IOException{
        //Get byte files
        byte[] ary = filePath.getBytes();
        int count = ary.length % subSize == 0 ? ary.length / subSize : ary.length / subSize + 1;
        List<List<Byte>> subAryList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = i * subSize;
            List<Byte> list = new ArrayList<>();
            int j = 0;
            while (j < subSize && index < ary.length) {
                list.add(ary[index++]);
                j++;
            }
            subAryList.add(list);
        }

        Object[] subAry = new Object[subAryList.size()];
        for(int i = 0; i < subAryList.size(); i++){
            List<Byte> subList = subAryList.get(i);
            byte[] subAryItem = new byte[subList.size()];
            for(int j = 0; j < subList.size(); j++){
                subAryItem[j] = subList.get(j);
            }
            subAry[i] = data(subAryItem);
        }
        return subAry;
    }


    private static byte[] data(byte[] array) throws IOException{
        short crc = (short) crc16.calcCRC(array);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeShort(crc);
        dos.close();
        byte[] crcBytes = baos.toByteArray();

        //把两个byte[]数组合并
        return concat(array,crcBytes);
    }



}
