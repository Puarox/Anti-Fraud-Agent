// 边接口
export interface IEdge {
  id: string; // 边唯一ID
  name: string | undefined; // 边名称
  sourceId: string; // 有向边起点ID
  targetId: string; // 有向边终点ID
}
