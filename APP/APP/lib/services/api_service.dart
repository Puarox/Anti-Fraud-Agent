import 'package:dio/dio.dart';

import '../utils/token_storage.dart';

class ApiService {
  static final Dio dio = Dio(
    BaseOptions(
      baseUrl: "http://192.168.5.2:8090/",
      connectTimeout: const Duration(seconds: 10),
    ),
  );

  static Future<Response> get(String path) async {
    String? token = await TokenStorage.getToken();

    return dio.get(
      path,
      options: Options(
        headers: {"Authorization": "Bearer $token"},
      ),
    );
  }

  static Future<Response> post(String path, dynamic data) async {
    String? token = await TokenStorage.getToken();

    return dio.post(
      path,
      data: data,
      options: Options(
        headers: {"Authorization": "Bearer $token"},
      ),
    );
  }
}
