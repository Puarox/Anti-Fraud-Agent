import * as echarts from "echarts";
import { useEffect, useRef, useState } from "react";
import { dashboardStyles } from "../styles/DashboardStyle";
import type { IDashboardData } from "../types/IDashBoardData";

export default function DashboardPage() {
  const [currentTime, setCurrentTime] = useState("");
  const [dashboardData, setDashboardData] = useState<IDashboardData>({
    onlineUserCount: 247,
    callToday: 1824,
    agentScript: 68.5,
    agentOptimize: 2.4,
    agentNavigate: 92.7,
    concurrentCalls: 86,
    waitingCalls: 12,
    answerRate: 94.2,
    avgWaitTime: "5m 0s",
    maxWaitTime: "8m 24s",
    waitTrend: "-5s",
  });

  const sessionChartRef = useRef<HTMLDivElement>(null);
  const businessChartRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      const timeString =
        now.toLocaleDateString("zh-CN", {
          year: "numeric",
          month: "long",
          day: "numeric",
          weekday: "long",
        }) +
        " " +
        now.toLocaleTimeString("zh-CN");
      setCurrentTime(timeString);
    };

    updateTime();
    const timer = setInterval(updateTime, 1000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    if (sessionChartRef.current && businessChartRef.current) {
      initSessionChart();
      initBusinessChart();
    }
  }, []);

  useEffect(() => {
    const updateData = () => {
      setDashboardData((prev) => ({
        ...prev,
        onlineUserCount: Math.max(230, Math.min(280, prev.onlineUserCount + Math.floor(Math.random() * 5) - 2)),
        callToday: prev.callToday + Math.floor(Math.random() * 5) + 2,
        agentScript: Math.max(65, Math.min(75, prev.agentScript + Math.random() * 0.6 - 0.3)),
        concurrentCalls: Math.max(70, Math.min(110, prev.concurrentCalls + Math.floor(Math.random() * 7) - 3)),
        waitingCalls: Math.max(5, Math.min(25, prev.waitingCalls + Math.floor(Math.random() * 5) - 2)),
        answerRate: Math.max(92, Math.min(96, prev.answerRate + Math.random() * 0.4 - 0.2)),
        avgWaitTime: generateAvgWaitTime(Math.max(200, Math.min(400, 200 + Math.floor(Math.random() * 60) - 30))),
        maxWaitTime: generateMaxWaitTime(prev.waitingCalls),
        waitTrend: generateWaitTrend(),
      }));
    };

    const dataTimer = setInterval(updateData, 5000);
    return () => clearInterval(dataTimer);
  }, []);

  function generateAvgWaitTime(avgWaitTime: number) {
    const seconds = Math.max(200, Math.min(400, avgWaitTime + Math.floor(Math.random() * 60) - 30));
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}m ${remainingSeconds}s`;
  }

  function generateMaxWaitTime(waitingCalls: number) {
    const maxWaitSeconds = waitingCalls * 10 + Math.floor(Math.random() * 30);
    const minutes = Math.floor(maxWaitSeconds / 60);
    const seconds = maxWaitSeconds % 60;
    return `${minutes}m ${seconds}s`;
  }

  function generateWaitTrend() {
    const trendChange = Math.floor(Math.random() * 10) - 5;
    return trendChange >= 0 ? `+${trendChange}s` : `${trendChange}s`;
  }

  function initSessionChart() {
    if (!sessionChartRef.current) return;
    const chart = echarts.init(sessionChartRef.current);
    const timeData: string[] = [];
    const emotionData: number[] = [];
    const now = new Date();

    for (let i = 9; i >= 0; i--) {
      const time = new Date(now.getTime() - i * 3600000);
      timeData.push(time.getHours() + ":00");
      let emotion = Math.floor(Math.random() * 75 + 25);
      if (i === 3) emotion = 25;
      if (i === 7) emotion = 90;
      emotionData.push(emotion);
    }

    const option = {
      tooltip: {
        trigger: "axis",
        formatter: function (params: any) {
          const time = params[0].axisValue;
          const value = params[0].data;
          return time + "<br/>风险评分: " + value.toFixed(2) + "<br/>风险概率: " + value.toFixed(0) + "%";
        },
      },
      grid: {
        left: "3%",
        right: "4%",
        bottom: "10%",
        top: "10%",
        containLabel: true,
      },
      xAxis: {
        type: "category",
        boundaryGap: false,
        data: timeData,
        axisLine: { lineStyle: { color: "#4a6aa6" } },
        axisLabel: { color: "#a0c8ff" },
      },
      yAxis: {
        type: "value",
        min: 0,
        max: 100,
        axisLine: { lineStyle: { color: "#4a6aa6" } },
        axisLabel: {
          color: "#a0c8ff",
          formatter: function (value: number) {
            return value.toFixed(0);
          },
        },
        splitLine: { lineStyle: { color: "rgba(100, 150, 255, 0.1)" } },
      },
      series: [
        {
          name: "风险评分",
          type: "line",
          smooth: true,
          symbol: "circle",
          symbolSize: 8,
          lineStyle: { width: 3, color: "#4ae2ff" },
          itemStyle: { color: "#4ae2ff" },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: "rgba(74, 226, 255, 0.4)" },
              { offset: 1, color: "rgba(74, 226, 255, 0.05)" },
            ]),
          },
          data: emotionData,
        },
      ],
    };

    chart.setOption(option);
  }

  function initBusinessChart() {
    if (!businessChartRef.current) return;

    const chart = echarts.init(businessChartRef.current);

    const option = {
      tooltip: {
        trigger: "item",
        formatter: "诈骗类型<br/>{b}: {c} ({d}%)",
      },
      legend: {
        orient: "horizontal",
        bottom: 10,
        left: "center",
        textStyle: { color: "#a0c8ff" },
      },
      series: [
        {
          name: "诈骗类型分布",
          type: "pie",
          radius: ["40%", "70%"],
          center: ["50%", "45%"],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: "#0a1636",
            borderWidth: 2,
          },
          label: { show: false, position: "center" },
          emphasis: {
            label: {
              show: true,
              fontSize: 18,
              fontWeight: "bold",
              color: "#fff",
            },
          },
          labelLine: { show: false },
          data: [
            { value: 34, name: "投资诈骗", itemStyle: { color: "#1a7cff" } },
            { value: 28, name: "刷单诈骗", itemStyle: { color: "#00ff88" } },
            { value: 18, name: "冒充公检法", itemStyle: { color: "#ffaa00" } },
            { value: 12, name: "贷款诈骗", itemStyle: { color: "#ff4d4f" } },
            { value: 8, name: "其他诈骗", itemStyle: { color: "#a0c8ff" } },
          ],
        },
      ],
    };

    chart.setOption(option);
  }

  return (
    <div style={dashboardStyles.container}>
      <div style={dashboardStyles.header}>
        <div style={dashboardStyles.title}>金睛</div>
        <div style={dashboardStyles.subtitle}>全模态反诈智能体监控平台</div>
        <div style={dashboardStyles.currentTime}>{currentTime}</div>
      </div>

      <div style={dashboardStyles.dashboardContainer}>
        <div style={{ ...dashboardStyles.panel, ...dashboardStyles.globalKpi }}>
          <div style={dashboardStyles.panelHeader}>
            <div style={dashboardStyles.panelTitle}>
              <div style={{ ...dashboardStyles.icon, ...dashboardStyles.iconKpi }}></div>
              全局风险监控
            </div>
            <div style={dashboardStyles.panelStatus}>实时更新</div>
          </div>

          <div style={dashboardStyles.panelContent}>
            <div style={dashboardStyles.kpiContainer}>

              <div style={dashboardStyles.kpiItem}>
                <div>活跃用户数</div>
                <div style={dashboardStyles.kpiValue}>{dashboardData.onlineUserCount}</div>
                <div><span style={dashboardStyles.trendUp}>▲ 用户增长</span></div>
              </div>

              <div style={dashboardStyles.kpiItem}>
                <div>今日检测次数</div>
                <div style={dashboardStyles.kpiValue}>{dashboardData.callToday.toLocaleString()}</div>
                <div><span style={dashboardStyles.trendUp}>▲ 检测规模提升</span></div>
              </div>

              <div style={dashboardStyles.kpiItem}>
                <div>AI识别次数</div>
                <div style={dashboardStyles.kpiValue}>{Math.round(dashboardData.callToday*2.8)}</div>
                <div><span style={dashboardStyles.trendUp}>准确率 ▲ 91%</span></div>
              </div>

              <div style={dashboardStyles.kpiItem}>
                <div>风险提示次数</div>
                <div style={dashboardStyles.kpiValue}>{Math.round(dashboardData.callToday*1.6)}</div>
                <div><span style={dashboardStyles.trendUp}>命中率 ▲ 89%</span></div>
              </div>

              <div style={dashboardStyles.kpiItem}>
                <div>干预执行次数</div>
                <div style={dashboardStyles.kpiValue}>{Math.round(dashboardData.callToday*2.1)}</div>
                <div><span style={dashboardStyles.trendUp}>成功率 ▲ 95%</span></div>
              </div>

            </div>
          </div>
        </div>

        <div style={{ ...dashboardStyles.panel, ...dashboardStyles.phoneLines }}>
          <div style={dashboardStyles.panelHeader}>
            <div style={dashboardStyles.panelTitle}>
              <div style={{ ...dashboardStyles.icon, ...dashboardStyles.iconPhone }}></div>
              风控系统运行状态
            </div>
            <div style={dashboardStyles.panelStatus}>系统正常</div>
          </div>

          <div style={dashboardStyles.panelContent}>
            <div style={dashboardStyles.lineMetrics}>

              <div style={dashboardStyles.metricCard}>
                <div style={dashboardStyles.metricTitle}>实时检测任务数</div>
                <div style={dashboardStyles.metricValue}>{dashboardData.concurrentCalls}</div>
                <div style={dashboardStyles.metricSubtitle}>系统容量: 120</div>
              </div>

              <div style={dashboardStyles.metricCard}>
                <div style={dashboardStyles.metricTitle}>高风险事件数</div>
                <div style={dashboardStyles.metricValue}>{dashboardData.waitingCalls}</div>
                <div style={dashboardStyles.metricSubtitle}>平均处理耗时: {dashboardData.maxWaitTime}</div>
              </div>

              <div style={dashboardStyles.metricCard}>
                <div style={dashboardStyles.metricTitle}>识别准确率</div>
                <div style={dashboardStyles.metricValue}>{dashboardData.answerRate.toFixed(1)}%</div>
                <div style={dashboardStyles.metricSubtitle}>目标: 95%</div>
              </div>

              <div style={dashboardStyles.metricCard}>
                <div style={dashboardStyles.metricTitle}>平均推理耗时</div>
                <div style={dashboardStyles.metricValue}>{dashboardData.avgWaitTime}</div>
                <div style={dashboardStyles.metricSubtitle}>波动: {dashboardData.waitTrend}</div>
              </div>

            </div>
          </div>
        </div>

        <div style={{ ...dashboardStyles.panel, ...dashboardStyles.sessionQuality }}>
          <div style={dashboardStyles.panelHeader}>
            <div style={dashboardStyles.panelTitle}>
              <div style={{ ...dashboardStyles.icon, ...dashboardStyles.iconSession }}></div>
              风险评分趋势
            </div>
            <div style={dashboardStyles.panelStatus}>2个高风险事件</div>
          </div>
          <div style={dashboardStyles.panelContent}>
            <div ref={sessionChartRef} style={dashboardStyles.chartContainer}></div>
          </div>
        </div>

        <div style={{ ...dashboardStyles.panel, ...dashboardStyles.businessInsight }}>
          <div style={dashboardStyles.panelHeader}>
            <div style={dashboardStyles.panelTitle}>
              <div style={{ ...dashboardStyles.icon, ...dashboardStyles.iconBusiness }}></div>
              诈骗类型分布
            </div>
          </div>
          <div style={dashboardStyles.panelContent}>
            <div ref={businessChartRef} style={dashboardStyles.businessChartContainer}></div>
          </div>
        </div>

      </div>
    </div>
  );
}