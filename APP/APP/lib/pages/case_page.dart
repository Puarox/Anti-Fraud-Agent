import 'package:flutter/material.dart';
import 'package:swipable_stack/swipable_stack.dart';
import 'package:url_launcher/url_launcher.dart';

void main() {
  runApp(const MyApp());
}

/// ================= App =================
class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: SwipePage(),
      debugShowCheckedModeBanner: false,
    );
  }
}

/// ================= 数据模型 =================
class QAQuestion {
  final String question;
  final List<String> options;
  final int correctAnswer;
  final int caseIndex;
  final String explanation;
  final String? imageUrl; // 新增：支持图片URL

  QAQuestion({
    required this.question,
    required this.options,
    required this.correctAnswer,
    required this.caseIndex,
    required this.explanation,
    this.imageUrl,
  });

  factory QAQuestion.fromJson(Map<String, dynamic> json) {
    return QAQuestion(
      question: json["question"],
      options: List<String>.from(json["options"]),
      correctAnswer: json["correct_answer"],
      caseIndex: json["case_index"],
      explanation: json["explanation"],
      imageUrl: json["image_url"],
    );
  }
}

/// ================= 模拟JSON数据 =================
final List<Map<String, dynamic>> rawData = [
  {
    "question": "在搜索引擎中搜索地产信息，点击带有官网标识的搜索结果后，页面却自动跳转到赌博网站，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 0,
    "case_index": 7,
    "explanation": "这是典型的网络钓鱼/恶意跳转诈骗。诈骗者利用搜索结果诱导用户点击并跳转到恶意网站。",
    // 添加一张占位测试图
    "image_url":
        "https://paroxbucket.oss-cn-beijing.aliyuncs.com/uploads/image/%E5%9C%A8%E7%BA%BF%E6%A3%8B%E7%89%8C.png"
  },
  {
    "question": "接到自称是公安机关的电话，称你涉嫌洗钱，要求你提供银行卡信息配合调查，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 0,
    "case_index": 2,
    "explanation": "这是典型的冒充公检法诈骗。公安机关不会通过电话要求提供银行卡信息或进行转账。",
    "image_url":
        "https://paroxbucket.oss-cn-beijing.aliyuncs.com/uploads/image/%E8%BA%AB%E4%BB%BD%E5%86%92%E5%85%85.png"
  },
  {
    "question": "收到短信称有包裹被海关扣留，需支付清关费，短信中包含付款链接，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 0,
    "case_index": 1,
    "explanation": "这是典型的虚假包裹诈骗。诈骗分子冒充海关或物流公司，以“包裹被扣”为由诱导用户点击恶意链接付款。",
    "image_url":
        "https://paroxbucket.oss-cn-beijing.aliyuncs.com/uploads/image/636f944eb93a856dae30c73d.jpg"
  },
  {
    "question": "收到银行官方App推送的账户余额变动通知，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 1,
    "case_index": -1,
    "explanation": "银行官方渠道的正常服务通知，不属于诈骗。",
    "image_url":
        "https://paroxbucket.oss-cn-beijing.aliyuncs.com/uploads/image/img_localize_0cb44ed5f2e7a042e2bda8a1ef2686ef_450x800.png"
  },
  {
    "question": "在二手交易平台看中商品，卖家要求绕过平台私下微信转账交易，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 0,
    "case_index": 3,
    "explanation": "脱离平台交易是常见诈骗手法，失去平台担保后钱款难以追回。",
    "image_url":
        "https://paroxbucket.oss-cn-beijing.aliyuncs.com/uploads/image/%E4%BA%8C%E6%89%8B%E5%95%86%E8%B4%A9.png"
  },
  {
    "question": "收到学校官方公众号发送的学期课程安排通知，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 1,
    "case_index": -1,
    "explanation": "学校官方渠道的正常教学通知，不属于诈骗。",
    "image_url": ""
  },
  {
    "question": "网友推荐“稳赚不赔”的投资平台，承诺高额回报，要求你注册入金，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 0,
    "case_index": 4,
    "explanation": "这是典型的虚假投资理财诈骗。以高回报为诱饵，诱导用户向虚假平台充值。",
    "image_url": ""
  },
  {
    "question": "收到10086发送的月度话费账单短信，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 1,
    "case_index": -1,
    "explanation": "运营商官方账单通知属于正常服务，不属于诈骗。",
    "image_url": ""
  },
  {
    "question": "接到“电商客服”电话，称你购买的商品有质量问题，要为你退款，并索要短信验证码，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 0,
    "case_index": 5,
    "explanation": "这是冒充客服诈骗。诈骗分子以退款为由，骗取用户的验证码或诱导进行转账操作。",
    "image_url": ""
  },
  {
    "question": "收到政府官方网站发布的惠民政策解读文章，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 1,
    "case_index": -1,
    "explanation": "政府官方网站发布的权威信息，属于正常的政务公开，不属于诈骗。",
    "image_url": ""
  },
  {
    "question": "网上交友后，对方自称掌握某赌博网站漏洞，邀请你一起投注赚钱，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 0,
    "case_index": 6,
    "explanation": "这是“杀猪盘”诈骗的常见手法。先建立感情信任，再诱导参与虚假赌博或投资。",
    "image_url": ""
  },
  {
    "question": "收到交警部门发送的车辆违章通知短信，告知违章时间地点，并提示通过官方渠道处理，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 1,
    "case_index": -1,
    "explanation": "交管部门的违章提醒是正常执法服务，且提示通过官方渠道处理，不属于诈骗。",
    "image_url": ""
  },
  {
    "question": "收到“领导”微信消息，称急需用钱，要求你向指定账户转账，并承诺事后归还，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 0,
    "case_index": 8,
    "explanation": "这是冒充领导诈骗。诈骗分子盗用或仿冒领导头像、昵称，利用下属不敢核实的心态实施诈骗。",
    "image_url": ""
  },
  {
    "question": "收到医保局发送的政策调整通知，告知通过官方公众号查询详情，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 1,
    "case_index": -1,
    "explanation": "医保部门的政策通知属于正常政务公开，且引导至官方渠道，不属于诈骗。",
    "image_url": ""
  },
  {
    "question": "点击“免费领红包”链接后，跳转到需填写身份证、银行卡信息的页面，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 0,
    "case_index": 9,
    "explanation": "这是虚假红包诈骗。以红包为诱饵，实则窃取个人敏感信息或植入木马。",
    "image_url": ""
  },
  {
    "question": "收到自来水公司发送的停水检修通知，告知停水区域和时间，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 1,
    "case_index": -1,
    "explanation": "公用事业公司的服务通知属于正常业务流程，不属于诈骗。",
    "image_url": ""
  },
  {
    "question": "收到“ETC认证失效”短信，要求点击链接在线认证，否则将停用，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 0,
    "case_index": 10,
    "explanation": "这是ETC认证类诈骗。短信中的链接指向钓鱼网站，旨在窃取车主的ETC账户信息和密码。",
    "image_url": ""
  },
  {
    "question": "收到运营商发送的“积分即将过期”提醒，并附上官方商城兑换链接，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 1,
    "case_index": -1,
    "explanation": "运营商通过官方渠道进行的正常营销或服务提醒，不属于诈骗。",
    "image_url": ""
  },
  {
    "question": "接到“金融平台客服”电话，称可帮你注销校园贷账户，否则影响征信，并要求你下载某会议软件共享屏幕，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 0,
    "case_index": 11,
    "explanation": "这是注销校园贷诈骗的升级版。通过制造恐慌（影响征信），诱导下载软件并共享屏幕，从而窃取支付密码等关键信息。",
    "image_url": ""
  },
  {
    "question": "收到社区街道办通过正规渠道发布的疫苗接种安排通知，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 1,
    "case_index": -1,
    "explanation": "基层组织的正常公共服务通知，不属于诈骗。",
    "image_url": ""
  },
  {
    "question": "网友推荐“零门槛、高佣金”的点赞关注任务，但需先缴纳会员费或押金，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 0,
    "case_index": 13,
    "explanation": "这是兼职诈骗的变种。以简单任务和高佣金为诱饵，实则骗取“入门费”，后续任务无法完成或无法提现。",
    "image_url": ""
  },
  {
    "question": "收到市场监管局发送的企业年报催报通知，并告知通过国家企业信用信息公示系统办理，这是否是诈骗？",
    "options": ["是", "否"],
    "correct_answer": 1,
    "case_index": -1,
    "explanation": "政府部门的法定职责履行通知，且指定了唯一的官方办理渠道，不属于诈骗。",
    "image_url": ""
  }
];

/// ================= 页面 =================
class SwipePage extends StatefulWidget {
  const SwipePage({super.key});

  @override
  State<SwipePage> createState() => _SwipePageState();
}

class _SwipePageState extends State<SwipePage> {
  final controller = SwipableStackController();

  late List<QAQuestion> questions;
  int correctCount = 0;

  @override
  void initState() {
    super.initState();
    questions = rawData.map((e) => QAQuestion.fromJson(e)).toList();
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

  /// ================= 滑动逻辑 =================
  void onSwipe(int index, SwipeDirection direction) {
    final q = questions[index];

    /// 左滑 = 诈骗（0），右滑 = 安全（1）
    int userAnswer = direction == SwipeDirection.left ? 0 : 1;

    bool correct = userAnswer == q.correctAnswer;
    if (correct) correctCount++;

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (_) => AlertDialog(
        backgroundColor: const Color(0xFF1A2E5A),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
        ),
        title: Text(
          correct ? "✅ 判断正确" : "❌ 判断错误",
          style: const TextStyle(color: Colors.white),
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text("你的判断：${q.options[userAnswer]}",
                style: const TextStyle(color: Colors.white70)),
            const SizedBox(height: 10),
            Text("正确答案：${q.options[q.correctAnswer]}",
                style: const TextStyle(color: Colors.white70)),
            const SizedBox(height: 10),
            Text("解析：${q.explanation}",
                style: const TextStyle(color: Colors.white60, height: 1.5)),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () async {
              Navigator.pop(context);

              final url = Uri.parse(
                  "https://tech.chinadaily.com.cn/a/202510/28/WS69008da3a310c4deea5eeb39.html");

              if (await canLaunchUrl(url)) {
                await launchUrl(url);
              } else {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text("无法打开链接")),
                );
              }
            },
            child: const Text("查看真实案例",
                style: TextStyle(color: Colors.orangeAccent)),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              setState(() {});
            },
            child: const Text("继续", style: TextStyle(color: Colors.white)),
          )
        ],
      ),
    );
  }

  /// ================= 卡片 (美化 + 图片区域) =================
  Widget buildCard(QAQuestion data) {
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(28),
        // 美化：采用稍亮一点的深蓝调渐变，与背景有层次感
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            Color(0xFF243B6B),
            Color(0xFF17284F),
          ],
        ),
        // 添加一个很浅的边框增加质感
        border: Border.all(
          color: Colors.white.withOpacity(0.15),
          width: 1.5,
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.5),
            blurRadius: 25,
            offset: const Offset(0, 15),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          /// 图片位置：固定高度，自适应所有尺寸
          ClipRRect(
            // 顶部的圆角需要与卡片的圆角匹配 (扣除边框厚度)
            borderRadius: const BorderRadius.vertical(top: Radius.circular(26)),
            child: SizedBox(
              height: 220, // 你可以在这里调整图片的固定高度
              child: data.imageUrl != null && data.imageUrl!.isNotEmpty
                  ? Image.network(
                      data.imageUrl!,
                      fit: BoxFit.cover, // 核心：保持比例填满并自动裁剪
                      errorBuilder: (context, error, stackTrace) {
                        return _buildImagePlaceholder();
                      },
                    )
                  : _buildImagePlaceholder(),
            ),
          ),

          /// 下半部分文字与操作区
          Expanded(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
              child: Column(
                children: [
                  const Spacer(),

                  /// 问题
                  Text(
                    data.question,
                    style: const TextStyle(
                      fontSize: 18,
                      height: 1.6,
                      fontWeight: FontWeight.w600,
                      color: Colors.white,
                    ),
                    textAlign: TextAlign.center,
                  ),

                  const Spacer(),

                  /// 操作提示
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(6),
                            decoration: BoxDecoration(
                              color: Colors.redAccent.withOpacity(0.2),
                              shape: BoxShape.circle,
                            ),
                            child: const Icon(Icons.close,
                                color: Colors.redAccent, size: 20),
                          ),
                          const SizedBox(width: 8),
                          const Text("诈骗",
                              style: TextStyle(
                                  color: Colors.redAccent,
                                  fontWeight: FontWeight.bold)),
                        ],
                      ),
                      Row(
                        children: [
                          const Text("安全",
                              style: TextStyle(
                                  color: Colors.greenAccent,
                                  fontWeight: FontWeight.bold)),
                          const SizedBox(width: 8),
                          Container(
                            padding: const EdgeInsets.all(6),
                            decoration: BoxDecoration(
                              color: Colors.greenAccent.withOpacity(0.2),
                              shape: BoxShape.circle,
                            ),
                            child: const Icon(Icons.check,
                                color: Colors.greenAccent, size: 20),
                          ),
                        ],
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  /// 图片占位符（如果没有图片或加载失败时显示）
  Widget _buildImagePlaceholder() {
    return Container(
      color: Colors.white.withOpacity(0.05),
      child: Center(
        child: Icon(
          Icons.image_not_supported_outlined,
          size: 50,
          color: Colors.white.withOpacity(0.3),
        ),
      ),
    );
  }

  /// ================= UI =================
  @override
  Widget build(BuildContext context) {
    // 获取屏幕的高度
    final screenHeight = MediaQuery.of(context).size.height;
    final screenWidth = MediaQuery.of(context).size.width;

    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Stack(
        children: [
          _buildBackground(),
          SafeArea(
            child: Column(
              children: [
                const SizedBox(height: 20),
                const Text(
                  "反诈判断训练",
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 22,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  "正确：$correctCount / ${questions.length}",
                  style: const TextStyle(color: Colors.white70, fontSize: 16),
                ),
                const Spacer(),

                /// 核心改动：用 SizedBox 限制高度为屏幕高度的 0.7
                Center(
                  child: SizedBox(
                    height: screenHeight * 0.7,
                    width: screenWidth * 0.85, // 限制一下宽度，让两边留出边距
                    child: SwipableStack(
                      controller: controller,
                      itemCount: questions.length,
                      onSwipeCompleted: onSwipe,
                      builder: (context, properties) {
                        return buildCard(questions[properties.index]);
                      },
                    ),
                  ),
                ),
                const Spacer(),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

/// ================= 案例详情页 =================
class CaseDetailPage extends StatelessWidget {
  final int caseIndex;

  const CaseDetailPage({super.key, required this.caseIndex});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0A1F44),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: Text("案例 #$caseIndex"),
      ),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Text(
          getCaseDetail(caseIndex),
          style: const TextStyle(
            color: Colors.white,
            height: 1.8,
            fontSize: 16,
          ),
        ),
      ),
    );
  }

  String getCaseDetail(int index) {
    switch (index) {
      case 7:
        return "【真实案例】\n\n用户通过搜索引擎点击“官网”进入房产网站，但页面被劫持跳转至赌博平台，导致资金损失。\n\n警方提示：搜索结果不等于安全，需核实域名。";
      default:
        return "暂无案例数据";
    }
  }
}
