package com.example.flutter_tel_record

//import android.telephony.TelephonyCallback
import android.R.attr.src
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class TelRecordService(binding: FlutterPlugin.FlutterPluginBinding) : EventChannel.StreamHandler {
    private var mainThreadExecutor: MainThreadExecutor = MainThreadExecutor()

    private var telephonyManager: TelephonyManager? = null
    // private var telephonyCallback: TelephonyCallback? = null
    private var phoneStateListener: PhoneStateListener? = null
    private  var context : Context
    private  var eventChannel: EventChannel
    private var eventSink: EventChannel.EventSink? = null

    private var uuid: String? = null
    private var phoneNumber: String? = null
    private var filename:String = ""
    private var record: Boolean = false
    private var time: Long? = null

    init {
        context = binding.applicationContext
        eventChannel = EventChannel(binding.binaryMessenger,
            "flutter_tel_record/listener"
        )
        eventChannel.setStreamHandler(this)
        telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            telephonyCallback = object: MyTelephonyCallback() {
//                override fun onCallStateChanged(state: Int) {
//                    handleCallStateChanged( state)
//                }
//            }
//            telephonyManager?.registerTelephonyCallback(mainThreadExecutor, telephonyCallback as MyTelephonyCallback)
//        } else {
//
//        }
        phoneStateListener = object: MyPhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                handleCallStateChanged( state)
            }
        }
        telephonyManager?.listen(phoneStateListener as MyPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }

    fun setPhoneNumber(uuid: String?, phoneNumber: String?, filename: String = "", record: Boolean = false) {
        this.uuid = uuid
        this.phoneNumber = phoneNumber
        this.filename = filename
        this.record = record
        time = System.currentTimeMillis()
    }

    private fun handleCallStateChanged(state: Int) {
        Log.e("TelRecordManager", state.toString())
        if (eventSink != null && phoneNumber != null) {
            when (state) {
                /** Device call state: No activity.  */
                TelephonyManager.CALL_STATE_IDLE -> {
                    thread {
                        val callLog = diffCallLog()
                        var newFile: File? = null
                        if ((callLog?.duration ?: 0) > 0 && record && uuid != null ) {
                            val file = RecordFileUtil.getFileSleep(context, 0)
                            if (file != null) {
                                val prefix: String = RecordFileUtil.getExtensionName(file.name)
                                val targetDirectory = File(getRecordDirectory(uuid))
                                val name = let {
                                    val time =  System.currentTimeMillis()
                                    if (filename.isNotEmpty()) {
                                        filename + "_"+ time
                                    } else {
                                        time
                                    }
                                }
                                newFile = File(targetDirectory, "$name.$prefix")
                                val result: Boolean = file.renameTo(newFile)
                                if (!result) {
                                    newFile = null
                                }

                            }
                        }
                        Handler(Looper.getMainLooper()).post {
                            eventSink!!.success(hashMapOf<String, Any?>(
                                "state" to 0,
                                "phoneNumber" to phoneNumber,
                                "file" to newFile?.path,
                                "date" to callLog?.date,
                                "duration" to callLog?.duration,
                            ))
                            time = null
                            uuid = null
                            phoneNumber = null
                            record = false
                            filename = ""
                        }
                    }
                }
                /** Device call state: Off-hook. At least one call exists
                 * that is dialing, active, or on hold, and no calls are ringing
                 * or waiting. */
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    eventSink!!.success(hashMapOf<String, Any?>(
                        "state" to 1,
                        "phoneNumber" to phoneNumber
                    ))
                }
                /** Device call state: Ringing. A new call arrived and is
                 * ringing or waiting. In the latter case, another call is
                 * already active.  */
                TelephonyManager.CALL_STATE_RINGING -> {
                    eventSink!!.success(hashMapOf<String, Any?>(
                        "state" to 2,
                        "phoneNumber" to phoneNumber
                    ))
                }
            }
        }
    }


    private fun getRecordDirectory(subDirectory: String? = null): String {
        val sub = subDirectory ?: ""

        val outputDir = File(context.externalCacheDir, sub)
        if (!outputDir.exists()) {
            outputDir.mkdir()
        }
        return outputDir.absolutePath
    }

    private fun diffCallLog(): MCallLog? {
        val callLog = getCallLog()
        if (callLog != null) {
            if (callLog.dateLong - time!! > -5 * 1000) {
                LogUtils.e("通话时长: ${callLog.duration}", "号码：${callLog.number}", "时间：${callLog.date}")
                return callLog
            }
        }
        Thread.sleep(1000)
        return diffCallLog()
    }
//
    private fun getCallLog(): MCallLog? {
        val logColumns = arrayOf(
            CallLog.Calls.CACHED_NAME// 通话记录的联系人
            , CallLog.Calls.NUMBER// 通话记录的电话号码
            , CallLog.Calls.DATE// 通话记录的日期
            , CallLog.Calls.DURATION// 通话时长
            , CallLog.Calls.TYPE)// 通话类型
        val cursor = context.contentResolver?.query(CallLog.Calls.CONTENT_URI, logColumns,
            "number like '%$phoneNumber%'", null, CallLog.Calls.DEFAULT_SORT_ORDER)
        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                val type = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE))  //通话类型
//                val name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))  //姓名
//                val number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER))  //号码
//                val dateLong = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)) //获取通话日期
//                val durationLong = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)) //获取通话时长
//                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(dateLong))
//                val mCallLog = MCallLog()
//                mCallLog.name = name
//                mCallLog.number = number
//                mCallLog.date = date
//                callLogList.add(mCallLog)
//            }
            val hasRecord = cursor.moveToFirst()

            if (hasRecord) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))  //姓名
                val type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                val duration = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                val number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER))  //号码
                val dateLong = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)) //获取通话日期
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(dateLong))
                val mCallLog = MCallLog()
                mCallLog.name = name
                mCallLog.number = number
                mCallLog.date = date
                mCallLog.dateLong = dateLong
                if (type == CallLog.Calls.OUTGOING_TYPE) {
                    mCallLog.duration = duration
                    //break;
                } else {
                    mCallLog.duration = 0
                }
                return mCallLog
            }
            cursor.close()
        }
        return null
    }

//    override fun onCreate() {
//        myBroadcast = FlutterTelRecordPlugin.TelRecordReceiver()
//        val intentFilter = IntentFilter(FlutterTelRecordPlugin.TelRecordReceiver::class.java.name)
//        registerReceiver(myBroadcast, intentFilter)
//        super.onCreate()
//    }
//
//    override fun onDestroy() {
//        try {
//            if (myBroadcast != null) {
//                unregisterReceiver(myBroadcast)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        super.onDestroy()
//    }
}

class MCallLog {
    var name: String? = ""
    var number: String = ""
    var date: String = ""
    var dateLong: Long = 0L
    var duration: Long = 0L
}