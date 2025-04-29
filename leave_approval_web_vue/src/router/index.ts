import { createRouter, createWebHistory } from 'vue-router';

// 导入页面组件
import LoginPage from '../views/LoginPage.vue';
import RegisterPage from '../views/RegisterPage.vue';
import HomePage from '../views/HomePage.vue';
import NotFoundPage from '../views/NotFoundPage.vue';

/**
 * 辅助函数：检查用户是否已认证
 * (简单地检查 localStorage 中是否存在 authToken)
 * @returns {boolean}
 */
const isAuthenticated = () => {
    const token = localStorage.getItem('authToken');
    // console.debug('isAuthenticated check:', !!token); // 调试日志
    return !!token;
};

// 定义路由规则数组
const routes = [
    {
        path: '/',          // 网站根路径
        name: 'Root',
        redirect: '/home',  // 默认重定向到主页
    },
    {
        path: '/login',     // 登录页面
        name: 'Login',
        component: LoginPage,
        meta: {
            title: '登录',      // 页面标题 (可选)
            requiresGuest: true // 标记此路由只允许未登录用户访问
        }
    },
    {
        path: '/register',  // 注册页面
        name: 'Register',
        component: RegisterPage,
        meta: {
            title: '注册',
            requiresGuest: true // 标记此路由只允许未登录用户访问
        }
    },
    {
        path: '/home',      // 主页 (受保护)
        name: 'Home',
        component: HomePage,
        meta: {
            title: '主页',
            requiresAuth: true  // 标记此路由需要认证才能访问
        }
    },
    // --- 可以添加其他路由 ---
    // 例如设置页面
    // {
    //     path: '/settings',
    //     name: 'Settings',
    //     // 路由懒加载 (仅在访问时才加载组件)
    //     component: () => import('../views/SettingsPage.vue'),
    //     meta: { requiresAuth: true, title: '设置' }
    // },

    // --- 404 Not Found 页面路由 ---
    // 使用通配符 `/:pathMatch(.*)*` 匹配所有未被前面规则捕获的路径
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: NotFoundPage,
        meta: {
            title: '页面未找到'
        }
    }
];

// 创建路由实例
const router = createRouter({
    // 使用 HTML5 History 模式 (需要服务器配置支持 URL 重写，否则刷新会 404)
    history: createWebHistory(import.meta.env.BASE_URL),
    routes, // 应用上面定义的路由规则
    // 定义路由链接激活时的 CSS 类名 (可选)
    linkActiveClass: 'router-link-active',
    linkExactActiveClass: 'router-link-exact-active',
    // 路由切换时的滚动行为
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            // 如果有保存的滚动位置 (例如浏览器后退)，则恢复该位置
            return savedPosition;
        } else {
            // 否则滚动到页面顶部
            return { top: 0, behavior: 'smooth' }; // 平滑滚动效果
        }
    }
});

// --- 全局前置导航守卫 (Navigation Guard) ---
// 在每次路由跳转发生之前执行此函数
router.beforeEach((to, from, next) => {
    // 获取目标路由的 meta 信息
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
    const requiresGuest = to.matched.some(record => record.meta.requiresGuest);
    // 获取当前用户的认证状态
    const authenticated = isAuthenticated();

    console.debug(`路由导航守卫: 从 ${from.fullPath} 到 ${to.fullPath}`);
    console.debug(`目标路由: requiresAuth=${requiresAuth}, requiresGuest=${requiresGuest}. 当前认证状态: ${authenticated}`);

    // 动态设置页面标题 (可选)
    if (to.meta.title) {
        document.title = `${to.meta.title} - 用户认证系统`; // 可以加上应用名称后缀
    } else {
         document.title = '用户认证系统'; // 默认标题
    }

    if (requiresAuth && !authenticated) {
        // --- 情况 1: 目标路由需要认证，但用户未认证 ---
        console.log('导航守卫: 访问受保护页面，用户未认证，重定向到登录页。');
        // 跳转到登录页，并将用户原本想访问的路径作为 `redirect` 查询参数传递过去
        next({ name: 'Login', query: { redirect: to.fullPath } });
    } else if (requiresGuest && authenticated) {
        // --- 情况 2: 目标路由只允许访客访问 (如登录、注册)，但用户已认证 ---
        console.log('导航守卫: 已认证用户访问访客页面，重定向到主页。');
        // 直接跳转到主页
        next({ name: 'Home' });
    } else {
        // --- 情况 3: 其他所有情况 (允许访问) ---
        // - 访问公共页面
        // - 已认证用户访问需认证页面
        // - 未认证用户访问访客页面
        // console.debug('导航守卫: 允许访问。');
        next(); // 调用 next() 表示允许导航继续
    }
});

// --- 全局后置钩子 (可选) ---
// 在每次路由跳转完成后执行
// router.afterEach((to, from) => {
//   console.log(`导航完成: 从 ${from.path} 到 ${to.path}`);
//   // 可以在这里做一些统计或清理工作
// });

export default router; // 导出路由实例供 main.js 使用