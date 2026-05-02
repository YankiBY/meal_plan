import axios from 'axios';

const primaryBaseUrl = '/api';
const fallbackBaseUrl = 'http://localhost:8080/api';

const api = axios.create({ baseURL: primaryBaseUrl });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const config = error.config as (typeof error.config & { __retriedWithFallback?: boolean }) | undefined;
    const canRetryWithFallback =
      !!config &&
      !config.__retriedWithFallback &&
      (!error.response || error.code === 'ERR_NETWORK') &&
      config.baseURL === primaryBaseUrl;

    if (canRetryWithFallback) {
      config.__retriedWithFallback = true;
      config.baseURL = fallbackBaseUrl;
      
      // Добавляем небольшую задержку перед повторной попыткой (500мс)
      await new Promise(resolve => setTimeout(resolve, 500));
      
      return api.request(config);
    }

    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
