package com.ril.dfuupgrade.base.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ril.dfuupgrade.common.AppManager


open class BaseActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppManager.instance.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppManager.instance.finishActivity(this)
    }
}