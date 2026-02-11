import { hallAPI, playerAPI } from '../api/index.js';
import { router } from '../utils/router.js';

export function GeneralsPage(container) {
  const userId = localStorage.getItem('userId');
  if (!userId) { router.navigate('/login'); return; }

  let allEquipments = [];
  let currentGeneralId = null;
  let currentSlotKey = null;

  const SLOT_CONFIG = [
    { key: 'weapon', label: '武器', icon: '⚔️' },
    { key: 'armor1', label: '防具', icon: '🛡️' },
    { key: 'helm', label: '头盔', icon: '🪖' },
    { key: 'boots', label: '鞋子', icon: '👢' },
    { key: 'mount', label: '坐骑', icon: '🐎' },
    { key: 'accessory', label: '饰品', icon: '💍' },
  ];

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

      <!-- 装备选择模态框 -->
      <div class="modal-overlay" id="equip-modal" style="display:none;">
        <div class="modal">
          <div class="modal-header">
            <h3>选择装备</h3>
            <button class="close-btn" id="close-modal">×</button>
          </div>
          <div class="modal-body" id="equip-list">
          </div>
        </div>
      </div>

      <div class="toast" id="toast"></div>
    </div>
  `;

  const style = document.createElement('style');
  style.id = 'generals-page-style';
  document.getElementById('generals-page-style')?.remove();
  style.textContent = `
    .generals-page { min-height: 100vh; background: linear-gradient(135deg, var(--bg-dark) 0%, var(--bg-medium) 100%); }
    .page-nav { background: rgba(0,0,0,0.3); backdrop-filter: blur(10px); padding: 15px 20px; display: flex; align-items: center; gap: 20px; border-bottom: 2px solid rgba(255,255,255,0.1); position: sticky; top: 0; z-index: 100; }
    .page-title { flex: 1; font-size: 1.3rem; background: linear-gradient(135deg, var(--primary-color), var(--accent-color)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    .nav-gold { font-weight: bold; font-size: 1.1rem; }
    .btn-sm { padding: 6px 14px; font-size: 0.85rem; }

    .generals-content { max-width: 1000px; margin: 0 auto; padding: 20px; display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 20px; }
    
    .general-detail-card {
      background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 8px; padding: 15px;
      display: flex; flex-direction: column; gap: 12px; transition: all 0.3s;
    }
    .general-detail-card:hover { border-color: var(--secondary-color); box-shadow: 0 5px 15px rgba(0,0,0,0.3); }
    .general-detail-card.inactive { opacity: 0.6; }

    .gd-top { display: flex; align-items: center; gap: 15px; }
    .gd-avatar { width: 50px; height: 50px; border-radius: 50%; background: linear-gradient(135deg, var(--primary-color), var(--accent-color)); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; }
    .gd-info { flex: 1; }
    .gd-name { font-weight: bold; font-size: 1.1rem; }
    .gd-meta { font-size: 0.8rem; color: #aaa; }
    
    .stats-row { display: flex; justify-content: space-between; background: rgba(0,0,0,0.2); padding: 8px; border-radius: 4px; font-size: 0.85rem; }
    .stat-item { text-align: center; }
    .stat-val { display: block; font-weight: bold; color: var(--secondary-color); }
    .stat-lbl { font-size: 0.7rem; color: #888; }
    
    .skill-section { font-size: 0.85rem; background: rgba(0,0,0,0.2); padding: 8px; border-radius: 4px; border-left: 3px solid var(--accent-color); }
    .skill-title { font-weight: bold; margin-bottom: 3px; }
    .skill-desc { color: #ccc; font-size: 0.8rem; }
    
    .equip-slots { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-top: 5px; }
    .equip-slot {
      height: 50px; background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 4px;
      display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; position: relative;
    }
    .equip-slot:hover { border-color: var(--secondary-color); background: rgba(255,255,255,0.1); }
    .equip-slot.equipped { border-color: var(--success-color); background: rgba(46,204,113,0.1); }
    .slot-icon { font-size: 1.2rem; }
    .slot-label { font-size: 0.6rem; color: #aaa; position: absolute; bottom: 2px; }

    .gd-actions { display: flex; gap: 8px; margin-top: auto; }
    .action-btn { flex: 1; padding: 6px; }

    .modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.7); backdrop-filter: blur(5px); z-index: 1000; display: flex; align-items: center; justify-content: center; }
    .modal { background: var(--bg-medium); border: 1px solid rgba(255,255,255,0.1); border-radius: 8px; width: 90%; max-width: 500px; max-height: 80vh; display: flex; flex-direction: column; }
    .modal-header { padding: 15px; border-bottom: 1px solid rgba(255,255,255,0.1); display: flex; justify-content: space-between; align-items: center; }
    .close-btn { background: none; border: none; font-size: 1.5rem; color: #fff; cursor: pointer; }
    .modal-body { padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 10px; }
    .equip-item { padding: 10px; background: rgba(255,255,255,0.05); border-radius: 4px; display: flex; align-items: center; gap: 10px; cursor: pointer; }
    .equip-item:hover { border-color: var(--secondary-color); background: rgba(255,255,255,0.1); }

    .toast { position: fixed; bottom: 40px; left: 50%; transform: translateX(-50%); padding: 12px 28px; background: #333; color: #fff; border-radius: 20px; opacity: 0; transition: opacity 0.3s; pointer-events: none; }
    .toast.show { opacity: 1; }
    .toast.success { background: var(--success-color); }
    .toast.error { background: var(--danger-color); }
  `;
  document.head.appendChild(style);

  loadAll();

  async function loadAll() {
    try {
      const [generalsRes, equipRes, playerRes] = await Promise.all([
        hallAPI.getGenerals(userId),
        hallAPI.getEquipments(userId),
        playerAPI.getInfo(userId),
      ]);

      if (playerRes.code === 200 && playerRes.data) {
        document.getElementById('gold-display').textContent = (playerRes.data.gold || 0).toLocaleString();
      }

      if (equipRes.code === 200) allEquipments = equipRes.data || [];

      const contentEl = document.getElementById('generals-content');
      if (generalsRes.code === 200 && generalsRes.data && generalsRes.data.length > 0) {
        renderGenerals(generalsRes.data, contentEl);
      } else {
        contentEl.innerHTML = '<p style="text-align:center;width:100%;">暂无武将</p>';
      }
    } catch (e) {
      console.error(e);
    }
  }

  function renderGenerals(generals, contentEl) {
    contentEl.innerHTML = generals.map(g => {
      const slotsHtml = SLOT_CONFIG.map(slot => {
        const eq = allEquipments.find(e => e.generalId === g.id && e.slot === slot.key);
        return `
              <div class="equip-slot ${eq ? 'equipped' : ''}" data-slot="${slot.key}" data-id="${g.id}">
                 <div class="slot-icon">${eq ? slot.icon : '+'}</div>
                 <div class="slot-label">${slot.label}</div>
              </div>
            `;
      }).join('');

      return `
          <div class="general-detail-card ${g.activated ? '' : 'inactive'}">
            <div class="gd-top">
               <div class="gd-avatar">🎖️</div>
               <div class="gd-info">
                 <div class="gd-name">${g.name || '武将'} #${g.templateId}</div>
                 <div class="gd-meta">
                   <span>Lv.${g.level}</span> · <span>阶 ${g.tier}</span> · <span>${g.activated ? '已激活' : '未激活'}</span>
                 </div>
               </div>
            </div>
            
            <div class="stats-row">
               <div class="stat-item"><span class="stat-val">${g.currentHp}/${g.maxHp}</span><span class="stat-lbl">HP</span></div>
               <div class="stat-item"><span class="stat-val">${g.atk || 0}</span><span class="stat-lbl">攻击</span></div>
               <div class="stat-item"><span class="stat-val">${g.speed || 0}</span><span class="stat-lbl">速度</span></div>
               <div class="stat-item"><span class="stat-val">${g.capacity || 0}</span><span class="stat-lbl">统率</span></div>
            </div>

            <div class="skill-section">
               <div class="skill-title">💡 技能: ${g.skillName || '无'}</div>
               <div class="skill-desc">${g.skillDesc || '暂无效果'}</div>
            </div>

            <div class="equip-slots">
                ${g.activated ? slotsHtml : '<div style="grid-column:1/-1;text-align:center;font-size:0.8rem;padding:10px;">需激活后才可穿戴装备</div>'}
            </div>

            <div class="gd-actions">
               ${!g.activated && g.unlocked ? `<button class="btn btn-primary action-btn" data-action="activate" data-id="${g.id}">激活</button>` : ''}
               ${g.activated ? `<button class="btn btn-primary action-btn" data-action="upgrade" data-id="${g.id}">升级</button>` : ''}
               ${g.activated ? `<button class="btn btn-secondary action-btn" data-action="ascend" data-id="${g.id}">升阶</button>` : ''}
            </div>
          </div>
        `;
    }).join('');

    contentEl.querySelectorAll('.action-btn').forEach(btn =>
      btn.addEventListener('click', () => handleAction(btn.dataset.action, btn.dataset.id)));

    contentEl.querySelectorAll('.equip-slot').forEach(slot =>
      slot.addEventListener('click', () => openEquipModal(slot.dataset.id, slot.dataset.slot)));
  }

  function openEquipModal(gId, sKey) {
    currentGeneralId = parseInt(gId);
    currentSlotKey = sKey;
    const list = document.getElementById('equip-list');

    // Filter for slot match
    const available = allEquipments.filter(e => e.slot === sKey && (!e.generalId || e.generalId === currentGeneralId));

    const content = available.length ? available.map(e => `
        <div class="equip-item" data-id="${e.id}">
           <div style="font-size:1.5rem">${SLOT_CONFIG.find(s => s.key === sKey).icon}</div>
           <div>
              <div style="font-weight:bold">${e.name} +${e.enhanceLevel}</div>
              <div style="font-size:0.8rem;color:#888;">${e.generalId === currentGeneralId ? '当前装备' : (e.generalId ? '他人装备' : '闲置')}</div>
           </div>
        </div>
      `).join('') : '<p style="text-align:center;color:#888;">暂无可用装备</p>';

    list.innerHTML = content;
    list.querySelectorAll('.equip-item').forEach(it => it.addEventListener('click', () => equip(it.dataset.id)));

    document.getElementById('equip-modal').style.display = 'flex';
  }

  async function equip(eid) {
    try {
      const res = await hallAPI.equipGeneral(userId, currentGeneralId, eid);
      if (res.code === 200) { showToast('装备成功'); document.getElementById('equip-modal').style.display = 'none'; loadAll(); }
      else showToast(res.message, 'error');
    } catch (e) { showToast(e.message, 'error'); }
  }

  async function handleAction(action, id) {
    try {
      let p;
      if (action === 'activate') p = hallAPI.activateGeneral(userId, id);
      if (action === 'upgrade') p = hallAPI.upgradeGeneral(userId, id);
      if (action === 'ascend') p = hallAPI.ascendGeneral(userId, id);
      const res = await p;
      if (res.code === 200) { showToast('操作成功'); loadAll(); }
      else showToast(res.message, 'error');
    } catch (e) { showToast(e.message, 'error'); }
  }

  document.getElementById('close-modal').addEventListener('click', () => document.getElementById('equip-modal').style.display = 'none');
  document.getElementById('back-btn').addEventListener('click', () => router.navigate('/hall'));

  function showToast(msg, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = msg;
    toast.className = `toast ${type} show`;
    setTimeout(() => { toast.className = 'toast'; }, 2000);
  }
}
