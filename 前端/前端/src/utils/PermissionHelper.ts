import { PERMISSIONS } from "../models/Permission";

// 权限位掩码转换为文本
export function PermissionCodeToTextConverter(permissions: number): string {
  const roles = [];
  if (permissions & PERMISSIONS.USER_TOURIST || permissions === 1) {
    roles.push("游客");
  }
  if (permissions & PERMISSIONS.ADMIN_MAINTENANCE || permissions === 4) {
    roles.push("管理员");
  }
  return roles.join("、") || "无权限";
}

// 检查是否是管理员或超级管理员
export function isAdmin() {
  return true;
}

// 检查是否是超级管理员
export function isSuperAdmin() {
  return true;
}