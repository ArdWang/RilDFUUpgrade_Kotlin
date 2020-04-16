package com.ril.dfuupgrade.widgets

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.Gravity
import android.widget.ImageView
import com.ril.dfuupgrade.R
import kotlinx.android.synthetic.main.progress_dialog.*
import org.jetbrains.anko.find
import java.util.*


/*
 *  Copyright © 2018 Radiance Instruments Ltd. All rights reserved.
 *  author ArdWang
 *  email 278161009@qq.com
 *  Created by ArdWang on 8/2/19.
 *
 *
 *  自定义加载Dialog
 */

class ProgressLoading private constructor(context:Context,theme:Int):Dialog(context,theme){
    companion object {

        private lateinit var mDialog: ProgressLoading

        private var animDrawable:AnimationDrawable?= null


        /*
            创建ProgressDialog
        * */
        fun create(context: Context):ProgressLoading{
            mDialog = ProgressLoading(context, R.style.LightProgressDialog)
            mDialog.setContentView(R.layout.progress_dialog)
            mDialog.setCancelable(true)

            mDialog.setCanceledOnTouchOutside(false)
            mDialog.window.attributes.gravity = Gravity.CENTER

            val lp = mDialog.window.attributes
            lp.dimAmount = 0.2f
            mDialog.window.attributes = lp

            val loadingView = mDialog.find<ImageView>(R.id.iv_loading)
            animDrawable = loadingView.background as AnimationDrawable

            return mDialog
        }
    }


    /**
     * 显示
     */
    fun showLoading(delayed:Int,text:String,isDisplay: Boolean){
        super.show()
        animDrawable?.start()

        if(text.isNotEmpty()){
            mLoadTxt.text = text
        }else{
            mLoadTxt.text = "Loading..."
        }

        if(isDisplay){
            val time = 1000 * delayed
            val timer = Timer()
            val timerTask = object : TimerTask() {
                override fun run() {
                    hideLoading()
                }
            }
            timer.schedule(timerTask, time.toLong())
        }
    }


    /**
     * 隐藏
     */
    fun hideLoading(){
        super.dismiss()
        animDrawable?.stop()
    }


}