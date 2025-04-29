<template>
  <div class="not-found-container">
    <div class="content">
      <h1>404</h1>
      <h2>页面未找到</h2>
      <p>抱歉，您要查找的页面似乎不存在或者已被移动。</p>
      <router-link to="/" class="home-button">返回首页</router-link>
    </div>
  </div>
</template>

<script setup>
// 通常 404 页面不需要脚本逻辑
import { onMounted } from 'vue';

onMounted(() => {
    document.title = "404 - 页面未找到"; // 设置页面标题
});
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 160px); /* 减去大致的页头页脚高度 */
  text-align: center;
  padding: 2rem;
}

.content {
  max-width: 500px;
}

h1 {
  font-size: 6rem; /* 大号 404 */
  font-weight: 700;
  color: #adb5bd; /* 灰色 */
  margin-bottom: 0.5rem;
  line-height: 1;
}

h2 {
  font-size: 1.8rem;
  font-weight: 500;
  color: #495057;
  margin-bottom: 1rem;
}

p {
  font-size: 1rem;
  color: #6c757d;
  margin-bottom: 2rem;
}

.home-button {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
  font-weight: 500;
  color: #fff;
  background-color: var(--primary-color);
  border: 1px solid var(--primary-color);
  border-radius: var(--border-radius);
  text-decoration: none;
  transition: all 0.2s ease-in-out;
}

.home-button:hover {
  background-color: #0b5ed7;
  border-color: #0a58ca;
}
</style>