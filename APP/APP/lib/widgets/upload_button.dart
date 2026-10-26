import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';

class UploadButton extends StatelessWidget {
  final String text;
  final Function(String path) onSelected;

  const UploadButton({required this.text, required this.onSelected});

  void pickFile(BuildContext context) async {
    var res = await FilePicker.platform.pickFiles(
      type: FileType.image, // ✅ 只选图片
    );

    if (res != null && res.files.single.path != null) {
      onSelected(res.files.single.path!);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 10),
      child: ElevatedButton(
        onPressed: () => pickFile(context),
        child: Text(text),
      ),
    );
  }
}
