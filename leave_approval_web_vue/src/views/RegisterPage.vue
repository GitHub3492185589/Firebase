<template>
  <div class="auth-page">
    <div class="auth-container register-container"> <!-- 可以给注册容器一个不同的类名或样式 -->
      <h2>创建您的账号</h2>

      <!-- 注册表单组件 -->
      <RegisterForm
        @register-success="onRegisterSuccess"
        @register-failed="onRegisterFailed"
      />

      <!-- 切换到登录页面的链接 -->
      <p class="auth-switch">
        已经拥有账号? <router-link :to="{ name: 'Login' }">立即登录</router-link>
      </p>

       <!-- 显示页面级别的错误消息 -->
      <div v-if="pageError" class="message-box error">
         {{ pageError }}
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import RegisterForm from '../components/RegisterForm.vue'; // 引入注册表单

const router = useRouter();
const pageError = ref(''); // 存储页面级别的错误信息

/**
 * RegisterForm 组件触发 register-success 事件时的回调
 */
const onRegisterSuccess = () => {
  console.log('[RegisterPage] 注册成功');
  pageError.value = ''; // 清空错误
  // 跳转到登录页面，并带上一个提示参数
  router.push({ name: 'Login', query: { registered: 'true' } });
};

/**
 * RegisterForm 组件触发 register-failed 事件时的回调
 * @param {string} errorMessage - 从 RegisterForm 传递的错误信息
 */
const onRegisterFailed = (errorMessage) => {
  console.error('[RegisterPage] 注册失败:', errorMessage);
  pageError.value = errorMessage || '注册过程中遇到问题，请稍后重试。';
};

</script>

<style scoped>
.register-container {
  max-width: 550px; /* 注册页面可以比登录页宽一些 */
}
/* 消息框样式已在 App.vue 或 LoginPage.vue 中定义 (或应提取到全局) */
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
</style>