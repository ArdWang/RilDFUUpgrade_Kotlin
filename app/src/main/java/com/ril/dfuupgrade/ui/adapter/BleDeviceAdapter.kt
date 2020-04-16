package com.ril.dfuupgrade.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.clw.base.ui.adapter.BaseRecyclerViewAdapter
import com.ril.dfuupgrade.R
import kotlinx.android.synthetic.main.layout_device_n_item.view.*
import java.time.Year
import java.util.ArrayList

class BleDeviceAdapter(context: Context) : BaseRecyclerViewAdapter<BleDevice, BleDeviceAdapter.ViewHolder>(context) {

    //private val mLeDevices: ArrayList<BleDevice> = arrayListOf()

    private val mLeDevices: MutableList<BleDevice> = mutableListOf()

    //拖动事件
    var mDragClickListener: OnDragClickListener<BleDevice>? = null

    //Item 连接 项点击事件
    var mConnectClickListener: OnConnectClickListener<BleDevice>? = null


    /**
     * contains()是判断是否有相同的字符串
     */
    fun addDevice(device: BleDevice) {
        if (!mLeDevices.contains(device)) {
           // if (device.name != null) {
                //if (device.name.contains("TMW025") || device.name.contains("DFU")|| device.name.contains("Acc"))
                //{
                mLeDevices.add(device)
                notifyDataSetChanged()
                //}
            //}
        }
    }

    fun clearList() {
        mLeDevices.removeAll {true}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.layout_device_n_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("RecyclerView", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val device = mLeDevices[position]

            val isConnected = BleManager.getInstance().isConnected(device)
            if (!device.name.isEmpty()) {
                    holder.itemView.mDeviceNameTv.text = device.name
            }else{
                holder.itemView.mDeviceNameTv.text = "UnKnown"
            }

            if (!device.mac.isEmpty()) {
                val array = device.mac.split(":")
                val address = "S/N:" + array[0] + array[1] + array[2] + array[3] + array[4] + array[5]
                holder.itemView.mDeviceMacTv.text = address
            }

            if (isConnected) {
                holder.itemView.mConnectTv.text = "Connected"
            } else {
                holder.itemView.mConnectTv.text = "Connect"
            }

            holder.itemView.mMoreClick.setOnClickListener {
                if (mItemClickListener != null)
                    mItemClickListener!!.onItemClick(mLeDevices[position], position)
            }


            holder.itemView.mDisConBtn.setOnClickListener {
                if (mDragClickListener != null) {
                    mDragClickListener!!.onDisConClick(mLeDevices[position], position)
                }
            }


            holder.itemView.mCancelBtn.setOnClickListener {
                if (mDragClickListener != null) {
                    mDragClickListener!!.onCancelClick(mLeDevices[position], position)
                }
            }


            holder.itemView.mConnectTv.setOnClickListener {
                if (mConnectClickListener != null) {
                    mConnectClickListener!!.onConnectClick(mLeDevices[position], position)
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun clear() {
        if (mLeDevices.size > 0) {
            mLeDevices.clear()
        }
    }


    override fun getItemCount(): Int {
        return mLeDevices.size
    }


    class ViewHolder(view: View): RecyclerView.ViewHolder(view)

    /**
     * ItemClick事件声明
     */
    interface OnItemClickListener<in T> {
        fun onItemClick(item: T, position: Int)
    }

    interface OnConnectClickListener<in T> {
        fun onConnectClick(item: T, position: Int)
    }


    //自定义拖动接口
    interface OnDragClickListener<in T> {
        fun onDisConClick(item: T, pos: Int)
        fun onCancelClick(item: T, pos: Int)
    }

}
