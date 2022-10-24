import 'package:flutter/services.dart';

import 'futronic_fingerprint_scanner_platform_interface.dart';

enum FileFormat {
  bitmap('BITMAP', '.bmp'),
  wsq('WSQ', '.wsq');

  final String value;
  final String extension;
  const FileFormat(this.value, this.extension);
}

class FutronicFingerprintScanner {
  final methodChannel = const MethodChannel('futronic_fingerprint_scanner');
  Function(String)? onMessage;
  FutronicFingerprintScanner() {
    methodChannel.setMethodCallHandler(
      (call) {
        switch (call.method) {
          case 'message':
            onMessage?.call(call.arguments);
            //print(call.arguments);
            break;
        }
        return Future.value();
      },
    );
  }
  final FutronicFingerprintScannerPlatform methods = FutronicFingerprintScannerPlatform.instance;
}
