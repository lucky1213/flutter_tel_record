import 'package:flutter/services.dart';

enum FlutterTelRecordException {
  /// auto record is not opened.
  autoRecordPermissionDenied,

  /// permission is denied.
  permissionDenied,

  /// permission is denied.
  inCalling,

  /// The device is not supported.
  unsupportedDevice,

  /// sim Invalid
  simInvalid,

  /// unknown error.
  unknown
}

extension FlutterTelRecordExceptionExtension on FlutterTelRecordException {
  String get value {
    switch (this) {
      case FlutterTelRecordException.permissionDenied:
        return '10000';
      case FlutterTelRecordException.inCalling:
        return '10001';
      case FlutterTelRecordException.autoRecordPermissionDenied:
        return '10002';
      case FlutterTelRecordException.unsupportedDevice:
        return '10003';
      case FlutterTelRecordException.simInvalid:
        return '10004';
      default:
        return '';
    }
  }

  static FlutterTelRecordException fromValue(String value) {
    if (value == null) {
      return FlutterTelRecordException.unknown;
    }
    switch (value) {
      case '10000':
        return FlutterTelRecordException.permissionDenied;
      case '10001':
        return FlutterTelRecordException.inCalling;
      case '10002':
        return FlutterTelRecordException.autoRecordPermissionDenied;
      case '10003':
        return FlutterTelRecordException.unsupportedDevice;
      case '10004':
        return FlutterTelRecordException.simInvalid;
      default:
        return FlutterTelRecordException.unknown;
    }
  }
}

class FlutterTelRecordError extends PlatformException {
  final FlutterTelRecordException exception;

  FlutterTelRecordError(this.exception, String message, [dynamic details])
      : super(code: exception.value, message: message, details: details);

  @override
  String toString() {
    return 'FlutterTelRecordError{exception: $exception, message: $message, details: $details}';
  }
}
