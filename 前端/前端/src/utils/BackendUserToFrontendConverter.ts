import type { IBackendUser } from "../types/IBackendUser";
import type { IUserManagementItem } from "../types/IUserManagementItem";
import type { IUserReviewItem } from "../types/IUserReviewItem";

// 转换后端数据为前端数据
export function BackendUserToUserReviewItemConverter(user: IBackendUser): IUserReviewItem {
  return {
    userId: user.id,
    username: user.username,
    alias: user.alias,
    phone: user.phone,
    email: user.email,
    permission: user.privilege,
    privilege: user.privilege,
    status: user.status ,
  };
}

export function BackendUserToUserManagementItemConverter(user: IBackendUser): IUserManagementItem {
  return {
    alias: user.alias,
    username: user.username,
    phone: user.phone,
    email: user.email,
    permission: user.privilege,
    id: user.id,
    status: user.status,
  };
}
