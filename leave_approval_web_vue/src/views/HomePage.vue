<template>
  <div class="home-container">
    <h2>仪表盘</h2>
    <p v-if="username">
      欢迎回来, <strong>{{ username }}</strong>!
    </p>
    <p v-else>
      正在加载用户信息...
    </p>

    <p>这是您的个人主页，只有登录用户才能看到此内容。</p>

    <div class="actions">
      <button @click="fetchProfile" :disabled="isLoadingProfile" class="action-button primary">
        <span v-if="isLoadingProfile" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
        {{ isLoadingProfile ? ' 加载中...' : '获取用户资料 (受保护)' }}
      </button>
      <!-- Logout button is now in App.vue header, but could be here too -->
      <!-- <button @click="handleLogout" class="action-button danger">安全退出</button> -->
    </div>

    <!-- 显示获取到的用户资料 -->
    <div v-if="profileData" class="profile-display info-box">
      <h3>来自受保护 API 的数据:</h3>
      <pre>{{ JSON.stringify(profileData, null, 2) }}</pre> <!-- 格式化 JSON 输出 -->
    </div>

    <!-- 显示获取资料时的错误 -->
    <div v-if="profileError" class="profile-display error-box">
      <h3>获取资料时出错:</h3>
      <p>{{ profileError }}</p>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router'; // 如果需要登出功能在这里
import api from '../services/api'; // 引入 API 服务

const router = useRouter(); // 如果需要登出功能在这里

// --- 响应式状态 ---
const username = ref(localStorage.getItem('username')); // 从 localStorage 初始化用户名
const profileData = ref(null);      // 存储获取到的用户资料
const profileError = ref(null);     // 存储获取资料时的错误信息
const isLoadingProfile = ref(false); // 标记是否正在加载资料

// --- 方法 ---
/**
 * 调用 API 获取用户资料
 */
const fetchProfile = async () => {
  isLoadingProfile.value = true;
  profileError.value = null; // 清除旧错误
  profileData.value = null;  // 清除旧数据
  console.log('[HomePage] 请求 /api/auth/profile');

  try {
    const response = await api.getProfile();
    console.log('[HomePage] 获取 Profile 成功:', response.data);
    profileData.value = response.data; // 存储成功获取的数据
  } catch (error) {
    console.error('[HomePage] 获取 Profile 失败:', error);
    // 尝试从 error 对象中提取更具体的错误消息
    if (error.message === '用户未认证或会话已过期') {
         profileError.value = error.message; // 使用拦截器返回的特定消息
         // 拦截器已经处理了跳转，这里不需要额外操作
    } else if (error.response && error.response.data) {
      profileError.value = error.response.data.error || error.response.data.message || JSON.stringify(error.response.data);
    } else {
      profileError.value = error.message || '无法获取用户资料，请稍后再试。';
    }
  } finally {
    isLoadingProfile.value = false; // 结束加载状态
  }
};

// --- 生命周期钩子 ---
// 可以在组件挂载时自动获取一次用户资料 (如果需要)
// onMounted(() => {
//   fetchProfile();
// });

// 如果登出按钮放在这里
// const handleLogout = () => {
//   console.log('[HomePage] 执行登出');
//   localStorage.removeItem('authToken');
//   localStorage.removeItem('username');
//   router.push('/login');
// };
</script>

<style scoped>
.home-container {
  padding: 1.5rem;
  background-color: #ffffff;
  border-radius: var(--border-radius);
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
  border: 1px solid #dee2e6;
}

.home-container h2 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #343a40;
}

.home-container p {
  color: #495057;
  margin-bottom: 1rem;
}

.home-container strong {
    color: var(--primary-color);
}

.actions {
  margin-top: 1.5rem;
  margin-bottom: 1.5rem;
  display: flex;
  gap: 1rem;
}

.action-button {
  display: inline-flex; /* 让文字和 spinner 对齐 */
  align-items: center;
  justify-content: center;
  font-weight: 500;
  line-height: 1.5;
  text-align: center;
  text-decoration: none;
  vertical-align: middle;
  cursor: pointer;
  user-select: none;
  border: 1px solid transparent;
  padding: 0.6rem 1.2rem;
  font-size: 0.95rem;
  border-radius: var(--border-radius);
  transition: all .15s ease-in-out;
}
.action-button.primary {
    color: #fff;
    background-color: var(--primary-color);
    border-color: var(--primary-color);
}
.action-button.primary:hover:not(:disabled) {
    background-color: #0b5ed7;
    border-color: #0a58ca;
}
.action-button:disabled {
    opacity: 0.65;
    cursor: not-allowed;
}

/* 加载中的 spinner (需要 Bootstrap CSS 或自定义) */
.spinner-border-sm {
  width: 1rem;
  height: 1rem;
  border-width: 0.2em;
  /* --- 如果没有 Bootstrap CSS --- */
  display: inline-block;
  vertical-align: -0.125em;
  border: .2em solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spinner-border .75s linear infinite;
}
@keyframes spinner-border {
  to { transform: rotate(360deg); }
}
/* --- 结束 spinner 样式 --- */


.profile-display {
  margin-top: 2rem;
  padding: 1.5rem;
  border-radius: var(--border-radius);
  border: 1px solid #dee2e6;
  text-align: left;
}
.profile-display h3 {
    margin-top: 0;
    margin-bottom: 1rem;
    padding-bottom: 0.5rem;
    border-bottom: 1px solid #eee;
    font-size: 1.1em;
    color: #343a40;
}

.info-box {
    background-color: #e9ecef; /* 浅灰色背景 */
}
.error-box {
    background-color: #f8d7da;
    border-color: #f5c6cb;
}
.error-box h3 {
    color: var(--danger-color);
}
.error-box p {
     color: var(--danger-color);
}


pre {
  background-color: #f8f9fa; /* 比 info-box 更浅一点 */
  padding: 1rem;
  border-radius: 0.25rem;
  overflow-x: auto; /* 水平滚动条 */
  font-family: SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 0.875em; /* 稍小字体 */
  color: #333;
  white-space: pre-wrap; /* 自动换行 */
  word-wrap: break-word;
}
</style>