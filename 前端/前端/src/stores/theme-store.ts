import type { BrandVariants } from "@fluentui/react-components";
import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { IThemeMode } from "../types/IThemeMode";

interface ThemeState {
  themeMode: IThemeMode;
  customBrandColors: BrandVariants | null;
  customThemeIsDark: boolean;
  setThemeMode: (mode: IThemeMode) => void;
  setCustomBrandColors: (colors: BrandVariants) => void;
  setCustomThemeIsDark: (isDark: boolean) => void;
}

export const useThemeStore = create<ThemeState>()(
  persist(
    (set) => ({
      themeMode: (localStorage.getItem("themeMode") as IThemeMode) || "system",
      customBrandColors: null,
      customThemeIsDark: false,
      setThemeMode: (mode) => {
        set({ themeMode: mode });
        localStorage.setItem("themeMode", mode);
      },
      setCustomBrandColors: (colors) => {
        set({ customBrandColors: colors });
      },
      setCustomThemeIsDark: (isDark) => {
        set({ customThemeIsDark: isDark });
      },
    }),
    {
      name: "theme-storage",
      partialize: (state) => ({
        themeMode: state.themeMode,
        customBrandColors: state.customBrandColors,
        customThemeIsDark: state.customThemeIsDark,
      }),
    }
  )
);
