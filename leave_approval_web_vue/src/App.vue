<template>
  <div id="app-container">
    <!-- 页头 -->
    <header class="app-header">
      <div class="logo-container">
        <router-link to="/" class="logo-link">MyApp</router-link> <!-- 替换为你的应用名称/Logo -->
      </div>
      <nav class="app-nav">
        <!-- 根据登录状态动态显示导航项 -->
        <router-link v-if="isLoggedIn" to="/home" class="nav-link">主页</router-link>
        <!-- <router-link v-if="isLoggedIn" to="/settings" class="nav-link">设置</router-link> -->
        <router-link v-if="!isLoggedIn" to="/login" class="nav-link">登录</router-link>
        <router-link v-if="!isLoggedIn" to="/register" class="nav-link">注册</router-link>
        <a v-if="isLoggedIn" href="#" @click.prevent="handleLogout" class="nav-link logout-link">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-box-arrow-right logout-icon" viewBox="0 0 16 16">
            <path fill-rule="evenodd" d="M10 12.5a.5.5 0 0 1-.5.5h-8a.5.5 0 0 1-.5-.5v-9a.5.5 0 0 1 .5-.5h8a.5.5 0 0 1 .5.5v2.5a.5.5 0 0 0 1 0v-2.5a1.5 1.5 0 0 0-1.5-1.5h-8A1.5 1.5 0 0 0 0 4v9a1.5 1.5 0 0 0 1.5 1.5h8a1.5 1.5 0 0 0 1.5-1.5v-2.5a.5.5 0 0 0-1 0z"/>
            <path fill-rule="evenodd" d="M15.854 8.354a.5.5 0 0 0 0-.708l-3-3a.5.5 0 0 0-.708.708L14.293 7.5H5.5a.5.5 0 0 0 0 1h8.793l-2.147 2.146a.5.5 0 0 0 .708.708l3-3z"/>
          </svg>
          退出
        </a>
        <span class="user-info" v-if="isLoggedIn && currentUsername">
          欢迎, {{ currentUsername }}
        </span>
      </nav>
    </header>

    <!-- 主要内容区域，路由视图将在这里渲染 -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer class="app-footer">
      <p>© {{ new Date().getFullYear() }} 我的应用. 保留所有权利.</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

// --- 响应式状态 ---
const isLoggedIn = ref(false);         // 用户是否登录
const currentUsername = ref(null);    // 当前登录的用户名

// --- 方法 ---
/**
 * 更新组件的认证状态 (从 localStorage 读取)
 */
const updateAuthState = () => {
  const token = localStorage.getItem('authToken');
  isLoggedIn.value = !!token; // !! 将值转为布尔型
  currentUsername.value = localStorage.getItem('username');
  console.debug(`[App.vue] updateAuthState: isLoggedIn=${isLoggedIn.value}, username=${currentUsername.value}`);
};

/**
 * 处理用户登出操作
 */
const handleLogout = () => {
  console.log('[App.vue] 用户请求登出');
  // 1. 清除本地存储
  localStorage.removeItem('authToken');
  localStorage.removeItem('username');
  // 2. 更新组件状态 (导航栏会立即变化)
  updateAuthState();
  // 3. 跳转到登录页
  router.push({ name: 'Login', query: { loggedOut: 'true' } }) // 添加登出提示参数
       .catch(err => {
           if (err.name !== 'NavigationDuplicated') console.error('Logout navigation error:', err);
       });
};

// --- 生命周期钩子和监听器 ---

/**
 * 监听 localStorage 的变化事件 (用于跨标签页同步状态)
 */
const handleStorageChange = (event) => {
  // 仅关心 'authToken' 或 'username' 的变化
  if (event.key === 'authToken' || event.key === 'username') {
    console.log(`[App.vue] 检测到 localStorage '${event.key}' 发生变化`);
    const wasLoggedIn = isLoggedIn.value; // 记录变化前的状态
    updateAuthState(); // 更新当前标签页的状态

    // 如果是从 "已登录" 变为 "未登录" (例如在其他标签页登出)
    // 并且当前页面是需要认证的页面，则强制跳转到登录页
    if (wasLoggedIn && !isLoggedIn.value && route.meta.requiresAuth) {
      console.warn('[App.vue] Token 在别处被移除，且当前页面需认证，强制跳转到登录页');
      router.push({ name: 'Login', query: { sessionExpired: 'external' } }).catch(()=>{});
    }
  }
};

// 组件挂载时: 初始化状态，并添加 storage 事件监听器
onMounted(() => {
  console.log('[App.vue] Component Mounted - 初始化认证状态并监听 storage 事件');
  updateAuthState(); // 初始化时就从 localStorage 读取一次状态
  window.addEventListener('storage', handleStorageChange);
});

// 组件卸载时: 移除 storage 事件监听器，防止内存泄漏
onUnmounted(() => {
  console.log('[App.vue] Component Unmounted - 移除 storage 事件监听');
  window.removeEventListener('storage', handleStorageChange);
});

// 监听路由变化: 确保即使没有 storage 事件，导航栏状态也能在需要时更新
// (例如，编程式导航后立即需要更新 UI)
watch(
  () => route.name, // 监听路由名称变化 (比监听 path 更能反映页面切换)
  (newName, oldName) => {
    if (newName !== oldName) {
        // console.debug(`[App.vue] 路由切换: ${oldName} -> ${newName}, 更新认证状态`);
        updateAuthState();
    }
  }
);

</script>

<style>
/* 基本的全局样式和布局 */
:root {
  --primary-color: #007bff;
  --secondary-color: #6c757d;
  --success-color: #198754;
  --danger-color: #dc3545;
  --warning-color: #ffc107;
  --light-color: #f8f9fa;
  --dark-color: #212529;
  --font-family-sans-serif: system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", "Noto Sans", "Liberation Sans", Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
  --border-radius: 0.375rem; /* 6px */
}

body {
  margin: 0;
  font-family: var(--font-family-sans-serif);
  font-size: 1rem;
  font-weight: 400;
  line-height: 1.5;
  color: var(--dark-color);
  background-color: #f4f7f9; /* 更淡的背景 */
  -webkit-text-size-adjust: 100%;
  -webkit-tap-highlight-color: rgba(0, 0, 0, 0);
}

#app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* 页头样式 */
.app-header {
  background-color: #ffffff;
  padding: 0.8rem 1.5rem; /* 调整内边距 */
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border-bottom: 1px solid #dee2e6;
  position: sticky;
  top: 0;
  z-index: 1020; /* 确保在顶层 */
}

.logo-container .logo-link {
  font-size: 1.5em;
  font-weight: 700;
  color: var(--primary-color);
  text-decoration: none;
}

/* 导航链接样式 */
.app-nav {
  display: flex;
  align-items: center;
  gap: 1.2rem; /* 增加链接间距 */
}

.nav-link {
  text-decoration: none;
  color: #495057;
  font-weight: 500;
  padding: 0.3rem 0;
  transition: color 0.2s ease-in-out;
  position: relative;
  display: flex; /* 让图标和文字对齐 */
  align-items: center;
  gap: 0.3rem; /* 图标和文字间距 */
}

.nav-link:hover,
.nav-link.router-link-active, /* 普通激活状态 */
.nav-link.router-link-exact-active /* 精确激活状态 */
{
  color: var(--primary-color);
}

/* 给激活链接添加下划线 */
.nav-link.router-link-exact-active::after {
    content: '';
    position: absolute;
    bottom: -2px; /* 稍微向下一点 */
    left: 0;
    width: 100%;
    height: 2px;
    background-color: var(--primary-color);
}


.logout-link {
    cursor: pointer;
}
.logout-link:hover {
    color: var(--danger-color); /* 退出链接悬停变红 */
}
.logout-icon {
    margin-bottom: -2px; /* 微调图标垂直位置 */
}


.user-info {
  color: var(--secondary-color);
  font-size: 0.9em;
  margin-left: 0.8rem;
  padding-left: 0.8rem;
  border-left: 1px solid #dee2e6;
}

/* 主内容区域 */
.app-main {
  flex-grow: 1; /* 让主内容区占据所有可用垂直空间 */
  padding: 1.5rem; /* 内边距 */
  max-width: 1200px; /* 可选：限制最大宽度 */
  margin: 0 auto; /* 水平居中 */
  width: 100%;
  box-sizing: border-box;
}

/* 页脚 */
.app-footer {
  background-color: #e9ecef; /* 浅灰色页脚 */
  color: #6c757d;
  padding: 1rem 1.5rem;
  text-align: center;
  font-size: 0.85em;
  margin-top: auto; /* 确保页脚在内容不足时也在底部 */
  border-top: 1px solid #dee2e6;
}
.app-footer p {
  margin: 0;
}

/* 页面切换过渡效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 通用表单样式 (可以放在 components 或 assets/main.css) */
.auth-page {
  display: flex;
  justify-content: center;
  align-items: flex-start; /* 顶部对齐，允许表单自然高度 */
  padding: 40px 20px; /* 页面内边距 */
}

.auth-container {
  width: 100%;
  max-width: 420px; /* 登录/注册框最大宽度 */
  padding: 2rem; /* 内边距加大 */
  background-color: #ffffff;
  border: 1px solid #dee2e6;
  border-radius: var(--border-radius);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.05); /* 柔和阴影 */
  text-align: center;
}
.auth-container h2 {
  margin-top: 0;
  margin-bottom: 1.5rem;
  color: #343a40;
  font-weight: 600;
}

.auth-form .form-group {
  margin-bottom: 1rem;
  text-align: left;
}

.auth-form label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #495057;
  font-size: 0.9rem;
}

.auth-form input[type="text"],
.auth-form input[type="password"],
.auth-form input[type="email"],
.auth-form input[type="date"],
.auth-form input[type="tel"],
.auth-form input[type="url"],
.auth-form textarea {
  display: block;
  width: 100%;
  padding: 0.75rem 1rem; /* 舒适的输入框内边距 */
  font-size: 1rem;
  font-weight: 400;
  line-height: 1.5;
  color: #212529;
  background-color: #fff;
  background-clip: padding-box;
  border: 1px solid #ced4da;
  appearance: none; /* 去除默认外观 */
  border-radius: var(--border-radius);
  transition: border-color .15s ease-in-out,box-shadow .15s ease-in-out;
  box-sizing: border-box; /* 确保 padding 不会撑大元素 */
}
.auth-form input::placeholder,
.auth-form textarea::placeholder {
    color: #6c757d;
    opacity: 1;
}
.auth-form input:focus,
.auth-form textarea:focus {
  color: #212529;
  background-color: #fff;
  border-color: #86b7fe;
  outline: 0;
  box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
}
.auth-form input:disabled,
.auth-form textarea:disabled {
    background-color: #e9ecef;
    opacity: 1;
}
.auth-form textarea {
    min-height: 80px;
    resize: vertical;
}


/* 按钮 */
.submit-button {
  display: inline-block;
  font-weight: 600;
  line-height: 1.5;
  color: #fff;
  text-align: center;
  text-decoration: none;
  vertical-align: middle;
  cursor: pointer;
  user-select: none;
  background-color: var(--primary-color);
  border: 1px solid var(--primary-color);
  padding: 0.75rem 1.25rem; /* 按钮内边距 */
  font-size: 1rem;
  border-radius: var(--border-radius);
  transition: color .15s ease-in-out,background-color .15s ease-in-out,border-color .15s ease-in-out,box-shadow .15s ease-in-out;
  width: 100%; /* 按钮宽度充满容器 */
  margin-top: 1rem;
}
.submit-button:hover:not(:disabled) {
  background-color: #0b5ed7;
  border-color: #0a58ca;
}
.submit-button:disabled {
  background-color: var(--secondary-color);
  border-color: var(--secondary-color);
  opacity: 0.65;
  cursor: not-allowed;
}

/* 错误消息 */
.error-message {
  color: var(--danger-color);
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: var(--border-radius);
  padding: 0.8rem 1rem;
  font-size: 0.9em;
  margin-top: 1rem;
  margin-bottom: 1rem;
  text-align: center;
}

/* 注册/登录切换链接 */
.auth-switch {
  margin-top: 1.5rem;
  font-size: 0.9em;
  color: #6c757d;
}
.auth-switch a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
}
.auth-switch a:hover {
  text-decoration: underline;
}

</style>