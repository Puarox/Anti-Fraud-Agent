import 'package:flutter/material.dart';

import '../services/api_service.dart';

class RegisterPage extends StatefulWidget {
  @override
  State<RegisterPage> createState() => _RegisterState();
}

class _RegisterState extends State<RegisterPage> {
  final user = TextEditingController();
  final pass = TextEditingController();
  final pass2 = TextEditingController();

  bool loading = false;

  void register() async {
    if (pass.text != pass2.text) {
      showMsg("两次密码不一致");
      return;
    }

    setState(() {
      loading = true;
    });

    try {
      var res = await ApiService.post(
        "/register",
        {"username": user.text, "password": pass.text},
      );

      showMsg("注册成功");

      Navigator.pop(context);
    } catch (e) {
      showMsg("注册失败");
    }

    setState(() {
      loading = false;
    });
  }

  void showMsg(String msg) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(msg)),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("注册账号"),
      ),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            TextField(
              controller: user,
              decoration: const InputDecoration(
                labelText: "用户名",
              ),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: pass,
              obscureText: true,
              decoration: const InputDecoration(
                labelText: "密码",
              ),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: pass2,
              obscureText: true,
              decoration: const InputDecoration(
                labelText: "确认密码",
              ),
            ),
            const SizedBox(height: 30),
            loading
                ? const CircularProgressIndicator()
                : ElevatedButton(
                    onPressed: register,
                    child: const Text("注册"),
                  ),
          ],
        ),
      ),
    );
  }
}
