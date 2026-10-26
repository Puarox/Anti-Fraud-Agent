import {
  Button,
  Input,
  InteractionTag,
  InteractionTagPrimary,
  InteractionTagSecondary,
  Label,
  SearchBox,
  SpinButton,
  TagGroup,
  Text,
  Textarea,
  tokens,
} from "@fluentui/react-components";
import { Add24Regular, Dismiss24Regular } from "@fluentui/react-icons";
import React, { useCallback, useEffect, useRef, useState } from "react";
import { useStyles } from "../styles/SideBarStyle.ts";
import type { ICondition, IScript } from "../types/IScript";
import api from "../utils/Api";
import {
  ConditionsDisplayToKeyConverter,
  ConditionsKeyToDisplayConverter,
  StringDisplayToKeyConverter,
  StringKeyToDisplayConverter,
} from "../utils/ConditionConverter.ts";
import { FrontendTagToBackendConverter } from "../utils/ScriptTagConverter.ts";
import ConditionNode from "./ConditionNode";

interface ScriptDetailSidebarProps {
  script: IScript;
  onUpdate: (script: IScript) => void;
  onBack: () => void;
}

export function ScriptDetailSidebar({ script, onUpdate, onBack }: ScriptDetailSidebarProps) {
  const styles = useStyles();
  const [localScript, setLocalScript] = useState<IScript>(script);
  const [tagInput, setTagInput] = useState("");
  const [variableSearchTerm, setVariableSearchTerm] = useState("");
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [contextMenuPos, setContextMenuPos] = useState<{ x: number; y: number } | null>(null);
  const [contextMenuTarget, setContextMenuTarget] = useState<{
    type: "textarea" | "input";
    conditionIndex?: number;
    field?: string;
  } | null>(null);
  const [variablePlaceholders, setVariablePlaceholders] = useState<Record<string, string> | null>(null);
  const fieldGroupRef = useRef<HTMLDivElement>(null);
  const updateTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // 当父组件的 script 或 placeholders 发生变化时，同步到本地状态（用于显示与编辑），并把后端的 key 转为展示用的中文
  useEffect(() => {
    if (!variablePlaceholders) {
      // 变量占位还没加载时直接写入原始脚本（后续 placeholders 加载会触发本 effect 再次执行）
      setLocalScript(script);
      return;
    }

    const converted: IScript = {
      ...script,
      content: StringKeyToDisplayConverter(script.content, variablePlaceholders),
      conditions: ConditionsKeyToDisplayConverter(script.conditions, variablePlaceholders) as unknown as any,
    } as IScript;

    setLocalScript(converted);
  }, [script, variablePlaceholders]);

  // 防抖提交到后端（负责将本地更改异步发送给父组件/后端，但不影响本地输入体验）
  function debouncedPersistUpdate(updatedScript: IScript) {
    if (updateTimerRef.current) {
      clearTimeout(updateTimerRef.current);
    }
    updateTimerRef.current = setTimeout(() => {
      // 在发送给父组件/后端前把展示用的中文变量（{中文}）转为后端需要的英文 key（{key}）
      const payload: IScript = {
        ...updatedScript,
        content: StringDisplayToKeyConverter(updatedScript.content, variablePlaceholders),
        conditions: ConditionsDisplayToKeyConverter(updatedScript.conditions, variablePlaceholders) as unknown as any,
        tag: FrontendTagToBackendConverter(updatedScript.tag),
      } as IScript;
      onUpdate(payload);
    }, 500);
  }

  // 清理定时器
  useEffect(() => {
    return () => {
      if (updateTimerRef.current) {
        clearTimeout(updateTimerRef.current);
      }
    };
  }, []);

  useEffect(() => {
    async function fetchVariablePlaceholders() {
      try {
        const res = await api.get<{ data: Record<string, string> }>("/interpreter/interface/list");
        const payload = res.data?.data;
        if (payload && typeof payload === "object" && !Array.isArray(payload)) {
          const record: Record<string, string> = Object.fromEntries(
            Object.entries(payload).map(([k, v]) => [k, String(v)])
          );
          setVariablePlaceholders(record);
        } else {
          setVariablePlaceholders({});
        }
      } catch (error: any) {
        console.error("Failed to fetch variable placeholders:", error);
      }
    }

    fetchVariablePlaceholders();
  }, []);

  const handleContextMenu = useCallback(
    (
      e: React.MouseEvent<HTMLTextAreaElement | HTMLInputElement>,
      target: { type: "textarea" | "input"; conditionPath?: number[]; field?: string }
    ) => {
      e.preventDefault();

      const fieldGroup = fieldGroupRef.current;
      if (!fieldGroup) return;

      // 获取fieldGroup相对于视口的位置
      const rect = fieldGroup.getBoundingClientRect();

      // 计算相对于fieldGroup的鼠标位置
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      // 菜单的估计尺寸
      const menuWidth = 215;
      const menuHeight = 350;

      // 计算实际显示位置，确保不超出fieldGroup
      let finalX = x;
      let finalY = y;

      // 如果菜单会超出右边界，则让菜单右边紧贴鼠标位置
      if (x + menuWidth > rect.width) {
        finalX = x - menuWidth;
      }

      // 如果菜单会超出底部边界，则向上调整
      if (y + menuHeight > rect.height) {
        finalY = Math.max(0, y - menuHeight);
      }

      // 确保不超出左边界
      finalX = Math.max(0, finalX);

      setVariableSearchTerm("");
      setContextMenuPos({ x: finalX, y: finalY });
      setContextMenuTarget(target);
    },
    []
  );

  function insertVariable(key: string, value: string) {
    if (!contextMenuTarget) return;

    if (contextMenuTarget.type === "textarea") {
      const textarea = textareaRef.current;
      if (!textarea) return;

      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      const text = localScript.content || "";
      const before = text.substring(0, start);
      const after = text.substring(end);
      const newText = `${before}{${value}}${after}`; // 显示使用中文 value，带花括号

      const updatedScript = { ...localScript, content: newText };
      setLocalScript(updatedScript);
      debouncedPersistUpdate(updatedScript);

      // 设置光标位置（估算）
      setTimeout(() => {
        textarea.selectionStart = textarea.selectionEnd = start + value.length + 2; // 包含花括号
        textarea.focus();
      }, 0);
    } else if (contextMenuTarget.type === "input") {
      // 处理条件Input的插入（在末尾追加，带花括号显示中文）
      const { conditionPath, field } = contextMenuTarget as any;
      if (!Array.isArray(conditionPath) || conditionPath.length === 0 || !field) return;

      // 规范化当前条件为数组（展示用）
      const arr = ConditionsKeyToDisplayConverter(localScript.conditions, variablePlaceholders ?? undefined);

      // 定位到目标节点
      let node: any = null;
      let parent: any = null;
      let parentArr: any = arr;
      for (let i = 0; i < conditionPath.length; i++) {
        const idx = conditionPath[i];
        parent = node;
        node = parentArr[idx];
        parentArr = node?.sub || [];
        if (!node) break;
      }

      if (node && node.type === "ATOMIC") {
        const currentValue = (node[field as "lhs" | "rhs"] as string) || "";
        const newValue = currentValue + `{${value}}`;
        if (field === "lhs") node.lhs = newValue;
        else node.rhs = newValue;

        const updatedScript = { ...localScript, conditions: arr } as IScript;
        setLocalScript(updatedScript);
        debouncedPersistUpdate(updatedScript);
      }
    }

    setContextMenuPos(null);
    setContextMenuTarget(null);
  }

  function handleTagAdd() {
    const trimmed = tagInput.trim();
    if (!trimmed) return;

    const existingTags = localScript.tag ?? [];
    const exists = existingTags.some((t) => t.toLowerCase() === trimmed.toLowerCase());
    if (exists) {
      // 若已存在相同（不区分大小写）的 tag，则不再添加，清空输入
      setTagInput("");
      return;
    }

    const updated = { ...localScript, tag: [...existingTags, trimmed] };
    setLocalScript(updated);
    debouncedPersistUpdate(updated);
    setTagInput("");
  }

  function handleTagRemove(tagToRemove: string) {
    const updated = { ...localScript, tag: (localScript.tag ?? []).filter((t) => t !== tagToRemove) };
    setLocalScript(updated);
    debouncedPersistUpdate(updated);
  }

  function handleConditionUpdate(index: number, updated: ICondition) {
    const arr = ConditionsKeyToDisplayConverter(localScript.conditions, variablePlaceholders ?? undefined);
    arr[index] = updated;
    const updatedScript = { ...localScript, conditions: arr };
    setLocalScript(updatedScript);
    debouncedPersistUpdate(updatedScript);
  }

  function handleConditionDelete(index: number) {
    const arr = ConditionsKeyToDisplayConverter(localScript.conditions, variablePlaceholders ?? undefined);
    const newConditions = arr.filter((_, i) => i !== index);
    const updatedScript = { ...localScript, conditions: newConditions };
    setLocalScript(updatedScript);
    debouncedPersistUpdate(updatedScript);
  }

  function handleAddCondition() {
    const arr = ConditionsKeyToDisplayConverter(localScript.conditions, variablePlaceholders ?? undefined);
    const updatedScript = {
      ...localScript,
      conditions: [...arr, { type: "ATOMIC" as const, lhs: "", op: "eq" as const, rhs: "" }],
    };
    setLocalScript(updatedScript);
    debouncedPersistUpdate(updatedScript);
  }

  return (
    <>
      <div
        style={{
          padding: "16px",
          borderBottom: "1px solid #e0e0e0",
          display: "flex",
          alignItems: "center",
          gap: "12px",
        }}>
        <Button appearance="subtle" icon={<Dismiss24Regular />} onClick={onBack} />
        <h2 style={{ margin: 0 }}>话术详情</h2>
      </div>
      <div className={styles.sidebarContent}>
        <div className={styles.fieldGroup}>
          <Label>话术名称</Label>
          <Input
            value={localScript.name}
            onChange={(e, d) => {
              const updated = { ...localScript, name: d.value };
              setLocalScript(updated);
              debouncedPersistUpdate(updated);
            }}
            placeholder="请输入话术名称"
          />
        </div>

        <div className={styles.fieldGroup}>
          <Label>话术摘要</Label>
          <Input
            value={localScript.summary}
            onChange={(e, d) => {
              const updated = { ...localScript, summary: d.value };
              setLocalScript(updated);
              debouncedPersistUpdate(updated);
            }}
            placeholder="请输入话术摘要"
          />
        </div>

        <div className={styles.fieldGroup} ref={fieldGroupRef} style={{ position: "relative" }}>
          <Label>话术内容</Label>
          <Textarea
            ref={textareaRef}
            value={localScript.content}
            onChange={(e, d) => {
              const updated = { ...localScript, content: d.value };
              setLocalScript(updated);
              debouncedPersistUpdate(updated);
            }}
            onContextMenu={(e) => handleContextMenu(e, { type: "textarea" })}
            placeholder="请输入话术内容，右键插入变量"
            rows={3}
          />
          {/* 变量插入菜单 */}
          {contextMenuPos && (
            <div
              className={styles.variableMenu}
              style={{
                position: "absolute",
                top: contextMenuPos.y,
                left: contextMenuPos.x,
                zIndex: 10000,
              }}>
              <div className={styles.variableMenuHeader}>
                <Text>插入变量</Text>
                <SearchBox
                  size="small"
                  placeholder="搜索变量"
                  appearance="underline"
                  value={variableSearchTerm}
                  onChange={(_, d) => setVariableSearchTerm(d.value)}
                  style={{ width: "120px" }}
                />
              </div>

              <div className={styles.variableMenuList}>
                {Object.entries(variablePlaceholders ?? {})
                  .filter(([_, value]) => value.toLowerCase().includes(variableSearchTerm.toLowerCase()))
                  .map(([key, value]) => (
                    <div key={key} className={styles.variableMenuItem} onClick={() => insertVariable(key, value)}>
                      {value}
                    </div>
                  ))}
              </div>
            </div>
          )}
          {contextMenuPos && (
            <div
              style={{
                position: "fixed",
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                zIndex: 9999,
              }}
              onClick={() => setContextMenuPos(null)}
            />
          )}
        </div>

        <div className={styles.fieldGroup}>
          <Label>话术标签</Label>
          <Input
            value={tagInput}
            onChange={(e, d) => setTagInput(d.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                handleTagAdd();
              }
            }}
            placeholder="输入标签后按回车添加"
          />
          {localScript.tag && localScript.tag.length > 0 && (
            <TagGroup
              onDismiss={(_e, { value }) => handleTagRemove(value as string)}
              style={{ display: "flex", flexWrap: "wrap", gap: "8px", marginTop: "8px" }}>
              {localScript.tag?.map((tag, index) => (
                <InteractionTag value={tag} key={index} shape="circular" appearance="brand">
                  <InteractionTagPrimary hasSecondaryAction>{tag}</InteractionTagPrimary>
                  <InteractionTagSecondary aria-label="remove" />
                </InteractionTag>
              ))}
            </TagGroup>
          )}
        </div>

        <div className={styles.fieldGroup}>
          <Label>话术优先级</Label>
          <SpinButton
            value={localScript.priority}
            onChange={(e, d) => {
              const val = d.value || 0;
              const updated = { ...localScript, priority: val };
              setLocalScript(updated);
              debouncedPersistUpdate(updated);
            }}
            min={0}
            max={100}
          />
        </div>

        <div className={styles.fieldGroup}>
          <Label>话术条件</Label>
          <div className={styles.conditionTree}>
            {(Array.isArray(localScript.conditions)
              ? localScript.conditions
              : ConditionsKeyToDisplayConverter(localScript.conditions, variablePlaceholders ?? undefined)
            ).map((condition, index) => (
              <ConditionNode
                key={index}
                condition={condition}
                depth={0}
                indexPath={[index]}
                onUpdate={(updated) => handleConditionUpdate(index, updated)}
                onDelete={() => handleConditionDelete(index)}
                onContextMenu={handleContextMenu}
              />
            ))}
          </div>
          <Button appearance="primary" icon={<Add24Regular />} onClick={handleAddCondition}>
            添加条件
          </Button>
        </div>
      </div>
    </>
  );
}
