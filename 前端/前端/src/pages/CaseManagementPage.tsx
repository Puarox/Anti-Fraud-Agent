import {
  Button,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  Field,
  Input,
  SearchBox,
  Spinner,
  Text,
  Textarea,
  Title1,
  Checkbox,
  createTableColumn,
  useTableFeatures,
  useTableSelection,
  useTableSort,
  useToastController,
  useId,
  Toast,
  ToastTitle,
  ToastBody,
  Table,
  TableHeader,
  TableRow,
  TableSelectionCell,
  TableHeaderCell,
  TableBody,
  TableCell,
  type TableColumnId,
} from "@fluentui/react-components";
import { AddRegular, DeleteRegular, EditRegular } from "@fluentui/react-icons";
import { useEffect, useState } from "react";
import { useStyles } from "../styles/AdminStyle";
import type { IFraudCase } from "../types/IFraudCase";
import api from "../utils/Api";

export default function CaseManagementPage() {
  const styles = useStyles();
  const [items, setItems] = useState<IFraudCase[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  useEffect(() => {
    const updatePageSize = () => {
      const rowHeight = 44; 
      // 这里的偏移量(约260-320px)包含：页面顶部Navigation、页边距、搜索栏Toolbar、表格Header及底部分页控件高度
      const offset = 260; 
      let newSize = Math.floor((window.innerHeight - offset-100) / rowHeight);
      if (newSize < 5) newSize = 5;
      setPageSize(newSize);
    };

    updatePageSize();
    window.addEventListener("resize", updatePageSize);
    return () => window.removeEventListener("resize", updatePageSize);
  }, []);

  const [riskFilters, setRiskFilters] = useState<{ [key: string]: boolean }>({
    低: true,
    中: true,
    高: true,
    极高: true,
  });
  const [minAmount, setMinAmount] = useState("");
  const [maxAmount, setMaxAmount] = useState("");
  const [minYear, setMinYear] = useState("");
  const [maxYear, setMaxYear] = useState("");

  const [openModal, setOpenModal] = useState(false);
  const [currentCase, setCurrentCase] = useState<Partial<IFraudCase>>({});
  const [isEdit, setIsEdit] = useState(false);

  const toasterId = useId("toaster");
  const { dispatchToast } = useToastController(toasterId);

  const notify = (title: string, body: string, intent: "success" | "error" | "warning" = "success") => {
    dispatchToast(
      <Toast>
        <ToastTitle>{title}</ToastTitle>
        <ToastBody>{body}</ToastBody>
      </Toast>,
      { intent }
    );
  };

  const fetchData = async () => {
    setLoading(true);
    try {
      // Backend api /fraud-case/list
      const res = await api.get("/fraud-case/list");
      if (res.data && res.data.success) {
        setItems(res.data.data);
      } else {
        setError(res.data?.message || "加载案件失败");
      }
    } catch (e) {
      setError("网络错误");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const filteredItems = items.filter((item) => {
    // 按风险等级过滤
    if (item.riskLevel && !riskFilters[item.riskLevel]) {
      return false;
    }
    // 未指定风险等级的项，如果任何一个过滤器取消勾选且它不在其中，这里简单处理就是按目前的riskFilters映射没有包括undefined的逻辑，
    // 一般来说有riskLevel才去判断。但安全起见，可以要求所有有效的只显示被勾选的级别。
    if (!item.riskLevel) {
      // 若需要显示空的，可放行；若不需要，这取决于业务。默认先放行。
    }

    // 按涉案金额区间过滤
    const amount = item.amountInvolved ?? 0;
    if (minAmount && amount < parseFloat(minAmount)) return false;
    if (maxAmount && amount > parseFloat(maxAmount)) return false;

    // 按发生年份区间过滤
    const year = item.caseYear ?? 0;
    if (minYear && year < parseInt(minYear)) return false;
    if (maxYear && year > parseInt(maxYear)) return false;

    // 关键字搜索 (移除查编号，只查标题和类型)
    if (searchQuery) {
      const lowerQuery = searchQuery.toLowerCase();
      const matchTitle = item.title && item.title.toLowerCase().includes(lowerQuery);
      const matchType = item.fraudType && item.fraudType.toLowerCase().includes(lowerQuery);
      if (!matchTitle && !matchType) return false;
    }

    return true;
  });

  const totalPages = Math.ceil(filteredItems.length / pageSize);
  const paginatedItems = filteredItems.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  useEffect(() => {
    if (currentPage > totalPages && totalPages > 0) {
      setCurrentPage(1);
    }
  }, [totalPages, currentPage]);

  const handleSave = async () => {
    try {
      if (isEdit) {
        const res = await api.put(`/fraud-case/update/${currentCase.id}`, currentCase);
        if (res.data.success) {
          notify("成功", "保存成功");
          setOpenModal(false);
          fetchData();
        } else {
          notify("错误", res.data.message, "error");
        }
      } else {
        const res = await api.post(`/fraud-case/create`, currentCase);
        if (res.data.success) {
          notify("成功", "添加成功");
          setOpenModal(false);
          fetchData();
        } else {
          notify("错误", res.data.message, "error");
        }
      }
    } catch (e) {
      notify("错误", "操作失败", "error");
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm("确定删除吗？")) return;
    try {
      const res = await api.delete(`/fraud-case/delete/${id}`);
      if (res.data.success) {
        notify("成功", "删除成功");
        fetchData();
      } else {
        notify("错误", res.data.message, "error");
      }
    } catch (e) {
      notify("错误", "操作失败", "error");
    }
  };

  const openAddModal = () => {
    setCurrentCase({
      caseId: "CASE-" + Date.now().toString(),
      title: "",
      riskLevel: "中",
      amountInvolved: 0,
      fraudType: "",
    });
    setIsEdit(false);
    setOpenModal(true);
  };

  const openEditModal = (item: IFraudCase) => {
    setCurrentCase({ ...item });
    setIsEdit(true);
    setOpenModal(true);
  };

  const [sortState, setSortState] = useState<{
    sortDirection: "ascending" | "descending";
    sortColumn: TableColumnId | undefined;
  }>({
    sortDirection: "descending",
    sortColumn: "标题",
  });

  const columns = [
    createTableColumn<IFraudCase>({
      columnId: "标题",
      compare: (a, b) => String(a.title || "").localeCompare(String(b.title || "")),
    }),
    createTableColumn<IFraudCase>({
      columnId: "诈骗类型",
      compare: (a, b) => String(a.fraudType || "").localeCompare(String(b.fraudType || "")),
    }),
    createTableColumn<IFraudCase>({
      columnId: "风险等级",
      compare: (a, b) => String(a.riskLevel || "").localeCompare(String(b.riskLevel || "")),
    }),
    createTableColumn<IFraudCase>({
      columnId: "涉案金额",
      compare: (a, b) => (a.amountInvolved || 0) - (b.amountInvolved || 0),
    }),
    createTableColumn<IFraudCase>({
      columnId: "发生年份",
      compare: (a, b) => (a.caseYear || 0) - (b.caseYear || 0),
    }),
    createTableColumn<IFraudCase>({
      columnId: "操作",
    }),
  ];

  const {
    getRows,
    selection: { allRowsSelected, someRowsSelected, toggleAllRows, toggleRow, isRowSelected },
    sort: { getSortDirection, toggleColumnSort, sort },
  } = useTableFeatures(
    {
      columns,
      items: filteredItems,
    },
    [
      useTableSelection({
        selectionMode: "multiselect",
        defaultSelectedItems: new Set(),
      }),
      useTableSort({
        sortState,
        onSortChange: (e, nextSortState) => setSortState(nextSortState),
      }),
    ]
  );

  function headerSortProps(columnId: TableColumnId) {
    return {
      onClick: (e: React.MouseEvent) => toggleColumnSort(e, columnId),
      sortDirection: getSortDirection(columnId),
    };
  }

  const sortedData = sort(getRows()).map((row) => row.item);
  const startIndex = (currentPage - 1) * pageSize;
  const endIndex = startIndex + pageSize;
  const pagedData = sortedData.slice(startIndex, endIndex);

  const tableRows = pagedData.map((item, index) => {
    const globalIndex = startIndex + index;
    const selected = isRowSelected(globalIndex);
    const rowClassName = index % 2 === 0 ? styles.evenRow : styles.oddRow;
    return {
      item,
      rowId: globalIndex,
      selected,
      onClick: (e: React.MouseEvent) => toggleRow(e, globalIndex),
      onKeyDown: (e: React.KeyboardEvent) => {
        if (e.key === " ") {
          e.preventDefault();
          toggleRow(e, globalIndex);
        }
      },
      rowClassName,
      appearance: selected ? ("brand" as const) : ("none" as const),
    };
  });

  return (
    <div className={styles.container}>
      <Title1>案件管理</Title1>

      <div className={styles.tableContainer}>
        <div className={styles.toolbar}>
          <SearchBox
            className={styles.searchBox}
            placeholder="搜索标题或类型..."
            value={searchQuery}
            onChange={(_, data) => setSearchQuery(data.value)}
          />

          <div style={{ display: "flex", gap: "10px", alignItems: "center", flexWrap: "wrap" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "4px" }}>
              <Text weight="semibold">风险等级:</Text>
              {["低", "中", "高", "极高"].map((level) => (
                <Checkbox
                  key={level}
                  label={level}
                  checked={riskFilters[level]}
                  onChange={(_, data) => {
                    setRiskFilters({ ...riskFilters, [level]: !!data.checked });
                    setCurrentPage(1);
                  }}
                />
              ))}
            </div>

            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <Text weight="semibold">涉案金额:</Text>
              <Input
                type="number"
                placeholder="最小金额"
                value={minAmount}
                onChange={(_, data) => {
                  setMinAmount(data.value);
                  setCurrentPage(1);
                }}
                style={{ width: 100 }}
              />
              <Text>-</Text>
              <Input
                type="number"
                placeholder="最大金额"
                value={maxAmount}
                onChange={(_, data) => {
                  setMaxAmount(data.value);
                  setCurrentPage(1);
                }}
                style={{ width: 100 }}
              />
            </div>

            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <Text weight="semibold">发生年份:</Text>
              <Input
                type="number"
                placeholder="开始年份"
                value={minYear}
                onChange={(_, data) => {
                  setMinYear(data.value);
                  setCurrentPage(1);
                }}
                style={{ width: 100 }}
              />
              <Text>-</Text>
              <Input
                type="number"
                placeholder="结束年份"
                value={maxYear}
                onChange={(_, data) => {
                  setMaxYear(data.value);
                  setCurrentPage(1);
                }}
                style={{ width: 100 }}
              />
            </div>
            
            <Button
              onClick={() => {
                setSearchQuery("");
                setRiskFilters({ 低: true, 中: true, 高: true, 极高: true });
                setMinAmount("");
                setMaxAmount("");
                setMinYear("");
                setMaxYear("");
                setCurrentPage(1);
              }}
            >
              清除筛选
            </Button>
          </div>

          <div style={{ flex: 1 }} />
          <Button appearance="primary" icon={<AddRegular />} onClick={openAddModal}>
            新增案件
          </Button>
        </div>

        {loading ? (
          <div className={styles.spinnerContainer}>
            <Spinner label="加载中..." />
          </div>
        ) : error ? (
          <div style={{ padding: "40px", textAlign: "center", color: "#d13438" }}>
            加载失败: {error}
            <div style={{ marginTop: "10px" }}>
              <Button appearance="primary" onClick={fetchData}>重试</Button>
            </div>
          </div>
        ) : (
          <Table sortable className={styles.table}>
            <TableHeader>
              <TableRow>
                <TableSelectionCell
                  checked={allRowsSelected ? true : someRowsSelected ? "mixed" : false}
                  onClick={toggleAllRows}
                  checkboxIndicator={{ "aria-label": "Select all rows" }}
                />
                <TableHeaderCell {...headerSortProps("标题")}>标题</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("诈骗类型")}>诈骗类型</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("风险等级")}>风险等级</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("涉案金额")}>涉案金额</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("发生年份")}>发生年份</TableHeaderCell>
                <TableHeaderCell>操作</TableHeaderCell>
              </TableRow>
            </TableHeader>
            <TableBody>
              {tableRows.map(({ item, selected, onClick, onKeyDown, appearance, rowClassName, rowId }) => (
                <TableRow
                  key={item.id || rowId}
                  onClick={onClick}
                  onKeyDown={onKeyDown}
                  aria-selected={selected}
                  appearance={appearance}
                  className={rowClassName}>
                  <TableSelectionCell
                    subtle
                    checked={selected}
                    checkboxIndicator={{ "aria-label": "Select row" }}
                  />
                  <TableCell>{item.title}</TableCell>
                  <TableCell>{item.fraudType}</TableCell>
                  <TableCell>{item.riskLevel}</TableCell>
                  <TableCell>{item.amountInvolved}</TableCell>
                  <TableCell>{item.caseYear}</TableCell>
                  <TableCell>
                    <div className={styles.buttonGroup}>
                      <Button
                        appearance="transparent"
                        icon={<EditRegular />}
                        onClick={(e) => {
                          e.stopPropagation();
                          openEditModal(item);
                        }}
                      />
                      <Button
                        appearance="transparent"
                        icon={<DeleteRegular />}
                        onClick={(e) => {
                          e.stopPropagation();
                          if (item.id) handleDelete(item.id);
                        }}
                      />
                    </div>
                  </TableCell>
                </TableRow>
              ))}
              {tableRows.length === 0 && (
                <TableRow>
                  <TableCell colSpan={7} style={{ textAlign: "center", padding: "20px" }}>
                    没有找到数据
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        )}

        {totalPages > 0 && (
          <div className={styles.pagination}>
            <Button
              disabled={currentPage === 1}
              onClick={() => setCurrentPage(1)}
            >
              首页
            </Button>
            <Button
              disabled={currentPage === 1}
              onClick={() => setCurrentPage(currentPage - 1)}
            >
              上一页
            </Button>
            <Text>
              第 {currentPage} 页，共 {totalPages} 页，总共 {filteredItems.length} 条
            </Text>
            <Button
              disabled={currentPage >= totalPages}
              onClick={() => setCurrentPage(currentPage + 1)}
            >
              下一页
            </Button>
            <Button
              disabled={currentPage >= totalPages}
              onClick={() => setCurrentPage(totalPages)}
            >
              末页
            </Button>
          </div>
        )}
      </div>

      <Dialog open={openModal} onOpenChange={(e, data) => setOpenModal(data.open)}>
        <DialogSurface style={{ maxWidth: "600px", width: "100%" }}>
          <DialogBody>
            <DialogTitle>{isEdit ? "编辑案件" : "新增案件"}</DialogTitle>
            <DialogContent style={{ display: "flex", flexDirection: "column", gap: "10px", maxHeight: "60vh", overflowY: "auto", paddingRight: "8px" }}>
              <Field label="案例编号">
                <Input
                  value={currentCase.caseId || ""}
                  disabled
                />
              </Field>
              <Field label="案件标题">
                <Input
                  value={currentCase.title || ""}
                  onChange={(e, d) => setCurrentCase({ ...currentCase, title: d.value })}
                />
              </Field>
              <Field label="目标群体">
                <Input
                  value={currentCase.targetGroup || ""}
                  onChange={(e, d) => setCurrentCase({ ...currentCase, targetGroup: d.value })}
                />
              </Field>
              <Field label="诈骗类型">
                <Input
                  value={currentCase.fraudType || ""}
                  onChange={(e, d) => setCurrentCase({ ...currentCase, fraudType: d.value })}
                />
              </Field>
              <Field label="诈骗场景">
                <Input
                  value={currentCase.scenario || ""}
                  onChange={(e, d) => setCurrentCase({ ...currentCase, scenario: d.value })}
                />
              </Field>
              <Field label="风险等级">
                <div style={{ display: "flex", gap: "8px" }}>
                  {["低", "中", "高", "极高"].map((level) => (
                    <Checkbox
                      key={level}
                      label={level}
                      checked={currentCase.riskLevel === level}
                      onChange={(e, data) => {
                        if (data.checked) {
                          setCurrentCase({ ...currentCase, riskLevel: level as any });
                        }
                      }}
                    />
                  ))}
                </div>
              </Field>
              <Field label="涉案金额">
                <Input
                  type="number"
                  value={currentCase.amountInvolved?.toString() || ""}
                  onChange={(e, d) => setCurrentCase({ ...currentCase, amountInvolved: parseFloat(d.value) || 0 })}
                />
              </Field>
              <Field label="发生地区">
                <Input
                  value={currentCase.region || ""}
                  onChange={(e, d) => setCurrentCase({ ...currentCase, region: d.value })}
                />
              </Field>
              <Field label="发生年份">
                <Input
                  type="number"
                  value={currentCase.caseYear?.toString() || ""}
                  onChange={(e, d) => setCurrentCase({ ...currentCase, caseYear: parseInt(d.value) || 0 })}
                />
              </Field>
              <Field label="关键特征（关键词）">
                <Input
                  value={currentCase.keyFeatures || ""}
                  onChange={(e, d) => setCurrentCase({ ...currentCase, keyFeatures: d.value })}
                  placeholder='例如：["冒充客服", "诱导下载"]'
                />
              </Field>
              <Field label="案例来源">
                <Input
                  value={currentCase.caseSource || ""}
                  onChange={(e, d) => setCurrentCase({ ...currentCase, caseSource: d.value })}
                />
              </Field>
              <Field label="来源链接">
                <Input
                  value={currentCase.sourceUrl || ""}
                  onChange={(e, d) => setCurrentCase({ ...currentCase, sourceUrl: d.value })}
                />
              </Field>
              <Field label="诈骗经过描述">
                <Textarea
                  value={currentCase.process || ""}
                  onChange={(e, d) => setCurrentCase({ ...currentCase, process: d.value })}
                  resize="vertical"
                  rows={4}
                />
              </Field>
            </DialogContent>
            <DialogActions style={{ marginTop: "16px" }}>
              <Button appearance="secondary" onClick={() => setOpenModal(false)}>
                取消
              </Button>
              <Button appearance="primary" onClick={handleSave}>
                保存
              </Button>
            </DialogActions>
          </DialogBody>
        </DialogSurface>
      </Dialog>
    </div>
  );
}
