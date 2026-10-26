import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:video_player/video_player.dart';

class VideoPreview extends StatefulWidget {
  const VideoPreview({super.key});

  @override
  State<VideoPreview> createState() => _VideoPreviewState();
}

class _VideoPreviewState extends State<VideoPreview> {
  late VideoPlayerController _controller;

  @override
  void initState() {
    super.initState();

    _controller = VideoPlayerController.network(
      "https://paroxbucket.oss-cn-beijing.aliyuncs.com/uploads/video/%E7%A6%8F%E5%88%A9.mp4",
    )..initialize().then((_) {
        setState(() {});
      });

    _controller.setLooping(true); // 可选：循环播放
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _togglePlay() {
    setState(() {
      _controller.value.isPlaying ? _controller.pause() : _controller.play();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        GestureDetector(
          onTap: _togglePlay,
          child: _controller.value.isInitialized
              ? AspectRatio(
                  aspectRatio: _controller.value.aspectRatio,
                  child: Stack(
                    alignment: Alignment.center,
                    children: [
                      VideoPlayer(_controller),

                      // 播放按钮覆盖层
                      if (!_controller.value.isPlaying)
                        Container(
                          color: Colors.black26,
                          child: const Icon(
                            Icons.play_circle_outline,
                            color: Colors.white,
                            size: 50,
                          ),
                        ),
                    ],
                  ),
                )
              : const SizedBox(
                  height: 120,
                  child: Center(
                    child: CircularProgressIndicator(),
                  ),
                ),
        ),
        const SizedBox(height: 4),
        const Text(
          "直播视频.mp4",
          style: TextStyle(color: Colors.white54, fontSize: 12),
        ),
      ],
    );
  }
}

class HistoryDetailPage extends StatefulWidget {
  const HistoryDetailPage({super.key});

  @override
  State<HistoryDetailPage> createState() => _HistoryDetailPageState();
}

class _HistoryDetailPageState extends State<HistoryDetailPage> {
  bool hasVibrated = false;

  final Map<String, dynamic> mockUserInput = {
    "createdAt": "2026-03-23 11:41:30",
    "text": "看这个主播真可怜，说在帮老人建房子，说当地都不管只有他在帮忙，呼吁大家刷礼物打赏资助，我看好多人都捐了，我也想捐点。",
    "hasImage": true,
  };

  final Map<String, dynamic> mockAnalysisResult = {
    "isFraud": "YES",
    "riskLevel": "HIGH",
    "fraudType": "情感操控型直播诈骗",
    "reason": "虚构助老情节，夸大权威介入，无实证却诱导打赏，符合多地通报的高发骗局特征。",
    "advice": "勿因感动打赏；核实医院、住房等信息；提醒老人警惕‘温情’营销；立即举报该账号。",
  };

  final Map<String, dynamic> mockCaseDetail = {
    "title": "团风警方破获多起帮助信息网络犯罪活动案",
    "targetGroup": "使用抖音、网络刷单、贷款、公检法诈骗的普通民众",
    "scenario": "通过抖音平台下载虚假APP、网络刷单、冒充客服解决征信问题、冒充公检法等多种诈骗场景",
    "region": "湖北团风县",
    "amountInvolved": "未知",
    "sourceUrl":
        "https://weixin.sogou.com/weixin?p=01030402&query=%E5%8F%8D%E8%AF%88&type=2&ie=utf8",
  };

  @override
  void initState() {
    super.initState();
    _handleHighRiskVibration();
  }

  // ============================= 🚨 高风险自动震动
  Future<void> _handleHighRiskVibration() async {
    final level =
        mockAnalysisResult["riskLevel"]?.toString().toUpperCase() ?? "";
    if (level == "HIGH" && !hasVibrated) {
      hasVibrated = true;
      HapticFeedback.heavyImpact(); // 使用系统自带强力震动替代第三方库
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
    if (await canLaunchUrl(uri)) await launchUrl(uri);
  }

  Color getRiskColor(String level) {
    switch (level.toUpperCase()) {
      case "HIGH":
        return Colors.redAccent;
      case "MEDIUM":
        return Colors.orangeAccent;
      case "LOW":
        return Colors.greenAccent;
      default:
        return Colors.grey;
    }
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

  @override
  Widget build(BuildContext context) {
    final isFraud = mockAnalysisResult["isFraud"] == "YES";
    final rawLevel = mockAnalysisResult["riskLevel"] ?? "";
    final riskLevel = _mapRiskLevel(rawLevel);
    final riskColor = getRiskColor(rawLevel);

    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Stack(
        children: [
          _buildBackground(),
          Column(
            children: [
              AppBar(
                title: const Text("历史记录详情",
                    style: TextStyle(
                        color: Colors.white, fontWeight: FontWeight.bold)),
                backgroundColor: Colors.transparent,
                elevation: 0,
                centerTitle: true,
                iconTheme: const IconThemeData(color: Colors.white),
              ),
              Expanded(
                child: SingleChildScrollView(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    children: [
                      /// 1. 用户输入信息区块
                      _buildUserInputSection(),
                      const SizedBox(height: 20),

                      /// 2. 模型鉴别结果核心卡片
                      _buildResultHeader(isFraud, riskLevel, riskColor),
                      const SizedBox(height: 16),

                      /// 3. 分析详情
                      _buildSection("诈骗类型", mockAnalysisResult["fraudType"],
                          Icons.category),
                      const SizedBox(height: 16),
                      _buildSection(
                          "原因分析", mockAnalysisResult["reason"], Icons.search),
                      const SizedBox(height: 16),
                      _buildSection("安全建议", mockAnalysisResult["advice"],
                          Icons.lightbulb),
                      const SizedBox(height: 16),

                      /// 4. 真实案例参考
                      _buildCaseSection(),
                      const SizedBox(height: 24),

                      /// 5. 紧急措施按钮
                      if (isFraud && rawLevel == "HIGH") ...[
                        _buildActionButton("再次触发警报震动", Icons.vibration,
                            Colors.red, () => HapticFeedback.heavyImpact()),
                        const SizedBox(height: 12),
                        _buildActionButton("联系紧急联系人", Icons.call, Colors.blue,
                            () => _callPhone("110")),
                        const SizedBox(height: 12),
                        _buildActionButton("发送求助短信", Icons.sms, Colors.orange,
                            () => _sendSMS("110")),
                        const SizedBox(height: 30),
                      ]
                    ],
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  // ================= 背景 =================
  Widget _buildBackground() {
    return Container(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          colors: [Color(0xFF0A1F44), Color(0xFF0D2A5C)],
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
        ),
      ),
    );
  }

  // ================= 卡片容器基类 =================
  Widget _buildCard({required Widget child}) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFF1A2E5A),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white.withOpacity(0.1)),
      ),
      child: child,
    );
  }

  // ================= 1. 用户输入信息区块 =================
  Widget _buildUserInputSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 4, bottom: 8),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text("提交内容",
                  style: TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                      fontWeight: FontWeight.bold)),
              Text(mockUserInput["createdAt"],
                  style: const TextStyle(color: Colors.white54, fontSize: 12)),
            ],
          ),
        ),
        _buildCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                mockUserInput["text"],
                style: const TextStyle(
                    color: Colors.white, fontSize: 15, height: 1.4),
              ),
              if (mockUserInput["hasImage"]) ...[
                const SizedBox(height: 12),
                Container(
                  width: double.infinity,
                  height: 220, // 👈 改大（建议 200~260）
                  decoration: BoxDecoration(
                    color: Colors.black26,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: Colors.white24),
                  ),
                  child: const ClipRRect(
                    borderRadius: BorderRadius.all(Radius.circular(12)),
                    child: VideoPreview(),
                  ),
                ),
              ]
            ],
          ),
        ),
      ],
    );
  }

  // ================= 2. 鉴别结果头部 =================
  Widget _buildResultHeader(bool isFraud, String riskLevel, Color riskColor) {
    return _buildCard(
      child: Column(
        children: [
          Icon(isFraud ? Icons.warning_amber : Icons.check_circle_outline,
              size: 60, color: riskColor),
          const SizedBox(height: 12),
          Text(
            isFraud ? "疑似诈骗" : "未发现明显风险",
            style: const TextStyle(
                color: Colors.white, fontSize: 22, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
              color: riskColor.withOpacity(0.15),
              borderRadius: BorderRadius.circular(20),
            ),
            child: Text(
              "风险等级：$riskLevel风险",
              style: TextStyle(color: riskColor, fontWeight: FontWeight.bold),
            ),
          ),
        ],
      ),
    );
  }

  // ================= 3. 通用信息小节 =================
  Widget _buildSection(String title, String content, IconData icon) {
    return _buildCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: Colors.blueAccent, size: 24),
              const SizedBox(width: 8),
              Text(
                title,
                style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 16),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(
            content,
            style: const TextStyle(
                color: Colors.white70, fontSize: 15, height: 1.5),
          ),
        ],
      ),
    );
  }

  // ================= 4. 真实案例模块 =================
  Widget _buildCaseSection() {
    List<Widget> rows = [];

    void addRow(String label, dynamic value) {
      if (value == null || value.toString().isEmpty || value.toString() == "未知")
        return;
      rows.add(Padding(
        padding: const EdgeInsets.only(bottom: 8.0),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 85,
              child: Text(label,
                  style: const TextStyle(color: Colors.white54, fontSize: 14)),
            ),
            Expanded(
              child: Text(value.toString(),
                  style: const TextStyle(
                      color: Colors.white, fontSize: 14, height: 1.4)),
            ),
          ],
        ),
      ));
    }

    addRow("📌 标题", mockCaseDetail["title"]);
    addRow("🎯 目标人群", mockCaseDetail["targetGroup"]);
    addRow("📱 场景", mockCaseDetail["scenario"]);
    addRow("📍 地区", mockCaseDetail["region"]);

    return _buildCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: const [
                  Icon(Icons.gavel, color: Colors.blueAccent, size: 24),
                  SizedBox(width: 8),
                  Text("真实案例参考",
                      style: TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                          fontSize: 16)),
                ],
              ),
              if (mockCaseDetail["sourceUrl"] != null)
                InkWell(
                  onTap: () =>
                      launchUrl(Uri.parse(mockCaseDetail["sourceUrl"])),
                  child: const Text("查看来源",
                      style: TextStyle(
                          color: Colors.lightBlueAccent,
                          fontSize: 13,
                          fontWeight: FontWeight.bold)),
                )
            ],
          ),
          const SizedBox(height: 12),
          ...rows,
        ],
      ),
    );
  }

  // ================= 5. 底部操作按钮 =================
  Widget _buildActionButton(
      String text, IconData icon, Color color, VoidCallback onTap) {
    return ElevatedButton.icon(
      style: ElevatedButton.styleFrom(
        backgroundColor: color.withOpacity(0.85),
        minimumSize: const Size(double.infinity, 54),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        elevation: 0,
        textStyle: const TextStyle(fontWeight: FontWeight.bold, fontSize: 17),
      ),
      onPressed: onTap,
      icon: Icon(icon, size: 22, color: Colors.white),
      label: Text(text, style: const TextStyle(color: Colors.white)),
    );
  }
}
