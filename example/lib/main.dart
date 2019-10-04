import 'dart:io';
import 'package:flutter/material.dart';

import 'dart:async';

import 'package:flutter/services.dart';
import 'package:christian_picker_image/christian_picker_image.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: "Test",
      home: TestPage(),
    );
  }
}

class TestPage extends StatefulWidget {
  @override
  TestPageState createState() => TestPageState();
}

class TestPageState extends State<TestPage> {
  String _platformVersion = 'Unknown';

  static const MethodChannel _channel = const MethodChannel('christian_picker_image');

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await ChristianPickerImage.platformVersion;
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

  void takeImage(BuildContext context) async {
    print("takeImage");
    List<File> images  = await ChristianPickerImage.pickImages(maxImages: 5);
    print(images);
    Navigator.of(context).pop();
    print("dissmiss11");
  }

  Future _pickImage(BuildContext context) async {

    //takeImage(context);
    var isPopup = false;

    showDialog<void>(
      context: context,
      barrierDismissible: true,
      builder: (BuildContext context) {
        if (!isPopup) {
          isPopup = true;
          takeImage(context);
        }
        return Center();
    });

  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Running on: $_platformVersion\n'),
        ),
        floatingActionButton: Column(
          mainAxisAlignment: MainAxisAlignment.end,
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.only(top: 16.0),
              child: FloatingActionButton(
                onPressed: () {
                  _pickImage(context);
                },
                tooltip: 'Take a Photo',
                child: const Icon(Icons.photo_library),
              ),
            ),
          ],
        )
      );
  }
}
