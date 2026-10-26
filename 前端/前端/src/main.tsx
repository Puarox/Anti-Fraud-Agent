import { FluentProvider } from "@fluentui/react-components";
import axios from "axios";
import { createRoot } from "react-dom/client";
import { RouterProvider } from "react-router";
import { ThemeProvider, useTheme } from "./components/ThemeContext";
import router from "./routers";
import "./styles/index.css";

export const baseURL = "http://localhost:8090";
axios.defaults.baseURL = baseURL;

const App = () => {
  const { theme } = useTheme();
  return (
    <FluentProvider theme={theme}>
      <RouterProvider router={router} />
    </FluentProvider>
  );
};

createRoot(document.getElementById("root")!).render(
  <ThemeProvider>
    <App />
  </ThemeProvider>
);
