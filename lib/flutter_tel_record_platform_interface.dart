import 'package:flutter/foundation.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_tel_record_method_channel.dart';
import 'sim_info_model.dart';

export 'sim_info_model.dart';

abstract class FlutterTelRecordPlatform extends PlatformInterface {
  /// Constructs a FlutterTelRecordPlatform.
  FlutterTelRecordPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterTelRecordPlatform _instance = MethodChannelFlutterTelRecord();

  /// The default instance of [FlutterTelRecordPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterTelRecord].
  static FlutterTelRecordPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterTelRecordPlatform] when
  /// they register themselves.
  static set instance(FlutterTelRecordPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool> requestManngeExternalStorage() {
    throw UnimplementedError(
        'requestManngeExternalStorage() has not been implemented.');
  }

  Future<void> openManngeExternalStorageSetting() {
    throw UnimplementedError(
        'openManngeExternalStorageSetting() has not been implemented.');
  }

  Future<bool> openRecordSetting() {
    throw UnimplementedError('openRecordSetting() has not been implemented.');
  }

  Future<bool> requestRecordPermission() {
    throw UnimplementedError(
        'requestRecordPermission() has not been implemented.');
  }

  Stream<Map<String, dynamic>> dial({
    @required String phone,
    int simIndex = 0,
    String uuid,
    bool record = false,
    bool ignoreCheck = true,
    String fileName,
  }) {
    throw UnimplementedError('dial() has not been implemented.');
  }

  Future<List<String>> getRecordFiles({
    @required int startTime,
    int endTime,
  }) {
    throw UnimplementedError('getRecordFiles() has not been implemented.');
  }

  Future<String> getRecordCacheDir() {
    throw UnimplementedError('getRecordCacheDir() has not been implemented.');
  }

  Future<int> getAudioDuration(String path) {
    throw UnimplementedError('getAudioDuration() has not been implemented.');
  }

  Future<List<SIMInfoModel>> getSIMInfos() {
    throw UnimplementedError('getAudioDuration() has not been implemented.');
  }
}
