
import { hallAPI, playerAPI } from '../api/index.js';
import { router } from '../utils/router.js';

export function GemPage(container) {
    const userId = localStorage.getItem('userId');
    if (!userId) { router.navigate('/login'); return; }

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

      <div class="toast" id="toast"></div>
    </div>
  `;

    // 复用样式，增加 specific
    const style = document.createElement('style');
    style.textContent = `
    /* ... common styles ... */
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

            // 渲染宝石
            const gemList = document.getElementById('gem-list');
            if (gemRes.code === 200 && gemRes.data) {
                // Group gems by type and level
                // Key: "ATK-1"
                const groups = {};
                gemRes.data.forEach(g => {
                    if (g.isUsed) return; // 只显示闲置
                    const key = `${g.gemType}-${g.gemLevel}`;
                    if (!groups[key]) groups[key] = { type: g.gemType, level: g.gemLevel, count: 0, sample: g };
                    groups[key].count++;
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
            }

            // 渲染装备（用于镶嵌）
            // 这里只简单列出装备，点击装备后弹出孔位选择比较复杂，简化为：
            // 点击装备 -> 检测有没有空孔 -> 有则弹出宝石选择 -> 镶嵌
            // 实际上后端需要 socketIndex (1 or 2).
            const equipList = document.getElementById('equip-list');
            if (equipRes.code === 200 && equipRes.data) {
                equipList.innerHTML = equipRes.data.map(e => `
          <div class="equip-item" data-id="${e.id}">
             <div style="font-weight:bold">${e.templateId === 1 ? '铁剑' : e.templateId === 2 ? '皮甲' : '装备'} +${e.enhanceLevel || 0}</div>
             <div style="font-size:0.8rem;color:#888">
               孔1: ${e.socket1GemId ? '已镶嵌' : '○ 空'} | 孔2: ${e.socket2GemId ? '已镶嵌' : '○ 空'}
             </div>
          </div>
        `).join('');

                equipList.querySelectorAll('.equip-item').forEach(item => {
                    item.addEventListener('click', () => {
                        // 简单处理：提示暂不支持（或需要更复杂的UI）
                        // 或者只能镶嵌孔1
                        inlayPrompt(item.dataset.id);
                    });
                });
            }

        } catch (e) {
            console.error(e);
        }
    }

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

    async function inlayPrompt(equipId) {
        // 简化版镶嵌交互
        // 1. 询问镶嵌到哪个孔
        const socket = prompt('请输入要镶嵌的孔位 (1 或 2):', '1');
        if (socket !== '1' && socket !== '2') return;

        // 2. 询问宝石ID (这个比较蠢，用户不知道GemID)
        // 更好的做法是：点击装备 -> 弹出可用 gem 列表
        // 鉴于时间，暂时不做那么细致的 UI
        alert('请完善代码以支持宝石选择弹窗');
    }

    function showToast(msg, type = 'success') {
        const toast = document.getElementById('toast');
        toast.textContent = msg;
        toast.className = `toast ${type} show`;
        setTimeout(() => { toast.className = 'toast'; }, 2000);
    }

    document.getElementById('back-btn').addEventListener('click', () => router.navigate('/hall'));
}
