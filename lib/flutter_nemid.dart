import 'dart:async';
import 'package:flutter/services.dart';

class FlutterNemid {
  static const MethodChannel _channel = const MethodChannel('flutter_nemid');

  static Future<void> setupBackendEndpoints({String signingEndpoint, String validationEndpoint}) async {
    try {
      await _channel.invokeMethod('setupBackendEndpoints', {
        "signingEndpoint": signingEndpoint,
        "validationEndpoint" : validationEndpoint
      });
    } catch (e){
      throw e;
    }
  }

  static Future<String> get startNemIDLogin async {
    final String response = await _channel.invokeMethod('startNemIDLogin');
    return response;
  }
}
