import {
  Button,
  Card,
  Dialog,
  DialogActions,
  DialogBody,
  DialogSurface,
  DialogTitle,
  Dropdown,
  Input,
  InteractionTag,
  InteractionTagPrimary,
  InteractionTagSecondary,
  Label,
  MessageBar,
  MessageBarBody,
  Option,
  SearchBox,
  SpinButton,
  Spinner,
  Subtitle2,
  TagGroup,
  Text,
  Textarea,
  Title1,
} from "@fluentui/react-components";
import {
  Add24Regular,
  CheckmarkCircle20Color,
  CodeBlock48Color,
  Delete24Regular,
  DismissCircle20Color,
  SlideTextSparkle48Color,
} from "@fluentui/react-icons";
import React, { useCallback, useEffect, useRef, useState } from "react";
import { useStyles } from "../styles/ScriptConfigurationStyle";
import { operationTypeLabels } from "../types/IOperationLog";
import type { ICondition, IScript } from "../types/IScript";
import api from "../utils/Api";
import {
  ConditionsDisplayToKeyConverter,
  ConditionsKeyToDisplayConverter,
  StringDisplayToKeyConverter,
  StringKeyToDisplayConverter,
} from "../utils/ConditionConverter";
import { BackendTagToFrontendConverter, FrontendTagToBackendConverter } from "../utils/ScriptTagConverter";
import ConditionNode from "./ConditionNode";

interface IFraudCase {
  id: string;
  case_id: string;
  title: string;
  target_group: string;
  fraud_type: string;
  scenario: string;
  process: string;
  key_features: string;
  risk_level: "低" | "中" | "高" | "极高";
  amount_involved: string;
  region: string;
  case_year: string;
  case_source: string;
  source_url: string;
  created_at: string;
}

export default function ScriptConfigurationView() {
  const styles = useStyles();
  const [isAutoRunning, setIsAutoRunning] = useState(false);
  const [isCrawling, setIsCrawling] = useState(false);

  // ===== [AUTO-EVOLUTION] 设置弹窗 =====
  const [isSettingOpen, setIsSettingOpen] = useState(false);

  // ===== [AUTO-EVOLUTION] 爬虫表单配置 =====
  const [crawlConfig, setCrawlConfig] = useState({
    urls: "https://www.people.com.cn/\nhttps://www.xinhuanet.com/",
    model: "gpt-4",
    temperature: 0.7,
    prompt: "请提取诈骗类型、风险等级和关键特征",
    extract_fields: "fraud_type, risk_level, key_features"
  });

  // ===== [AUTO-EVOLUTION] 替换 scripts =====
  const [cases, setCases] = useState<IFraudCase[]>([]);
  const [selectedCaseId, setSelectedCaseId] = useState<string | null>(null);

  // ===== [AUTO-EVOLUTION] 新增：爬虫配置 =====
  const [crawlUrl, setCrawlUrl] = useState("");
  const [crawlInterval, setCrawlInterval] = useState(60); // 秒
  const crawlTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  
  // ====== 案例池 ======
  const mockCasePool: Omit<IFraudCase, "id" | "created_at">[] = [
    {
      case_id: "CASE_AI",
      title: "利用AI生成假图骗“仅退款”，新型电商欺诈侵蚀信任",
      target_group: "线上购物消费者",
      fraud_type: "AI造假诈骗 / 虚假售后",
      scenario: "电商平台",
      process: "利用AI生成商品瑕疵图片申请仅退款",
      key_features: '["AI生成","图片伪造","仅退款"]',
      risk_level: "中" as any,
      amount_involved: "几十至数百元",
      region: "全国",
      case_year: "2025-2026",
      case_source: "新华网",
      source_url: "https://www.xinhuanet.com/",
    },
    {
      case_id: "CASE_SPRING",
      title: "春节诈骗：假礼券、退改签、红包链接陷阱",
      target_group: "春节出行人群",
      fraud_type: "节日诈骗",
      scenario: "社交+电话",
      process: "通过红包链接、伪客服诱导诈骗",
      key_features: '["节假日","钓鱼链接","伪客服"]',
      risk_level: "高",
      amount_involved: "数千至数万",
      region: "全国",
      case_year: "2026",
      case_source: "人民日报",
      source_url: "https://www.people.com.cn/",
    },
    {
      case_id: "CASE_OLD",
      title: "“稳赚不赔”投资骗局瞄准老年人",
      target_group: "老年人",
      fraud_type: "投资诈骗",
      scenario: "社交平台",
      process: "情感诱导+虚假投资平台",
      key_features: '["高收益","情感诈骗","虚假APP"]',
      risk_level: "高",
      amount_involved: "数万至百万",
      region: "全国",
      case_year: "2025-2026",
      case_source: "人民日报",
      source_url: "https://www.people.com.cn/",
    },
  ];

  // ====== Mock生成 ======
  function generateMockCase(): IFraudCase {
    const base = mockCasePool[Math.floor(Math.random() * mockCasePool.length)];

    return {
      ...base,
      id: Date.now().toString() + Math.random(),
      case_id: base.case_id + "_" + Date.now(),
      created_at: new Date().toLocaleString(),
    };
  }

  // ====== 自动爬取（每次1条） ======
  function startAutoCrawl() {
    if (crawlTimerRef.current) clearInterval(crawlTimerRef.current);

    crawlTimerRef.current = setInterval(() => {
      const newCase = generateMockCase();

      setCases((prev) => {
        const updated = [newCase, ...prev];
        return updated.slice(0, 50); // 防爆
      });
    }, 2000); // 每2秒1条

    setIsAutoRunning(true);
  }

  function stopAutoCrawl() {
    if (crawlTimerRef.current) {
      clearInterval(crawlTimerRef.current);
      crawlTimerRef.current = null;
    }
    setIsAutoRunning(false);
  }

  // ====== 保存函数 ======
  function handleSaveCase() {
    if (!selectedCase) return;

    try {
      JSON.parse(selectedCase.key_features);
    } catch {
      showError("关键特征必须为合法JSON");
      return;
    }

    alert("保存成功 ✅");
  }

  function showError(message: string) {
    setErrorMessage(message);
    setTimeout(() => setErrorMessage(null), 3000);
  }

  // ===== [AUTO-EVOLUTION] 手动爬取 =====
  async function handleManualCrawl() {
    setIsCrawling(true);
    await new Promise((r) => setTimeout(r, 3000));
    const newCase = generateMockCase();
    setCases((prev) => [newCase, ...prev]);
    setIsCrawling(false);
  }

  // ===== [AUTO-EVOLUTION] 删除案例 =====
  function handleDeleteCase(id: string) {
    setCases((prev) => prev.filter((c) => c.id !== id));
    if (selectedCaseId === id) setSelectedCaseId(null);
  }

  const selectedCase = cases.find((c) => c.id === selectedCaseId);

  return (
    <div className={styles.container}>
      {errorMessage && (
        <MessageBar intent="error">
          <MessageBarBody>{errorMessage}</MessageBarBody>
        </MessageBar>
      )}
      
      {/* ===== 自动爬取配置表单弹窗 ===== */}
      {isSettingOpen && (
        <Dialog open onOpenChange={(_, data) => setIsSettingOpen(data.open)}>
          <DialogSurface>
            <DialogTitle>自动爬取配置</DialogTitle>
            <DialogBody style={{ display: "flex", flexDirection: "column", gap: "16px", marginTop: "12px" }}>
              <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                <Label><strong>目标URL列表 (每行一个)</strong></Label>
                <Textarea
                  rows={3}
                  placeholder="https://example.com/fraud1"
                  value={crawlConfig.urls}
                  onChange={(_, d) => setCrawlConfig({ ...crawlConfig, urls: d.value })}
                />
              </div>

              <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                <Label><strong>提取模型</strong></Label>
                <Dropdown
                  value={crawlConfig.model}
                  selectedOptions={[crawlConfig.model]}
                  onOptionSelect={(_, d) => setCrawlConfig({ ...crawlConfig, model: d.optionValue as string })}
                >
                  <Option value="gpt-4">gpt-4</Option>
                  <Option value="gpt-3.5-turbo">gpt-3.5-turbo</Option>
                  <Option value="claude-3-opus">claude-3-opus</Option>
                  <Option value="gemini-pro">gemini-pro</Option>
                </Dropdown>
              </div>

              <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                <Label><strong>随机性 (Temperature)</strong></Label>
                <SpinButton
                  value={crawlConfig.temperature}
                  step={0.1}
                  min={0}
                  max={2}
                  onChange={(_, d) => {
                    if (d.value !== undefined) {
                      setCrawlConfig({ ...crawlConfig, temperature: 0.1 });
                    } else if (d.displayValue !== undefined) {
                      setCrawlConfig({ ...crawlConfig, temperature: parseFloat(d.displayValue) || 0 });
                    }
                  }}
                />
              </div>

              <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                <Label><strong>提取要求 (Prompt)</strong></Label>
                <Textarea
                  rows={2}
                  value={crawlConfig.prompt}
                  onChange={(_, d) => setCrawlConfig({ ...crawlConfig, prompt: d.value })}
                />
              </div>

              <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                <Label><strong>目标字段 (用逗号分隔)</strong></Label>
                <Input
                  value={crawlConfig.extract_fields}
                  onChange={(_, d) => setCrawlConfig({ ...crawlConfig, extract_fields: d.value })}
                />
              </div>
            </DialogBody>
            <DialogActions style={{ marginTop: "24px" }}>
              <Button onClick={() => setIsSettingOpen(false)}>取消</Button>
              <Button
                appearance="primary"
                onClick={() => {
                  // 这里可以添加对表单内容的验证逻辑，验证通过后保存并关闭
                  console.log("保存的配置信息: ", crawlConfig);
                  setIsSettingOpen(false);
                }}
              >
                保存配置
              </Button>
            </DialogActions>
          </DialogSurface>
        </Dialog>
      )}

      {/* ===== Header ===== */}
      <div className={styles.header}>
        <Title1>自进化反诈模块</Title1>
      </div>

      {/* ===== 爬虫配置卡片 ===== */}
      <Card
        style={{
          marginBottom: 12,
          position: "relative",
          paddingBottom: 48,
        }}
      >
        <Subtitle2 style={{ marginBottom: 8, borderBottom: "1px solid #eee", paddingBottom: 4 }}>
          <strong>📡 数据采集配置</strong>
        </Subtitle2>
        <Label><strong>目标爬虫URL</strong></Label>
        <Input
          value={crawlUrl}
          onChange={(_, d) => setCrawlUrl(d.value)}
          placeholder="输入反诈网站URL"
        />
        <Button
          appearance="primary"
          onClick={handleManualCrawl}
          disabled={isCrawling}
          style={{
            position: "absolute",
            right: 12,
            bottom: 12,
          }}
        >
          {isCrawling ? "正在抓取中..." : "手动提取数据"}
        </Button>
      </Card>
          
      <div className={styles.mainContent}>
        {/* ===== 左侧区域 ===== */}
        <div className={styles.leftPanel} style={{ display: "flex", flexDirection: "column", gap: 12 }}>
          
          {/* 案例流卡片 */}
          <Card style={{ flex: 1, overflowY: "auto", padding: 0 }}>
            <div style={{ padding: "12px 16px", borderBottom: "1px solid #eaeaea", backgroundColor: "#fdfdfd" }}>
              <Subtitle2><strong>📑 实时案例数据流</strong></Subtitle2>
            </div>
            {cases.length === 0 && (
              <div style={{ padding: 16, color: "#888", textAlign: "center" }}>暂无采集数据</div>
            )}
            {cases.map((item) => (
              <Card
                key={item.id}
                className={styles.scriptItem}
                onClick={() => setSelectedCaseId(item.id)}
                style={{
                  margin: "8px 12px",
                  cursor: "pointer",
                  border: selectedCaseId === item.id ? "1px solid #0078d4" : "1px solid transparent",
                  backgroundColor: selectedCaseId === item.id ? "#f3f9fd" : undefined
                }}
              >
                <div className={styles.scriptActions}>
                  <Button
                    size="small"
                    appearance="transparent"
                    icon={<Delete24Regular />}
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDeleteCase(item.id);
                    }}
                  />
                </div>
                <div>
                  <div style={{ fontWeight: selectedCaseId === item.id ? "bold" : "normal" }}>
                    {item.title}
                  </div>
                  <div style={{ fontSize: 12, opacity: 0.6, marginTop: 4 }}>
                    {item.fraud_type} · {item.risk_level}风险
                  </div>
                </div>
              </Card>
            ))}
          </Card>

          {/* 底部操作卡片 */}
          <Card>
            <Subtitle2 style={{ marginBottom: 12 }}>
              <strong>⚙️ 自动化系统控制</strong>
            </Subtitle2>
            <div style={{ display: "flex", gap: 8, justifyContent: "space-between" }}>
              <Button
                style={{ flex: 1, fontWeight: isAutoRunning ? "bold" : "normal" }}
                appearance={isAutoRunning ? "primary" : "outline"}
                onClick={() => {
                  if (isAutoRunning) stopAutoCrawl();
                  else startAutoCrawl();
                }}
              >
                {isAutoRunning ? "停止自动演进" : "启动自动演进"}
              </Button>
              <Button style={{ flex: 1 }} onClick={() => setIsSettingOpen(true)}>
                策略设置
              </Button>
            </div>
          </Card>
        </div>

        {/* ===== 右侧区域：案例详情 ===== */}
        <Card className={styles.rightPanel}>
          {!selectedCase ? (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%', color: '#666' }}>
              <Subtitle2>👈 请从左侧选择一个案例以查看和编辑详情</Subtitle2>
            </div>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
              <Subtitle2 style={{ borderBottom: "2px solid #0078d4", paddingBottom: 8, color: "#0078d4" }}>
                <strong>🔍 结构化案例详情审核</strong>
              </Subtitle2>
              
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
                {/* 基础信息 */}
                <div className={styles.fieldGroup}>
                  <Label><strong>案例编号</strong></Label>
                  <Input
                    value={selectedCase.case_id}
                    onChange={(_, d) =>
                      setCases((prev) => prev.map((c) => c.id === selectedCase.id ? { ...c, case_id: d.value } : c))
                    }
                  />
                </div>

                <div className={styles.fieldGroup}>
                  <Label><strong>案例标题</strong></Label>
                  <Input
                    value={selectedCase.title}
                    onChange={(_, d) =>
                      setCases((prev) => prev.map((c) => c.id === selectedCase.id ? { ...c, title: d.value } : c))
                    }
                  />
                </div>

                <div className={styles.fieldGroup}>
                  <Label><strong>诈骗类型</strong></Label>
                  <Input
                    value={selectedCase.fraud_type}
                    onChange={(_, d) =>
                      setCases((prev) => prev.map((c) => c.id === selectedCase.id ? { ...c, fraud_type: d.value } : c))
                    }
                  />
                </div>

                <div className={styles.fieldGroup}>
                  <Label><strong>目标群体</strong></Label>
                  <Input
                    value={selectedCase.target_group}
                    onChange={(_, d) =>
                      setCases((prev) => prev.map((c) => c.id === selectedCase.id ? { ...c, target_group: d.value } : c))
                    }
                  />
                </div>
              </div>

              {/* 核心特征分析 */}
              <div style={{ marginTop: 8, padding: 12, backgroundColor: "#fff9f9", borderRadius: 4, border: "1px solid #ffdcdc" }}>
                <Subtitle2 style={{ marginBottom: 12, color: "#d13438" }}><strong>🚨 核心风险分析 (审核重点)</strong></Subtitle2>
                
                <div className={styles.fieldGroup} style={{ marginBottom: 12 }}>
                  <Label><strong>诈骗经过</strong></Label>
                  <Textarea
                    style={{ fontSize: 14, fontWeight: 500 }}
                    value={selectedCase.process}
                    onChange={(_, d) =>
                      setCases((prev) => prev.map((c) => c.id === selectedCase.id ? { ...c, process: d.value } : c))
                    }
                    rows={4}
                  />
                </div>

                <div className={styles.fieldGroup} style={{ marginBottom: 12 }}>
                  <Label><strong>关键特征 (JSON提取)</strong></Label>
                  <Textarea
                    style={{ fontFamily: "monospace" }}
                    value={selectedCase.key_features}
                    onChange={(_, d) =>
                      setCases((prev) => prev.map((c) => c.id === selectedCase.id ? { ...c, key_features: d.value } : c))
                    }
                  />
                </div>

                <div className={styles.fieldGroup}>
                  <Label><strong>风险等级判定</strong></Label>
                  <Dropdown
                    selectedOptions={[selectedCase.risk_level]}
                    onOptionSelect={(_, d) =>
                      setCases((prev) => prev.map((c) => c.id === selectedCase.id ? { ...c, risk_level: d.optionValue as any } : c))
                    }
                  >
                    <Option value="低">低</Option>
                    <Option value="中">中</Option>
                    <Option value="高">高</Option>
                    <Option value="极高">极高</Option>
                  </Dropdown>
                </div>
              </div>

              {/* 来源与辅助信息 */}
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16, marginTop: 8 }}>
                <div className={styles.fieldGroup}>
                  <Label><strong>发生地区</strong></Label>
                  <Input
                    value={selectedCase.region}
                    onChange={(_, d) =>
                      setCases((prev) => prev.map((c) => c.id === selectedCase.id ? { ...c, region: d.value } : c))
                    }
                  />
                </div>

                <div className={styles.fieldGroup}>
                  <Label><strong>涉案金额</strong></Label>
                  <Input
                    value={selectedCase.amount_involved}
                    onChange={(_, d) =>
                      setCases((prev) => prev.map((c) => c.id === selectedCase.id ? { ...c, amount_involved: d.value } : c))
                    }
                  />
                </div>

                <div className={styles.fieldGroup}>
                  <Label><strong>来源机构</strong></Label>
                  <Input
                    value={selectedCase.case_source}
                    onChange={(_, d) =>
                      setCases((prev) => prev.map((c) => c.id === selectedCase.id ? { ...c, case_source: d.value } : c))
                    }
                  />
                </div>

                <div className={styles.fieldGroup}>
                  <Label><strong>来源URL</strong></Label>
                  <Input
                    value={selectedCase.source_url}
                    onChange={(_, d) =>
                      setCases((prev) => prev.map((c) => c.id === selectedCase.id ? { ...c, source_url: d.value } : c))
                    }
                  />
                </div>
              </div>

              <div className={styles.fieldGroup} style={{ marginTop: 8 }}>
                <Label><strong>系统录入时间</strong></Label>
                <Text style={{ color: "#666" }}>{selectedCase.created_at}</Text>
              </div>
              <Button
                appearance="primary"
                style={{ marginTop: 12, alignSelf: "flex-end" }}
                onClick={handleSaveCase}
              >
                💾 保存审核结果
              </Button>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}