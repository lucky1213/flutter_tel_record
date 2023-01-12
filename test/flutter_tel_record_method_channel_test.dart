import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_tel_record/flutter_tel_record_method_channel.dart';

void main() {
  MethodChannelFlutterTelRecord platform = MethodChannelFlutterTelRecord();
  const MethodChannel channel = MethodChannel('flutter_tel_record');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
