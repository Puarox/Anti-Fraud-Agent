import 'api_service.dart';

class AuthService {
  static Future<String?> login(String user, String pass) async {
    var res = await ApiService.post(
        "/user/login", {"username": user, "password": pass});
    print(res);
    print(res.data["token"]);
    return res.data["data"]["token"];
  }
}
