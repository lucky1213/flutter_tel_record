package com.example.flutter_tel_record

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

class MainThreadExecutor : Executor {
    private val handler = Handler(Looper.getMainLooper())

    override fun execute(r: Runnable?) {
        r?.let {
            handler.post(it)
        }
    }
}