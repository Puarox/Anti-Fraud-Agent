import 'package:flutter/material.dart';
import 'package:setest/pages/personal_setting_page.dart';

import 'history_page.dart';
import 'settings_page.dart';

class ProfilePage extends StatelessWidget {
  const ProfilePage({super.key});

  void go(BuildContext context, Widget page) {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => page),
    );
  }

  /// ================= 背景渐变 =================
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
      backgroundColor: Colors.transparent, // 设为透明以显示背景渐变
      body: Stack(
        children: [
          _buildBackground(),
          SafeArea(
            child: Column(
              children: [
                /// 1. 自定义头部（替代原生的 UserAccountsDrawerHeader）
                _buildHeader(),

                const SizedBox(height: 20),

                /// 2. 菜单列表
                Expanded(
                  child: ListView(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    children: [
                      _buildMenuTile(
                        icon: Icons.history_rounded,
                        title: "历史记录",
                        subtitle: "查看过往风险分析报告",
                        onTap: () => go(context, HistoryPage()),
                      ),
                      _buildMenuTile(
                        icon: Icons.person_outline_rounded,
                        title: "个人信息",
                        subtitle: "管理您的基本账户资料",
                        onTap: () => go(context, const PersonalSettingPage()),
                      ),
                      _buildMenuTile(
                        icon: Icons.settings_suggest_rounded,
                        title: "设置",
                        subtitle: "应用偏好与安全设置",
                        onTap: () => go(context, SettingsPage()),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  /// 用户信息头部
  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 40, horizontal: 24),
      child: Row(
        children: [
          // 头像
          Container(
            width: 70,
            height: 70,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              border: Border.all(color: Colors.white24, width: 2),
              image: const DecorationImage(
                image: NetworkImage(
                    "https://img.k2r2.com/uploads/frombd/1/253/273441762/3333656782.jpg"),
                fit: BoxFit.cover,
              ),
            ),
          ),
          const SizedBox(width: 20),
          // 文本信息
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: const [
              Text(
                "王**先生",
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 22,
                  fontWeight: FontWeight.bold,
                ),
              ),
              SizedBox(height: 4),
              Text(
                "57岁，国企职工",
                style: TextStyle(
                  color: Colors.white54,
                  fontSize: 14,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  /// 通用的菜单项组件
  Widget _buildMenuTile({
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      decoration: BoxDecoration(
        color: const Color(0xFF1A2E5A), // 与卡片颜色一致
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white.withOpacity(0.08)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.2),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
        leading: Icon(icon, color: Colors.blueAccent, size: 28),
        title: Text(
          title,
          style: const TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.w600,
          ),
        ),
        subtitle: Text(
          subtitle,
          style: const TextStyle(color: Colors.white38, fontSize: 12),
        ),
        trailing: const Icon(
          Icons.arrow_forward_ios,
          color: Colors.white24,
          size: 16,
        ),
        onTap: onTap,
      ),
    );
  }
}
