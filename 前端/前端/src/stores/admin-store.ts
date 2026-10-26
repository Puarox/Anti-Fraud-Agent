import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { IAdminState } from "../types/IAdminState";

interface AdminStore extends IAdminState {
  setMainTab: (tab: string) => void;
  setSubTab: (tab: string) => void;
  updateState: (updates: Partial<IAdminState>) => void;
}

const defaultAdminState: IAdminState = {
  selectedMainTab: "review",
  selectedSubTab: "pending",
};

export const useAdminStore = create<AdminStore>()(
  persist(
    (set) => ({
      ...defaultAdminState,
      setMainTab: (tab) => set({ selectedMainTab: tab }),
      setSubTab: (tab) => set({ selectedSubTab: tab }),
      updateState: (updates) => set((state) => ({ ...state, ...updates })),
    }),
    {
      name: "admin-storage",
    }
  )
);
