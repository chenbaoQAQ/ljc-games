import { troopAPI } from '../api/index.js';
import { router } from '../utils/router.js';
import { getTroopMeta, getTroopTypeName } from '../config/gameData.js';

export function CodexPage(container) {
    const userId = localStorage.getItem('userId');
    if (!userId) { router.navigate('/login'); return; }

    container.innerHTML = `
    <div class="codex-page">
      <nav class="page-nav">
        <button class="btn btn-secondary btn-sm" id="back-btn">← 返回大厅</button>
        <h1 class="page-title">兵种图鉴</h1>
      </nav>

      <div class="codex-content">
        <div class="filters">
            <button class="filter-btn active" data-filter="all">全部</button>
            <button class="filter-btn" data-filter="CN">汉</button>
            <button class="filter-btn" data-filter="JP">和</button>
            <button class="filter-btn" data-filter="KR">鲜</button>
            <button class="filter-btn" data-filter="GB">英</button>
        </div>
        <div id="codex-list" class="codex-grid">
          <div class="spinner"></div>
        </div>
      </div>
      
      <div class="toast" id="toast"></div>
    </div>
  `;

    // Styles
    const style = document.createElement('style');
    style.id = 'codex-page-style';
    document.getElementById('codex-page-style')?.remove();
    style.textContent = `
    .codex-page { min-height: 100vh; background: linear-gradient(135deg, var(--bg-dark) 0%, var(--bg-medium) 100%); }
    .page-nav { background: rgba(0,0,0,0.3); backdrop-filter: blur(10px); padding: var(--spacing-md) var(--spacing-lg); display:flex; align-items:center; gap: var(--spacing-lg); border-bottom: 2px solid rgba(255,255,255,0.1); position: sticky; top: 0; z-index: 100; }
    .page-title { flex: 1; font-size: 1.3rem; background: linear-gradient(135deg, var(--primary-color), var(--accent-color)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    
    .codex-content { max-width: 1000px; margin: 0 auto; padding: var(--spacing-xl); display: flex; flex-direction: column; gap: var(--spacing-lg); }
    
    .filters { display: flex; gap: 10px; justify-content: center; margin-bottom: 20px; }
    .filter-btn { background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); color: #ccc; padding: 6px 16px; border-radius: 20px; cursor: pointer; transition: all 0.2s; }
    .filter-btn.active, .filter-btn:hover { background: var(--primary-color); color: #fff; border-color: var(--primary-color); }
    
    .codex-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
    
    .codex-card { background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; padding: 16px; display: flex; flex-direction: column; gap: 12px; position: relative; overflow: hidden; transition: transform 0.2s; }
    .codex-card:hover { transform: translateY(-2px); background: rgba(255,255,255,0.08); }
    .codex-card.locked { opacity: 0.7; filter: grayscale(0.8); }
    
    .card-header { display: flex; align-items: center; gap: 12px; }
    .avatar { width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; background: #333; position: relative; }
    .civ-badge { position: absolute; bottom: -2px; right: -2px; font-size: 0.7rem; background: #000; padding: 2px 4px; border-radius: 4px; border: 1px solid #555; }
    
    .card-info h3 { margin: 0; font-size: 1.1rem; color: #fff; }
    .card-info p { margin: 2px 0 0; font-size: 0.85rem; color: #aaa; }
    
    .card-stats { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 0.9rem; color: #ddd; background: rgba(0,0,0,0.2); padding: 8px; border-radius: 6px; }
    .stat-row { display: flex; justify-content: space-between; }
    
    .evolution-section { border-top: 1px solid rgba(255,255,255,0.1); padding-top: 10px; margin-top: auto; }
    .evo-status { font-size: 0.85rem; color: #aaa; display: flex; justify-content: space-between; align-items: center; }
    .evo-btn { padding: 4px 12px; font-size: 0.85rem; }
    
    .locked-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; flex-direction: column; z-index: 10; backdrop-filter: blur(2px); text-align: center; padding: 20px; }
    .lock-icon { font-size: 2rem; margin-bottom: 10px; }
    .lock-text { color: #ef4444; font-weight: bold; font-size: 0.9rem; }
    
    .toast { position: fixed; bottom: 40px; left: 50%; transform: translateX(-50%); padding: 12px 28px; border-radius: var(--radius-lg); font-weight: bold; opacity: 0; pointer-events: none; transition: opacity .3s ease, transform .3s ease; z-index: 999; }
    .toast.show { opacity: 1; transform: translateX(-50%) translateY(-10px); }
    .toast.success { background: var(--success-color); color:#fff; }
    .toast.error { background: var(--danger-color); color:#fff; }
  `;
    document.head.appendChild(style);

    let allTroops = [];
    let currentFilter = 'all';

    loadData();

    async function loadData() {
        try {
            const res = await troopAPI.getCodex(userId);
            if (res.code === 200 && res.data) {
                allTroops = res.data;
                renderList();
            }
        } catch (e) {
            console.error(e);
            document.getElementById('codex-list').innerHTML = '<div style="text-align:center;width:100%">加载失败</div>';
        }
    }

    function renderList() {
        const list = document.getElementById('codex-list');
        let filtered = allTroops;

        if (currentFilter !== 'all') {
            filtered = allTroops.filter(t => t.civ === currentFilter);
        }

        // Sort: Unlocked -> Locked, then ID
        filtered.sort((a, b) => {
            if (a.status !== b.status) return b.status - a.status;
            return a.troopId - b.troopId;
        });

        list.innerHTML = filtered.map(t => {
            const meta = getTroopMeta(t.troopId);
            const isLocked = t.status < 2; // 0 or 1 (Discovered but not unlocked) -> Treat as locked for usage, but shown in codex
            // Actually, status 0=Locked (Hidden/Gray), 1=Discovered (Visible but gray), 2=Unlocked (Color)
            // Let's assume server returns everything.

            const isHidden = t.status === 0; // Completely unknown? Maybe show silhouette?

            // For now, render everything but style differently
            return `
            <div class="codex-card ${isLocked ? 'locked' : ''}">
                <div class="card-header">
                    <div class="avatar" style="background:${meta.color}">
                        ${isHidden ? '?' : meta.icon}
                        <span class="civ-badge">${t.civ}</span>
                    </div>
                    <div class="card-info">
                        <h3>${t.name}</h3>
                        <p>${getTroopTypeName(t.type)} ${t.isElite ? '· 特种' : ''}</p>
                    </div>
                </div>
                
                <div class="card-stats">
                    <div class="stat-row"><span>攻击</span> <strong>${t.baseAtk}</strong></div>
                    <div class="stat-row"><span>生命</span> <strong>${t.baseHp}</strong></div>
                    <div class="stat-row"><span>统率</span> <strong>${t.cost}</strong></div>
                    <div class="stat-row"><span>射程</span> <strong>${t.type === 'ARC' ? '远' : '近'}</strong></div>
                </div>
                
                <div class="evolution-section">
                    <div class="evo-status">
                        <span>进化阶段: ${t.evolutionTier || 0}阶</span>
                        ${!isLocked && t.evolutionUnlocked
                    ? `<button class="btn btn-primary btn-sm evo-btn" data-id="${t.troopId}">进化</button>`
                    : '<span style="font-size:0.8em;color:#777">进化未解锁</span>'}
                    </div>
                </div>
                
                ${isLocked ? `
                    <div class="locked-overlay">
                        <div class="lock-icon">🔒</div>
                        <div class="lock-text">
                            ${t.status === 1 ? '已发现' : '未解锁'}<br>
                            <span style="font-size:0.8em;color:#aaa">${t.unlockHint || `需通关 ${t.civ} 关卡`}</span>
                        </div>
                    </div>
                ` : ''}
            </div>
        `;
        }).join('');

        // Bind events
        document.querySelectorAll('.evo-btn').forEach(btn => {
            btn.onclick = () => handleEvolve(btn.dataset.id);
        });
    }

    async function handleEvolve(troopId) {
        // P1-2: Redirect to new Tree Page
        if (confirm('进化功能已升级为【兵种进化树】，是否前往查看？')) {
            router.navigate('/codex/tree');
        }
    }

    // Filter events
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.onclick = () => {
            document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            currentFilter = btn.dataset.filter;
            renderList();
        };
    });

    document.getElementById('back-btn').addEventListener('click', () => router.navigate('/hall'));

    function showToast(msg, type = 'success') {
        const toast = document.getElementById('toast');
        toast.textContent = msg;
        toast.className = `toast ${type} show`;
        setTimeout(() => { toast.className = 'toast'; }, 2000);
    }
}
