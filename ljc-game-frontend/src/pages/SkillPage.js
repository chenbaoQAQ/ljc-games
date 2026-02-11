
import { hallAPI, playerAPI } from '../api/index.js';
import { router } from '../utils/router.js';
import { SKILL_BOOK_META } from '../config/gameData.js';

export function SkillPage(container) {
    const userId = localStorage.getItem('userId');
    if (!userId) { router.navigate('/login'); return; }

    container.innerHTML = `
    <div class="skill-page">
      <nav class="page-nav">
        <button class="btn btn-secondary btn-sm" id="back-btn">← 返回大厅</button>
        <h1 class="page-title">📚 技能研修</h1>
        <div class="nav-gold">💰 <span id="gold-display">--</span></div>
      </nav>

      <div class="skill-content">
        <!-- 1. 选择武将 -->
        <div class="section-card card">
          <h3>👤 选择武将</h3>
          <div class="general-list-h" id="general-list">
            <div class="spinner"></div>
          </div>
        </div>

        <!-- 2. 选择技能书 -->
        <div class="section-card card" id="book-section">
          <h3>📖 技能书背包</h3>
          <div class="book-list" id="book-list">
            <p style="text-align:center;color:#888">请先选择武将</p>
          </div>
        </div>
      </div>

      <div class="toast" id="toast"></div>
    </div>
  `;

    // 样式
    const style = `
    /* Common */
    .skill-page { min-height: 100vh; background: linear-gradient(135deg, var(--bg-dark) 0%, var(--bg-medium) 100%); }
    .page-nav { padding: 15px 20px; background: rgba(0,0,0,0.3); border-bottom: 1px solid rgba(255,255,255,0.1); display: flex; align-items: center; gap: 20px; position: sticky; top: 0; z-index: 100; backdrop-filter: blur(10px); }
    .page-title { flex: 1; font-size: 1.3rem; font-weight: bold; background: linear-gradient(135deg, var(--primary-color), var(--accent-color)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    
    .skill-content { max-width: 1000px; margin: 0 auto; padding: 20px; display: flex; flex-direction: column; gap: 20px; }
    .section-card { background: rgba(255,255,255,0.05); border-radius: 8px; padding: 20px; border: 1px solid rgba(255,255,255,0.1); }
    .section-card h3 { margin-bottom: 10px; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 5px; }

    .general-list-h { display: flex; gap: 10px; overflow-x: auto; padding-bottom: 10px; }
    .g-card { min-width: 100px; background: rgba(0,0,0,0.2); border-radius: 4px; padding: 10px; text-align: center; cursor: pointer; border: 2px solid transparent; transition: all 0.2s; }
    .g-card:hover { transform: translateY(-3px); }
    .g-card.selected { border-color: var(--primary-color); background: rgba(255,107,53,0.1); }
    .g-av { width: 48px; height: 48px; background: #444; border-radius: 50%; margin: 0 auto 5px; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; }

    .book-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 15px; }
    .book-card { background: rgba(0,0,0,0.2); border-radius: 4px; padding: 10px; display: flex; align-items: center; gap: 10px; cursor: pointer; border: 1px solid transparent; }
    .book-card:hover { border-color: var(--secondary-color); }
    .book-icon { width: 40px; height: 40px; background: rgba(255,255,255,0.1); display: flex; align-items: center; justify-content: center; border-radius: 4px; font-size: 1.2rem; }
    
    .toast { position: fixed; bottom: 40px; left: 50%; transform: translateX(-50%); padding: 10px 20px; background: #333; color: #fff; border-radius: 20px; opacity: 0; pointer-events: none; transition: opacity 0.3s; }
    .toast.show { opacity: 1; }
    .toast.success { background: var(--success-color); }
    .toast.error { background: var(--danger-color); }
  `;
    const styleEl = document.createElement('style');
    styleEl.textContent = style;
    document.head.appendChild(styleEl);

    let selectedGeneralId = null;
    let generals = [];
    let items = [];

    loadData();

    async function loadData() {
        try {
            const [genRes, itemRes, playerRes] = await Promise.all([
                hallAPI.getGenerals(userId),
                hallAPI.getItems(userId),
                playerAPI.getInfo(userId)
            ]);

            if (playerRes.code === 200 && playerRes.data) {
                document.getElementById('gold-display').textContent = (playerRes.data.gold || 0).toLocaleString();
            }

            generals = (genRes.code === 200) ? genRes.data : [];
            items = (itemRes.code === 200) ? itemRes.data : [];

            renderGenerals();
            renderBooks();
        } catch (e) { console.error(e); }
    }

    function renderGenerals() {
        const list = document.getElementById('general-list');
        if (generals.length === 0) list.innerHTML = '<p>暂无武将</p>';
        else {
            list.innerHTML = generals.map(g => `
        <div class="g-card ${selectedGeneralId == g.id ? 'selected' : ''}" data-id="${g.id}">
          <div class="g-av">🎖️</div>
          <div style="font-size:0.9rem;font-weight:bold">武将#${g.templateId}</div>
          <div style="font-size:0.8rem;color:#ccc">Lv.${g.level}</div>
        </div>
      `).join('');

            list.querySelectorAll('.g-card').forEach(card => {
                card.addEventListener('click', () => {
                    selectedGeneralId = card.dataset.id;
                    renderGenerals(); // refresh selection
                    renderBooks();
                });
            });
        }
    }

    function renderBooks() {
        const list = document.getElementById('book-list');
        if (!selectedGeneralId) {
            list.innerHTML = '<p style="text-align:center;color:#888;width:100%">请先选择武将</p>';
            return;
        }

        // 过滤出技能书 (假设 itemId >= 300 是技能书 ? 或者只认 301)
        // 简单起见，所有 item 都是技能书
        const books = items.filter(i => i.count > 0 && i.itemId >= 300); // Magic number assumption

        if (books.length === 0) {
            list.innerHTML = '<p style="text-align:center;color:#888;width:100%">背包中没有技能书</p>';
        } else {
            list.innerHTML = books.map(b => `
        <div class="book-card" data-iid="${b.itemId}">
          <div class="book-icon">📚</div>
          <div>
            <div style="font-weight:bold">${SKILL_BOOK_META[b.itemId]?.name || ('技能书 #' + b.itemId)}</div>
            <div style="font-size:0.8rem;color:#888">拥有: ${b.count}</div>
          </div>
          <button class="btn btn-primary btn-sm learn-btn" data-iid="${b.itemId}">学习</button>
        </div>
      `).join('');

            list.querySelectorAll('.learn-btn').forEach(btn => {
                btn.addEventListener('click', () => learn(btn.dataset.iid));
            });
        }
    }

    async function learn(itemId) {
        if (!confirm('确认消耗一本技能书让该武将学习技能吗？(旧技能将被覆盖)')) return;
        try {
            const res = await hallAPI.learnSkill(userId, selectedGeneralId, parseInt(itemId));
            if (res.code === 200) {
                showToast('学习成功！', 'success');
                loadData(); // refresh count
            } else {
                showToast(res.message || '学习失败', 'error');
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
