import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:setest/pages/case_page.dart';
import 'package:setest/pages/diagnose_page.dart';
import 'package:setest/pages/history_page.dart';
import 'package:setest/pages/main_page.dart';
import 'package:setest/pages/profile_page.dart';

// ================== ⭐ Channel ==================
const MethodChannel channel = MethodChannel('share_channel');

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // ⭐ 注册监听（必须在 runApp 前）
  channel.setMethodCallHandler(_handleShare);

  // ⭐ 处理冷启动分享
  await _checkInitialShare();

  runApp(const MyApp());
}

// ================== ⭐ 处理分享 ==================
Future<void> _handleShare(MethodCall call) async {
  final path = call.arguments;

  switch (call.method) {
    case "onAudioShared":
      _navigateToDiagnose("audio", path);
      break;

    case "onVideoShared":
      _navigateToDiagnose("video", path);
      break;

    case "onImageShared":
      _navigateToDiagnose("image", path);
      break;

    default:
      print("未知类型: ${call.method}");
  }
}

// ================== ⭐ 冷启动补偿 ==================
Future<void> _checkInitialShare() async {
  final result = await channel.invokeMethod("getInitialSharedFile");

  if (result != null) {
    final type = result["type"];
    final path = result["path"];

    _navigateToDiagnose(type, path);
  }
}

// ================== ⭐ 全局导航Key ==================
final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

void _navigateToDiagnose(String type, String path) {
  print("跳转诊断页: $type -> $path");

  navigatorKey.currentState?.pushNamed(
    '/diagnose',
    arguments: {
      "type": type,
      "path": path,
    },
  );
}

// ================== App ==================
class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: "Anti Fraud",
      theme: ThemeData(primarySwatch: Colors.blue),

      navigatorKey: navigatorKey, // ⭐ 必须加

      debugShowCheckedModeBanner: false,

      initialRoute: '/main',

      routes: {
        '/main': (context) => MainPage(),
        '/diagnose': (context) => DiagnosePage(),
        '/history': (context) => HistoryPage(),
        '/profile': (context) => ProfilePage(),
        '/cases': (context) => const SwipePage(),
      },
    );
  }
}
