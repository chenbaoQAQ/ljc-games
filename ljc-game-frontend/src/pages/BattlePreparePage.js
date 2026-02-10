import { hallAPI, playerAPI, battleAPI } from '../api/index.js';
import { router } from '../utils/router.js';

export function BattlePreparePage(container, params) {
    const userId = localStorage.getItem('userId');
    if (!userId) { router.navigate('/login'); return; }

    // params comes from router, e.g. { civ: 'CN', stageNo: 1 }
    const { civ, stageNo } = params;
    if (!civ || !stageNo) { router.navigate('/hall'); return; }

    // State
    let generals = [];
    let selectedGeneralId = null;
    let userTroops = [];
    let troopConfig = {}; // troopId -> count

    container.innerHTML = `
      <div class="prepare-page">
        <div class="page-header">
           <button class="btn btn-sm btn-secondary" id="back-btn">← 放弃出征</button>
           <h2>⚔️ 战前整备 - ${civ} 第${stageNo}关</h2>
        </div>
        
        <div class="prepare-content">
           <!-- 1. 选择主将 -->
           <div class="section-title">1. 选择主将</div>
           <div class="generals-list" id="generals-list">
              <div class="spinner"></div>
           </div>
           
           <!-- 2. 分配兵力 -->
           <div class="section-title">2. 分配兵力 <span id="capacity-display" style="font-size:0.9rem; margin-left:10px; color:#aaa;">(统率: --/--)</span></div>
           <div class="troops-list" id="troops-list">
              <div class="empty-tip">请先选择主将</div>
           </div>
        </div>
        
        <div class="page-footer">
           <button class="btn btn-primary btn-lg btn-block" id="start-btn" disabled>出征！</button>
        </div>
        
        <div class="toast" id="toast"></div>
      </div>
    `;

    // Styles
    const style = document.createElement('style');
    style.id = 'prepare-page-style';
    document.getElementById('prepare-page-style')?.remove();
    style.textContent = `
      .prepare-page { min-height: 100vh; background: #1a1a2e; color: #fff; display: flex; flex-direction: column; }
      .page-header { padding: 15px 20px; background: rgba(0,0,0,0.3); display: flex; align-items: center; gap: 20px; }
      .page-header h2 { margin: 0; font-size: 1.2rem; color: var(--accent-color); }
      
      .prepare-content { flex: 1; padding: 20px; max-width: 800px; margin: 0 auto; width: 100%; overflow-y: auto; }
      .section-title { font-size: 1.1rem; font-weight: bold; margin-bottom: 15px; border-left: 4px solid var(--primary-color); padding-left: 10px; }
      
      .generals-list { display: flex; gap: 15px; overflow-x: auto; padding-bottom: 10px; margin-bottom: 30px; }
      .general-card { 
         min-width: 200px; background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 8px; padding: 15px;
         cursor: pointer; transition: all 0.2s; display: flex; gap: 10px; align-items: center;
      }
      .general-card.selected { border-color: var(--accent-color); background: rgba(255,215,0,0.1); box-shadow: 0 0 10px rgba(255,215,0,0.2); }
      .g-avatar { width: 40px; height: 40px; background: var(--primary-color); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; }
      .g-name { font-weight: bold; font-size: 0.95rem; }
      .g-lv { font-size: 0.8rem; color: #aaa; margin-left: 5px; }
      .g-stats { font-size: 0.8rem; color: #ccc; margin-top: 4px; display: flex; flex-direction: column; }
      
      .troops-list { display: flex; flex-direction: column; gap: 10px; background: rgba(0,0,0,0.2); padding: 15px; border-radius: 8px; }
      .troop-row { display: flex; align-items: center; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid rgba(255,255,255,0.05); }
      .t-name { font-weight: bold; }
      .t-control { display: flex; align-items: center; gap: 5px; }
      .troop-input { width: 60px; text-align: center; padding: 5px; background: #333; border: 1px solid #555; color: #fff; border-radius: 4px; }
      .btn-tiny { padding: 2px 8px; background: #444; border: none; color: #fff; border-radius: 4px; cursor: pointer; }
      .btn-tiny:hover { background: #555; }
      
      .page-footer { padding: 20px; background: rgba(0,0,0,0.5); text-align: center; }
      .btn-block { width: 100%; max-width: 300px; padding: 12px; font-size: 1.1rem; }
      
      .empty-tip { text-align: center; color: #777; padding: 20px; }
    `;
    document.head.appendChild(style);

    init();

    async function init() {
        try {
            const [gRes, pRes] = await Promise.all([
                hallAPI.getGenerals(userId),
                playerAPI.getInfo(userId)
            ]);

            if (gRes.code === 200) {
                generals = gRes.data.filter(g => g.activated);
                renderGenerals();
            }
            if (pRes.code === 200 && pRes.data.troops) {
                userTroops = pRes.data.troops || [];
            }
        } catch (e) { console.error(e); }
    }

    function renderGenerals() {
        const list = document.getElementById('generals-list');
        if (generals.length === 0) {
            list.innerHTML = '<div class="empty-tip">没有激活的武将，请先去大厅激活</div>';
            return;
        }

        list.innerHTML = generals.map(g => `
           <div class="general-card ${selectedGeneralId === g.id ? 'selected' : ''}" data-id="${g.id}">
              <div class="g-avatar">🤴</div>
              <div class="g-info">
                 <div class="g-name">${g.name} <span class="g-lv">Lv.${g.level}</span></div>
                 <div class="g-stats">
                    <span style="color:var(--accent-color)">CAP: ${g.capacity || 0}</span>
                 </div>
              </div>
           </div>
        `).join('');

        list.querySelectorAll('.general-card').forEach(el => {
            el.addEventListener('click', () => selectGeneral(parseInt(el.dataset.id)));
        });

        // Auto select first if none selected
        if (!selectedGeneralId && generals.length > 0) {
            selectGeneral(generals[0].id);
        }
    }

    function selectGeneral(id) {
        selectedGeneralId = id;

        // Update selection visually
        document.querySelectorAll('.general-card').forEach(el => {
            el.classList.toggle('selected', parseInt(el.dataset.id) === id);
        });

        renderTroops();
        updateCapacity();
    }

    function renderTroops() {
        if (!selectedGeneralId) return;
        const list = document.getElementById('troops-list');

        if (userTroops.length === 0) {
            list.innerHTML = '<div class="empty-tip">没有兵力，请先去招募</div>';
            return;
        }

        list.innerHTML = userTroops.map(t => {
            const currentVal = troopConfig[t.troopId] || 0;
            return `
            <div class="troop-row">
               <div class="t-name">${t.name} (余:${t.totalCount})</div>
               <div class="t-control">
                  <button class="btn-tiny btn-minus" data-id="${t.troopId}">-10</button>
                  <input type="number" class="troop-input" id="input-${t.troopId}" value="${currentVal}" max="${t.totalCount}" min="0">
                  <button class="btn-tiny btn-plus" data-id="${t.troopId}">+10</button>
               </div>
            </div>
            `;
        }).join('');

        // Bind events
        userTroops.forEach(t => {
            const input = document.getElementById('input-' + t.troopId);
            if (!input) return;

            input.onchange = (e) => {
                let val = parseInt(e.target.value) || 0;
                updateConfig(t.troopId, val);
            };
        });

        list.querySelectorAll('.btn-minus').forEach(btn => {
            btn.onclick = () => {
                const id = parseInt(btn.dataset.id);
                updateConfig(id, (troopConfig[id] || 0) - 10);
            };
        });

        list.querySelectorAll('.btn-plus').forEach(btn => {
            btn.onclick = () => {
                const id = parseInt(btn.dataset.id);
                updateConfig(id, (troopConfig[id] || 0) + 10);
            };
        });

        updateCapacity();
    }

    function updateConfig(troopId, val) {
        const troop = userTroops.find(t => t.troopId === troopId);
        if (!troop) return;

        // Clamp
        val = Math.max(0, Math.min(val, troop.totalCount));
        troopConfig[troopId] = val;

        const input = document.getElementById('input-' + troopId);
        if (input) input.value = val;

        updateCapacity();
    }

    function updateCapacity() {
        const gen = generals.find(g => g.id === selectedGeneralId);
        if (!gen) return;

        const totalSoldiers = Object.values(troopConfig).reduce((a, b) => a + b, 0);
        // Rule: Total soldiers <= capacity
        const maxSoldiers = gen.capacity || 0;

        const capEl = document.getElementById('capacity-display');
        capEl.textContent = `(兵力/统率: ${totalSoldiers}/${maxSoldiers})`;

        const btn = document.getElementById('start-btn');
        const isValid = totalSoldiers > 0 && totalSoldiers <= maxSoldiers;

        btn.disabled = !isValid;

        if (totalSoldiers > maxSoldiers) {
            capEl.style.color = 'var(--danger-color)';
            btn.textContent = "兵力超过统率上限";
        } else if (totalSoldiers === 0) {
            capEl.style.color = '#aaa';
            btn.textContent = "请分配兵力";
        } else {
            capEl.style.color = 'var(--success-color)';
            btn.textContent = "出征！";
        }
    }

    document.getElementById('start-btn').onclick = async () => {
        const btn = document.getElementById('start-btn');
        btn.disabled = true;
        btn.textContent = "出征中...";
        try {
            const res = await battleAPI.startStoryBattle(userId, civ, stageNo, selectedGeneralId, troopConfig);
            if (res.code === 200) {
                // Navigate to BattlePage, which will call getBattleState to resume
                router.navigate('/battle');
            } else {
                alert(res.message || "出征失败");
                btn.disabled = false;
                btn.textContent = "出征！";
            }
        } catch (e) {
            console.error(e);
            alert("请求失败");
            btn.disabled = false;
            btn.textContent = "出征！";
        }
    };

    document.getElementById('back-btn').onclick = () => router.navigate('/stages');
}
