export type IProfileData = {
  username: string;
  email: string | undefined;
  phone: string | undefined;
  alias: string; 
  avatarUrl: string | undefined; // 头像CDN地址
  id: string; 
  privilege: number; // 权限位掩码
};
