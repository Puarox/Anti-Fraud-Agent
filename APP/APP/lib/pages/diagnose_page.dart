import 'dart:io';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:setest/pages/result_page.dart';

import '../services/api_service.dart';
import '../services/upload_service.dart';
import '../widgets/upload_button.dart';

enum InputType { text, image, audio, video }

class DiagnosePage extends StatefulWidget {
  @override
  State<DiagnosePage> createState() => _DiagnosePageState();
}

class _DiagnosePageState extends State<DiagnosePage> {
  bool isLoading = false;
  InputType selectedType = InputType.text;

  static const platform = MethodChannel('share_channel');
  final TextEditingController _textController = TextEditingController();

  File? selectedFile;

  // =========================
  // 初始化
  // =========================
  @override
  void initState() {
    super.initState();

    platform.setMethodCallHandler((call) async {
      final path = call.arguments;

      switch (call.method) {
        case "onAudioShared":
          _handleSharedFile(InputType.audio, path);
          break;

        case "onVideoShared":
          _handleSharedFile(InputType.video, path);
          break;

        case "onImageShared":
          _handleSharedFile(InputType.image, path);
          break;
      }
    });

    _checkInitialShare();
  }

  void _handleSharedFile(InputType type, String path) {
    selectedType = type;
    selectedFile = File(path);

    setState(() {});

    // ⭐ 可选：自动触发分析（推荐打开）
    Future.delayed(const Duration(milliseconds: 300), () {
      _submit();
    });
  }

  Future<void> _checkInitialShare() async {
    try {
      final result = await platform.invokeMethod("getInitialSharedFile");

      if (result != null) {
        final type = result["type"];
        final path = result["path"];

        switch (type) {
          case "audio":
            _handleSharedFile(InputType.audio, path);
            break;
          case "video":
            _handleSharedFile(InputType.video, path);
            break;
          case "image":
            _handleSharedFile(InputType.image, path);
            break;
        }
      }
    } catch (_) {}
  }

  // =========================
  // 提交
  // =========================
  Future<void> _submit() async {
    if (selectedType == InputType.text) {
      _handleText();
      return;
    }

    if (selectedFile == null) {
      ScaffoldMessenger.of(context)
          .showSnackBar(const SnackBar(content: Text("请先上传文件")));
      return;
    }

    setState(() => isLoading = true);

    try {
      File file = selectedFile!;

      // ⭐ 1. 先上传
      String url = await UploadService.uploadFile(file);

      print("上传完成URL: $url");

      if (url.isEmpty) {
        throw Exception("上传失败");
      }

      // ⭐ 2. 再请求
      dynamic response;

      switch (selectedType) {
        case InputType.image:
          response = await ApiService.post("/model/image", {
            "text": _textController.text,
            "image_url": url,
          });
          break;

        case InputType.audio:
          response = await ApiService.post("/model/audio", {
            "text": _textController.text,
            "audio_url": url,
          });
          break;

        case InputType.video:
          print("上传完成URL: $url");
          response = await ApiService.post("/model/video", {
            "text": _textController.text,
            "videoUrl": url,
          });
          break;

        default:
          throw Exception("不支持");
      }

      final data = response.data;

      if (data["success"] != true) {
        throw Exception(data["message"]);
      }

      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => ResultPage(result: data["data"]),
        ),
      );
    } catch (e) {
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text("失败: $e")));
    } finally {
      setState(() => isLoading = false);
    }
  }

  Future<void> _handleRequest(
      BuildContext context, Future responseFuture) async {
    setState(() => isLoading = true);

    try {
      final response = await responseFuture;
      final data = response.data;

      if (data["success"] != true) {
        throw Exception(data["message"]);
      }

      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => ResultPage(result: data["data"]),
        ),
      );
    } catch (e) {
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text("失败: $e")));
    } finally {
      setState(() => isLoading = false);
    }
  }

  void _handleText() {
    final text = _textController.text.trim();

    if (text.isEmpty) {
      ScaffoldMessenger.of(context)
          .showSnackBar(const SnackBar(content: Text("请输入内容")));
      return;
    }

    _handleRequest(
      context,
      ApiService.post("/model/text", {"text": text}),
    );
  }

  // =========================
  // UI
  // =========================
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      appBar: AppBar(
        title: const Text("鉴诈", style: TextStyle(color: Colors.white)),
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: true,
      ),
      body: Stack(
        children: [
          _buildBackground(),
          _buildContent(),
          if (isLoading) _buildLoading(),
        ],
      ),
    );
  }

  // =========================
  // 深蓝背景（安全感）
  // =========================
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

  Widget _buildContent() {
    return SafeArea(
      // 🌟 1. 添加 SafeArea，防止刘海屏/底部指示器引发微小溢出
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            // 🌟 2. 使用 Expanded + SingleChildScrollView 让主体内容自适应可滚动
            Expanded(
              child: SingleChildScrollView(
                physics: const BouncingScrollPhysics(), // 增加原生回弹效果体验更好
                child: Column(
                  children: [
                    _buildSection(
                      title: "选择检测模式",
                      child: _buildModeSelector(),
                    ),
                    const SizedBox(height: 16),
                    _buildSection(
                      title: "补充信息",
                      child: Column(
                        children: [
                          _buildGlassInput(),
                          const SizedBox(height: 10),
                          if (selectedFile != null) _buildFilePreview(),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),

            // 🌟 3. 原本的 Spacer() 被移除，用一点固定间距代替，按钮固定在最下方
            const SizedBox(height: 16),
            _buildSubmitButton(),
          ],
        ),
      ),
    );
  }

  // =========================
  // 区块框（透明）
  // =========================
  Widget _buildSection({required String title, required Widget child}) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white24),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title,
              style: const TextStyle(
                  color: Colors.white70,
                  fontSize: 14,
                  fontWeight: FontWeight.w500)),
          const SizedBox(height: 10),
          child,
        ],
      ),
    );
  }

  // =========================
  // 模态卡片（重点改造）
  // =========================
  Widget _buildModeSelector() {
    return GridView.count(
      crossAxisCount: 2,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      crossAxisSpacing: 12,
      mainAxisSpacing: 12,
      childAspectRatio: 1.3,
      children: [
        _modeCard(InputType.text, "文本", Color(0xFF3A7BD5)),
        _modeCard(InputType.image, "图片", Color(0xFF6A5ACD)),
        _modeCard(InputType.audio, "语音", Color(0xFF1E90FF)),
        _modeCard(InputType.video, "视频", Color(0xFF4169E1)),
      ],
    );
  }

  String _getIconPath(InputType type) {
    switch (type) {
      case InputType.text:
        return "assets/file.png";
      case InputType.image:
        return "assets/picture.png";
      case InputType.audio:
        return "assets/voice.png";
      case InputType.video:
        return "assets/video.png";
    }
  }

  Widget _modeCard(InputType type, String title, Color color) {
    bool selected = selectedType == type;

    return GestureDetector(
      onTap: () {
        setState(() {
          selectedType = type;
          selectedFile = null;
        });
      },
      child: Container(
        decoration: BoxDecoration(
          color: selected ? color : color.withOpacity(0.4),
          borderRadius: BorderRadius.circular(16),
        ),
        padding: const EdgeInsets.all(12),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // ⭐ 这里放你的LOGO
            Container(
              height: 40,
              width: 40,
              alignment: Alignment.center,
              child: Image.asset(
                _getIconPath(type),
                color: Colors.white,
                fit: BoxFit.contain,
              ),
            ),

            const SizedBox(height: 8),

            Text(
              title,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            )
          ],
        ),
      ),
    );
  }

  // =========================
  // 半透明输入框 + 上传按钮
  // =========================
  Widget _buildGlassInput() {
    return Stack(
      children: [
        ClipRRect(
          borderRadius: BorderRadius.circular(18),
          child: BackdropFilter(
            filter: ImageFilter.blur(sigmaX: 12, sigmaY: 12),
            child: Container(
              padding: const EdgeInsets.fromLTRB(14, 14, 14, 40),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.08),
                borderRadius: BorderRadius.circular(18),
                border: Border.all(color: Colors.white24),
              ),
              child: TextField(
                controller: _textController,
                maxLines: 4,
                style: const TextStyle(color: Colors.white),
                decoration: const InputDecoration(
                  hintText: "输入补充信息...",
                  hintStyle: TextStyle(color: Colors.white54),
                  border: InputBorder.none,
                ),
              ),
            ),
          ),
        ),

        // ➕ 上传按钮
        Positioned(
          right: 8,
          bottom: 8,
          child: GestureDetector(
            onTap: _pickFile,
            child: Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: Colors.blueAccent,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Row(
                children: [
                  const Icon(Icons.add, size: 18, color: Colors.white),
                  const SizedBox(width: 4),
                  Text(
                    _uploadText(),
                    style: const TextStyle(color: Colors.white, fontSize: 12),
                  )
                ],
              ),
            ),
          ),
        )
      ],
    );
  }

  String _uploadText() {
    switch (selectedType) {
      case InputType.image:
        return "图片";
      case InputType.audio:
        return "音频";
      case InputType.video:
        return "视频";
      default:
        return "";
    }
  }

  void _pickFile() {
    if (selectedType == InputType.text) return;

    UploadButton(
      text: "",
      onSelected: (path) {
        selectedFile = File(path);
        setState(() {});
      },
    ).pickFile(context);
  }

  // =========================
  // 文件展示
  // =========================
  Widget _buildFilePreview() {
    String name = selectedFile!.path.split('/').last;
    bool isImage = selectedType == InputType.image;

    return Row(
      children: [
        GestureDetector(
          onTap: isImage
              ? () => showDialog(
                    context: context,
                    builder: (_) => Dialog(
                      child: Image.file(selectedFile!),
                    ),
                  )
              : null,
          child: Icon(
            isImage ? Icons.image : Icons.insert_drive_file,
            color: Colors.white70,
          ),
        ),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            name,
            style: const TextStyle(color: Colors.white),
            overflow: TextOverflow.ellipsis,
          ),
        ),
        IconButton(
          icon: const Icon(Icons.close, color: Colors.white),
          onPressed: () => setState(() => selectedFile = null),
        )
      ],
    );
  }

  Widget _buildSubmitButton() {
    return SizedBox(
      width: double.infinity,
      child: ElevatedButton(
        onPressed: _submit,
        style: ElevatedButton.styleFrom(
          backgroundColor: const Color(0xFF0A1F44),
          textStyle: const TextStyle(color: Colors.white),
        ),
        child: const Text(
          "开始分析",
          style: TextStyle(color: Colors.white),
        ),
      ),
    );
  }

  Widget _buildLoading() {
    return Container(
      color: Colors.black.withOpacity(0.4),
      child: const Center(child: CircularProgressIndicator()),
    );
  }
}
