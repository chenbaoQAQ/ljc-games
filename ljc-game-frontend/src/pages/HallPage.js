import { playerAPI, hallAPI } from '../api/index.js';
import { router } from '../utils/router.js';
import { getTroopMeta } from '../config/gameData.js';

export function HallPage(container) {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    router.navigate('/login');
    return;
  }

  container.innerHTML = `
    <div class="hall-container">
      <!-- 顶部导航栏 -->
      <nav class="hall-nav">
        <div class="nav-left">
          <h1 class="hall-title">三国群英传</h1>
          <span class="player-name" id="player-name">${localStorage.getItem('username') || '主公'}</span>
        </div>
        <div class="nav-right">
          <div class="resource-display">
            <div class="resource-item">
              <span class="resource-icon">💰</span>
              <span id="gold-count">0</span>
            </div>
            <div class="resource-item">
              <span class="resource-icon">💎</span>
              <span id="diamond-count">0</span>
            </div>
          </div>
          <button class="btn btn-secondary btn-sm" id="logout-btn">退出</button>
        </div>
      </nav>

      <!-- 主内容区 -->
      <div class="hall-content container">
        <!-- 武将卡片 -->
        <div class="section-card card">
          <h2 class="card-title">👥 武将</h2>
          <div class="generals-grid" id="generals-grid">
            <div class="spinner"></div>
          </div>
          <button class="btn btn-primary" id="manage-generals-btn">管理武将</button>
        </div>

        <!-- 兵力卡片 -->
        <div class="section-card card">
          <h2 class="card-title">⚔️ 兵力</h2>
          <div class="troops-grid" id="troops-grid">
            <div class="spinner"></div>
          </div>
          <button class="btn btn-primary" id="recruit-btn">招募士兵</button>
        </div>

        <!-- 关卡进度卡片 -->
        <div class="section-card card">
          <h2 class="card-title">🗺️ 征战四方</h2>
          <div class="progress-grid" id="progress-grid">
            <div class="spinner"></div>
          </div>
          <button class="btn btn-success" id="select-stage-btn">选择关卡</button>
        </div>

        <!-- 其他功能 -->
        <div class="section-card card">
          <h2 class="card-title">🔧 其他功能</h2>
            <button class="btn btn-secondary" id="equip-btn">装备强化</button>
            <button class="btn btn-secondary" id="gem-btn">宝石镶嵌</button>
            <button class="btn btn-secondary" id="skill-btn">技能学习</button>
            <button class="btn btn-secondary" id="codex-btn">兵种图鉴</button>
            <button class="btn btn-secondary" id="tower-btn">爬塔挑战</button>
          </div>
        </div>
      </div>
    </div>
  `;

  // 添加样式
  const style = document.createElement('style');
  style.textContent = `
    .hall-container {
      min-height: 100vh;
      background: linear-gradient(135deg, var(--bg-dark) 0%, var(--bg-medium) 100%);
    }

    .hall-nav {
      background: rgba(0, 0, 0, 0.3);
      backdrop-filter: blur(10px);
      padding: var(--spacing-md) var(--spacing-lg);
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 2px solid rgba(255, 255, 255, 0.1);
      position: sticky;
      top: 0;
      z-index: 100;
    }

    .nav-left {
      display: flex;
      align-items: center;
      gap: var(--spacing-lg);
    }

    .hall-title {
      font-size: 1.5rem;
      font-weight: bold;
      background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }

    .player-name {
      color: var(--text-secondary);
      font-size: 1.1rem;
    }

    .nav-right {
      display: flex;
      align-items: center;
      gap: var(--spacing-lg);
    }

    .resource-display {
      display: flex;
      gap: var(--spacing-md);
    }

    .resource-item {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      padding: 8px 16px;
      background: rgba(255, 255, 255, 0.05);
      border-radius: var(--radius-lg);
      font-weight: bold;
    }

    .resource-icon {
      font-size: 1.2rem;
    }

    .btn-sm {
      padding: 8px 16px;
      font-size: 0.9rem;
    }

    .hall-content {
      padding: var(--spacing-xl);
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: var(--spacing-lg);
    }

    .section-card {
      height: fit-content;
    }

    .generals-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-md);
      min-height: 100px;
      align-items: center;
      justify-items: center;
    }

    .general-card {
      background: rgba(255, 255, 255, 0.05);
      border: 2px solid rgba(255, 255, 255, 0.1);
      border-radius: var(--radius-md);
      padding: var(--spacing-md);
      text-align: center;
      transition: all 0.3s ease;
      cursor: pointer;
      width: 100%;
    }

    .general-card:hover {
      transform: translateY(-5px);
      border-color: var(--secondary-color);
      box-shadow: 0 5px 15px rgba(78, 205, 196, 0.3);
    }

    .general-avatar {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      margin: 0 auto var(--spacing-sm);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 2rem;
      background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
    }

    .general-name {
      font-weight: bold;
      color: var(--text-primary);
      margin-bottom: var(--spacing-xs);
    }

    .general-level {
      font-size: 0.85rem;
      color: var(--text-secondary);
    }

    .troops-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-md);
      max-height: 300px;
      overflow-y: auto;
      padding-right: 4px;
    }

    .troop-item {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      padding: var(--spacing-md);
      background: rgba(255, 255, 255, 0.05);
      border-radius: var(--radius-md);
      border: 2px solid rgba(255, 255, 255, 0.1);
      transition: all 0.3s ease;
    }

    .troop-item:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-md);
    }

    .troop-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
    }

    .troop-info {
      display: flex;
      flex-direction: column;
      min-width: 0;
    }

    .troop-name {
      font-size: 0.9rem;
      color: var(--text-secondary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .troop-count {
      font-size: 1.2rem;
      font-weight: bold;
      color: var(--text-primary);
    }

    .progress-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-md);
      min-height: 100px;
      align-items: center;
    }

    .progress-item {
      padding: var(--spacing-md);
      background: rgba(255, 255, 255, 0.05);
      border-radius: var(--radius-md);
      border: 2px solid rgba(255, 255, 255, 0.1);
    }

    .progress-civ {
      font-weight: bold;
      margin-bottom: var(--spacing-sm);
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
    }

    .progress-stage {
      font-size: 0.9rem;
      color: var(--text-secondary);
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: var(--spacing-md);
    }

    @media (max-width: 768px) {
      .hall-content {
        grid-template-columns: 1fr;
      }

      .troops-grid {
        grid-template-columns: 1fr;
      }

      .actions-grid {
        grid-template-columns: 1fr;
      }
    }
  `;
  document.head.appendChild(style);

  // 加载数据
  loadPlayerData();
  loadGenerals();
  loadProgress();

  async function loadPlayerData() {
    try {
      const result = await playerAPI.getInfo(userId);
      console.log('玩家数据:', result);
      if (result.code === 200 && result.data) {
        document.getElementById('player-name').textContent = result.data.nickname;
        document.getElementById('gold-count').textContent = (result.data.gold || 0).toLocaleString();
        document.getElementById('diamond-count').textContent = (result.data.diamond || 0).toLocaleString();

        renderTroops(result.data.troops || [], result.data.initialCiv || 'CN');
      } else {
        console.warn('加载玩家数据: code不为200', result);
      }
    } catch (error) {
      console.error('加载玩家数据失败:', error);
    }
  }

  async function loadGenerals() {
    try {
      const result = await hallAPI.getGenerals(userId);
      console.log('武将数据:', result);
      const grid = document.getElementById('generals-grid');

      if (result.code === 200 && result.data && result.data.length > 0) {
        grid.innerHTML = result.data.slice(0, 4).map(general => `
          <div class="general-card" data-id="${general.id}">
            <div class="general-avatar">🎖️</div>
            <div class="general-name">武将#${general.templateId}</div>
            <div class="general-level">Lv.${general.level}</div>
            ${general.activated ? '<div class="badge badge-inf">已激活</div>' : '<div class="badge" style="background:rgba(255,255,255,0.1)">未激活</div>'}
          </div>
        `).join('');
      } else {
        grid.innerHTML = '<p style="grid-column: 1/-1; text-align: center;">暂无武将</p>';
      }
    } catch (error) {
      console.error('加载武将失败:', error);
      document.getElementById('generals-grid').innerHTML =
        '<p style="grid-column: 1/-1; text-align: center; color: var(--danger-color);">加载失败</p>';
    }
  }

  async function loadProgress() {
    try {
      const result = await hallAPI.getProgress(userId);
      console.log('进度数据:', result);
      const grid = document.getElementById('progress-grid');

      if (result.code === 200 && result.data && result.data.length > 0) {
        const civNames = { CN: '🇨🇳 中国', JP: '🇯🇵 日本', KR: '🇰🇷 韩国', GB: '🇬🇧 英国' };
        grid.innerHTML = result.data.map(prog => `
          <div class="progress-item">
            <div class="progress-civ">
              <span class="badge badge-${prog.civ.toLowerCase()}">${civNames[prog.civ] || prog.civ}</span>
            </div>
            <div class="progress-stage">
              当前进度: ${prog.maxStageCleared || 0}/10 关
              ${prog.unlocked ? '' : '<br/><span style="color: var(--danger-color);">🔒 未解锁</span>'}
            </div>
          </div>
        `).join('');
      } else {
        grid.innerHTML = '<p style="grid-column: 1/-1; text-align: center;">暂无进度</p>';
      }
    } catch (error) {
      console.error('加载进度失败:', error);
    }
  }

  function renderTroops(troops, civ) {
    const grid = document.getElementById('troops-grid');
    if (!grid) return;

    const list = (troops || [])
      .map(t => {
        const meta = getTroopMeta(t.troopId);
        return {
          troopId: t.troopId,
          count: t.count || 0,
          civ: meta.civ,
          type: meta.type,
          icon: meta.icon,
          name: meta.name,
          color: meta.color,
          isElite: !!meta.isElite,
        };
      })
      .filter(t => t.civ === civ)
      .sort((a, b) => {
        if (a.isElite !== b.isElite) return a.isElite ? 1 : -1;
        const typeOrder = { INF: 1, ARC: 2, CAV: 3 };
        return (typeOrder[a.type] || 9) - (typeOrder[b.type] || 9);
      });

    if (list.length === 0) {
      grid.innerHTML = '<p style="grid-column: 1/-1; text-align: center;">暂无兵力</p>';
      return;
    }

    grid.innerHTML = list.map(troop => `
      <div class="troop-item">
        <div class="troop-icon" style="background: ${troop.color};">${troop.icon}</div>
        <div class="troop-info">
          <span class="troop-name">${troop.name}${troop.isElite ? ' [特种]' : ''}</span>
          <span class="troop-count">${troop.count.toLocaleString()}</span>
        </div>
      </div>
    `).join('');
  }

  // 事件绑定
  document.getElementById('logout-btn').addEventListener('click', () => {
    localStorage.clear();
    router.navigate('/login');
  });

  document.getElementById('select-stage-btn').addEventListener('click', () => {
    router.navigate('/stages');
  });

  document.getElementById('manage-generals-btn').addEventListener('click', () => {
    router.navigate('/generals');
  });

  document.getElementById('recruit-btn').addEventListener('click', () => { router.navigate('/recruit'); });
  document.getElementById('equip-btn').addEventListener('click', () => { router.navigate('/equipment'); });
  document.getElementById('gem-btn').addEventListener('click', () => { router.navigate('/gem'); });
  document.getElementById('skill-btn').addEventListener('click', () => { router.navigate('/skill'); });
  document.getElementById('codex-btn').addEventListener('click', () => { router.navigate('/codex'); });
  document.getElementById('tower-btn').addEventListener('click', () => { alert('爬塔功能敬请期待'); });
}
