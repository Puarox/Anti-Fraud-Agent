import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { IProfileData } from "../types/IProfileData";
import api from "../utils/Api";

interface AuthState {
  token: string | null;
  profileData: IProfileData | null;
  setToken: (token: string | null) => void;
  setProfileData: (data: IProfileData | null) => void;
  logout: () => void;
  initialize: () => Promise<boolean>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: localStorage.getItem("token"),
      profileData: localStorage.getItem("profileData") ? JSON.parse(localStorage.getItem("profileData")!) : null,

      setToken: (token) => {
        set({ token });
        if (token) {
          localStorage.setItem("token", token);
        } else {
          localStorage.removeItem("token");
        }
      },

      setProfileData: (profileData) => {
        set({ profileData });
        if (profileData) {
          localStorage.setItem("profileData", JSON.stringify(profileData));
        } else {
          localStorage.removeItem("profileData");
        }
      },

      logout: () => {
        set({ token: null, profileData: null });
        localStorage.removeItem("token");
        localStorage.removeItem("profileData");
      },

      initialize: async () => {
        const { token, setProfileData } = get();
        console.log("Initializing auth store with token:", token);

        if (!token) {
          const legacyToken = localStorage.getItem("token");
          if (!legacyToken) {
            console.error("未登录或令牌已过期");
            return false;
          }
          set({ token: legacyToken });
        }

        try {
          const res = await api.get("/user/getUserDetail");
          if (res.data?.success && res.data?.data) {
            setProfileData(res.data.data);
            return true;
          }
        } catch (error) {
          console.error("获取用户详情失败", error);
        }
        return false;
      },
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({ token: state.token, profileData: state.profileData }),
    }
  )
);
