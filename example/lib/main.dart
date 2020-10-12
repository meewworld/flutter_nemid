import 'dart:convert';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_nemid/flutter_nemid.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  Map<String, dynamic> _response;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    await FlutterNemid.setupBackendEndpoints(signingEndpoint: "https://api.phoneloan.dk/v1/nemid/parameters/", validationEndpoint: "https://api.phoneloan.dk/v1/nemid/response/");
    String response;

    try {
      response = await FlutterNemid.startNemIDLogin;
    } on PlatformException {
      response = null;
    }

    if (!mounted) return;

    setState(() {
      _response = jsonDecode(response);
    });
  }

  String getResult(){
    String result = "";

    if(_response.containsKey("status")){
      result += "Status: ${_response['status']}\n";
    }

    if(_response.containsKey("result")){
      result += "Result: ${_response['result']}\n";
    }

    return result;
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('NemID login example app'),
        ),
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.max,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              RaisedButton(
                onPressed: initPlatformState,
                child: Text("NemID Login"),
              ),
              Text(
                getResult(),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
