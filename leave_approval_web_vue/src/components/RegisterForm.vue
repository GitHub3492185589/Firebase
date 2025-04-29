<template>
  <form @submit.prevent="handleRegisterSubmit" class="auth-form register-form" novalidate>
    <!-- 用户名 (必填) -->
    <div class="form-group">
      <label for="reg-username">用户名:*</label>
      <input type="text" id="reg-username" v-model.trim="formData.username" :disabled="isLoading" required minlength="3" maxlength="50" placeholder="3-50 个字符" aria-describedby="username-error">
      <small v-if="errors.username" id="username-error" class="field-error">{{ errors.username }}</small>
    </div>

    <!-- 密码 (必填) -->
    <div class="form-group">
      <label for="reg-password">密码:*</label>
      <input type="password" id="reg-password" v-model="formData.password" :disabled="isLoading" required minlength="6" placeholder="至少 6 位" aria-describedby="password-error">
      <small v-if="errors.password" id="password-error" class="field-error">{{ errors.password }}</small>
    </div>

    <!-- 确认密码 (必填) -->
    <div class="form-group">
      <label for="reg-confirm-password">确认密码:*</label>
      <input type="password" id="reg-confirm-password" v-model="confirmPassword" :disabled="isLoading" required placeholder="请再次输入密码" aria-describedby="confirm-password-error">
      <small v-if="errors.confirmPassword" id="confirm-password-error" class="field-error">{{ errors.confirmPassword }}</small>
    </div>

    <!-- 分隔线和可选字段提示 -->
    <hr class="divider">
    <p class="optional-fields-label">选填信息 (完善资料有助于我们更好地为您服务)</p>

    <!-- 邮箱 -->
    <div class="form-group">
      <label for="reg-email">邮箱:</label>
      <input type="email" id="reg-email" v-model.trim="formData.email" :disabled="isLoading" placeholder="例如: your.email@example.com" aria-describedby="email-error">
      <small v-if="errors.email" id="email-error" class="field-error">{{ errors.email }}</small>
    </div>

    <!-- 手机号 -->
    <div class="form-group">
      <label for="reg-phone">手机号:</label>
      <input type="tel" id="reg-phone" v-model.trim="formData.phoneNumber" :disabled="isLoading" placeholder="请输入您的手机号码">
       <!-- <small v-if="errors.phoneNumber" class="field-error">{{ errors.phoneNumber }}</small> -->
    </div>

    <!-- 出生日期 -->
    <div class="form-group">
      <label for="reg-birthdate">出生日期:</label>
      <input type="date" id="reg-birthdate" v-model="formData.birthDate" :disabled="isLoading" :max="todayDate"> <!-- 限制最大日期为今天 -->
    </div>

    <!-- 头像 URL -->
    <div class="form-group">
      <label for="reg-avatar">头像 URL:</label>
      <input type="url" id="reg-avatar" v-model.trim="formData.avatarUrl" :disabled="isLoading" placeholder="图片的网址链接">
       <!-- <small v-if="errors.avatarUrl" class="field-error">{{ errors.avatarUrl }}</small> -->
    </div>

    <!-- 国籍/地区 -->
    <div class="form-group">
      <label for="reg-nationality">国籍/地区:</label>
      <input type="text" id="reg-nationality" v-model.trim="formData.nationality" :disabled="isLoading" placeholder="例如：中国大陆">
    </div>

    <!-- 地址 -->
    <div class="form-group">
      <label for="reg-address">地址:</label>
      <textarea id="reg-address" v-model.trim="formData.address" :disabled="isLoading" rows="3" placeholder="您的详细通讯地址"></textarea>
    </div>

    <!-- QQ 号 -->
    <div class="form-group">
      <label for="reg-qq">QQ 号:</label>
      <input type="text" id="reg-qq" v-model.trim="formData.socialQq" :disabled="isLoading" placeholder="可选">
    </div>

    <!-- 微信号 -->
    <div class="form-group">
      <label for="reg-wechat">微信号:</label>
      <input type="text" id="reg-wechat" v-model.trim="formData.socialWechat" :disabled="isLoading" placeholder="可选">
    </div>

    <!-- 显示整体 API 错误 -->
    <div v-if="apiErrorMessage" class="error-message api-error">
      {{ apiErrorMessage }}
    </div>

     <!-- 注册按钮 -->
    <button type="submit" :disabled="isLoading || !isFormValid" class="submit-button register-button">
      <span v-if="isLoading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
      {{ isLoading ? ' 注册中...' : '立即注册' }}
    </button>
  </form>
</template>

<script setup>
import { ref, reactive, computed, defineEmits } from 'vue';
import api from '../services/api';

// --- 定义事件 ---
const emit = defineEmits(['register-success', 'register-failed']);

// --- 响应式状态 ---
const formData = reactive({
  username: '',
  password: '',
  email: '',
  phoneNumber: '',
  birthDate: null, // 日期类型用 null 初始化
  avatarUrl: '',
  nationality: '',
  address: '',
  socialQq: '',
  socialWechat: '',
});
const confirmPassword = ref('');       // 确认密码
const isLoading = ref(false);         // 加载状态
const apiErrorMessage = ref('');      // API 错误信息
const errors = reactive({});          // 字段级别错误

// --- 计算属性 ---
// 获取今天的日期字符串 (YYYY-MM-DD)，用于限制 birthDate 的最大值
const todayDate = computed(() => new Date().toISOString().split('T')[0]);

// 检查表单是否基本有效 (仅检查必填项和密码一致性)
const isFormValid = computed(() => {
  return formData.username.length >= 3 &&
         formData.password.length >= 6 &&
         confirmPassword.value === formData.password;
});

// --- 方法 ---
/**
 * 客户端表单验证逻辑
 * @returns {boolean} 是否验证通过
 */
const validateForm = () => {
    // 清空旧错误
    Object.keys(errors).forEach(key => delete errors[key]);
    apiErrorMessage.value = ''; // 清空 API 错误
    let isValid = true;

    // 用户名校验
    if (!formData.username) { errors.username = '用户名不能为空'; isValid = false; }
    else if (formData.username.length < 3 || formData.username.length > 50) { errors.username = '用户名长度需在 3-50 字符之间'; isValid = false; }

    // 密码校验
    if (!formData.password) { errors.password = '密码不能为空'; isValid = false; }
    else if (formData.password.length < 6) { errors.password = '密码至少需要 6 位'; isValid = false; }

    // 确认密码校验
    if (!confirmPassword.value) { errors.confirmPassword = '请再次输入密码'; isValid = false; }
    else if (formData.password && confirmPassword.value !== formData.password) { errors.confirmPassword = '两次输入的密码不一致'; isValid = false; }

    // 邮箱格式校验 (如果填写了)
    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) { errors.email = '请输入有效的邮箱地址'; isValid = false; }

    // 可以在此添加其他字段的格式校验，例如手机号、URL 等

    return isValid;
};

/**
 * 处理注册表单提交
 */
const handleRegisterSubmit = async () => {
  if (!validateForm()) {
    console.warn('[RegisterForm] 客户端表单校验失败');
    apiErrorMessage.value = '请检查表单中标红的错误信息。'; // 提示用户检查字段错误
    return;
  }

  isLoading.value = true;
  console.log('[RegisterForm] 准备调用注册 API，数据:', formData);

  try {
    // 准备提交给后端的数据 (过滤掉 confirmPassword)
    const payload = { ...formData };
    // 确保空字符串日期发送 null
    if (payload.birthDate === '') {
      payload.birthDate = null;
    }

    // 调用 API 注册接口
    const response = await api.register(payload);
    console.log('[RegisterForm] 注册 API 成功响应:', response.data);

    // 注册成功，触发成功事件
    emit('register-success');

  } catch (error) {
    // 注册失败
    console.error('[RegisterForm] 注册 API 调用失败:', error);
    let message = '注册失败，请稍后重试。';
    if (error.response && error.response.data) {
        if (error.response.data.errors) {
            // 处理后端校验返回的字段错误
            message = error.response.data.message || '输入数据校验失败';
            const backendErrors = error.response.data.errors;
            Object.keys(backendErrors).forEach(field => {
                // 将后端字段错误映射到前端 errors 对象
                if (errors.hasOwnProperty(field)) { // 只映射前端存在的字段
                     errors[field] = backendErrors[field];
                } else { // 对于没有对应输入框的后端错误，显示在通用错误区
                    message += ` (${field}: ${backendErrors[field]})`;
                }
            });
        } else {
             message = error.response.data.error || error.response.data.message || JSON.stringify(error.response.data);
        }
    } else if (error.message) {
      message = error.message;
    }
    apiErrorMessage.value = message; // 显示整体 API 错误
    // 触发失败事件
    emit('register-failed', message);

  } finally {
    isLoading.value = false; // 结束加载
  }
};

</script>

<style scoped>
/* 继承或使用共享样式 */
.register-form {
    /* 可能需要特定样式 */
}

/* 字段错误提示 */
.field-error {
    color: var(--danger-color);
    font-size: 0.8em;
    margin-top: 0.25rem;
    display: block;
}

/* API 错误 */
.api-error {
    margin-top: 1rem;
    margin-bottom: 1rem;
}

/* 分隔线 */
.divider {
    border: 0;
    border-top: 1px solid #e9ecef;
    margin: 1.5rem 0;
}
.optional-fields-label {
    font-size: 0.85em;
    color: var(--secondary-color);
    text-align: center;
    margin-bottom: 1.5rem;
    margin-top: -1rem; /* 靠近分隔线 */
}

/* 注册按钮特定样式 */
.register-button {
  background-color: var(--success-color); /* 绿色 */
  border-color: var(--success-color);
}
.register-button:hover:not(:disabled) {
  background-color: #157347;
  border-color: #146c43;
}

/* Spinner 样式 (与 LoginForm 共享) */
.spinner-border-sm {
  width: 1em;
  height: 1em;
  border-width: 0.15em;
  display: inline-block;
  vertical-align: -0.125em;
  border: .15em solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spinner-border .75s linear infinite;
  margin-right: 0.5em;
}
@keyframes spinner-border { to { transform: rotate(360deg); } }
</style>