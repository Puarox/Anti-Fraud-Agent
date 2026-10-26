import 'dart:async';

import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:setest/pages/security_page.dart';
import 'package:url_launcher/url_launcher.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final PageController _controller = PageController();
  int currentIndex = 0;

  final List<Map<String, String>> banners = [
    {
      "img":
          "https://paroxbucket.oss-cn-beijing.aliyuncs.com/uploads/image/55213f3b1b624cafa0fb87572e10fdcb%7Etplv-tt-origin-web_gif.jpg",
      "text": "揭秘网络打赏骗局",
      "url": ""
    },
    {
      "img":
          "https://paroxbucket.oss-cn-beijing.aliyuncs.com/uploads/image/屏幕截图%202026-04-04%20143920.png",
      "text": "直播诈骗案例",
      "url": "https://www.peopleapp.com/column/30047004174-500005842027"
    },
    {
      "img":
          "https://paroxbucket.oss-cn-beijing.aliyuncs.com/uploads/image/屏幕截图%202026-04-04%20143439.png",
      "text": "荐股骗局",
      "url": "https://m.thepaper.cn/newsDetail_forward_32773472"
    },
  ];

  @override
  void initState() {
    super.initState();

    Timer.periodic(const Duration(seconds: 4), (timer) {
      if (_controller.hasClients) {
        currentIndex = (currentIndex + 1) % banners.length;
        _controller.animateToPage(
          currentIndex,
          duration: const Duration(milliseconds: 400),
          curve: Curves.easeInOut,
        );
      }
    });
  }

  // =========================
  // 打开网页（关键方法）
  // =========================
  Future<void> _openUrl(String url) async {
    final uri = Uri.parse(url);

    if (!await launchUrl(uri)) {
      throw Exception('无法打开链接');
    }
  }

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
      backgroundColor: Colors.transparent,
      appBar: AppBar(
        title: const Text("首页", style: TextStyle(color: Colors.white)),
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: true,
      ),
      body: Stack(
        children: [
          _buildBackground(),
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  _buildBanner(),
                  const SizedBox(height: 20),
                  _buildQuickActions(context),
                  const SizedBox(height: 20),
                  _buildStatsPanel(),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  // =========================
  // 轮播图（已加点击）
  // =========================
  Widget _buildBanner() {
    return SizedBox(
      height: 160,
      child: PageView.builder(
        controller: _controller,
        itemCount: banners.length,
        onPageChanged: (i) => setState(() => currentIndex = i),
        itemBuilder: (_, i) {
          final item = banners[i];

          return GestureDetector(
            onTap: () async {
              try {
                await _openUrl(item["url"]!);
              } catch (e) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text("链接打开失败")),
                );
              }
            },
            child: ClipRRect(
              borderRadius: BorderRadius.circular(16),
              child: Stack(
                children: [
                  Image.network(item["img"]!,
                      width: double.infinity, fit: BoxFit.cover),
                  Container(color: Colors.black.withOpacity(0.3)),
                  Positioned(
                    left: 16,
                    bottom: 16,
                    child: Text(
                      item["text"]!,
                      style: const TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  )
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  // =========================
  // 快捷入口
  // =========================
  Widget _buildQuickActions(BuildContext context) {
    return GridView.count(
      crossAxisCount: 2,
      shrinkWrap: true,
      crossAxisSpacing: 12,
      mainAxisSpacing: 12,
      childAspectRatio: 1.4,
      physics: const NeverScrollableScrollPhysics(),
      children: [
        _actionCard(Icons.search, "智能检测",
            () => Navigator.pushNamed(context, "/diagnose")),
        _actionCard(Icons.history, "历史记录",
            () => Navigator.pushNamed(context, "/history")),
        _actionCard(Icons.warning, "诈骗案例",
            () => Navigator.pushNamed(context, "/cases")),
        _actionCard(Icons.settings, "个人设置",
            () => Navigator.pushNamed(context, "/profile")),
      ],
    );
  }

  Widget _actionCard(IconData icon, String title, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white.withOpacity(0.08),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: Colors.white24),
        ),
        padding: const EdgeInsets.all(14),
        child: Row(
          children: [
            Icon(icon, color: Colors.white, size: 26),
            const SizedBox(width: 10),
            Text(
              title,
              style: const TextStyle(
                  color: Colors.white, fontWeight: FontWeight.bold),
            )
          ],
        ),
      ),
    );
  }

  // =========================
  // 数据统计面板
  // =========================
  Widget _buildStatsPanel() {
    return GestureDetector(
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => SafetyReportPage(),
          ),
        );
      },
      child: Container(
        height: 140,
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.white.withOpacity(0.06),
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: Colors.white24),
        ),
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
                          color: Colors.grey,
                          radius: 10,
                          showTitle: false,
                        ),
                      ],
                    ),
                  ),
                  const Text(
                    "高风险",
                    style: TextStyle(
                      color: Colors.redAccent,
                      fontWeight: FontWeight.bold,
                    ),
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
                  Row(
                    children: [
                      Icon(Icons.analytics, color: Colors.blue, size: 18),
                      SizedBox(width: 6),
                      Text("检测次数：12", style: TextStyle(color: Colors.white)),
                    ],
                  ),
                  Row(
                    children: [
                      Icon(Icons.warning, color: Colors.orange, size: 18),
                      SizedBox(width: 6),
                      Text("中风险：3", style: TextStyle(color: Colors.white)),
                    ],
                  ),
                  Row(
                    children: [
                      Icon(Icons.error, color: Colors.red, size: 18),
                      SizedBox(width: 6),
                      Text("高风险：2", style: TextStyle(color: Colors.white)),
                    ],
                  ),
                ],
              ),
            )
          ],
        ),
      ),
    );
  }
}
