import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class HistoryItem {
  final String inputText;
  final String analysisResult;
  final int riskLevel; // 0低 1中 2高
  final String createdAt;

  HistoryItem({
    required this.inputText,
    required this.analysisResult,
    required this.riskLevel,
    required this.createdAt,
  });
}

class SafetyReportPage extends StatelessWidget {
  SafetyReportPage({super.key});

  /// ===== 模拟数据（与你统计面板一致）=====
  final List<HistoryItem> list = [
    HistoryItem(
      inputText: "客服让我提供验证码进行退款",
      analysisResult: "典型验证码诈骗，试图接管账户",
      riskLevel: 2,
      createdAt: "2026-04-06 14:22",
    ),
    HistoryItem(
      inputText: "点击链接领取补贴",
      analysisResult: "钓鱼链接，诱导填写信息",
      riskLevel: 2,
      createdAt: "2026-04-05 10:12",
    ),
  ];

  @override
  Widget build(BuildContext context) {
    HapticFeedback.vibrate(); // 高风险震动

    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Stack(
        children: [
          _buildBackground(),
          Column(
            children: [
              /// ===== AppBar =====
              AppBar(
                backgroundColor: Colors.transparent,
                elevation: 0,
                iconTheme: const IconThemeData(color: Colors.white),
                title: const Text(
                  "安全报告",
                  style: TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                centerTitle: true,
              ),

              Expanded(
                child: ListView(
                  padding: const EdgeInsets.all(16),
                  children: [
                    _buildStatsPanel(),

                    const SizedBox(height: 16),

                    /// ===== 1. 高风险历史 =====
                    _buildHistorySection(context),

                    const SizedBox(height: 16),

                    /// ===== 2. AI报告 =====
                    _buildAiReport(),

                    const SizedBox(height: 16),

                    /// ===== 3. 建议 =====
                    _buildAdvice(),

                    const SizedBox(height: 16),

                    /// ===== 4. 措施 =====
                    _buildActions(),
                  ],
                ),
              )
            ],
          ),
        ],
      ),
    );
  }

  /// ================= 背景 =================
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

  /// ================= 统计面板 =================
  Widget _buildStatsPanel() {
    return Container(
      height: 140,
      padding: const EdgeInsets.all(12),
      decoration: _card(),
      child: Row(
        children: [
          SizedBox(
            width: 100,
            child: Stack(
              alignment: Alignment.center,
              children: [
                PieChart(
                  PieChartData(
                    sectionsSpace: 2,
                    centerSpaceRadius: 28,
                    sections: [
                      PieChartSectionData(
                        value: 70,
                        color: Colors.redAccent,
                        radius: 10,
                        showTitle: false,
                      ),
                      PieChartSectionData(
                        value: 30,
                        color: Colors.grey.withOpacity(0.3),
                        radius: 10,
                        showTitle: false,
                      ),
                    ],
                  ),
                ),
                const Text(
                  "高风险",
                  style: TextStyle(
                      color: Colors.redAccent, fontWeight: FontWeight.bold),
                )
              ],
            ),
          ),
          const SizedBox(width: 10),
          const Expanded(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(children: [
                  Icon(Icons.analytics, color: Colors.blue, size: 18),
                  SizedBox(width: 6),
                  Text("检测次数：12", style: TextStyle(color: Colors.white)),
                ]),
                Row(children: [
                  Icon(Icons.warning, color: Colors.orange, size: 18),
                  SizedBox(width: 6),
                  Text("中风险：3", style: TextStyle(color: Colors.white)),
                ]),
                Row(children: [
                  Icon(Icons.error, color: Colors.red, size: 18),
                  SizedBox(width: 6),
                  Text("高风险：2", style: TextStyle(color: Colors.white)),
                ]),
              ],
            ),
          )
        ],
      ),
    );
  }

  /// ================= 1. 历史 =================
  Widget _buildHistorySection(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _title("近期高风险记录"),
        const SizedBox(height: 10),
        ...list.map((item) {
          return Container(
            margin: const EdgeInsets.only(bottom: 12),
            padding: const EdgeInsets.all(16),
            decoration: _card(),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    _riskTag(item.riskLevel),
                    Text(item.createdAt,
                        style: const TextStyle(color: Colors.white70)),
                  ],
                ),
                const SizedBox(height: 10),
                Text(item.inputText,
                    style: const TextStyle(
                        color: Colors.white, fontWeight: FontWeight.bold)),
                const SizedBox(height: 6),
                Text(item.analysisResult,
                    style: const TextStyle(color: Colors.white70)),
              ],
            ),
          );
        }).toList(),
      ],
    );
  }

  /// ================= 2. AI报告 =================
  Widget _buildAiReport() {
    return _sectionCard(
      "AI风险分析报告",
      "基于近期检测记录，系统判断您可能正遭遇“验证码诈骗”和“钓鱼链接诈骗”。\n\n"
          "诈骗者通常通过伪装官方身份，引导您点击链接或提供验证码，从而控制账户或窃取资金。",
    );
  }

  /// ================= 3. 建议 =================
  Widget _buildAdvice() {
    return _sectionCard(
      "安全建议",
      "1. 不向任何人提供验证码\n"
          "2. 不点击陌生链接\n"
          "3. 官方操作请通过正规App完成\n"
          "4. 遇到异常立即终止操作",
    );
  }

  /// ================= 4. 措施 =================
  Widget _buildActions() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _title("紧急措施"),
        const SizedBox(height: 10),
        ElevatedButton(onPressed: () {}, child: const Text("联系紧急联系人")),
        ElevatedButton(onPressed: () {}, child: const Text("发送求助短信")),
        ElevatedButton(
            onPressed: () => HapticFeedback.vibrate(),
            child: const Text("再次震动警报")),
      ],
    );
  }

  /// ================= UI组件 =================
  BoxDecoration _card() {
    return BoxDecoration(
      color: const Color(0xFF1A2E5A),
      borderRadius: BorderRadius.circular(16),
      border: Border.all(color: Colors.white.withOpacity(0.1)),
    );
  }

  Widget _title(String text) {
    return Text(text,
        style: const TextStyle(
            color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold));
  }

  Widget _sectionCard(String title, String content) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: _card(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _title(title),
          const SizedBox(height: 8),
          Text(content, style: const TextStyle(color: Colors.white70)),
        ],
      ),
    );
  }

  Widget _riskTag(int level) {
    Color color =
        level == 2 ? Colors.red : (level == 1 ? Colors.orange : Colors.green);

    String text = level == 2 ? "高风险" : (level == 1 ? "中风险" : "低风险");

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: color.withOpacity(0.15),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(text,
          style: TextStyle(color: color, fontWeight: FontWeight.bold)),
    );
  }
}
