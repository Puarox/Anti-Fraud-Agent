import { Button, Dropdown, Input, Option } from "@fluentui/react-components";
import { Add24Regular, Delete24Regular } from "@fluentui/react-icons";
import { useStyles } from "../styles/SideBarStyle";
import { type ICondition, conditionTypeLabels } from "../types/IScript";

// 条件树节点组件
export default function ConditionNode({
  condition,
  depth,
  indexPath,
  onUpdate,
  onDelete,
  onContextMenu,
}: {
  condition: ICondition;
  depth: number;
  indexPath: number[];
  onUpdate: (updated: ICondition) => void;
  onDelete: () => void;
  onContextMenu: (e: React.MouseEvent<HTMLInputElement>, target: any) => void;
}) {
  const styles = useStyles();

  function handleTypeChange(newType: string) {
    const updated: ICondition = { type: newType as any };
    if (newType === "ATOMIC") {
      updated.lhs = "";
      updated.op = "eq";
      updated.rhs = "";
    } else {
      updated.sub = [{ type: "ATOMIC" as const, lhs: "", op: "eq" as const, rhs: "" }];
    }
    onUpdate(updated);
  }

  function handleFieldChange(field: string, value: any) {
    onUpdate({ ...condition, [field]: value });
  }

  function handleSubConditionUpdate(index: number, updated: ICondition) {
    const newSub = [...(condition.sub || [])];
    newSub[index] = updated;
    onUpdate({ ...condition, sub: newSub });
  }

  function handleSubConditionDelete(index: number) {
    const newSub = (condition.sub || []).filter((_, i) => i !== index);
    onUpdate({ ...condition, sub: newSub });
  }

  function handleAddSubCondition() {
    const newSub = [...(condition.sub || []), { type: "ATOMIC" as const, lhs: "", op: "eq" as const, rhs: "" }];
    onUpdate({ ...condition, sub: newSub });
  }

  return (
    <div style={{ paddingLeft: depth > 0 ? "24px" : "0" }}>
      <div className={styles.conditionRow}>
        <Dropdown
          value={conditionTypeLabels[condition.type]}
          selectedOptions={[condition.type]}
          onOptionSelect={(e, d) => handleTypeChange(d.optionValue as string)}>
          <Option value="ATOMIC">{conditionTypeLabels["ATOMIC"]}</Option>
          <Option value="AND">{conditionTypeLabels["AND"]}</Option>
          <Option value="OR">{conditionTypeLabels["OR"]}</Option>
          <Option value="NOT">{conditionTypeLabels["NOT"]}</Option>
        </Dropdown>

        {condition.type === "ATOMIC" && (
          <>
            <Input
              style={{ flex: "1 1 180px", minWidth: "120px" }}
              value={(condition.lhs as string) || ""}
              onChange={(e, d) => handleFieldChange("lhs", d.value)}
              onContextMenu={(e) => onContextMenu(e, { type: "input", conditionPath: indexPath, field: "lhs" })}
              placeholder="左操作数"
            />
            <Dropdown
              value={conditionTypeLabels[condition.op || "eq"]}
              selectedOptions={[condition.op || "eq"]}
              className={styles.cambria}
              onOptionSelect={(e, d) => handleFieldChange("op", d.optionValue)}>
              <Option value="eq" className={styles.cambria}>
                {conditionTypeLabels["eq"]}
              </Option>
              <Option value="ne" className={styles.cambria}>
                {conditionTypeLabels["ne"]}
              </Option>
              <Option value="gt" className={styles.cambria}>
                {conditionTypeLabels["gt"]}
              </Option>
              <Option value="ge" className={styles.cambria}>
                {conditionTypeLabels["ge"]}
              </Option>
              <Option value="lt" className={styles.cambria}>
                {conditionTypeLabels["lt"]}
              </Option>
              <Option value="le" className={styles.cambria}>
                {conditionTypeLabels["le"]}
              </Option>
              <Option value="startswith">{conditionTypeLabels["startswith"]}</Option>
              <Option value="endswith">{conditionTypeLabels["endswith"]}</Option>
              <Option value="in">{conditionTypeLabels["in"]}</Option>
              <Option value="contains">{conditionTypeLabels["contains"]}</Option>
            </Dropdown>
            <Input
              style={{ flex: "1 1 180px", minWidth: "120px" }}
              value={(condition.rhs as string) || ""}
              onChange={(e, d) => handleFieldChange("rhs", d.value)}
              onContextMenu={(e) => onContextMenu(e, { type: "input", conditionPath: indexPath, field: "rhs" })}
              placeholder="右操作数"
            />
          </>
        )}

        <Button appearance="subtle" icon={<Delete24Regular />} onClick={onDelete} />
      </div>

      {condition.type !== "ATOMIC" && (
        <div style={{ marginTop: "8px" }}>
          {condition.sub?.map((subCondition, subIndex) => (
            <ConditionNode
              key={subIndex}
              condition={subCondition}
              depth={depth + 1}
              indexPath={[...indexPath, subIndex]}
              onUpdate={(updated) => handleSubConditionUpdate(subIndex, updated)}
              onDelete={() => handleSubConditionDelete(subIndex)}
              onContextMenu={onContextMenu}
            />
          ))}
          {condition.type !== "NOT" && (
            <div style={{ marginTop: "8px" }}>
              <Button appearance="subtle" icon={<Add24Regular />} onClick={handleAddSubCondition}>
                添加子条件
              </Button>
            </div>
          )}
          {condition.type === "NOT" && (!condition.sub || condition.sub.length === 0) && (
            <div style={{ marginTop: "8px" }}>
              <Button appearance="subtle" icon={<Add24Regular />} onClick={handleAddSubCondition}>
                添加子条件
              </Button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
