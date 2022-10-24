import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:fingerprint/sdk/futronic_fingerprint_scanner.dart';

import 'futronic_fingerprint_scanner_platform_interface.dart';

/// An implementation of [FutronicFingerprintScannerPlatform] that uses method channels.
class MethodChannelFutronicFingerprintScanner extends FutronicFingerprintScannerPlatform {
  /// The method channel used to interact with the native platform.
  @override
  final methodChannel = const MethodChannel('futronic_fingerprint_scanner');

  @override
  Future<String?> getPlatformVersion() async {
    final result = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return result;
  }

  @override
  Future<bool?> isFrameChecked() async {
    final result = await methodChannel.invokeMethod<bool>('isFrameChecked');
    return result;
  }

  @override
  Future<bool?> isLFDChecked() async {
    final result = await methodChannel.invokeMethod<bool>('isLFDChecked');
    return result;
  }

  @override
  Future<bool?> isInvertChecked() async {
    final result = await methodChannel.invokeMethod<bool>('isInvertChecked');
    return result;
  }

  @override
  Future<bool?> isUSBChecked() async {
    final result = await methodChannel.invokeMethod<bool>('isUSBChecked');
    return result;
  }

  @override
  Future<bool?> isNFIQChecked() async {
    final result = await methodChannel.invokeMethod<bool>('isNFIQChecked');
    return result;
  }

  @override
  Future<bool?> scan() async {
    final result = await methodChannel.invokeMethod<bool>('scan');
    return result;
  }

  @override
  Future<bool?> stop() async {
    final result = await methodChannel.invokeMethod<bool>('stop');
    return result;
  }

  @override
  Future<void> setCheckFrame(bool value) async {
    await methodChannel.invokeMethod<void>('setCheckFrame', {'value': value});
  }

  @override
  Future<void> setCheckLFD(bool value) async {
    await methodChannel.invokeMethod<void>('setCheckLFD', {'value': value});
  }

  @override
  Future<void> setCheckInvert(bool value) async {
    await methodChannel.invokeMethod<void>('setCheckInvert', {'value': value});
  }

  @override
  Future<void> setCheckUSB(bool value) async {
    await methodChannel.invokeMethod<void>('setCheckUSB', {'value': value});
  }

  @override
  Future<void> setCheckNFIQ(bool value) async {
    await methodChannel.invokeMethod<void>('setCheckNFIQ', {'value': value});
  }

  @override
  Future<Size?> getScannerSize() async {
    final result = await methodChannel.invokeMethod<String>('getScannerSize');
    Map? map = jsonDecode(result);
    return Size(map?['width']?.toDouble() ?? 0.0, map?['height']?.toDouble() ?? 0.0);
  }

  @override
  Future<Uint8List?> getFingerprintImageBytes() async {
    final result = await methodChannel.invokeMethod<Uint8List>('getFingerprintImageBytes');
    return result;
  }

  @override
  Future<void> saveImage(FileFormat fileFormat, String filePath, String fileName) {
    return methodChannel.invokeMethod<void>('saveImage', {'fileFormat': fileFormat.value, 'fileName': fileName, 'filePath': filePath});
  }
}
