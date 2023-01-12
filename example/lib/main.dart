// @dart=2.9
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_tel_record/flutter_tel_record.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
    // FlutterTelRecord.stream.listen((event) {
    //   print(event);
    // });
  }

  @override
  void dispose() {
    super.dispose();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await FlutterTelRecord.getPlatformVersion() ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            Center(
              child: Text('Running on: $_platformVersion\n'),
            ),
            Center(
              child: TextButton(
                onPressed: () async {
                  FlutterTelRecord.requestManngeExternalStorage()
                      .then((value1) {
                    if (value1) {
                      [Permission.storage, Permission.phone]
                          .request()
                          .then((value) {
                        if (value[Permission.storage] !=
                                PermissionStatus.granted ||
                            value[Permission.phone] !=
                                PermissionStatus.granted) {
                          return;
                        }
                        DateTime now = DateTime.now();
                        DateTime start = DateTime(now.year, now.month, now.day)
                            .subtract(const Duration(days: 3));
                        DateTime end = DateTime(now.year, now.month, now.day)
                            .add(const Duration(microseconds: 86399999999));
                        FlutterTelRecord.getRecordFiles(
                          startTime: start.millisecondsSinceEpoch,
                          endTime: end.millisecondsSinceEpoch,
                        ).then((value) {
                          print(value);
                        });
                        // FlutterTelRecord.dial(
                        //   phone: '13280237838',
                        //   record: true,
                        //   ignoreCheck: false,
                        // ).listen((event) {
                        //   print(event);
                        // }, onError: (err, stack) {
                        //   print(err);
                        // });
                      });
                    }
                  });
                },
                child: const Text('拨号13280237838'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
