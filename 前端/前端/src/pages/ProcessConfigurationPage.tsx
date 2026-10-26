import { InfoLabel, MessageBar, MessageBarBody, Spinner, Title1, tokens } from "@fluentui/react-components";
import {
  AddCircle24Regular,
  CheckmarkCircle20Color,
  Delete24Regular,
  DismissCircle20Color,
  Edit24Regular,
  Link24Regular,
} from "@fluentui/react-icons";
import {
  addEdge,
  Background,
  Controls,
  type Edge as FlowEdge,
  type Node as FlowNode,
  Handle,
  MarkerType,
  type NodeProps,
  Position,
  ReactFlow,
  ReactFlowProvider,
  useEdgesState,
  useNodesState,
  useReactFlow,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import React, { useCallback, useEffect, useRef, useState } from "react";
import { EdgeSidebar } from "../components/EdgeSidebar";
import { NodeSidebar } from "../components/NodeSidebar";
import { ScriptDetailSidebar } from "../components/ScriptDetailSidebar";
import { useStyles } from "../styles/ProcessConfigurationStyle";
import type { IEdge } from "../types/IEdge";
import type { INode } from "../types/INode";
import type { IScript } from "../types/IScript";
import api from "../utils/Api";
import { BackendTagToFrontendConverter, FrontendTagToBackendConverter } from "../utils/ScriptTagConverter";

// --- 类型 ---
type CustomNodeData = INode & { label: string; [key: string]: unknown };
type CustomNodeType = FlowNode<CustomNodeData>;

// --- 自定义节点组件 ---
const CustomNode = ({ data, selected }: NodeProps<CustomNodeType>) => {
  const styles = useStyles();
  return (
    <div className={`${styles.customNode} ${selected ? styles.customNodeSelected : ""}`}>
      <Handle type="target" position={Position.Left} style={{ visibility: "hidden" }} />
      <div>{data.label}</div>
      <Handle type="source" position={Position.Right} style={{ visibility: "hidden" }} />
    </div>
  );
};

const nodeTypes = {
  custom: CustomNode,
};

function pick<T, K extends keyof T>(obj: any, keys: K[]): Pick<T, K> {
  return keys.reduce((acc, k) => {
    if (k in obj) (acc as any)[k] = obj[k];
    return acc;
  }, {} as Pick<T, K>);
}

// --- 主组件 ---
function ProcessConfigurationPageContent() {
  const styles = useStyles();
  const reactFlowWrapper = useRef<HTMLDivElement>(null);
  const [nodes, setNodes, onNodesChange] = useNodesState<CustomNodeType>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState<FlowEdge>([]);
  const { screenToFlowPosition, getNodes, getEdges, flowToScreenPosition } = useReactFlow<CustomNodeType, FlowEdge>();

  // 交互状态
  const [menu, setMenu] = useState<{
    id: string;
    top: number;
    left: number;
    type: "node" | "edge";
  } | null>(null);
  const [selectedElement, setSelectedElement] = useState<{
    type: "node" | "edge";
    id: string;
  } | null>(null);
  const [interactionMode, setInteractionMode] = useState<"none" | "connecting" | "creating">("none");
  const [sourceNodeId, setSourceNodeId] = useState<string | null>(null);
  const [mousePos, setMousePos] = useState<{ x: number; y: number } | null>(null);

  // 侧边栏状态
  const [sidebarView, setSidebarView] = useState<"none" | "node" | "edge">("none");
  const [sidebarData, setSidebarData] = useState<any>(null);
  const [currentScriptId, setCurrentScriptId] = useState<string | null>(null);
  const [showScriptDetail, setShowScriptDetail] = useState<boolean>(false);
  const [refreshTrigger, setRefreshTrigger] = useState<number>(0);

  // 错误提示状态
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // 话术列表
  const [scripts, setScripts] = useState<IScript[]>([]);

  // 保存相关的refs
  const saveTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const pendingSaveRef = useRef<{
    nodes: Map<string, Partial<INode>>;
    edges: Map<string, Partial<IEdge>>;
  }>({ nodes: new Map(), edges: new Map() });

  // 保存状态（idle | saving | saved | error）
  const [savingState, setSavingState] = useState<"idle" | "saving" | "saved" | "error">("idle");
  const [lastSavedAt, setLastSavedAt] = useState<Date | null>(null);

  function formatTime(date: Date | null) {
    if (!date) return "";
    return date.toLocaleTimeString("zh-CN", { hour12: false });
  }

  // 带保存状态包装器：用于单次后端写操作（非页面加载）
  async function withSaveState<T>(fn: () => Promise<T>): Promise<T> {
    setSavingState("saving");
    try {
      const res = await fn();
      const now = new Date();
      setLastSavedAt(now);
      setSavingState("saved");
      return res;
    } catch (err) {
      setSavingState("error");
      throw err;
    }
  }

  // 显示错误信息
  function showError(message: string) {
    setErrorMessage(message);
    setTimeout(() => setErrorMessage(null), 5000);
  }

  // 初始化加载数据
  useEffect(() => {
    // 避免内存泄漏
    const controller = new AbortController();

    async function load() {
      try {
        const nodesRes = await api.post("/node/list", {}, { signal: controller.signal });
        const edgesRes = await api.post("/edge/list", {}, { signal: controller.signal });
        const scriptsRes = await api.post(
          "/script/list",
          {
            "EQ:type": "NODE",
          },
          { signal: controller.signal }
        );

        const nodes: INode[] = nodesRes.data.data;
        const edges: IEdge[] = edgesRes.data.data;
        const scriptsFromApi: IScript[] = scriptsRes.data.data;

        if (nodes.length === 0) {
          const startNodePayload = {
            name: "开始",
            type: "START",
            x: 50,
            y: 350,
          };
          const endNodePayload = {
            name: "结束",
            type: "END",
            x: 700,
            y: 350,
          };
          const startNodeRes = await api.post("/node/create", startNodePayload, { signal: controller.signal });
          const endNodeRes = await api.post("/node/create", endNodePayload, { signal: controller.signal });
          nodes.push(startNodeRes.data.data);
          nodes.push(endNodeRes.data.data);
        }

        const flowNodes: CustomNodeType[] = nodes.map((n) => ({
          id: n.id,
          type: "custom",
          position: { x: n.x, y: n.y },
          data: { ...n, label: n.name },
        }));

        const flowEdges: FlowEdge[] = edges.map((e) => ({
          id: e.id,
          source: e.sourceId,
          target: e.targetId,
          type: "default",
          markerEnd: { type: MarkerType.Arrow, color: tokens.colorBrandBackground },
          style: {
            stroke: tokens.colorBrandBackground,
            strokeWidth: 2,
          },
          data: { name: e.name },
        }));

        setNodes(flowNodes);
        setEdges(flowEdges);
        // 将后端的 string 格式 tag 转为前端的 string[] 格式
        setScripts(
          scriptsFromApi.map((s) => {
            const { isDeleted, ...rest } = s as any;
            return { ...(rest as any), tag: BackendTagToFrontendConverter((rest as any).tag) } as IScript;
          })
        );
      } catch (err: any) {
        // 请求被取消时不需要报错
        if (err?.name === "CanceledError" || err?.code === "ERR_CANCELED") return;
        console.error("加载数据失败:", err);
        showError("加载数据失败，请刷新重试");
      }
    }

    load();

    return () => {
      controller.abort();
    };
  }, [setNodes, setEdges]);

  // 定时保存逻辑
  useEffect(() => {
    saveTimerRef.current = setInterval(() => {
      savePendingChanges();
    }, 5000); // 每15秒保存一次

    return () => {
      if (saveTimerRef.current) {
        clearInterval(saveTimerRef.current);
      }
    };
  }, []);

  // 离开页面前保存
  useEffect(() => {
    function handleBeforeUnload() {
      savePendingChanges();
    }

    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
      savePendingChanges();
    };
  }, []);

  // 保存待处理的更改
  async function savePendingChanges() {
    const { nodes: pendingNodes, edges: pendingEdges } = pendingSaveRef.current;

    // 并发保存所有更改，返回 boolean 表示成功或失败
    const savePromises: Promise<boolean>[] = [];

    // 保存节点更改 — 发送完整节点数据（id 只放在 URL）
    for (const [nodeId, data] of pendingNodes.entries()) {
      // 如果节点已从画布移除（例如被删除），则跳过并清理待保存项，避免向后端发送更新请求
      const node = getNodes().find((n) => n.id === nodeId);
      if (!node) {
        pendingNodes.delete(nodeId);
        continue;
      }

      // 尝试从当前画布节点获取完整数据并合并未改变的属性
      const base = node.data || {};
      const raw = { ...base, ...data };
      const payload = pick<INode, "name" | "type" | "x" | "y">(raw, ["name", "type", "x", "y"]);

      savePromises.push(
        api
          .put(`/node/${nodeId}`, payload)
          .then(() => true)
          .catch((error) => {
            console.error(`保存节点 ${nodeId} 失败:`, error);
            showError(`保存节点失败: ${error.response?.data?.message || "无法连接至服务器"}`);
            return false;
          })
      );
    }

    // 保存边更改
    for (const [edgeId, data] of pendingEdges.entries()) {
      savePromises.push(
        api
          .put(`/edge/${edgeId}`, data)
          .then(() => true)
          .catch((error) => {
            console.error(`保存边 ${edgeId} 失败:`, error);
            showError(`保存边失败: ${error.response?.data?.message || "无法连接至服务器"}`);
            return false;
          })
      );
    }

    if (savePromises.length > 0) {
      // 开始保存
      setSavingState("saving");
      const results = await Promise.all(savePromises);
      const allSucceeded = results.every((r) => r === true);
      if (allSucceeded) {
        const now = new Date();
        setLastSavedAt(now);
        setSavingState("saved");
        // 清空待保存队列
        pendingSaveRef.current = { nodes: new Map(), edges: new Map() };
      } else {
        // 保存失败，保留上次成功时间不变
        setSavingState("error");
      }
    }
  }

  // 标记节点需要保存
  function markNodeForSave(nodeId: string, data: Partial<INode>) {
    const existing = pendingSaveRef.current.nodes.get(nodeId) || {};
    pendingSaveRef.current.nodes.set(nodeId, { ...existing, ...data });
  }

  // 标记边需要保存
  function markEdgeForSave(edgeId: string, data: Partial<IEdge>) {
    const existing = pendingSaveRef.current.edges.get(edgeId) || {};
    pendingSaveRef.current.edges.set(edgeId, { ...existing, ...data });
  }

  // --- 事件处理程序 ---

  // 节点右键菜单
  const onNodeContextMenu = useCallback((event: React.MouseEvent, node: FlowNode) => {
    event.preventDefault();
    const pane = reactFlowWrapper.current?.getBoundingClientRect();
    if (!pane) return;

    // 计算相对于画布的鼠标位置
    const x = event.clientX - pane.left;
    const y = event.clientY - pane.top;

    // 菜单的估计尺寸
    const menuWidth = 170;
    const menuHeight = 165;

    // 计算实际显示位置
    let finalX = x;
    let finalY = y;

    // 如果菜单会超出右边界，则让菜单右边紧贴鼠标位置
    if (x + menuWidth > pane.width) {
      finalX = x - menuWidth;
    }

    // 如果菜单会超出底部边界，则让菜单底部紧贴鼠标位置
    if (y + menuHeight > pane.height) {
      finalY = y - menuHeight;
    }

    // 确保不超出左边界和顶部
    finalX = Math.max(0, finalX);
    finalY = Math.max(0, finalY);

    setMenu({
      id: node.id,
      top: finalY,
      left: finalX,
      type: "node",
    });
  }, []);

  // 边右键菜单
  const onEdgeContextMenu = useCallback((event: React.MouseEvent, edge: FlowEdge) => {
    event.preventDefault();
    const pane = reactFlowWrapper.current?.getBoundingClientRect();
    if (!pane) return;

    // 计算相对于画布的鼠标位置
    const x = event.clientX - pane.left;
    const y = event.clientY - pane.top;

    // 菜单的估计尺寸
    const menuWidth = 160;
    const menuHeight = 100;

    // 计算实际显示位置
    let finalX = x;
    let finalY = y;

    // 如果菜单会超出右边界，则让菜单右边紧贴鼠标位置
    if (x + menuWidth > pane.width) {
      finalX = x - menuWidth;
    }

    // 如果菜单会超出底部边界，则让菜单底部紧贴鼠标位置
    if (y + menuHeight > pane.height) {
      finalY = y - menuHeight;
    }

    // 确保不超出左边界和顶部
    finalX = Math.max(0, finalX);
    finalY = Math.max(0, finalY);

    setMenu({
      id: edge.id,
      top: finalY,
      left: finalX,
      type: "edge",
    });
  }, []);

  // 画布点击后处理
  const onPaneClick = useCallback(() => {
    setMenu(null);
    if (sidebarView !== "none" || showScriptDetail) {
      savePendingChanges();
      setSidebarView("none");
      setSelectedElement(null);
      setShowScriptDetail(false);
      setCurrentScriptId(null);
      setNodes((nodes) => nodes.map((n) => ({ ...n, selected: false })));
      setEdges((edges) =>
        edges.map((e) => ({
          ...e,
          style: { ...e.style, strokeWidth: 2 },
          selected: false,
        }))
      );
    }
  }, [sidebarView, showScriptDetail, setNodes, setEdges]);

  // 画布点击事件
  const onPaneClickEvent = useCallback(
    async (event: React.MouseEvent) => {
      if (interactionMode === "creating" && sourceNodeId) {
        const position = screenToFlowPosition({
          x: event.clientX,
          y: event.clientY,
        });

        const newNodePayload = {
          name: "新节点",
          type: "READ",
          x: position.x,
          y: position.y,
        };

        try {
          setSavingState("saving");
          // 创建节点
          const createdNodeResponse = await api.post("/node/create", newNodePayload);
          const createdNode = createdNodeResponse.data.data;

          if (!createdNode || !createdNode.id) {
            throw new Error("节点创建返回数据异常");
          }

          const newNodeFlow: CustomNodeType = {
            id: createdNode.id,
            type: "custom",
            position,
            data: { ...createdNode, label: createdNode.name },
          };

          setNodes((nodes) => nodes.concat(newNodeFlow));

          // 创建边
          const linkResponse = await api.post("/node/link", null, {
            params: {
              srcId: sourceNodeId,
              tgtId: createdNode.id,
            },
          });

          const createdEdge = linkResponse.data.data;
          if (!createdEdge || !createdEdge.id) {
            throw new Error("连线创建返回数据异常");
          }

          const newEdge: FlowEdge = {
            id: createdEdge.id,
            source: sourceNodeId,
            target: createdNode.id,
            type: "default",
            markerEnd: { type: MarkerType.Arrow, color: tokens.colorBrandBackground },
            style: {
              stroke: tokens.colorBrandBackground,
              strokeWidth: 2,
            },
            data: { conditions: "[]", ...createdEdge },
          };

          setEdges((edges) => edges.concat(newEdge));
          const now = new Date();
          setLastSavedAt(now);
          setSavingState("saved");
        } catch (error: any) {
          setSavingState("error");
          console.error("创建节点和连线失败:", error);
          showError(`创建节点和连线失败: ${error.response?.data?.message || "无法连接至服务器"}`);
        }

        setInteractionMode("none");
        setSourceNodeId(null);
      } else if (interactionMode === "connecting") {
        setInteractionMode("none");
        setSourceNodeId(null);
      } else {
        onPaneClick();
      }
    },
    [interactionMode, sourceNodeId, screenToFlowPosition, setNodes, setEdges, onPaneClick]
  );

  // 节点点击事件
  const onNodeClick = useCallback(
    async (event: React.MouseEvent, node: FlowNode) => {
      if (interactionMode === "connecting" && sourceNodeId) {
        if (sourceNodeId === node.id) return;

        try {
          setSavingState("saving");
          // 创建边
          const linkResponse = await api.post("/node/link", null, {
            params: {
              srcId: sourceNodeId,
              tgtId: node.id,
            },
          });
          const createdEdge = linkResponse.data.data;

          if (!createdEdge || !createdEdge.id) {
            throw new Error("连线创建返回数据异常");
          }

          const newEdge: FlowEdge = {
            id: createdEdge.id,
            source: sourceNodeId,
            target: node.id,
            type: "default",
            markerEnd: { type: MarkerType.Arrow, color: tokens.colorBrandBackground },
            style: {
              stroke: tokens.colorBrandBackground,
              strokeWidth: 2,
            },
            data: { conditions: "[]", ...createdEdge },
          };

          setEdges((edges) => addEdge(newEdge, edges));
          const now = new Date();
          setLastSavedAt(now);
          setSavingState("saved");
        } catch (error: any) {
          setSavingState("error");
          console.error("创建连线失败:", error);
          showError(`创建连线失败: ${error.response?.data?.message || "无法连接至服务器"}`);
        }

        setInteractionMode("none");
        setSourceNodeId(null);
      } else {
        setSelectedElement({ type: "node", id: node.id });
        setSidebarData({ ...node.data });
        setSidebarView("node");
        setNodes((nodes) => nodes.map((n) => ({ ...n, selected: n.id === node.id })));
      }
    },
    [interactionMode, sourceNodeId, setEdges, setNodes]
  );

  // 边点击事件
  const onEdgeClick = useCallback(
    (event: React.MouseEvent, edge: FlowEdge) => {
      setSelectedElement({ type: "edge", id: edge.id });
      setSidebarData({ ...edge.data });
      setSidebarView("edge");
      setEdges((edges) =>
        edges.map((e) => ({
          ...e,
          style: {
            ...e.style,
            strokeWidth: e.id === edge.id ? 4 : 2,
          },
          selected: e.id === edge.id,
        }))
      );
    },
    [setEdges]
  );

  // 鼠标移动事件
  const onMouseMove = useCallback(
    (event: React.MouseEvent) => {
      if (interactionMode !== "none") {
        const bounds = reactFlowWrapper.current?.getBoundingClientRect();
        if (bounds) {
          setMousePos({
            x: event.clientX - bounds.left,
            y: event.clientY - bounds.top,
          });
        }
      }
    },
    [interactionMode]
  );

  // 节点拖拽开始事件（调试用）
  const onNodeDragStart = useCallback((event: React.MouseEvent, node: FlowNode) => {
    console.log(`[node-drag-start] id=${node.id}`);
  }, []);

  // 节点拖拽停止事件（调试用）
  const onNodeDragStop = useCallback((event: React.MouseEvent, node: FlowNode) => {
    console.log(`[node-drag-stop] id=${node.id} final=(${node.position?.x ?? 0}, ${node.position?.y ?? 0})`);
    markNodeForSave(node.id, { x: node.position?.x ?? 0, y: node.position?.y ?? 0 });
  }, []);

  // 删除节点处理
  const handleNodesDelete = useCallback(
    async (deleted: FlowNode[]) => {
      const deletedIds = new Set(deleted.map((n) => n.id));

      if (selectedElement?.type === "node" && deleted.some((n) => n.id === selectedElement.id)) {
        setSidebarView("none");
        setSelectedElement(null);
      }

      try {
        setSavingState("saving");
        const results = await Promise.allSettled(deleted.map((n) => api.delete(`/node/${n.id}`).then(() => n.id)));
        const succeeded = results
          .filter((r): r is PromiseFulfilledResult<string> => r.status === "fulfilled")
          .map((r) => r.value);
        const failed = results.some((r) => r.status === "rejected");

        if (succeeded.length > 0) {
          const succeededSet = new Set(succeeded);
          if (selectedElement?.type === "node" && succeeded.some((id) => id === selectedElement.id)) {
            setSidebarView("none");
            setSelectedElement(null);
          }
          setNodes((nodes) => nodes.filter((n) => !succeededSet.has(n.id)));
          setEdges((edges) => edges.filter((e) => !succeededSet.has(e.source) && !succeededSet.has(e.target)));

          // 清理 pending 保存项，避免后续定时保存再次尝试保存已删除的节点/连线
          for (const id of succeededSet) {
            pendingSaveRef.current.nodes.delete(id);
          }
          const currentEdges = getEdges();
          for (const [edgeId] of pendingSaveRef.current.edges.entries()) {
            const edge = currentEdges.find((e) => e.id === edgeId);
            if (!edge || succeededSet.has(edge.source) || succeededSet.has(edge.target)) {
              pendingSaveRef.current.edges.delete(edgeId);
            }
          }
        }

        if (failed) {
          setSavingState("error");
          showError("部分节点删除失败，请重试或刷新");
        } else {
          const now = new Date();
          setLastSavedAt(now);
          setSavingState("saved");
        }
      } catch (error: any) {
        setSavingState("error");
        console.error("删除节点时出错:", error);
        showError(`删除节点失败: ${error.response?.data?.message || "无法连接至服务器"}`);
      }
    },
    [selectedElement, setSidebarView, setSelectedElement, setNodes, setEdges, showError]
  );

  // 删除连线处理
  const handleEdgesDelete = useCallback(
    async (deleted: FlowEdge[]) => {
      const deletedIds = new Set(deleted.map((e) => e.id));

      if (selectedElement?.type === "edge" && deleted.some((e) => e.id === selectedElement.id)) {
        setSidebarView("none");
        setSelectedElement(null);
      }

      try {
        setSavingState("saving");
        const results = await Promise.allSettled(deleted.map((e) => api.delete(`/edge/${e.id}`).then(() => e.id)));
        const succeeded = results
          .filter((r): r is PromiseFulfilledResult<string> => r.status === "fulfilled")
          .map((r) => r.value);
        const failed = results.some((r) => r.status === "rejected");

        if (succeeded.length > 0) {
          const succeededSet = new Set(succeeded);
          if (selectedElement?.type === "edge" && succeeded.some((id) => id === selectedElement.id)) {
            setSidebarView("none");
            setSelectedElement(null);
          }
          setEdges((edges) => edges.filter((e) => !succeededSet.has(e.id)));
        }

        if (failed) {
          setSavingState("error");
          showError("部分连线删除失败，请重试或刷新");
        } else {
          const now = new Date();
          setLastSavedAt(now);
          setSavingState("saved");
        }
      } catch (error: any) {
        setSavingState("error");
        console.error("删除连线时出错:", error);
        showError(`删除连线失败: ${error.response?.data?.message || "无法连接至服务器"}`);
      }
    },
    [selectedElement, setSidebarView, setSelectedElement, setEdges, showError]
  );

  // 禁用默认删除后，添加全局键盘处理，在后端成功后再更新 UI
  useEffect(() => {
    function isInputFocused() {
      const active = document.activeElement as HTMLElement | null;
      return !!active && (active.tagName === "INPUT" || active.tagName === "TEXTAREA" || active.isContentEditable);
    }

    async function onKeyDown(e: KeyboardEvent) {
      if (e.key !== "Delete" && e.key !== "Backspace") return;
      if (isInputFocused()) return;

      const nodesToDelete = getNodes().filter((n) => n.selected);
      const edgesToDelete = getEdges().filter((e) => e.selected);

      if (nodesToDelete.length === 0 && edgesToDelete.length === 0) return;
      e.preventDefault();

      if (nodesToDelete.length > 0) {
        setSavingState("saving");
        const results = await Promise.allSettled(
          nodesToDelete.map((n) => api.delete(`/node/${n.id}`).then(() => n.id))
        );
        const succeeded = results
          .filter((r): r is PromiseFulfilledResult<string> => r.status === "fulfilled")
          .map((r) => r.value);
        const failed = results.some((r) => r.status === "rejected");

        if (succeeded.length > 0) {
          const succeededSet = new Set(succeeded);
          if (selectedElement?.type === "node" && succeeded.some((id) => id === selectedElement.id)) {
            setSidebarView("none");
            setSelectedElement(null);
          }
          setNodes((nodes) => nodes.filter((n) => !succeededSet.has(n.id)));
          setEdges((edges) => edges.filter((e) => !succeededSet.has(e.source) && !succeededSet.has(e.target)));
        }

        if (failed) {
          setSavingState("error");
          showError("部分节点删除失败，请重试或刷新");
        } else {
          const now = new Date();
          setLastSavedAt(now);
          setSavingState("saved");
        }
      }

      if (edgesToDelete.length > 0) {
        setSavingState("saving");
        const results = await Promise.allSettled(
          edgesToDelete.map((e) => api.delete(`/edge/${e.id}`).then(() => e.id))
        );
        const succeeded = results
          .filter((r): r is PromiseFulfilledResult<string> => r.status === "fulfilled")
          .map((r) => r.value);
        const failed = results.some((r) => r.status === "rejected");

        if (succeeded.length > 0) {
          const succeededSet = new Set(succeeded);
          if (selectedElement?.type === "edge" && succeeded.some((id) => id === selectedElement.id)) {
            setSidebarView("none");
            setSelectedElement(null);
          }
          setEdges((edges) => edges.filter((e) => !succeededSet.has(e.id)));
        }
        if (failed) {
          setSavingState("error");
          showError("部分连线删除失败，请重试或刷新");
        } else {
          const now = new Date();
          setLastSavedAt(now);
          setSavingState("saved");
        }
      }
    }

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [getNodes, getEdges, setNodes, setEdges, selectedElement, setSidebarView, setSelectedElement, showError]);

  // 处理菜单操作
  async function handleMenuAction(action: string) {
    if (!menu) return;
    const { id, type } = menu;
    setMenu(null);

    if (type === "node") {
      if (action === "new_node") {
        setInteractionMode("creating");
        setSourceNodeId(id);
      } else if (action === "connect") {
        setInteractionMode("connecting");
        setSourceNodeId(id);
      } else if (action === "delete") {
        try {
          setSavingState("saving");
          await api.delete(`/node/${id}`);
          setNodes((nodes) => nodes.filter((n) => n.id !== id));
          setEdges((edges) => edges.filter((e) => e.source !== id && e.target !== id));
          const now = new Date();
          setLastSavedAt(now);
          setSavingState("saved");
        } catch (error: any) {
          setSavingState("error");
          console.error("删除节点失败:", error);
          showError(`删除节点失败: ${error.response?.data?.message || "无法连接至服务器"}`);
        }
      } else if (action === "properties") {
        const node = getNodes().find((n) => n.id === id);
        if (node) onNodeClick({} as any, node);
      }
    } else if (type === "edge") {
      if (action === "delete") {
        try {
          setSavingState("saving");
          await api.delete(`/edge/${id}`);
          setEdges((edges) => edges.filter((e) => e.id !== id));
          const now = new Date();
          setLastSavedAt(now);
          setSavingState("saved");
        } catch (error: any) {
          setSavingState("error");
          console.error("删除边失败:", error);
          showError(`删除连线失败: ${error.response?.data?.message || "无法连接至服务器"}`);
        }
      } else if (action === "properties") {
        const edge = getEdges().find((e) => e.id === id);
        if (edge) onEdgeClick({} as any, edge);
      }
    }
  }

  // 更新节点数据
  function updateNodeData(key: string, value: any) {
    if (!selectedElement || selectedElement.type !== "node") return;
    setSidebarData((prev: any) => ({ ...prev, [key]: value }));
    setNodes((nodes) =>
      nodes.map((n) => {
        if (n.id === selectedElement.id) {
          const newData = { ...n.data, [key]: value };
          if (key === "name") newData.label = value;
          return { ...n, data: newData };
        }
        return n;
      })
    );
    // 标记需要保存
    markNodeForSave(selectedElement.id, { [key]: value });
  }

  // 更新边数据
  function updateEdgeData(key: string, value: any) {
    if (!selectedElement || selectedElement.type !== "edge") return;
    setSidebarData((prev: any) => ({ ...prev, [key]: value }));
    setEdges((edges) =>
      edges.map((e) => {
        if (e.id === selectedElement.id) {
          return { ...e, data: { ...e.data, [key]: value } };
        }
        return e;
      })
    );
    // 标记需要保存
    markEdgeForSave(selectedElement.id, { [key]: value });
  }

  // 处理侧边栏关闭
  async function handleSidebarClose() {
    await savePendingChanges();
    setSidebarView("none");
    setSelectedElement(null);
    setCurrentScriptId(null);
    setShowScriptDetail(false);
    setNodes((nodes) => nodes.map((n) => ({ ...n, selected: false })));
    setEdges((edges) =>
      edges.map((e) => ({
        ...e,
        style: { ...e.style, strokeWidth: 2 },
        selected: false,
      }))
    );
  }

  // 查看话术详情
  function handleViewScript(scriptId: string) {
    setCurrentScriptId(scriptId);
    setShowScriptDetail(true);
  }

  // 删除话术
  async function handleDeleteScript(scriptId: string) {
    if (!selectedElement || selectedElement.type !== "node") return;
    try {
      await withSaveState(() => api.delete(`/script/${scriptId}`));
      setScripts((prev) => prev.filter((s) => s.id !== scriptId));

      // 触发NodeSidebar刷新
      setRefreshTrigger((prev) => prev + 1);
    } catch (error: any) {
      console.error("删除话术失败:", error);
      showError(`删除话术失败: ${error.response?.data?.message || "无法连接至服务器"}`);
    }
  }

  // 从话术详情返回
  function handleBackFromScript() {
    setShowScriptDetail(false);
    setCurrentScriptId(null);
  }

  // 更新话术（发送给后端前把 tag 转为字符串；如果后端不返回 data，则使用本地 updatedScript 并做 tag 转换）
  async function handleUpdateScript(updatedScript: IScript) {
    try {
      const { id, ...data } = updatedScript;
      const content = data.content;
      const conditions = data.conditions;

      // 校验话术文本
      const contentRes = await api.post("/interpreter/template/legal", content);
      const contentData = contentRes?.data?.data;
      // 如果后端返回非空字符串，则视为错误信息并中断
      if (typeof contentData === "string" && contentData.trim() !== "") {
        throw new Error(contentData);
      }

      // 校验条件表达式
      const conditionsRes = await api.post("/interpreter/condition/legal", conditions);
      const condData = conditionsRes?.data?.data;
      if (condData && typeof condData === "object") {
        const warnings = Array.isArray(condData.warnings) ? condData.warnings : [];
        const errorsArr = Array.isArray(condData.errors) ? condData.errors : [];
        if (warnings.length > 0 || errorsArr.length > 0) {
          const parts: string[] = [];
          if (errorsArr.length > 0) parts.push(`错误: ${errorsArr.join("；")}`);
          if (warnings.length > 0) parts.push(`警告: ${warnings.join("；")}`);
          throw new Error(parts.join("。"));
        }
      }

      await withSaveState(() => api.put(`/script/${id}`, data));
      const normalized = { ...updatedScript, tag: BackendTagToFrontendConverter(data.tag) } as IScript;
      setScripts((prev) => prev.map((s) => (s.id === id ? normalized : s)));

      // 触发NodeSidebar刷新
      setRefreshTrigger((prev) => prev + 1);
    } catch (error: any) {
      console.error("更新话术失败:", error);
      const message = error.response?.data?.message || error.message || "无法连接至服务器";
      showError(`更新话术失败: ${message}`);
    }
  }

  // 添加新话术
  async function handleAddScript() {
    if (!selectedElement || selectedElement.type !== "node") return;

    const scriptPayload = {
      name: "新话术",
      summary: " ",
      content: "",
      type: "NODE",
      associateId: selectedElement.id,
      tag: "",
      priority: 50,
      conditions: "{}",
    };

    try {
      const createdScript = (await withSaveState(() => api.post("/script/create", scriptPayload))).data.data;
      const normalized = {
        ...createdScript,
        tag: BackendTagToFrontendConverter((createdScript as any).tag),
      } as IScript;
      setScripts((prev) => [...prev, normalized]);

      // 直接进入编辑新话术
      setCurrentScriptId(normalized.id);
      setShowScriptDetail(true);

      // 触发NodeSidebar刷新
      setRefreshTrigger((prev) => prev + 1);
    } catch (error: any) {
      console.error("创建话术失败:", error);
      showError(`创建话术失败: ${error.response?.data?.message || "无法连接至服务器"}`);
    }
  }

  // 临时连线组件
  function TempLine() {
    if (interactionMode === "none" || !sourceNodeId || !mousePos) return null;

    const sourceNode = nodes.find((n) => n.id === sourceNodeId);
    if (!sourceNode) return null;

    const width = sourceNode.measured?.width ?? 144;
    const height = sourceNode.measured?.height ?? 44;

    const screenPos = flowToScreenPosition({
      x: sourceNode.position.x + width,
      y: sourceNode.position.y + height / 2,
    });

    const bounds = reactFlowWrapper.current?.getBoundingClientRect();
    if (!bounds) return null;

    const startPos = {
      x: screenPos.x - bounds.left,
      y: screenPos.y - bounds.top,
    };

    const endPos = mousePos;

    return (
      <svg
        style={{
          position: "absolute",
          top: 0,
          left: 0,
          width: "100%",
          height: "100%",
          pointerEvents: "none",
          zIndex: 999,
          overflow: "visible",
        }}>
        <path
          d={`M ${startPos.x} ${startPos.y} Q ${(startPos.x + endPos.x) / 2} ${(startPos.y + endPos.y) / 2} ${
            endPos.x
          } ${endPos.y}`}
          stroke={tokens.colorBrandBackground}
          strokeWidth="2"
          fill="none"
        />
      </svg>
    );
  }

  return (
    <div className={styles.container} ref={reactFlowWrapper} onMouseMove={onMouseMove}>
      {" "}
      {errorMessage && (
        <MessageBar
          intent="error"
          style={{
            position: "absolute",
            top: 0,
            left: 0,
            right: 0,
            zIndex: 10000,
          }}>
          <MessageBarBody>{errorMessage}</MessageBarBody>
        </MessageBar>
      )}{" "}
      <div className={styles.title}>
        <Title1>流程配置</Title1>
        <InfoLabel
          size="large"
          info={
            <>
              右键节点或连线以打开操作菜单。点击画布空白处关闭菜单或侧边栏。
              <br />
              在菜单中选择“新建节点”后，点击画布空白处以创建新节点并自动连接。
              <br />
              在菜单中选择“连接”后，点击目标节点以创建连线。
              <br />
              选择节点或连线后，可在侧边栏编辑其属性。编辑完成后，点击侧栏右上角的关闭按钮保存修改并关闭侧边栏。
            </>
          }></InfoLabel>

        <div className={styles["saveStatus"]}>
          {savingState === "saving" && (
            <>
              <Spinner size="extra-tiny" />
              <span>正在保存</span>
            </>
          )}
          {savingState === "saved" && (
            <>
              <CheckmarkCircle20Color />
              <span>已保存 {formatTime(lastSavedAt)}</span>
            </>
          )}
          {savingState === "error" && (
            <>
              <DismissCircle20Color />
              <span>上次保存 {formatTime(lastSavedAt)}</span>
            </>
          )}
        </div>
      </div>
      <ReactFlow<CustomNodeType, FlowEdge>
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onNodeContextMenu={onNodeContextMenu}
        onEdgeContextMenu={onEdgeContextMenu}
        onNodeClick={onNodeClick}
        onEdgeClick={onEdgeClick}
        onNodeDragStart={onNodeDragStart}
        onNodeDragStop={onNodeDragStop}
        onPaneClick={onPaneClickEvent}
        nodeTypes={nodeTypes}
        deleteKeyCode={[]}
        onNodesDelete={handleNodesDelete}
        onEdgesDelete={handleEdgesDelete}
        defaultViewport={{ x: 0, y: 0, zoom: 0.9 }}
        proOptions={{ hideAttribution: true }}>
        <Background />
        <Controls className={styles.flowControls} />
      </ReactFlow>
      {/* 临时连线覆盖层 */}
      {interactionMode !== "none" && mousePos && <TempLine />}
      {/* 上下文菜单 */}
      {menu && (
        <div className={styles.contextMenu} style={{ top: menu.top, left: menu.left }}>
          {menu.type === "node" && (
            <>
              <div className={styles.contextMenuItem} onClick={() => handleMenuAction("new_node")}>
                <AddCircle24Regular />
                <span>新建节点</span>
              </div>
              <div className={styles.contextMenuItem} onClick={() => handleMenuAction("connect")}>
                <Link24Regular />
                <span>连接</span>
              </div>
              <div className={styles.contextMenuItem} onClick={() => handleMenuAction("properties")}>
                <Edit24Regular />
                <span>属性</span>
              </div>
              <div className={styles.contextMenuItem} onClick={() => handleMenuAction("delete")}>
                <Delete24Regular />
                <span>删除节点</span>
              </div>
            </>
          )}
          {menu.type === "edge" && (
            <>
              <div className={styles.contextMenuItem} onClick={() => handleMenuAction("properties")}>
                <Edit24Regular />
                <span>属性</span>
              </div>
              <div className={styles.contextMenuItem} onClick={() => handleMenuAction("delete")}>
                <Delete24Regular />
                <span>删除线路</span>
              </div>
            </>
          )}
        </div>
      )}
      {/* 侧边栏 */}
      <div className={`${styles.sidebar} ${sidebarView !== "none" ? styles.sidebarOpen : ""}`}>
        {sidebarView === "node" && sidebarData && (
          <NodeSidebar
            key={`node-sidebar-${sidebarData.id}-${refreshTrigger}`}
            data={sidebarData}
            onUpdate={updateNodeData}
            onViewScript={handleViewScript}
            onDeleteScript={handleDeleteScript}
            onAddScript={handleAddScript}
            onClose={handleSidebarClose}
          />
        )}
        {sidebarView === "edge" && sidebarData && (
          <EdgeSidebar data={sidebarData} onUpdate={updateEdgeData} onClose={handleSidebarClose} />
        )}
      </div>
      {/* 话术详情侧边栏 */}
      <div className={`${styles.scriptSidebar} ${showScriptDetail ? styles.scriptSidebarOpen : ""}`}>
        {showScriptDetail && currentScriptId && scripts.find((s) => s.id === currentScriptId) && (
          <ScriptDetailSidebar
            script={scripts.find((s) => s.id === currentScriptId)!}
            onUpdate={handleUpdateScript}
            onBack={handleBackFromScript}
          />
        )}
      </div>
    </div>
  );
}

export default function ProcessConfigurationPage() {
  return (
    <ReactFlowProvider>
      <ProcessConfigurationPageContent />
    </ReactFlowProvider>
  );
}
