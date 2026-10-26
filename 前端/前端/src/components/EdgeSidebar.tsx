import { Button, Input, Label } from "@fluentui/react-components";
import { Dismiss24Regular } from "@fluentui/react-icons";
import { useStyles } from "../styles/SideBarStyle";

interface EdgeSidebarProps {
  data: any;
  onUpdate: (key: string, value: any) => void;
  onClose: () => void;
}

export function EdgeSidebar({ data, onUpdate, onClose }: EdgeSidebarProps) {
  const styles = useStyles();

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
        <h2 style={{ margin: 0 }}>线路属性</h2>
        <Button appearance="subtle" icon={<Dismiss24Regular />} onClick={onClose} />
      </div>
      <div className={styles.sidebarContent}>
        <div className={styles.fieldGroup}>
          <Label>线路名称</Label>
          <Input value={data?.name || ""} onChange={(e, d) => onUpdate("name", d.value)} placeholder="请输入线路名称" />
        </div>
      </div>
    </>
  );
}
