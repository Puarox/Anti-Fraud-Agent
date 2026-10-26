import 'package:flutter/material.dart';

import 'history_detail_page.dart';

class HistoryItem {
  final String userId;
  final String taskId;
  final String inputText;
  final String analysisResult;
  final String riskLevel;
  final DateTime createdAt;

  HistoryItem({
    required this.userId,
    required this.taskId,
    required this.inputText,
    required this.analysisResult,
    required this.riskLevel,
    required this.createdAt,
  });
}

class HistoryPage extends StatelessWidget {
  HistoryPage({super.key});

  // ✅ 模拟数据（后续替换为接口）
  final List<HistoryItem> list = [
    HistoryItem(
      userId: "u1",
      taskId: "t1",
      inputText: "快手主播直播片段，是否可信",
      analysisResult: "情感操控型直播诈骗，存在高风险",
      riskLevel: "HIGH",
      createdAt: DateTime.now().subtract(const Duration(minutes: 5)),
    ),
    HistoryItem(
      userId: "u1",
      taskId: "t2",
      inputText: "中奖短信让我点链接",
      analysisResult: "典型钓鱼诈骗",
      riskLevel: "MEDIUM",
      createdAt: DateTime.now().subtract(const Duration(hours: 2)),
    ),
  ];

  /// 风险颜色映射（深色模式下使用带有 Accent 的颜色会更亮眼）
  Color getRiskColor(String level) {
    switch (level) {
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

  /// 风险文本
  String getRiskText(String level) {
    switch (level) {
      case "HIGH":
        return "高风险";
      case "MEDIUM":
        return "中风险";
      case "LOW":
        return "低风险";
      default:
        return "未知";
    }
  }

  /// 时间格式化
  String formatTime(DateTime time) {
    return "${time.year}-${time.month.toString().padLeft(2, '0')}-${time.day.toString().padLeft(2, '0')} "
        "${time.hour.toString().padLeft(2, '0')}:${time.minute.toString().padLeft(2, '0')}";
  }

  /// ================= 背景 =================
  Widget _buildBackground() {
    return Container(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          colors: [
            Color(0xFF0A1F44),
            Color(0xFF0D2A5C),
          ],
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      // Scaffold 背景设为透明，以便露出底层 Stack 的渐变背景
      backgroundColor: Colors.transparent,
      body: Stack(
        children: [
          _buildBackground(),
          Column(
            children: [
              // 自定义深色模式 AppBar
              AppBar(
                backgroundColor: Colors.transparent,
                elevation: 0,
                iconTheme: const IconThemeData(color: Colors.white), // 返回键变白
                title: const Text(
                  "历史记录",
                  style: TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                centerTitle: true,
              ),
              Expanded(
                child: ListView.builder(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  itemCount: list.length,
                  itemBuilder: (context, i) {
                    final item = list[i];

                    return GestureDetector(
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (_) => HistoryDetailPage(),
                          ),
                        );
                      },
                      child: Container(
                        margin: const EdgeInsets.only(bottom: 16),
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: const Color(0xFF1A2E5A), // 匹配之前的深蓝卡片色
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(
                            color: Colors.white.withOpacity(0.1), // 微弱边框增强质感
                            width: 1,
                          ),
                          boxShadow: [
                            BoxShadow(
                              color: Colors.black.withOpacity(0.3),
                              blurRadius: 10,
                              offset: const Offset(0, 5),
                            ),
                          ],
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            /// 第一行：风险标签 + 时间
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Container(
                                  padding: const EdgeInsets.symmetric(
                                      horizontal: 10, vertical: 4),
                                  decoration: BoxDecoration(
                                    color: getRiskColor(item.riskLevel)
                                        .withOpacity(0.15), // 背景稍微加深一点点
                                    borderRadius: BorderRadius.circular(20),
                                  ),
                                  child: Text(
                                    getRiskText(item.riskLevel),
                                    style: TextStyle(
                                      color: getRiskColor(item.riskLevel),
                                      fontWeight: FontWeight.bold,
                                      fontSize: 12,
                                    ),
                                  ),
                                ),
                                Text(
                                  formatTime(item.createdAt),
                                  style: const TextStyle(
                                    color: Colors.white, // 深色背景下的次要文本
                                    fontSize: 12,
                                  ),
                                )
                              ],
                            ),

                            const SizedBox(height: 12),

                            /// 第二行：用户输入（主要信息，白字展示）
                            Text(
                              item.inputText,
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 16,
                                fontWeight: FontWeight.w600,
                                height: 1.4,
                              ),
                            ),

                            const SizedBox(height: 8),

                            /// 第三行：AI分析摘要（次要信息，浅色展示）
                            Text(
                              item.analysisResult,
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(
                                color: Colors.white70,
                                fontSize: 14,
                                height: 1.4,
                              ),
                            ),

                            const SizedBox(height: 12),

                            /// 第四行：操作提示（右箭头）
                            Row(
                              mainAxisAlignment: MainAxisAlignment.end,
                              children: const [
                                Icon(
                                  Icons.arrow_forward_ios,
                                  size: 14,
                                  color: Colors.white,
                                ),
                              ],
                            )
                          ],
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
