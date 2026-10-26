import {
  Badge,
  Body1,
  Body1Strong,
  Button,
  Caption1,
  Card,
  CardHeader,
  Field,
  Input,
  Menu,
  MenuItem,
  MenuList,
  MenuPopover,
  MenuTrigger,
  ProgressBar,
  Textarea,
  Tooltip,
  mergeClasses,
} from "@fluentui/react-components";
import {
  Add20Regular,
  ArrowLeft20Regular,
  ArrowRight20Regular,
  ArrowSort20Regular,
  Box20Regular,
  CallEnd20Regular,
  Chat20Regular,
  CheckmarkCircle20Regular,
  Clock20Regular,
  DismissCircle20Regular,
  Filter20Regular,
  History20Regular,
  MicOff20Regular,
  MoreHorizontal20Regular,
  Pause20Regular,
  People20Regular,
  Person20Regular,
  Receipt20Regular,
  Sparkle20Regular,
  Wrench20Regular,
} from "@fluentui/react-icons";
import axios from "axios";
import { useEffect, useRef, useState, type JSX } from "react";
import io from "socket.io-client";
import ContextComponent from "../components/ContextComponent";
import { useAuthStore } from "../stores/auth-store";
import { useStyles } from "../styles/WorkbenchStyle";

// 1. 定义 Dialogue 对象的接口（与 Java 后端 pojo 对应）
interface Dialogue {
  role: string;
  content: string;
  time: string; // 或者 Date，取决于后端传回的是 ISO 字符串还是时间戳
}

const scriptGroups = {
  operate: {
    label: "操作话术",
    scripts: [
      "很抱歉为您带来不愉快的体验，我现在查询到您的话费剩余与套餐信息。",
      "我先为您核对本月账单明细，请稍等 10 秒钟。",
      "已为您确认到该笔扣费来源，稍后我可以发送详细说明。",
    ],
  },
};

const feedbackScripts = [
  "我已为您完成费用核对，是否需要发送明细给您？",
  "调整申请已提交，预计 2 小时内完成，请留意短信通知。",
  "回访已安排，时间可调整，请确认您方便的时间。",
];

const highlightOrder = [
  "打招呼",
  "反馈话术按钮",
  "查询话费余额",
  "查询流量余量",
  "用户查询个人套餐信息",
  "节点话术按钮",
  "套餐状态正常",
  "查询套餐信息",
  "所有套餐简述",
  "反馈话术按钮",
  "查询套餐详情",
  "创建客户套餐成功",
];

type ConsumptionRecord = {
  id: string;
  time: string;
  amount: string;
  desc: string;
  status?: string | null;
  costType?: string | null;
  dueDate?: string | null;
  paidAt?: string | null;
};
function isChatMessage(msg: any) {
  return (
    (msg.role === "AGENT" || msg.role === "客服" || msg.role === "CUSTOMER" || msg.role === "客户") &&
    typeof msg.content === "string" &&
    msg.content.trim().length > 0
  );
}
function isCustomerRole(role?: string) {
  return role === "customer" || role === "CUSTOMER" || role === "客户";
}
function getRandomScore(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}
type MaintenanceRecord = { id: string; time: string; desc: string };
type RepairmentDetail = {
  id: string;
  title?: string | null;
  description?: string | null;
  address?: string | null;
  status?: string | null;
  createdAt?: string | null;
};
type BundleItem = {
  id: string;
  name: string;
  status: string;
  description?: string | null;
  startDate?: string | null;
  endDate?: string | null;
  price?: number | null;
  dataLimit?: number | null;
  callMinutes?: number | null;
  smsCount?: number | null;
  validityDays?: number | null;
};
type HistoryRecord = {
  id: string;
  time: string;
  desc: string;
  result: string;
  summary?: string | null;
  satisfactionScore?: number | null;
  createdAt?: string | null;
  fullContext?: string | null;
};
type CustomerInfo = {
  name: string;
  phone: string;
  status: string;
  balance: number;
  currentMonthCost: number;
  currentMonthBundleAmount: number;
  recentBillDate?: string | null;
  recentBillAmount?: number | null;
  unpaidBillAmount?: number | null;
  remainingDataCount?: number | null;
};
type ScriptItem = {
  id: string;
  name?: string | null;
  summary?: string | null;
  content?: string | null;
  enabled?: boolean | string | number | null;
  score?: number | string | null;
  priority?: number | null;
};
type OutEdge = {
  edgeId: string;
  nodeId: string;
  nodeName: string;
  nodeDescription?: string | null;
  score?: number | null;
};

const initialMaintenanceRecords: MaintenanceRecord[] = [
  { id: "m1", time: "2024-11-02", desc: "宽带掉线工单，已恢复" },
  { id: "m2", time: "2024-09-16", desc: "手机信号弱投诉，已优化" },
];

const initialServiceHistory: HistoryRecord[] = [
  { id: "h1", time: "2024-12-25", desc: "账单咨询", result: "已解释费用" },
  { id: "h2", time: "2024-12-10", desc: "套餐变更", result: "已完成升级" },
  { id: "h3", time: "2024-11-28", desc: "投诉回访", result: "客户满意" },
];

type DetailType = "consumption" | "maintenance" | "history" | "bundleUser" | "bundleAll" | "maintenanceCreate";
type DetailState = { type: DetailType; id: string } | null;
type StatusValue = "AVAILABLE" | "BUSY" | "POST_PROCESS" | "BREAK" | "OFFLINE" | "OTHER";

export default function WorkbenchPage() {
  const styles = useStyles();
  const activeGroup = scriptGroups.operate;
  const token = useAuthStore.getState().token;
  const profileData = useAuthStore.getState().profileData;
  const [workStatus, setWorkStatus] = useState<StatusValue>("BUSY");
  const [rightTab, setRightTab] = useState<
    "context" | "consumption" | "maintenance" | "userBundles" | "allBundles" | "history"
  >("context");
  const [rightDetail, setRightDetail] = useState<DetailState>(null);
  const [incomingVisible, setIncomingVisible] = useState(true);
  const [callSeconds, setCallSeconds] = useState(0);
  const [callTimerRunning, setCallTimerRunning] = useState(false);
  const [showIncomingCard, setShowIncomingCard] = useState(true);
  const [agentAlias, setAgentAlias] = useState(profileData?.alias || "客服");
  const [incomingControlsVisible, setIncomingControlsVisible] = useState(true);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [nodeScripts, setNodeScripts] = useState<ScriptItem[]>([]);

  const [commonScripts, setCommonScripts] = useState<ScriptItem[]>([]);
  const [feedbackScriptsData, setFeedbackScriptsData] = useState<ScriptItem[]>([]);
  const [scriptGroupTab, setScriptGroupTab] = useState<"node" | "common" | "feedback">("node");
  const [selectedScript, setSelectedScript] = useState<ScriptItem | null>(null);
  const [outEdges, setOutEdges] = useState<OutEdge[]>([]);
  const [undoNodeId, setUndoNodeId] = useState<string | null>(null);
  const [maintenanceForm, setMaintenanceForm] = useState({
    title: "",
    description: "",
    address: "",
    contactPhone: "",
    appointmentTime: "",
  });

  const [unsubscribeTarget, setUnsubscribeTarget] = useState<{ id: string; name: string } | null>(null);
  const [repairmentDetail, setRepairmentDetail] = useState<RepairmentDetail | null>(null);
  const [optimizedScript, setOptimizedScript] = useState<string>("");
  // 添加socket和messages状态
  const [messages, setMessages] = useState<Dialogue[]>([]);
  const [aiLoading, setAiLoading] = useState(false);
  const [customerSatisfaction, setCustomerSatisfaction] = useState(() => getRandomScore(70, 90));
  const [scriptMatch, setScriptMatch] = useState(() => getRandomScore(70, 90));
  const [highlightIndex, setHighlightIndex] = useState(-1);
  const lastTriggeredIndexRef = useRef(-1);
  const customerId = "0017ca61-5d5e-45b1-8ce4-30566523eddb";
  const userId = profileData?.id ?? "";
  const isScriptEnabled = (item: ScriptItem) => {
    const value = item.enabled;
    if (value === undefined || value === null) return true;
    if (typeof value === "string") {
      const normalized = value.trim().toLowerCase();
      return normalized !== "false" && normalized !== "0";
    }
    return value !== false && value !== 0;
  };
  const getRecommendedIndex = (items: ScriptItem[]) => {
    if (items.length === 0) return null;
    let hasScore = false;
    let maxScore = -Infinity;
    let bestIndex = 0;
    items.forEach((item, index) => {
      const raw = item.score;
      if (raw === null || raw === undefined || raw === "") return;
      const score = typeof raw === "number" ? raw : Number(raw);
      if (Number.isNaN(score)) return;
      hasScore = true;
      if (score > maxScore) {
        maxScore = score;
        bestIndex = index;
      }
    });
    return hasScore ? bestIndex : 0;
  };
  const getHighlightTarget = () => (highlightIndex >= 0 ? highlightOrder[highlightIndex] : "");
  const isHighlightName = (name?: string | null) => {
    if (!name) return false;
    const target = getHighlightTarget();
    return Boolean(target) && name.includes(target);
  };
  const getTargetScriptGroup = (target: string) => {
    if (!target) return null;
    if (nodeScripts.some((item) => item.name && item.name.includes(target))) return "node";
    if (feedbackScriptsData.some((item) => item.name && item.name.includes(target))) return "feedback";
    if (commonScripts.some((item) => item.name && item.name.includes(target))) return "common";
    return null;
  };
  const getStatusLabel = (status?: string | null) => {
    const normalized = (status ?? "").toUpperCase();
    switch (normalized) {
      case "ARREARS":
        return "欠费";
      case "NORMAL":
        return "正常";
      case "SUSPENDED":
        return "暂停";
      case "CANCELLED":
        return "已注销";
      default:
        return status ?? "-";
    }
  };
  const getConsumptionStatusLabel = (status?: string | null) => {
    const normalized = (status ?? "").toUpperCase();
    switch (normalized) {
      case "UNPAID":
        return "未支付";
      case "PAID":
        return "已支付";
      case "OVERDUE":
        return "已逾期";
      default:
        return status ?? "-";
    }
  };
  const getConsumptionTypeLabel = (type?: string | null) => {
    const normalized = (type ?? "").toUpperCase();
    switch (normalized) {
      case "MONTHLY":
        return "月费";
      case "OVERAGE":
        return "超额";
      case "PENALTY":
        return "违约金";
      case "REFUND":
        return "退款";
      case "OTHER":
        return "其他";
      default:
        return type ?? "-";
    }
  };
  const formatDateTime = (value?: string | null) => {
    if (!value) return "-";
    const timestamp = Date.parse(value);
    if (Number.isNaN(timestamp)) return value;
    return new Date(timestamp).toLocaleString("zh-CN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      hour12: false,
    });
  };
  const parseFullContext = (value?: string | null) => {
    if (!value) return [];
    try {
      const parsed = JSON.parse(value);
      if (Array.isArray(parsed)) {
        return parsed
          .map((item: any) => ({
            role: item.role ?? "",
            time: item.time ?? "",
            content: item.content ?? "",
          }))
          .filter((item: any) => item.role || item.time || item.content);
      }
    } catch {
      return [];
    }
    return [];
  };

  useEffect(() => {
    console.log("auth token:", token);
  }, [token]);

  // 监听sessionId变化
  useEffect(() => {
    console.log("🔄 SessionId changed:", sessionId);
    if (!sessionId) {
      console.log("🔄 SessionId is now null/undefined");
    }
  }, [sessionId]);
  useEffect(() => {
    let socketInstance: any = null;

    if (sessionId) {
      console.log("sessionId available, attempting to establish socket connection");

      try {
        // 初始化Socket连接
        const socket = io("http://localhost:9092", {
          transports: ["websocket"],
          forceNew: true,
          timeout: 5000, // 设置5秒超时
        });
        socketInstance = socket;
        console.log("Socket instance created");

        // 监听连接事件
        socket.on("connect", () => {
          console.log("✅ Socket connected successfully");
          console.log("Attempting to join room with sessionId:", sessionId);
          socket.emit("join", { sessionId });
          console.log("✅ Sent join request with sessionId:", sessionId);
        });

        // 监听连接错误
        socket.on("connect_error", (error: Error) => {
          console.error("❌ Socket connection error:", error);
        });

        // 监听连接超时
        socket.on("connect_timeout", () => {
          console.error("❌ Socket connection timeout");
        });

        // 监听断开连接
        socket.on("disconnect", (reason: String) => {
          console.log("🔌 Socket disconnected:", reason);
        });

        // 监听新消息
        socket.on("new_message", (newDialogue: Dialogue) => {
          console.log("📩 Received new message:", newDialogue);
          setMessages((prev) => [...prev, newDialogue]);
          if (isCustomerRole(newDialogue?.role)) {
            setCustomerSatisfaction(getRandomScore(70, 90));
            setScriptMatch(getRandomScore(70, 90));
          }
        });
      } catch (error) {
        console.error("❌ Error establishing socket connection:", error);
      }
    } else {
      console.log("sessionId is null, skipping socket connection");
    }

    return () => {
      if (socketInstance) {
        console.log("Cleaning up socket connection");
        socketInstance.disconnect();
      }
    };
  }, [sessionId]);
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.code !== "Space") return;
      const target = event.target as HTMLElement | null;
      const tagName = target?.tagName?.toLowerCase();
      if (tagName === "input" || tagName === "textarea" || target?.isContentEditable) {
        return;
      }
      event.preventDefault();
      setHighlightIndex((prev) => {
        const next = prev + 1;
        return next >= highlightOrder.length ? 0 : next;
      });
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);
  useEffect(() => {
    if (!sessionId) return;
    if (messages.length === 0) return;
    if (nodeScripts.length === 0 && commonScripts.length === 0 && feedbackScriptsData.length === 0) return;
    if (aiLoading) return;

    const normalizeRole = (role: string) => {
      const normalized = role?.toLowerCase();
      if (normalized === "customer" || role === "客户") return "customer";
      if (normalized === "user" || role === "客服" || normalized === "agent") return "user";
      return normalized || "customer";
    };
    const dialogue = messages.filter(isChatMessage).map((item) => ({
      role: normalizeRole(item.role),
      time: item.time,
      content: item.content,
    }));

    if (dialogue.length === 0) return;

    setAiLoading(true);
    axios
      .post("http://127.0.0.1:8091/classify", {
        nodeScript: nodeScripts,
        feedbackScript: feedbackScriptsData,
        commonScript: commonScripts,
        dialogue,
      })
      .then((resp) => {
        const ids: string[] = resp.data?.data?.ids ?? [];
        const scores: number[] = resp.data?.data?.scores ?? [];
        if (ids.length !== scores.length || ids.length === 0) return;
        const scoreMap = new Map(ids.map((id, index) => [id, scores[index]]));
        const applyScores = (items: ScriptItem[]) =>
          items.map((item) => ({
            ...item,
            score: scoreMap.has(item.id) ? scoreMap.get(item.id) : item.score,
          }));
        setNodeScripts((prev) => applyScores(prev));
        setCommonScripts((prev) => applyScores(prev));
        setFeedbackScriptsData((prev) => applyScores(prev));
      })
      .catch((err) => {
        console.error("AI 置信度更新失败", err);
      })
      .finally(() => setAiLoading(false));
  }, [sessionId, messages, nodeScripts, commonScripts, feedbackScriptsData, aiLoading]);
  useEffect(() => {
    setRightTab("context");
    let timer: number | undefined;
    if (callTimerRunning) {
      timer = window.setInterval(() => setCallSeconds((s) => s + 1), 1000);
    }
    return () => {
      if (timer) {
        window.clearInterval(timer);
      }
    };
  }, [callTimerRunning]);
  const [consumptionRecords, setConsumptionRecords] = useState<ConsumptionRecord[]>([
    { id: "c1", time: "2024-12-28 13:50", amount: "￥56.00", desc: "套餐外流量" },
    { id: "c2", time: "2024-12-20 09:10", amount: "￥48.00", desc: "语音超出" },
    { id: "c3", time: "2024-12-12 18:32", amount: "￥94.00", desc: "增值服务" },
  ]);
  const [maintenanceRecords, setMaintenanceRecords] = useState<MaintenanceRecord[]>(initialMaintenanceRecords);
  const [userBundles, setUserBundles] = useState<BundleItem[]>([
    { id: "b1", name: "星耀 5G 79 元", status: "在用" },
    { id: "b2", name: "流量加油包 20GB", status: "在用" },
    { id: "b3", name: "国际漫游日包", status: "已过期" },
  ]);
  const [allBundles, setAllBundles] = useState<BundleItem[]>([
    { id: "ab1", name: "星耀 5G 79 元", status: "在售", description: "套内 30GB + 500 分钟" },
    { id: "ab2", name: "星耀 5G 129 元", status: "在售", description: "套内 60GB + 1000 分钟" },
    { id: "ab3", name: "流量加油包 20GB", status: "在售", description: "月度附加流量包" },
    { id: "ab4", name: "国际漫游日包", status: "在售", description: "含境外流量/语音" },
  ]);
  const [allBundlesSearch, setAllBundlesSearch] = useState("");
  const [allBundlesFilter, setAllBundlesFilter] = useState<"all" | "onSale" | "inUse" | "expired">("all");
  const [allBundlesSort, setAllBundlesSort] = useState<"default" | "priceAsc" | "priceDesc" | "nameAsc">("default");
  const fixedBalance = 33.8;
  const fixedRemainingDataCount = 1.3;
  const [customerInfo, setCustomerInfo] = useState<CustomerInfo>({
    name: "张三",
    phone: "13800138000",
    status: "NORMAL",
    balance: fixedBalance,
    currentMonthCost: 198,
    currentMonthBundleAmount: 3,
    recentBillDate: "2024-12-28 14:20:15",
    recentBillAmount: 0,
    unpaidBillAmount: 0,
    remainingDataCount: fixedRemainingDataCount,
  });
  const [serviceHistory, setServiceHistory] = useState<HistoryRecord[]>(initialServiceHistory);
  const handleDetailBack = () => {
    if (rightDetail?.type === "maintenance") {
      setRepairmentDetail(null);
    }
    setRightDetail(null);
  };

  useEffect(() => {
    axios
      .get("/customer/detail", { params: { id: customerId } })
      .then((resp) => {
        const info = resp.data?.data;
        if (info) {
          setCustomerInfo({
            name: info.name ?? customerInfo.name,
            phone: info.phone ?? customerInfo.phone,
            status: info.status ?? customerInfo.status,
            balance: fixedBalance,
            currentMonthCost: Number(info.currentMonthCost ?? customerInfo.currentMonthCost ?? 0),
            currentMonthBundleAmount: Number(
              info.currentMonthBundleAmount ?? customerInfo.currentMonthBundleAmount ?? 0
            ),
            recentBillDate: info.recentBillDate ?? customerInfo.recentBillDate,
            recentBillAmount: info.recentBillAmount ?? customerInfo.recentBillAmount,
            unpaidBillAmount: info.unpaidBillAmount ?? customerInfo.unpaidBillAmount,
            remainingDataCount: fixedRemainingDataCount,
          });
        }
        const list = resp.data?.data?.costList;
        if (Array.isArray(list)) {
          const mapped: ConsumptionRecord[] = list.map((item: any) => ({
            id: item.id ?? crypto.randomUUID?.() ?? Math.random().toString(36).slice(2),
            time: item.billingDate ?? item.createdAt ?? "",
            amount: typeof item.amount === "number" ? `￥${item.amount.toFixed(2)}` : `${item.amount ?? ""}`,
            desc: item.description ?? "",
            status: item.status ?? "",
            costType: item.costType ?? "",
            dueDate: item.dueDate ?? "",
            paidAt: item.paidAt ?? "",
          }));
          setConsumptionRecords(mapped);
        }

        const bundles = resp.data?.data?.bundleList;
        if (Array.isArray(bundles)) {
          const mappedBundles: BundleItem[] = bundles.map((item: any) => ({
            id: item.id ?? crypto.randomUUID?.() ?? Math.random().toString(36).slice(2),
            name: item.name ?? item.description ?? "套餐",
            status: item.status ?? "在用",
            description: item.description ?? "",
            startDate: item.startDate ?? item.beginDate ?? "",
            endDate: item.endDate ?? item.expireDate ?? "",
            price: item.price ?? null,
            dataLimit: item.dataLimit ?? null,
            callMinutes: item.callMinutes ?? null,
            smsCount: item.smsCount ?? null,
            validityDays: item.validityDays ?? null,
          }));
          setUserBundles(mappedBundles);
        }
      })
      .catch(() => {
        // 保留默认示例数据
      });

    axios
      .post("http://localhost:8090/bundle/list", null, {
        // 按后端要求走 POST，并携带分页参数（缺省为第一页/20条）
        params: {
          current: 1,
          size: 20,
        },
      })
      .then((resp) => {
        const raw = resp.data?.data ?? resp.data;
        const list = Array.isArray(raw) ? raw : Array.isArray(raw?.records) ? raw.records : [];
        if (Array.isArray(list)) {
          const mapped: BundleItem[] = list.map((item: any) => ({
            id: item.id ?? crypto.randomUUID?.() ?? Math.random().toString(36).slice(2),
            name: item.name ?? "套餐",
            status: item.bundleStatus ?? item.state ?? "在售",
            description: item.description ?? "",
            startDate: item.startDate ?? "",
            endDate: item.endDate ?? "",
            price: item.price ?? null,
            dataLimit: item.dataLimit ?? null,
            callMinutes: item.callMinutes ?? null,
            smsCount: item.smsCount ?? null,
            validityDays: item.validityDays ?? null,
          }));
          if (mapped.length > 0) {
            setAllBundles(mapped);
          }
        }
      })
      .catch((err) => {
        console.error("获取所有套餐失败", err);
        // 保留默认示例数据
      });

    axios
      .post(
        "http://localhost:8090/record/list",
        {
          customerId,
        },
        {
          headers: { token },
        }
      )
      .then((resp) => {
        const raw = resp.data?.data ?? resp.data;
        const records = Array.isArray(raw) ? raw : Array.isArray(raw?.records) ? raw.records : [];
        if (Array.isArray(records) && records.length > 0) {
          const mapped: HistoryRecord[] = records.map((item: any) => ({
            id: item.id ?? crypto.randomUUID?.() ?? Math.random().toString(36).slice(2),
            time: formatDateTime(item.startTime ?? item.createdAt ?? item.updatedAt ?? ""),
            desc: item.summary ?? "历史服务记录",
            result:
              item.satisfactionScore !== null && item.satisfactionScore !== undefined
                ? `满意度 ${item.satisfactionScore}`
                : "",
            summary: item.summary ?? "",
            satisfactionScore: item.satisfactionScore ?? null,
            createdAt: item.createdAt ?? "",
            fullContext: item.fullContext ?? "",
          }));
          setServiceHistory(mapped);
        }
      })
      .catch((err) => {
        console.error("获取历史服务失败", err);
      });

    axios
      .post("http://localhost:8090/repairment/list", null, {
        params: {
          current: 1,
          size: 20,
          customerId,
        },
        headers: { token },
      })
      .then((resp) => {
        const raw = resp.data?.data ?? resp.data;
        const records = Array.isArray(raw?.records) ? raw.records : Array.isArray(raw) ? raw : [];
        if (Array.isArray(records) && records.length > 0) {
          const mapped: MaintenanceRecord[] = records.map((item: any) => ({
            id: item.id ?? crypto.randomUUID?.() ?? Math.random().toString(36).slice(2),
            time: formatDateTime(item.createdAt ?? item.updatedAt ?? item.completedAt ?? ""),
            desc: item.title ?? item.description ?? "维修记录",
          }));
          setMaintenanceRecords(mapped);
        }
      })
      .catch((err) => {
        console.error("获取维修记录失败", err);
      });

    axios
      .get("http://localhost:8090/user/getUserDetail", {
        headers: { token },
      })
      .then((resp) => {
        const alias = resp.data?.data?.alias;
        if (alias) {
          setAgentAlias(alias);
        }
      })
      .catch((err) => {
        console.error("获取客服信息失败", err);
      });

    axios
      .get("http://localhost:8090/customer-bundle/meta", {
        params: {
          customerId,
        },
      })
      .then((resp) => {
        setCustomerInfo((prev) => ({
          ...prev,
          remainingDataCount: fixedRemainingDataCount,
        }));
      })
      .catch((err) => {
        console.error("获取剩余流量失败", err);
      });

    axios
      .get("http://localhost:8090/script/list/feedback", { headers: { token } })
      .then((resp) => {
        const list = Array.isArray(resp.data?.data) ? resp.data.data : [];
        const mapped: ScriptItem[] = list.map((item: any) => ({
          id: item.id ?? crypto.randomUUID?.() ?? Math.random().toString(36).slice(2),
          name: item.name ?? "反馈话术",
          summary: item.summary ?? "",
          content: item.content ?? "",
          enabled: item.enabled ?? true,
          score: item.score ?? null,
          priority: typeof item.priority === "number" ? item.priority : Number(item.priority ?? 0),
        }));
        const sorted = mapped
          .slice()
          .sort((a, b) => (b.priority ?? 0) - (a.priority ?? 0))
          .slice(0, 7);
        setFeedbackScriptsData(sorted);
      })
      .catch((err) => {
        console.error("获取反馈话术失败", err);
      });
  }, []);

  const renderRightContent = () => {
    if (rightDetail?.type === "consumption") {
      const record = consumptionRecords.find((r) => r.id === rightDetail.id);
      if (!record) return null;
      const detailItems = [
        { label: "消费内容", value: record.desc },
        { label: "发生时间", value: record.time },
        record.costType ? { label: "类型", value: getConsumptionTypeLabel(record.costType) } : null,
        record.dueDate ? { label: "到期日", value: record.dueDate } : null,
        record.status ? { label: "状态", value: getConsumptionStatusLabel(record.status) } : null,
        record.paidAt ? { label: "支付时间", value: record.paidAt } : null,
      ].filter(Boolean) as { label: string; value: string }[];
      return (
        <div className={mergeClasses(styles.panelList, styles.detailPanel)}>
          <div className={mergeClasses(styles.panelItem, styles.detailPanelItemTall)}>
            <div className={styles.consumptionDetail}>
              <div className={styles.consumptionAmount}>{record.amount}</div>
              <div className={styles.detailList}>
                {detailItems.map((item) => (
                  <div key={item.label} className={styles.detailItem}>
                    <span className={styles.detailLabel}>{item.label}</span>
                    <span className={styles.detailValue}>{item.value}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      );
    }

    if (rightDetail?.type === "maintenance") {
      const record = maintenanceRecords.find((r) => r.id === rightDetail.id);
      const detail = repairmentDetail && repairmentDetail.id === rightDetail.id ? repairmentDetail : null;
      if (!record && !detail) return null;
      const titleText = detail?.title || record?.desc || "-";
      const detailItems = [
        detail?.description ? { label: "描述", value: detail.description } : null,
        detail?.address ? { label: "地址", value: detail.address } : null,
        detail?.status ? { label: "状态", value: detail.status } : null,
        detail?.createdAt
          ? { label: "创建时间", value: formatDateTime(detail.createdAt) }
          : record?.time
          ? { label: "创建时间", value: record.time }
          : null,
      ].filter(Boolean) as { label: string; value: string }[];
      return (
        <div className={mergeClasses(styles.panelList, styles.detailPanel)}>
          <div className={mergeClasses(styles.panelItem, styles.detailPanelItemTall)}>
            <div className={styles.consumptionDetail}>
              <div className={styles.detailTitle}>{titleText}</div>
              <div className={styles.detailList}>
                {detailItems.map((item) => (
                  <div key={item.label} className={styles.detailItem}>
                    <span className={styles.detailLabel}>{item.label}</span>
                    <span className={styles.detailValue}>{item.value}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      );
    }

    if (rightDetail?.type === "maintenanceCreate") {
      return (
        <div className={mergeClasses(styles.panelList, styles.detailPanel)} style={{ minHeight: 600 }}>
          <div className={styles.panelItem} style={{ minHeight: 600, height: "600px" }}>
            <div className={styles.panelItemHeader}>
              <Body1Strong>新建维修</Body1Strong>
            </div>
            <div className={styles.maintenanceForm}>
              <div className={styles.maintenanceFields}>
                <Field label="标题">
                  <Input
                    value={maintenanceForm.title}
                    onChange={(_, data) => setMaintenanceForm((prev) => ({ ...prev, title: data.value }))}
                  />
                </Field>
                <Field label="描述">
                  <Textarea
                    value={maintenanceForm.description}
                    onChange={(_, data) => setMaintenanceForm((prev) => ({ ...prev, description: data.value }))}
                    rows={3}
                  />
                </Field>
                <Field label="地址">
                  <Input
                    value={maintenanceForm.address}
                    onChange={(_, data) => setMaintenanceForm((prev) => ({ ...prev, address: data.value }))}
                  />
                </Field>
                <Field label="联系电话">
                  <Input
                    value={maintenanceForm.contactPhone}
                    onChange={(_, data) => setMaintenanceForm((prev) => ({ ...prev, contactPhone: data.value }))}
                  />
                </Field>
                <Field label="预约时间">
                  <Input
                    type="datetime-local"
                    value={maintenanceForm.appointmentTime}
                    onChange={(_, data) => setMaintenanceForm((prev) => ({ ...prev, appointmentTime: data.value }))}
                  />
                </Field>
              </div>
              <div className={styles.maintenanceActions}>
                <Button appearance="primary" onClick={handleSubmitMaintenance}>
                  提交
                </Button>
              </div>
            </div>
          </div>
        </div>
      );
    }

    if (rightDetail?.type === "bundleUser") {
      const record = userBundles.find((r) => r.id === rightDetail.id);
      if (!record) return null;
      const detailItems = [
        { label: "状态", value: record.status },
        record.description ? { label: "描述", value: record.description } : null,
        record.startDate ? { label: "开始时间", value: record.startDate } : null,
        record.endDate ? { label: "到期时间", value: record.endDate } : null,
        record.price ? { label: "价格", value: `￥${record.price}` } : null,
        record.dataLimit ? { label: "流量", value: `${record.dataLimit} GB` } : null,
        record.callMinutes ? { label: "语音", value: `${record.callMinutes} 分钟` } : null,
        record.smsCount ? { label: "短信", value: `${record.smsCount} 条` } : null,
        record.validityDays ? { label: "有效期", value: `${record.validityDays} 天` } : null,
      ].filter(Boolean) as { label: string; value: string }[];
      return (
        <div className={mergeClasses(styles.panelList, styles.detailPanel)}>
          <div className={mergeClasses(styles.panelItem, styles.detailPanelItemTall)}>
            <div className={styles.panelItemHeader} style={{ alignItems: "flex-start" }}>
              <div style={{ display: "flex", flexDirection: "column" }}>
                <div className={mergeClasses(styles.detailTitle, styles.detailTitleSpacious)}>{record.name}</div>
              </div>
              <div style={{ display: "flex", flexDirection: "column", gap: 6, alignItems: "flex-end" }}>
                <Button size="small" appearance="primary" onClick={() => handleUnsubscribe(record.id, record.name)}>
                  退订
                </Button>
              </div>
            </div>
            <div className={styles.consumptionDetail}>
              <div className={mergeClasses(styles.detailList, styles.detailListSpacious)}>
                {detailItems.map((item) => (
                  <div key={item.label} className={styles.detailItem}>
                    <span className={styles.detailLabel}>{item.label}</span>
                    <span className={styles.detailValue}>{item.value}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      );
    }

    if (rightDetail?.type === "bundleAll") {
      const record = allBundles.find((r) => r.id === rightDetail.id);
      if (!record) return null;
      const isUnlimitedBundle = record.name === "无限流量套餐_137";
      const displayStatus = isUnlimitedBundle ? "可办理" : record.status;
      const detailItems = [
        { label: "状态", value: displayStatus },
        record.description ? { label: "描述", value: record.description } : null,
        record.startDate ? { label: "开始时间", value: record.startDate } : null,
        record.endDate ? { label: "到期时间", value: record.endDate } : null,
        record.price ? { label: "价格", value: `￥${record.price}` } : null,
        record.dataLimit ? { label: "流量", value: `${record.dataLimit} GB` } : null,
        record.callMinutes ? { label: "语音", value: `${record.callMinutes} 分钟` } : null,
        record.smsCount ? { label: "短信", value: `${record.smsCount} 条` } : null,
        !isUnlimitedBundle && record.validityDays ? { label: "有效期", value: `${record.validityDays} 天` } : null,
      ].filter(Boolean) as { label: string; value: string }[];
      return (
        <div className={mergeClasses(styles.panelList, styles.detailPanel)}>
          <div className={mergeClasses(styles.panelItem, styles.detailPanelItemTall)}>
            <div className={styles.consumptionDetail}>
              <div className={styles.detailTitle}>{record.name}</div>
              <div className={styles.detailList}>
                {detailItems.map((item) => (
                  <div key={item.label} className={styles.detailItem}>
                    <span className={styles.detailLabel}>{item.label}</span>
                    <span className={styles.detailValue}>{item.value}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      );
    }

    if (rightDetail?.type === "history") {
      const record = serviceHistory.find((r) => r.id === rightDetail.id);
      if (!record) return null;
      const contextItems = parseFullContext(record.fullContext);
      return (
        <div className={styles.panelList}>
          <Body1Strong className={styles.historyDetailTitle}>{record.desc}</Body1Strong>
          <div className={styles.detailSection}>
            <div className={styles.detailSectionCard}>
              <div style={{ display: "grid", gap: 8 }}>
                <div>
                  <Caption1 className={styles.panelMeta}>摘要</Caption1>
                  <Body1 style={{ display: "block", marginTop: 4 }}>{record.summary || "-"}</Body1>
                </div>
                <div>
                  <Caption1 className={styles.panelMeta}>满意度</Caption1>
                  <Body1 style={{ display: "block", marginTop: 4 }}>
                    {record.satisfactionScore !== null && record.satisfactionScore !== undefined
                      ? record.satisfactionScore
                      : "-"}
                  </Body1>
                </div>
                <div>
                  <Caption1 className={styles.panelMeta}>创建时间</Caption1>
                  <Body1 style={{ display: "block", marginTop: 4 }}>
                    {record.createdAt ? formatDateTime(record.createdAt) : record.time || "-"}
                  </Body1>
                </div>
              </div>
            </div>
            <div className={styles.detailSectionCard}>
              <Caption1 className={styles.panelMeta}>对话记录</Caption1>
              {contextItems.length === 0 ? (
                <Body1>-</Body1>
              ) : (
                <div className={styles.historyChatList} style={{ marginTop: 8 }}>
                  {contextItems.map((item: any, index: number) => {
                    const isAgent = (item.role ?? "").toLowerCase() === "user";
                    const roleLabel = isAgent ? "客服" : "客户";
                    const displayName = item.name || item.userName || item.user || roleLabel || "-";
                    const avatarText = isAgent
                      ? (agentAlias || roleLabel).slice(0, 1)
                      : (customerInfo.name || roleLabel).slice(0, 1);
                    return (
                      <div key={`${item.time}-${index}`} className={styles.historyChatRow}>
                        <div
                          className={mergeClasses(
                            styles.historyChatAvatar,
                            isAgent ? styles.historyChatAvatarAgent : styles.historyChatAvatarCustomer
                          )}>
                          {avatarText}
                        </div>
                        <div className={styles.historyChatBody}>
                          <div className={styles.historyChatMeta}>
                            <span className={styles.historyChatName}>{displayName}</span>
                            <span className={styles.historyChatTime}>{item.time || "-"}</span>
                          </div>
                          <div className={styles.historyChatContent}>{item.content || "-"}</div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        </div>
      );
    }

    if (rightTab === "context") {
      return (
        <ContextComponent customerInfo={customerInfo} messages={messages} agentAlias={agentAlias} styles={styles} />
      );
    }

    if (rightTab === "consumption") {
      const totalAmount = consumptionRecords.reduce((sum, item) => {
        const raw = String(item.amount ?? "");
        const numeric = Number(raw.replace(/[^\d.-]/g, ""));
        if (Number.isNaN(numeric)) return sum;
        return sum + numeric;
      }, 0);
      return (
        <div className={styles.panelList}>
          <Caption1 className={styles.panelMeta} style={{ marginTop: 4 }}>
            共 {consumptionRecords.length} 条，消费共 ￥{totalAmount.toFixed(2)}
          </Caption1>
          {consumptionRecords.map((item) => (
            <div
              key={item.id}
              className={styles.listRow}
              role="button"
              tabIndex={0}
              onClick={() => setRightDetail({ type: "consumption", id: item.id })}
              onKeyDown={(event) => {
                if (event.key === "Enter" || event.key === " ") {
                  event.preventDefault();
                  setRightDetail({ type: "consumption", id: item.id });
                }
              }}>
              <div className={styles.listRowLeading}>
                <Receipt20Regular className={styles.listRowIcon} />
                <div className={styles.listRowInfo}>
                  <Body1Strong className={styles.listRowTitle}>{item.amount}</Body1Strong>
                  <Caption1 className={styles.listRowMeta}>
                    {item.desc ? <span>{item.desc}</span> : null}
                    {item.desc && item.time ? <span> · </span> : null}
                    {item.time ? <span>{item.time}</span> : null}
                  </Caption1>
                </div>
              </div>
              <MoreHorizontal20Regular className={styles.listRowActionIcon} />
            </div>
          ))}
        </div>
      );
    }

    if (rightTab === "maintenance") {
      return (
        <div className={styles.panelList}>
          <Caption1 className={styles.panelMeta} style={{ marginTop: 4 }}>
            共 {maintenanceRecords.length} 条
          </Caption1>
          {maintenanceRecords.map((item) => (
            <div
              key={item.id}
              className={styles.listRow}
              role="button"
              tabIndex={0}
              onClick={() => handleOpenRepairmentDetail(item.id)}
              onKeyDown={(event) => {
                if (event.key === "Enter" || event.key === " ") {
                  event.preventDefault();
                  handleOpenRepairmentDetail(item.id);
                }
              }}>
              <div className={styles.listRowLeading}>
                <Wrench20Regular className={styles.listRowIcon} />
                <div className={styles.listRowInfo}>
                  <Body1Strong className={styles.listRowTitle}>{item.desc}</Body1Strong>
                  <Caption1 className={styles.listRowMeta}>{item.time}</Caption1>
                </div>
              </div>
              <MoreHorizontal20Regular className={styles.listRowActionIcon} />
            </div>
          ))}
        </div>
      );
    }

    if (rightTab === "userBundles") {
      return (
        <div className={styles.panelList}>
          <Caption1 className={styles.panelMeta} style={{ marginTop: 4 }}>
            共 {userBundles.length} 条
          </Caption1>
          {userBundles.map((item) => (
            <div
              key={item.id}
              className={styles.listRow}
              role="button"
              tabIndex={0}
              onClick={() => setRightDetail({ type: "bundleUser", id: item.id })}
              onKeyDown={(event) => {
                if (event.key === "Enter" || event.key === " ") {
                  event.preventDefault();
                  setRightDetail({ type: "bundleUser", id: item.id });
                }
              }}>
              <div className={styles.listRowLeading}>
                <People20Regular className={styles.listRowIcon} />
                <div className={styles.listRowInfo}>
                  <Body1Strong className={styles.listRowTitle}>{item.name}</Body1Strong>
                  <Caption1 className={styles.listRowMeta}>状态：{item.status}</Caption1>
                </div>
              </div>
              <MoreHorizontal20Regular className={styles.listRowActionIcon} />
            </div>
          ))}
        </div>
      );
    }

    if (rightTab === "allBundles") {
      const normalizedQuery = allBundlesSearch.trim().toLowerCase();
      const searchedBundles = normalizedQuery
        ? allBundles.filter((item) => {
            const haystack = [item.name, item.description, item.status].filter(Boolean).join(" ").toLowerCase();
            return haystack.includes(normalizedQuery);
          })
        : allBundles;
      const filteredBundles =
        allBundlesFilter === "all"
          ? searchedBundles
          : searchedBundles.filter((item) => {
              const statusText = String(item.status ?? "");
              if (allBundlesFilter === "onSale") return statusText.includes("在售");
              if (allBundlesFilter === "inUse") return statusText.includes("在用");
              return statusText.includes("已过期");
            });
      const displayedBundles = [...filteredBundles].sort((a, b) => {
        if (allBundlesSort === "default") return 0;
        if (allBundlesSort === "nameAsc") {
          return String(a.name ?? "").localeCompare(String(b.name ?? ""), "zh-CN");
        }
        const aPrice = typeof a.price === "number" ? a.price : Number(a.price ?? NaN);
        const bPrice = typeof b.price === "number" ? b.price : Number(b.price ?? NaN);
        const aValue = Number.isNaN(aPrice) ? Number.POSITIVE_INFINITY : aPrice;
        const bValue = Number.isNaN(bPrice) ? Number.POSITIVE_INFINITY : bPrice;
        return allBundlesSort === "priceAsc" ? aValue - bValue : bValue - aValue;
      });
      return (
        <div className={styles.panelList}>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <Input
              value={allBundlesSearch}
              onChange={(_, data) => setAllBundlesSearch(data.value)}
              placeholder="搜索套餐"
              style={{ width: "100%" }}
            />
            <Menu>
              <MenuTrigger disableButtonEnhancement>
                <Button
                  size="small"
                  appearance="secondary"
                  icon={<Filter20Regular />}
                  className={styles.inlineActionButton}
                  aria-label="筛选套餐">
                  筛选
                </Button>
              </MenuTrigger>
              <MenuPopover>
                <MenuList>
                  <MenuItem onClick={() => setAllBundlesFilter("all")}>全部</MenuItem>
                  <MenuItem onClick={() => setAllBundlesFilter("onSale")}>在售</MenuItem>
                  <MenuItem onClick={() => setAllBundlesFilter("inUse")}>在用</MenuItem>
                  <MenuItem onClick={() => setAllBundlesFilter("expired")}>已过期</MenuItem>
                </MenuList>
              </MenuPopover>
            </Menu>
            <Menu>
              <MenuTrigger disableButtonEnhancement>
                <Button
                  size="small"
                  appearance="secondary"
                  icon={<ArrowSort20Regular />}
                  className={styles.inlineActionButton}
                  aria-label="排序套餐">
                  排序
                </Button>
              </MenuTrigger>
              <MenuPopover>
                <MenuList>
                  <MenuItem onClick={() => setAllBundlesSort("default")}>默认</MenuItem>
                  <MenuItem onClick={() => setAllBundlesSort("priceAsc")}>价格升序</MenuItem>
                  <MenuItem onClick={() => setAllBundlesSort("priceDesc")}>价格降序</MenuItem>
                  <MenuItem onClick={() => setAllBundlesSort("nameAsc")}>名称排序</MenuItem>
                </MenuList>
              </MenuPopover>
            </Menu>
          </div>
          <Caption1 className={styles.panelMeta} style={{ marginTop: 4 }}>
            共 {displayedBundles.length} 条
          </Caption1>
          {displayedBundles.length === 0 ? (
            <Caption1 className={styles.panelMeta}>暂无套餐数据</Caption1>
          ) : (
            displayedBundles.map((item) => (
              <div
                key={item.id}
                className={styles.listRow}
                role="button"
                tabIndex={0}
                onClick={() => setRightDetail({ type: "bundleAll", id: item.id })}
                onKeyDown={(event) => {
                  if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    setRightDetail({ type: "bundleAll", id: item.id });
                  }
                }}>
                <div className={styles.listRowLeading}>
                  <Box20Regular className={styles.listRowIcon} />
                  <div className={styles.listRowInfo}>
                    <Body1Strong className={styles.listRowTitle}>{item.name}</Body1Strong>
                  </div>
                </div>
                <Menu>
                  <MenuTrigger disableButtonEnhancement>
                    <Button
                      size="small"
                      appearance="subtle"
                      icon={<MoreHorizontal20Regular />}
                      aria-label="更多操作"
                      onClick={(event) => event.stopPropagation()}
                      onKeyDown={(event) => event.stopPropagation()}
                    />
                  </MenuTrigger>
                  <MenuPopover>
                    <MenuList>
                      <MenuItem
                        onClick={(event) => {
                          event.stopPropagation();
                          setRightDetail({ type: "bundleAll", id: item.id });
                        }}>
                        详情
                      </MenuItem>
                      <MenuItem onClick={() => handleSubscribeBundle(item.id)}>办理</MenuItem>
                    </MenuList>
                  </MenuPopover>
                </Menu>
              </div>
            ))
          )}
        </div>
      );
    }

    if (rightTab === "history") {
      return (
        <div className={styles.panelList}>
          <Caption1 className={styles.panelMeta} style={{ marginTop: 4 }}>
            共 {serviceHistory.length} 条
          </Caption1>
          {serviceHistory.map((item) => (
            <div
              key={item.id}
              className={styles.listRow}
              role="button"
              tabIndex={0}
              onClick={() => setRightDetail({ type: "history", id: item.id })}
              onKeyDown={(event) => {
                if (event.key === "Enter" || event.key === " ") {
                  event.preventDefault();
                  setRightDetail({ type: "history", id: item.id });
                }
              }}>
              <div className={styles.listRowLeading}>
                <History20Regular className={styles.listRowIcon} />
                <div className={styles.listRowInfo}>
                  <Body1Strong className={styles.listRowTitle}>{item.desc}</Body1Strong>
                  <Caption1 className={styles.listRowMeta}>
                    {item.time}
                    {item.result ? ` · ${item.result}` : ""}
                  </Caption1>
                </div>
              </div>
              <MoreHorizontal20Regular className={styles.listRowActionIcon} />
            </div>
          ))}
        </div>
      );
    }

    return null;
  };

  const statusOptions: { label: string; value: StatusValue; icon: JSX.Element }[] = [
    { label: "空闲", value: "AVAILABLE", icon: <CheckmarkCircle20Regular /> },
    { label: "忙碌中", value: "BUSY", icon: <CallEnd20Regular /> },
    { label: "后处理", value: "POST_PROCESS", icon: <Clock20Regular /> },
    { label: "暂离/休息", value: "BREAK", icon: <Pause20Regular /> },
    { label: "离线", value: "OFFLINE", icon: <DismissCircle20Regular /> },
    { label: "其他", value: "OTHER", icon: <Person20Regular /> },
  ];

  const handleStatusChange = (next: StatusValue) => {
    setWorkStatus(next);
    axios
      .put(
        `http://localhost:8090/user/status`,
        {},
        {
          params: { status: next },
          headers: { token },
        }
      )
      .catch((err) => {
        console.error("更新状态失败", err);
      });
  };

  const handleCreateMaintenance = () => {
    setMaintenanceForm({
      title: "",
      description: "",
      address: "",
      contactPhone: "",
      appointmentTime: "",
    });
    setRightDetail({ type: "maintenanceCreate", id: "new" });
  };

  const handleOpenRepairmentDetail = (repairmentId: string) => {
    if (!repairmentId) return;
    axios
      .get(`http://localhost:8090/repairment/${repairmentId}`, { headers: { token } })
      .then((resp) => {
        const data = resp.data?.data;
        if (data) {
          setRepairmentDetail({
            id: data.id ?? repairmentId,
            title: data.title ?? "",
            description: data.description ?? "",
            address: data.address ?? "",
            status: data.status ?? "",
            createdAt: data.createdAt ?? "",
          });
        } else {
          setRepairmentDetail(null);
        }
        setRightDetail({ type: "maintenance", id: repairmentId });
      })
      .catch((err) => {
        console.error("获取维修详情失败", err);
      });
  };

  const handleSubscribeBundle = (bundleId: string) => {
    if (!bundleId) return;
    const now = new Date().toISOString();
    axios
      .post(
        "http://localhost:8090/customer-bundle/create",
        {
          customerId,
          bundleId,
          startDate: now,
          endDate: now,
          createdAt: now,
        },
        { headers: { token } }
      )
      .catch((err) => {
        console.error("办理失败", err);
      });
  };

  const handleSubmitMaintenance = () => {
    axios
      .post(
        "http://localhost:8090/repairment/create",
        {
          customerId,
          userId,
          title: maintenanceForm.title,
          description: maintenanceForm.description,
          address: maintenanceForm.address,
          contactPhone: maintenanceForm.contactPhone,
          appointmentTime: maintenanceForm.appointmentTime,
          status: "",
          priority: "",
          result: "",
        },
        { headers: { token } }
      )
      .then(() => {
        setRightDetail(null);
      })
      .catch((err) => {
        console.error("提交维修失败", err);
      });
  };

  const fetchSessionScripts = (sid: string) => {
    axios
      .get(`http://localhost:8090/session/${sid}/scripts`, { headers: { token } })
      .then((resp) => {
        const data = resp.data?.data ?? {};
        setNodeScripts(Array.isArray(data.nodeScript) ? data.nodeScript : []);
        setCommonScripts(Array.isArray(data.commonScript) ? data.commonScript : []);
      })
      .catch((err) => {
        console.error("获取话术失败", err);
      });
  };

  const fetchSessionState = (sid: string) => {
    axios
      .get(`http://localhost:8090/session/${sid}/state`, { headers: { token } })
      .then((resp) => {
        const data = resp.data?.data;
        if (data?.outEdge && Array.isArray(data.outEdge)) {
          const mapped: OutEdge[] = data.outEdge.map((item: any) => ({
            edgeId: item.edgeId ?? "",
            nodeId: item.nodeId ?? "",
            nodeName: item.nodeName ?? "",
            nodeDescription: item.nodeDescription ?? item.edgeDescription ?? "",
            score: typeof item.score === "number" ? item.score : Number(item.score ?? NaN),
          }));
          setOutEdges(mapped);
        }
        setUndoNodeId(data?.undoNodeId ?? null);
      })
      .catch((err) => console.error("获取会话状态失败", err));
  };

  const handleTransfer = (edgeId: string) => {
    if (!sessionId) return;
    axios
      .put(`http://localhost:8090/session/${sessionId}/transfer/${edgeId}`, {}, { headers: { token } })
      .then(() => {
        setSelectedScript(null);
        fetchSessionState(sessionId);
        fetchSessionScripts(sessionId);
      })
      .catch((err) => console.error("转移会话失败", err));
  };

  const handleUndoTransfer = () => {
    if (!sessionId) return;
    axios
      .put(`http://localhost:8090/session/${sessionId}/undo`, {}, { headers: { token } })
      .then(() => {
        setSelectedScript(null);
        fetchSessionState(sessionId);
        fetchSessionScripts(sessionId);
      })
      .catch((err) => console.error("回退会话失败", err));
  };

  const handleUnsubscribe = (bundleId: string, name?: string) => {
    if (!bundleId) return;
    setUnsubscribeTarget({ id: bundleId, name: name ?? "" });
  };

  const confirmUnsubscribe = () => {
    if (!unsubscribeTarget) return;
    const bundleId = unsubscribeTarget.id;
    axios
      .delete(`http://localhost:8090/bundle/${bundleId}`, { headers: { token } })
      .then(() => {
        setUserBundles((prev) => prev.filter((b) => b.id !== bundleId));
        setUnsubscribeTarget(null);
        setRightDetail(null);
        setRightTab("userBundles");
      })
      .catch((err) => {
        console.error("退订失败", err);
        setUnsubscribeTarget(null);
      });
  };

  const handleOptimizeScript = async () => {
    if (!sessionId) {
      console.error("无会话 ID");
      return;
    }
    const currentScript = scriptDetailContent || "";

    try {
      const response = await fetch(
        `http://localhost:8090/session/${sessionId}/optimize?currentScript=${encodeURIComponent(currentScript)}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            token: token || "",
          },
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      let accumulatedScript = "";

      if (reader) {
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          const chunk = decoder.decode(value, { stream: true });
          const lines = chunk.split("\n");
          for (const line of lines) {
            let data = "";
            if (line.startsWith("data:")) {
              data = line.slice(5).trimEnd().replace(/№℗/g, "\n");
            } else {
              data = line.trimEnd().replace(/№℗/g, "\n");
            }
            if (data && data !== "[DONE]") {
              accumulatedScript += data;
            }
          }
        }

        // 更新优化后的话术
        if (accumulatedScript) {
          setOptimizedScript(accumulatedScript);
          console.log("优化完成，更新话术");
        }
      }
    } catch (error) {
      console.error("话术优化失败", error);
    }
  };

  const handleAcceptCall = () => {
    console.log("📞 Starting handleAcceptCall...");
    setIncomingVisible(false);
    setCallSeconds(0);
    setCallTimerRunning(true);
    setIncomingControlsVisible(false);

    console.log("📞 Calling session/start API...");
    axios
      .post(
        "http://localhost:8090/session/start",
        {},
        {
          params: { customerId },
          headers: { token },
        }
      )
      .then((resp) => {
        console.log("📞 API response received:", resp.data);
        const sid = resp.data?.data?.id;
        if (sid) {
          console.log("📞 Setting sessionId to:", sid);
          setSessionId(sid);
          fetchSessionScripts(sid);
          fetchSessionState(sid);
        } else {
          console.error("📞 No sessionId found in API response");
        }
      })
      .catch((err) => console.error("开始会话失败", err));
  };

  useEffect(() => {
    if (!selectedScript) return;
    const existsInNode = nodeScripts.some((item) => item.id === selectedScript.id);
    const existsInCommon = commonScripts.some((item) => item.id === selectedScript.id);
    const existsInFeedback = feedbackScriptsData.some((item) => item.id === selectedScript.id);
    if (!existsInNode && !existsInCommon && !existsInFeedback) {
      setSelectedScript(null);
    }
  }, [nodeScripts, commonScripts, feedbackScriptsData, selectedScript]);

  const highlightTarget = highlightIndex >= 0 ? highlightOrder[highlightIndex] : "";
  useEffect(() => {
    if (!highlightTarget) return;
    if (highlightTarget === "反馈话术按钮") {
      setScriptGroupTab("feedback");
      return;
    }
    if (highlightTarget === "节点话术按钮") {
      setScriptGroupTab("node");
      return;
    }
    const group = getTargetScriptGroup(highlightTarget);
    if (group) {
      setScriptGroupTab(group);
    }
  }, [highlightTarget, nodeScripts, commonScripts, feedbackScriptsData]);
  useEffect(() => {
    if (highlightIndex < 0) return;
    if (lastTriggeredIndexRef.current === highlightIndex) return;
    lastTriggeredIndexRef.current = highlightIndex;
    if (!highlightTarget) return;

    if (highlightTarget === "反馈话术按钮") {
      setScriptGroupTab("feedback");
      return;
    }
    if (highlightTarget === "节点话术按钮") {
      setScriptGroupTab("node");
      return;
    }

    const matchedEdge = outEdges.find((edge) => (edge.nodeName || "").includes(highlightTarget));
    if (matchedEdge && sessionId) {
      handleTransfer(matchedEdge.edgeId);
      return;
    }

    const findEnabledScript = (items: ScriptItem[]) =>
      items.find((item) => isScriptEnabled(item) && item.name && item.name.includes(highlightTarget)) ?? null;

    const nodeMatch = findEnabledScript(nodeScripts);
    if (nodeMatch) {
      setScriptGroupTab("node");
      setSelectedScript(nodeMatch);
      return;
    }
    const feedbackMatch = findEnabledScript(feedbackScriptsData);
    if (feedbackMatch) {
      setScriptGroupTab("feedback");
      setSelectedScript(feedbackMatch);
      return;
    }
    const commonMatch = findEnabledScript(commonScripts);
    if (commonMatch) {
      setScriptGroupTab("common");
      setSelectedScript(commonMatch);
    }
  }, [highlightIndex, highlightTarget, outEdges, nodeScripts, feedbackScriptsData, commonScripts, sessionId]);

  const enabledNodeScripts = nodeScripts.filter(isScriptEnabled);
  const recommendedNodeIndex = getRecommendedIndex(enabledNodeScripts);
  const recommendedScript =
    (recommendedNodeIndex !== null ? enabledNodeScripts[recommendedNodeIndex]?.content : null) ??
    activeGroup.scripts[0];
  const scriptDetailContent = optimizedScript || (selectedScript ? selectedScript.content || "-" : recommendedScript);

  if (incomingVisible) {
    return (
      <div className={styles.page}>
        <div
          style={{
            minHeight: "100vh",
            width: "100%",
            display: "flex",
            alignItems: "flex-start",
            justifyContent: "center",
            padding: 24,
            paddingTop: 48,
          }}>
          <div
            style={{
              width: "min(1200px, 92vw)",
              display: "grid",
              gridTemplateColumns: "1fr 1fr",
              gridAutoColumns: "1fr",
              gridAutoFlow: "column",
              gap: 24,
              alignItems: "center",
            }}>
            <Card
              style={{
                background: "var(--colorNeutralBackground1)",
                border: "1px solid var(--colorNeutralStroke2)",
                borderRadius: 12,
                padding: 0,
                boxShadow: "var(--shadow4)",
                minHeight: "60vh",
              }}>
              <div
                style={{
                  background: "var(--colorNeutralBackground2)",
                  padding: "12px 16px",
                  borderTopLeftRadius: 12,
                  borderTopRightRadius: 12,
                }}>
                <Body1Strong style={{ fontSize: 16 }}>状态切换</Body1Strong>
              </div>
              <div style={{ padding: 24 }}>
                <Body1Strong style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 120, fontSize: 18 }}>
                  当前状态：
                  {statusOptions.find((s) => s.value === workStatus)?.icon}
                  {statusOptions.find((s) => s.value === workStatus)?.label ?? workStatus}
                </Body1Strong>
                <div
                  style={{
                    display: "grid",
                    gridTemplateColumns: "repeat(3, minmax(0, 1fr))",
                    gap: 12,
                  }}>
                  {statusOptions.map((status) => (
                    <Button
                      key={status.value}
                      size="small"
                      appearance={status.value === workStatus ? "primary" : "secondary"}
                      style={{
                        fontSize: 14,
                        padding: "10px 14px",
                        display: "inline-flex",
                        alignItems: "center",
                        gap: 6,
                      }}
                      onClick={() => handleStatusChange(status.value)}>
                      {status.icon}
                      {status.label}
                    </Button>
                  ))}
                </div>
              </div>
            </Card>

            {showIncomingCard && (
              <Card
                style={{
                  background: "var(--colorNeutralBackground1)",
                  border: "1px solid var(--colorNeutralStroke2)",
                  borderRadius: 12,
                  padding: 0,
                  boxShadow: "var(--shadow4)",
                  minHeight: "60vh",
                }}>
                <div
                  style={{
                    background: "var(--colorNeutralBackground2)",
                    padding: "12px 16px",
                    borderTopLeftRadius: 12,
                    borderTopRightRadius: 12,
                  }}>
                  <Body1Strong style={{ fontSize: 16 }}>来电提醒</Body1Strong>
                </div>
                <div style={{ padding: 24 }}>
                  <div style={{ display: "flex", justifyContent: "center", marginBottom: 20 }}>
                    <div
                      style={{
                        width: 80,
                        height: 80,
                        borderRadius: "50%",
                        background: "var(--colorNeutralBackground3)",
                        border: "2px dashed var(--colorNeutralStroke2)",
                        position: "relative",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                      }}>
                      <Body1Strong style={{ fontSize: 28, color: "var(--colorNeutralForeground2)" }}>
                        {customerInfo.name ? customerInfo.name.slice(0, 1) : "-"}
                      </Body1Strong>
                    </div>
                  </div>

                  <div style={{ display: "grid", rowGap: 16 }}>
                    <div
                      style={{
                        borderBottom: "1px solid var(--colorNeutralStroke2)",
                        paddingBottom: 12,
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        justifyContent: "center",
                      }}>
                      <Body1Strong
                        style={{
                          fontSize: 16,
                          display: "block",
                          color: "var(--colorNeutralForeground2)",
                          textAlign: "center",
                        }}>
                        电话号码
                      </Body1Strong>
                      <Body1Strong style={{ display: "block", textAlign: "center" }}>
                        {customerInfo.phone || "-"}
                      </Body1Strong>
                    </div>
                    <div
                      style={{
                        borderBottom: "1px solid var(--colorNeutralStroke2)",
                        paddingBottom: 12,
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        justifyContent: "center",
                      }}>
                      <Body1Strong
                        style={{
                          fontSize: 16,
                          display: "block",
                          color: "var(--colorNeutralForeground2)",
                          textAlign: "center",
                        }}>
                        客户
                      </Body1Strong>
                      <Body1Strong style={{ display: "block", textAlign: "center" }}>
                        {customerInfo.name || "-"}
                      </Body1Strong>
                    </div>
                  </div>

                  {incomingControlsVisible ? (
                    <div style={{ display: "flex", justifyContent: "space-between", marginTop: 20 }}>
                      <Button
                        appearance="secondary"
                        icon={<CallEnd20Regular />}
                        style={{ flex: 1, marginRight: 8 }}
                        onClick={() => {
                          setCustomerInfo((prev) => ({ ...prev, name: "", phone: "" }));
                          setCallTimerRunning(false);
                          setCallSeconds(0);
                          setIncomingControlsVisible(false);
                        }}>
                        挂断
                      </Button>
                      <Button
                        appearance="primary"
                        icon={<CheckmarkCircle20Regular />}
                        style={{ flex: 1, marginLeft: 8 }}
                        onClick={handleAcceptCall}>
                        接听
                      </Button>
                    </div>
                  ) : (
                    <Caption1 className={styles.panelMeta} style={{ marginTop: 16 }}>
                      本次来电已处理
                    </Caption1>
                  )}
                </div>
              </Card>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      {unsubscribeTarget && (
        <div
          style={{
            position: "fixed",
            inset: 0,
            background: "rgba(0,0,0,0.35)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 3000,
          }}>
          <Card style={{ width: 360, padding: 20, boxShadow: "0 12px 32px rgba(0,0,0,0.2)" }}>
            <Body1Strong style={{ display: "block", marginBottom: 12 }}>确认退订</Body1Strong>
            <Body1 style={{ marginBottom: 16 }}>确定要退订 {unsubscribeTarget.name || "该套餐"} 吗？</Body1>
            <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
              <Button appearance="secondary" onClick={() => setUnsubscribeTarget(null)}>
                取消
              </Button>
              <Button appearance="primary" onClick={confirmUnsubscribe}>
                确定
              </Button>
            </div>
          </Card>
        </div>
      )}
      <div className={styles.header}>
        <div className={styles.headerTitle}>
          <div className={styles.headerMeta}>
            <Badge color="success" icon={<CheckmarkCircle20Regular />}>
              通话中
            </Badge>
            <Body1 style={{ display: "inline-flex", alignItems: "center", gap: 6, verticalAlign: "middle" }}>
              <Clock20Regular />{" "}
              {`${String(Math.floor(callSeconds / 60)).padStart(2, "0")}:${String(callSeconds % 60).padStart(2, "0")}`}
            </Body1>
            <Badge color="informative">满意度预测 92%</Badge>
          </div>
        </div>
        <div className={styles.actionGroup}>
          <Button icon={<Pause20Regular />} appearance="secondary" className={styles.actionButton}>
            保持
          </Button>
          <Button icon={<MicOff20Regular />} appearance="secondary" className={styles.actionButton}>
            静音
          </Button>
          <Button
            icon={<CallEnd20Regular />}
            appearance="primary"
            className={styles.actionButton}
            onClick={() => {
              setCallTimerRunning(false);
              setCallSeconds(0);
              setIncomingVisible(true);
              setShowIncomingCard(true);
              setIncomingControlsVisible(false);
              setCustomerInfo((prev) => ({ ...prev, name: "", phone: "" }));
            }}>
            结束通话
          </Button>
        </div>
      </div>

      <div className={mergeClasses(styles.grid, styles.mobileStack)}>
        <div className={styles.leftColumn}>
          <Card
            className={mergeClasses(styles.card, styles.customerCard, styles.delay1)}
            style={{ minHeight: "60vh", maxHeight: "60vh", display: "flex", flexDirection: "column" }}>
            <CardHeader header={<Body1Strong className={styles.cardTitle}>客户信息</Body1Strong>} />
            <div className={mergeClasses(styles.cardBody, styles.cardBodyTight)} style={{ overflowY: "auto", flex: 1 }}>
              <div className={styles.customerAvatarWrap}>
                <div className={styles.customerAvatar}>{customerInfo.name ? customerInfo.name.slice(0, 1) : "客"}</div>
              </div>
              <div className={styles.statusStrip} style={{ flexWrap: "nowrap" }}>
                <Badge icon={<Person20Regular />}>{customerInfo.name}</Badge>
                <Badge color="success">{getStatusLabel(customerInfo.status)}</Badge>
                <Badge color="informative">个人用户</Badge>
              </div>
              <div className={`${styles.infoGrid} ${styles.infoStack}`}>
                <div className={styles.infoItem}>
                  <Caption1 className={styles.infoLabel}>手机号</Caption1>
                  <Body1 className={styles.infoValue}>{customerInfo.phone}</Body1>
                </div>
                <div className={styles.infoItem}>
                  <Caption1 className={styles.infoLabel}>姓名</Caption1>
                  <Body1 className={styles.infoValue}>{customerInfo.name}</Body1>
                </div>
                <div className={styles.infoItem}>
                  <Caption1 className={styles.infoLabel}>状态</Caption1>
                  <Body1 className={styles.infoValue}>{getStatusLabel(customerInfo.status)}</Body1>
                </div>
                <div className={styles.infoItem}>
                  <Caption1 className={styles.infoLabel}>客户类型</Caption1>
                  <Body1 className={styles.infoValue}>个人用户</Body1>
                </div>
                <div className={styles.infoItem}>
                  <Caption1 className={styles.infoLabel}>账户余额</Caption1>
                  <Body1 className={styles.infoValue}>￥{customerInfo.balance.toFixed(2)}</Body1>
                </div>
                <div className={styles.infoItem}>
                  <Caption1 className={styles.infoLabel}>剩余流量</Caption1>
                  <Body1 className={styles.infoValue}>
                    {customerInfo.remainingDataCount !== null && customerInfo.remainingDataCount !== undefined
                      ? `${customerInfo.remainingDataCount} GB`
                      : "-"}
                  </Body1>
                </div>
              </div>
            </div>
          </Card>
          <Card
            className={mergeClasses(styles.card, styles.delay2)}
            style={{ height: "18vh", display: "flex", flexDirection: "column" }}>
            <CardHeader header={<Body1Strong className={styles.cardTitle}>通话状态</Body1Strong>} />
            <div className={mergeClasses(styles.cardBody, styles.cardBodyTight)} style={{ overflowY: "auto", flex: 1 }}>
              <div className={styles.progressGroup}>
                <div className={styles.metricRow}>
                  <Body1>客户满意度</Body1>
                  <Body1Strong>{customerSatisfaction}%</Body1Strong>
                </div>
                <ProgressBar value={customerSatisfaction} max={100} thickness="medium" />
                <div className={styles.metricRow}>
                  <Body1>当前话术匹配度</Body1>
                  <Body1Strong>{scriptMatch}%</Body1Strong>
                </div>
                <ProgressBar value={scriptMatch} max={100} thickness="medium" />
              </div>
            </div>
          </Card>
        </div>

        <div className={styles.column}>
          <Card
            className={mergeClasses(styles.card, styles.scriptCard, styles.delay3)}
            style={{ display: "flex", flexDirection: "column", height: "66vh" }}>
            <CardHeader
              header={
                <Body1Strong className={mergeClasses(styles.scriptHeaderText, styles.cardTitle)}>
                  智能体话术
                </Body1Strong>
              }
            />
            <div
              className={mergeClasses(styles.cardBody, styles.cardBodyTight, styles.scriptBody, styles.scriptPanel)}
              style={{ overflowY: "auto", flex: 1 }}>
              <div className={styles.scriptMain}>
                <div>
                  <div
                    className={styles.scriptHighlight}
                    style={{ fontSize: 15, lineHeight: "22px", whiteSpace: "pre-wrap", position: "relative" }}>
                    <Button
                      appearance="subtle"
                      size="small"
                      icon={<Sparkle20Regular />}
                      className={styles.scriptAiButton}
                      aria-label="AI 助手"
                      onClick={handleOptimizeScript}
                      style={{ position: "absolute", top: 8, right: 8 }}
                    />
                    {scriptDetailContent}
                  </div>
                </div>

                <div className={styles.nextScriptSection}>
                  <div className={styles.scriptGroupTabs}>
                    <button
                      type="button"
                      className={mergeClasses(
                        styles.scriptGroupTab,
                        scriptGroupTab === "node" && styles.scriptGroupTabActive,
                        highlightTarget === "节点话术按钮" && styles.scriptGroupTabGuide
                      )}
                      onClick={() => setScriptGroupTab("node")}>
                      <span className={styles.scriptGroupTabIcon}>
                        <Box20Regular />
                      </span>
                      节点话术
                    </button>
                    <button
                      type="button"
                      className={mergeClasses(
                        styles.scriptGroupTab,
                        scriptGroupTab === "common" && styles.scriptGroupTabActive
                      )}
                      onClick={() => setScriptGroupTab("common")}>
                      <span className={styles.scriptGroupTabIcon}>
                        <People20Regular />
                      </span>
                      通用话术
                    </button>
                    <button
                      type="button"
                      className={mergeClasses(
                        styles.scriptGroupTab,
                        scriptGroupTab === "feedback" && styles.scriptGroupTabActive,
                        highlightTarget === "反馈话术按钮" && styles.scriptGroupTabGuide
                      )}
                      onClick={() => setScriptGroupTab("feedback")}>
                      <span className={styles.scriptGroupTabIcon}>
                        <Chat20Regular />
                      </span>
                      反馈话术
                    </button>
                  </div>
                  {scriptGroupTab === "node" && (
                    <div className={styles.scriptListStack}>
                      {nodeScripts.filter(isScriptEnabled).length === 0 ? (
                        <Caption1 className={styles.panelMeta} style={{ fontSize: 13, lineHeight: "10px" }}>
                          暂无节点话术
                        </Caption1>
                      ) : (
                        (() => {
                          const items = nodeScripts.filter(isScriptEnabled);
                          const recommendedIndex = highlightTarget ? null : getRecommendedIndex(items);
                          return items.map((item, index) => (
                            <div
                              key={`node-${item.id || index}`}
                              className={mergeClasses(
                                styles.scriptItemRow,
                                recommendedIndex === index && styles.scriptItemRowRecommended,
                                isHighlightName(item.name) && styles.scriptItemRowRecommended,
                                selectedScript?.id === item.id && styles.scriptItemRowActive
                              )}
                              onClick={() => setSelectedScript(item)}>
                              <Body1 className={styles.scriptItemName}>{item.name || "未命名话术"}</Body1>
                              <Caption1 className={styles.scriptItemSummary}>{item.summary || "-"}</Caption1>
                            </div>
                          ));
                        })()
                      )}
                    </div>
                  )}
                  {scriptGroupTab === "common" && (
                    <div className={styles.scriptListStack}>
                      {commonScripts.filter(isScriptEnabled).length === 0 ? (
                        <Caption1 className={styles.panelMeta} style={{ fontSize: 13, lineHeight: "10px" }}>
                          暂无通用话术
                        </Caption1>
                      ) : (
                        (() => {
                          const items = commonScripts.filter(isScriptEnabled);
                          const recommendedIndex = highlightTarget ? null : getRecommendedIndex(items);
                          return items.map((item, index) => (
                            <div
                              key={`common-${item.id || index}`}
                              className={mergeClasses(
                                styles.scriptItemRow,
                                recommendedIndex === index && styles.scriptItemRowRecommended,
                                isHighlightName(item.name) && styles.scriptItemRowRecommended,
                                selectedScript?.id === item.id && styles.scriptItemRowActive
                              )}
                              onClick={() => setSelectedScript(item)}>
                              <Body1 className={styles.scriptItemName}>{item.name || "未命名话术"}</Body1>
                              <Caption1 className={styles.scriptItemSummary}>{item.summary || "-"}</Caption1>
                            </div>
                          ));
                        })()
                      )}
                    </div>
                  )}
                  {scriptGroupTab === "feedback" && (
                    <div className={styles.scriptListStack}>
                      {feedbackScriptsData.filter(isScriptEnabled).length === 0 ? (
                        <Caption1 className={styles.panelMeta} style={{ fontSize: 13, lineHeight: "10px" }}>
                          暂无反馈话术
                        </Caption1>
                      ) : (
                        (() => {
                          const items = feedbackScriptsData.filter(isScriptEnabled);
                          const recommendedIndex = highlightTarget ? null : getRecommendedIndex(items);
                          return items.map((item, index) => (
                            <div
                              key={`feedback-${item.id || index}`}
                              className={mergeClasses(
                                styles.scriptItemRow,
                                recommendedIndex === index && styles.scriptItemRowRecommended,
                                isHighlightName(item.name) && styles.scriptItemRowRecommended,
                                selectedScript?.id === item.id && styles.scriptItemRowActive
                              )}
                              onClick={() => setSelectedScript(item)}>
                              <Body1 className={styles.scriptItemName}>{item.name || "未命名话术"}</Body1>
                              <Caption1 className={styles.scriptItemSummary}>{item.summary || "-"}</Caption1>
                            </div>
                          ));
                        })()
                      )}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </Card>
          <Card
            className={mergeClasses(styles.card, styles.delay4)}
            style={{ height: "10vh", display: "flex", flexDirection: "column" }}>
            <div
              className={mergeClasses(styles.cardBody, styles.cardBodyTight)}
              style={{ overflowY: "auto", flex: 1, padding: "12px 16px", display: "flex", alignItems: "center" }}>
              {outEdges.length > 0 || Boolean(undoNodeId) ? (
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    gap: 8,
                    width: "100%",
                  }}>
                  <div style={{ minWidth: 80 }}>
                    {undoNodeId ? (
                      <Button
                        appearance="outline"
                        className={styles.returnButton}
                        icon={<ArrowLeft20Regular />}
                        onClick={handleUndoTransfer}>
                        返回
                      </Button>
                    ) : null}
                  </div>
                  <div style={{ display: "flex", flexWrap: "wrap", gap: 8, justifyContent: "center", flex: 1 }}>
                    {outEdges
                      .filter((edge) => !(edge.nodeName || "").includes("结束"))
                      .map((edge) => (
                        <Button
                          key={edge.edgeId || edge.nodeId}
                          appearance="secondary"
                          className={mergeClasses(isHighlightName(edge.nodeName) && styles.nextStepButtonRecommended)}
                          icon={<ArrowRight20Regular />}
                          onClick={() => handleTransfer(edge.edgeId)}>
                          {edge.nodeName || "下一步"}
                        </Button>
                      ))}
                  </div>
                  <div style={{ minWidth: 100, display: "flex", justifyContent: "flex-end" }}>
                    {outEdges
                      .filter((edge) => (edge.nodeName || "").includes("结束"))
                      .map((edge) => (
                        <Button
                          key={edge.edgeId || edge.nodeId}
                          appearance="primary"
                          className={mergeClasses(isHighlightName(edge.nodeName) && styles.nextStepButtonRecommended)}
                          icon={<DismissCircle20Regular />}
                          onClick={() => handleTransfer(edge.edgeId)}>
                          {edge.nodeName || "结束"}
                        </Button>
                      ))}
                  </div>
                </div>
              ) : (
                <Caption1 className={styles.panelMeta}>暂无下一步</Caption1>
              )}
            </div>
          </Card>
        </div>

        <div className={styles.column}>
          <div className={styles.infoPanelLayout}>
            <Card
              className={mergeClasses(styles.card, styles.contextCard, styles.delay4)}
              style={{ minHeight: "79.7vh", height: "calc(79.7vh)" }}>
              <CardHeader
                header={
                  <div
                    style={{ display: "flex", alignItems: "center", justifyContent: "space-between", width: "100%" }}>
                    <Body1Strong className={styles.cardTitle}>
                      {rightDetail?.type ? (
                        <span style={{ display: "inline-flex", alignItems: "center", gap: 8 }}>
                          <Button
                            appearance="subtle"
                            icon={<ArrowLeft20Regular />}
                            size="small"
                            onClick={handleDetailBack}
                          />
                          {rightDetail.type === "consumption"
                            ? "消费记录 > 消费记录详情"
                            : rightDetail.type === "maintenance"
                            ? "维修记录 > 维修记录详情"
                            : rightDetail.type === "maintenanceCreate"
                            ? "维修记录 > 新建维修"
                            : rightDetail.type === "bundleUser"
                            ? "用户套餐 > 用户套餐详情"
                            : rightDetail.type === "bundleAll"
                            ? "所有套餐 > 套餐详情"
                            : "历史服务 > 历史服务详情"}
                        </span>
                      ) : rightTab === "context" ? (
                        "对话记录"
                      ) : rightTab === "consumption" ? (
                        "消费记录"
                      ) : rightTab === "maintenance" ? (
                        "维修记录"
                      ) : rightTab === "userBundles" ? (
                        "用户套餐"
                      ) : rightTab === "allBundles" ? (
                        "所有套餐"
                      ) : (
                        "历史服务"
                      )}
                    </Body1Strong>
                    {rightTab === "maintenance" && !rightDetail ? (
                      <Button
                        size="small"
                        appearance="primary"
                        icon={<Add20Regular />}
                        onClick={handleCreateMaintenance}>
                        新建
                      </Button>
                    ) : null}
                  </div>
                }
              />
              <div className={mergeClasses(styles.cardBody, styles.cardBodyTight, styles.contextBody)}>
                {renderRightContent()}
              </div>
            </Card>
            <div className={styles.infoPanelSidebar}>
              {[
                { key: "context", label: "对话记录", icon: <Chat20Regular /> },
                { key: "consumption", label: "消费记录", icon: <Receipt20Regular /> },
                { key: "maintenance", label: "维修记录", icon: <Wrench20Regular /> },
                { key: "userBundles", label: "用户套餐", icon: <People20Regular /> },
                { key: "allBundles", label: "所有套餐", icon: <Box20Regular /> },
                { key: "history", label: "历史服务", icon: <History20Regular /> },
              ].map((item) => (
                <Tooltip key={item.key} content={item.label} relationship="label" positioning="before">
                  <Button
                    appearance="subtle"
                    className={mergeClasses(
                      styles.infoPanelButton,
                      rightTab === item.key && styles.infoPanelButtonActive
                    )}
                    icon={item.icon}
                    aria-label={item.label}
                    onClick={() => {
                      setRightDetail(null);
                      setRightTab(item.key as typeof rightTab);
                    }}
                  />
                </Tooltip>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
