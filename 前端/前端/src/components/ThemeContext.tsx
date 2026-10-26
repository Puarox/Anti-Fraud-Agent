import type { Theme } from "@fluentui/react-components";
import {
  createDarkTheme,
  createLightTheme,
  teamsDarkTheme,
  teamsHighContrastTheme,
  teamsLightTheme,
  webDarkTheme,
  webLightTheme,
} from "@fluentui/react-components";
import { createContext, useContext, type ReactNode } from "react";
import { useThemeStore } from "../stores/theme-store";
import type { IThemeContext } from "../types/IThemeContext";

interface IThemeProviderProps {
  children: ReactNode;
}

const ThemeContext = createContext<IThemeContext | undefined>(undefined);

export function useTheme() {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error("useTheme must be used within a ThemeProvider");
  }
  return context;
}

export function ThemeProvider({ children }: IThemeProviderProps) {
  const { themeMode, setThemeMode, customBrandColors, setCustomBrandColors, customThemeIsDark, setCustomThemeIsDark } =
    useThemeStore();

  function getSystemTheme(): "light" | "dark" {
    return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
  }

  function getCurrentTheme(): Theme {
    switch (themeMode) {
      case "light":
        return webLightTheme;
      case "dark":
        return webDarkTheme;
      case "teamsLight":
        return teamsLightTheme;
      case "teamsDark":
        return teamsDarkTheme;
      case "teamsHighContrast":
        return teamsHighContrastTheme;
      case "customLight":
        if (customBrandColors) {
          return createLightTheme(customBrandColors);
        }
        return webLightTheme;
      case "customDark":
        if (customBrandColors) {
          return createDarkTheme(customBrandColors);
        }
        return webDarkTheme;
      case "custom":
        if (customBrandColors) {
          return customThemeIsDark ? createDarkTheme(customBrandColors) : createLightTheme(customBrandColors);
        }
        return webLightTheme;
      case "system":
      default:
        return getSystemTheme() === "dark" ? webDarkTheme : webLightTheme;
    }
  }

  const isDark =
    themeMode === "dark" ||
    themeMode === "teamsDark" ||
    themeMode === "teamsHighContrast" ||
    themeMode === "customDark" ||
    (themeMode === "system" && getSystemTheme() === "dark") ||
    (themeMode === "custom" && customThemeIsDark);

  function toggleTheme() {
    const newMode = themeMode === "light" ? "dark" : "light";
    setThemeMode(newMode);
  }

  function setLightTheme() {
    setThemeMode("light");
  }

  function setDarkTheme() {
    setThemeMode("dark");
  }

  function setSystemTheme() {
    setThemeMode("system");
  }

  function setTeamsLightTheme() {
    setThemeMode("teamsLight");
  }

  function setTeamsDarkTheme() {
    setThemeMode("teamsDark");
  }

  function setTeamsHighContrastTheme() {
    setThemeMode("teamsHighContrast");
  }

  function setCustomTheme(brandColors: any, isDark?: boolean) {
    setCustomBrandColors(brandColors);
    if (isDark !== undefined) {
      setCustomThemeIsDark(isDark);
      setThemeMode(isDark ? "customDark" : "customLight");
    } else {
      setThemeMode("custom");
    }
  }

  return (
    <ThemeContext.Provider
      value={{
        theme: getCurrentTheme(),
        isDark,
        toggleTheme,
        setLightTheme,
        setDarkTheme,
        setSystemTheme,
        setTeamsLightTheme,
        setTeamsDarkTheme,
        setTeamsHighContrastTheme,
        setCustomTheme,
        themeMode,
      }}>
      {children}
    </ThemeContext.Provider>
  );
}
