(function(){const o=document.createElement("link").relList;if(o&&o.supports&&o.supports("modulepreload"))return;for(const c of document.querySelectorAll('link[rel="modulepreload"]'))v(c);new MutationObserver(c=>{for(const g of c)if(g.type==="childList")for(const p of g.addedNodes)p.tagName==="LINK"&&p.rel==="modulepreload"&&v(p)}).observe(document,{childList:!0,subtree:!0});function s(c){const g={};return c.integrity&&(g.integrity=c.integrity),c.referrerPolicy&&(g.referrerPolicy=c.referrerPolicy),c.crossOrigin==="use-credentials"?g.credentials="include":c.crossOrigin==="anonymous"?g.credentials="omit":g.credentials="same-origin",g}function v(c){if(c.ep)return;c.ep=!0;const g=s(c);fetch(c.href,g)}})();class P{constructor(){this.routes={},this.currentRoute=null}register(o,s){this.routes[o]=s}navigate(o,s={}){const v=this.routes[o];if(!v){console.error(`Route not found: ${o}`);return}this.currentRoute=o;const c=document.getElementById("app");c.innerHTML="",v(c,s)}back(){window.history.back()}}const x=new P,M="/api";class E{static async request(o,s={}){const v=`${M}${o}`,c={headers:{"Content-Type":"application/json",...s.headers},...s};try{const g=await fetch(v,c),p=g.headers.get("content-type")||"";let b;if(p.includes("application/json")?b=await g.json():b=await g.text(),!g.ok){const f=typeof b=="object"&&b.message?b.message:b;throw new Error(f||`请求失败 (${g.status})`)}return b}catch(g){throw console.error(`API Error [${o}]:`,g),g}}static get(o,s){const v=s?"?"+new URLSearchParams(s).toString():"";return this.request(o+v,{method:"GET"})}static post(o,s){return this.request(o,{method:"POST",body:s?JSON.stringify(s):void 0})}static postWithParams(o,s,v){const c=s?"?"+new URLSearchParams(s).toString():"";return this.request(o+c,{method:"POST",body:v?JSON.stringify(v):void 0})}}const S={register(d,o,s,v){return E.post("/auth/register",{username:d,password:o,nickname:s,initialCiv:v})},login(d,o){return E.post("/auth/login",{username:d,password:o})}},B={getInfo(d){return E.get("/player/info",{userId:d})},getProgress(d){return E.get("/player/progress",{userId:d})}},w={getGenerals(d){return E.get("/hall/generals",{userId:d})},getProgress(d){return E.get("/hall/progress",{userId:d})},upgradeGeneral(d,o){return E.postWithParams("/hall/general/upgrade",{userId:d,generalId:o})},activateGeneral(d,o){return E.postWithParams("/hall/general/activate",{userId:d,generalId:o})},ascendGeneral(d,o){return E.postWithParams("/hall/general/ascend",{userId:d,generalId:o})},recruit(d,o,s){return E.postWithParams("/hall/recruit",{userId:d},{troopId:o,count:s})},equipGeneral(d,o,s){return E.postWithParams("/hall/general/equip",{userId:d},{generalId:o,equipmentId:s})},enhanceEquipment(d,o){return E.postWithParams("/hall/equipment/enhance",{userId:d,equipmentId:o})},learnSkill(d,o,s){return E.postWithParams("/hall/skill/learn",{userId:d,generalId:o,bookItemId:s})},inlayGem(d,o,s,v){return E.postWithParams("/hall/gem/inlay",{userId:d},{equipmentId:o,socketIndex:s,gemId:v})},combineGem(d,o,s){return E.postWithParams("/hall/gem/combine",{userId:d},{gemType:o,level:s})},getEquipments(d){return E.get("/hall/equipments",{userId:d})},getGems(d){return E.get("/hall/gems",{userId:d})},getItems(d){return E.get("/hall/items",{userId:d})}},C={startStoryBattle(d,o,s,v,c){return E.postWithParams("/battle/story/start",{userId:d},{civ:o,stageNo:s,generalId:v,troopConfig:c})},processTurn(d,o,s){return E.postWithParams("/battle/turn",{userId:d},{castSkill:o,clientTurnNo:s})},getBattleState(d){return E.get("/battle/state",{userId:d})}},H={getTroops(d){return E.get("/troop/list",{userId:d})}};function j(d){var a;d.innerHTML=`
    <div class="login-container">
      <div class="login-card card">
        <h1 class="login-title">LJC 三国群英传</h1>
        <p class="login-subtitle">逐鹿中原，问鼎天下</p>
        
        <div class="login-form">
          <div class="form-group">
            <label>账号</label>
            <input type="text" id="username" class="form-input" placeholder="请输入账号" />
          </div>
          
          <div class="form-group">
            <label>密码</label>
            <input type="password" id="password" class="form-input" placeholder="请输入密码" />
          </div>
          
          <!-- 注册模式才显示 -->
          <div class="register-fields" id="register-fields" style="display: none;">
            <div class="form-group">
              <label>昵称</label>
              <input type="text" id="nickname" class="form-input" placeholder="取个霸气的名字" />
            </div>
            
            <div class="form-group">
              <label>选择初始阵营</label>
              <div class="civ-select" id="civ-select">
                <div class="civ-option selected" data-civ="CN">
                  <span class="civ-flag">🇨🇳</span>
                  <span class="civ-name">中国</span>
                </div>
                <div class="civ-option" data-civ="JP">
                  <span class="civ-flag">🇯🇵</span>
                  <span class="civ-name">日本</span>
                </div>
                <div class="civ-option" data-civ="KR">
                  <span class="civ-flag">🇰🇷</span>
                  <span class="civ-name">韩国</span>
                </div>
                <div class="civ-option" data-civ="GB">
                  <span class="civ-flag">🇬🇧</span>
                  <span class="civ-name">英国</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="btn-row">
            <button class="btn btn-primary btn-full" id="login-btn">登录</button>
            <button class="btn btn-secondary btn-full" id="register-btn">注册</button>
          </div>
          
          <div class="message" id="message"></div>
        </div>
      </div>
    </div>
  `;const o=document.createElement("style");o.id="login-page-style",(a=document.getElementById("login-page-style"))==null||a.remove(),o.textContent=`
    .login-container {
      min-height: 100vh;
      display: flex;
      justify-content: center;
      align-items: center;
      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
      position: relative;
      overflow: hidden;
    }
    
    .login-container::before {
      content: '';
      position: absolute;
      width: 500px;
      height: 500px;
      background: radial-gradient(circle, rgba(78, 205, 196, 0.1) 0%, transparent 70%);
      border-radius: 50%;
      top: -200px;
      right: -200px;
      animation: pulse 4s ease-in-out infinite;
    }

    .login-container::after {
      content: '';
      position: absolute;
      width: 400px;
      height: 400px;
      background: radial-gradient(circle, rgba(255, 107, 53, 0.08) 0%, transparent 70%);
      border-radius: 50%;
      bottom: -150px;
      left: -150px;
      animation: pulse 5s ease-in-out infinite reverse;
    }
    
    .login-card {
      width: 100%;
      max-width: 480px;
      padding: 40px;
      text-align: center;
      position: relative;
      z-index: 1;
    }
    
    .login-title {
      font-size: 2.5rem;
      font-weight: bold;
      background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin-bottom: var(--spacing-sm);
    }
    
    .login-subtitle {
      color: var(--text-secondary);
      font-size: 1.1rem;
      margin-bottom: var(--spacing-xl);
    }
    
    .login-form {
      display: flex;
      flex-direction: column;
      gap: var(--spacing-md);
    }
    
    .form-group {
      text-align: left;
    }
    
    .form-group label {
      display: block;
      margin-bottom: var(--spacing-sm);
      color: var(--text-secondary);
      font-size: 0.9rem;
      font-weight: 600;
    }
    
    .form-input {
      width: 100%;
      padding: 12px 16px;
      background: rgba(255, 255, 255, 0.05);
      border: 2px solid rgba(255, 255, 255, 0.1);
      border-radius: var(--radius-md);
      color: var(--text-primary);
      font-size: 1rem;
      transition: all 0.3s ease;
    }
    
    .form-input:focus {
      outline: none;
      border-color: var(--secondary-color);
      box-shadow: 0 0 0 3px rgba(78, 205, 196, 0.1);
    }

    .register-fields {
      display: flex;
      flex-direction: column;
      gap: var(--spacing-md);
      animation: slideUp 0.3s ease;
    }

    .civ-select {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: var(--spacing-sm);
    }

    .civ-option {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      padding: 12px 8px;
      background: rgba(255, 255, 255, 0.03);
      border: 2px solid rgba(255, 255, 255, 0.1);
      border-radius: var(--radius-md);
      cursor: pointer;
      transition: all 0.3s ease;
    }

    .civ-option:hover {
      border-color: rgba(255, 255, 255, 0.3);
      transform: translateY(-2px);
    }

    .civ-option.selected {
      border-color: var(--secondary-color);
      background: rgba(78, 205, 196, 0.1);
      box-shadow: 0 0 12px rgba(78, 205, 196, 0.2);
    }

    .civ-flag { font-size: 1.8rem; }

    .civ-name {
      font-size: 0.85rem;
      color: var(--text-secondary);
      font-weight: 600;
    }

    .civ-option.selected .civ-name {
      color: var(--secondary-color);
    }

    .btn-row {
      display: flex;
      gap: var(--spacing-md);
    }

    .btn-full { flex: 1; }
    
    .message {
      font-size: 0.9rem;
      min-height: 24px;
      text-align: center;
      transition: all 0.3s ease;
    }

    .message.error { color: var(--danger-color); }
    .message.success { color: var(--success-color); }
    .message.info { color: var(--secondary-color); }
  `,document.head.appendChild(o);let s=!1,v="CN";const c=document.getElementById("username"),g=document.getElementById("password"),p=document.getElementById("nickname"),b=document.getElementById("register-fields"),f=document.getElementById("login-btn"),i=document.getElementById("register-btn"),l=document.getElementById("message"),u=document.querySelectorAll(".civ-option");u.forEach(m=>{m.addEventListener("click",()=>{u.forEach(I=>I.classList.remove("selected")),m.classList.add("selected"),v=m.dataset.civ})});function r(m,I="error"){l.className=`message ${I}`,l.textContent=m}function e(m){localStorage.setItem("userId",m.id),localStorage.setItem("username",m.nickname),r("登录成功！正在进入游戏...","success"),setTimeout(()=>x.navigate("/hall"),600)}i.addEventListener("click",()=>{s=!s,b.style.display=s?"flex":"none",f.textContent=s?"确认注册":"登录",i.textContent=s?"返回登录":"注册",l.textContent=""}),f.addEventListener("click",async()=>{const m=c.value.trim(),I=g.value.trim();if(!m||!I){r("请输入账号和密码");return}f.disabled=!0,i.disabled=!0;try{if(s){const n=p.value.trim();if(!n){r("请输入昵称");return}f.textContent="注册中...";const t=await S.register(m,I,n,v);console.log("注册结果:",t),t.code===200&&t.data?e(t.data):r(t.message||"注册失败")}else{f.textContent="登录中...";const n=await S.login(m,I);console.log("登录结果:",n),n.code===200&&n.data?e(n.data):r(n.message||"登录失败")}}catch(n){r(n.message||"操作失败，请重试")}finally{f.disabled=!1,i.disabled=!1,f.textContent=s?"确认注册":"登录"}}),d.addEventListener("keypress",m=>{m.key==="Enter"&&f.click()})}function G(d){const o=localStorage.getItem("userId");if(!o){x.navigate("/login");return}d.innerHTML=`
    <div class="hall-container">
      <!-- 顶部导航栏 -->
      <nav class="hall-nav">
        <div class="nav-left">
          <h1 class="hall-title">三国群英传</h1>
          <span class="player-name" id="player-name">${localStorage.getItem("username")||"主公"}</span>
        </div>
        <div class="nav-right">
          <div class="resource-display">
            <div class="resource-item">
              <span class="resource-icon">💰</span>
              <span id="gold-count">0</span>
            </div>
            <div class="resource-item">
              <span class="resource-icon">💎</span>
              <span id="diamond-count">0</span>
            </div>
          </div>
          <button class="btn btn-secondary btn-sm" id="logout-btn">退出</button>
        </div>
      </nav>

      <!-- 主内容区 -->
      <div class="hall-content container">
        <!-- 武将卡片 -->
        <div class="section-card card">
          <h2 class="card-title">👥 武将</h2>
          <div class="generals-grid" id="generals-grid">
            <div class="spinner"></div>
          </div>
          <button class="btn btn-primary" id="manage-generals-btn">管理武将</button>
        </div>

        <!-- 兵力卡片 -->
        <div class="section-card card">
          <h2 class="card-title">⚔️ 兵力</h2>
          <div class="troops-grid" id="troops-grid">
            <div class="troop-item">
              <div class="troop-icon" style="background: var(--inf-color);">🛡️</div>
              <div class="troop-info">
                <span class="troop-name">步兵</span>
                <span class="troop-count" id="inf-count">0</span>
              </div>
            </div>
            <div class="troop-item">
              <div class="troop-icon" style="background: var(--arc-color);">🏹</div>
              <div class="troop-info">
                <span class="troop-name">弓兵</span>
                <span class="troop-count" id="arc-count">0</span>
              </div>
            </div>
            <div class="troop-item">
              <div class="troop-icon" style="background: var(--cav-color);">🐎</div>
              <div class="troop-info">
                <span class="troop-name">骑兵</span>
                <span class="troop-count" id="cav-count">0</span>
              </div>
            </div>
          </div>
          <button class="btn btn-primary" id="recruit-btn">招募士兵</button>
        </div>

        <!-- 关卡进度卡片 -->
        <div class="section-card card">
          <h2 class="card-title">🗺️ 征战四方</h2>
          <div class="progress-grid" id="progress-grid">
            <div class="spinner"></div>
          </div>
          <button class="btn btn-success" id="select-stage-btn">选择关卡</button>
        </div>

        <!-- 其他功能 -->
        <div class="section-card card">
          <h2 class="card-title">🔧 其他功能</h2>
            <button class="btn btn-secondary" id="equip-btn">装备强化</button>
            <button class="btn btn-secondary" id="gem-btn">宝石镶嵌</button>
            <button class="btn btn-secondary" id="skill-btn">技能学习</button>
            <button class="btn btn-secondary" id="tower-btn">爬塔挑战</button>
          </div>
        </div>
      </div>
    </div>
  `;const s=document.createElement("style");s.textContent=`
    .hall-container {
      min-height: 100vh;
      background: linear-gradient(135deg, var(--bg-dark) 0%, var(--bg-medium) 100%);
    }

    .hall-nav {
      background: rgba(0, 0, 0, 0.3);
      backdrop-filter: blur(10px);
      padding: var(--spacing-md) var(--spacing-lg);
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 2px solid rgba(255, 255, 255, 0.1);
      position: sticky;
      top: 0;
      z-index: 100;
    }

    .nav-left {
      display: flex;
      align-items: center;
      gap: var(--spacing-lg);
    }

    .hall-title {
      font-size: 1.5rem;
      font-weight: bold;
      background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }

    .player-name {
      color: var(--text-secondary);
      font-size: 1.1rem;
    }

    .nav-right {
      display: flex;
      align-items: center;
      gap: var(--spacing-lg);
    }

    .resource-display {
      display: flex;
      gap: var(--spacing-md);
    }

    .resource-item {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      padding: 8px 16px;
      background: rgba(255, 255, 255, 0.05);
      border-radius: var(--radius-lg);
      font-weight: bold;
    }

    .resource-icon {
      font-size: 1.2rem;
    }

    .btn-sm {
      padding: 8px 16px;
      font-size: 0.9rem;
    }

    .hall-content {
      padding: var(--spacing-xl);
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: var(--spacing-lg);
    }

    .section-card {
      height: fit-content;
    }

    .generals-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-md);
      min-height: 100px;
      align-items: center;
      justify-items: center;
    }

    .general-card {
      background: rgba(255, 255, 255, 0.05);
      border: 2px solid rgba(255, 255, 255, 0.1);
      border-radius: var(--radius-md);
      padding: var(--spacing-md);
      text-align: center;
      transition: all 0.3s ease;
      cursor: pointer;
      width: 100%;
    }

    .general-card:hover {
      transform: translateY(-5px);
      border-color: var(--secondary-color);
      box-shadow: 0 5px 15px rgba(78, 205, 196, 0.3);
    }

    .general-avatar {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      margin: 0 auto var(--spacing-sm);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 2rem;
      background: linear-gradient(135deg, var(--primary-color), var(--accent-color));
    }

    .general-name {
      font-weight: bold;
      color: var(--text-primary);
      margin-bottom: var(--spacing-xs);
    }

    .general-level {
      font-size: 0.85rem;
      color: var(--text-secondary);
    }

    .troops-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-md);
    }

    .troop-item {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      padding: var(--spacing-md);
      background: rgba(255, 255, 255, 0.05);
      border-radius: var(--radius-md);
      border: 2px solid rgba(255, 255, 255, 0.1);
      transition: all 0.3s ease;
    }

    .troop-item:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-md);
    }

    .troop-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
    }

    .troop-info {
      display: flex;
      flex-direction: column;
    }

    .troop-name {
      font-size: 0.9rem;
      color: var(--text-secondary);
    }

    .troop-count {
      font-size: 1.2rem;
      font-weight: bold;
      color: var(--text-primary);
    }

    .progress-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-md);
      min-height: 100px;
      align-items: center;
    }

    .progress-item {
      padding: var(--spacing-md);
      background: rgba(255, 255, 255, 0.05);
      border-radius: var(--radius-md);
      border: 2px solid rgba(255, 255, 255, 0.1);
    }

    .progress-civ {
      font-weight: bold;
      margin-bottom: var(--spacing-sm);
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
    }

    .progress-stage {
      font-size: 0.9rem;
      color: var(--text-secondary);
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: var(--spacing-md);
    }

    @media (max-width: 768px) {
      .hall-content {
        grid-template-columns: 1fr;
      }

      .troops-grid {
        grid-template-columns: 1fr;
      }

      .actions-grid {
        grid-template-columns: 1fr;
      }
    }
  `,document.head.appendChild(s),v(),c(),g();async function v(){try{const p=await B.getInfo(o);if(console.log("玩家数据:",p),p.code===200&&p.data){if(document.getElementById("player-name").textContent=p.data.nickname,document.getElementById("gold-count").textContent=(p.data.gold||0).toLocaleString(),document.getElementById("diamond-count").textContent=(p.data.diamond||0).toLocaleString(),p.data.troops){let b=0,f=0,i=0;p.data.troops.forEach(l=>{const u=l.troopId%100;u===1?b+=l.count||0:u===2?f+=l.count||0:u===3&&(i+=l.count||0)}),document.getElementById("inf-count").textContent=b.toLocaleString(),document.getElementById("arc-count").textContent=f.toLocaleString(),document.getElementById("cav-count").textContent=i.toLocaleString()}}else console.warn("加载玩家数据: code不为200",p)}catch(p){console.error("加载玩家数据失败:",p)}}async function c(){try{const p=await w.getGenerals(o);console.log("武将数据:",p);const b=document.getElementById("generals-grid");p.code===200&&p.data&&p.data.length>0?b.innerHTML=p.data.slice(0,4).map(f=>`
          <div class="general-card" data-id="${f.id}">
            <div class="general-avatar">🎖️</div>
            <div class="general-name">武将#${f.templateId}</div>
            <div class="general-level">Lv.${f.level}</div>
            ${f.activated?'<div class="badge badge-inf">已激活</div>':'<div class="badge" style="background:rgba(255,255,255,0.1)">未激活</div>'}
          </div>
        `).join(""):b.innerHTML='<p style="grid-column: 1/-1; text-align: center;">暂无武将</p>'}catch(p){console.error("加载武将失败:",p),document.getElementById("generals-grid").innerHTML='<p style="grid-column: 1/-1; text-align: center; color: var(--danger-color);">加载失败</p>'}}async function g(){try{const p=await w.getProgress(o);console.log("进度数据:",p);const b=document.getElementById("progress-grid");if(p.code===200&&p.data&&p.data.length>0){const f={CN:"🇨🇳 中国",JP:"🇯🇵 日本",KR:"🇰🇷 韩国",GB:"🇬🇧 英国"};b.innerHTML=p.data.map(i=>`
          <div class="progress-item">
            <div class="progress-civ">
              <span class="badge badge-${i.civ.toLowerCase()}">${f[i.civ]||i.civ}</span>
            </div>
            <div class="progress-stage">
              当前进度: ${i.maxStageCleared||0}/10 关
              ${i.unlocked?"":'<br/><span style="color: var(--danger-color);">🔒 未解锁</span>'}
            </div>
          </div>
        `).join("")}else b.innerHTML='<p style="grid-column: 1/-1; text-align: center;">暂无进度</p>'}catch(p){console.error("加载进度失败:",p)}}document.getElementById("logout-btn").addEventListener("click",()=>{localStorage.clear(),x.navigate("/login")}),document.getElementById("select-stage-btn").addEventListener("click",()=>{x.navigate("/stages")}),document.getElementById("manage-generals-btn").addEventListener("click",()=>{x.navigate("/generals")}),document.getElementById("recruit-btn").addEventListener("click",()=>{x.navigate("/recruit")}),document.getElementById("equip-btn").addEventListener("click",()=>{x.navigate("/equipment")}),document.getElementById("gem-btn").addEventListener("click",()=>{x.navigate("/gem")}),document.getElementById("skill-btn").addEventListener("click",()=>{x.navigate("/skill")}),document.getElementById("tower-btn").addEventListener("click",()=>{alert("爬塔功能敬请期待")})}function A(d){var f;const o=localStorage.getItem("userId");if(!o){x.navigate("/login");return}const s=[{troopId:2001,name:"步兵",type:"INF",icon:"🛡️",color:"var(--inf-color)",desc:"近战单位，攻守兼备",cost:20},{troopId:2002,name:"弓兵",type:"ARC",icon:"🏹",color:"var(--arc-color)",desc:"远程单位，先手攻击",cost:20},{troopId:2003,name:"骑兵",type:"CAV",icon:"🐎",color:"var(--cav-color)",desc:"速度最快，冲锋陷阵",cost:40},{troopId:3001,civ:"CN",name:"诸葛连弩(CN)",type:"ARC",icon:"🏹✨",color:"#d35400",desc:"【特种】连射弓兵，火力压制",cost:100},{troopId:3002,civ:"JP",name:"鬼武者(JP)",type:"INF",icon:"👹",color:"#8e44ad",desc:"【特种】强力近战，高暴击",cost:100},{troopId:3003,civ:"KR",name:"花郎箭手(KR)",type:"ARC",icon:"🌸",color:"#e056fd",desc:"【特种】精准射击，长射程",cost:90},{troopId:3004,civ:"GB",name:"皇家骑士(GB)",type:"CAV",icon:"💂",color:"#16a085",desc:"【特种】重装骑兵，高防御",cost:120}];d.innerHTML=`
    <div class="recruit-page">
      <nav class="page-nav">
        <button class="btn btn-secondary btn-sm" id="back-btn">← 返回大厅</button>
        <h1 class="page-title">🏕️ 招兵买马</h1>
        <div class="nav-gold">
          <span>💰 <span id="gold-display">--</span></span>
        </div>
      </nav>

      <div class="recruit-content" id="recruit-list">
        <div class="spinner"></div>
      </div>

      <div class="toast" id="toast"></div>
    </div>
  `;const v=document.createElement("style");v.id="recruit-page-style",(f=document.getElementById("recruit-page-style"))==null||f.remove(),v.textContent=`
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
    /* ... other styles ... */
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
  `,document.head.appendChild(v),c();async function c(){try{const[i,l,u]=await Promise.all([B.getInfo(o),w.getProgress(o),w.getGenerals(o)]);let r={};i.code===200&&i.data&&(document.getElementById("gold-display").textContent=(i.data.gold||0).toLocaleString(),i.data.troops&&i.data.troops.forEach(a=>r[a.troopId]=a.count));let e=["CN"];l.code===200&&l.data&&(e=l.data.filter(a=>a.unlocked).map(a=>a.civ)),g(e,r)}catch(i){console.error("加载数据失败:",i),document.getElementById("recruit-list").innerHTML='<p style="text-align:center">加载失败</p>'}}function g(i,l){const u=document.getElementById("recruit-list"),r=s.filter(e=>!e.civ||i.includes(e.civ));u.innerHTML=r.map(e=>`
        <div class="recruit-card card" data-troop-id="${e.troopId}">
          <div class="troop-header">
            <div class="troop-avatar" style="background: ${e.color};">${e.icon}</div>
            <div>
              <h3>${e.name} ${e.civ?'<span style="font-size:0.8em;color:#aaa">['+e.civ+"]</span>":""}</h3>
              <p class="troop-desc">${e.desc}</p>
            </div>
          </div>
          <div class="troop-stats">
            <span>当前拥有: <strong>${(l[e.troopId]||0).toLocaleString()}</strong></span>
            <span>单价: <strong>${e.cost}</strong> 金</span>
          </div>
          <div class="recruit-controls">
            <button class="btn btn-sm qty-btn" data-delta="-10">-10</button>
            <button class="btn btn-sm qty-btn" data-delta="-1">-1</button>
            <input type="number" class="recruit-input" id="qty-${e.troopId}" value="10" min="1" />
            <button class="btn btn-sm qty-btn" data-delta="1">+1</button>
            <button class="btn btn-sm qty-btn" data-delta="10">+10</button>
          </div>
          <button class="btn btn-primary recruit-btn" data-troop-id="${e.troopId}">招募${e.name}</button>
        </div>
      `).join(""),p()}function p(){document.querySelectorAll(".qty-btn").forEach(i=>{i.onclick=()=>{const l=i.parentElement.querySelector(".recruit-input"),u=parseInt(i.dataset.delta);let r=parseInt(l.value)||0;r=Math.max(1,r+u),l.value=r}}),document.querySelectorAll(".recruit-btn").forEach(i=>{i.onclick=async()=>{const l=parseInt(i.dataset.troopId),u=document.getElementById(`qty-${l}`),r=parseInt(u.value)||0;if(!(r<=0)){i.disabled=!0;try{const e=await w.recruit(o,l,r);e.code===200?(b("招募成功","success"),c()):b(e.message||"失败","error")}catch(e){b(e.message,"error")}finally{i.disabled=!1}}}})}document.getElementById("back-btn").addEventListener("click",()=>{x.navigate("/hall")});function b(i,l="success"){const u=document.getElementById("toast");u.textContent=i,u.className=`toast ${l} show`,setTimeout(()=>{u.className="toast"},2e3)}}function R(d){const o=localStorage.getItem("userId");if(!o){x.navigate("/login");return}let s=[],v=null;const c=[{key:"weapon",label:"武器",icon:"⚔️"},{key:"armor1",label:"防具",icon:"🛡️"},{key:"helm",label:"头盔",icon:"🪖"},{key:"boots",label:"鞋子",icon:"👢"},{key:"mount",label:"坐骑",icon:"🐎"},{key:"accessory",label:"饰品",icon:"💍"}];d.innerHTML=`
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
  `;const g=document.createElement("style");g.textContent=`
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
  `,document.head.appendChild(g),p();async function p(){try{const[r,e,a]=await Promise.all([w.getGenerals(o),w.getEquipments(o),B.getInfo(o)]);a.code===200&&a.data&&(document.getElementById("gold-display").textContent=(a.data.gold||0).toLocaleString()),e.code===200&&(s=e.data||[]);const m=document.getElementById("generals-content");r.code===200&&r.data&&r.data.length>0?b(r.data,m):m.innerHTML='<p style="text-align:center;width:100%;">暂无武将</p>'}catch(r){console.error(r)}}function b(r,e){e.innerHTML=r.map(a=>{const m=c.map(I=>{const n=s.find(t=>t.generalId===a.id&&t.slot===I.key);return`
              <div class="equip-slot ${n?"equipped":""}" data-slot="${I.key}" data-id="${a.id}">
                 <div class="slot-icon">${n?I.icon:"+"}</div>
                 <div class="slot-label">${I.label}</div>
              </div>
            `}).join("");return`
          <div class="general-detail-card ${a.activated?"":"inactive"}">
            <div class="gd-top">
               <div class="gd-avatar">🎖️</div>
               <div class="gd-info">
                 <div class="gd-name">${a.name||"武将"} #${a.templateId}</div>
                 <div class="gd-meta">
                   <span>Lv.${a.level}</span> · <span>阶 ${a.tier}</span> · <span>${a.activated?"已激活":"未激活"}</span>
                 </div>
               </div>
            </div>
            
            <div class="stats-row">
               <div class="stat-item"><span class="stat-val">${a.currentHp}/${a.maxHp}</span><span class="stat-lbl">HP</span></div>
               <div class="stat-item"><span class="stat-val">${a.atk||0}</span><span class="stat-lbl">攻击</span></div>
               <div class="stat-item"><span class="stat-val">${a.capacity||0}</span><span class="stat-lbl">统率</span></div>
            </div>

            <div class="skill-section">
               <div class="skill-title">💡 技能: ${a.skillName||"无"}</div>
               <div class="skill-desc">${a.skillDesc||"暂无效果"}</div>
            </div>

            <div class="equip-slots">
                ${a.activated?m:'<div style="grid-column:1/-1;text-align:center;font-size:0.8rem;padding:10px;">需激活后才可穿戴装备</div>'}
            </div>

            <div class="gd-actions">
               ${!a.activated&&a.unlocked?`<button class="btn btn-primary action-btn" data-action="activate" data-id="${a.id}">激活</button>`:""}
               ${a.activated?`<button class="btn btn-primary action-btn" data-action="upgrade" data-id="${a.id}">升级</button>`:""}
               ${a.activated?`<button class="btn btn-secondary action-btn" data-action="ascend" data-id="${a.id}">升阶</button>`:""}
            </div>
          </div>
        `}).join(""),e.querySelectorAll(".action-btn").forEach(a=>a.addEventListener("click",()=>l(a.dataset.action,a.dataset.id))),e.querySelectorAll(".equip-slot").forEach(a=>a.addEventListener("click",()=>f(a.dataset.id,a.dataset.slot)))}function f(r,e){v=parseInt(r);const a=document.getElementById("equip-list"),m=s.filter(n=>n.slot===e),I=m.length?m.map(n=>`
        <div class="equip-item" data-id="${n.id}">
           <div style="font-size:1.5rem">${c.find(t=>t.key===e).icon}</div>
           <div>
              <div style="font-weight:bold">${n.name} +${n.enhanceLevel}</div>
              <div style="font-size:0.8rem;color:#888;">${n.generalId===v?"当前装备":n.generalId?"他人装备":"闲置"}</div>
           </div>
        </div>
      `).join(""):'<p style="text-align:center;color:#888;">暂无可用装备</p>';a.innerHTML=I,a.querySelectorAll(".equip-item").forEach(n=>n.addEventListener("click",()=>i(n.dataset.id))),document.getElementById("equip-modal").style.display="flex"}async function i(r){try{const e=await w.equipGeneral(o,v,r);e.code===200?(u("装备成功"),document.getElementById("equip-modal").style.display="none",p()):u(e.message,"error")}catch(e){u(e.message,"error")}}async function l(r,e){try{let a;r==="activate"&&(a=w.activateGeneral(o,e)),r==="upgrade"&&(a=w.upgradeGeneral(o,e)),r==="ascend"&&(a=w.ascendGeneral(o,e));const m=await a;m.code===200?(u("操作成功"),p()):u(m.message,"error")}catch(a){u(a.message,"error")}}document.getElementById("close-modal").addEventListener("click",()=>document.getElementById("equip-modal").style.display="none"),document.getElementById("back-btn").addEventListener("click",()=>x.navigate("/hall"));function u(r,e="success"){const a=document.getElementById("toast");a.textContent=r,a.className=`toast ${e} show`,setTimeout(()=>{a.className="toast"},2e3)}}function N(d){var f;const o=localStorage.getItem("userId");if(!o){x.navigate("/login");return}d.innerHTML=`
    <div class="stage-page">
      <nav class="page-nav">
        <button class="btn btn-secondary btn-sm" id="back-btn">← 返回大厅</button>
        <h1 class="page-title">🗺️ 征战四方</h1>
        <div class="nav-gold">💰 <span id="gold-display">--</span></div>
      </nav>

      <div class="stage-content">
        <!-- 阵营切换 Tab -->
        <div class="civ-tabs" id="civ-tabs">
          <button class="civ-tab active" data-civ="CN">🇨🇳 中国</button>
          <button class="civ-tab" data-civ="JP">🇯🇵 日本</button>
          <button class="civ-tab" data-civ="KR">🇰🇷 韩国</button>
          <button class="civ-tab" data-civ="GB">🇬🇧 英国</button>
        </div>

        <!-- 关卡列表容器 -->
        <div class="stage-grid" id="stage-grid">
          <div class="spinner"></div>
        </div>
      </div>

      <div class="toast" id="toast"></div>
    </div>
  `;const s=document.createElement("style");s.id="stage-page-style",(f=document.getElementById("stage-page-style"))==null||f.remove(),s.textContent=`
    .stage-page {
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

    .stage-content {
      max-width: 1000px; margin: 0 auto; padding: var(--spacing-xl);
      display: flex; flex-direction: column; gap: var(--spacing-lg);
    }

    .civ-tabs {
      display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 10px;
    }
    .civ-tab {
      flex: 1;
      padding: 12px;
      background: rgba(255,255,255,0.05);
      border: 1px solid rgba(255,255,255,0.1);
      border-radius: var(--radius-md);
      color: var(--text-secondary);
      font-weight: bold; cursor: pointer;
      transition: all 0.2s ease;
    }
    .civ-tab:hover { background: rgba(255,255,255,0.1); }
    .civ-tab.active {
      background: rgba(78, 205, 196, 0.15);
      border-color: var(--secondary-color);
      color: var(--secondary-color);
      box-shadow: 0 0 10px rgba(78, 205, 196, 0.2);
    }

    .stage-grid {
      display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 20px;
    }

    .stage-card {
      position: relative;
      background: rgba(255,255,255,0.03);
      border: 2px solid rgba(255,255,255,0.08);
      border-radius: var(--radius-lg);
      padding: 20px;
      text-align: center;
      transition: all 0.3s ease;
      cursor: pointer;
      overflow: hidden;
    }
    
    .stage-card:hover { transform: translateY(-3px); }

    /* 解锁状态 */
    .stage-card.unlocked {
      border-color: rgba(255,255,255,0.3);
      background: rgba(255,255,255,0.08);
    }
    .stage-card.unlocked:hover {
      border-color: var(--primary-color);
      box-shadow: 0 5px 15px rgba(255, 107, 53, 0.2);
    }

    /* 已通关状态 */
    .stage-card.cleared {
      border-color: var(--success-color);
      background: rgba(46, 204, 113, 0.1);
    }
    .stage-card.cleared::after {
      content: '✅'; position: absolute; top: 5px; right: 5px; font-size: 1.2rem;
    }

    /* 锁定状态 */
    .stage-card.locked {
      opacity: 0.5; cursor: not-allowed;
      border-style: dashed;
    }

    .stage-num {
      font-size: 2.5rem; font-weight: bold; opacity: 0.2;
      position: absolute; bottom: -5px; right: 10px;
      line-height: 1; pointer-events: none;
    }

    .stage-name {
      font-size: 1.2rem; font-weight: bold; margin-bottom: 8px; z-index: 1; position: relative;
    }
    .stage-desc {
      font-size: 0.85rem; color: var(--text-secondary); z-index: 1; position: relative;
    }

    .lock-icon { font-size: 2rem; margin-bottom: 10px; }

    .toast {
      position: fixed; bottom: 40px; left: 50%; transform: translateX(-50%);
      padding: 12px 28px; border-radius: var(--radius-lg);
      font-weight: bold; font-size: 0.95rem;
      opacity: 0; pointer-events: none;
      transition: opacity 0.3s ease, transform 0.3s ease; z-index: 999;
    }
    .toast.show { opacity: 1; transform: translateX(-50%) translateY(-10px); }
  `,document.head.appendChild(s);let v="CN",c=[];g();async function g(){document.querySelectorAll(".civ-tab").forEach(i=>{i.addEventListener("click",()=>{document.querySelectorAll(".civ-tab").forEach(l=>l.classList.remove("active")),i.classList.add("active"),v=i.dataset.civ,p()})}),document.getElementById("back-btn").addEventListener("click",()=>{x.navigate("/hall")});try{const[i,l]=await Promise.all([w.getProgress(o),B.getInfo(o)]);l.code===200&&l.data&&(document.getElementById("gold-display").textContent=(l.data.gold||0).toLocaleString()),i.code===200&&i.data&&(c=i.data,p())}catch(i){console.error("加载失败",i),document.getElementById("stage-grid").innerHTML="<p>加载失败，请重试</p>"}}function p(){const i=document.getElementById("stage-grid");i.innerHTML="";const l=c.find(e=>e.civ===v);if(!l||!l.unlocked){i.innerHTML=`
        <div style="grid-column:1/-1; text-align:center; padding: 40px;">
          <h2>🔒 该势力尚未解锁</h2>
          <p style="color:var(--text-secondary)">请先通关前置势力的主线关卡</p>
        </div>
      `;return}const u=10,r=l.maxStageCleared||0;for(let e=1;e<=u;e++){const a=e<=r,m=e<=r+1,I=document.createElement("div");I.className=`stage-card ${a?"cleared":""} ${m?"unlocked":"locked"}`;let n="普通";(e===5||e===9)&&(n="🏰 攻城"),e===10&&(n="👹 BOSS"),I.innerHTML=`
        ${m?"":'<div class="lock-icon">🔒</div>'}
        <div class="stage-name">第 ${e} 关</div>
        <div class="stage-desc">${n}</div>
        <div class="stage-num">${e}</div>
      `,m&&I.addEventListener("click",()=>{b(v,e)}),i.appendChild(I)}}function b(i,l){x.navigate("/battle/prepare",{civ:i,stageNo:l})}}function O(d){const o=localStorage.getItem("userId");if(!o){x.navigate("/login");return}d.innerHTML=`
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
  `;const s=`...
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
  `,v=document.createElement("style");v.textContent=s,document.head.appendChild(v),c();async function c(){try{const[b,f]=await Promise.all([w.getEquipments(o),B.getInfo(o)]);f.code===200&&f.data&&(document.getElementById("gold-display").textContent=(f.data.gold||0).toLocaleString());const i=document.getElementById("equip-list");b.code===200&&b.data&&b.data.length>0?(i.innerHTML=b.data.map(l=>{const u=l.enhanceLevel||0,r=(u+1)*100,e=l.templateId===1?"铁剑":l.templateId===2?"皮甲":`装备#${l.templateId}`;return`
           <div class="equip-card">
             <div class="ec-top">
               <div class="ec-icon">${l.templateId===1?"⚔️":"🛡️"}</div>
               <div class="ec-info">
                 <div class="ec-name">${e} <span class="ec-lv">+${u}</span></div>
                 <div class="ec-stat">${l.generalId?"已穿戴":"闲置"}</div>
               </div>
             </div>
             <div class="ec-stat">消耗: ${r}金币</div>
             <button class="btn btn-primary enhance-btn" data-id="${l.id}" data-cost="${r}">强化</button>
           </div>
           `}).join(""),i.querySelectorAll(".enhance-btn").forEach(l=>{l.addEventListener("click",()=>g(l.dataset.id,l.dataset.cost))})):i.innerHTML='<p style="text-align:center;grid-column:1/-1;">暂无装备</p>'}catch(b){console.error(b)}}async function g(b,f){if(confirm(`确认消耗 ${f} 金币强化装备吗？
(+3以上可能会失败掉级)`))try{const i=await w.enhanceEquipment(o,b);if(i.code===200){const l=i.data,u=l.includes("失败");p(l,u?"error":"success"),c()}else p(i.message||"强化失败","error")}catch(i){p(i.message||"操作失败","error")}}function p(b,f="success"){const i=document.getElementById("toast");i.textContent=b,i.className=`toast ${f} show`,setTimeout(()=>{i.className="toast"},2e3)}document.getElementById("back-btn").addEventListener("click",()=>x.navigate("/hall"))}function W(d){const o=localStorage.getItem("userId");if(!o){x.navigate("/login");return}let s=[];d.innerHTML=`
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
  `;const v=document.createElement("style");v.textContent=`
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
  `,document.head.appendChild(v),c();async function c(){try{const[u,r,e]=await Promise.all([w.getGems(o),w.getEquipments(o),B.getInfo(o)]);e.code===200&&e.data&&(document.getElementById("gold-display").textContent=(e.data.gold||0).toLocaleString()),u.code===200&&(s=u.data||[]);const a=document.getElementById("gem-list"),m={};s.forEach(n=>{if(n.isUsed)return;const t=`${n.gemType}-${n.gemLevel}`;m[t]||(m[t]={type:n.gemType,level:n.gemLevel,count:0,ids:[]}),m[t].count++,m[t].ids.push(n.id)}),Object.keys(m).length===0?a.innerHTML='<p style="text-align:center;color:#888">暂无闲置宝石</p>':(a.innerHTML=Object.values(m).map(n=>`
          <div class="gem-item">
            <div class="gem-info">
              <div class="gem-icon">💎</div>
              <div>
                <div>${n.type} Lv.${n.level}</div>
                <div style="font-size:0.8rem;color:#888">拥有: ${n.count}</div>
              </div>
            </div>
            ${n.count>=5?`<button class="btn btn-primary btn-sm combine-btn" data-type="${n.type}" data-level="${n.level}">合成</button>`:""}
          </div>
        `).join(""),a.querySelectorAll(".combine-btn").forEach(n=>{n.addEventListener("click",()=>i(n.dataset.type,n.dataset.level))}));const I=document.getElementById("equip-list");r.code===200&&r.data&&(I.innerHTML=r.data.map(n=>`
          <div class="equip-item" data-id="${n.id}">
             <div style="font-weight:bold">${n.templateId===1?"铁剑":n.templateId===2?"皮甲":"装备"} +${n.enhanceLevel||0}</div>
             <div style="font-size:0.8rem;color:#888; margin-top:5px;">
               <span class="socket-span" data-eid="${n.id}" data-idx="1" style="background:${n.socket1GemId?"rgba(46,204,113,0.2)":"rgba(255,255,255,0.1)"};padding:2px 6px;border-radius:4px;cursor:pointer;">
                 孔1: ${n.socket1GemId?"已镶嵌":"+ 空"}
               </span>
               <span class="socket-span" data-eid="${n.id}" data-idx="2" style="background:${n.socket2GemId?"rgba(46,204,113,0.2)":"rgba(255,255,255,0.1)"};padding:2px 6px;border-radius:4px;cursor:pointer;margin-left:5px;">
                 孔2: ${n.socket2GemId?"已镶嵌":"+ 空"}
               </span>
             </div>
          </div>
        `).join(""),I.querySelectorAll(".socket-span").forEach(n=>{n.addEventListener("click",t=>{t.stopPropagation(),n.textContent.includes("已镶嵌")?l("由于时间限制，暂不支持拆卸宝石","error"):b(n.dataset.eid,n.dataset.idx)})}))}catch(u){console.error(u)}}let g=null,p=null;function b(u,r){g=u,p=r;const e=s.filter(m=>!m.isUsed),a=document.getElementById("select-gem-list");e.length===0?a.innerHTML='<p style="text-align:center;color:#888">没有闲置宝石</p>':(a.innerHTML=e.map(m=>`
        <div class="select-item" data-gid="${m.id}">
          <div class="gem-icon" style="width:24px;height:24px;font-size:0.8rem;">💎</div>
          <div>${m.gemType} Lv.${m.gemLevel}</div>
          <div style="margin-left:auto;font-size:0.8rem;color:#aaa;">+${m.statValue}</div>
        </div>
      `).join(""),a.querySelectorAll(".select-item").forEach(m=>{m.addEventListener("click",()=>{f(m.dataset.gid)})})),document.getElementById("gem-select-modal").style.display="flex"}async function f(u){try{const r=await w.inlayGem(o,g,parseInt(p),u);r.code===200?(l("镶嵌成功！"),document.getElementById("gem-select-modal").style.display="none",c()):l(r.message||"镶嵌失败","error")}catch(r){l(r.message,"error")}}document.getElementById("close-modal").addEventListener("click",()=>{document.getElementById("gem-select-modal").style.display="none"});async function i(u,r){try{const e=await w.combineGem(o,u,parseInt(r));e.code===200?(l("合成成功！"),c()):l(e.message,"error")}catch(e){l(e.message,"error")}}function l(u,r="success"){const e=document.getElementById("toast");e.textContent=u,e.className=`toast ${r} show`,setTimeout(()=>{e.className="toast"},2e3)}document.getElementById("back-btn").addEventListener("click",()=>x.navigate("/hall"))}function D(d){const o=localStorage.getItem("userId");if(!o){x.navigate("/login");return}d.innerHTML=`
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
  `;const s=`
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
  `,v=document.createElement("style");v.textContent=s,document.head.appendChild(v);let c=null,g=[],p=[];b();async function b(){try{const[r,e,a]=await Promise.all([w.getGenerals(o),w.getItems(o),B.getInfo(o)]);a.code===200&&a.data&&(document.getElementById("gold-display").textContent=(a.data.gold||0).toLocaleString()),g=r.code===200?r.data:[],p=e.code===200?e.data:[],f(),i()}catch(r){console.error(r)}}function f(){const r=document.getElementById("general-list");g.length===0?r.innerHTML="<p>暂无武将</p>":(r.innerHTML=g.map(e=>`
        <div class="g-card ${c==e.id?"selected":""}" data-id="${e.id}">
          <div class="g-av">🎖️</div>
          <div style="font-size:0.9rem;font-weight:bold">武将#${e.templateId}</div>
          <div style="font-size:0.8rem;color:#ccc">Lv.${e.level}</div>
        </div>
      `).join(""),r.querySelectorAll(".g-card").forEach(e=>{e.addEventListener("click",()=>{c=e.dataset.id,f(),i()})}))}function i(){const r=document.getElementById("book-list");if(!c){r.innerHTML='<p style="text-align:center;color:#888;width:100%">请先选择武将</p>';return}const e=p.filter(a=>a.count>0&&a.itemId>=300);e.length===0?r.innerHTML='<p style="text-align:center;color:#888;width:100%">背包中没有技能书</p>':(r.innerHTML=e.map(a=>`
        <div class="book-card" data-iid="${a.itemId}">
          <div class="book-icon">📚</div>
          <div>
            <div style="font-weight:bold">技能书 #${a.itemId}</div>
            <div style="font-size:0.8rem;color:#888">拥有: ${a.count}</div>
          </div>
          <button class="btn btn-primary btn-sm learn-btn" data-iid="${a.itemId}">学习</button>
        </div>
      `).join(""),r.querySelectorAll(".learn-btn").forEach(a=>{a.addEventListener("click",()=>l(a.dataset.iid))}))}async function l(r){if(confirm("确认消耗一本技能书让该武将学习技能吗？(旧技能将被覆盖)"))try{const e=await w.learnSkill(o,c,parseInt(r));e.code===200?(u("学习成功！","success"),b()):u(e.message||"学习失败","error")}catch(e){u(e.message,"error")}}function u(r,e="success"){const a=document.getElementById("toast");a.textContent=r,a.className=`toast ${e} show`,setTimeout(()=>{a.className="toast"},2e3)}document.getElementById("back-btn").addEventListener("click",()=>x.navigate("/hall"))}function F(d,o){var n;const s=localStorage.getItem("userId");if(!s){x.navigate("/login");return}const v=!!o.civ;let c=null,g=!1;d.innerHTML=`
    <div class="battle-page">
      <div class="battle-header">
        <div class="turn-indicator" id="turn-display">Battle Start</div>
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
      <div class="action-bar" id="action-bar">
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
  `;const p=document.createElement("style");p.id="battle-page-style",(n=document.getElementById("battle-page-style"))==null||n.remove(),p.textContent=`
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
  `,document.head.appendChild(p),v?b(o):f();async function b({civ:t,stageNo:y}){var h;try{const $=(h=(await w.getGenerals(s)).data)==null?void 0:h.find(q=>q.activated);if(!$){alert("无出战武将"),x.navigate("/hall");return}const L=await H.getTroops(s),z={};L.data&&L.data.forEach(q=>z[q.troopId]=Math.min(q.totalCount,100));const T=await C.startStoryBattle(s,t,y,$.id,z);T.code===200?(c=T.data.context,i(c),a()):(alert("战斗启动失败: "+T.message),x.navigate("/hall"))}catch(k){console.error(k)}}async function f(){try{const t=await C.getBattleState(s);t.code===200?(c=t.data,i(c),a()):x.navigate("/hall")}catch{x.navigate("/hall")}}function i(t){t&&(l("a",t.sideA.hero),u("a",t.sideA.troops),l("b",t.sideB.hero),u("b",t.sideB.troops),document.getElementById("turn-display").textContent=`Turn ${t.turnNo}`)}function l(t,y){const h=document.getElementById(`hero-${t}`),k=h.querySelector(".hp-fill"),$=h.querySelector(".hp-text"),L=Math.max(0,Math.min(100,y.hp/y.maxHp*100));k.style.width=L+"%",$.textContent=`${y.hp}/${y.maxHp}`,h.querySelector(".hero-name").textContent=y.name,y.hp<=0&&(h.style.opacity="0.5")}function u(t,y){const h=document.getElementById(`troops-${t}`);h.innerHTML=y.map(k=>`
        <div class="troop-unit ${k.count<=0?"troop-dead":""}">
           <span>${r(k.type)} ${k.name||k.type}</span>
           <span>x${k.count}</span>
        </div>
      `).join("")}function r(t){return t==="INF"?"🛡️":t==="ARC"?"🏹":t==="CAV"?"🐎":"⚔️"}function e(t){if(!t)return;const y=document.getElementById("logs-content");t.forEach(h=>{const k=document.createElement("div");k.className="log-entry";let $=`[${h.source}] used ${h.action}`;h.value>0?$+=` caused <span class="log-dmg">${h.value}</span> dmg`:$+=` -> ${h.desc}`,k.innerHTML=$,y.prepend(k)})}function a(){if(!c)return;if(c.isFinished){I(c.isWin);return}const t=c.nextActorDesc==="HeroA",y=document.getElementById("action-status"),h=document.getElementById("action-buttons");if(t)y.style.display="none",h.style.display="flex",document.getElementById("btn-attack").onclick=()=>m(!1),document.getElementById("btn-skill").onclick=()=>m(!0);else{y.style.display="block",h.style.display="none";let k=c.nextActorDesc||"Wait";k==="HeroB"&&(k="Enemy Hero"),y.textContent=`${k} acting...`,g||setTimeout(()=>m(!1),800)}}async function m(t){if(!g){g=!0;try{const y=c.turnNo+1,h=await C.processTurn(s,t,y);if(h.code===200){const k=h.data;k.lastEvents&&e(k.lastEvents),c=k,i(k),g=!1,a()}else console.error(h.message),g=!1}catch(y){console.error(y),g=!1}}}function I(t){const y=document.getElementById("result-modal"),h=document.getElementById("result-title"),k=document.getElementById("result-desc");y.style.display="flex",h.textContent=t?"VICTORY!":"DEFEAT",h.style.color=t?"var(--success-color)":"var(--danger-color)",k.textContent=t?"战斗胜利！获得金币与战利品。":"战斗失败，请强化武将后再试。",document.getElementById("result-ok").onclick=()=>{x.navigate("/hall")}}document.getElementById("flee-btn").addEventListener("click",()=>{confirm("确定要撤退吗？(视为战败)")&&x.navigate("/hall")})}function V(d,o){var I;const s=localStorage.getItem("userId");if(!s){x.navigate("/login");return}const{civ:v,stageNo:c}=o;if(!v||!c){x.navigate("/hall");return}let g=[],p=null,b=[],f={};d.innerHTML=`
      <div class="prepare-page">
        <div class="page-header">
           <button class="btn btn-sm btn-secondary" id="back-btn">← 放弃出征</button>
           <h2>⚔️ 战前整备 - ${v} 第${c}关</h2>
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
    `;const i=document.createElement("style");i.id="prepare-page-style",(I=document.getElementById("prepare-page-style"))==null||I.remove(),i.textContent=`
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
    `,document.head.appendChild(i),l();async function l(){try{const[n,t]=await Promise.all([w.getGenerals(s),B.getInfo(s)]);n.code===200&&(g=n.data.filter(y=>y.activated),u()),t.code===200&&t.data.troops&&(b=t.data.troops||[])}catch(n){console.error(n)}}function u(){const n=document.getElementById("generals-list");if(g.length===0){n.innerHTML='<div class="empty-tip">没有激活的武将，请先去大厅激活</div>';return}n.innerHTML=g.map(t=>`
           <div class="general-card ${p===t.id?"selected":""}" data-id="${t.id}">
              <div class="g-avatar">🤴</div>
              <div class="g-info">
                 <div class="g-name">${t.name} <span class="g-lv">Lv.${t.level}</span></div>
                 <div class="g-stats">
                    <span style="color:var(--accent-color)">CAP: ${t.capacity||0}</span>
                 </div>
              </div>
           </div>
        `).join(""),n.querySelectorAll(".general-card").forEach(t=>{t.addEventListener("click",()=>r(parseInt(t.dataset.id)))}),!p&&g.length>0&&r(g[0].id)}function r(n){p=n,document.querySelectorAll(".general-card").forEach(t=>{t.classList.toggle("selected",parseInt(t.dataset.id)===n)}),e(),m()}function e(){if(!p)return;const n=document.getElementById("troops-list");if(b.length===0){n.innerHTML='<div class="empty-tip">没有兵力，请先去招募</div>';return}n.innerHTML=b.map(t=>{const y=f[t.troopId]||0;return`
            <div class="troop-row">
               <div class="t-name">${t.name} (余:${t.totalCount})</div>
               <div class="t-control">
                  <button class="btn-tiny btn-minus" data-id="${t.troopId}">-10</button>
                  <input type="number" class="troop-input" id="input-${t.troopId}" value="${y}" max="${t.totalCount}" min="0">
                  <button class="btn-tiny btn-plus" data-id="${t.troopId}">+10</button>
               </div>
            </div>
            `}).join(""),b.forEach(t=>{const y=document.getElementById("input-"+t.troopId);y&&(y.onchange=h=>{let k=parseInt(h.target.value)||0;a(t.troopId,k)})}),n.querySelectorAll(".btn-minus").forEach(t=>{t.onclick=()=>{const y=parseInt(t.dataset.id);a(y,(f[y]||0)-10)}}),n.querySelectorAll(".btn-plus").forEach(t=>{t.onclick=()=>{const y=parseInt(t.dataset.id);a(y,(f[y]||0)+10)}}),m()}function a(n,t){const y=b.find(k=>k.troopId===n);if(!y)return;t=Math.max(0,Math.min(t,y.totalCount)),f[n]=t;const h=document.getElementById("input-"+n);h&&(h.value=t),m()}function m(){const n=g.find(L=>L.id===p);if(!n)return;const t=Object.values(f).reduce((L,z)=>L+z,0),y=n.capacity||0,h=document.getElementById("capacity-display");h.textContent=`(兵力/统率: ${t}/${y})`;const k=document.getElementById("start-btn"),$=t>0&&t<=y;k.disabled=!$,t>y?(h.style.color="var(--danger-color)",k.textContent="兵力超过统率上限"):t===0?(h.style.color="#aaa",k.textContent="请分配兵力"):(h.style.color="var(--success-color)",k.textContent="出征！")}document.getElementById("start-btn").onclick=async()=>{const n=document.getElementById("start-btn");n.disabled=!0,n.textContent="出征中...";try{const t=await C.startStoryBattle(s,v,c,p,f);t.code===200?x.navigate("/battle"):(alert(t.message||"出征失败"),n.disabled=!1,n.textContent="出征！")}catch(t){console.error(t),alert("请求失败"),n.disabled=!1,n.textContent="出征！"}},document.getElementById("back-btn").onclick=()=>x.navigate("/stages")}x.register("/login",j);x.register("/hall",G);x.register("/recruit",A);x.register("/generals",R);x.register("/stages",N);x.register("/equipment",O);x.register("/gem",W);x.register("/skill",D);x.register("/battle",F);x.register("/battle/prepare",V);function Y(){localStorage.getItem("userId")?x.navigate("/hall"):x.navigate("/login")}Y();
//# sourceMappingURL=index-D3Sdmevq.js.map
