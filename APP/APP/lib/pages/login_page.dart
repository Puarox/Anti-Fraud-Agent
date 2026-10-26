import 'package:flutter/material.dart';
import 'package:setest/pages/register_page.dart';

import '../services/auth_service.dart';
import '../utils/token_storage.dart';
import 'main_page.dart';

class LoginPage extends StatefulWidget {
  @override
  State<LoginPage> createState() => _LoginState();
}

class _LoginState extends State<LoginPage> {
  final user = TextEditingController();
  final pass = TextEditingController();

  void login() async {
    String? token = await AuthService.login(user.text, pass.text);

    if (token != null) {
      await TokenStorage.saveToken(token);

      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (_) => MainPage()),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("登录")),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            TextField(
                controller: user,
                decoration: const InputDecoration(labelText: "用户名")),
            TextField(
                controller: pass,
                decoration: const InputDecoration(labelText: "密码")),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: login,
              child: const Text("登录"),
            ),
            TextButton(
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (_) => RegisterPage()),
                );
              },
              child: const Text("注册"),
            )
          ],
        ),
      ),
    );
  }
}
