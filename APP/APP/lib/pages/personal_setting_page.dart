import 'package:flutter/material.dart';

class PersonalSettingPage extends StatefulWidget {
  const PersonalSettingPage({super.key});

  @override
  State<PersonalSettingPage> createState() => _PersonalSettingPageState();
}

class _PersonalSettingPageState extends State<PersonalSettingPage> {
  final _formKey = GlobalKey<FormState>();

  /// 控制器
  final TextEditingController emergencyContactController =
      TextEditingController();

  String? ageGroup;
  String? jobType;
  String? usageScene;
  bool needFamilyProtection = false;

  /// 🌙 样式常量（与全局主题对齐）
  final Color cardColor = const Color(0xFF1A2E5A);
  final Color inputFillColor = const Color(0xFF0A1F44).withOpacity(0.5);
  final Color textColor = Colors.white;
  final Color subTextColor = Colors.white54;

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
      backgroundColor: Colors.transparent, // 设为透明以露出背景
      appBar: AppBar(
        title: const Text("个人信息设置", style: TextStyle(color: Colors.white)),
        centerTitle: true,
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: const IconThemeData(color: Colors.white),
      ),
      body: Stack(
        children: [
          _buildBackground(),
          SafeArea(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Form(
                key: _formKey,
                child: Column(
                  children: [
                    /// ================= 基础画像 =================
                    _buildCard(
                      title: "基础用户画像",
                      icon: Icons.assignment_ind_rounded,
                      children: [
                        _buildDropdown(
                          label: "年龄段",
                          value: ageGroup,
                          items: ["18岁以下", "18-30岁", "30-50岁", "50岁以上"],
                          onChanged: (v) => setState(() => ageGroup = v),
                        ),
                        _buildDropdown(
                          label: "职业类型",
                          value: jobType,
                          items: ["学生", "职场人士", "自由职业", "退休人员"],
                          onChanged: (v) => setState(() => jobType = v),
                        ),
                        _buildDropdown(
                          label: "常见使用场景",
                          value: usageScene,
                          items: ["社交聊天", "网购交易", "投资理财", "游戏娱乐"],
                          onChanged: (v) => setState(() => usageScene = v),
                        ),
                      ],
                    ),

                    /// ================= 安全策略 =================
                    _buildCard(
                      title: "安全策略设置",
                      icon: Icons.security_rounded,
                      children: [
                        SwitchListTile(
                          contentPadding: EdgeInsets.zero,
                          value: needFamilyProtection,
                          onChanged: (v) =>
                              setState(() => needFamilyProtection = v),
                          title: Text(
                            "开启家人守护",
                            style: TextStyle(color: textColor, fontSize: 15),
                          ),
                          subtitle: Text(
                            "检测到高风险行为时，将同步通知紧急联系人",
                            style: TextStyle(color: subTextColor, fontSize: 12),
                          ),
                          activeColor: Colors.blueAccent,
                          activeTrackColor: Colors.blueAccent.withOpacity(0.3),
                        ),
                      ],
                    ),

                    /// ================= 紧急联系人 =================
                    _buildCard(
                      title: "紧急联系人",
                      icon: Icons.contact_phone_rounded,
                      children: [
                        const SizedBox(height: 8),
                        TextFormField(
                          controller: emergencyContactController,
                          keyboardType: TextInputType.phone,
                          style: TextStyle(color: textColor),
                          decoration: InputDecoration(
                            hintText: "请输入紧急联系人电话",
                            hintStyle:
                                TextStyle(color: Colors.white24, fontSize: 14),
                            filled: true,
                            fillColor: inputFillColor,
                            prefixIcon: const Icon(Icons.phone,
                                color: Colors.blueAccent, size: 20),
                            contentPadding: const EdgeInsets.symmetric(
                                horizontal: 16, vertical: 16),
                            enabledBorder: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(12),
                              borderSide: BorderSide(
                                  color: Colors.white.withOpacity(0.1)),
                            ),
                            focusedBorder: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(12),
                              borderSide: const BorderSide(
                                  color: Colors.blueAccent, width: 1.5),
                            ),
                            errorStyle:
                                const TextStyle(color: Colors.redAccent),
                          ),
                          validator: (value) {
                            if (value == null || value.isEmpty)
                              return "请填写紧急联系人电话";
                            if (value.length < 6) return "电话格式不正确";
                            return null;
                          },
                        ),
                      ],
                    ),

                    const SizedBox(height: 30),

                    /// ================= 保存按钮 =================
                    Container(
                      width: double.infinity,
                      height: 54,
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(12),
                        gradient: const LinearGradient(
                          colors: [Color(0xFF3B82F6), Color(0xFF2563EB)],
                        ),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.blueAccent.withOpacity(0.3),
                            blurRadius: 10,
                            offset: const Offset(0, 4),
                          ),
                        ],
                      ),
                      child: ElevatedButton(
                        onPressed: _save,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.transparent,
                          shadowColor: Colors.transparent,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(12),
                          ),
                        ),
                        child: const Text(
                          "保存设置",
                          style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                              color: Colors.white),
                        ),
                      ),
                    ),
                    const SizedBox(height: 40),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  /// ================= 卡片组件 =================
  Widget _buildCard(
      {required String title,
      required IconData icon,
      required List<Widget> children}) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: cardColor,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: Colors.white.withOpacity(0.08)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.2),
            blurRadius: 12,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: Colors.blueAccent, size: 22),
              const SizedBox(width: 10),
              Text(
                title,
                style: TextStyle(
                  color: textColor,
                  fontSize: 17,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          ...children,
        ],
      ),
    );
  }

  /// ================= 下拉组件 =================
  Widget _buildDropdown({
    required String label,
    required String? value,
    required List<String> items,
    required Function(String?) onChanged,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: DropdownButtonFormField<String>(
        value: value,
        dropdownColor: const Color(0xFF1A2E5A),
        iconEnabledColor: Colors.blueAccent,
        decoration: InputDecoration(
          labelText: label,
          labelStyle: TextStyle(color: subTextColor, fontSize: 14),
          filled: true,
          fillColor: inputFillColor,
          contentPadding:
              const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: BorderSide(color: Colors.white.withOpacity(0.1)),
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: const BorderSide(color: Colors.blueAccent, width: 1.5),
          ),
        ),
        style: TextStyle(color: textColor, fontSize: 15),
        items: items
            .map((e) => DropdownMenuItem(
                  value: e,
                  child: Text(e),
                ))
            .toList(),
        onChanged: onChanged,
      ),
    );
  }

  /// ================= 保存逻辑 =================
  void _save() {
    if (!_formKey.currentState!.validate()) return;

    // TODO: 调用保存接口
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: const Text("设置已保存", style: TextStyle(color: Colors.white)),
        backgroundColor: Colors.blueAccent,
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
      ),
    );
  }
}
