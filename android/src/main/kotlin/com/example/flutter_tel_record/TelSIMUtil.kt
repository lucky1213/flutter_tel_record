package com.example.flutter_tel_record

import android.annotation.SuppressLint
import android.content.Context
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import com.blankj.utilcode.util.LogUtils


object TelSIMUtil {
    private val TAG = TelSIMUtil::class.java.simpleName
    private const val SIM_STATE = "getSimState"
    private const val SIM_OPERATOR = "getSimOperator"
    private const val SIM_NETWORK_TYPE = "getNetworkType"
    private const val SIM_IMEI = "getImei"
    private const val SIM_LINE_NUMBER = "getLine1Number"

    private fun getSimPhonenumber(context: Context, slotIdx: Int): String? {
        return getSimByMethod(
            context,
            SIM_LINE_NUMBER,
            getSubidBySlotId(context, slotIdx)
        ) as String?
    }

    private fun getSimImei(context: Context, slotIdx: Int): String? {
        return getSimByMethod(context, SIM_IMEI, slotIdx) as String?
    }

    private fun getSimNetworkType(context: Context, slotIdx: Int): Int {
        return getSimByMethod(
            context,
            SIM_NETWORK_TYPE,
            getSubidBySlotId(context, slotIdx)
        ) as Int
    }

    private fun getSimNetworkName(context: Context, slotIdx: Int): String {
        return getNetworkName(getSimNetworkType(context, slotIdx))
    }

    private fun getSimOperator(context: Context, slotIdx: Int): Int? {
        return getSimByMethod(
            context,
            SIM_OPERATOR,
            getSubidBySlotId(context, slotIdx)
        ) as Int?
    }

    private fun getSimOperatorName(context: Context, slotIdx: Int): String {
        return getOperatorName(getSimOperator(context, slotIdx))
    }

    /**
     * @param context
     * @param slotIdx:0(sim1),1(sim2)
     * @return
     */
    private fun getSimStateBySlotIdx(context: Context, slotIdx: Int): Boolean {
        var isReady = false
        val getSimState = getSimByMethod(context, SIM_STATE, slotIdx)
        if (getSimState != null) {
            val simState = getSimState.toString().toInt()
            if (simState != TelephonyManager.SIM_STATE_UNKNOWN && simState != TelephonyManager.SIM_STATE_ABSENT) {
                isReady = true
            }
        }
        return isReady
    }

    @SuppressLint("MissingPermission")
    private fun getSimByMethod(context: Context, method: String, param: Int): Any? {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        try {
            val telephonyClass = TelephonyManager::class.java
            val getSimState = telephonyClass.getMethod(method, Int::class.java)
            getSimState.isAccessible = true
            val obPhone = getSimState.invoke(telephony, param)
            LogUtils.e(method, obPhone, param)
            if (obPhone != null) {
                return obPhone
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @SuppressLint("MissingPermission")
    fun getSIMInfos(context: Context): List<SIMInfo> {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val count = telephony.phoneCount
        val list = Array(count) {
            if (!getSimStateBySlotIdx(context, it)) {
                null
            } else {
                // phoneNumber = getSimPhonenumber(context, it), networkName = null, operateName = getSimOperatorName(context, it)
                SIMInfo(null, null, null)
            }
        }.filterNotNull()

        return list.toList()
    }

    private fun getNetworkName(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            else -> "UNKNOWN"
        }
    }

    private fun getOperatorName(networkType: Int?): String {
        return when (networkType) {
            46000, 46002 -> "中国移动"
            46001 -> "中国联通"
            46003 -> "中国电信"
            else -> "UNKNOWN"
        }
    }

    /**
     * to
     *
     * @param context
     * @param slotId
     * @return
     */
    private fun getSubidBySlotId(context: Context, slotId: Int): Int {
        val subscriptionManager = context.getSystemService(
            Context.TELEPHONY_SUBSCRIPTION_SERVICE
        ) as SubscriptionManager
        
        try {
            val telephonyClass = SubscriptionManager::class.java
            val getSimState = telephonyClass.getMethod("getSubId", Int::class.java)
            val obPhone = getSimState.invoke(subscriptionManager, slotId)
            if (obPhone != null) {
                Log.d(TAG, "slotId:" + slotId + ";" + (obPhone as IntArray)[0])
                return obPhone[0]
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

    class SIMInfo(phoneNumber: String?, networkName: String?, operateName: String?) {
        var phoneNumber // 电话号码
                : String? = null
        var networkName // 几G网络
                : String? = null
        var operateName // 卡生厂商
                : String? = null

        init {
            this.phoneNumber = phoneNumber
            this.networkName = networkName
            this.operateName = operateName
        }
    }
}