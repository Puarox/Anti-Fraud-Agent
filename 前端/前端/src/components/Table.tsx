import {
  Button,
  Spinner,
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableHeaderCell,
  TableRow,
  TableSelectionCell,
  useFluent,
  useId,
  useScrollbarWidth,
  useToastController,
  type TableColumnDefinition,
  type TableRowData,
} from "@fluentui/react-components";
import { AddRegular } from "@fluentui/react-icons";
import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router";
import { List } from "react-window";

// 自适应大小组件
const AutoSizer = ({ children }: { children: (size: { height: number; width: number }) => React.ReactNode }) => {
  const ref = useRef<HTMLDivElement>(null);
  const [size, setSize] = useState({ width: 0, height: 0 });

  useEffect(() => {
    if (!ref.current) return;
    const observer = new ResizeObserver((entries) => {
      const entry = entries[0];
      if (entry) {
        setSize({ width: entry.contentRect.width, height: entry.contentRect.height });
      }
    });
    observer.observe(ref.current);
    return () => observer.disconnect();
  }, []);

  return (
    <div ref={ref} style={{ width: "100%", height: "100%", overflow: "hidden" }}>
      {size.width > 0 && size.height > 0 && children(size)}
    </div>
  );
};

// 定义表格属性
export interface TableDisplayProps {
  reset?: boolean;
  originPage: string;
  rows: any[];
  columns: any[];
  loading: boolean;
  error: string | null;
  allRowsSelected: boolean;
  someRowsSelected: boolean;
  withAddButton: boolean;
  fetchData: () => void;
  toggleAllRows: (e: React.MouseEvent) => void;
  toggleAllKeydown: (e: React.KeyboardEvent<HTMLDivElement>) => void;
  getRows: (rowRenderer: (row: TableRowData<any>) => any) => any[];
  isRowSelected: (rowId: string | number) => boolean;
  toggleRow: (e: React.SyntheticEvent, rowId: string | number) => void;
  onSelectionChange: (selectedItems: any[]) => void;
  onAddClick?: () => void;
  onBatchDeleteClick?: () => void;
}

// 渲染行组件
const RenderRow = ({ index, style, ...rowProps }: any) => {
  const toasterId = useId("toaster");
  const { dispatchToast } = useToastController(toasterId);
  const [Message, setMessage] = useState({
    success: false,
    message: "",
  });
  const { originPage, columns = [], rows = [] } = rowProps || {};
  const row = rows[index] || {};

  return (
    <div>
      <TableRow
        aria-rowindex={index + 2}
        style={style}
        onClick={(e) => row.onClick(e)}
        onKeyDown={(e) => row.onKeyDown(e)}
        appearance={row.selected ? "brand" : "none"}>
        <TableSelectionCell
          style={{
            width: "100%",
          }}
          checked={row.selected}
          checkboxIndicator={{ "aria-label": "选择行" }}
        />

        {columns.map((column: TableColumnDefinition<any>) => (
          <TableCell
            key={column.columnId}
            style={{
              textAlign: "left",
              whiteSpace: "nowrap", // 防止换行
              overflow: "hidden", // 隐藏溢出内容
              textOverflow: "ellipsis", // 显示省略号
              width: `calc(100% / ${columns.length})`,
              padding: "8px 12px", // 保持原有内边距
              boxSizing: "border-box", // 确保内边距不影响宽度计算
            }}>
            {column.renderCell?.(row.item) ?? "--"}
          </TableCell>
        ))}
      </TableRow>
    </div>
  );
};

const RenderAddButton = (withAddButton: boolean, handleAdd: React.MouseEventHandler<HTMLButtonElement> | undefined) => {
  if (withAddButton) {
    return (
      <div style={{ marginTop: "20px", textAlign: "right", display: "none" }}>
        <Button
          appearance="primary"
          style={{ width: "100px", display: "none" }}
          icon={<AddRegular />}
          onClick={handleAdd}>
          新增
        </Button>
      </div>
    );
  } else {
    return null;
  }
};

// 加载状态组件
const LoadingSpinner = () => (
  <TableRow>
    <TableCell colSpan={6}>
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          height: 400,
        }}>
        <Spinner size="huge" label="正在加载数据..." />
      </div>
    </TableCell>
  </TableRow>
);

// 空状态组件
const EmptyState = () => (
  <TableRow>
    <TableCell colSpan={6} align="center">
      <div style={{ padding: "40px", textAlign: "center", color: "#666" }}>没有找到数据</div>
    </TableCell>
  </TableRow>
);

// 错误状态组件
const ErrorState = ({ error, onRetry }: { error: string; onRetry: () => void }) => (
  <TableRow>
    <TableCell colSpan={6} align="center">
      <div style={{ padding: "40px", textAlign: "center" }}>
        <p style={{ color: "#d13438", marginBottom: "20px" }}>加载失败: {error}</p>
        <Button appearance="primary" onClick={onRetry}>
          重试
        </Button>
      </div>
    </TableCell>
  </TableRow>
);

// 表格显示组件
export const TableDisplay = (props: TableDisplayProps) => {
  const { targetDocument } = useFluent();
  const scrollbarWidth = useScrollbarWidth({ targetDocument });
  const navigate = useNavigate();
  const {
    originPage,
    rows,
    columns,
    loading,
    error,
    fetchData,
    allRowsSelected,
    someRowsSelected,
    toggleAllRows: propToggleAllRows,
    toggleAllKeydown: propToggleAllKeydown,
    getRows,
    isRowSelected,
    toggleRow: propToggleRow,
    onSelectionChange,
    withAddButton = true,
    onAddClick,
    onBatchDeleteClick,
  } = props;

  // 使用 ref 存储虚拟点击目标
  const virtualClickTargetRef = useRef<HTMLDivElement>(null);

  // 处理新增操作
  const handleAdd = () => {
    if (onAddClick) {
      onAddClick();
    } else {
      navigate(`/archive/${originPage}/add`, {
        state: {
          current: "",
          mode: "add",
          origin: originPage,
        },
      });
    }
  };

  // 触发虚拟点击
  const triggerVirtualClick = () => {
    if (virtualClickTargetRef.current) {
      const clickEvent = new MouseEvent("click", {
        view: window,
        bubbles: true,
        cancelable: true,
      });
      virtualClickTargetRef.current.dispatchEvent(clickEvent);
    }
  };

  // 处理行选中状态变化
  const handleToggleRow = (e: React.SyntheticEvent, rowId: string | number) => {
    propToggleRow(e, rowId);

    // 使用 setTimeout 确保在状态更新后触发虚拟点击
    setTimeout(() => {
      triggerVirtualClick();
    }, 0);
  };

  // 处理全选操作
  const handleToggleAllRows = (e: React.MouseEvent) => {
    propToggleAllRows(e);

    // 使用 setTimeout 确保在状态更新后触发虚拟点击
    setTimeout(() => {
      triggerVirtualClick();
    }, 0);
  };

  // 处理全选键盘事件
  const handleToggleAllKeydown = (e: React.KeyboardEvent<HTMLDivElement>) => {
    propToggleAllKeydown(e);

    // 使用 setTimeout 确保在状态更新后触发虚拟点击
    setTimeout(() => {
      triggerVirtualClick();
    }, 0);
  };

  // 处理虚拟点击事件
  const handleVirtualClick = () => {
    const selectedRows = getRows((row) => row)
      .filter((row) => isRowSelected(row.rowId))
      .map((row) => row.item);

    onSelectionChange(selectedRows);
  };

  // 生成表格行数据
  const tableRows = getRows((row) => ({
    ...row,
    onClick: (e: React.MouseEvent) => handleToggleRow(e, row.rowId),
    onKeyDown: (e: React.KeyboardEvent) => {
      if (e.key === " ") {
        handleToggleRow(e, row.rowId);
        e.preventDefault();
      }
    },
    selected: isRowSelected(row.rowId),
  }));

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", width: "100%", overflow: "hidden" }}>
      {/* 虚拟点击目标 - 隐藏但可点击 */}
      <div
        ref={virtualClickTargetRef}
        onClick={handleVirtualClick}
        style={{
          position: "absolute",
          width: "1px",
          height: "1px",
          opacity: 0,
          pointerEvents: "none",
        }}
      />

      {RenderAddButton(withAddButton, handleAdd)}

      <Table
        noNativeElements
        aria-label="档案信息表"
        aria-rowcount={columns.length}
        style={{
          width: "100%",
          textAlign: "left",
          display: "flex",
          flexDirection: "column",
          flex: 1,
          height: "100%",
          overflow: "hidden",
        }}>
        <TableHeader
          style={{
            width: "100%",
            flex: "0 0 auto",
          }}>
          <TableRow aria-rowindex={1}>
            {/* 选择列 */}
            <TableSelectionCell
              checked={allRowsSelected ? true : someRowsSelected ? "mixed" : false}
              onClick={handleToggleAllRows}
              onKeyDown={handleToggleAllKeydown}
              checkboxIndicator={{ "aria-label": "全选" }}
              style={{
                textAlign: "center", // 内容居中
              }}
            />

            {columns.map((column) => (
              <TableHeaderCell
                key={column.key}
                style={{
                  fontWeight: "bold",
                  textAlign: "center", // 标题居中
                  padding: "0px 12px",
                  boxSizing: "border-box",
                  whiteSpace: "nowrap", // 防止换行
                  overflow: "hidden",
                  textOverflow: "ellipsis",
                }}>
                {column.columnId}
              </TableHeaderCell>
            ))}

            {/** 滚动条对齐 */}
            <div role="presentation" style={{ width: scrollbarWidth }} />
          </TableRow>
        </TableHeader>

        <TableBody
          style={{ flex: "1 1 auto", overflow: "hidden", width: "100%", display: "block", position: "relative" }}>
          <AutoSizer>
            {({ height, width }) => (
              <>
                {loading ? (
                  <LoadingSpinner />
                ) : error ? (
                  <ErrorState error={error} onRetry={fetchData} />
                ) : rows.length > 0 ? (
                  <List
                    style={{
                      textDecoration: "overline",
                      width: width,
                      height: height,
                    }}
                    rowCount={tableRows.length}
                    rowHeight={45}
                    rowComponent={RenderRow}
                    rowProps={{
                      columns: columns || [],
                      rows: tableRows,
                      originPage: props.originPage,
                    }}
                  />
                ) : (
                  <EmptyState />
                )}
              </>
            )}
          </AutoSizer>
        </TableBody>
      </Table>
    </div>
  );
};
