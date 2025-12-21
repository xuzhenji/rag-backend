import axios from "axios";

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE, // 后端地址
  timeout: 30000
});

// 请求拦截器（可选）
http.interceptors.request.use((config) => {
  return config;
});

// 响应拦截器（可选）
http.interceptors.response.use(
  (res) => res, // 自动返回 data
  (err) => {
    console.error("API Error:", err);
    return Promise.reject(err);
  }
);

export default http;
