import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class ChristianPickerImage {
  static const MethodChannel _channel =
      const MethodChannel('christian_picker_image');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<List<File>> pickImages({
    required int maxImages,
    enableGestures = true,
  }) async {
    
    final List<dynamic> images = await _channel.invokeMethod('pickImages',
    <String, dynamic>{
      "maxImages": maxImages,
      "enableGestures": enableGestures
    });

    return images.map((f) {
      return File(f["path"]);
    }).toList();
  }

}
