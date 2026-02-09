// 简单的路由管理器
class Router {
    constructor() {
        this.routes = {};
        this.currentRoute = null;
    }

    // 注册路由
    register(path, component) {
        this.routes[path] = component;
    }

    // 导航到指定路由
    navigate(path, params = {}) {
        const component = this.routes[path];
        if (!component) {
            console.error(`Route not found: ${path}`);
            return;
        }

        this.currentRoute = path;
        const app = document.getElementById('app');
        app.innerHTML = '';
        component(app, params);
    }

    // 返回上一页
    back() {
        window.history.back();
    }
}

export const router = new Router();
export default router;
