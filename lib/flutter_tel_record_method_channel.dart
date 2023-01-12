import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:rxdart/rxdart.dart';
import 'package:uuid/uuid.dart';

import 'flutter_tel_record_platform_interface.dart';
import 'flutter_tel_record_error.dart';

/// An implementation of [FlutterTelRecordPlatform] that uses method channels.
class MethodChannelFlutterTelRecord extends FlutterTelRecordPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_tel_record');
  @visibleForTesting
  final eventChannel = const EventChannel('flutter_tel_record/listener');

  // MethodChannelFlutterTelRecord() {

  // }

  // Stream<Map<String, dynamic>> _stream;

  @override
  Future<String> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<bool> requestManngeExternalStorage() async {
    final result =
        await methodChannel.invokeMethod<bool>('requestManngeExternalStorage');
    return result;
  }

  @override
  Future<void> openManngeExternalStorageSetting() async {
    await methodChannel.invokeMethod<void>('openManngeExternalStorageSetting');
  }

  @override
  Future<bool> openRecordSetting() async {
    try {
      final result =
          await methodChannel.invokeMethod<bool>('openRecordSetting');
      return result;
    } catch (error) {
      if (error is PlatformException) {
        throw FlutterTelRecordError(
          FlutterTelRecordExceptionExtension.fromValue(error.code),
          error.message,
          error.details,
        );
      } else {
        rethrow;
      }
    }
  }

  @override
  Future<bool> requestRecordPermission() async {
    try {
      final result =
          await methodChannel.invokeMethod<bool>('requestRecordPermission');
      return result;
    } catch (error) {
      if (error is PlatformException) {
        throw FlutterTelRecordError(
          FlutterTelRecordExceptionExtension.fromValue(error.code),
          error.message,
          error.details,
        );
      } else {
        rethrow;
      }
    }
  }

  @override
  Future<List<String>> getRecordFiles({
    @required int startTime,
    int endTime,
  }) async {
    final result =
        await methodChannel.invokeMethod<List<dynamic>>('getRecordFiles', {
      'startTime': startTime,
      'endTime': endTime ?? DateTime.now().millisecondsSinceEpoch,
    });
    return result.map((e) {
      if (e is String) {
        return e;
      }
      return null;
    }).toList()
      ..remove(null);
    // 1673324672000
    // 1673020800000
    // 1673193600000
  }

  @override
  Future<String> getRecordCacheDir() {
    return methodChannel.invokeMethod<String>('getRecordCacheDir');
  }

  @override
  Stream<Map<String, dynamic>> dial({
    @required String phone,
    String uuid,
    String fileName,
    bool record = false,
    bool ignoreCheck = true,
  }) {
    String id = uuid;
    if (record && id == null) {
      id = Uuid().v4();
    }
    final stream = eventChannel
        .receiveBroadcastStream()
        .asBroadcastStream()
        .flatMap((value) {
      return Stream.value(Map<String, dynamic>.from(value));
    }).onErrorResume((error) {
      if (error is PlatformException) {
        return Stream.error(FlutterTelRecordError(
          FlutterTelRecordExceptionExtension.fromValue(error.code),
          error.message,
          error.details,
        ));
      } else {
        return Stream.error(error);
      }
    });
    // assert((record && uuid != null) || !record,
    //     'record is true, uuid must is not null');
    return methodChannel
        .invokeMethod<String>('dial', {
          'phone': phone,
          'uuid': id,
          'record': record,
          'ignoreCheck': ignoreCheck,
          'filename': fileName,
        })
        .asStream()
        .flatMap((value) {
          return stream;
        })
        .onErrorResume((
          error,
        ) {
          if (error is PlatformException) {
            return Stream.error(
              FlutterTelRecordError(
                FlutterTelRecordExceptionExtension.fromValue(error.code),
                error.message,
                error.details,
              ),
            );
          } else {
            return Stream.error(error);
          }
        });
  }
}
