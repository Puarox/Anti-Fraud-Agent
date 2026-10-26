export interface IDashboardData {
    onlineUserCount: number; // 在线坐席数
  callToday: number; // 今日通话量
  agentScript: number; // 智能体解决率
    agentOptimize: number; // 平均响应速度
  agentNavigate: number; // 客户满意度
  concurrentCalls: number; // 并发通话数
  waitingCalls: number; // 排队等待数
  answerRate: number; // 今日接通率
  avgWaitTime: string; // 平均等待时长
  maxWaitTime: string; // 最长等待时间
  waitTrend: string; // 等待时间环比
}
