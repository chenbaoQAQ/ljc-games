import { hallAPI, playerAPI } from '../api/index.js';
import { router } from '../utils/router.js';

export function EquipmentPage(container) {
  const userId = localStorage.getItem('userId');
  if (!userId) { router.navigate('/login'); return; }

  container.innerHTML = `
    <div class="equipment-page">
      <nav class="page-nav">
        <button class="btn btn-secondary btn-sm" id="back-btn">← 返回大厅</button>
        <h1 class="page-title">⚒️ 装备强化</h1>
        <div class="nav-gold">💰 <span id="gold-display">--</span></div>
      </nav>

      <div class="equip-content" id="equip-list">
        <div class="spinner"></div>
      </div>
      
      <div class="toast" id="toast"></div>
    </div>
  `;

  const style = '...' + // 复用之前的样式
    `
    .equipment-page {
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
    .page-title { flex: 1; font-size: 1.3rem; background: linear-gradient(135deg, var(--primary-color), var(--accent-color)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    .nav-gold { font-weight: bold; font-size: 1.1rem; }
    .btn-sm { padding: 6px 14px; font-size: 0.85rem; }

    .equip-content {
      max-width: 800px; margin: 0 auto; padding: var(--spacing-xl);
      display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
      gap: var(--spacing-lg);
    }
    
    .equip-card {
      background: rgba(255,255,255,0.05); border: 2px solid rgba(255,255,255,0.1);
      border-radius: var(--radius-md); padding: 15px;
      display: flex; flex-direction: column; gap: 10px;
      transition: all 0.3s;
    }
    .equip-card:hover { border-color: var(--secondary-color); transform: translateY(-3px); }
    
    .ec-top { display: flex; align-items: center; gap: 10px; }
    .ec-icon { width: 48px; height: 48px; background: rgba(0,0,0,0.3); border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; }
    .ec-info { flex: 1; }
    .ec-name { font-weight: bold; }
    .ec-lv { font-size: 0.8rem; color: var(--secondary-color); }
    
    .ec-stat { font-size: 0.85rem; color: var(--text-secondary); }
    .enhance-btn { margin-top: auto; width: 100%; }

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

  const styleEl = document.createElement('style');
  styleEl.textContent = style;
  document.head.appendChild(styleEl);

  loadData();

  async function loadData() {
    try {
      const [equipRes, playerRes] = await Promise.all([
        hallAPI.getEquipments(userId),
        playerAPI.getInfo(userId)
      ]);

      if (playerRes.code === 200 && playerRes.data) {
        document.getElementById('gold-display').textContent = (playerRes.data.gold || 0).toLocaleString();
      }

      const list = document.getElementById('equip-list');
      if (equipRes.code === 200 && equipRes.data && equipRes.data.length > 0) {
        list.innerHTML = equipRes.data.map(e => {
          const lv = e.enhanceLevel || 0;
          const cost = (lv + 1) * 100;
          // 简单的名字判断
          const name = (e.templateId === 1) ? '铁剑' : (e.templateId === 2) ? '皮甲' : `装备#${e.templateId}`;
          const icon = (e.templateId === 1) ? '⚔️' : '🛡️';

          return `
           <div class="equip-card">
             <div class="ec-top">
               <div class="ec-icon">${icon}</div>
               <div class="ec-info">
                 <div class="ec-name">${name} <span class="ec-lv">+${lv}</span></div>
                 <div class="ec-stat">${e.generalId ? '已穿戴' : '闲置'}</div>
               </div>
             </div>
             <div class="ec-stat">消耗: ${cost}金币</div>
             <button class="btn btn-primary enhance-btn" data-id="${e.id}" data-cost="${cost}">强化</button>
           </div>
           `;
        }).join('');

        list.querySelectorAll('.enhance-btn').forEach(btn => {
          btn.addEventListener('click', () => enhance(btn.dataset.id, btn.dataset.cost));
        });
      } else {
        list.innerHTML = '<p style="text-align:center;grid-column:1/-1;">暂无装备</p>';
      }
    } catch (e) {
      console.error(e);
    }
  }

  async function enhance(id, cost) {
    if (!confirm(`确认消耗 ${cost} 金币强化装备吗？\n(+3以上可能会失败掉级)`)) return;
    try {
      const res = await hallAPI.enhanceEquipment(userId, id);
      if (res.code === 200) {
        // 后端现在返回具体的消息（成功或失败掉级）
        const msg = res.data;
        const isFail = msg.includes("失败");
        showToast(msg, isFail ? 'error' : 'success');
        loadData();
      } else {
        showToast(res.message || '强化失败', 'error');
      }
    } catch (e) {
      showToast(e.message || '操作失败', 'error');
    }
  }

  function showToast(msg, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = msg;
    toast.className = `toast ${type} show`;
    setTimeout(() => { toast.className = 'toast'; }, 2000);
  }

  document.getElementById('back-btn').addEventListener('click', () => router.navigate('/hall'));
}
