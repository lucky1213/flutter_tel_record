package com.example.flutter_tel_record

import android.os.Build
import android.text.TextUtils
import java.util.*


object  ManufacturerUtil {
    private val ROM_HUAWEI = arrayOf("huawei", "honor")
    private val ROM_XIAOMI = arrayOf("xiaomi")
    private val ROM_VIVO = arrayOf("vivo")
    private val ROM_OPPO = arrayOf("oppo")
    private val ROM_MEIZU = arrayOf("meizu")
    private val UNKNOWN = "unknown"

    private var bean: RomInfo? = null

    /**
     * Return whether the rom is made by huawei.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    fun isHuawei(): Boolean {
        return ROM_HUAWEI[0] == getRomInfo().name
    }

    /**
     * Return whether the rom is made by vivo.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    fun isVivo(): Boolean {
        return ROM_VIVO[0] == getRomInfo().name
    }

    /**
     * Return whether the rom is made by xiaomi.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    fun isXiaomi(): Boolean {
        return ROM_XIAOMI[0] == getRomInfo().name
    }

    /**
     * Return whether the rom is made by oppo.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    fun isOppo(): Boolean {
        return ROM_OPPO[0] == getRomInfo().name
    }

    /**
     * Return whether the rom is made by meizu.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    fun isMeizu(): Boolean {
        return ROM_MEIZU[0] == getRomInfo().name
    }

    /**
     * 是否为鸿蒙系统
     *
     * @return true为鸿蒙系统
     */
     fun  isHarmonyOs(): Boolean {
        return try {
            val buildExClass = Class.forName("com.huawei.system.BuildEx")
            val osBrand = buildExClass.getMethod("getOsBrand").invoke(buildExClass)
            "Harmony".lowercase() == osBrand?.toString()?.lowercase()
        } catch ( err: Throwable) {
            false
        }
    }

        /**
     * Return the rom's information.
     *
     * @return the rom's information
     */
    private fun getRomInfo(): RomInfo {
        if (bean != null) return bean!!
        bean = RomInfo()
        val brand = getBrand()
        val manufacturer = getManufacturer()
        if (isRightRom(brand, manufacturer, *ROM_HUAWEI)) {
            bean!!.name = ROM_HUAWEI[0]
            return bean!!
        }
        if (isRightRom(brand, manufacturer, *ROM_VIVO)) {
            bean!!.name = ROM_VIVO[0]
            return bean!!
        }
        if (isRightRom(brand, manufacturer, *ROM_XIAOMI)) {
            bean!!.name = ROM_XIAOMI[0]
            return bean!!
        }
        if (isRightRom(brand, manufacturer, *ROM_OPPO)) {
            bean!!.name = ROM_OPPO[0]
            return bean!!
        }
        if (isRightRom(brand, manufacturer, *ROM_MEIZU)) {
            bean!!.name = ROM_MEIZU[0]
        } else {
            bean!!.name = manufacturer
        }
        return bean!!
    }

    private fun isRightRom(brand: String, manufacturer: String, vararg names: String): Boolean {
        for (name in names) {
            if (brand.contains(name) || manufacturer.contains(name)) {
                return true
            }
        }
        return false
    }

    private fun getManufacturer(): String {
        try {
            val manufacturer = Build.MANUFACTURER
            if (!TextUtils.isEmpty(manufacturer)) {
                return manufacturer.lowercase(Locale.getDefault())
            }
        } catch (ignore: Throwable) { /**/
        }
        return UNKNOWN
    }

    private fun getBrand(): String {
        try {
            val brand = Build.BRAND
            if (!TextUtils.isEmpty(brand)) {
                return brand.lowercase(Locale.getDefault())
            }
        } catch (ignore: Throwable) { /**/
        }
        return UNKNOWN
    }





    class RomInfo {
        var name: String? = null

        override fun toString(): String {
            return "RomInfo{name=$name}"
        }
    }
}