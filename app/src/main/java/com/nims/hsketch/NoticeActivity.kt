package com.nims.hsketch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class NoticeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)
    }

    override fun finish() {
        super.finish()
        DM.getInstance().finishActivity(this)
    }
}