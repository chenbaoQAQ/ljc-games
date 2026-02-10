import { hallAPI, playerAPI } from '../api/index.js';
import { router } from '../utils/router.js';

export function StageSelectionPage(container) {
  const userId = localStorage.getItem('userId');
  if (!userId) { router.navigate('/login'); return; }

  container.innerHTML = `
    <div class="stage-page">
      <nav class="page-nav">
        <button class="btn btn-secondary btn-sm" id="back-btn">← 返回大厅</button>
        <h1 class="page-title">🗺️ 征战四方</h1>
        <div class="nav-gold">💰 <span id="gold-display">--</span></div>
      </nav>

      <div class="stage-content">
        <!-- 阵营切换 Tab -->
        <div class="civ-tabs" id="civ-tabs">
          <button class="civ-tab active" data-civ="CN">🇨🇳 中国</button>
          <button class="civ-tab" data-civ="JP">🇯🇵 日本</button>
          <button class="civ-tab" data-civ="KR">🇰🇷 韩国</button>
          <button class="civ-tab" data-civ="GB">🇬🇧 英国</button>
        </div>

        <!-- 关卡列表容器 -->
        <div class="stage-grid" id="stage-grid">
          <div class="spinner"></div>
        </div>
      </div>

      <div class="toast" id="toast"></div>
    </div>
  `;

  const style = document.createElement('style');
  style.id = 'stage-page-style';
  document.getElementById('stage-page-style')?.remove();
  style.textContent = `
    .stage-page {
      min-height: 100vh;
      background: linear-gradient(135deg, var(--bg-dark) 0%, var(--bg-medium) 100%);
    }
    .page-nav {
      background: rgba(0,0,0,0.3); backdrop-filter: blur(10px);
      padding: var(--spacing-md) var(--spacing-lg);
      display: flex; align-items: center; gap: var(--spacing-lg);
      border-bottom: 2px solid rgba(255,255,255,0.1);
      position: sticky; top: 0; z-index: 100;
    }
    .page-title {
      flex: 1; font-size: 1.3rem;
      background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
      -webkit-background-clip: text; -webkit-text-fill-color: transparent;
    }
    .nav-gold { font-weight: bold; font-size: 1.1rem; }
    .btn-sm { padding: 6px 14px; font-size: 0.85rem; }

    .stage-content {
      max-width: 1000px; margin: 0 auto; padding: var(--spacing-xl);
      display: flex; flex-direction: column; gap: var(--spacing-lg);
    }

    .civ-tabs {
      display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 10px;
    }
    .civ-tab {
      flex: 1;
      padding: 12px;
      background: rgba(255,255,255,0.05);
      border: 1px solid rgba(255,255,255,0.1);
      border-radius: var(--radius-md);
      color: var(--text-secondary);
      font-weight: bold; cursor: pointer;
      transition: all 0.2s ease;
    }
    .civ-tab:hover { background: rgba(255,255,255,0.1); }
    .civ-tab.active {
      background: rgba(78, 205, 196, 0.15);
      border-color: var(--secondary-color);
      color: var(--secondary-color);
      box-shadow: 0 0 10px rgba(78, 205, 196, 0.2);
    }

    .stage-grid {
      display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 20px;
    }

    .stage-card {
      position: relative;
      background: rgba(255,255,255,0.03);
      border: 2px solid rgba(255,255,255,0.08);
      border-radius: var(--radius-lg);
      padding: 20px;
      text-align: center;
      transition: all 0.3s ease;
      cursor: pointer;
      overflow: hidden;
    }
    
    .stage-card:hover { transform: translateY(-3px); }

    /* 解锁状态 */
    .stage-card.unlocked {
      border-color: rgba(255,255,255,0.3);
      background: rgba(255,255,255,0.08);
    }
    .stage-card.unlocked:hover {
      border-color: var(--primary-color);
      box-shadow: 0 5px 15px rgba(255, 107, 53, 0.2);
    }

    /* 已通关状态 */
    .stage-card.cleared {
      border-color: var(--success-color);
      background: rgba(46, 204, 113, 0.1);
    }
    .stage-card.cleared::after {
      content: '✅'; position: absolute; top: 5px; right: 5px; font-size: 1.2rem;
    }

    /* 锁定状态 */
    .stage-card.locked {
      opacity: 0.5; cursor: not-allowed;
      border-style: dashed;
    }

    .stage-num {
      font-size: 2.5rem; font-weight: bold; opacity: 0.2;
      position: absolute; bottom: -5px; right: 10px;
      line-height: 1; pointer-events: none;
    }

    .stage-name {
      font-size: 1.2rem; font-weight: bold; margin-bottom: 8px; z-index: 1; position: relative;
    }
    .stage-desc {
      font-size: 0.85rem; color: var(--text-secondary); z-index: 1; position: relative;
    }

    .lock-icon { font-size: 2rem; margin-bottom: 10px; }

    .toast {
      position: fixed; bottom: 40px; left: 50%; transform: translateX(-50%);
      padding: 12px 28px; border-radius: var(--radius-lg);
      font-weight: bold; font-size: 0.95rem;
      opacity: 0; pointer-events: none;
      transition: opacity 0.3s ease, transform 0.3s ease; z-index: 999;
    }
    .toast.show { opacity: 1; transform: translateX(-50%) translateY(-10px); }
  `;
  document.head.appendChild(style);

  // --- 状态 ---
  let currentCiv = 'CN';
  let progressData = []; // 存储后端返回的进度 List

  // --- 初始化 ---
  init();

  async function init() {
    // 绑定 Tab 点击
    document.querySelectorAll('.civ-tab').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.civ-tab').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentCiv = btn.dataset.civ;
        renderStages();
      });
    });

    document.getElementById('back-btn').addEventListener('click', () => {
      router.navigate('/hall');
    });

    // 加载数据
    try {
      const [progRes, playerRes] = await Promise.all([
        hallAPI.getProgress(userId),
        playerAPI.getInfo(userId)
      ]);

      if (playerRes.code === 200 && playerRes.data) {
        document.getElementById('gold-display').textContent = (playerRes.data.gold || 0).toLocaleString();
      }

      if (progRes.code === 200 && progRes.data) {
        progressData = progRes.data;
        renderStages();
      }
    } catch (e) {
      console.error('加载失败', e);
      document.getElementById('stage-grid').innerHTML = '<p>加载失败，请重试</p>';
    }
  }

  function renderStages() {
    const grid = document.getElementById('stage-grid');
    grid.innerHTML = '';

    // 找到当前阵营的进度
    const civProg = progressData.find(p => p.civ === currentCiv);

    if (!civProg || !civProg.unlocked) {
      grid.innerHTML = `
        <div style="grid-column:1/-1; text-align:center; padding: 40px;">
          <h2>🔒 该势力尚未解锁</h2>
          <p style="color:var(--text-secondary)">请先通关前置势力的主线关卡</p>
        </div>
      `;
      return;
    }

    // 假设每个阵营 10 关
    const totalStages = 10;
    const cleared = civProg.maxStageCleared || 0;

    for (let i = 1; i <= totalStages; i++) {
      const isCleared = i <= cleared;
      const isUnlocked = i <= cleared + 1; // 下一关解锁

      const card = document.createElement('div');
      card.className = `stage-card ${isCleared ? 'cleared' : ''} ${isUnlocked ? 'unlocked' : 'locked'}`;

      // 关卡类型判断 (简单逻辑：5是城墙，10是BOSS)
      let typeText = '普通';
      if (i === 5 || i === 9) typeText = '🏰 攻城';
      if (i === 10) typeText = '👹 BOSS';

      card.innerHTML = `
        ${!isUnlocked ? '<div class="lock-icon">🔒</div>' : ''}
        <div class="stage-name">第 ${i} 关</div>
        <div class="stage-desc">${typeText}</div>
        <div class="stage-num">${i}</div>
      `;

      if (isUnlocked) {
        card.addEventListener('click', () => {
          // 这里以后跳转到战斗准备页，现在先弹个提示
          // router.navigate(`/battle/prepare?civ=${currentCiv}&stage=${i}`);
          enterBattlePrepare(currentCiv, i);
        });
      }

      grid.appendChild(card);
    }
  }

  function enterBattlePrepare(civ, stage) {
    if (confirm(`准备攻打 [${civ} 第${stage}关] 吗？`)) {
      router.navigate('/battle', { civ, stageNo: stage });
    }
  }
}
