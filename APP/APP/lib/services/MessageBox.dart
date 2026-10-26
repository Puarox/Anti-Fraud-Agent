import 'package:flutter/material.dart';

class MessageBox with ChangeNotifier{
  List<String> _messages = [];

  List<String> get message => _messages;

  void getMessage(String message) {
    _messages.add(message);
    notifyListeners();
  }

  void removeMessage(String message) {
    _messages.remove(message);
    notifyListeners();
  }


}