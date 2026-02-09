import { playerAPI, hallAPI } from '../api/index.js';
import { router } from '../utils/router.js';

export function RecruitPage(container) {
  const userId = localStorage.getItem('userId');
  if (!userId) { router.navigate('/login'); return; }

  // CN兵种 troopId 映射（后续可根据用户阵营动态加载）
  const troops = [
    { troopId: 2001, name: '步兵', type: 'INF', icon: '🛡️', color: 'var(--inf-color)', desc: '近战单位，攻守兼备', cost: 20 },
    { troopId: 2002, name: '弓兵', type: 'ARC', icon: '🏹', color: 'var(--arc-color)', desc: '远程单位，先手攻击', cost: 20 },
    { troopId: 2003, name: '骑兵', type: 'CAV', icon: '🐎', color: 'var(--cav-color)', desc: '速度最快，冲锋陷阵', cost: 40 },
  ];

  container.innerHTML = `
    <div class="recruit-page">
      <nav class="page-nav">
        <button class="btn btn-secondary btn-sm" id="back-btn">← 返回大厅</button>
        <h1 class="page-title">🏕️ 招兵买马</h1>
        <div class="nav-gold">
          <span>💰 <span id="gold-display">--</span></span>
        </div>
      </nav>

      <div class="recruit-content">
        ${troops.map(t => `
        <div class="recruit-card card" data-troop-id="${t.troopId}">
          <div class="troop-header">
            <div class="troop-avatar" style="background: ${t.color};">${t.icon}</div>
            <div>
              <h3>${t.name}</h3>
              <p class="troop-desc">${t.desc}</p>
            </div>
          </div>
          <div class="troop-stats">
            <span>当前数量: <strong id="count-${t.troopId}">0</strong></span>
            <span>单价: <strong>${t.cost}</strong> 金</span>
          </div>
          <div class="recruit-controls">
            <button class="btn btn-sm qty-btn" data-delta="-10">-10</button>
            <button class="btn btn-sm qty-btn" data-delta="-1">-1</button>
            <input type="number" class="recruit-input" id="qty-${t.troopId}" value="10" min="1" />
            <button class="btn btn-sm qty-btn" data-delta="1">+1</button>
            <button class="btn btn-sm qty-btn" data-delta="10">+10</button>
          </div>
          <button class="btn btn-primary recruit-btn" data-troop-id="${t.troopId}">招募${t.name}</button>
        </div>
        `).join('')}
      </div>

      <div class="toast" id="toast"></div>
    </div>
  `;

  // 样式
  const style = document.createElement('style');
  style.id = 'recruit-page-style';
  document.getElementById('recruit-page-style')?.remove();
  style.textContent = `
    .recruit-page {
      min-height: 100vh;
      background: linear-gradient(135deg, var(--bg-dark) 0%, var(--bg-medium) 100%);
    }
    .page-nav {
      background: rgba(0,0,0,0.3);
      backdrop-filter: blur(10px);
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

    .recruit-content {
      max-width: 900px; margin: 0 auto; padding: var(--spacing-xl);
      display: flex; flex-direction: column; gap: var(--spacing-lg);
    }
    .recruit-card { display: flex; flex-direction: column; gap: var(--spacing-md); }
    .troop-header { display: flex; align-items: center; gap: var(--spacing-md); }
    .troop-avatar {
      width: 56px; height: 56px; border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      font-size: 1.8rem; flex-shrink: 0;
    }
    .troop-header h3 { margin: 0; font-size: 1.2rem; }
    .troop-desc { margin: 4px 0 0; font-size: 0.85rem; color: var(--text-secondary); }
    .troop-stats {
      display: flex; justify-content: space-between;
      padding: 8px 12px; background: rgba(255,255,255,0.03);
      border-radius: var(--radius-sm); font-size: 0.95rem; color: var(--text-secondary);
    }
    .recruit-controls { display: flex; align-items: center; gap: 8px; justify-content: center; }
    .qty-btn {
      background: rgba(255,255,255,0.08) !important;
      border: 1px solid rgba(255,255,255,0.15) !important;
      min-width: 42px; text-align: center;
    }
    .qty-btn:hover { background: rgba(255,255,255,0.15) !important; }
    .recruit-input {
      width: 80px; text-align: center; padding: 8px;
      background: rgba(255,255,255,0.05); border: 2px solid rgba(255,255,255,0.15);
      border-radius: var(--radius-sm); color: var(--text-primary);
      font-size: 1rem; font-weight: bold;
    }
    .recruit-input:focus { outline: none; border-color: var(--secondary-color); }
    .recruit-btn { align-self: stretch; }

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

  // --- 加载当前数据 ---
  loadData();

  async function loadData() {
    try {
      const result = await playerAPI.getInfo(userId);
      if (result.code === 200 && result.data) {
        document.getElementById('gold-display').textContent = (result.data.gold || 0).toLocaleString();
        if (result.data.troops) {
          result.data.troops.forEach(t => {
            const el = document.getElementById(`count-${t.troopId}`);
            if (el) el.textContent = (t.count || 0).toLocaleString();
          });
        }
      }
    } catch (e) {
      console.error('加载数据失败:', e);
    }
  }

  // --- 数量加减按钮 ---
  document.querySelectorAll('.qty-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const input = btn.closest('.recruit-controls').querySelector('.recruit-input');
      const delta = parseInt(btn.dataset.delta);
      let val = parseInt(input.value) || 0;
      val = Math.max(1, val + delta);
      input.value = val;
    });
  });

  // --- 招募 ---
  document.querySelectorAll('.recruit-btn').forEach(btn => {
    btn.addEventListener('click', async () => {
      const troopId = parseInt(btn.dataset.troopId);
      const input = document.getElementById(`qty-${troopId}`);
      const count = parseInt(input.value) || 0;
      if (count <= 0) { showToast('请输入招募数量', 'error'); return; }

      const troopInfo = troops.find(t => t.troopId === troopId);
      btn.disabled = true;
      btn.textContent = '招募中...';
      try {
        const result = await hallAPI.recruit(userId, troopId, count);
        console.log('招募结果:', result);
        if (result.code === 200) {
          showToast(`招募成功！+${count} ${troopInfo.name}`, 'success');
          loadData();
        } else {
          showToast(result.message || '招募失败', 'error');
        }
      } catch (e) {
        const msg = (typeof e.message === 'string') ? e.message : '招募失败';
        showToast(msg, 'error');
      } finally {
        btn.disabled = false;
        btn.textContent = `招募${troopInfo.name}`;
      }
    });
  });

  // --- 返回 ---
  document.getElementById('back-btn').addEventListener('click', () => {
    router.navigate('/hall');
  });

  // --- Toast ---
  function showToast(msg, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = msg;
    toast.className = `toast ${type} show`;
    setTimeout(() => { toast.className = 'toast'; }, 2000);
  }
}
