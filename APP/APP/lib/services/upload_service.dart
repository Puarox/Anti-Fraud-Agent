import 'dart:io';

import 'package:dio/dio.dart';

import 'api_service.dart';

class UploadService {
  /// ✅ 通用上传入口（图片 / 音频 / 视频）
  static Future<String> uploadFile(File file) async {
    try {
      // 1️⃣ 获取签名URL
      final signData = await _getSignedUrl(file);

      final String signedUrl = signData['signedUrl'];
      final String finalUrl = signData['finalUrl'];

      // 2️⃣ 上传文件
      await _uploadToOss(file, signedUrl);
      print("debuglog:url" + finalUrl);
      return finalUrl;
    } catch (e) {
      throw Exception("上传失败: $e");
    }
  }

  /// 获取签名URL
  static Future<Map<String, dynamic>> _getSignedUrl(File file) async {
    String suffix = _getFileSuffix(file.path);

    final response = await ApiService.get(
      "oss/sign-url?suffix=$suffix",
    );

    return response.data;
  }

  /// 上传到 OSS
  static Future<void> _uploadToOss(File file, String signedUrl) async {
    final dio = Dio();

    await dio.put(
      signedUrl,
      data: file.openRead(),
    );
  }

  /// 获取文件后缀
  static String _getFileSuffix(String path) {
    return path.substring(path.lastIndexOf("."));
  }

  /// 自动识别 MIME 类型（关键升级）
  static String _getContentType(String path) {
    path = path.toLowerCase();

    // 图片
    if (path.endsWith(".png")) return "image/png";
    if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
    if (path.endsWith(".webp")) return "image/webp";

    // 音频
    if (path.endsWith(".mp3")) return "audio/mpeg";
    if (path.endsWith(".wav")) return "audio/wav";
    if (path.endsWith(".m4a")) return "audio/mp4";

    // 视频
    if (path.endsWith(".mp4")) return "video/mp4";
    if (path.endsWith(".mov")) return "video/quicktime";
    if (path.endsWith(".avi")) return "video/x-msvideo";

    return "application/octet-stream";
  }
}
