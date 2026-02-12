import { playerAPI, hallAPI, troopAPI } from '../api/index.js';
import { router } from '../utils/router.js';
import { getTroopMeta, getTroopTypeName } from '../config/gameData.js';

export function RecruitPage(container) {
  const userId = localStorage.getItem('userId');
  if (!userId) { router.navigate('/login'); return; }

  container.innerHTML = `
    <div class="recruit-page">
      <nav class="page-nav">
        <button class="btn btn-secondary btn-sm" id="back-btn">← 返回大厅</button>
        <h1 class="page-title">招兵买马</h1>
        <div class="nav-gold">💰 <span id="gold-display">--</span></div>
      </nav>

      <div class="recruit-content" id="recruit-list">
        <div class="spinner"></div>
      </div>

      <div class="toast" id="toast"></div>
    </div>
  `;

  const style = document.createElement('style');
  style.id = 'recruit-page-style';
  document.getElementById('recruit-page-style')?.remove();
  style.textContent = `
    .recruit-page { min-height: 100vh; background: linear-gradient(135deg, var(--bg-dark) 0%, var(--bg-medium) 100%); }
    .page-nav { background: rgba(0,0,0,0.3); backdrop-filter: blur(10px); padding: var(--spacing-md) var(--spacing-lg); display:flex; align-items:center; gap: var(--spacing-lg); border-bottom: 2px solid rgba(255,255,255,0.1); position: sticky; top: 0; z-index: 100; }
    .page-title { flex: 1; font-size: 1.3rem; background: linear-gradient(135deg, var(--primary-color), var(--accent-color)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    .nav-gold { font-weight: bold; font-size: 1.1rem; }
    .recruit-content { max-width: 900px; margin: 0 auto; padding: var(--spacing-xl); display: flex; flex-direction: column; gap: var(--spacing-lg); }
    .recruit-card { display:flex; flex-direction:column; gap: var(--spacing-md); }
    .troop-header { display:flex; align-items:center; gap: var(--spacing-md); }
    .troop-avatar { width:56px; height:56px; border-radius:50%; display:flex; align-items:center; justify-content:center; font-size: 1.8rem; }
    .troop-header h3 { margin:0; font-size: 1.2rem; }
    .troop-desc { margin:4px 0 0; font-size:.85rem; color:var(--text-secondary); }
    .troop-stats { display:flex; justify-content:space-between; padding:8px 12px; background: rgba(255,255,255,.03); border-radius: var(--radius-sm); font-size:.95rem; color: var(--text-secondary); }
    .recruit-controls { display:flex; align-items:center; gap:8px; justify-content:center; }
    .qty-btn { background: rgba(255,255,255,.08)!important; border:1px solid rgba(255,255,255,.15)!important; min-width:42px; }
    .recruit-input { width:80px; text-align:center; padding:8px; background: rgba(255,255,255,.05); border:2px solid rgba(255,255,255,.15); border-radius: var(--radius-sm); color: var(--text-primary); font-size: 1rem; font-weight: bold; }
    .recruit-btn { align-self: stretch; }
    .toast { position: fixed; bottom: 40px; left: 50%; transform: translateX(-50%); padding: 12px 28px; border-radius: var(--radius-lg); font-weight: bold; opacity: 0; pointer-events: none; transition: opacity .3s ease, transform .3s ease; z-index: 999; }
    .toast.show { opacity: 1; transform: translateX(-50%) translateY(-10px); }
    .toast.success { background: var(--success-color); color:#fff; }
    .toast.error { background: var(--danger-color); color:#fff; }
  `;
  document.head.appendChild(style);

  loadData();

  async function loadData() {
    try {
      const [infoRes, codexRes] = await Promise.all([
        playerAPI.getInfo(userId),
        troopAPI.getCodex(userId),
      ]);

      const ownedTroopsMap = {};
      if (infoRes.code === 200 && infoRes.data) {
        document.getElementById('gold-display').textContent = (infoRes.data.gold || 0).toLocaleString();
        (infoRes.data.troops || []).forEach(t => { ownedTroopsMap[t.troopId] = t.count || 0; });
      }

      let codexList = [];
      if (codexRes.code === 200 && codexRes.data) {
        codexList = codexRes.data;
      }

      // Sort: Unlocked first, then by ID
      codexList.sort((a, b) => {
        if (a.status !== b.status) return b.status - a.status; // 2=Unlocked, 1=Discovered, 0=Locked
        return a.troopId - b.troopId;
      });

      renderTroops(codexList, ownedTroopsMap);
    } catch (e) {
      console.error('加载数据失败:', e);
      document.getElementById('recruit-list').innerHTML = '<p style="text-align:center">加载失败</p>';
    }
  }

  function renderTroops(troops, ownedMap) {
    const list = document.getElementById('recruit-list');
    list.innerHTML = troops.map(t => {
      // Merge with local meta for colors/icons (or backend should provide, but for now mix)
      const meta = getTroopMeta(t.troopId);
      const isLocked = t.status < 2; // 2=UNLOCKED
      const opacity = isLocked ? '0.6' : '1';
      const grayscale = isLocked ? 'filter: grayscale(1);' : '';

      return `
      <div class="recruit-card card" data-troop-id="${t.troopId}" style="${grayscale} opacity: ${opacity}">
        <div class="troop-header">
          <div class="troop-avatar" style="background:${meta.color};">${isLocked ? '🔒' : meta.icon}</div>
          <div>
            <h3>${t.name} ${t.isElite ? '<span style="font-size:.8em;color:#ffd166">[特种]</span>' : ''}</h3>
            <p class="troop-desc">${getTroopTypeName(t.type)} · ${t.civ}</p>
          </div>
        </div>
        <div class="troop-stats">
          ${isLocked
          ? `<span><span style="color:#ef4444">未解锁</span>`
          : `<span>当前: <strong>${(ownedMap[t.troopId] || 0).toLocaleString()}</strong></span>`
        }
          <span>单价: <strong>${meta.recruitGold}</strong> 金</span>
        </div>
        
        ${isLocked ? `
            <div style="font-size:0.9em; color:#ef4444; text-align:center; padding:10px; background:rgba(0,0,0,0.2); border-radius:4px;">
                ${t.unlockHint || `需通关 ${t.civ} 关卡解锁`}
            </div>
        ` : `
            <div class="recruit-controls">
              <button class="btn btn-sm qty-btn" data-delta="-10">-10</button>
              <button class="btn btn-sm qty-btn" data-delta="-1">-1</button>
              <input type="number" class="recruit-input" id="qty-${t.troopId}" value="10" min="1" />
              <button class="btn btn-sm qty-btn" data-delta="1">+1</button>
              <button class="btn btn-sm qty-btn" data-delta="10">+10</button>
            </div>
            <button class="btn btn-primary recruit-btn" data-troop-id="${t.troopId}">招募</button>
        `}
      </div>
    `}).join('');

    bindEvents();
  }

  function bindEvents() {
    document.querySelectorAll('.qty-btn').forEach(btn => {
      btn.onclick = () => {
        const input = btn.parentElement.querySelector('.recruit-input');
        const delta = parseInt(btn.dataset.delta || '0', 10);
        const val = Math.max(1, (parseInt(input.value || '1', 10) || 1) + delta);
        input.value = String(val);
      };
    });

    document.querySelectorAll('.recruit-btn').forEach(btn => {
      btn.onclick = async () => {
        const troopId = parseInt(btn.dataset.troopId || '0', 10);
        const input = document.getElementById(`qty-${troopId}`);
        const count = parseInt(input?.value || '0', 10);
        if (count <= 0) return;

        btn.disabled = true;
        try {
          const res = await hallAPI.recruit(userId, troopId, count);
          if (res.code === 200) {
            showToast('招募成功', 'success');
            loadData();
          } else {
            showToast(res.message || '招募失败', 'error');
          }
        } catch (e) {
          showToast(e.message || '请求失败', 'error');
        } finally {
          btn.disabled = false;
        }
      };
    });
  }

  document.getElementById('back-btn').addEventListener('click', () => router.navigate('/hall'));

  function showToast(msg, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = msg;
    toast.className = `toast ${type} show`;
    setTimeout(() => { toast.className = 'toast'; }, 2000);
  }
}
