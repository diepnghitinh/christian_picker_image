import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:christian_picker_image/christian_picker_image.dart';

void main() {
  const MethodChannel channel = MethodChannel('christian_picker_image');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });

  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await ChristianPickerImage.platformVersion, '42');
  });

}
