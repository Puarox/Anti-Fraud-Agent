export type IUserReviewItem = {
  userId: string;
  username: string;
  alias: string;
  phone?: string;
  email: string;
  status?: "OFFLINE" | "AVAILABLE" | "BREAK" | "BUSY" | "POST_PROCESS" | "OTHER";
  permission: number;
  privilege: number;
};
