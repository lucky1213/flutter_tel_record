// You have generated a new plugin project without specifying the `--platforms`
// flag. A plugin project with no platform support was generated. To add a
// platform, run `flutter create -t plugin --platforms <platforms> .` under the
// same directory. You can also find a detailed instruction on how to add
// platforms in the `pubspec.yaml` at
// https://flutter.dev/docs/development/packages-and-plugins/developing-packages#plugin-platforms.

import 'package:flutter/foundation.dart';

import 'flutter_tel_record_platform_interface.dart';

export 'flutter_tel_record_error.dart';

class FlutterTelRecord {
  static Future<String> getPlatformVersion() {
    return FlutterTelRecordPlatform.instance.getPlatformVersion();
  }

  static Future<bool> requestManngeExternalStorage() {
    return FlutterTelRecordPlatform.instance.requestManngeExternalStorage();
  }

  static Future<void> openManngeExternalStorageSetting() {
    return FlutterTelRecordPlatform.instance.openManngeExternalStorageSetting();
  }

  static Future<bool> openRecordSetting() {
    return FlutterTelRecordPlatform.instance.openRecordSetting();
  }

  static Future<bool> requestRecordPermission() {
    return FlutterTelRecordPlatform.instance
        .requestRecordPermission()
        .then((value) {
      return value;
    });
  }

  static Stream<Map<String, dynamic>> dial({
    @required String phone,
    String uuid,
    bool record = false,
    bool ignoreCheck = true,
    String fileName,
  }) {
    return FlutterTelRecordPlatform.instance.dial(
      phone: phone,
      uuid: uuid,
      record: record,
      ignoreCheck: ignoreCheck,
      fileName: fileName,
    );
  }

  static Future<List<String>> getRecordFiles({
    @required int startTime,
    int endTime,
  }) {
    return FlutterTelRecordPlatform.instance.getRecordFiles(
      startTime: startTime,
      endTime: endTime,
    );
  }

  static Future<String> getRecordCacheDir() {
    return FlutterTelRecordPlatform.instance.getRecordCacheDir();
  }

  static Future<int> getAudioDuration(String path) {
    return FlutterTelRecordPlatform.instance.getAudioDuration(path);
  }

  static Future<List<SIMInfoModel>> getSIMInfos() {
    return FlutterTelRecordPlatform.instance.getSIMInfos();
  }
}
