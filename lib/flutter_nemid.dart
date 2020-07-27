import 'dart:async';
import 'package:flutter/services.dart';

class FlutterNemid {
  static const MethodChannel _channel = const MethodChannel('flutter_nemid');

  static Future<bool> get startNemIDLogin async {
    final bool loggedIn = await _channel.invokeMethod('startNemIDLogin');
    return loggedIn;
  }
}
