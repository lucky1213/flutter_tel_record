package com.example.flutter_tel_record

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.telecom.TelecomManager
import android.text.TextUtils
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File


/** FlutterTelRecordPlugin */
class FlutterTelRecordPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var context : Context

  private  lateinit var  telRecordManager: TelRecordService

  private var activity: Activity? = null

  var uuid: String? = null
  var phoneNumber: String? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_tel_record")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
    telRecordManager = TelRecordService(flutterPluginBinding)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "requestManngeExternalStorage" -> {
        val hasPermission = requestManngeExternalStorage()
        result.success(hasPermission)
      }
      "openManngeExternalStorageSetting" -> {
        openManngeExternalStorageSetting()
        result.success(null)
      }
      "requestRecordPermission" -> {
        try {
          val permission = RecordUtil.checkAutoRecord(context)
          if (!permission) {
            result.error("10002", "尚未开启自动录音权限", null)
          } else {
            result.success(permission)
          }
        } catch (e: Exception) {
          result.error("10003", "暂不支持该设备", null)
        }
      }
      "openRecordSetting" -> {
        try {
          RecordUtil.openRecordSetting(context)
          result.success(true)
        } catch (e: Exception) {
          result.error("10003", "暂不支持该设备", null)
        }
      }
      "dial" -> {
        if (!hasPermission()) {
          result.error("10000", "缺少权限", null)
          return
        }
        val simIndex: Int = call.argument("simIndex")?:0
        val phone: String = call.argument("phone")!!
        val filename: String = call.argument("filename")?:""
        // 是否获取录音文件
        val record: Boolean = call.argument("record") ?: false
        val ignoreCheck: Boolean = call.argument("ignoreCheck") ?: true
        if (record && !ignoreCheck) {
          try {
            if (!RecordUtil.checkAutoRecord(context)) {
              result.error("10002", "尚未开启自动录音权限", null)
              return
            }
          } catch (e: Exception) {
            result.error("10003", "暂不支持该设备", null)
            return
          }
        }
        if (dial(getDialIntent(phone, simIndex)!!)) {
          val uuid: String? = call.argument("uuid")
          telRecordManager.setPhoneNumber(uuid, phone, filename, record)
          result.success(phone)
        } else {
          result.error("10001", "通话中", null)
        }
      }
      "getRecordFiles" -> {
        val startTime: Long = call.argument("startTime")!!
        val endTime: Long = (call.argument("endTime")?:System.currentTimeMillis())
        val files = RecordFileUtil.getFilesByTimeRange(context, startTime, endTime).map { it.path }
        result.success(files)
      }
      "getRecordCacheDir" -> {
        val recordDir = RecordFileUtil.getRecordCacheDir(context)
        result.success(recordDir)
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  private fun requestManngeExternalStorage() : Boolean {
    var hasPermission = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      hasPermission = XXPermissions.isGranted(context, Permission.MANAGE_EXTERNAL_STORAGE)
      if (!hasPermission) {
        val intent =  Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.data = Uri.parse("package:" + context.packageName);
        startActivityForResult(activity!!, intent, 1024, null)
        hasPermission = XXPermissions.isGranted(context, Permission.MANAGE_EXTERNAL_STORAGE)
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      hasPermission = true
    }

    return hasPermission
  }

  private fun openManngeExternalStorageSetting() {
    XXPermissions.startPermissionActivity(context, Permission.MANAGE_EXTERNAL_STORAGE)
  }


  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private fun hasPermission(): Boolean {
    val storage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      Permission.MANAGE_EXTERNAL_STORAGE
    } else {
      Permission.READ_EXTERNAL_STORAGE
    }
    return XXPermissions.isGranted(context, Permission.MANAGE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE, Permission.READ_PHONE_STATE , Permission.READ_CALL_LOG, Permission.CALL_PHONE)
//    val hasStorge = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//      (ActivityCompat.checkSelfPermission(
//        context,
//        Manifest.permission.MANAGE_EXTERNAL_STORAGE
//      )
//              != PackageManager.PERMISSION_GRANTED)
//    } else {
//      (ActivityCompat.checkSelfPermission(
//        context,
//        Manifest.permission.READ_EXTERNAL_STORAGE
//      )
//              != PackageManager.PERMISSION_GRANTED)
//    }
//    return !((ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
//            != PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(
//      context,
//      Manifest.permission.READ_CALL_LOG
//    )
//            != PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(
//      context,
//      Manifest.permission.READ_PHONE_STATE
//    )
//            != PackageManager.PERMISSION_GRANTED) || !hasStorge)
  }


  private fun dial(intent: Intent): Boolean {
    val tm = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    if (tm.isInCall) {
      return false
    }
    context.startActivity(intent)
    return true
  }

//  private fun getSIMLength() {
//    val tm = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
//    if (XXPermissions.isGranted(context, Permission.READ_PHONE_STATE)) {
//      if (ActivityCompat.checkSelfPermission(
//          context,
//          Manifest.permission.READ_PHONE_STATE
//        ) != PackageManager.PERMISSION_GRANTED
//      ) {
//        val handles = tm.callCapablePhoneAccounts
//        if (handles != null && handles.size > 1 && simIndex >= 0) {
//          simIndex %= handles.size
//          intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handles[simIndex])
//        }
//      }
//
//    }
//
//  }

  private fun getDialIntent(phone: String, simIndex: Int): Intent? {
    var simIndex = simIndex
    val intent = Intent(Intent.ACTION_CALL)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val data: Uri = Uri.parse("tel:$phone")
    intent.data = data
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val tm = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val handles = tm.callCapablePhoneAccounts
        if (handles != null && handles.size > 1 && simIndex >= 0) {
          simIndex %= handles.size
          intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handles[simIndex])
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return intent
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity();
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding);
  }

  override fun onDetachedFromActivity() {
    activity = null
  }


}
