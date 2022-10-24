import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:fingerprint/sdk/futronic_fingerprint_scanner.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'futronic_fingerprint_scanner_method_channel.dart';

abstract class FutronicFingerprintScannerPlatform extends PlatformInterface {
  /// Constructs a FutronicFingerprintScannerPlatform.
  FutronicFingerprintScannerPlatform() : super(token: _token);

  static final Object _token = Object();

  static FutronicFingerprintScannerPlatform _instance = MethodChannelFutronicFingerprintScanner();

  get methodChannel => _instance.methodChannel;

  /// The default instance of [FutronicFingerprintScannerPlatform] to use.
  ///
  /// Defaults to [MethodChannelFutronicFingerprintScanner].
  static FutronicFingerprintScannerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FutronicFingerprintScannerPlatform] when
  /// they register themselves.
  static set instance(FutronicFingerprintScannerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool?> isFrameChecked() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool?> isLFDChecked() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool?> isInvertChecked() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool?> isUSBChecked() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool?> isNFIQChecked() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool?> scan() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<bool?> stop() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> setCheckFrame(bool value) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> setCheckLFD(bool value) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> setCheckInvert(bool value) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> setCheckUSB(bool value) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> setCheckNFIQ(bool value) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<Size?> getScannerSize() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<Uint8List?> getFingerprintImageBytes() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> saveImage(FileFormat fileFormat, String filePath, String fileName) {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
