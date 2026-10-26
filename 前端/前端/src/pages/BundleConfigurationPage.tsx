import {
  Body1,
  Button,
  Card,
  createTableColumn,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  DialogTrigger,
  Dropdown,
  Field,
  Input,
  Label,
  MessageBar,
  MessageBarBody,
  Option,
  SearchBox,
  Select,
  Spinner,
  Text,
  Title1,
  Toast,
  ToastBody,
  ToastTitle,
  tokens,
  useId,
  useTableFeatures,
  useTableSelection,
  useToastController,
} from "@fluentui/react-components";
import { DatePicker } from "@fluentui/react-datepicker-compat";
import {
  Add24Regular,
  Backspace24Regular,
  BackspaceRegular,
  DeleteRegular,
  EditRegular,
  EyeRegular,
  SearchRegular,
} from "@fluentui/react-icons";
import { Col, Row } from "antd";
import { useCallback, useEffect, useState } from "react";
import SearchBoxStyle from "../components/SearchBox.tsx";
import { TableDisplay } from "../components/Table";
import { useStyles } from "../styles/BundleConfigurationStyle";
import type { IBundle } from "../types/IBundle";
import api from "../utils/Api";
import { localizedStrings, onFormatDate } from "../utils/CalenderHelper.ts";

export default function BundleConfigurationPage() {
  const toasterId = useId("toaster");
  const { dispatchToast } = useToastController(toasterId);
  const styles = useStyles();

  // 清除未使用的变量警告
  const _ = toasterId;

  // 数据状态
  const [bundleList, setBundleList] = useState<IBundle[]>([]);
  const [allBundles, setAllBundles] = useState<IBundle[]>([]); // 存储所有数据用于前端分页
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 搜索和筛选状态
  const [searchParams, setSearchParams] = useState({
    name: "",
    state: "",
    orderKey: "",
    isAsc: "",
  });

  // 分页状态
  const [pagination, setPagination] = useState({
    pageNum: 1,
    pageSize: 20,
    total: 0,
    totalPages: 0,
  });

  // 选中行状态
  const [selectedRows, setSelectedRows] = useState<IBundle[]>([]);

  // 对话框状态
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [isViewDialogOpen, setIsViewDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isBatchDeleteDialogOpen, setIsBatchDeleteDialogOpen] = useState(false);

  // 表单状态
  const [editingBundle, setEditingBundle] = useState<IBundle>({
    id: "",
    name: "",
    description: "",
    price: 0,
    dataLimit: 0,
    callMinutes: 0,
    smsCount: 0,
    effectiveDate: null,
    expiryDate: null,
    validityDays: 0,
    state: "ACTIVE",
  });

  // 错误状态
  const [addError, setAddError] = useState("");
  const [editError, setEditError] = useState("");
  const [deleteError, setDeleteError] = useState("");
  const [nameError, setNameError] = useState<string | undefined>(undefined);
  const [priceError, setPriceError] = useState<string | undefined>(undefined);

  // 操作状态
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [editingItem, setEditingItem] = useState<IBundle | null>(null);

  // 映射关系
  const orderKeyLabels: Record<string, string> = {
    name: "套餐名称",
    price: "月费",
    dataLimit: "流量限制",
    callMinutes: "通话时长",
    smsCount: "短信条数",
  };

  const isAscLabels: Record<string, string> = {
    ASC: "升序",
    DESC: "降序",
  };

  const stateLabels: Record<string, string> = {
    ACTIVE: "启用",
    SUSPENDED: "暂停",
    TERMINATED: "终止",
  };

  // 表格列定义
  const columns = [
    createTableColumn<IBundle>({
      columnId: "套餐名称",
      renderHeaderCell: () => "套餐名称",
      renderCell: (item) => item.name,
    }),
    createTableColumn<IBundle>({
      columnId: "月费",
      renderHeaderCell: () => "月费",
      renderCell: (item) => `¥${item.price}`,
    }),
    createTableColumn<IBundle>({
      columnId: "流量限制(GB)",
      renderHeaderCell: () => "流量限制(GB)",
      renderCell: (item) => item.dataLimit,
    }),
    createTableColumn<IBundle>({
      columnId: "通话时长(分钟)",
      renderHeaderCell: () => "通话时长(分钟)",
      renderCell: (item) => item.callMinutes,
    }),
    createTableColumn<IBundle>({
      columnId: "短信条数",
      renderHeaderCell: () => "短信条数",
      renderCell: (item) => item.smsCount,
    }),
    createTableColumn<IBundle>({
      columnId: "状态",
      renderHeaderCell: () => "状态",
      renderCell: (item) => {
        let color = tokens.colorNeutralForeground3;
        let text = "未知";
        switch (item.state) {
          case "ACTIVE":
            color = "green"; // 蓝色
            text = "启用";
            break;
          case "SUSPENDED":
            color = "#0078d4"; // 黄色
            text = "暂停";
            break;
          case "TERMINATED":
            color = "#d13438"; // 红色
            text = "终止";
            break;
        }
        return <Text style={{ color }}>{text}</Text>;
      },
    }),
    createTableColumn<IBundle>({
      columnId: "操作",
      renderCell: (item) => (
        <div className="flex justify-center">
          <Button icon={<EyeRegular />} aria-label="查看" onClick={() => handleView(item)} />
          <Button icon={<EditRegular />} aria-label="编辑" onClick={() => handleEdit(item)} />
          <Button
            icon={<DeleteRegular />}
            aria-label="删除"
            onClick={() => handleDelete(item)}
            style={{ color: "#d13438" }}
          />
        </div>
      ),
    }),
  ];

  // 显示错误提示
  const showErrorToast = (message: string) => {
    dispatchToast(
      <Toast>
        <ToastTitle>操作失败</ToastTitle>
        <ToastBody>{message}</ToastBody>
      </Toast>,
      { intent: "error", position: "bottom-end" }
    );
  };

  // 显示成功提示
  const showSuccessToast = (message: string) => {
    dispatchToast(
      <Toast>
        <ToastBody>{message}</ToastBody>
      </Toast>,
      { intent: "success", position: "bottom-end" }
    );
  };

  // 获取套餐列表
  const fetchBundleList = useCallback(async () => {
    try {
      console.log(searchParams);
      setLoading(true);
      // 构造 query 参数，以数字字符串形式发送
      const params = new URLSearchParams();

      // 只有当参数有值时才添加到查询字符串中
      if (searchParams.name && searchParams.name.trim() !== "") {
        params.append("name", searchParams.name.trim());
      }

      if (searchParams.state && searchParams.state !== "") {
        params.append("state", searchParams.state);
      }

      console.log(params);
      const response = await api.post(`/bundle/list?${params.toString()}`, {
        current: 1, // 获取第一页
        size: 1000, // 使用大尺寸获取所有数据
      });

      if (response.data.success) {
        const data = response.data.data || [];
        const formattedData = data.map((item: IBundle) => ({
          id: item.id || "",
          name: item.name || "",
          description: item.description || "",
          price: item.price || 0,
          dataLimit: item.dataLimit || 0,
          callMinutes: item.callMinutes || 0,
          smsCount: item.smsCount || 0,
          effectiveDate: item.effectiveDate || null,
          expiryDate: item.expiryDate || null,
          validityDays: item.validityDays || 0,
          state: item.state || "ACTIVE",
        }));
        setAllBundles(formattedData); // 存储所有数据
        setPagination((p) => ({
          ...p,
          total: formattedData.length,
          totalPages: Math.ceil(formattedData.length / p.pageSize),
        }));
      }
    } catch (err: any) {
      console.error("获取套餐列表失败:", err);
      const errorMessage = err.response?.data?.message || "获取套餐列表失败";
      setError(errorMessage);
      showErrorToast(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [searchParams]);

  // 重置表单
  function resetForm() {
    setEditingBundle({
      id: "",
      name: "",
      description: "",
      price: 0,
      dataLimit: 0,
      callMinutes: 0,
      smsCount: 0,
      effectiveDate: null,
      expiryDate: null,
      validityDays: 0,
      state: "ACTIVE",
    });
    setNameError(undefined);
    setPriceError(undefined);
    setAddError("");
    setEditError("");
  }

  // 打开新增对话框
  function handleAdd() {
    resetForm();
    setIsAddDialogOpen(true);
  }

  // 打开查看对话框
  function handleView(item: IBundle) {
    setEditingItem(item);
    setIsViewDialogOpen(true);
  }

  // 打开编辑对话框
  function handleEdit(item: IBundle) {
    setEditingBundle({ ...item });
    setEditingItem(item);
    setNameError(undefined);
    setPriceError(undefined);
    setEditError("");
    setIsEditDialogOpen(true);
  }

  // 打开删除确认对话框
  function handleDelete(item: IBundle) {
    setEditingItem(item);
    setDeleteError("");
    setIsDeleteDialogOpen(true);
  }

  // 打开批量删除确认对话框
  function handleBatchDelete() {
    if (selectedRows.length === 0) {
      showErrorToast("请先选择要删除的套餐");
      return;
    }
    setDeleteError("");
    setIsBatchDeleteDialogOpen(true);
  }

  // 验证表单
  function validateForm() {
    let isValid = true;

    if (!editingBundle.name || editingBundle.name.trim() === "") {
      setNameError("套餐名称不能为空");
      isValid = false;
    } else if (editingBundle.name.length > 50) {
      setNameError("套餐名称不能超过50个字符");
      isValid = false;
    } else {
      setNameError(undefined);
    }

    if (editingBundle.price < 0) {
      setPriceError("价格不能为负数");
      isValid = false;
    } else {
      setPriceError(undefined);
    }

    return isValid;
  }

  // 新增套餐
  async function handleAddSubmit() {
    setAddError("");

    if (!validateForm()) {
      return;
    }

    try {
      setConfirmLoading(true);
      // 排除id字段，因为创建时不需要id
      const { id, ...createData } = editingBundle;
      (createData as any).effectiveDate = new Date();
      (createData as any).expiryDate = new Date();
      const response = await api.post("/bundle/create", createData);

      if (response.data.success) {
        showSuccessToast("添加套餐成功");
        setIsAddDialogOpen(false);
        fetchBundleList();
      } else {
        setAddError(response.data.message || "添加套餐失败");
      }
    } catch (err: any) {
      console.error("添加套餐失败:", err);
      setAddError(err.response?.data?.message || "添加套餐失败，请重试");
    } finally {
      setConfirmLoading(false);
    }
  }

  // 编辑套餐
  async function handleEditSubmit() {
    setEditError("");

    if (!validateForm()) {
      return;
    }

    try {
      setConfirmLoading(true);
      const response = await api.put(`/bundle/${editingBundle.id}`, editingBundle);

      if (response.data.success) {
        showSuccessToast("更新套餐成功");
        setIsEditDialogOpen(false);
        fetchBundleList();
      } else {
        setEditError(response.data.message || "更新套餐失败");
      }
    } catch (err: any) {
      console.error("更新套餐失败:", err);
      setEditError(err.response?.data?.message || "更新套餐失败，请重试");
    } finally {
      setConfirmLoading(false);
    }
  }

  // 删除套餐
  async function handleDeleteSubmit() {
    if (!editingItem) return;

    try {
      setConfirmLoading(true);
      const response = await api.delete(`/bundle/${editingItem.id}`);

      if (response.data.success) {
        showSuccessToast("删除套餐成功");
        setIsDeleteDialogOpen(false);
        fetchBundleList();
      } else {
        setDeleteError(response.data.message || "删除套餐失败");
      }
    } catch (err: any) {
      console.error("删除套餐失败:", err);
      setDeleteError(err.response?.data?.message || "删除套餐失败，请重试");
    } finally {
      setConfirmLoading(false);
    }
  }

  // 批量删除套餐
  async function handleBatchDeleteSubmit() {
    try {
      setConfirmLoading(true);

      // 使用并行处理提高批量删除性能
      const deletePromises = selectedRows.map((item) =>
        api
          .delete(`/bundle/${item.id}`)
          .then((response) => ({ success: response.data.success, id: item.id, name: item.name }))
          .catch((err) => {
            // 记录单个删除失败的信息，但继续执行其他删除
            console.error(`删除套餐 ${item.id} 失败:`, err);
            return { success: false, id: item.id, name: item.name };
          })
      );

      const results = await Promise.all(deletePromises);

      // 统计成功和失败的数量
      const successCount = results.filter((r) => r.success).length;
      const failedCount = results.filter((r) => !r.success).length;

      // 显示结果
      if (failedCount === 0) {
        showSuccessToast(`成功删除 ${successCount} 个套餐`);
        setIsBatchDeleteDialogOpen(false);
        fetchBundleList();
        setSelectedRows([]);
      } else {
        setDeleteError(
          `部分删除失败: 成功 ${successCount} 个，失败 ${failedCount} 个。请重试或检查失败的套餐是否被其他资源引用。`
        );
      }
    } catch (err: any) {
      console.error("批量删除套餐失败:", err);
      setDeleteError(err.response?.data?.message || "批量删除失败，请重试");
    } finally {
      setConfirmLoading(false);
    }
  }

  // 清除搜索参数
  function clearSearchParam() {
    setSearchParams({
      name: "",
      state: "",
      orderKey: "",
      isAsc: "",
    });
  }

  // 处理选择变化
  function handleSelectionChange(rows: IBundle[]) {
    setSelectedRows(rows);
  }

  // 表格功能
  const {
    getRows,
    selection: { allRowsSelected, someRowsSelected, toggleAllRows, toggleRow, isRowSelected },
  } = useTableFeatures(
    {
      columns,
      items: bundleList,
    },
    [
      useTableSelection({
        selectionMode: "multiselect",
        defaultSelectedItems: new Set(),
        onSelectionChange: (event, data) => {
          const selectedIds = data.selectedItems;
          const selectedData = bundleList.filter((item) => selectedIds.has(item.id));
          setSelectedRows(selectedData);
        },
      }),
    ]
  );

  // 初始加载
  useEffect(() => {
    fetchBundleList();
  }, [fetchBundleList]);

  // 前端过滤和分页逻辑
  useEffect(() => {
    if (allBundles.length === 0) {
      setBundleList([]);
      return;
    }

    let filtered = [...allBundles];

    // 套餐名称模糊搜索
    if (searchParams.name && searchParams.name.trim() !== "") {
      const searchLower = searchParams.name.toLowerCase();
      filtered = filtered.filter((bundle) => (bundle.name || "").toLowerCase().includes(searchLower));
    }

    // 状态筛选
    if (searchParams.state && searchParams.state !== "") {
      filtered = filtered.filter((bundle) => bundle.state === searchParams.state);
    }

    // 前端排序
    if (searchParams.orderKey && searchParams.isAsc) {
      filtered.sort((a, b) => {
        let valueA = a[searchParams.orderKey];
        let valueB = b[searchParams.orderKey];

        // 处理空值
        if (valueA === null || valueA === undefined) return 1;
        if (valueB === null || valueB === undefined) return -1;

        // 根据数据类型进行比较
        if (typeof valueA === "number" && typeof valueB === "number") {
          return searchParams.isAsc === "ASC" ? valueA - valueB : valueB - valueA;
        }

        if (typeof valueA === "string" && typeof valueB === "string") {
          const comparison = valueA.localeCompare(valueB);
          return searchParams.isAsc === "ASC" ? comparison : -comparison;
        }

        // 处理日期字符串
        if (typeof valueA === "string" && typeof valueB === "string") {
          const dateA = new Date(valueA);
          const dateB = new Date(valueB);
          if (!isNaN(dateA.getTime()) && !isNaN(dateB.getTime())) {
            const comparison = dateA.getTime() - dateB.getTime();
            return searchParams.isAsc === "ASC" ? comparison : -comparison;
          }
        }

        return 0;
      });
    }

    // 处理分页
    const startIndex = (pagination.pageNum - 1) * pagination.pageSize;
    const endIndex = startIndex + pagination.pageSize;
    const paginatedBundles = filtered.slice(startIndex, endIndex);

    setBundleList(paginatedBundles);
    setPagination((p) => ({
      ...p,
      total: filtered.length,
      totalPages: Math.ceil(filtered.length / p.pageSize),
    }));
  }, [allBundles, searchParams, pagination.pageNum, pagination.pageSize]);

  // 搜索参数变化时重置页码到第一页
  useEffect(() => {
    setPagination((p) => ({ ...p, pageNum: 1 }));
  }, [searchParams.name, searchParams.state, searchParams.orderKey, searchParams.isAsc]);

  return (
    <div
      className={styles.container}
      style={{
        height: "100%",
        padding: "2px",
        boxSizing: "border-box",
        overflow: "hidden",
        display: "flex",
        flexDirection: "column",
        gap: tokens.spacingVerticalM,
      }}>
      <Title1>套餐管理</Title1>
      {/* 搜索和筛选区域 */}
      <Card style={{ flexShrink: 0 }}>
        <Row gutter={[16, 24]} style={{ alignItems: "flex-end" }}>
          <Col xs={24} sm={12} md={8} lg={6}>
            <SearchBoxStyle
              label="套餐搜索"
              placeholder="请输入套餐名称"
              value={searchParams.name}
              onChange={(_, data) => {
                setSearchParams((prev) => ({ ...prev, name: data.value }));
              }}
            />
          </Col>
          <Col xs={24} sm={12} md={8} lg={6}>
            <div className={styles.filterItem}>
              <Text size={200}>套餐状态</Text>
              <Dropdown
                aria-label="套餐状态筛选"
                value={stateLabels[searchParams.state] || "全部"}
                selectedOptions={[searchParams.state]}
                onOptionSelect={(_, data) => {
                  setSearchParams((prev) => ({ ...prev, state: data.optionValue as string }));
                }}>
                <Option value="">全部</Option>
                <Option value="ACTIVE">启用</Option>
                <Option value="SUSPENDED">暂停</Option>
                <Option value="TERMINATED">终止</Option>
              </Dropdown>
            </div>
          </Col>
        </Row>
        <Row gutter={[16, 24]} style={{ alignItems: "flex-end" }}>
          <Col xs={24} sm={12} md={8} lg={6}>
            <div className={styles.filterItem}>
              <Text size={200}>套餐排序依据</Text>
              <Dropdown
                aria-label="排序依据"
                value={orderKeyLabels[searchParams.orderKey] || "无依据"}
                selectedOptions={[searchParams.orderKey]}
                onOptionSelect={(_, data) => {
                  setSearchParams((prev) => ({ ...prev, orderKey: data.optionValue as string }));
                }}>
                <Option value="">无依据</Option>
                <Option value="name">套餐名称</Option>
                <Option value="price">月费</Option>
                <Option value="dataLimit">流量限制</Option>
                <Option value="callMinutes">通话时长</Option>
                <Option value="smsCount">短信条数</Option>
              </Dropdown>
            </div>
          </Col>
          <Col xs={24} sm={12} md={8} lg={6}>
            <div className={styles.filterItem}>
              <Text size={200}>升降序</Text>
              <Dropdown
                aria-label="选择升降序"
                value={isAscLabels[searchParams.isAsc] || "乱序"}
                selectedOptions={[searchParams.isAsc]}
                onOptionSelect={(_, data) => {
                  setSearchParams((prev) => ({ ...prev, isAsc: data.optionValue as string }));
                }}>
                <Option value="">乱序</Option>
                <Option value="ASC">升序</Option>
                <Option value="DESC">降序</Option>
              </Dropdown>
            </div>
          </Col>
          <Col xs={24} sm={12} md={8} lg={6}></Col>
          <Col xs={24} sm={12} md={8} lg={6}>
            <div className={styles.filterRow} style={{ flexDirection: "row-reverse", alignItems: "flex-end" }}>
              <Button appearance="primary" icon={<Add24Regular />} onClick={handleAdd}>
                新增套餐
              </Button>
              <Button onClick={clearSearchParam} disabled={loading} icon={<Backspace24Regular />}>
                清除搜索
              </Button>
            </div>
          </Col>
        </Row>
      </Card>
      {/* 数据表格区域 */}
      <Card style={{ flex: 1, display: "flex", flexDirection: "column", minHeight: 0, overflow: "hidden" }}>
        <TableDisplay
          originPage="bundle"
          rows={bundleList}
          columns={columns}
          loading={loading}
          error={error}
          fetchData={fetchBundleList}
          allRowsSelected={allRowsSelected}
          someRowsSelected={someRowsSelected}
          toggleAllRows={toggleAllRows}
          toggleAllKeydown={(e) => {
            if (e.key === " ") {
              toggleAllRows(e);
              e.preventDefault();
            }
          }}
          getRows={getRows}
          isRowSelected={isRowSelected}
          toggleRow={toggleRow}
          onSelectionChange={handleSelectionChange}
          withAddButton={true}
          onAddClick={handleAdd}
          onBatchDeleteClick={handleBatchDelete}
        />
        {/* 分页控制区域 */}
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            gap: tokens.spacingHorizontalM,
            marginTop: tokens.spacingVerticalM,
            flexShrink: 0,
            position: "relative",
          }}>
          <Button
            appearance="secondary"
            onClick={handleBatchDelete}
            disabled={loading || selectedRows.length <= 0}
            icon={loading ? <Spinner size="tiny" /> : <DeleteRegular />}
            style={{ position: "absolute", left: 15 }}>
            {loading ? "加载中..." : "批量删除"}
          </Button>
          <Button
            onClick={() => {
              setPagination((p) => ({
                ...p,
                pageNum: 1,
              }));
            }}
            disabled={pagination.pageNum === 1 || pagination.totalPages === 0}>
            首页
          </Button>
          <Button
            onClick={() => {
              const newPageNum = pagination.pageNum > 1 ? pagination.pageNum - 1 : 1;
              setPagination((p) => ({
                ...p,
                pageNum: newPageNum,
              }));
            }}
            disabled={pagination.pageNum === 1 || pagination.totalPages === 0}>
            上一页
          </Button>
          <div style={{ display: "flex", alignItems: "center", gap: tokens.spacingHorizontalS }}>
            <Text>第</Text>
            <Dropdown
              value={`${pagination.pageNum}`}
              selectedOptions={[`${pagination.pageNum}`]}
              onOptionSelect={(_, data) => {
                const newPageNum = parseInt(data.optionValue as string);
                setPagination((p) => ({
                  ...p,
                  pageNum: newPageNum,
                }));
              }}
              style={{ minWidth: "80px" }}>
              {Array.from({ length: pagination.totalPages }, (_, i) => (
                <Option key={i + 1} value={`${i + 1}`}>
                  {`${i + 1}`}
                </Option>
              ))}
            </Dropdown>
            <Text>
              页 / 共 {pagination.totalPages} 页，总共 {pagination.total} 条
            </Text>
          </div>
          <Button
            onClick={() => {
              const newPageNum =
                pagination.pageNum < pagination.totalPages ? pagination.pageNum + 1 : pagination.totalPages;
              setPagination((p) => ({
                ...p,
                pageNum: newPageNum,
              }));
            }}
            disabled={pagination.pageNum >= pagination.totalPages || pagination.totalPages === 0}>
            下一页
          </Button>
          <Button
            onClick={() => {
              setPagination((p) => ({
                ...p,
                pageNum: p.totalPages,
              }));
            }}
            disabled={pagination.pageNum === pagination.totalPages || pagination.totalPages === 0}>
            末页
          </Button>
          <div style={{ display: "flex", alignItems: "center", gap: tokens.spacingHorizontalS }}>
            <Text>每页</Text>
            <Dropdown
              value={`${pagination.pageSize}`}
              selectedOptions={[`${pagination.pageSize}`]}
              onOptionSelect={(_, data) => {
                const newPageSize = parseInt(data.optionValue as string);
                setPagination((p) => ({
                  ...p,
                  pageSize: newPageSize,
                  pageNum: 1, // 切换每页条数时回到第一页
                }));
              }}
              style={{ minWidth: "80px" }}>
              {[10, 20, 50, 100].map((size) => (
                <Option key={size} value={`${size}`}>
                  {`${size}`}
                </Option>
              ))}
            </Dropdown>
            <Text>条</Text>
          </div>
        </div>
      </Card>
      {/* 新增套餐对话框 */}
      <Dialog open={isAddDialogOpen} onOpenChange={(_, data) => setIsAddDialogOpen(data.open)}>
        <DialogSurface>
          <DialogBody>
            <DialogTitle>新增套餐</DialogTitle>
            <DialogContent>
              {addError && (
                <MessageBar intent="error" style={{ marginBottom: tokens.spacingVerticalM }}>
                  <MessageBarBody>{addError}</MessageBarBody>
                </MessageBar>
              )}

              <div style={{ display: "flex", flexDirection: "column", gap: tokens.spacingVerticalM }}>
                <Field label="套餐名称" required validationMessage={nameError}>
                  <Input
                    value={editingBundle.name}
                    maxLength={50}
                    placeholder="请输入套餐名称"
                    onChange={(_, data) => {
                      setEditingBundle((prev) => ({ ...prev, name: data.value }));
                      if (!data.value) {
                        setNameError("套餐名称不能为空");
                      } else if (data.value.length > 50) {
                        setNameError("套餐名称不能超过50个字符");
                      } else {
                        setNameError(undefined);
                      }
                    }}
                  />
                </Field>

                <Field label="描述">
                  <Input
                    value={editingBundle.description}
                    maxLength={200}
                    placeholder="请输入套餐描述"
                    onChange={(_, data) => setEditingBundle((prev) => ({ ...prev, description: data.value }))}
                  />
                </Field>

                <Field label="月费 (元)" required validationMessage={priceError}>
                  <Input
                    type="number"
                    value={editingBundle.price.toString()}
                    min={0}
                    step={0.01}
                    onChange={(_, data) => {
                      const value = parseFloat(data.value);
                      setEditingBundle((prev) => ({ ...prev, price: isNaN(value) ? 0 : value }));
                      if (value < 0) {
                        setPriceError("价格不能为负数");
                      } else {
                        setPriceError(undefined);
                      }
                    }}
                  />
                </Field>

                <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                  <Field label="流量限制 (GB)" style={{ flex: 1 }}>
                    <Input
                      type="number"
                      value={editingBundle.dataLimit.toString()}
                      min={0}
                      onChange={(_, data) =>
                        setEditingBundle((prev) => ({
                          ...prev,
                          dataLimit: parseInt(data.value) || 0,
                        }))
                      }
                    />
                  </Field>

                  <Field label="通话时长 (分钟)" style={{ flex: 1 }}>
                    <Input
                      type="number"
                      value={editingBundle.callMinutes.toString()}
                      min={0}
                      onChange={(_, data) =>
                        setEditingBundle((prev) => ({
                          ...prev,
                          callMinutes: parseInt(data.value) || 0,
                        }))
                      }
                    />
                  </Field>
                </div>

                <Field label="短信条数">
                  <Input
                    type="number"
                    value={editingBundle.smsCount.toString()}
                    min={0}
                    onChange={(_, data) =>
                      setEditingBundle((prev) => ({
                        ...prev,
                        smsCount: parseInt(data.value) || 0,
                      }))
                    }
                  />
                </Field>

                <Field label="状态">
                  <Dropdown
                    value={stateLabels[editingBundle.state] || "启用"}
                    selectedOptions={[editingBundle.state]}
                    onOptionSelect={(_, data) =>
                      setEditingBundle((prev) => ({ ...prev, state: data.optionValue || prev.state }))
                    }>
                    <Option value="ACTIVE">{stateLabels["ACTIVE"]}</Option>
                    <Option value="SUSPENDED">{stateLabels["SUSPENDED"]}</Option>
                    <Option value="TERMINATED">{stateLabels["TERMINATED"]}</Option>
                  </Dropdown>
                </Field>
              </div>
            </DialogContent>
            <DialogActions>
              <DialogTrigger disableButtonEnhancement>
                <Button appearance="secondary">取消</Button>
              </DialogTrigger>
              <Button appearance="primary" onClick={handleAddSubmit} disabled={confirmLoading}>
                {confirmLoading ? "提交中..." : "确定"}
              </Button>
            </DialogActions>
          </DialogBody>
        </DialogSurface>
      </Dialog>
      {/* 编辑套餐对话框 */}
      <Dialog open={isEditDialogOpen} onOpenChange={(_, data) => setIsEditDialogOpen(data.open)}>
        <DialogSurface>
          <DialogBody>
            <DialogTitle>编辑套餐</DialogTitle>
            <DialogContent>
              {editError && (
                <MessageBar intent="error" style={{ marginBottom: tokens.spacingVerticalM }}>
                  <MessageBarBody>{editError}</MessageBarBody>
                </MessageBar>
              )}

              <div style={{ display: "flex", flexDirection: "column", gap: tokens.spacingVerticalM }}>
                <Field label="套餐名称" required validationMessage={nameError}>
                  <Input
                    value={editingBundle.name}
                    maxLength={50}
                    placeholder="请输入套餐名称"
                    onChange={(_, data) => {
                      setEditingBundle((prev) => ({ ...prev, name: data.value }));
                      if (!data.value) {
                        setNameError("套餐名称不能为空");
                      } else if (data.value.length > 50) {
                        setNameError("套餐名称不能超过50个字符");
                      } else {
                        setNameError(undefined);
                      }
                    }}
                  />
                </Field>

                <Field label="描述">
                  <Input
                    value={editingBundle.description}
                    maxLength={200}
                    placeholder="请输入套餐描述"
                    onChange={(_, data) => setEditingBundle((prev) => ({ ...prev, description: data.value }))}
                  />
                </Field>

                <Field label="月费 (元)" required validationMessage={priceError}>
                  <Input
                    type="number"
                    value={editingBundle.price.toString()}
                    min={0}
                    step={0.01}
                    onChange={(_, data) => {
                      const value = parseFloat(data.value);
                      setEditingBundle((prev) => ({ ...prev, price: isNaN(value) ? 0 : value }));
                      if (value < 0) {
                        setPriceError("价格不能为负数");
                      } else {
                        setPriceError(undefined);
                      }
                    }}
                  />
                </Field>

                <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                  <Field label="流量限制 (GB)" style={{ flex: 1 }}>
                    <Input
                      type="number"
                      value={editingBundle.dataLimit.toString()}
                      min={0}
                      onChange={(_, data) =>
                        setEditingBundle((prev) => ({
                          ...prev,
                          dataLimit: parseInt(data.value) || 0,
                        }))
                      }
                    />
                  </Field>

                  <Field label="通话时长 (分钟)" style={{ flex: 1 }}>
                    <Input
                      type="number"
                      value={editingBundle.callMinutes.toString()}
                      min={0}
                      onChange={(_, data) =>
                        setEditingBundle((prev) => ({
                          ...prev,
                          callMinutes: parseInt(data.value) || 0,
                        }))
                      }
                    />
                  </Field>
                </div>

                <Field label="短信条数">
                  <Input
                    type="number"
                    value={editingBundle.smsCount.toString()}
                    min={0}
                    onChange={(_, data) =>
                      setEditingBundle((prev) => ({
                        ...prev,
                        smsCount: parseInt(data.value) || 0,
                      }))
                    }
                  />
                </Field>

                <Field label="状态">
                  <Dropdown
                    value={stateLabels[editingBundle.state] || "启用"}
                    selectedOptions={[editingBundle.state]}
                    onOptionSelect={(_, data) =>
                      setEditingBundle((prev) => ({ ...prev, state: data.optionValue || prev.state }))
                    }>
                    <Option value="ACTIVE">启用</Option>
                    <Option value="SUSPENDED">暂停</Option>
                    <Option value="TERMINATED">终止</Option>
                  </Dropdown>
                </Field>
              </div>
            </DialogContent>
            <DialogActions>
              <DialogTrigger disableButtonEnhancement>
                <Button appearance="secondary">取消</Button>
              </DialogTrigger>
              <Button appearance="primary" onClick={handleEditSubmit} disabled={confirmLoading}>
                {confirmLoading ? "保存中..." : "保存"}
              </Button>
            </DialogActions>
          </DialogBody>
        </DialogSurface>
      </Dialog>
      {/* 查看套餐对话框 */}
      <Dialog open={isViewDialogOpen} onOpenChange={(_, data) => setIsViewDialogOpen(data.open)}>
        <DialogSurface>
          <DialogBody>
            <DialogTitle>套餐详情</DialogTitle>
            <DialogContent>
              {editingItem && (
                <div style={{ display: "flex", flexDirection: "column", gap: tokens.spacingVerticalM }}>
                  <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                    <Label style={{ fontWeight: "bold", width: 100 }}>套餐名称:</Label>
                    <Body1>{editingItem.name}</Body1>
                  </div>

                  <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                    <Label style={{ fontWeight: "bold", width: 100 }}>描述:</Label>
                    <Body1>{editingItem.description || "-"}</Body1>
                  </div>

                  <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                    <Label style={{ fontWeight: "bold", width: 100 }}>月费:</Label>
                    <Body1>¥{editingItem.price}</Body1>
                  </div>

                  <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                    <Label style={{ fontWeight: "bold", width: 100 }}>流量限制:</Label>
                    <Body1>{editingItem.dataLimit} GB</Body1>
                  </div>

                  <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                    <Label style={{ fontWeight: "bold", width: 100 }}>通话时长:</Label>
                    <Body1>{editingItem.callMinutes} 分钟</Body1>
                  </div>

                  <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                    <Label style={{ fontWeight: "bold", width: 100 }}>短信条数:</Label>
                    <Body1>{editingItem.smsCount} 条</Body1>
                  </div>

                  <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                    <Label style={{ fontWeight: "bold", width: 100 }}>状态:</Label>
                    <Body1>
                      {(() => {
                        let color = "gray";
                        let text = "未知";
                        switch (editingItem.state) {
                          case "ACTIVE":
                            color = "green";
                            text = "启用";
                            break;
                          case "SUSPENDED":
                            color = "#0078d4";
                            text = "暂停";
                            break;
                          case "TERMINATED":
                            color = "#d13438";
                            text = "终止";
                            break;
                        }
                        return <span style={{ color }}>{text}</span>;
                      })()}
                    </Body1>
                  </div>
                </div>
              )}
            </DialogContent>
            <DialogActions>
              <DialogTrigger disableButtonEnhancement>
                <Button appearance="primary">关闭</Button>
              </DialogTrigger>
            </DialogActions>
          </DialogBody>
        </DialogSurface>
      </Dialog>
      {/* 删除确认对话框 */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={(_, data) => setIsDeleteDialogOpen(data.open)}>
        <DialogSurface>
          <DialogBody>
            <DialogTitle>删除套餐</DialogTitle>
            <DialogContent>
              {deleteError && (
                <MessageBar intent="error" style={{ marginBottom: tokens.spacingVerticalM }}>
                  <MessageBarBody>{deleteError}</MessageBarBody>
                </MessageBar>
              )}
              <p>确定要删除套餐 "{editingItem?.name}" 吗？此操作无法撤销。</p>
            </DialogContent>
            <DialogActions>
              <DialogTrigger disableButtonEnhancement>
                <Button appearance="secondary">取消</Button>
              </DialogTrigger>
              <Button
                appearance="secondary"
                onClick={handleDeleteSubmit}
                disabled={confirmLoading}
                style={{ backgroundColor: "#d13438", color: "white" }}>
                {confirmLoading ? "删除中..." : "确定删除"}
              </Button>
            </DialogActions>
          </DialogBody>
        </DialogSurface>
      </Dialog>
      {/* 批量删除确认对话框 */}
      <Dialog open={isBatchDeleteDialogOpen} onOpenChange={(_, data) => setIsBatchDeleteDialogOpen(data.open)}>
        <DialogSurface>
          <DialogBody>
            <DialogTitle>批量删除套餐</DialogTitle>
            <DialogContent>
              {deleteError && (
                <MessageBar intent="error" style={{ marginBottom: tokens.spacingVerticalM }}>
                  <MessageBarBody>{deleteError}</MessageBarBody>
                </MessageBar>
              )}
              <p>确定要删除选中的 {selectedRows.length} 个套餐吗？此操作无法撤销。</p>
            </DialogContent>
            <DialogActions>
              <DialogTrigger disableButtonEnhancement>
                <Button appearance="secondary">取消</Button>
              </DialogTrigger>
              <Button
                appearance="secondary"
                onClick={handleBatchDeleteSubmit}
                disabled={confirmLoading}
                style={{ backgroundColor: "#d13438", color: "white" }}>
                {confirmLoading ? "删除中..." : "确定删除"}
              </Button>
            </DialogActions>
          </DialogBody>
        </DialogSurface>
      </Dialog>
    </div>
  );
}
