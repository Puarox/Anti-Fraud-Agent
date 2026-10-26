// 话术接口
export interface IScript {
  id: string; // 话术唯一ID
  name: string; // 话术名称
  summary: string; // 话术摘要
  content: string; // 话术内容
  type: "NODE" | "COMMON" | "FEEDBACK"; // 话术类型，NODE表示节点话术，COMMON表示通用话术，FEEDBACK表示反馈话术
  associateId: string | undefined; // 若为NODE类型，则为节点ID；若为FEEDBACK类型，则为操作ID；COMMON类型则为undefined
  tag: string[] | undefined; // 话术标签列表
  priority: number; // 话术优先级，数值越小优先级越高
  conditions: ICondition[]; // 话术条件
}

// 条件接口
export interface ICondition {
  type: "ATOMIC" | "AND" | "OR" | "NOT"; // 条件类型，ATOMIC表示原子条件，AND表示与条件，OR表示或条件，NOT表示非条件
  lhs?: string | string[]; // 左操作数，支持：①变量占位符如{用户年龄} ，调用对应方法获取值，后端会自动根据当前sessionId并填充true或false；②常量: 数字、字符串。
  op?: "eq" | "ne" | "gt" | "ge" | "lt" | "le" | "startswith" | "endswith" | "in" | "contains"; // 操作符
  rhs?: string | string[]; // 右操作数，支持同lhs
  sub?: ICondition[]; // 子条件列表
}

export const conditionTypeLabels: Record<string, string> = {
  ATOMIC: "原子",
  AND: "与",
  OR: "或",
  NOT: "非",
  eq: "=",
  ne: "≠",
  gt: ">",
  ge: "⩾",
  lt: "<",
  le: "⩽",
  startswith: "以...开头",
  endswith: "以...结尾",
  in: "包含于",
  contains: "包含",
};
