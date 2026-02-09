import { hallAPI, playerAPI } from '../api/index.js';
import { router } from '../utils/router.js';

export function GeneralsPage(container) {
    const userId = localStorage.getItem('userId');
    if (!userId) { router.navigate('/login'); return; }

    container.innerHTML = `
    <div class="generals-page">
      <nav class="page-nav">
        <button class="btn btn-secondary btn-sm" id="back-btn">← 返回大厅</button>
        <h1 class="page-title">👥 武将管理</h1>
        <div class="nav-gold">💰 <span id="gold-display">--</span></div>
      </nav>

      <div class="generals-content" id="generals-content">
        <div class="spinner" style="margin: 60px auto;"></div>
      </div>

      <div class="toast" id="toast"></div>
    </div>
  `;

    const style = document.createElement('style');
    style.id = 'generals-page-style';
    document.getElementById('generals-page-style')?.remove();
    style.textContent = `
    .generals-page {
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

    .generals-content {
      max-width: 1000px; margin: 0 auto; padding: var(--spacing-xl);
      display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: var(--spacing-lg);
    }

    .general-detail-card {
      border-radius: var(--radius-lg);
      background: rgba(255,255,255,0.04);
      border: 2px solid rgba(255,255,255,0.1);
      padding: var(--spacing-lg);
      display: flex; flex-direction: column; gap: var(--spacing-md);
      transition: all 0.3s ease;
    }
    .general-detail-card:hover {
      border-color: rgba(78, 205, 196, 0.4);
      box-shadow: 0 4px 20px rgba(78, 205, 196, 0.1);
    }
    .general-detail-card.inactive {
      opacity: 0.65;
      border-color: rgba(255,255,255,0.06);
    }

    .gd-top {
      display: flex; align-items: center; gap: var(--spacing-md);
    }
    .gd-avatar {
      width: 64px; height: 64px; border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      font-size: 2rem; flex-shrink: 0;
      background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
    }
    .gd-avatar.inactive-avatar { background: rgba(255,255,255,0.1); }
    .gd-info { flex: 1; }
    .gd-name { font-size: 1.15rem; font-weight: bold; margin-bottom: 4px; }
    .gd-meta { font-size: 0.85rem; color: var(--text-secondary); display: flex; gap: 12px; }

    .gd-stats {
      display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px;
    }
    .gd-stat {
      padding: 6px 10px; background: rgba(255,255,255,0.03);
      border-radius: var(--radius-sm); font-size: 0.85rem;
    }
    .gd-stat-label { color: var(--text-secondary); }
    .gd-stat-value { font-weight: bold; float: right; }

    .gd-hp-bar {
      height: 8px; border-radius: 4px;
      background: rgba(255,255,255,0.1); overflow: hidden;
    }
    .gd-hp-fill {
      height: 100%; border-radius: 4px;
      background: linear-gradient(90deg, var(--success-color), #2ecc71);
      transition: width 0.4s ease;
    }

    .gd-actions {
      display: flex; gap: 8px; flex-wrap: wrap;
    }
    .gd-actions .btn { flex: 1; min-width: 80px; font-size: 0.85rem; padding: 8px 12px; }

    .status-badge {
      display: inline-block; padding: 2px 10px; border-radius: 20px;
      font-size: 0.75rem; font-weight: bold;
    }
    .status-active { background: rgba(46,204,113,0.2); color: var(--success-color); }
    .status-inactive { background: rgba(255,107,53,0.2); color: var(--primary-color); }
    .status-locked { background: rgba(255,255,255,0.08); color: var(--text-secondary); }

    .toast {
      position: fixed; bottom: 40px; left: 50%; transform: translateX(-50%);
      padding: 12px 28px; border-radius: var(--radius-lg);
      font-weight: bold; font-size: 0.95rem;
      opacity: 0; pointer-events: none;
      transition: opacity 0.3s ease, transform 0.3s ease; z-index: 999;
    }
    .toast.show { opacity: 1; transform: translateX(-50%) translateY(-10px); }
    .toast.success { background: var(--success-color); color: #fff; }
    .toast.error { background: var(--danger-color); color: #fff; }
  `;
    document.head.appendChild(style);

    // 加载
    loadAll();

    async function loadAll() {
        try {
            const [generalsRes, playerRes] = await Promise.all([
                hallAPI.getGenerals(userId),
                playerAPI.getInfo(userId),
            ]);

            if (playerRes.code === 200 && playerRes.data) {
                document.getElementById('gold-display').textContent = (playerRes.data.gold || 0).toLocaleString();
            }

            const contentEl = document.getElementById('generals-content');

            if (generalsRes.code === 200 && generalsRes.data && generalsRes.data.length > 0) {
                renderGenerals(generalsRes.data, contentEl);
            } else {
                contentEl.innerHTML = '<p style="text-align:center; grid-column:1/-1; color:var(--text-secondary);">暂无武将</p>';
            }
        } catch (e) {
            console.error('加载武将失败:', e);
            document.getElementById('generals-content').innerHTML =
                '<p style="text-align:center; grid-column:1/-1; color:var(--danger-color);">加载失败</p>';
        }
    }

    function renderGenerals(generals, contentEl) {
        contentEl.innerHTML = generals.map(g => {
            const isActive = g.activated;
            const hpPct = g.maxHp > 0 ? Math.round((g.currentHp / g.maxHp) * 100) : 100;

            let statusHtml, statusClass;
            if (!g.unlocked) {
                statusHtml = '🔒 未解锁'; statusClass = 'status-locked';
            } else if (!g.activated) {
                statusHtml = '⚡ 未激活'; statusClass = 'status-inactive';
            } else {
                statusHtml = '✅ 已激活'; statusClass = 'status-active';
            }

            return `
      <div class="general-detail-card ${isActive ? '' : 'inactive'}" data-id="${g.id}">
        <div class="gd-top">
          <div class="gd-avatar ${isActive ? '' : 'inactive-avatar'}">🎖️</div>
          <div class="gd-info">
            <div class="gd-name">武将 #${g.templateId}</div>
            <div class="gd-meta">
              <span>Lv.${g.level}</span>
              <span>阶 ${g.tier}</span>
              <span class="status-badge ${statusClass}">${statusHtml}</span>
            </div>
          </div>
        </div>

        <div class="gd-hp-bar"><div class="gd-hp-fill" style="width:${hpPct}%"></div></div>
        <div style="font-size:0.8rem;color:var(--text-secondary);">HP: ${g.currentHp}/${g.maxHp} · 统帅: ${g.capacity}</div>

        <div class="gd-actions">
          ${g.unlocked && !g.activated ? `<button class="btn btn-primary action-btn" data-action="activate" data-id="${g.id}">激活</button>` : ''}
          ${g.activated ? `<button class="btn btn-primary action-btn" data-action="upgrade" data-id="${g.id}">升级</button>` : ''}
          ${g.activated ? `<button class="btn btn-secondary action-btn" data-action="ascend" data-id="${g.id}">升阶</button>` : ''}
          ${!g.unlocked ? `<span style="font-size:0.8rem;color:var(--text-secondary);">通关可解锁</span>` : ''}
        </div>
      </div>
      `;
        }).join('');

        // 绑定按钮事件
        contentEl.querySelectorAll('.action-btn').forEach(btn => {
            btn.addEventListener('click', () => handleAction(btn.dataset.action, btn.dataset.id));
        });
    }

    async function handleAction(action, generalId) {
        const actionMap = {
            activate: { fn: () => hallAPI.activateGeneral(userId, generalId), label: '激活' },
            upgrade: { fn: () => hallAPI.upgradeGeneral(userId, generalId), label: '升级' },
            ascend: { fn: () => hallAPI.ascendGeneral(userId, generalId), label: '升阶' },
        };

        const { fn, label } = actionMap[action];
        try {
            const result = await fn();
            console.log(`${label}结果:`, result);
            if (result.code === 200) {
                showToast(`${label}成功！`, 'success');
                loadAll(); // 刷新
            } else {
                showToast(result.message || `${label}失败`, 'error');
            }
        } catch (e) {
            showToast((typeof e.message === 'string') ? e.message : `${label}失败`, 'error');
        }
    }

    document.getElementById('back-btn').addEventListener('click', () => {
        router.navigate('/hall');
    });

    function showToast(msg, type = 'success') {
        const toast = document.getElementById('toast');
        toast.textContent = msg;
        toast.className = `toast ${type} show`;
        setTimeout(() => { toast.className = 'toast'; }, 2000);
    }
}
