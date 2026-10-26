import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';

class UploadButtonAudio extends StatelessWidget {
  final String text;
  final Function(String path) onSelected;

  const UploadButtonAudio({
    required this.text,
    required this.onSelected,
  });

  Future<void> pickAudio(BuildContext context) async {
    var res = await FilePicker.platform.pickFiles(
      type: FileType.audio, // ✅ 关键：音频
    );

    if (res != null && res.files.single.path != null) {
      onSelected(res.files.single.path!);
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("未选择音频")),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 10),
      child: ElevatedButton(
        onPressed: () => pickAudio(context),
        child: Text(text),
      ),
    );
  }
}
