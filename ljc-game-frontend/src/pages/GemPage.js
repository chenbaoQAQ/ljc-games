import { hallAPI, playerAPI } from '../api/index.js';
import { router } from '../utils/router.js';

export function GemPage(container) {
  const userId = localStorage.getItem('userId');
  if (!userId) { router.navigate('/login'); return; }

  let allGems = []; // Store raw gem data

  container.innerHTML = `
    <div class="gem-page">
      <nav class="page-nav">
        <button class="btn btn-secondary btn-sm" id="back-btn">← 返回大厅</button>
        <h1 class="page-title">💎 宝石工坊</h1>
        <div class="nav-gold">💰 <span id="gold-display">--</span></div>
      </nav>

      <div class="gem-content">
        <!-- 宝石合成 -->
        <div class="section-card card">
          <h3>📦 宝石背包 (5合1)</h3>
          <div class="gem-list" id="gem-list">
            <div class="spinner"></div>
          </div>
        </div>

        <!-- 宝石镶嵌 -->
        <div class="section-card card">
          <h3>⚒️ 装备镶嵌</h3>
          <div class="equip-list" id="equip-list">
            <div class="spinner"></div>
          </div>
        </div>
      </div>

      <!-- 宝石选择模态框 -->
      <div class="modal-overlay" id="gem-select-modal" style="display:none;">
        <div class="modal">
          <div class="modal-header">
            <h3>选择宝石</h3>
            <button class="close-btn" id="close-modal">×</button>
          </div>
          <div class="modal-body" id="select-gem-list">
            <!-- Render gems here -->
          </div>
        </div>
      </div>

      <div class="toast" id="toast"></div>
    </div>
  `;

  // Styles...
  // 复用之前的样式，增加 modal 样式
  const style = document.createElement('style');
  style.textContent = `
    .gem-page { min-height: 100vh; background: linear-gradient(135deg, var(--bg-dark) 0%, var(--bg-medium) 100%); }
    .page-nav { background: rgba(0,0,0,0.3); backdrop-filter: blur(10px); padding: 15px 20px; display: flex; align-items: center; gap: 20px; border-bottom: 1px solid rgba(255,255,255,0.1); position: sticky; top: 0; z-index: 100; }
    .page-title { flex: 1; font-size: 1.3rem; background: linear-gradient(135deg, var(--primary-color), var(--accent-color)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; font-weight: bold; }
    .nav-gold { font-weight: bold; }
    
    .gem-content { max-width: 1000px; margin: 0 auto; padding: 20px; display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
    @media(max-width: 768px) { .gem-content { grid-template-columns: 1fr; } }
    
    .section-card { background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 8px; padding: 20px; display: flex; flex-direction: column; gap: 15px; }
    .section-card h3 { border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 10px; margin-bottom: 10px; }

    .gem-item { display: flex; align-items: center; justify-content: space-between; padding: 10px; background: rgba(0,0,0,0.2); border-radius: 4px; margin-bottom: 8px; }
    .gem-info { display: flex; align-items: center; gap: 10px; }
    .gem-icon { width: 32px; height: 32px; background: rgba(255,255,255,0.1); border-radius: 50%; display: flex; align-items: center; justify-content: center; }
    
    .equip-item { padding: 10px; background: rgba(0,0,0,0.2); border-radius: 4px; margin-bottom: 8px; cursor: pointer; border: 1px solid transparent; }
    .equip-item:hover { border-color: var(--secondary-color); }

    .btn-sm { padding: 4px 10px; font-size: 0.8rem; }
    
    /* Modal */
    .modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.7); backdrop-filter: blur(5px); z-index: 1000; display: flex; align-items: center; justify-content: center; }
    .modal { background: var(--bg-medium); border: 1px solid rgba(255,255,255,0.1); border-radius: 8px; width: 90%; max-width: 400px; max-height: 80vh; display: flex; flex-direction: column; box-shadow: 0 10px 30px rgba(0,0,0,0.5); }
    .modal-header { padding: 15px; border-bottom: 1px solid rgba(255,255,255,0.1); display: flex; justify-content: space-between; align-items: center; }
    .close-btn { background: none; border: none; font-size: 1.5rem; color: #fff; cursor: pointer; }
    .modal-body { padding: 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 10px; }
    
    .select-item { padding: 10px; background: rgba(255,255,255,0.05); border-radius: 4px; cursor: pointer; display: flex; align-items: center; gap: 10px; }
    .select-item:hover { background: rgba(255,255,255,0.1); }

    .toast { position: fixed; bottom: 40px; left: 50%; transform: translateX(-50%); padding: 12px 28px; background: #333; color: #fff; border-radius: 20px; opacity: 0; transition: opacity 0.3s; pointer-events: none; }
    .toast.show { opacity: 1; }
    .toast.success { background: var(--success-color); }
    .toast.error { background: var(--danger-color); }
  `;
  document.head.appendChild(style);

  // Load Data
  loadData();

  async function loadData() {
    try {
      const [gemRes, equipRes, playerRes] = await Promise.all([
        hallAPI.getGems(userId),
        hallAPI.getEquipments(userId),
        playerAPI.getInfo(userId)
      ]);

      if (playerRes.code === 200 && playerRes.data) {
        document.getElementById('gold-display').textContent = (playerRes.data.gold || 0).toLocaleString();
      }

      if (gemRes.code === 200) allGems = gemRes.data || [];

      // 渲染宝石 (合成)
      const gemList = document.getElementById('gem-list');
      const groups = {};
      allGems.forEach(g => {
        if (g.isUsed) return;
        const key = `${g.gemType}-${g.gemLevel}`;
        if (!groups[key]) groups[key] = { type: g.gemType, level: g.gemLevel, count: 0, ids: [] };
        groups[key].count++;
        groups[key].ids.push(g.id);
      });

      if (Object.keys(groups).length === 0) {
        gemList.innerHTML = '<p style="text-align:center;color:#888">暂无闲置宝石</p>';
      } else {
        gemList.innerHTML = Object.values(groups).map(grp => `
          <div class="gem-item">
            <div class="gem-info">
              <div class="gem-icon">💎</div>
              <div>
                <div>${grp.type} Lv.${grp.level}</div>
                <div style="font-size:0.8rem;color:#888">拥有: ${grp.count}</div>
              </div>
            </div>
            ${grp.count >= 5 ? `<button class="btn btn-primary btn-sm combine-btn" data-type="${grp.type}" data-level="${grp.level}">合成</button>` : ''}
          </div>
        `).join('');

        gemList.querySelectorAll('.combine-btn').forEach(btn => {
          btn.addEventListener('click', () => combine(btn.dataset.type, btn.dataset.level));
        });
      }

      // 渲染装备 (镶嵌)
      const equipList = document.getElementById('equip-list');
      if (equipRes.code === 200 && equipRes.data) {
        equipList.innerHTML = equipRes.data.map(e => `
          <div class="equip-item" data-id="${e.id}">
             <div style="font-weight:bold">${e.templateId === 1 ? '铁剑' : e.templateId === 2 ? '皮甲' : '装备'} +${e.enhanceLevel || 0}</div>
             <div style="font-size:0.8rem;color:#888; margin-top:5px;">
               <span class="socket-span" data-eid="${e.id}" data-idx="1" style="background:${e.socket1GemId ? 'rgba(46,204,113,0.2)' : 'rgba(255,255,255,0.1)'};padding:2px 6px;border-radius:4px;cursor:pointer;">
                 孔1: ${e.socket1GemId ? '已镶嵌' : '+ 空'}
               </span>
               <span class="socket-span" data-eid="${e.id}" data-idx="2" style="background:${e.socket2GemId ? 'rgba(46,204,113,0.2)' : 'rgba(255,255,255,0.1)'};padding:2px 6px;border-radius:4px;cursor:pointer;margin-left:5px;">
                 孔2: ${e.socket2GemId ? '已镶嵌' : '+ 空'}
               </span>
             </div>
          </div>
        `).join('');

        // 绑定孔位点击
        equipList.querySelectorAll('.socket-span').forEach(span => {
          span.addEventListener('click', (ev) => {
            ev.stopPropagation();
            // 检查是否已有宝石
            if (span.textContent.includes('已镶嵌')) {
              showToast('由于时间限制，暂不支持拆卸宝石', 'error');
            } else {
              openUseGemModal(span.dataset.eid, span.dataset.idx);
            }
          });
        });
      }

    } catch (e) { console.error(e); }
  }

  // 打开选择宝石弹窗
  let currentEquipId = null;
  let currentSocketIdx = null;

  function openUseGemModal(equipId, socketIdx) {
    currentEquipId = equipId;
    currentSocketIdx = socketIdx;

    // 过滤可用闲置宝石
    const unusedGems = allGems.filter(g => !g.isUsed);
    const list = document.getElementById('select-gem-list');

    if (unusedGems.length === 0) {
      list.innerHTML = '<p style="text-align:center;color:#888">没有闲置宝石</p>';
    } else {
      list.innerHTML = unusedGems.map(g => `
        <div class="select-item" data-gid="${g.id}">
          <div class="gem-icon" style="width:24px;height:24px;font-size:0.8rem;">💎</div>
          <div>${g.gemType} Lv.${g.gemLevel}</div>
          <div style="margin-left:auto;font-size:0.8rem;color:#aaa;">+${g.statValue}</div>
        </div>
      `).join('');

      list.querySelectorAll('.select-item').forEach(item => {
        item.addEventListener('click', () => {
          inlayGem(item.dataset.gid);
        });
      });
    }

    document.getElementById('gem-select-modal').style.display = 'flex';
  }

  async function inlayGem(gemId) {
    try {
      const res = await hallAPI.inlayGem(userId, currentEquipId, parseInt(currentSocketIdx), gemId);
      if (res.code === 200) {
        showToast('镶嵌成功！');
        document.getElementById('gem-select-modal').style.display = 'none';
        loadData();
      } else {
        showToast(res.message || '镶嵌失败', 'error');
      }
    } catch (e) {
      showToast(e.message, 'error');
    }
  }

  document.getElementById('close-modal').addEventListener('click', () => {
    document.getElementById('gem-select-modal').style.display = 'none';
  });

  async function combine(type, level) {
    try {
      const res = await hallAPI.combineGem(userId, type, parseInt(level));
      if (res.code === 200) {
        showToast('合成成功！');
        loadData();
      } else {
        showToast(res.message, 'error');
      }
    } catch (e) {
      showToast(e.message, 'error');
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
