import axios from 'axios';
import router from '../router'; // 引入 router 实例，用于在拦截器中跳转

// 创建 Axios 实例
const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api', // 确认这是你 Spring Boot 后端的正确 API 基础路径
    timeout: 10000, // 设置请求超时时间 (10 秒)
    headers: {
        'Content-Type': 'application/json', // 默认发送 JSON 格式数据
        'Accept': 'application/json',       // 期望接收 JSON 格式响应
    },
});

// --- 请求拦截器 ---
// 在每个请求发送出去之前执行
apiClient.interceptors.request.use(
    (config) => {
        // 从 localStorage 获取身份验证令牌
        const token = localStorage.getItem('authToken');
        if (token) {
            // 如果令牌存在，将其添加到请求头的 Authorization 字段
            config.headers['Authorization'] = `Bearer ${token}`;
            // console.debug('[API Request] 添加 Authorization Header');
        } else {
            // console.debug('[API Request] 未找到 Token，不添加 Header');
        }
        return config; // 返回修改后的请求配置
    },
    (error) => {
        // 处理请求配置错误
        console.error('API 请求配置错误:', error);
        return Promise.reject(error); // 将错误继续传递下去
    }
);

// --- 响应拦截器 ---
// 在接收到响应之后，但在 then/catch 处理之前执行
apiClient.interceptors.response.use(
    (response) => {
        // 如果响应成功 (状态码 2xx)，直接返回响应对象
        // console.debug(`[API Response ${response.status}] from ${response.config.url}`);
        return response;
    },
    (error) => {
        // 处理响应错误
        const { response, request, message } = error; // 解构错误对象

        if (response) {
            // 服务器返回了响应，但状态码不是 2xx
            console.error(`[API Response Error ${response.status}] from ${request?.responseURL || response.config.url}:`, response.data);

            if (response.status === 401) {
                // --- **处理 401 未授权错误 (关键)** ---
                console.warn('收到 401 未授权响应，Token 可能无效或已过期。执行登出操作...');

                // 1. 清除本地存储的认证信息
                localStorage.removeItem('authToken');
                localStorage.removeItem('username');

                // 2. 重定向到登录页面
                //    - 检查当前是否已在登录页，避免无限循环重定向
                //    - 添加查询参数 `sessionExpired=true` 以便登录页可以显示提示信息
                if (router.currentRoute.value.name !== 'Login') {
                    router.push({
                        name: 'Login',
                        query: { sessionExpired: 'true', redirect: router.currentRoute.value.fullPath } // 保存原始路径
                    }).catch(err => {
                        // 捕获可能的导航错误 (例如，如果用户快速连续触发多个 401)
                        if (err.name !== 'NavigationDuplicated' && err.message.includes('cancelled')) {
                             console.warn('导航到登录页被取消或重复:', err.message);
                        } else if(err.name !== 'NavigationDuplicated'){
                            console.error('导航到登录页时发生错误:', err);
                        }
                    });
                }
                // 返回一个已被处理的 Promise reject，告知调用方认证失败
                // 避免调用方代码继续尝试处理 401 (例如显示通用错误消息)
                return Promise.reject(new Error('用户未认证或会话已过期'));
            }
            // 对于其他错误 (400, 403, 404, 500 等)，让调用 API 的组件去处理
            // 通常可以通过 error.response.data 获取后端返回的具体错误信息
            // 例如: error.response.data.error 或 error.response.data.message

        } else if (request) {
            // 请求已发出，但没有收到响应 (例如网络中断、后端服务崩溃)
            console.error('API 请求无响应:', message);
            // 返回一个更友好的网络错误提示
             return Promise.reject(new Error('无法连接到服务器，请检查网络连接或稍后再试。'));
        } else {
            // 在设置请求时触发了一个错误 (代码错误)
            console.error('API 请求设置错误:', message);
        }

        // 将原始错误继续抛出，以便调用方可以捕获并显示错误信息
        return Promise.reject(error);
    }
);

// --- 定义具体的 API 调用函数 ---
export default {
    /**
     * 发送用户注册请求
     * @param {object} userData 包含注册信息的对象 (e.g., { username, password, email, ... })
     * @returns {Promise<import('axios').AxiosResponse<any>>} Axios 响应对象
     */
    register(userData) {
        console.log('[API Call] POST /auth/register', userData);
        return apiClient.post('/auth/register', userData);
    },

    /**
     * 发送用户登录请求
     * @param {object} credentials 包含用户名和密码的对象 (e.g., { username, password })
     * @returns {Promise<import('axios').AxiosResponse<{token: string, username: string, tokenType: string}>>} 成功时 data 包含 token 等信息
     */
    login(credentials) {
        console.log('[API Call] POST /auth/login', credentials);
        return apiClient.post('/auth/login', credentials);
    },

    /**
     * 请求获取用户资料 (这是一个受保护的接口)
     * @returns {Promise<import('axios').AxiosResponse<any>>} Axios 响应对象
     */
    getProfile() {
        console.log('[API Call] GET /auth/profile');
        return apiClient.get('/auth/profile');
    },

    // --- 未来可以添加更多 API 函数 ---
    // updateUserProfile(profileData) {
    //     return apiClient.put('/user/profile', profileData);
    // }
};