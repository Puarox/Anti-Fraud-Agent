import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:vibration/vibration.dart';

import '../services/api_service.dart';

class ResultPage extends StatefulWidget {
  final Map result;

  const ResultPage({super.key, required this.result});

  @override
  State<ResultPage> createState() => _ResultPageState();
}

class _ResultPageState extends State<ResultPage> {
  bool hasVibrated = false;
  Map<String, dynamic>? caseDetail;
  bool isLoadingCase = false;

  @override
  void initState() {
    super.initState();
    _handleHighRiskVibration();
    _fetchCaseDetail();
  }

  // ============================= 🚨 高风险自动震动
  Future<void> _handleHighRiskVibration() async {
    final level = widget.result["riskLevel"]?.toString().toUpperCase() ?? "";
    if (level == "HIGH" && !hasVibrated) {
      hasVibrated = true;
      try {
        if (await Vibration.hasVibrator() ?? false) {
          Vibration.vibrate(pattern: [0, 500, 300, 500]);
        }
      } catch (_) {}
    }
  }

  // ============================= 📡 获取案例详情
  Future<void> _fetchCaseDetail() async {
    final caseId = widget.result["caseId"];
    if (caseId == null || caseId.toString().isEmpty) return;
    setState(() => isLoadingCase = true);
    try {
      final res = await ApiService.get("fraud-case/case/$caseId");
      if (res.data["success"] == true) {
        setState(() => caseDetail = res.data["data"]);
      }
    } catch (e) {
      print("获取案例失败: $e");
    } finally {
      setState(() => isLoadingCase = false);
    }
  }

  Future<void> _callPhone(String phone) async {
    final uri = Uri(scheme: 'tel', path: phone);
    if (await canLaunchUrl(uri)) await launchUrl(uri);
  }

  Future<void> _sendSMS(String phone) async {
    final uri = Uri(
      scheme: 'sms',
      path: phone,
      queryParameters: {'body': '⚠️ 我可能遭遇诈骗，请尽快联系我！'},
    );
    await launchUrl(uri);
  }

  Future<void> _vibrateAlert() async {
    try {
      if (await Vibration.hasVibrator() ?? false)
        Vibration.vibrate(duration: 1000);
    } catch (_) {}
  }

  String _mapRiskLevel(String level) {
    switch (level.toUpperCase()) {
      case "HIGH":
        return "高";
      case "MEDIUM":
        return "中";
      case "LOW":
        return "低";
      default:
        return "未知";
    }
  }

  Color getRiskColor(String level) {
    switch (level) {
      case "高":
        return Colors.redAccent;
      case "中":
        return Colors.orangeAccent;
      case "低":
        return Colors.greenAccent;
      default:
        return Colors.grey;
    }
  }

  IconData getRiskIcon(bool isFraud) =>
      isFraud ? Icons.warning_amber : Icons.check_circle_outline;

  @override
  Widget build(BuildContext context) {
    final result = widget.result;
    final isFraud = result["isFraud"] == "YES" || result["isFraud"] == "是";
    final rawLevel = result["riskLevel"] ?? "";
    final riskLevel = _mapRiskLevel(rawLevel);

    return Scaffold(
      backgroundColor: const Color(0xFF0A1F44),
      appBar: AppBar(
        title: const Text("鉴别结果", style: TextStyle(color: Colors.white)),
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            _buildCard(
              child: Column(
                children: [
                  Icon(getRiskIcon(isFraud),
                      size: 60, color: getRiskColor(riskLevel)),
                  const SizedBox(height: 12),
                  Text(
                    isFraud ? "疑似诈骗" : "未发现明显风险",
                    style: const TextStyle(
                        color: Colors.white,
                        fontSize: 22,
                        fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 8),
                  Chip(
                    label: Text("风险等级：$riskLevel"),
                    backgroundColor: getRiskColor(riskLevel).withOpacity(0.1),
                    labelStyle: TextStyle(
                        color: getRiskColor(riskLevel),
                        fontWeight: FontWeight.bold),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            _buildSection("诈骗类型", result["fraudType"] ?? "未知", Icons.category),
            const SizedBox(height: 20),
            _buildSection("原因分析", result["reason"] ?? "无", Icons.search),
            const SizedBox(height: 20),
            _buildSection("安全建议", result["advice"] ?? "无", Icons.lightbulb),
            const SizedBox(height: 20),
            if (isLoadingCase) const CircularProgressIndicator(),
            if (caseDetail != null) _buildCaseSection(),
            const SizedBox(height: 20),
            if (isFraud)
              Column(
                children: [
                  if (rawLevel == "HIGH")
                    _buildActionButton(
                        "再次触发警报震动", Icons.vibration, Colors.red, _vibrateAlert),
                  const SizedBox(height: 10),
                  _buildActionButton("联系紧急联系人", Icons.call, Colors.blue,
                      () => _callPhone("13800138000")),
                  const SizedBox(height: 10),
                  _buildActionButton("发送求助短信", Icons.sms, Colors.orange,
                      () => _sendSMS("13800138000")),
                ],
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildCard({required Widget child}) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.06),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white24),
      ),
      child: child,
    );
  }

  Widget _buildSection(String title, String content, IconData icon) {
    if (content.toLowerCase() == "unknown" || content.isEmpty)
      return const SizedBox();

    return _buildCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: Colors.white, size: 28),
              const SizedBox(width: 8),
              Text(
                title,
                style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 18),
              ),
            ],
          ),
          const SizedBox(height: 6),
          Padding(
            padding: const EdgeInsets.only(left: 0), // 对齐到标题文字左边
            child: Text(
              content,
              style: const TextStyle(color: Colors.white70, fontSize: 16),
            ),
          ),
        ],
      ),
    );
  }

// ======================= 改动点 2️⃣: 下方按钮改浅色粗体 =====================
  Widget _buildActionButton(
      String text, IconData icon, Color color, VoidCallback onTap) {
    return ElevatedButton.icon(
      style: ElevatedButton.styleFrom(
        backgroundColor: color.withOpacity(0.85), // 🔹 稍微浅色
        minimumSize: const Size(double.infinity, 54), // 🔹 高度略增
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        textStyle: const TextStyle(
          fontWeight: FontWeight.bold, // 🔹 字体加粗
          fontSize: 17, // 🔹 字体放大
        ),
      ),
      onPressed: onTap,
      icon: Icon(icon, size: 24),
      label: Text(text),
    );
  }

// ======================= 改动点 3️⃣: 真实案例结构化显示 =====================
  Widget _buildCaseSection() {
    if (caseDetail == null) return const SizedBox();

    List<Widget> rows = [];

    void addRow(String label, dynamic value, {bool newline = false}) {
      if (value == null || value.toString().toLowerCase() == "unknown") return;
      rows.add(Padding(
        padding: const EdgeInsets.symmetric(vertical: 2),
        child: Text(
          "$label: ${value.toString()}",
          style: const TextStyle(color: Colors.white70, fontSize: 16),
        ),
      ));
    }

    addRow("📌 标题", caseDetail!["title"]);
    addRow("🎯 目标人群", caseDetail!["targetGroup"]);
    addRow("📱 场景", caseDetail!["scenario"]);
    addRow("💰 涉案金额", caseDetail!["amountInvolved"]);
    addRow("📍 地区", caseDetail!["region"]);
    addRow("📖 经过", caseDetail!["process"], newline: true);

    return _buildCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: const [
              Icon(Icons.gavel, color: Colors.white),
              SizedBox(width: 8),
              Text("真实案例参考",
                  style: TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 18)),
            ],
          ),
          const SizedBox(height: 10),
          ...rows,
          if (caseDetail!["sourceUrl"] != null &&
              caseDetail!["sourceUrl"].toString().isNotEmpty)
            TextButton(
              onPressed: () => launchUrl(Uri.parse(caseDetail!["sourceUrl"])),
              child: const Text(
                "查看来源",
                style: TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 16,
                    color: Colors.lightBlueAccent),
              ),
            ),
        ],
      ),
    );
  }
}
