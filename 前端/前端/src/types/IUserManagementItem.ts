export type IUserManagementItem = {
  alias: string;
  username: string;
  phone: string;
  permission: number;
  email: string;
  id: string;
  status: "AVAILABLE" | "OTHER" | "OFFLINE" | "BREAK" | "POST_PROCESS" | "BUSY";
};
