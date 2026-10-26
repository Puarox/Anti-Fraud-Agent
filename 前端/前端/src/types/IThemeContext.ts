import type { BrandVariants, Theme } from "@fluentui/react-components";
import type { IThemeMode } from "./IThemeMode";

export interface IThemeContext {
  theme: Theme;
  isDark: boolean;
  toggleTheme: () => void;
  setLightTheme: () => void;
  setDarkTheme: () => void;
  setSystemTheme: () => void;
  setTeamsLightTheme: () => void;
  setTeamsDarkTheme: () => void;
  setTeamsHighContrastTheme: () => void;
  setCustomTheme: (brandColors: BrandVariants, isDark?: boolean) => void;
  themeMode: IThemeMode;
}
