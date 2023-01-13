package com.example.flutter_tel_record

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log

object RecordUtil {
    private const val HUAWEI = "HUAWEI"
    private const val HONOR = "HONOR"

    private const val XIAOMI = "XIAOMI"

    private const val VIVO = "VIVO"

    private const val OPPO = "OPPO"

    private const val MEIZU = "MEIZU"

    fun checkAutoRecord(context: Context) : Boolean {
        try {
            var key = 0
            if (ManufacturerUtil.isHuawei()) {
                key = Settings.Secure.getInt(context.contentResolver, "enable_record_auto_key")
                if (ManufacturerUtil.isHarmonyOs()) {

                } else {

                }
            } else if (ManufacturerUtil.isOppo()) {
                key = Settings.Global.getInt(context.contentResolver, "oppo_all_call_audio_record")
            } else if (ManufacturerUtil.isVivo()) {
                key = Settings.Global.getInt(context.contentResolver, "call_record_state_global")

            } else if (ManufacturerUtil.isXiaomi()) {
                key = Settings.System.getInt(context.contentResolver, "button_auto_record_call")
            } else {
                throw Exception("10003")
            }
            Log.e("RecordUtil", "key:$key");
            return key != 0;
        } catch (e: Settings.SettingNotFoundException) {
            if (ManufacturerUtil.isVivo()) {
                throw Exception("10003")
            }
            return false
        } catch (e: Exception) {
            throw Exception("10003")
        }
    }

     fun openRecordSetting(context: Context) {
        var componentName: ComponentName? = null
         if (ManufacturerUtil.isHuawei()) {
             componentName = ComponentName("com.android.phone", "com.android.phone.MSimCallFeaturesSetting")
         } else if (ManufacturerUtil.isOppo()) {
             componentName = ComponentName("com.android.phone", "com.android.phone.OppoCallFeaturesSetting")
         } else if (ManufacturerUtil.isVivo()) {
             componentName = ComponentName("com.android.incallui", "com.android.incallui.record.CallRecordSetting")
         } else if (ManufacturerUtil.isXiaomi()) {
             componentName = ComponentName("com.android.phone", "com.android.phone.settings.CallRecordSetting")
         }
        if (componentName != null) {
            val intent = Intent()
            intent.component = componentName
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            throw Exception("10003")
        }
    }
}