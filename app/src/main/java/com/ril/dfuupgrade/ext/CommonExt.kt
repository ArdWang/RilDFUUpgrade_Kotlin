package com.ril.dfuupgrade.ext

import android.view.View


/*
 *  Copyright © 2018 Radiance Instruments Ltd. All rights reserved.
 *  author ArdWang
 *  email 278161009@qq.com
 *  Created by ArdWang on 8/2/19.
 */

/**
    扩展点击事件
 */
fun View.onClick(listener: View.OnClickListener): View {
    setOnClickListener(listener)
    return this
}

/**
    扩展点击事件，参数为方法
 */
fun View.onClick(method:() -> Unit): View {
    setOnClickListener { method() }
    return this
}

/**
    扩展视图可见性
 */
fun View.setVisible(visible:Boolean){
    this.visibility = if (visible) View.VISIBLE else View.GONE
}
