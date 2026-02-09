import { router } from './utils/router.js';
import { LoginPage } from './pages/LoginPage.js';
import { HallPage } from './pages/HallPage.js';
import { RecruitPage } from './pages/RecruitPage.js';
import { GeneralsPage } from './pages/GeneralsPage.js';
import { StageSelectionPage } from './pages/StageSelectionPage.js';

// 注册路由
router.register('/login', LoginPage);
router.register('/hall', HallPage);
router.register('/recruit', RecruitPage);
router.register('/generals', GeneralsPage);
router.register('/stages', StageSelectionPage);

// 初始化应用
function init() {
    // 检查是否已登录
    const userId = localStorage.getItem('userId');

    if (userId) {
        router.navigate('/hall');
    } else {
        router.navigate('/login');
    }
}

// 启动应用
init();
