import axios from "axios";
import { useAuthStore } from "../stores/auth-store";

// 创建实例（先不设置Authorization头）
const api = axios.create({
  baseURL: "http://localhost:8090",
  timeout: 5000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 通过拦截器动态添加token
api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.token = token;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default api;
