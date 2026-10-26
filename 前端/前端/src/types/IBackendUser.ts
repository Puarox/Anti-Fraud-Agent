// 后端用户数据接口
export type IBackendUser = {
  id: string;
  username: string;
  email: string;
  phone: string;
  alias: string;
  avatarUrl: string;
  status: "AVAILABLE" | "OTHER" | "OFFLINE" | "BREAK" | "POST_PROCESS" | "BUSY";
  online: boolean;
  privilege: number;
  isDeleted: number;
};
