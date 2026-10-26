import { Button, Dropdown, Input, Label, Option } from "@fluentui/react-components";
import { Add24Regular, Delete24Regular, Dismiss24Regular, Eye24Regular } from "@fluentui/react-icons";
import { useEffect, useState } from "react";
import { nodeTypeLabels } from "../types/INode";
import type { IScript } from "../types/IScript";
import api from "../utils/Api";
import { useStyles } from "../styles/SideBarStyle";

interface NodeSidebarProps {
  data: any;
  onUpdate: (key: string, value: any) => void;
  onViewScript: (scriptId: string) => void;
  onDeleteScript: (scriptId: string) => Promise<void> | void;
  onAddScript: () => void;
  onClose: () => void;
}

export function NodeSidebar({ data, onUpdate, onViewScript, onDeleteScript, onAddScript, onClose }: NodeSidebarProps) {
  const styles = useStyles();

  const [scripts, setScripts] = useState<IScript[]>([]);

  useEffect(() => {
    const controller = new AbortController();
    async function loadScripts() {
      try {
        const res = await api.get<{ data: IScript[] }>(`/script/list/node/${data.id}`, { signal: controller.signal });
        const scriptList = res.data?.data || [];
        setScripts(scriptList);
      } catch (err: any) {
        if (err?.name === "CanceledError" || err?.code === "ERR_CANCELED") return;
        console.error("加载话术失败:", err);
        setScripts([]);
      }
    }
    loadScripts();
    return () => {
      controller.abort();
    };
  }, [data.id]);

  return (
    <>
      <div
        style={{
          padding: "16px",
          borderBottom: "1px solid #e0e0e0",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}>
        <h2 style={{ margin: 0 }}>节点属性</h2>
        <Button appearance="subtle" icon={<Dismiss24Regular />} onClick={onClose} />
      </div>
      <div className={styles.sidebarContent}>
        <div className={styles.fieldGroup}>
          <Label>节点名称</Label>
          <Input value={data?.name || ""} onChange={(e, d) => onUpdate("name", d.value)} placeholder="请输入节点名称" />
        </div>

        <div className={styles.fieldGroup}>
          <Label>节点类型</Label>
          <Dropdown
            value={nodeTypeLabels[data?.type] || ""}
            selectedOptions={[data?.type || "READ"]}
            onOptionSelect={(e, d) => onUpdate("type", d.optionValue)}>
            <Option value="READ">{nodeTypeLabels["READ"]}</Option>
            <Option value="CREATE">{nodeTypeLabels["CREATE"]}</Option>
            <Option value="UPDATE">{nodeTypeLabels["UPDATE"]}</Option>
            <Option value="DELETE">{nodeTypeLabels["DELETE"]}</Option>
            <Option value="START">{nodeTypeLabels["START"]}</Option>
            <Option value="END">{nodeTypeLabels["END"]}</Option>
          </Dropdown>
        </div>

        <div className={styles.fieldGroup}>
          <Label>关联话术</Label>
          <div className={styles.listContainer}>
            {scripts.length > 0 ? (
              scripts.map((script: IScript) => {
                return (
                  <Button key={script.id} className={styles.scriptItem} onClick={() => onViewScript(script.id)}>
                    <div className={styles.scriptInfo}>
                      <div className={styles.scriptName}>{script?.name || script.id}</div>
                      <div className={styles.scriptSummary}>{script?.summary || "暂无摘要"}</div>
                    </div>
                    <div className={styles.scriptActions}>
                      <Button
                        appearance="subtle"
                        size="small"
                        icon={<Eye24Regular />}
                        onClick={() => onViewScript(script.id)}
                      />
                      <Button
                        appearance="subtle"
                        size="small"
                        icon={<Delete24Regular />}
                        onClick={async () => {
                          try {
                            await onDeleteScript(script.id);
                            setScripts((prev) => prev.filter((s) => s.id !== script.id));
                          } catch (err) {
                            console.error("删除话术失败:", err);
                          }
                        }}
                      />
                    </div>
                  </Button>
                );
              })
            ) : (
              <div style={{ color: "#888", fontSize: "14px" }}>暂无关联话术</div>
            )}
          </div>
          <Button appearance="primary" icon={<Add24Regular />} onClick={onAddScript}>
            添加话术
          </Button>
        </div>
      </div>
    </>
  );
}
