package com.example.flutter_tel_record

import android.telephony.PhoneStateListener
import android.util.Log

open class MyPhoneStateListener : PhoneStateListener() {

    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
        Log.e("MyPhoneStateListener", state.toString())
    }
}