// 节点接口
export interface INode {
  id: string; // 节点唯一ID
  name: string; // 节点名称
  type: "READ" | "CREATE" | "UPDATE" | "DELETE" | "START" | "END"; // 节点类型，默认值为"READ"
  x: number; // X坐标
  y: number; // Y坐标
}

export const nodeTypeLabels: Record<string, string> = {
  READ: "读取",
  CREATE: "新增",
  UPDATE: "更新",
  DELETE: "删除",
  START: "开始",
  END: "完成",
};
