package com.example.handsight

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

open class BaseModuleActivity : AppCompatActivity() {
    protected var mBackgroundThread: HandlerThread? = null
    protected var mBackgroundHandler: Handler? = null
    protected var mUIHandler: Handler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUIHandler = Handler(mainLooper)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        startBackgroundThread()
    }

    protected fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("ModuleActivity")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    override fun onDestroy() {
        stopBackgroundThread()
        super.onDestroy()
    }

    protected fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e("TEST", "Error on stopping background thread", e)
        }
    }

    protected open val infoViewAdditionalText: String?
        protected get() = null

    companion object {
        protected const val infoViewCode = 0
    }
}