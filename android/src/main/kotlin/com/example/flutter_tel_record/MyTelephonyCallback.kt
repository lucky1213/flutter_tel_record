//package com.example.flutter_tel_record
//
//import android.os.Build
//import android.telephony.TelephonyCallback
//import android.util.Log
//import androidx.annotation.RequiresApi
//
//@RequiresApi(Build.VERSION_CODES.S)
//open class MyTelephonyCallback: TelephonyCallback(), TelephonyCallback.CallStateListener {
//    override fun onCallStateChanged(state: Int) {
//        Log.e("MyTelephonyCallback", state.toString())
//    }
//}