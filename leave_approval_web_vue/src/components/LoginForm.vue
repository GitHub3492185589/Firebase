<template>
  <form @submit.prevent="handleLoginSubmit" class="auth-form" novalidate>
    <!-- 用户名输入 -->
    <div class="form-group">
      <label for="login-username">用户名:</label>
      <input
        type="text"
        id="login-username"
        v-model.trim="credentials.username"
        :disabled="isLoading"
        required
        autocomplete="username"
        placeholder="请输入用户名"
        aria-describedby="username-error"
      />
       <small v-if="errors.username" id="username-error" class="field-error">{{ errors.username }}</small>
    </div>

    <!-- 密码输入 -->
    <div class="form-group">
      <label for="login-password">密码:</label>
      <input
        type="password"
        id="login-password"
        v-model="credentials.password"
        :disabled="isLoading"
        required
        autocomplete="current-password"
        placeholder="请输入密码"
         aria-describedby="password-error"
      />
       <small v-if="errors.password" id="password-error" class="field-error">{{ errors.password }}</small>
    </div>

    <!-- 显示 API 返回的错误信息 -->
    <div v-if="apiErrorMessage" class="error-message api-error">
      {{ apiErrorMessage }}
    </div>

    <!-- 登录按钮 -->
    <button type="submit" :disabled="isLoading || !isFormValid" class="submit-button">
       <span v-if="isLoading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
      {{ isLoading ? ' 登录中...' : '登 录' }}
    </button>
  </form>
</template>

<script setup>
import { ref, reactive, computed, defineEmits } from 'vue';
import api from '../services/api'; // 引入 API 服务

// --- 定义事件 ---
const emit = defineEmits(['login-success', 'login-failed']);

// --- 响应式状态 ---
const credentials = reactive({
  username: '',
  password: '',
});
const isLoading = ref(false);         // 加载状态
const apiErrorMessage = ref('');      // API 错误信息
const errors = reactive({});          // 字段校验错误

// --- 计算属性 ---
// 简单判断表单是否有效 (用于禁用按钮)
const isFormValid = computed(() => {
  return credentials.username.length > 0 && credentials.password.length > 0;
});

// --- 方法 ---
/**
 * 验证表单字段
 * @returns {boolean} 是否验证通过
 */
const validateForm = () => {
    // 清空旧错误
    Object.keys(errors).forEach(key => delete errors[key]);
    apiErrorMessage.value = ''; // 清空 API 错误
    let isValid = true;
    if (!credentials.username) {
        errors.username = '用户名不能为空';
        isValid = false;
    }
    if (!credentials.password) {
        errors.password = '密码不能为空';
        isValid = false;
    }
    return isValid;
};

/**
 * 处理登录表单提交
 */
const handleLoginSubmit = async () => {
  if (!validateForm()) {
      console.warn('[LoginForm] 客户端表单校验失败');
      return; // 校验失败则不提交
  }

  isLoading.value = true; // 开始加载
  console.log('[LoginForm] 准备调用登录 API，用户:', credentials.username);

  try {
    // 调用 API 登录接口
    const response = await api.login({
      username: credentials.username,
      password: credentials.password,
    });

    console.log('[LoginForm] 登录 API 成功响应:', response.data);
    const { token, username } = response.data; // 假设后端返回 token 和 username

    // 登录成功: 存储 token 和 username
    localStorage.setItem('authToken', token);
    localStorage.setItem('username', username);
    console.log('[LoginForm] Token 和用户名已存储');

    // 触发成功事件，通知父组件
    emit('login-success');

  } catch (error) {
    // 登录失败
    console.error('[LoginForm] 登录 API 调用失败:', error);
    let message = '登录时发生未知错误。'; // 默认错误
    if (error.response && error.response.data) {
        // 优先使用后端返回的错误信息
        message = error.response.data.error || error.response.data.message || JSON.stringify(error.response.data);
    } else if (error.message) {
        // 其次使用 Axios 错误对象的 message (可能包含网络错误等)
        message = error.message;
    }
    apiErrorMessage.value = message; // 在表单中显示 API 错误
    // 触发失败事件，并将错误信息传递给父组件
    emit('login-failed', message);

  } finally {
    // 无论成功或失败，结束加载状态
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* 共享样式通常来自 App.vue 或全局 CSS */

/* 字段下方错误提示 */
.field-error {
    color: var(--danger-color);
    font-size: 0.8em;
    margin-top: 0.25rem;
    display: block;
}

/* API 错误消息特定样式 */
.api-error {
    margin-bottom: 1rem; /* 与按钮保持距离 */
    margin-top: 0; /* 紧跟输入框下方 */
}

/* 按钮内的 Spinner */
.spinner-border-sm {
  width: 1em; /* 调整大小 */
  height: 1em;
  border-width: 0.15em;
  /* --- 如果没有 Bootstrap CSS --- */
  display: inline-block;
  vertical-align: -0.125em;
  border: .15em solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spinner-border .75s linear infinite;
  margin-right: 0.5em; /* 与文字间距 */
}
@keyframes spinner-border { to { transform: rotate(360deg); } }
/* --- 结束 spinner 样式 --- */
</style>