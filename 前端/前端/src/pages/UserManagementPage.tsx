import {
  Body1,
  Button,
  Checkbox,
  createTableColumn,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  InfoLabel,
  Input,
  Label,
  MessageBar,
  MessageBarBody,
  SearchBox,
  Spinner,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableHeaderCell,
  TableRow,
  TableSelectionCell,
  TabList,
  Text,
  Title1,
  tokens,
  useTableFeatures,
  useTableSelection,
  useTableSort,
  type TableColumnDefinition,
  type TableColumnId,
  type TableRowId,
} from "@fluentui/react-components";
import { CheckmarkRegular, DeleteRegular, DismissRegular, EyeRegular } from "@fluentui/react-icons";
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { PERMISSIONS } from "../models/Permission";
import { useAdminStore } from "../stores/admin-store";
import { useStyles } from "../styles/AdminStyle";
import type { IAdminState } from "../types/IAdminState";
import type { IUserManagementItem } from "../types/IUserManagementItem";
import type { IUserReviewItem } from "../types/IUserReviewItem";
import api from "../utils/Api";
import {
  BackendUserToUserManagementItemConverter,
  BackendUserToUserReviewItemConverter,
} from "../utils/BackendUserToFrontendConverter";
import { isSuperAdmin, PermissionCodeToTextConverter } from "../utils/PermissionHelper";

export default function UserManagementPage() {
  const styles = useStyles();
  const navigate = useNavigate();

  // 使用全局状态管理的状态
  const { selectedMainTab, setMainTab, selectedSubTab, setSubTab, updateState } = useAdminStore();
  const [searchValue, setSearchValue] = useState("");

  // 每个列表独立的分页状态
  const [pendingCurrentPage, setPendingCurrentPage] = useState(1);
  const [pendingPageSize, setPendingPageSize] = useState(10);

  const [checkedCurrentPage, setCheckedCurrentPage] = useState(1);
  const [checkedPageSize, setCheckedPageSize] = useState(10);

  const [managementCurrentPage, setManagementCurrentPage] = useState(1);
  const [managementPageSize, setManagementPageSize] = useState(10);

  useEffect(() => {
    const updatePageSize = () => {
      const rowHeight = 44; // Table row default height
      // User management offset: Layout header (48), MainTabs (44+16), Toolbar (32+16+16), Table Header (44), Pagination (50), paddings/margins
      // Rough estimate ~300.
      const offset = 320; 
      let newSize = Math.floor((window.innerHeight - offset-100) / rowHeight);
      if (newSize < 5) newSize = 5;
      
      setPendingPageSize(newSize);
      setCheckedPageSize(newSize);
      setManagementPageSize(newSize);
    };

    updatePageSize();
    window.addEventListener("resize", updatePageSize);
    return () => window.removeEventListener("resize", updatePageSize);
  }, []);

  const [selectedUsers, setSelectedUsers] = useState<Set<string>>(new Set());

  // 表格重置状态 - 用于强制重新渲染表格以清除选中状态
  const [tableResetKey, setTableResetKey] = useState(0);

  // 数据状态
  const [pendingUsers, setPendingUsers] = useState<IUserReviewItem[]>([]);
  const [checkedUsers, setCheckedUsers] = useState<IUserReviewItem[]>([]);
  const [managementUsers, setManagementUsers] = useState<IUserManagementItem[]>([]);
  const [filteredPendingUsers, setFilteredPendingUsers] = useState<IUserReviewItem[]>([]);
  const [filteredCheckedUsers, setFilteredCheckedUsers] = useState<IUserReviewItem[]>([]);
  const [filteredManagementUsers, setFilteredManagementUsers] = useState<IUserManagementItem[]>([]);
  const [pendingLoading, setPendingLoading] = useState(false);
  const [checkedLoading, setCheckedLoading] = useState(false);
  const [managementLoading, setManagementLoading] = useState(false);
  const [pendingTotalCount, setPendingTotalCount] = useState(0);
  const [checkedTotalCount, setCheckedTotalCount] = useState(0);
  const [managementTotalCount, setManagementTotalCount] = useState(0);

  // 过滤器状态
  const [checkedSearchValue, setCheckedSearchValue] = useState("");

  // 角色分配状态
  const [roleDialog, setRoleDialog] = useState({
    open: false,
    userId: "",
    username: "",
  });
  const [selectedRoles, setSelectedRoles] = useState({
    service: false,
    admin: false,
  });
  const [roleAssignLoading, setRoleAssignLoading] = useState(false);
  const [roleAssignError, setRoleAssignError] = useState("");

  // 确认对话框状态
  const [confirmDialog, setConfirmDialog] = useState({
    open: false,
    type: "",
    userId: "",
    title: "",
    message: "",
  });
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [confirmError, setConfirmError] = useState("");

  // 查看用户对话框状态
  const [viewDialog, setViewDialog] = useState({
    open: false,
    user: {} as IUserReviewItem | null,
    loading: false,
    error: "",
  });

  // 用户管理状态
  const [managementSearchValue, setManagementSearchValue] = useState("");
  const [managementFilters, setManagementFilters] = useState({
    showVisitors: true,
    showService: true,
    showAdmins: true,
  });

  // 排序状态
  const [pendingSortState, setPendingSortState] = useState<{
    sortDirection: "ascending" | "descending";
    sortColumn: TableColumnId | undefined;
  }>({
    sortDirection: "descending" as const,
    sortColumn: "邮箱",
  });
  const [checkedSortState, setCheckedSortState] = useState<{
    sortDirection: "ascending" | "descending";
    sortColumn: TableColumnId | undefined;
  }>({
    sortDirection: "descending" as const,
    sortColumn: "邮箱",
  });
  const [managementSortState, setManagementSortState] = useState<{
    sortDirection: "ascending" | "descending";
    sortColumn: string;
  }>({
    sortDirection: "descending",
    sortColumn: "email",
  });

  // 页码跳转输入框状态
  const [pendingPageInput, setPendingPageInput] = useState("");
  const [checkedPageInput, setCheckedPageInput] = useState("");
  const [managementPageInput, setManagementPageInput] = useState("");

  // 状态更新函数 - 同步到全局状态管理
  function updateAdminState(updates: Partial<IAdminState>) {
    updateState(updates);
  }

  // 切换页面时清空选中状态
  useEffect(() => {
    setSelectedUsers(new Set());
    setTableResetKey((prev) => prev + 1); // 重置表格
  }, [pendingCurrentPage, checkedCurrentPage, managementCurrentPage, selectedMainTab, selectedSubTab]);

  // 清除选中状态的通用函数
  function clearSelection() {
    setSelectedUsers(new Set());
    setTableResetKey((prev) => prev + 1);
  }

  // 统一获取用户数据接口
  async function fetchAllUsers() {
    try {
      const payload = {
        pageNum: 1,
        pageSize: 1000,
      };

      const response = await api.post("/user/page", payload);

      if (response.data.success) {
        return response.data.data;
      }
      return [];
    } catch (error) {
      console.error("获取用户数据失败:", error);
      return [];
    }
  }

  // 获取未激活用户列表
  async function fetchUnactivatedUsers() {
    try {
      setPendingLoading(true);
      const allUsers = await fetchAllUsers();
      const filteredUsers = allUsers
        .map(BackendUserToUserReviewItemConverter)
        .filter((user: IUserReviewItem) => user.privilege === 0);
      setPendingUsers(filteredUsers);
      setPendingTotalCount(filteredUsers.length);
      // 数据刷新后清除选中状态
      clearSelection();
    } catch (error) {
      console.error("获取未激活用户失败:", error);
    } finally {
      setPendingLoading(false);
    }
  }

  // 获取已审核用户列表
  async function fetchCheckedUsers() {
    try {
      setCheckedLoading(true);
      const allUsers = await fetchAllUsers();
      const filteredUsers = allUsers
        .map(BackendUserToUserReviewItemConverter)
        .filter((user: IUserReviewItem) => user.privilege > 0);
      setCheckedUsers(filteredUsers);
      setCheckedTotalCount(filteredUsers.length);
      clearSelection();
    } catch (error) {
      console.error("获取已通过用户失败:", error);
    } finally {
      setCheckedLoading(false);
    }
  }

  // 获取用户管理列表
  async function fetchManagementUsers(
    pageNum: number = 1,
    pageSize: number = 10,
    privileges: number[] = [],
    sortField: string = "email",
    sortDirection: "ASC" | "DESC" = "DESC"
  ) {
    try {
      setManagementLoading(true);

      // 如果没有选中任何权限，清空表格
      if (privileges.length === 0) {
        setManagementUsers([]);
        setManagementTotalCount(0);
        clearSelection();
        return;
      }

      const allUsers = await fetchAllUsers();

      // 转换所有用户数据
      const convertedUsers = allUsers.map(BackendUserToUserManagementItemConverter);

      // 在前端使用位运算进行权限过滤
      const filteredUsers = convertedUsers.filter((user: IUserManagementItem) => {
        // 检查用户权限是否包含至少一个选中的权限
        return privileges.some((privilege) => (user.permission || 0) & privilege);
      });

      // 更新状态 - 保存所有过滤后的数据
      setManagementUsers(filteredUsers);
      setManagementTotalCount(filteredUsers.length);
      clearSelection();
    } catch (error) {
      console.error("获取用户管理列表失败:", error);
    } finally {
      setManagementLoading(false);
    }
  }

  function handleReject(userId: string) {
    setConfirmDialog({
      open: true,
      type: "reject",
      userId,
      title: "拒绝用户注册",
      message: "确定要拒绝该用户的注册申请吗？",
    });
  }

  function handleBatchReject() {
    if (selectedUsers.size === 0) return;
    setConfirmDialog({
      open: true,
      type: "batchReject",
      userId: "",
      title: "批量拒绝",
      message: `确定要拒绝选中的 ${selectedUsers.size} 个用户吗？`,
    });
  }

  // 删除单个用户
  async function deleteUser(alias: string) {
    try {
      setConfirmLoading(true);
      setConfirmError("");

      const response = await api.delete(`/users/${alias}`);

      if (response.data.success) {
        // 删除成功，刷新用户管理列表
        await fetchManagementUsers(
          managementCurrentPage,
          managementPageSize,
          getSelectedPermissions(),
          managementSortState.sortColumn,
          managementSortState.sortDirection === "ascending" ? "ASC" : "DESC"
        );
        setConfirmDialog({
          open: false,
          type: "",
          userId: "",
          title: "",
          message: "",
        });
        // 清除选中状态
        clearSelection();
      } else {
        setConfirmError(response.data.message || "删除失败");
      }
    } catch (error: any) {
      setConfirmError(error.response?.data?.message || "删除失败，请重试");
    } finally {
      setConfirmLoading(false);
    }
  }

  // 批量删除用户
  async function batchDeleteUsers() {
    try {
      setConfirmLoading(true);
      setConfirmError("");

      const response = await api.delete("/users/batch/subtree");

      if (response.data.success) {
        // 删除成功，刷新用户管理列表
        await fetchManagementUsers(
          managementCurrentPage,
          managementPageSize,
          getSelectedPermissions(),
          managementSortState.sortColumn,
          managementSortState.sortDirection === "ascending" ? "ASC" : "DESC"
        );
        setConfirmDialog({
          open: false,
          type: "",
          userId: "",
          title: "",
          message: "",
        });
        // 清除选中状态
        clearSelection();
      }
    } catch (error: any) {
      setConfirmError(error.response?.data?.message || "批量删除失败，请重试");
    } finally {
      setConfirmLoading(false);
    }
  }

  // 获取选中的权限过滤器
  function getSelectedPermissions() {
    const selected = [];

    // 按复选框收集基础权限位
    if (managementFilters.showVisitors) selected.push(1, PERMISSIONS.USER_TOURIST);
    if (managementFilters.showService) selected.push(2, PERMISSIONS.CUSTOMER_SERVICE);
    if (managementFilters.showAdmins) selected.push(4, 8, 16, PERMISSIONS.ADMIN_MAINTENANCE);

    // 返回选中的基础权限，用于后续的位运算过滤
    // 当选择一个权限时，显示所有包含该权限的用户
    // 当选择多个权限时，显示包含至少一个选中权限的用户
    return selected;
  }

  // 获取用户管理总页数
  function getManagementTotalPages() {
    return Math.ceil(managementTotalCount / managementPageSize);
  }

  // 初始化加载数据（审核页）
  // 管理页的加载统一由下方依赖管理筛选条件的 effect 处理，避免刷新时重复调用
  useEffect(() => {
    if (selectedMainTab === "review" && selectedSubTab === "pending") {
      fetchUnactivatedUsers();
    } else if (selectedMainTab === "review" && selectedSubTab === "reviewed") {
      fetchCheckedUsers();
    }
  }, [selectedMainTab, selectedSubTab]);

  // 用户管理数据变化时重新加载
  useEffect(() => {
    if (selectedMainTab === "management") {
      fetchManagementUsers(
        managementCurrentPage,
        managementPageSize,
        getSelectedPermissions(),
        managementSortState.sortColumn,
        managementSortState.sortDirection === "ascending" ? "ASC" : "DESC"
      );
    }
  }, [selectedMainTab, managementFilters, managementSortState]);

  // 过滤和搜索逻辑
  useEffect(() => {
    let filtered = [...pendingUsers];

    // 模糊搜索
    if (searchValue) {
      const searchLower = searchValue.toLowerCase();
      filtered = filtered.filter(
        (user) =>
          (user.username || "").toLowerCase().includes(searchLower) ||
          (user.alias || "").toLowerCase().includes(searchLower) ||
          (user.phone || "").toLowerCase().includes(searchLower) ||
          (user.email || "").toLowerCase().includes(searchLower)
      );
    }

    setFilteredPendingUsers(filtered);
    setPendingTotalCount(filtered.length);
  }, [pendingUsers, searchValue]);

  // 搜索值变化时重置页码
  useEffect(() => {
    setPendingCurrentPage(1);
  }, [searchValue]);

  // 已审核用户过滤和搜索逻辑
  useEffect(() => {
    let filtered = [...checkedUsers];

    // 过滤掉privilege为0的数据
    filtered = filtered.filter((user) => user.privilege > 0);

    // 模糊搜索
    if (checkedSearchValue) {
      const searchLower = checkedSearchValue.toLowerCase();
      filtered = filtered.filter(
        (user) =>
          (user.username || "").toLowerCase().includes(searchLower) ||
          (user.alias || "").toLowerCase().includes(searchLower) ||
          (user.phone || "").toLowerCase().includes(searchLower) ||
          (user.email || "").toLowerCase().includes(searchLower)
      );
    }

    setFilteredCheckedUsers(filtered);
    setCheckedTotalCount(filtered.length);
  }, [checkedUsers, checkedSearchValue]);

  // 搜索值变化时重置页码
  useEffect(() => {
    setCheckedCurrentPage(1);
  }, [checkedSearchValue]);

  // 用户管理过滤和搜索逻辑
  useEffect(() => {
    let filtered = [...managementUsers];

    // 模糊搜索
    if (managementSearchValue) {
      const searchLower = managementSearchValue.toLowerCase();
      filtered = filtered.filter(
        (user) =>
          (user.username || "").toLowerCase().includes(searchLower) ||
          (user.alias || "").toLowerCase().includes(searchLower) ||
          (user.phone || "").toLowerCase().includes(searchLower) ||
          (user.email || "").toLowerCase().includes(searchLower)
      );
    }

    setFilteredManagementUsers(filtered);
    setManagementTotalCount(filtered.length);
  }, [managementUsers, managementSearchValue]);

  // 搜索值变化时重置页码
  useEffect(() => {
    setManagementCurrentPage(1);
  }, [managementSearchValue]);

  function getTotalPages() {
    return Math.ceil(pendingTotalCount / pendingPageSize);
  }

  function getCheckedTotalPages() {
    return Math.ceil(checkedTotalCount / checkedPageSize);
  }

  function handleApprove(userId: string, username: string) {
    setRoleDialog({ open: true, userId, username });
    setSelectedRoles({ service: false, admin: false });
    setRoleAssignError("");
  }

  function handleDelete(alias: string) {
    setConfirmDialog({
      open: true,
      type: "delete",
      userId: alias,
      title: "删除用户",
      message: "确定要删除该用户吗？此操作无法撤销。",
    });
  }

  function handleBatchDelete() {
    if (selectedUsers.size === 0) return;
    setConfirmDialog({
      open: true,
      type: "batchDelete",
      userId: "",
      title: "批量删除用户",
      message: `确定要删除选中的 ${selectedUsers.size} 个用户吗？此操作无法撤销。`,
    });
  }

  function handleBatchApprove() {
    if (selectedUsers.size === 0) return;
    setConfirmDialog({
      open: true,
      type: "batchApprove",
      userId: "",
      title: "批量批准",
      message: `确定要批准选中的 ${selectedUsers.size} 个用户吗？选中的用户的权限均为客服。`,
    });
  }

  function handleRefreeze(userId: string) {
    setConfirmDialog({
      open: true,
      type: "refreeze",
      userId,
      title: "重新拒绝用户",
      message: "确定要重新拒绝该用户吗？",
    });
  }

  async function handleViewUser(id: string) {
    try {
      // 打开对话框并显示加载状态
      setViewDialog({
        open: true,
        user: null,
        loading: true,
        error: "",
      });

      // 调用API获取用户详情
      const response = await api.get(`/users/${id}`);

      if (response.data.success) {
        // 检查响应数据结构并转换为适当的用户类型
        let user: IUserReviewItem;

        user = BackendUserToUserReviewItemConverter(response.data.data);

        // 更新对话框状态，显示用户详情
        setViewDialog({
          open: true,
          user,
          loading: false,
          error: "",
        });
      } else {
        // API返回错误
        setViewDialog({
          open: true,
          user: null,
          loading: false,
          error: response.data.message || "获取用户详情失败",
        });
      }
    } catch (error: any) {
      // 捕获网络错误或其他异常
      setViewDialog({
        open: true,
        user: null,
        loading: false,
        error: error.response?.data?.message || "获取用户详情失败，请重试",
      });
    }
  }

  // 激活用户
  async function activateUser(userId: string, privilege: number) {
    try {
      setRoleAssignLoading(true);
      setRoleAssignError("");

      const response = await api.put(`/users/${userId}`, {
        privilege: privilege,
      });

      if (response.data.success) {
        // 激活成功，根据当前tab刷新对应的表格
        if (selectedSubTab === "pending") {
          await fetchUnactivatedUsers();
        } else if (selectedSubTab === "reviewed") {
          await fetchCheckedUsers();
        }
        setRoleDialog({ open: false, userId: "", username: "" });
        // 清除选中状态
        clearSelection();
      }
    } catch (error: any) {
      setRoleAssignError(error.response?.data?.message || "激活失败，请重试");
    } finally {
      setRoleAssignLoading(false);
    }
  }

  // 拒绝用户
  async function rejectUser(userId: string) {
    try {
      setConfirmLoading(true);
      setConfirmError("");

      const response = await api.delete(`/users/${userId}`);

      if (response.data.success) {
        // 拒绝成功，根据当前tab刷新对应的表格
        if (selectedSubTab === "pending") {
          await fetchUnactivatedUsers();
        } else if (selectedSubTab === "reviewed") {
          await fetchCheckedUsers();
        }
        setConfirmDialog({
          open: false,
          type: "",
          userId: "",
          title: "",
          message: "",
        });
        // 清除选中状态
        clearSelection();
      }
    } catch (error: any) {
      setConfirmError(error.response?.data?.message || "拒绝失败，请重试");
    } finally {
      setConfirmLoading(false);
    }
  }

  // 批量激活用户
  async function batchActivateUsers() {
    try {
      setConfirmLoading(true);
      setConfirmError("");

      const userIds = Array.from(selectedUsers);
      const results = [];

      for (const userId of userIds) {
        try {
          const response = await api.post(`/users/${userId}/activate`, null, {
            params: { privilege: 1 }, // 默认权限位掩码为1
          });
          results.push({ userId, success: response.data.success });
        } catch (error) {
          results.push({ userId, success: false });
        }
      }

      const failedCount = results.filter((r) => !r.success).length;
      if (failedCount > 0) {
        setConfirmError(`批量操作完成，其中 ${failedCount} 个用户激活失败`);
      }

      // 刷新表格
      await fetchUnactivatedUsers();

      // 清除选中状态
      clearSelection();

      if (failedCount === 0) {
        setConfirmDialog({
          open: false,
          type: "",
          userId: "",
          title: "",
          message: "",
        });
      }
    } catch (error: any) {
      setConfirmError(error.response?.data?.message || "批量激活失败，请重试");
    } finally {
      setConfirmLoading(false);
    }
  }

  // 批量重新批准用户（跳过已通过的人）
  async function batchReapproveUsers() {
    try {
      setConfirmLoading(true);
      setConfirmError("");

      const userIds = Array.from(selectedUsers);
      // 过滤出需要重新批准的用户（状态是FROZEN的）
      const usersToReapprove = checkedUsers.filter((user) => userIds.includes(user.userId) && user.status == "OFFLINE");

      const results = [];

      for (const user of usersToReapprove) {
        try {
          const response = await api.post(`/users/${user.userId}/activate`, null, {
            params: { privilege: 1 }, // 默认权限位掩码为1
          });
          results.push({
            userId: user.userId,
            success: response.data.success,
          });
        } catch (error) {
          results.push({ userId: user.userId, success: false });
        }
      }

      const failedCount = results.filter((r) => !r.success).length;

      let message = "";
      if (failedCount > 0) {
        message = `批量操作完成，其中 ${failedCount} 个用户重新批准失败`;
      }

      if (message) {
        setConfirmError(message);
      }

      // 刷新表格
      await fetchCheckedUsers();

      // 清除选中状态
      clearSelection();

      if (failedCount === 0 && !message) {
        setConfirmDialog({
          open: false,
          type: "",
          userId: "",
          title: "",
          message: "",
        });
      }
    } catch (error: any) {
      setConfirmError(error.response?.data?.message || "批量重新批准失败，请重试");
    } finally {
      setConfirmLoading(false);
    }
  }

  // 批量重新拒绝用户
  async function batchRefreezeUsers() {
    try {
      setConfirmLoading(true);
      setConfirmError("");

      const userIds = Array.from(selectedUsers);
      const results = [];

      for (const userId of userIds) {
        try {
          const response = await api.post(`/users/${userId}/freeze`);
          results.push({ userId, success: response.data.success });
        } catch (error) {
          results.push({ userId, success: false });
        }
      }

      const failedCount = results.filter((r) => !r.success).length;
      if (failedCount > 0) {
        setConfirmError(`批量操作完成，其中 ${failedCount} 个用户重新拒绝失败`);
      }

      // 刷新表格
      await fetchCheckedUsers();

      // 清除选中状态
      clearSelection();

      if (failedCount === 0) {
        setConfirmDialog({
          open: false,
          type: "",
          userId: "",
          title: "",
          message: "",
        });
      }
    } catch (error: any) {
      setConfirmError(error.response?.data?.message || "批量重新拒绝失败，请重试");
    } finally {
      setConfirmLoading(false);
    }
  }

  // 批量拒绝用户
  async function batchRejectUsers() {
    try {
      setConfirmLoading(true);
      setConfirmError("");

      const userIds = Array.from(selectedUsers);
      const results = [];

      for (const userId of userIds) {
        try {
          const response = await api.post(`/users/${userId}/freeze`);
          results.push({ userId, success: response.data.success });
        } catch (error) {
          results.push({ userId, success: false });
        }
      }

      const failedCount = results.filter((r) => !r.success).length;
      if (failedCount > 0) {
        setConfirmError(`批量操作完成，其中 ${failedCount} 个用户拒绝失败`);
      }

      // 刷新表格
      await fetchUnactivatedUsers();

      // 清除选中状态
      clearSelection();

      if (failedCount === 0) {
        setConfirmDialog({
          open: false,
          type: "",
          userId: "",
          title: "",
          message: "",
        });
      }
    } catch (error: any) {
      setConfirmError(error.response?.data?.message || "批量拒绝失败，请重试");
    } finally {
      setConfirmLoading(false);
    }
  }

  function confirmAction() {
    switch (confirmDialog.type) {
      case "reject":
        rejectUser(confirmDialog.userId);
        break;
      case "batchApprove":
        batchActivateUsers();
        break;
      case "batchReject":
        batchRejectUsers();
        break;
      case "refreeze":
        rejectUser(confirmDialog.userId);
        break;
      case "batchReapprove":
        batchReapproveUsers();
        break;
      case "batchRefreeze":
        batchRefreezeUsers();
        break;
      case "delete":
        deleteUser(confirmDialog.userId);
        break;
      case "batchDelete":
        batchDeleteUsers();
        break;
      default:
        setConfirmDialog({
          open: false,
          type: "",
          userId: "",
          title: "",
          message: "",
        });
    }
  }

  async function saveRoleAssignment() {
    let permissions = 0;
    if (selectedRoles.service) {
      permissions |= PERMISSIONS.CUSTOMER_SERVICE;
    }
    if (selectedRoles.admin) {
      permissions |= PERMISSIONS.ADMIN_MAINTENANCE;
    }
    if (permissions === 0) {
      setRoleAssignError("请至少选择一个权限");
      return;
    }

    await activateUser(roleDialog.userId, permissions);
  }

  // 统一分页组件
  function PaginationComponent({
    currentPage,
    setCurrentPage,
    pageSize,
    totalCount,
    pageInput,
    setPageInput,
    getTotalPages,
  }: {
    currentPage: number;
    setCurrentPage: (page: number) => void;
    pageSize: number;
    totalCount: number;
    pageInput: string;
    setPageInput: (input: string) => void;
    getTotalPages: () => number;
  }) {
    return (
      <div className={styles.pagination}>
        <Button
          disabled={currentPage === 1}
          onClick={() => {
            setCurrentPage(1);
          }}>
          首页
        </Button>
        <Button
          disabled={currentPage === 1}
          onClick={() => {
            const newPage = currentPage - 1;
            setCurrentPage(newPage);
          }}>
          上一页
        </Button>
        <Text>
          第 {currentPage} 页，共 {getTotalPages()} 页，总共 {totalCount} 条
        </Text>
        <Button
          disabled={currentPage >= getTotalPages() || getTotalPages() === 0}
          onClick={() => {
            const newPage = currentPage + 1;
            setCurrentPage(newPage);
          }}>
          下一页
        </Button>
        <Button
          disabled={currentPage >= getTotalPages() || getTotalPages() === 0}
          onClick={() => {
            setCurrentPage(getTotalPages());
          }}>
          末页
        </Button>
        <Text style={{ marginLeft: "16px" }}>跳转到第</Text>
        <Input
          type="number"
          style={{ width: "100px" }}
          min={1}
          max={getTotalPages()}
          value={pageInput}
          onChange={(_, data) => {
            // 只允许输入数字
            const sanitizedValue = data.value.replace(/[^0-9]/g, "");
            // 限制最小值为1
            const numericValue = parseInt(sanitizedValue) || "";
            setPageInput(numericValue ? String(numericValue) : "");
          }}
          onBlur={() => {
            // 失去焦点时验证并跳转
            if (pageInput) {
              const pageNum = Math.floor(Number(pageInput));
              const totalPages = getTotalPages();
              let targetPage = pageNum;

              if (pageNum < 1) targetPage = 1;
              else if (pageNum > totalPages) targetPage = totalPages;

              setCurrentPage(targetPage);
              setPageInput("");
            }
          }}
          onKeyDown={(e) => {
            if (e.key === "Enter") {
              const pageNum = Math.floor(Number(pageInput));
              const totalPages = getTotalPages();
              let targetPage = pageNum;

              if (pageNum < 1) targetPage = 1;
              else if (pageNum > totalPages) targetPage = totalPages;

              setCurrentPage(targetPage);
              setPageInput("");
            }
          }}
        />
        <Text>页</Text>
      </div>
    );
  }

  // 渲染待审核用户表格
  function renderPendingTable() {
    const columns: TableColumnDefinition<IUserReviewItem>[] = [
      createTableColumn<IUserReviewItem>({
        columnId: "用户名",
        compare: (a, b) => String(a.username ?? "").localeCompare(String(b.username ?? "")),
      }),
      createTableColumn<IUserReviewItem>({
        columnId: "昵称",
        compare: (a, b) => String(a.alias ?? "").localeCompare(String(b.alias ?? "")),
      }),
      createTableColumn<IUserReviewItem>({
        columnId: "手机号",
        compare: (a, b) => {
          // 按区号排序，提取手机号前三位作为区号
          const getAreaCode = (phone: string | null | undefined) => String(phone ?? "").substring(0, 3);
          return getAreaCode(a.phone).localeCompare(getAreaCode(b.phone));
        },
      }),
      createTableColumn<IUserReviewItem>({
        columnId: "邮箱",
        compare: (a, b) => String(a.email ?? "").localeCompare(String(b.email ?? "")),
      }),
      createTableColumn<IUserReviewItem>({
        columnId: "状态",
        compare: (a, b) => String(a.status ?? "").localeCompare(String(b.status ?? "")),
      }),
      createTableColumn<IUserReviewItem>({ columnId: "操作" }),
    ];

    const [selectedRows, setSelectedRows] = useState(() => new Set<TableRowId>());

    // 当 tableResetKey 变化时重置选中状态
    useEffect(() => {
      setSelectedRows(new Set<TableRowId>());
    }, [tableResetKey]);

    // 预先计算排序和分页数据
    const startIndex = (pendingCurrentPage - 1) * pendingPageSize;
    const endIndex = startIndex + pendingPageSize;

    const {
      getRows,
      selection: { allRowsSelected, someRowsSelected, toggleAllRows, toggleRow, isRowSelected },
      sort: { getSortDirection, toggleColumnSort, sort },
    } = useTableFeatures(
      {
        columns,
        items: filteredPendingUsers, // 对所有过滤后的数据进行排序
      },
      [
        useTableSelection({
          selectionMode: "multiselect",
          selectedItems: selectedRows,
          onSelectionChange: (_, data) => {
            setSelectedRows(data.selectedItems);
            // 更新全局选中状态
            const selectedIds = new Set<string>();
            const sortedData = sort(getRows()).map((row) => row.item);
            const pagedData = sortedData.slice(startIndex, endIndex);

            data.selectedItems.forEach((rowId) => {
              // rowId是globalIndex，需要计算相对于当前页的索引
              const pageIndex = (rowId as number) - startIndex;
              const user = pagedData[pageIndex];
              if (user) {
                selectedIds.add(user.userId);
              }
            });
            setSelectedUsers(selectedIds);
          },
        }),
        useTableSort({
          sortState: pendingSortState,
          onSortChange: (_, nextSortState) => setPendingSortState(nextSortState),
        }),
      ]
    );

    function headerSortProps(columnId: TableColumnId) {
      return {
        onClick: (e: React.MouseEvent) => toggleColumnSort(e, columnId),
        sortDirection: getSortDirection(columnId),
      };
    }

    // 对所有数据进行排序，然后进行分页
    const sortedData = sort(getRows()).map((row) => row.item);
    const pagedData = sortedData.slice(startIndex, endIndex);

    const rows = pagedData.map((item, index) => {
      const globalIndex = startIndex + index;
      const selected = isRowSelected(globalIndex);
      // 行样式：奇偶行不同底色
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
        appearance: selected ? ("brand" as const) : ("none" as const),
        rowClassName,
      };
    });

    const toggleAllKeydown = useCallback(
      (e: React.KeyboardEvent<HTMLDivElement>) => {
        if (e.key === " ") {
          toggleAllRows(e);
          e.preventDefault();
        }
      },
      [toggleAllRows]
    );

    return (
      <div className={styles.tableContainer}>
        {/* 工具栏 */}
        <div className={styles.toolbar}>
          <Button appearance="primary" onClick={handleBatchApprove} disabled={selectedUsers.size === 0}>
            批量批准
          </Button>
          <Button onClick={handleBatchReject} disabled={selectedUsers.size === 0}>
            批量拒绝
          </Button>
          <SearchBox
            className={styles.searchBox}
            placeholder="搜索用户名、昵称、手机号或邮箱"
            value={searchValue}
            onChange={(_, data) => {
              setSearchValue(data.value);
              setPendingCurrentPage(1); // 重置到第一页
            }}
          />
          <Button
            onClick={() => {
              setSearchValue("");
              setPendingCurrentPage(1);
            }}>
            清除筛选
          </Button>
        </div>

        {/* 加载状态 */}
        {pendingLoading ? (
          <div className={styles.spinnerContainer}>
            <Spinner label="加载中..." />
          </div>
        ) : (
          <Table key={`pending-${tableResetKey}`} sortable className={styles.table}>
            <TableHeader>
              <TableRow>
                <TableSelectionCell
                  checked={allRowsSelected ? true : someRowsSelected ? "mixed" : false}
                  onClick={toggleAllRows}
                  onKeyDown={toggleAllKeydown}
                  checkboxIndicator={{
                    "aria-label": "Select all rows ",
                  }}
                />
                <TableHeaderCell {...headerSortProps("用户名")}>用户名</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("昵称")}>昵称</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("手机号")}>手机号</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("邮箱")}>邮箱</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("状态")}>状态</TableHeaderCell>
                <TableHeaderCell>操作</TableHeaderCell>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map(({ item, selected, onClick, onKeyDown, appearance, rowClassName }) => (
                <TableRow
                  key={item.userId}
                  onClick={onClick}
                  onKeyDown={onKeyDown}
                  aria-selected={selected}
                  appearance={appearance}
                  className={rowClassName}>
                  <TableSelectionCell
                    subtle
                    checked={selected}
                    checkboxIndicator={{
                      "aria-label": "Select row",
                    }}
                  />
                  <TableCell>{item.username}</TableCell>
                  <TableCell>{item.alias}</TableCell>
                  <TableCell>{item.phone}</TableCell>
                  <TableCell>{item.email}</TableCell>
                  <TableCell>{renderUserStatus(item.status)}</TableCell>
                  <TableCell>
                    <div className={styles.buttonGroup}>
                      <Button
                        appearance="primary"
                        size="small"
                        icon={<CheckmarkRegular />}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleApprove(item.userId, item.username);
                        }}>
                        批准
                      </Button>
                      <Button
                        size="small"
                        icon={<DismissRegular />}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleReject(item.userId);
                        }}>
                        拒绝
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}

        {/* 分页 */}
        <PaginationComponent
          currentPage={pendingCurrentPage}
          setCurrentPage={setPendingCurrentPage}
          pageSize={pendingPageSize}
          totalCount={pendingTotalCount}
          pageInput={pendingPageInput}
          setPageInput={setPendingPageInput}
          getTotalPages={getTotalPages}
        />
      </div>
    );
  }

  // 渲染已审核用户表格
  function renderReviewedTable() {
    const columns: TableColumnDefinition<IUserReviewItem>[] = [
      createTableColumn<IUserReviewItem>({
        columnId: "用户名",
        compare: (a, b) => String(a.username ?? "").localeCompare(String(b.username ?? "")),
      }),
      createTableColumn<IUserReviewItem>({
        columnId: "昵称",
        compare: (a, b) => String(a.alias ?? "").localeCompare(String(b.alias ?? "")),
      }),
      createTableColumn<IUserReviewItem>({
        columnId: "手机号",
        compare: (a, b) => {
          // 按区号排序，提取手机号前三位作为区号
          const getAreaCode = (phone: string | null | undefined) => String(phone ?? "").substring(0, 3);
          return getAreaCode(a.phone).localeCompare(getAreaCode(b.phone));
        },
      }),
      createTableColumn<IUserReviewItem>({
        columnId: "邮箱",
        compare: (a, b) => String(a.email ?? "").localeCompare(String(b.email ?? "")),
      }),
      createTableColumn<IUserReviewItem>({
        columnId: "状态",
        compare: (a, b) => String(a.status ?? "").localeCompare(String(b.status ?? "")),
      }),
      createTableColumn<IUserReviewItem>({
        columnId: "权限",
        compare: (a, b) => {
          const privA = a.permission ?? 0;
          const privB = b.permission ?? 0;
          return privA - privB;
        },
      }),
      createTableColumn<IUserReviewItem>({
        columnId: "操作",
      }),
    ];

    const [selectedRows, setSelectedRows] = useState(() => new Set<TableRowId>());

    // 当 tableResetKey 变化时重置选中状态
    useEffect(() => {
      setSelectedRows(new Set<TableRowId>());
    }, [tableResetKey]);

    // 预先计算排序和分页数据
    const startIndex = (checkedCurrentPage - 1) * checkedPageSize;
    const endIndex = startIndex + checkedPageSize;

    const {
      getRows,
      selection: { allRowsSelected, someRowsSelected, toggleAllRows, toggleRow, isRowSelected },
      sort: { getSortDirection, toggleColumnSort, sort },
    } = useTableFeatures(
      {
        columns,
        items: filteredCheckedUsers, // 对所有过滤后的数据进行排序
      },
      [
        useTableSelection({
          selectionMode: "multiselect",
          selectedItems: selectedRows,
          onSelectionChange: (_, data) => {
            setSelectedRows(data.selectedItems);
            // 更新全局选中状态
            const selectedIds = new Set<string>();
            const sortedData = sort(getRows()).map((row) => row.item);
            const pagedData = sortedData.slice(startIndex, endIndex);

            data.selectedItems.forEach((rowId) => {
              // rowId是globalIndex，需要计算相对于当前页的索引
              const pageIndex = (rowId as number) - startIndex;
              const user = pagedData[pageIndex];
              if (user) {
                selectedIds.add(user.userId);
              }
            });
            setSelectedUsers(selectedIds);
          },
        }),
        useTableSort({
          sortState: checkedSortState,
          onSortChange: (_, nextSortState) => setCheckedSortState(nextSortState),
        }),
      ]
    );

    function headerSortProps(columnId: TableColumnId) {
      return {
        onClick: (e: React.MouseEvent) => toggleColumnSort(e, columnId),
        sortDirection: getSortDirection(columnId),
      };
    }

    // 对所有数据进行排序，然后进行分页
    const sortedData = sort(getRows()).map((row) => row.item);
    const pagedData = sortedData.slice(startIndex, endIndex);

    const rows = pagedData.map((item, index) => {
      const globalIndex = startIndex + index;
      const selected = isRowSelected(globalIndex);
      // 行样式：奇偶行不同底色
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
        appearance: selected ? ("brand" as const) : ("none" as const),
        rowClassName,
      };
    });

    const toggleAllKeydown = useCallback(
      (e: React.KeyboardEvent<HTMLDivElement>) => {
        if (e.key === " ") {
          toggleAllRows(e);
          e.preventDefault();
        }
      },
      [toggleAllRows]
    );

    return (
      <div className={styles.tableContainer}>
        {/* 工具栏 */}
        <div className={styles.toolbar}>
          <SearchBox
            className={styles.searchBox}
            placeholder="搜索用户名、昵称、手机号或邮箱"
            value={checkedSearchValue}
            onChange={(_, data) => {
              setCheckedSearchValue(data.value);
              setCheckedCurrentPage(1); // 重置到第一页
            }}
          />
          <Button
            onClick={() => {
              setCheckedSearchValue("");
              setCheckedCurrentPage(1);
            }}>
            清除筛选
          </Button>
        </div>

        {/* 加载状态 */}
        {checkedLoading ? (
          <div className={styles.spinnerContainer}>
            <Spinner label="加载中..." />
          </div>
        ) : (
          <Table key={`reviewed-${tableResetKey}`} sortable className={styles.table}>
            <TableHeader>
              <TableRow>
                <TableSelectionCell
                  checked={allRowsSelected ? true : someRowsSelected ? "mixed" : false}
                  onClick={toggleAllRows}
                  onKeyDown={toggleAllKeydown}
                  checkboxIndicator={{
                    "aria-label": "Select all rows ",
                  }}
                />
                <TableHeaderCell {...headerSortProps("用户名")}>用户名</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("昵称")}>昵称</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("手机号")}>手机号</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("邮箱")}>邮箱</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("状态")}>状态</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("权限")}>权限</TableHeaderCell>
                <TableHeaderCell>操作</TableHeaderCell>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map(({ item, selected, onClick, onKeyDown, appearance, rowClassName }) => (
                <TableRow
                  key={item.userId}
                  onClick={onClick}
                  onKeyDown={onKeyDown}
                  aria-selected={selected}
                  appearance={appearance}
                  className={rowClassName}>
                  <TableSelectionCell
                    subtle
                    checked={selected}
                    checkboxIndicator={{
                      "aria-label": "Select row",
                    }}
                  />
                  <TableCell>{item.username}</TableCell>
                  <TableCell>{item.alias}</TableCell>
                  <TableCell>{item.phone}</TableCell>
                  <TableCell>{item.email}</TableCell>
                  <TableCell>{renderUserStatus(item.status)}</TableCell>
                  <TableCell>{PermissionCodeToTextConverter(item.permission)}</TableCell>
                  <TableCell>
                    <div className={styles.buttonGroup}>
                      <Button
                        size="small"
                        icon={<EyeRegular />}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleViewUser(item.userId);
                        }}>
                        查看
                      </Button>
                      <Button
                        size="small"
                        icon={<CheckmarkRegular />}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleApprove(item.userId, item.username);
                        }}>
                        编辑
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}

        {/* 分页 */}
        <PaginationComponent
          currentPage={checkedCurrentPage}
          setCurrentPage={setCheckedCurrentPage}
          pageSize={checkedPageSize}
          totalCount={checkedTotalCount}
          pageInput={checkedPageInput}
          setPageInput={setCheckedPageInput}
          getTotalPages={getCheckedTotalPages}
        />
      </div>
    );
  }

  function renderUserStatus(status?: string) {
    const statusMap: Record<string, string> = {
      AVAILABLE: "空闲",
      BUSY: "忙碌中",
      POST_PROCESS: "后处理",
      BREAK: "暂离/休息",
      OFFLINE: "离线",
      OTHER: "其他"
    };
    return statusMap[status || ""] || status || "未知";
  }

  // 渲染用户管理表格
  function renderManagementTable() {
    const columns: TableColumnDefinition<IUserManagementItem>[] = [
      createTableColumn<IUserManagementItem>({
        columnId: "username",
        compare: (a, b) => String(a.username ?? "").localeCompare(String(b.username ?? "")),
      }),
      createTableColumn<IUserManagementItem>({
        columnId: "alias",
        compare: (a, b) => String(a.alias ?? "").localeCompare(String(b.alias ?? "")),
      }),
      createTableColumn<IUserManagementItem>({
        columnId: "phone",
        compare: (a, b) => String(a.phone ?? "").localeCompare(String(b.phone ?? "")),
      }),
      createTableColumn<IUserManagementItem>({
        columnId: "email",
        compare: (a, b) => String(a.email ?? "").localeCompare(String(b.email ?? "")),
      }),
      createTableColumn<IUserManagementItem>({
        columnId: "status",
        compare: (a, b) => String(a.status ?? "").localeCompare(String(b.status ?? "")),
      }),
      createTableColumn<IUserManagementItem>({
        columnId: "privilege",
        compare: (a, b) => {
          const privA = a.permission ?? 0;
          const privB = b.permission ?? 0;
          return privA - privB;
        },
      }),
      createTableColumn<IUserManagementItem>({
        columnId: "操作",
      }),
    ];

    const [selectedRows, setSelectedRows] = useState(() => new Set<TableRowId>());

    // 当 tableResetKey 变化时重置选中状态
    useEffect(() => {
      setSelectedRows(new Set<TableRowId>());
    }, [tableResetKey]);

    // 预先计算排序和分页数据
    const startIndex = (managementCurrentPage - 1) * managementPageSize;
    const endIndex = startIndex + managementPageSize;

    const {
      getRows,
      selection: { allRowsSelected, someRowsSelected, toggleAllRows, toggleRow, isRowSelected },
      sort: { getSortDirection, toggleColumnSort, sort },
    } = useTableFeatures(
      {
        columns,
        items: filteredManagementUsers,
      },
      [
        useTableSelection({
          selectionMode: "multiselect",
          selectedItems: selectedRows,
          onSelectionChange: (_, data) => {
            setSelectedRows(data.selectedItems);
            // 更新全局选中状态
            const selectedIds = new Set<string>();
            const sortedData = sort(getRows()).map((row) => row.item);
            const pagedData = sortedData.slice(startIndex, endIndex);

            data.selectedItems.forEach((rowId) => {
              // rowId是globalIndex，需要计算相对于当前页的索引
              const pageIndex = (rowId as number) - startIndex;
              const user = pagedData[pageIndex];
              if (user) {
                selectedIds.add(user.alias);
              }
            });
            setSelectedUsers(selectedIds);
          },
        }),
        useTableSort({
          sortState: {
            sortDirection: managementSortState.sortDirection as "ascending" | "descending",
            sortColumn: managementSortState.sortColumn,
          },
          onSortChange: (_, nextSortState) => {
            setManagementSortState({
              sortDirection: nextSortState.sortDirection,
              sortColumn: String(nextSortState.sortColumn) || "email",
            });
          },
        }),
      ]
    );

    function headerSortProps(columnId: TableColumnId) {
      return {
        onClick: (e: React.MouseEvent) => toggleColumnSort(e, columnId),
        sortDirection: getSortDirection(columnId),
      };
    }

    // 对所有数据进行排序，然后进行分页
    const sortedData = sort(getRows()).map((row) => row.item);
    const pagedData = sortedData.slice(startIndex, endIndex);

    const rows = pagedData.map((item, index) => {
      const globalIndex = startIndex + index;
      const selected = isRowSelected(globalIndex);
      // 行样式：奇偶行不同底色
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
        appearance: selected ? ("brand" as const) : ("none" as const),
        rowClassName,
      };
    });

    const toggleAllKeydown = useCallback(
      (e: React.KeyboardEvent<HTMLDivElement>) => {
        if (e.key === " ") {
          toggleAllRows(e);
          e.preventDefault();
        }
      },
      [toggleAllRows]
    );

    return (
      <div className={styles.tableContainer}>
        {/* 工具栏 */}
        <div className={styles.toolbar}>
          <Button icon={<DeleteRegular />} onClick={handleBatchDelete} disabled={selectedUsers.size === 0}>
            批量删除
          </Button>
          <SearchBox
            className={styles.searchBox}
            placeholder="搜索用户名、昵称、手机号或邮箱"
            value={managementSearchValue}
            onChange={(_, data) => {
              setManagementSearchValue(data.value);
              setManagementCurrentPage(1); // 重置到第一页
            }}
          />
          <Button
            onClick={() => {
              setManagementSearchValue("");
              setManagementCurrentPage(1);
            }}>
            清除筛选
          </Button>
          <div className={styles.checkboxGroup}>
            <Checkbox
              label="游客"
              checked={managementFilters.showVisitors}
              onChange={(_, data) => {
                setManagementFilters((prev) => ({
                  ...prev,
                  showVisitors: !!data.checked,
                }));
                setManagementCurrentPage(1); // 重置到第一页
              }}
            />
            {isSuperAdmin() && (
              <Checkbox
                label="管理员"
                checked={managementFilters.showAdmins}
                onChange={(_, data) => {
                  setManagementFilters((prev) => ({
                    ...prev,
                    showAdmins: !!data.checked,
                  }));
                  setManagementCurrentPage(1); // 重置到第一页
                }}
              />
            )}
          </div>
          <InfoLabel
            info={
              <>
                筛选逻辑：
                <br />
                若只有一个复选框被选中，则显示仅有该权限的用户。
                <br />
                若多个复选框被选中，则显示分别仅有对应权限的用户以及同时具有选择的权限的用户。
              </>
            }></InfoLabel>
        </div>

        {/* 加载状态 */}
        {managementLoading ? (
          <div className={styles.spinnerContainer}>
            <Spinner label="加载中..." />
          </div>
        ) : (
          <Table key={`management-${tableResetKey}`} sortable className={styles.table}>
            <TableHeader>
              <TableRow>
                <TableSelectionCell
                  checked={allRowsSelected ? true : someRowsSelected ? "mixed" : false}
                  onClick={toggleAllRows}
                  onKeyDown={toggleAllKeydown}
                  checkboxIndicator={{
                    "aria-label": "Select all rows ",
                  }}
                />
                <TableHeaderCell {...headerSortProps("username")}>用户名</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("alias")}>昵称</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("phone")}>手机号</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("email")}>邮箱</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("status")}>状态</TableHeaderCell>
                <TableHeaderCell {...headerSortProps("privilege")}>权限</TableHeaderCell>
                <TableHeaderCell>操作</TableHeaderCell>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map(({ item, selected, onClick, onKeyDown, appearance, rowClassName }) => (
                <TableRow
                  key={item.alias}
                  onClick={onClick}
                  onKeyDown={onKeyDown}
                  aria-selected={selected}
                  appearance={appearance}
                  className={rowClassName}>
                  <TableSelectionCell
                    subtle
                    checked={selected}
                    checkboxIndicator={{
                      "aria-label": "Select row",
                    }}
                  />
                  <TableCell>{item.username}</TableCell>
                  <TableCell>{item.alias}</TableCell>
                  <TableCell>{item.phone}</TableCell>
                  <TableCell>{item.email}</TableCell>
                  <TableCell>{renderUserStatus(item.status)}</TableCell>
                  <TableCell>{PermissionCodeToTextConverter(item.permission)}</TableCell>
                  <TableCell>
                    <div className={styles.buttonGroup}>
                      <Button
                        size="small"
                        icon={<EyeRegular />}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleViewUser(item.id);
                        }}>
                        查看
                      </Button>
                      <Button
                        size="small"
                        icon={<DeleteRegular />}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete(item.alias);
                        }}>
                        删除
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}

        {/* 分页 */}
        <PaginationComponent
          currentPage={managementCurrentPage}
          setCurrentPage={setManagementCurrentPage}
          pageSize={managementPageSize}
          totalCount={managementTotalCount}
          pageInput={managementPageInput}
          setPageInput={setManagementPageInput}
          getTotalPages={getManagementTotalPages}
        />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <Title1>管理员中心</Title1>

      {/* 主选项卡 */}
      <TabList
        className={styles.mainTabs}
        appearance="transparent"
        selectedValue={selectedMainTab}
        onTabSelect={(_, data) => {
          const newTab = data.value as string;
          setMainTab(newTab);
          updateState({ selectedMainTab: newTab });
        }}>
        <Tab value="review">用户注册审核</Tab>
        <Tab value="management">用户管理</Tab>
      </TabList>

      {/* 用户注册审核内容 */}
      {selectedMainTab === "review" && (
        <>
          <TabList
            className={styles.subTabs}
            appearance="subtle-circular"
            selectedValue={selectedSubTab}
            onTabSelect={(_, data) => {
              const newSubTab = data.value as string;
              setSubTab(newSubTab);
              updateState({ selectedSubTab: newSubTab });
            }}>
            <Tab value="pending">未通过</Tab>
            <Tab value="reviewed">已通过</Tab>
          </TabList>

          {selectedSubTab === "pending" && renderPendingTable()}
          {selectedSubTab === "reviewed" && renderReviewedTable()}
        </>
      )}

      {/* 用户管理内容 */}
      {selectedMainTab === "management" && renderManagementTable()}

      {/* 角色分配对话框 */}
      <Dialog
        open={roleDialog.open}
        onOpenChange={(_, data) => {
          if (!roleAssignLoading) {
            setRoleDialog({ ...roleDialog, open: data.open });
            if (!data.open) {
              setRoleAssignError("");
            }
          }
        }}>
        <DialogSurface>
          <DialogBody>
            <DialogTitle>分配用户角色</DialogTitle>
            <DialogContent>
              <Text>为用户 "{roleDialog.username}" 选择角色权限：</Text>

              {/* 错误消息 */}
              {roleAssignError && (
                <MessageBar intent="error" style={{ marginBottom: "16px" }}>
                  <MessageBarBody>{roleAssignError}</MessageBarBody>
                </MessageBar>
              )}

              <div className={styles.roleCheckboxes}>
                <Checkbox
                  label="管理员"
                  checked={selectedRoles.admin}
                  onChange={(_, data) =>
                    setSelectedRoles((prev) => ({
                      ...prev,
                      admin: !!data.checked,
                    }))
                  }
                  disabled={roleAssignLoading}
                />
              </div>
            </DialogContent>
            <DialogActions>
              <Button
                appearance="secondary"
                onClick={() => {
                  setRoleDialog({
                    open: false,
                    userId: "",
                    username: "",
                  });
                  setRoleAssignError("");
                }}
                disabled={roleAssignLoading}>
                取消
              </Button>
              <Button appearance="primary" onClick={saveRoleAssignment} disabled={roleAssignLoading}>
                {roleAssignLoading ? <Spinner size="tiny" /> : "确定"}
              </Button>
            </DialogActions>
          </DialogBody>
        </DialogSurface>
      </Dialog>

      {/* 确认对话框 */}
      <Dialog
        open={confirmDialog.open}
        onOpenChange={(_, data) => {
          if (!confirmLoading) {
            setConfirmDialog({ ...confirmDialog, open: data.open });
            if (!data.open) {
              setConfirmError("");
            }
          }
        }}>
        <DialogSurface>
          <DialogBody>
            <DialogTitle>{confirmDialog.title}</DialogTitle>
            <DialogContent>
              <Text>{confirmDialog.message}</Text>

              {/* 错误消息 */}
              {confirmError && (
                <MessageBar intent="error" style={{ marginTop: "16px" }}>
                  <MessageBarBody>{confirmError}</MessageBarBody>
                </MessageBar>
              )}
            </DialogContent>
            <DialogActions>
              <Button
                appearance="secondary"
                onClick={() => {
                  setConfirmDialog({
                    open: false,
                    type: "",
                    userId: "",
                    title: "",
                    message: "",
                  });
                  setConfirmError("");
                }}
                disabled={confirmLoading}>
                取消
              </Button>
              <Button appearance="primary" onClick={confirmAction} disabled={confirmLoading}>
                {confirmLoading ? <Spinner size="tiny" /> : "确定"}
              </Button>
            </DialogActions>
          </DialogBody>
        </DialogSurface>
      </Dialog>

      {/* 用户详情查看对话框 */}
      <Dialog
        open={viewDialog.open}
        onOpenChange={(_, data) => {
          setViewDialog({ ...viewDialog, open: data.open });
        }}>
        <DialogSurface>
          <DialogTitle>查看用户详情</DialogTitle>
          <DialogContent>
            {/* 加载状态 */}
            {viewDialog.loading && (
              <div className={styles.loadingContainer}>
                <Spinner size="large" label="加载中..." />
              </div>
            )}

            {/* 错误状态 */}
            {viewDialog.error && (
              <MessageBar intent="error" icon={<DismissRegular />} className={styles.errorMessage}>
                <MessageBarBody>{viewDialog.error}</MessageBarBody>
              </MessageBar>
            )}

            {/* 用户详情 */}
            {viewDialog.user && (
              <div style={{ display: "flex", flexDirection: "column", gap: tokens.spacingVerticalM }}>
                <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                  <Label style={{ fontWeight: "bold", width: 100 }}>用户ID:</Label>
                  <Body1>{viewDialog.user.userId}</Body1>
                </div>

                <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                  <Label style={{ fontWeight: "bold", width: 100 }}>用户名:</Label>
                  <Body1>{viewDialog.user.username}</Body1>
                </div>

                <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                  <Label style={{ fontWeight: "bold", width: 100 }}>昵称:</Label>
                  <Body1>{viewDialog.user.alias}</Body1>
                </div>

                <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                  <Label style={{ fontWeight: "bold", width: 100 }}>手机号:</Label>
                  <Body1>{viewDialog.user.phone}</Body1>
                </div>

                <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                  <Label style={{ fontWeight: "bold", width: 100 }}>邮箱:</Label>
                  <Body1>{viewDialog.user.email}</Body1>
                </div>

                <div style={{ display: "flex", gap: tokens.spacingHorizontalM }}>
                  <Label style={{ fontWeight: "bold", width: 100 }}>权限:</Label>
                  <Body1>{PermissionCodeToTextConverter(viewDialog.user.permission)}</Body1>
                </div>
              </div>
            )}
          </DialogContent>
          <DialogActions>
            <Button
              appearance="primary"
              onClick={() => {
                setViewDialog({ ...viewDialog, open: false });
              }}>
              关闭
            </Button>
          </DialogActions>
        </DialogSurface>
      </Dialog>
    </div>
  );
}
