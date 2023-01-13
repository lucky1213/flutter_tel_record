package com.example.flutter_tel_record

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import java.io.File
import java.nio.file.FileSystemException
import java.util.*


object  RecordFileUtil {
    private val record_dir_key = "record_dir_key"
    private var count = 0
    fun getAvailabeRecordDir(): String {
        val parent: File = Environment.getExternalStorageDirectory()
        return if (Build.BRAND == "realme" || Build.MODEL == "PFUM10") {
            File(parent, "Music/Recordings/Call Recordings").getPath()
        } else if (ManufacturerUtil.isXiaomi()) {
            "/sdcard/miui/sound_recorder"
        } else if (ManufacturerUtil.isHuawei()) {
            "/sdcard/Sounds/CallRecord"
        } else if (ManufacturerUtil.isMeizu()) {
            File(parent, "Recorder").path
        } else if (ManufacturerUtil.isOppo()) {
            File(parent, "Recordings").path
        } else if (ManufacturerUtil.isVivo()) {
            File(parent, "Record/Call").path
        } else {
            File(parent, "").path
        }
    }

    // 获取目录下所有文件(按时间排序)
    fun getFileSort(path: String): List<File> {
        val list: List<File> = getFiles(path, ArrayList<File>())
        try {
            if (list.isNotEmpty()) {
                Collections.sort(list, Comparator<File?> { file, newFile ->
                    if (file.lastModified() <= newFile.lastModified()) { //降序<;升序>
                        1
                    } else {
                        -1
                    }
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // 获取目录下所有文件
    private fun getFiles(realpath: String, files: MutableList<File>): List<File> {
        val realFile = File(realpath)
        if (realFile.isDirectory) {
            try {
                val subfiles: Array<File> = realFile.listFiles() as Array<File>
                if (subfiles.isNotEmpty()) {
                    for (file in subfiles) {
                        if (file.isDirectory) {
                            getFiles(file.absolutePath, files)
                        } else {
                            files.add(file)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return files
    }
    //寻找到录音文件 ---  并存储录音文件夹(以便下次拿到录音文件)

    //寻找到录音文件 ---  并存储录音文件夹(以便下次拿到录音文件)
    private fun saveRecordDir(dir: String, context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("share_file_name", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(record_dir_key, dir).apply()
    }

     fun getRecordCacheDir(context: Context): String? {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("share_file_name", Context.MODE_PRIVATE)
        return sharedPreferences.getString(record_dir_key, "")
    }


    private fun searchRecordDir(time: Long, context: Context): File? {
        val parent: File = Environment.getExternalStorageDirectory()
        val files: Array<File> = parent.listFiles() as Array<File>
        if (files.isNotEmpty()) {
            for (i in files.indices) {
                if (files[i].isDirectory) {
                    count = 0
                    val file: File? = searchRecordFile(time, files[i], count, context)
                    if (file != null) {
                        return file
                    }
                }
            }
        }
        return null
    }

    private fun searchRecordFile(time: Long, dir: File, count: Int, context: Context): File? {
        //计算调用次数 --- 层级不必太多
        if (dir.isDirectory && isNotRecordAppDir(dir) && count < 4) {
            val files: Array<File> = dir.listFiles() as Array<File>
            if (files.isNotEmpty()) {
                for (i in files.indices) {
                    val file: File = files[i]
                    //10秒之内生成的文件 默认为当前的录音文件
                    if (matchFileNameIsRecord(file.name) && file.lastModified() - time > -10 * 1000 && file.length() > 0 && file.isFile) {
                        file.parent?.let { saveRecordDir(it, context) }
                        return file
                    }
                    if (file.isDirectory) {
                        return searchRecordFile(time, file, count + 1, context)
                    }
                }
            }
        }
        return null
    }

     fun getSystemRecord(): String? {
        val parent: File = Environment.getExternalStorageDirectory()
        var child: File
        if (ManufacturerUtil.isHuawei()) {
            child = File(parent, "record")
            if (!child.exists()) {
                child = File(parent, "Sounds/CallRecord")
            }
        } else if (ManufacturerUtil.isXiaomi()) {
            child = File(parent, "MIUI/sound_recorder/call_rec")
        } else if (ManufacturerUtil.isMeizu()) {
            child = File(parent, "Recorder")
        } else if (ManufacturerUtil.isOppo()) {
            child = File(parent, "Recordings/Call Recordings")
            if (!child.exists()) {
                child = File(parent, "Recordings")
            }
        } else if (ManufacturerUtil.isVivo()) {
            child = File(parent, "Record/Call")
        } else {
            child = File(parent, "")
        }
        return if (!child.exists()) {
            null
        } else child.getAbsolutePath()
    }

    fun getExtensionName(filename: String): String {
        if (filename.isNotEmpty()) {
            val dot = filename.lastIndexOf('.')
            if (dot > -1 && dot < filename.length - 1) {
                return filename.substring(dot + 1)
            }
        }
        return filename
    }

    fun getFileNameNoEx(filename: String): String {
        if (filename.isNotEmpty()) {
            val dot = filename.lastIndexOf('.')
            if (dot > -1 && dot < filename.length) {
                return filename.substring(0, dot)
            }
        }
        return filename
    }

    //常用系统录音文件存放文件夹
    private fun getRecordFiles(): ArrayList<String> {
        val parentPath = Environment.getExternalStorageDirectory().absolutePath
        val list: ArrayList<String> = ArrayList()
        var file = File(parentPath, "record")
        if (file.exists()) {
            list.add(file.absolutePath)
        }
        file = File(parentPath, "Sounds/CallRecord")
        if (file.exists()) {
            list.add(file.absolutePath)
        }
        file = File(parentPath, "MIUI/sound_recorder/call_rec")
        if (file.exists()) {
            list.add(file.absolutePath)
        }
        file = File(parentPath, "Recorder")
        if (file.exists()) {
            list.add(file.absolutePath)
        }
        file = File(parentPath, "Recordings/Call Recordings")
        if (file.exists()) {
            list.add(file.absolutePath)
        }
        file = File(parentPath, "Recordings")
        if (file.exists()) {
            list.add(file.absolutePath)
        }
        file = File(parentPath, "Record/Call")
        if (file.exists()) {
            list.add(file.absolutePath)
        }
        file = File(parentPath, "Sounds")
        if (file.exists()) {
            list.add(file.absolutePath)
        }
        //oppp android-10 手机存储系统录音
        file = File(parentPath, "Music/Recordings/Call Recordings")
        if (file.exists()) {
            list.add(file.absolutePath)
        }
        file = File(parentPath, "PhoneRecord")
        if (file.exists()) {
            list.add(file.absolutePath)
        }

        // 或者其余机型系统录音文件夹 添加
        return list
    }

    fun getFileSleep(context: Context, current: Int, maxCount: Int = 9): File? {
        var file:File? = null
        if (current < maxCount) {
            file = getFile(context)
            if (file == null) {
                Thread.sleep(1000)
                return getFileSleep(context, current +1, maxCount)
            }
        }
        return file
    }

    //寻找文件
    fun getFile(context: Context): File? {
        try {
            val time: Long = Calendar.getInstance().timeInMillis
            var dir: File
            //使用记录下的文件夹下搜索
            var recordDir = getRecordCacheDir(context)
            // LogUtils.e("sp是否有缓存文件%s", "filePath" + recordDir + "当前时间" + DateUtil.stringToFormatString(time, DateUtil.DATE_FORMAT_CHINESE));
            if (TextUtils.isEmpty(recordDir)) {
                //使用固定系统下文件夹下搜索
                recordDir = getSystemRecord()
                if (!TextUtils.isEmpty(recordDir)) {
                    dir = File(recordDir!!)
                    val file: File? = getRecordFile(time, dir)
                    if (file != null) {
                        file.parent?.let { saveRecordDir(it, context) }
                        return file
                    }
                }
                //使用常用系统下文件夹下搜索
                val recordFiles = getRecordFiles()
                for (i in 0 until recordFiles.size) {
                    dir = File(recordFiles[i])
                    val file: File? = getRecordFile(time, dir)
                    if (file != null) {
                        file.parent?.let { saveRecordDir(it, context) }
                        return file
                    }
                }
            } else {
                //直接使用已存储文件夹下搜索
                val file: File? = getRecordFile(time, File(recordDir!!))
                if (file != null) {
                    file.parent?.let { saveRecordDir(it, context) }
                    return file
                }
            }
            //全局搜索录音文件夹并存储下来
            val file: File? = searchRecordDir(time, context)
            val time2: Long = Calendar.getInstance().timeInMillis
            LogUtils.e("录音文件夹所花时间%s",  "${(time2 - time)}ms" )
            return file
        } catch (e: Exception) {
            // LogUtils.e(e);
        }
        return null
    }

    fun getFilesByTimeRange(context: Context, startTime: Long, endTime: Long): List<File> {
        //使用记录下的文件夹下搜索
        var dir: File
        var files: List<File> = arrayListOf()
        var recordDir = getRecordCacheDir(context)
        // LogUtils.e("sp是否有缓存文件%s", "filePath" + recordDir + "当前时间" + DateUtil.stringToFormatString(time, DateUtil.DATE_FORMAT_CHINESE));
        if (TextUtils.isEmpty(recordDir)) {
            //使用固定系统下文件夹下搜索
            recordDir = getSystemRecord()
            if (!TextUtils.isEmpty(recordDir)) {
                dir = File(recordDir!!)
                files = getRecordFileByTimeRange(startTime, endTime, dir)
                if (files.isNotEmpty()) {
                    files.first().parent?.let { saveRecordDir(it, context) }
                    return files
                }
            }
            //使用常用系统下文件夹下搜索
            val recordFiles = getRecordFiles()
            for (i in 0 until recordFiles.size) {
                dir = File(recordFiles[i])
                files = getRecordFileByTimeRange(startTime, endTime, dir)
                if (files.isNotEmpty()) {
                    files.first().parent?.let { saveRecordDir(it, context) }
                    return files
                }
            }
        } else {
            //直接使用已存储文件夹下搜索
            files = getRecordFileByTimeRange(startTime, endTime, File(recordDir!!))
            if (files.isNotEmpty()) {
                files.first().parent?.let { saveRecordDir(it, context) }
                return files
            }
        }
        return files
    }

    private fun getRecordFileByTimeRange(startTime: Long, endTime: Long, dir: File): List<File> {
       var list: MutableList<File> = arrayListOf()
        if (dir.isDirectory && isNotRecordAppDir(dir)) {
            try {
                list = (dir.listFiles{ file ->
                    (file.lastModified() in startTime until endTime) && matchFileNameIsRecord(file.name) && file.length() > 0 && file.isFile
                } as Array<File>).toMutableList()
//                if (files.isNotEmpty()) {
//                    Collections.sort(files, Comparator<File?> { file, newFile ->
//                        if (file.lastModified() <= newFile.lastModified()) { //降序<;升序>
//                            1
//                        } else {
//                            -1
//                        }
//                    })
//                }
//                for (i in files.indices) {
//                    val file: File = files[i]
//
//                    if (file.lastModified() in startTime until endTime) {
//                        if (matchFileNameIsRecord(file.name) && file.length() > 0 && file.isFile) {
//                            list.add(file)
//                        }
//                    } else {
//                        break
//                    }
//                }
            } catch (e: Exception) {
                LogUtils.e(e.toString())
            }

        }
        return list
    }


    private fun getRecordFile(time: Long, dir: File): File? {
        if (dir.isDirectory && isNotRecordAppDir(dir)) {
            try {
                val files: Array<File> = dir.listFiles() as Array<File>
                if (files.isNotEmpty()) {
                    for (i in files.indices) {
                        val file: File = files[i]
                        //20秒之内生成的文件 默认为当前的录音文件(TODO 这里如果需要更准确可以判断是否是录音,录音时长校对)
                        if (matchFileNameIsRecord(file.name) && file.lastModified() - time > -20 * 1000 && file.length() > 0 && file.isFile) {
                            return file
                        }
                    }
                }
            } catch (e: Exception) {
                LogUtils.e(e.toString())
            }

        }
        return null
    }

    private fun isNotRecordAppDir(dir: File): Boolean {
        val name: String = dir.name
        if ("Android" == name) {
            return false
        } else if ("不是录音文件夹都可以写在这" == name) {
            return false
        }

        //加入一些会录音的app,会生成录音文件,防止使用其他录音文件而没有使用系统录音文件
        return true
    }

    private fun matchFileNameIsRecord(name: String): Boolean {
        //录音文件匹配规则 -- 可以自行添加其他格式录音匹配
        val namelowercase = name.lowercase(Locale.getDefault())
        try {
            if (namelowercase.endsWith(".mp3")) {
                return true
            } else if (namelowercase.endsWith(".wav")) {
                return true
            } else if (namelowercase.endsWith(".3gp")) {
                return true
            } else if (namelowercase.endsWith(".amr")) {
                return true
            } else if (namelowercase.endsWith(".3gpp")) {
                return true
            } else if (namelowercase.endsWith(".m4a")) {
                return true
            }
        } catch (e: Exception) {
            // LogUtils.e(e);
        }
        return false
    }
}