class TokenStorage {
  static String? token;

  static Future<void> saveToken(String t) async {
    token = t;
  }

  static Future<String?> getToken() async {
    return token;
  }
}
