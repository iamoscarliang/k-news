package com.oscarliang.knews.util

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.oscarliang.knews.TestApp

class NewsTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        return super.newApplication(cl, TestApp::class.java.name, context)
    }

}