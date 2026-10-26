import 'package:flutter/material.dart';

import 'login_page.dart';

class SettingsPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("设置")),
      body: ListView(
        children: [
          ListTile(
            leading: const Icon(Icons.info),
            title: const Text("关于"),
          ),
          ListTile(
            leading: const Icon(Icons.logout),
            title: const Text("退出登录"),
            onTap: () {
              Navigator.pushReplacement(
                context,
                MaterialPageRoute(builder: (_) => LoginPage()),
              );
            },
          )
        ],
      ),
    );
  }
}
