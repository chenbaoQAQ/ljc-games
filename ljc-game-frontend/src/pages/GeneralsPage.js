import { hallAPI, playerAPI } from '../api/index.js';
import { router } from '../utils/router.js';

export function GeneralsPage(container) {
  const userId = localStorage.getItem('userId');
  if (!userId) { router.navigate('/login'); return; }

  // 全局数据
  let allEquipments = [];
  let currentGeneralId = null;
  let currentSlot = null;

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
            <!-- 装备列表 -->
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
      display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
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

    .equip-slots {
      display: flex; gap: 10px; margin-top: 5px;
    }
    .equip-slot {
      width: 40px; height: 40px; border-radius: 4px;
      background: rgba(0,0,0,0.3); border: 1px solid rgba(255,255,255,0.1);
      display: flex; align-items: center; justify-content: center;
      font-size: 1.2rem; cursor: pointer; position: relative;
    }
    .equip-slot:hover { border-color: var(--secondary-color); }
    .equip-slot.equipped { border-color: var(--primary-color); background: rgba(255,107,53,0.1); }
    .slot-label {
      position: absolute; bottom: -14px; left: 0; width: 100%; text-align: center;
      font-size: 0.6rem; color: var(--text-secondary);
    }

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
      display: flex; gap: 8px; flex-wrap: wrap; margin-top: auto;
    }
    .gd-actions .btn { flex: 1; min-width: 80px; font-size: 0.85rem; padding: 8px 12px; }

    .status-badge {
      display: inline-block; padding: 2px 10px; border-radius: 20px;
      font-size: 0.75rem; font-weight: bold;
    }
    .status-active { background: rgba(46,204,113,0.2); color: var(--success-color); }
    .status-inactive { background: rgba(255,107,53,0.2); color: var(--primary-color); }
    .status-locked { background: rgba(255,255,255,0.08); color: var(--text-secondary); }

    /* Modal */
    .modal-overlay {
      position: fixed; top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.7); backdrop-filter: blur(5px);
      z-index: 1000; display: flex; align-items: center; justify-content: center;
    }
    .modal {
      background: var(--bg-medium); border: 1px solid rgba(255,255,255,0.1);
      border-radius: var(--radius-lg); width: 90%; max-width: 500px;
      max-height: 80vh; display: flex; flex-direction: column;
      box-shadow: 0 10px 30px rgba(0,0,0,0.5);
    }
    .modal-header {
      padding: 15px 20px; border-bottom: 1px solid rgba(255,255,255,0.1);
      display: flex; justify-content: space-between; align-items: center;
    }
    .close-btn { background: none; border: none; font-size: 1.5rem; color: #fff; cursor: pointer; }
    .modal-body {
      padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 10px;
    }
    .equip-item {
      padding: 10px; background: rgba(255,255,255,0.05); border-radius: var(--radius-md);
      display: flex; align-items: center; gap: 10px; cursor: pointer;
      border: 1px solid transparent;
    }
    .equip-item:hover { border-color: var(--secondary-color); background: rgba(255,255,255,0.1); }
    .equip-item-icon { font-size: 1.5rem; width: 40px; text-align: center; }
    .equip-item-info { flex: 1; }
    .equip-item-stats { font-size: 0.8rem; color: var(--text-secondary); }

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
      // 同时获取武将和装备
      const [generalsRes, equipRes, playerRes] = await Promise.all([
        hallAPI.getGenerals(userId),
        hallAPI.getEquipments(userId),
        playerAPI.getInfo(userId),
      ]);

      if (playerRes.code === 200 && playerRes.data) {
        document.getElementById('gold-display').textContent = (playerRes.data.gold || 0).toLocaleString();
      }

      if (equipRes.code === 200) {
        allEquipments = equipRes.data || [];
      } else {
        allEquipments = [];
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
        '<p style="text-align:center; grid-column:1/-1; color:var(--danger-color);">加载失败（请确保后端服务已更新）</p>';
    }
  }

  function renderGenerals(generals, contentEl) {
    contentEl.innerHTML = generals.map(g => {
      const isActive = g.activated;
      // 找到穿戴在当前武将身上的装备
      const weapon = allEquipments.find(e => e.generalId === g.id && e.slot === 'weapon');
      const armor = allEquipments.find(e => e.generalId === g.id && e.slot === 'armor1');

      const hpPct = g.maxHp > 0 ? Math.round((g.currentHp / g.maxHp) * 100) : 100;

      let statusHtml, statusClass;
      if (!g.unlocked) { statusHtml = '🔒 未解锁'; statusClass = 'status-locked'; }
      else if (!g.activated) { statusHtml = '⚡ 未激活'; statusClass = 'status-inactive'; }
      else { statusHtml = '✅ 已激活'; statusClass = 'status-active'; }

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
            
            ${g.activated ? `
            <div class="equip-slots">
              <div class="equip-slot ${weapon ? 'equipped' : ''}" data-slot="weapon" data-id="${g.id}">
                ${weapon ? '⚔️' : '+'}
                <div class="slot-label">武器</div>
              </div>
              <div class="equip-slot ${armor ? 'equipped' : ''}" data-slot="armor1" data-id="${g.id}">
                ${armor ? '🛡️' : '+'}
                <div class="slot-label">防具</div>
              </div>
            </div>
            ` : ''}
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

    // 绑定动作按钮
    contentEl.querySelectorAll('.action-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation(); // 防止冒泡
        handleAction(btn.dataset.action, btn.dataset.id);
      });
    });

    // 绑定装备槽点击
    contentEl.querySelectorAll('.equip-slot').forEach(slot => {
      slot.addEventListener('click', () => {
        openEquipModal(slot.dataset.id, slot.dataset.slot);
      });
    });
  }

  function openEquipModal(generalId, slot) {
    currentGeneralId = parseInt(generalId);
    currentSlot = slot;
    const modal = document.getElementById('equip-modal');
    const list = document.getElementById('equip-list');

    // 过滤可用装备: 该 Slot，且 (未穿戴 OR 穿戴在当前武将身上) 
    // 其实也可以显示别人的装备，点击就是“抢过来”
    // 这里我们显示所有符合 Slot 的装备
    // 假设 templateId -> Slot 的映射是已知的？
    // 问题：前端不知道 equipmentId 对应的 slot 是什么，除非后端返回 UserEquipmentTbl 里包含 slot 字段。
    // HallService.equip 里提到 userEquipmentMapper.update(equip) 存了 slot。
    // 所以 UserEquipmentTbl 里应该有 slot 字段。

    // 过滤出 slot 匹配的装备
    // 注意：有些闲置装备 slot 可能是 null (如果之前没穿过)。
    // 这就麻烦了。如果从未穿过，slot 是 null，我们就不知道它是 weapon 还是 armor。
    // 解决办法：我们应该根据 templateId 来判断，或者后端在发装备时就填好 slot。
    // 暂时：假设 inventory 里的装备 slot 字段可能为空，我们需要判断 templateId。
    // 简单起见，假设 templateId 1=Weapon, 2=Armor。
    // 更好的做法：allEquipments 应该包含 template 信息，或者是前端知道映射。
    // 这里先简单 hardcode: templateId=1 是 Weapon, 2=Armor1.

    const available = allEquipments.filter(e => {
      // 如果 e.slot 有值，直接用。如果没有，用 templateId 猜
      let s = e.slot;
      if (!s) {
        if (e.templateId === 1) s = 'weapon';
        if (e.templateId === 2) s = 'armor1';
      }
      return s === slot;
    });

    if (available.length === 0) {
      list.innerHTML = '<p style="text-align:center;color:var(--text-secondary)">暂无可用装备</p>';
    } else {
      list.innerHTML = available.map(e => {
        // 是否已被他人穿戴
        const isUsed = e.generalId && e.generalId !== currentGeneralId;
        const isMine = e.generalId === currentGeneralId;
        return `
        <div class="equip-item" data-eid="${e.id}">
          <div class="equip-item-icon">${slot === 'weapon' ? '⚔️' : '🛡️'}</div>
          <div class="equip-item-info">
             <div style="font-weight:bold;">装备 #${e.templateId} <span style="font-size:0.8em;color:var(--secondary-color);">+${e.enhanceLevel || 0}</span></div>
             <div class="equip-item-stats">
               ${isMine ? '<span style="color:var(--success-color)">当前装备</span>' : ''}
               ${isUsed ? '<span style="color:var(--danger-color)">他人装备 (点击抢夺)</span>' : ''}
               ${!isUsed && !isMine ? '<span style="color:var(--text-secondary)">闲置</span>' : ''}
             </div>
          </div>
        </div>
        `;
      }).join('');

      list.querySelectorAll('.equip-item').forEach(item => {
        item.addEventListener('click', () => equipItem(item.dataset.eid));
      });
    }

    modal.style.display = 'flex';
  }

  async function equipItem(equipmentId) {
    try {
      const res = await hallAPI.equipGeneral(userId, currentGeneralId, equipmentId);
      if (res.code === 200) {
        showToast('装备成功', 'success');
        document.getElementById('equip-modal').style.display = 'none';
        loadAll(); // 刷新
      } else {
        showToast(res.message || '装备失败', 'error');
      }
    } catch (e) {
      showToast(e.message || '操作失败', 'error');
    }
  }

  document.getElementById('close-modal').addEventListener('click', () => {
    document.getElementById('equip-modal').style.display = 'none';
  });

  async function handleAction(action, generalId) {
    const actionMap = {
      activate: { fn: () => hallAPI.activateGeneral(userId, generalId), label: '激活' },
      upgrade: { fn: () => hallAPI.upgradeGeneral(userId, generalId), label: '升级' },
      ascend: { fn: () => hallAPI.ascendGeneral(userId, generalId), label: '升阶' },
    };

    const { fn, label } = actionMap[action];
    try {
      const result = await fn();
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
