<template>
  <div class="auth-page">
    <div class="auth-container">
      <h2>登录</h2>

      <!-- 登录表单组件 -->
      <LoginForm
        @login-success="onLoginSuccess"
        @login-failed="onLoginFailed"
      />

      <!-- 切换到注册页面的链接 -->
      <p class="auth-switch">
        还没有账号? <router-link :to="{ name: 'Register' }">立即注册</router-link>
      </p>

      <!-- 显示从父组件传递或路由参数带来的错误/提示信息 -->
      <div v-if="pageMessage" :class="['message-box', messageType]">
        {{ pageMessage }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import LoginForm from '../components/LoginForm.vue'; // 引入登录表单组件

const router = useRouter(); // 用于导航
const route = useRoute();   // 用于读取路由查询参数

// --- 响应式状态 ---
const pageMessage = ref(''); // 用于显示页面级别的提示或错误信息
const messageType = ref('error'); // 消息类型 ('error', 'success', 'info')

// --- 方法 ---
/**
 * LoginForm 组件触发 login-success 事件时的回调
 */
const onLoginSuccess = () => {
  console.log('[LoginPage] 登录成功');
  pageMessage.value = ''; // 清除错误信息
  // 检查路由查询参数中是否有 'redirect'，决定登录后跳转到哪里
  const redirectPath = route.query.redirect || '/home'; // 默认跳转到主页
  console.log(`[LoginPage] 准备跳转到: ${redirectPath}`);
  router.replace(redirectPath); // 使用 replace 避免登录页出现在历史记录中
};

/**
 * LoginForm 组件触发 login-failed 事件时的回调
 * @param {string} errorMessage - 从 LoginForm 传递过来的错误信息
 */
const onLoginFailed = (errorMessage) => {
  console.error('[LoginPage] 登录失败:', errorMessage);
  pageMessage.value = errorMessage || '登录失败，请检查您的凭证或网络连接。';
  messageType.value = 'error'; // 设置消息类型为错误
};

// --- 生命周期钩子 ---
// 组件挂载后检查路由查询参数，看是否需要显示特定提示
onMounted(() => {
  if (route.query.sessionExpired) {
    pageMessage.value = '您的登录会话已过期或无效，请重新登录。';
    messageType.value = 'warning'; // 或 'info'
  } else if (route.query.loggedOut) {
    pageMessage.value = '您已成功退出登录。';
    messageType.value = 'success';
  } else if (route.query.registered) {
    pageMessage.value = '注册成功！请使用您的新账号登录。';
    messageType.value = 'success';
  }
  // 清除查询参数，避免刷新页面后再次显示提示
  if (Object.keys(route.query).length > 0) {
      router.replace({ query: {} }); // 替换当前路由，清空 query
  }

});
</script>

<style scoped>
/* 页面特定样式可以放在这里 */
/* 例如消息框的样式 */
.message-box {
  border-radius: var(--border-radius);
  padding: 0.8rem 1rem;
  font-size: 0.9em;
  margin-top: 1rem;
  text-align: center;
  border: 1px solid transparent;
}
.message-box.error {
  color: var(--danger-color);
  background-color: #f8d7da;
  border-color: #f5c6cb;
}
.message-box.success {
  color: var(--success-color);
  background-color: #d1e7dd;
  border-color: #badbcc;
}
.message-box.warning {
    color: #664d03;
    background-color: #fff3cd;
    border-color: #ffecb5;
}
.message-box.info {
    color: #055160;
    background-color: #cff4fc;
    border-color: #b6effb;
}
</style>