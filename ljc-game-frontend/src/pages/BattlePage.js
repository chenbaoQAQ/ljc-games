import { battleAPI, hallAPI } from '../api/index.js';
import { router } from '../utils/router.js';
import { getTroopMeta, getTroopTypeName } from '../config/gameData.js';

export function BattlePage(container, params) {
    const userId = localStorage.getItem('userId');
    if (!userId) { router.navigate('/login'); return; }

    // Params: { civ, stageNo } if starting new battle OR empty if resuming
    const isNewBattle = !!params.civ;

    // State
    let battleState = null;
    let isProcessing = false;

    // UI Structure
    container.innerHTML = `
    <div class="battle-page">
      <div class="battle-header">
        <div class="turn-indicator">
            <span id="turn-display">Battle Start</span>
            <span id="phase-display" style="font-size:0.8em; margin-left:10px; background:#444; padding:2px 6px; border-radius:4px;">INIT</span>
        </div>
        <button class="btn btn-sm btn-secondary" id="flee-btn">撤退</button>
      </div>
      
      <div class="battle-field">
        <!-- 我方区域 (Side A) -->
        <div class="battle-side side-a" id="side-a">
           <div class="hero-card" id="hero-a">
              <div class="hero-avatar">🤴</div>
              <div class="hero-stats">
                 <div class="hero-name">我方主公</div>
                 <div class="hp-bar"><div class="hp-fill" style="width:100%"></div></div>
                 <div class="hp-text">--/--</div>
              </div>
           </div>
           <div class="troops-container" id="troops-a"></div>
        </div>

        <!-- VS Icon -->
        <div class="vs-divider">VS</div>

        <!-- 敌方区域 (Side B) -->
        <div class="battle-side side-b" id="side-b">
           <div class="hero-card" id="hero-b">
              <div class="hero-avatar enemy">👹</div>
              <div class="hero-stats">
                 <div class="hero-name">敌方首领</div>
                 <div class="hp-bar"><div class="hp-fill" style="width:100%"></div></div>
                 <div class="hp-text">--/--</div>
              </div>
           </div>
           <div class="troops-container" id="troops-b"></div>
        </div>
      </div>

      <!-- 战斗日志 -->
      <div class="battle-logs">
         <div class="logs-content" id="logs-content"></div>
      </div>

       <!-- 操作栏 -->
       <div class="action-bar" id="action-bar" style="flex-direction:column;gap:10px;height:auto;padding:10px;">
          <div id="tactics-ui" style="display:flex;gap:15px;color:#aaa;font-size:0.9rem;">
              <span style="color:#ffd700">战术:</span>
              <label><input type="radio" name="tactics" value="DEFAULT" checked>默认</label>
              <label><input type="radio" name="tactics" value="TARGET_INF">攻步</label>
              <label><input type="radio" name="tactics" value="TARGET_ARC">攻弓</label>
              <label><input type="radio" name="tactics" value="TARGET_CAV">攻骑</label>
          </div>
          <div class="action-status" id="action-status">正在初始化...</div>
          <div class="action-buttons" id="action-buttons" style="display:none">
             <button class="btn btn-primary" id="btn-attack">⚔️ 普通攻击</button>
             <button class="btn btn-accent" id="btn-skill">✨ 释放技能</button>
          </div>
       </div>
      
      <!-- 结算弹窗 -->
      <div class="result-modal" id="result-modal" style="display:none">
         <div class="result-content">
            <h2 id="result-title">Victory!</h2>
            <div id="result-desc">获得战利品...</div>
            <button class="btn btn-primary" id="result-ok">返回大厅</button>
         </div>
      </div>
    </div>
  `;

    // Styles
    const style = document.createElement('style');
    style.id = 'battle-page-style';
    document.getElementById('battle-page-style')?.remove();
    style.textContent = `
    .battle-page { height: 100vh; display: flex; flex-direction: column; background: #1a1a2e; color: #fff; overflow: hidden; }
    .battle-header { padding: 10px 20px; display: flex; justify-content: space-between; align-items: center; background: rgba(0,0,0,0.3); }
    .turn-indicator { font-size: 1.2rem; font-weight: bold; color: var(--accent-color); }
    
    .battle-field { flex: 1; display: flex; padding: 20px; gap: 20px; align-items: center; justify-content: center; position: relative; }
    .vs-divider { font-size: 3rem; font-weight: 900; color: rgba(255,255,255,0.1); position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); z-index: 0; }
    
    .battle-side { flex: 1; display: flex; flex-direction: column; gap: 20px; z-index: 1; max-width: 400px; }
    .side-a { align-items: flex-start; }
    .side-b { align-items: flex-end; }
    
    .hero-card { 
      display: flex; gap: 15px; padding: 15px; background: rgba(255,255,255,0.05); border: 2px solid rgba(255,255,255,0.1); border-radius: 12px; width: 100%;
      align-items: center; transition: all 0.3s;
    }
    .side-b .hero-card { flex-direction: row-reverse; text-align: right; border-color: rgba(255, 87, 87, 0.3); }
    
    .hero-avatar { width: 60px; height: 60px; border-radius: 50%; background: var(--primary-color); display: flex; align-items: center; justify-content: center; font-size: 2rem; border: 2px solid #fff; }
    .hero-avatar.enemy { background: var(--danger-color); }
    
    .hero-stats { flex: 1; }
    .hero-name { font-weight: bold; margin-bottom: 5px; }
    .hp-bar { height: 8px; background: rgba(0,0,0,0.5); border-radius: 4px; overflow: hidden; }
    .hp-fill { height: 100%; background: var(--success-color); transition: width 0.3s; }
    .hp-text { font-size: 0.8rem; color: #aaa; margin-top: 2px; }
    
    .troops-container { display: flex; flex-direction: column; gap: 10px; width: 100%; }
    .troop-unit { 
      display: flex; justify-content: space-between; padding: 8px 12px; background: rgba(0,0,0,0.2); border-radius: 6px; 
      font-size: 0.9rem; align-items: center; border-left: 3px solid #666;
    }
    .side-b .troop-unit { flex-direction: row-reverse; border-left: none; border-right: 3px solid #666; }
    
    .troop-dead { opacity: 0.3; filter: grayscale(100%); }
    .troop-active { border-color: var(--accent-color); background: rgba(255,215,0,0.1); box-shadow: 0 0 10px rgba(255,215,0,0.2); }
    
    .battle-logs { height: 150px; background: rgba(0,0,0,0.5); padding: 10px; overflow-y: auto; font-family: monospace; font-size: 0.85rem; border-top: 1px solid #333; }
    .log-entry { margin-bottom: 4px; color: #ccc; }
    .log-highlight { color: var(--accent-color); }
    .log-dmg { color: var(--danger-color); }
    
    .action-bar { height: 80px; background: #222; display: flex; align-items: center; justify-content: center; border-top: 1px solid #444; }
    .action-status { color: #888; font-size: 1.1rem; animation: pulse 1.5s infinite; }
    .action-buttons { display: flex; gap: 20px; }
    .btn-accent { background: var(--accent-color); color: #000; }
    
    @keyframes pulse { 0% { opacity: 0.6; } 50% { opacity: 1; } 100% { opacity: 0.6; } }
    
    .result-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.85); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .result-content { background: #333; padding: 40px; border-radius: 12px; text-align: center; min-width: 300px; border: 2px solid var(--accent-color); }
    #result-title { font-size: 2rem; margin-bottom: 10px; color: var(--accent-color); }
  `;
    document.head.appendChild(style);

    // Tactics
    let currentTactics = "";
    container.querySelectorAll('input[name="tactics"]').forEach(input => {
        input.addEventListener('change', (e) => currentTactics = e.target.value);
    });

    // Mod: Only resume battle. New battles are started in BattlePreparePage.
    resumeBattle();

    async function resumeBattle() {
        try {
            const res = await battleAPI.getBattleState(userId);
            if (res.code === 200) {
                battleState = res.data;
                renderBattle(battleState);
                checkTurn();
            } else {
                router.navigate('/hall'); // No active battle
            }
        } catch (e) { router.navigate('/hall'); }
    }

    function renderBattle(state) {
        if (!state) return;

        updateHero('a', state.sideA.hero);
        updateTroops('a', state.sideA.troops);
        updateHero('b', state.sideB.hero);
        updateTroops('b', state.sideB.troops);

        document.getElementById('turn-display').textContent = `Turn ${state.turnNo}`;
    }

    function updateHero(side, hero) {
        const el = document.getElementById(`hero-${side}`);
        const hpFill = el.querySelector('.hp-fill');
        const hpText = el.querySelector('.hp-text');
        const pct = Math.max(0, Math.min(100, (hero.hp / hero.maxHp) * 100));
        hpFill.style.width = pct + '%';
        hpText.textContent = `${hero.hp}/${hero.maxHp}`;
        el.querySelector('.hero-name').textContent = hero.name;

        if (hero.hp <= 0) el.style.opacity = '0.5';
    }

    function updateTroops(side, troops) {
        const container = document.getElementById(`troops-${side}`);
        const list = Array.isArray(troops) ? troops : [];
        if (list.length === 0) {
            container.innerHTML = `<div class="troop-unit troop-dead"><span>${side === 'a' ? '我方暂无出战兵力' : '敌方暂无兵力'}</span></div>`;
            return;
        }

        container.innerHTML = list.map(t => {
            const meta = getTroopMeta(t.troopId);
            const troopLabel = meta?.name || getTroopTypeName(t.type) || t.type || '未知兵种';
            return `
        <div class="troop-unit ${t.count <= 0 ? 'troop-dead' : ''}">
           <span>${getTroopIcon(t.type)} ${troopLabel}</span>
           <span>x${t.count}</span>
        </div>
      `;
        }).join('');
    }

    function getTroopIcon(type) {
        if (type === 'INF') return '🛡️';
        if (type === 'ARC') return '🏹';
        if (type === 'CAV') return '🐎';
        return '⚔️';
    }

    function checkTurn() {
        if (!battleState) return;

        // Show Phase
        const phaseEl = document.getElementById('phase-display');
        const phaseCode = battleState.phase || 'HERO_SOLO';
        if (phaseEl) {
            const phase = (phaseCode === 'TROOP_WAR') ? "🔥 全军出击" : "⚔️ 武将单挑";
            phaseEl.textContent = phase;
            phaseEl.className = (phaseCode === 'TROOP_WAR') ? "phase-badge war" : "phase-badge solo";
        }

        if (battleState.finished) {
            showResult(battleState.win);
            return;
        }

        const statusEl = document.getElementById('action-status');
        const btnGroup = document.getElementById('action-buttons');
        const btnAttack = document.getElementById('btn-attack');
        const btnSkill = document.getElementById('btn-skill');

        // Always Player Control for Next Round
        statusEl.style.display = 'none';
        btnGroup.style.display = 'flex';

        // Read direct mapped hero state from sideA.
        const hero = battleState.sideA.hero;

        btnAttack.textContent = `⚔️ ${battleState.currentTurn + 1}回合: 开始`;

        if (phaseCode === 'TROOP_WAR') {
            btnSkill.disabled = true;
            btnSkill.textContent = `阶段2不可用`;
        } else if ((hero.skillCd || 0) > 0) {
            btnSkill.disabled = true;
            btnSkill.textContent = `技能冷却 (${hero.skillCd})`;
        } else {
            btnSkill.disabled = false;
            btnSkill.textContent = `✨ 释放技能`;
        }

        btnAttack.onclick = () => doTurn(false);
        btnSkill.onclick = () => doTurn(true);
    }

    function addLogs(events) {
        if (!events) return;
        const container = document.getElementById('logs-content');

        // Process V3 Events
        events.forEach(ev => {
            const div = document.createElement('div');
            div.className = 'log-entry';
            let msg = "";

            // Map Type
            switch (ev.type) {
                case 'TURN_START':
                    msg = `<span class="log-highlight">=== 第 ${ev.turn} 回合 ===</span>`;
                    break;
                case 'PHASE_CHANGE':
                    msg = `<span class="log-highlight" style="font-size:1.1em">⚠️ 阶段切换: ${ev.fromPhase || 'HERO_SOLO'} -> ${ev.toPhase || 'TROOP_WAR'}</span>`;
                    break;
                case 'HERO_ATTACK':
                    msg = `[${ev.actorSide === 'my' ? '我方' : '敌方'}] 主将普攻`;
                    break;
                case 'HERO_SKILL':
                    msg = `[${ev.actorSide === 'my' ? '我方' : '敌方'}] 释放技能! (Dmg: ${ev.value})`;
                    break;
                case 'TROOP_ATTACK':
                    // Show Roll
                    const roll = ev.rollToHero || 0;
                    msg = `[${ev.actorSide === 'my' ? '我方' : '敌方'}] ${getTroopName(ev.attackerTroopType)} 进攻! ` +
                        `<span style="color:#ffd700">🎲Roll: ${roll}</span> ` +
                        `(${roll}% 打主将, ${100 - roll}% 打兵)`;
                    break;
                case 'HERO_HP_CHANGE':
                    const val = ev.value;
                    msg = `> [${ev.side === 'my' ? '我方' : '敌方'}] 主将 ` +
                        (val < 0 ? `<span class="log-dmg">HP ${val}</span>` : `<span class="log-heal">HP +${val}</span>`);
                    break;
                case 'TROOP_STACK_CHANGE':
                    msg = `> [${ev.side === 'my' ? '我方' : '敌方'}] ${getTroopName(ev.troopType)}: ` +
                        `<span class="log-dmg">损失 ${ev.killed} 单位</span> (Remaining: ${ev.countAfter})`;
                    break;
                default:
                    // Legacy Fallback
                    if (ev.desc) msg = ev.desc;
                    else msg = JSON.stringify(ev);
            }

            div.innerHTML = msg;
            container.prepend(div);
        });
    }

    function getTroopName(type) {
        return getTroopTypeName(type);
    }

    async function doTurn(castSkill) {
        if (isProcessing) return;
        isProcessing = true;

        // Optimistic UI Update? No, wait for response.

        // Pass Tactics
        try {
            const res = await battleAPI.processTurn(
                userId,
                castSkill,
                battleState.turnNo + 1,
                currentTactics
            );

            if (res.code === 200) {
                const newState = res.data;
                if (newState.lastEvents) addLogs(newState.lastEvents);
                battleState = newState;
                renderBattle(newState);
                isProcessing = false;
                checkTurn();
            } else {
                // idempotent retry or error?
                console.error(res.message);
                isProcessing = false;
            }
        } catch (e) {
            console.error(e);
            isProcessing = false;
        }
    }

    function showResult(win) {
        const modal = document.getElementById('result-modal');
        const title = document.getElementById('result-title');
        const desc = document.getElementById('result-desc');

        modal.style.display = 'flex';
        title.textContent = win ? 'VICTORY!' : 'DEFEAT';
        title.style.color = win ? 'var(--success-color)' : 'var(--danger-color)';
        desc.textContent = win ? '战斗胜利！获得金币与战利品。' : '战斗失败，请强化武将后再试。';

        document.getElementById('result-ok').onclick = () => {
            router.navigate('/hall');
        };
    }

    document.getElementById('flee-btn').addEventListener('click', () => {
        if (confirm("确定要撤退吗？(视为战败)")) {
            router.navigate('/hall');
        }
    });
}
