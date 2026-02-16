(function(){const n=document.createElement("link").relList;if(n&&n.supports&&n.supports("modulepreload"))return;for(const m of document.querySelectorAll('link[rel="modulepreload"]'))g(m);new MutationObserver(m=>{for(const f of m)if(f.type==="childList")for(const k of f.addedNodes)k.tagName==="LINK"&&k.rel==="modulepreload"&&g(k)}).observe(document,{childList:!0,subtree:!0});function d(m){const f={};return m.integrity&&(f.integrity=m.integrity),m.referrerPolicy&&(f.referrerPolicy=m.referrerPolicy),m.crossOrigin==="use-credentials"?f.credentials="include":m.crossOrigin==="anonymous"?f.credentials="omit":f.credentials="same-origin",f}function g(m){if(m.ep)return;m.ep=!0;const f=d(m);fetch(m.href,f)}})();class R{constructor(){this.routes={},this.currentRoute=null}register(n,d){this.routes[n]=d}navigate(n,d={}){const g=this.routes[n];if(!g){console.error(`Route not found: ${n}`);return}this.currentRoute=n;const m=document.getElementById("app");m.innerHTML="",g(m,d)}back(){window.history.back()}}const h=new R,M="/api";class w{static async request(n,d={}){const g=`${M}${n}`,m={headers:{"Content-Type":"application/json",...d.headers},...d};try{const f=await fetch(g,m),k=f.headers.get("content-type")||"";let u;if(k.includes("application/json")?u=await f.json():u=await f.text(),!f.ok){const l=typeof u=="object"&&u.message?u.message:u;throw new Error(l||`请求失败 (${f.status})`)}return u}catch(f){throw console.error(`API Error [${n}]:`,f),f}}static get(n,d){const g=d?"?"+new URLSearchParams(d).toString():"";return this.request(n+g,{method:"GET"})}static post(n,d){return this.request(n,{method:"POST",body:d?JSON.stringify(d):void 0})}static postWithParams(n,d,g){const m=d?"?"+new URLSearchParams(d).toString():"";return this.request(n+m,{method:"POST",body:g?JSON.stringify(g):void 0})}}const P={register(i,n,d,g){return w.post("/auth/register",{username:i,password:n,nickname:d,initialCiv:g})},login(i,n){return w.post("/auth/login",{username:i,password:n})}},T={getInfo(i){return w.get("/player/info",{userId:i})},getProgress(i){return w.get("/player/progress",{userId:i})}},$={getGenerals(i){return w.get("/hall/generals",{userId:i})},getProgress(i){return w.get("/hall/progress",{userId:i})},upgradeGeneral(i,n){return w.postWithParams("/hall/general/upgrade",{userId:i,generalId:n})},activateGeneral(i,n){return w.postWithParams("/hall/general/activate",{userId:i,generalId:n})},ascendGeneral(i,n){return w.postWithParams("/hall/general/ascend",{userId:i,generalId:n})},recruit(i,n,d){return w.postWithParams("/hall/recruit",{userId:i},{troopId:n,count:d})},equipGeneral(i,n,d){return w.postWithParams("/hall/general/equip",{userId:i},{generalId:n,equipmentId:d})},enhanceEquipment(i,n){return w.postWithParams("/hall/equipment/enhance",{userId:i,equipmentId:n})},learnSkill(i,n,d){return w.postWithParams("/hall/skill/learn",{userId:i,generalId:n,bookItemId:d})},inlayGem(i,n,d,g){return w.postWithParams("/hall/gem/inlay",{userId:i},{equipmentId:n,socketIndex:d,gemId:g})},combineGem(i,n,d){return w.postWithParams("/hall/gem/combine",{userId:i},{gemType:n,level:d})},getEquipments(i){return w.get("/hall/equipments",{userId:i})},getGems(i){return w.get("/hall/gems",{userId:i})},getItems(i){return w.get("/hall/items",{userId:i})}},q={startStoryBattle(i,n,d,g,m){return w.postWithParams("/battle/story/start",{userId:i},{civ:n,stageNo:d,generalId:g,troopConfig:m})},processTurn:(i,n,d,g)=>w.postWithParams("/battle/turn",{userId:i},{castSkill:n,clientTurnNo:d,tactics:g}),getBattleState(i){return w.get("/battle/state",{userId:i})}},S={getTroops(i){return w.get("/troop/list",{userId:i})},getCodex(i){return w.get("/troop/codex",{userId:i})},evolve(i,n){return w.postWithParams("/troop/evolve",{userId:i},{troopId:n})}};function N(i){var c;i.innerHTML=`
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
  `;const n=document.createElement("style");n.id="login-page-style",(c=document.getElementById("login-page-style"))==null||c.remove(),n.textContent=`
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
  `,document.head.appendChild(n);let d=!1,g="CN";const m=document.getElementById("username"),f=document.getElementById("password"),k=document.getElementById("nickname"),u=document.getElementById("register-fields"),l=document.getElementById("login-btn"),p=document.getElementById("register-btn"),a=document.getElementById("message"),s=document.querySelectorAll(".civ-option");s.forEach(r=>{r.addEventListener("click",()=>{s.forEach(y=>y.classList.remove("selected")),r.classList.add("selected"),g=r.dataset.civ})});function t(r,y="error"){a.className=`message ${y}`,a.textContent=r}function o(r){localStorage.setItem("userId",r.id),localStorage.setItem("username",r.nickname),t("登录成功！正在进入游戏...","success"),setTimeout(()=>h.navigate("/hall"),600)}p.addEventListener("click",()=>{d=!d,u.style.display=d?"flex":"none",l.textContent=d?"确认注册":"登录",p.textContent=d?"返回登录":"注册",a.textContent=""}),l.addEventListener("click",async()=>{const r=m.value.trim(),y=f.value.trim();if(!r||!y){t("请输入账号和密码");return}l.disabled=!0,p.disabled=!0;try{if(d){const b=k.value.trim();if(!b){t("请输入昵称");return}l.textContent="注册中...";const e=await P.register(r,y,b,g);console.log("注册结果:",e),e.code===200&&e.data?o(e.data):t(e.message||"注册失败")}else{l.textContent="登录中...";const b=await P.login(r,y);console.log("登录结果:",b),b.code===200&&b.data?o(b.data):t(b.message||"登录失败")}}catch(b){t(b.message||"操作失败，请重试")}finally{l.disabled=!1,p.disabled=!1,l.textContent=d?"确认注册":"登录"}}),i.addEventListener("keypress",r=>{r.key==="Enter"&&l.click()})}const j={2001:{troopId:2001,civ:"CN",type:"INF",name:"重盾步兵",icon:"🛡️",color:"var(--inf-color)",recruitGold:22,capCost:2,isElite:!1},2002:{troopId:2002,civ:"CN",type:"ARC",name:"强弩兵",icon:"🏹",color:"var(--arc-color)",recruitGold:24,capCost:2,isElite:!1},2003:{troopId:2003,civ:"CN",type:"CAV",name:"虎豹骑",icon:"🐎",color:"var(--cav-color)",recruitGold:42,capCost:3,isElite:!1},2101:{troopId:2101,civ:"JP",type:"INF",name:"足轻",icon:"🛡️",color:"var(--inf-color)",recruitGold:22,capCost:2,isElite:!1},2102:{troopId:2102,civ:"JP",type:"ARC",name:"弓足轻",icon:"🏹",color:"var(--arc-color)",recruitGold:26,capCost:2,isElite:!1},2103:{troopId:2103,civ:"JP",type:"CAV",name:"骑马武者",icon:"🐎",color:"var(--cav-color)",recruitGold:45,capCost:3,isElite:!1},2201:{troopId:2201,civ:"KR",type:"INF",name:"步卒",icon:"🛡️",color:"var(--inf-color)",recruitGold:23,capCost:2,isElite:!1},2202:{troopId:2202,civ:"KR",type:"ARC",name:"长弓手",icon:"🏹",color:"var(--arc-color)",recruitGold:25,capCost:2,isElite:!1},2203:{troopId:2203,civ:"KR",type:"CAV",name:"重骑卒",icon:"🐎",color:"var(--cav-color)",recruitGold:44,capCost:3,isElite:!1},2301:{troopId:2301,civ:"GB",type:"INF",name:"长枪步兵",icon:"🛡️",color:"var(--inf-color)",recruitGold:24,capCost:2,isElite:!1},2302:{troopId:2302,civ:"GB",type:"ARC",name:"长弓手",icon:"🏹",color:"var(--arc-color)",recruitGold:28,capCost:2,isElite:!1},2303:{troopId:2303,civ:"GB",type:"CAV",name:"重骑士",icon:"🐎",color:"var(--cav-color)",recruitGold:46,capCost:3,isElite:!1},3001:{troopId:3001,civ:"CN",type:"ARC",name:"青囊医官",icon:"💊",color:"#10b981",recruitGold:130,capCost:3,isElite:!0},3002:{troopId:3002,civ:"JP",type:"ARC",name:"爆裂火筒队",icon:"🔥",color:"#f97316",recruitGold:145,capCost:3,isElite:!0},3003:{troopId:3003,civ:"KR",type:"INF",name:"军乐旗卫",icon:"🎺",color:"#06b6d4",recruitGold:140,capCost:3,isElite:!0},3004:{troopId:3004,civ:"GB",type:"INF",name:"破甲工兵",icon:"🪓",color:"#a3a3a3",recruitGold:150,capCost:3,isElite:!0}},O={weapon:"武器",armor1:"防具",helm:"头盔",boots:"鞋子",mount:"坐骑",accessory:"饰品"},_={301:{name:"鼓舞技能书"},302:{name:"战地医治技能书"},303:{name:"毒箭技能书"},304:{name:"威压技能书"},305:{name:"铁壁技能书"},306:{name:"一石二鸟技能书"},307:{name:"偷袭技能书"},308:{name:"红颜克制技能书"},309:{name:"铁躯技能书"},310:{name:"易伤掌控技能书"},311:{name:"反震技能书"},312:{name:"先机技能书"},313:{name:"避锋技能书"}};function z(i){return j[i]||{troopId:i,civ:"",type:"INF",name:`兵种#${i}`,icon:"⚔️",color:"var(--text-secondary)",recruitGold:20,capCost:1,isElite:!1}}function A(i){return{INF:"步兵",ARC:"弓兵",CAV:"骑兵"}[i]||i}function F(i){return O[i]||"装备"}function W(i){return i==="weapon"?"⚔️":i==="armor1"?"🛡️":i==="helm"?"🪖":i==="boots"?"👢":i==="mount"?"🐎":i==="accessory"?"💍":"📦"}function K(i){const n=localStorage.getItem("userId");if(!n){h.navigate("/login");return}i.innerHTML=`
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
            <div class="spinner"></div>
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
            <button class="btn btn-secondary" id="codex-btn">兵种图鉴</button>
            <button class="btn btn-secondary" id="tower-btn">爬塔挑战</button>
          </div>
        </div>
      </div>
    </div>
  `;const d=document.createElement("style");d.textContent=`
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
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-md);
      max-height: 300px;
      overflow-y: auto;
      padding-right: 4px;
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
      min-width: 0;
    }

    .troop-name {
      font-size: 0.9rem;
      color: var(--text-secondary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
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
  `,document.head.appendChild(d),g(),m(),f();async function g(){try{const u=await T.getInfo(n);console.log("玩家数据:",u),u.code===200&&u.data?(document.getElementById("player-name").textContent=u.data.nickname,document.getElementById("gold-count").textContent=(u.data.gold||0).toLocaleString(),document.getElementById("diamond-count").textContent=(u.data.diamond||0).toLocaleString(),k(u.data.troops||[])):console.warn("加载玩家数据: code不为200",u)}catch(u){console.error("加载玩家数据失败:",u)}}async function m(){try{const u=await $.getGenerals(n);console.log("武将数据:",u);const l=document.getElementById("generals-grid");u.code===200&&u.data&&u.data.length>0?l.innerHTML=u.data.slice(0,4).map(p=>`
          <div class="general-card" data-id="${p.id}">
            <div class="general-avatar">🎖️</div>
            <div class="general-name">武将#${p.templateId}</div>
            <div class="general-level">Lv.${p.level}</div>
            ${p.activated?'<div class="badge badge-inf">已激活</div>':'<div class="badge" style="background:rgba(255,255,255,0.1)">未激活</div>'}
          </div>
        `).join(""):l.innerHTML='<p style="grid-column: 1/-1; text-align: center;">暂无武将</p>'}catch(u){console.error("加载武将失败:",u),document.getElementById("generals-grid").innerHTML='<p style="grid-column: 1/-1; text-align: center; color: var(--danger-color);">加载失败</p>'}}async function f(){try{const u=await $.getProgress(n);console.log("进度数据:",u);const l=document.getElementById("progress-grid");if(u.code===200&&u.data&&u.data.length>0){const p={CN:"🇨🇳 中国",JP:"🇯🇵 日本",KR:"🇰🇷 韩国",GB:"🇬🇧 英国"};l.innerHTML=u.data.map(a=>`
          <div class="progress-item">
            <div class="progress-civ">
              <span class="badge badge-${a.civ.toLowerCase()}">${p[a.civ]||a.civ}</span>
            </div>
            <div class="progress-stage">
              当前进度: ${a.maxStageCleared||0}/10 关
              ${a.unlocked?"":'<br/><span style="color: var(--danger-color);">🔒 未解锁</span>'}
            </div>
          </div>
        `).join("")}else l.innerHTML='<p style="grid-column: 1/-1; text-align: center;">暂无进度</p>'}catch(u){console.error("加载进度失败:",u)}}function k(u){const l=document.getElementById("troops-grid");if(!l)return;const p=(u||[]).map(a=>{const s=z(a.troopId);return{troopId:a.troopId,count:a.count||0,civ:s.civ,type:s.type,icon:s.icon,name:s.name,color:s.color,isElite:!!s.isElite}}).filter(a=>a.count>0).sort((a,s)=>{if(a.civ!==s.civ)return String(a.civ).localeCompare(String(s.civ));if(a.isElite!==s.isElite)return a.isElite?1:-1;const t={INF:1,ARC:2,CAV:3};return(t[a.type]||9)-(t[s.type]||9)});if(p.length===0){l.innerHTML='<p style="grid-column: 1/-1; text-align: center;">暂无兵力</p>';return}l.innerHTML=p.map(a=>`
      <div class="troop-item">
        <div class="troop-icon" style="background: ${a.color};">${a.icon}</div>
        <div class="troop-info">
          <span class="troop-name">[${a.civ}] ${a.name}${a.isElite?" [特种]":""}</span>
          <span class="troop-count">${a.count.toLocaleString()}</span>
        </div>
      </div>
    `).join("")}document.getElementById("logout-btn").addEventListener("click",()=>{localStorage.clear(),h.navigate("/login")}),document.getElementById("select-stage-btn").addEventListener("click",()=>{h.navigate("/stages")}),document.getElementById("manage-generals-btn").addEventListener("click",()=>{h.navigate("/generals")}),document.getElementById("recruit-btn").addEventListener("click",()=>{h.navigate("/recruit")}),document.getElementById("equip-btn").addEventListener("click",()=>{h.navigate("/equipment")}),document.getElementById("gem-btn").addEventListener("click",()=>{h.navigate("/gem")}),document.getElementById("skill-btn").addEventListener("click",()=>{h.navigate("/skill")}),document.getElementById("codex-btn").addEventListener("click",()=>{h.navigate("/codex")}),document.getElementById("tower-btn").addEventListener("click",()=>{alert("爬塔功能敬请期待")})}function V(i){var u;const n=localStorage.getItem("userId");if(!n){h.navigate("/login");return}i.innerHTML=`
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
  `;const d=document.createElement("style");d.id="recruit-page-style",(u=document.getElementById("recruit-page-style"))==null||u.remove(),d.textContent=`
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
  `,document.head.appendChild(d),g();async function g(){try{const[l,p]=await Promise.all([T.getInfo(n),S.getCodex(n)]),a={};l.code===200&&l.data&&(document.getElementById("gold-display").textContent=(l.data.gold||0).toLocaleString(),(l.data.troops||[]).forEach(t=>{a[t.troopId]=t.count||0}));let s=[];p.code===200&&p.data&&(s=p.data),s.sort((t,o)=>t.status!==o.status?o.status-t.status:t.troopId-o.troopId),m(s,a)}catch(l){console.error("加载数据失败:",l),document.getElementById("recruit-list").innerHTML='<p style="text-align:center">加载失败</p>'}}function m(l,p){const a=document.getElementById("recruit-list");a.innerHTML=l.map(s=>{const t=z(s.troopId),o=s.status<2,c=o?"0.6":"1",r=o?"filter: grayscale(1);":"";return`
      <div class="recruit-card card" data-troop-id="${s.troopId}" style="${r} opacity: ${c}">
        <div class="troop-header">
          <div class="troop-avatar" style="background:${t.color};">${o?"🔒":t.icon}</div>
          <div>
            <h3>${s.name} ${s.isElite?'<span style="font-size:.8em;color:#ffd166">[特种]</span>':""}</h3>
            <p class="troop-desc">${A(s.type)} · ${s.civ}</p>
          </div>
        </div>
        <div class="troop-stats">
          ${o?'<span><span style="color:#ef4444">未解锁</span>':`<span>当前: <strong>${(p[s.troopId]||0).toLocaleString()}</strong></span>`}
          <span>单价: <strong>${t.recruitGold}</strong> 金</span>
        </div>
        
        ${o?`
            <div style="font-size:0.9em; color:#ef4444; text-align:center; padding:10px; background:rgba(0,0,0,0.2); border-radius:4px;">
                ${s.unlockHint||`需通关 ${s.civ} 关卡解锁`}
            </div>
        `:`
            <div class="recruit-controls">
              <button class="btn btn-sm qty-btn" data-delta="-10">-10</button>
              <button class="btn btn-sm qty-btn" data-delta="-1">-1</button>
              <input type="number" class="recruit-input" id="qty-${s.troopId}" value="10" min="1" />
              <button class="btn btn-sm qty-btn" data-delta="1">+1</button>
              <button class="btn btn-sm qty-btn" data-delta="10">+10</button>
            </div>
            <button class="btn btn-primary recruit-btn" data-troop-id="${s.troopId}">招募</button>
        `}
      </div>
    `}).join(""),f()}function f(){document.querySelectorAll(".qty-btn").forEach(l=>{l.onclick=()=>{const p=l.parentElement.querySelector(".recruit-input"),a=parseInt(l.dataset.delta||"0",10),s=Math.max(1,(parseInt(p.value||"1",10)||1)+a);p.value=String(s)}}),document.querySelectorAll(".recruit-btn").forEach(l=>{l.onclick=async()=>{const p=parseInt(l.dataset.troopId||"0",10),a=document.getElementById(`qty-${p}`),s=parseInt((a==null?void 0:a.value)||"0",10);if(!(s<=0)){l.disabled=!0;try{const t=await $.recruit(n,p,s);t.code===200?(k("招募成功","success"),g()):k(t.message||"招募失败","error")}catch(t){k(t.message||"请求失败","error")}finally{l.disabled=!1}}}})}document.getElementById("back-btn").addEventListener("click",()=>h.navigate("/hall"));function k(l,p="success"){const a=document.getElementById("toast");a.textContent=l,a.className=`toast ${p} show`,setTimeout(()=>{a.className="toast"},2e3)}}function D(i){var t;const n=localStorage.getItem("userId");if(!n){h.navigate("/login");return}let d=[],g=null;const m=[{key:"weapon",label:"武器",icon:"⚔️"},{key:"armor1",label:"防具",icon:"🛡️"},{key:"helm",label:"头盔",icon:"🪖"},{key:"boots",label:"鞋子",icon:"👢"},{key:"mount",label:"坐骑",icon:"🐎"},{key:"accessory",label:"饰品",icon:"💍"}];i.innerHTML=`
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
  `;const f=document.createElement("style");f.id="generals-page-style",(t=document.getElementById("generals-page-style"))==null||t.remove(),f.textContent=`
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
  `,document.head.appendChild(f),k();async function k(){try{const[o,c,r]=await Promise.all([$.getGenerals(n),$.getEquipments(n),T.getInfo(n)]);r.code===200&&r.data&&(document.getElementById("gold-display").textContent=(r.data.gold||0).toLocaleString()),c.code===200&&(d=c.data||[]);const y=document.getElementById("generals-content");o.code===200&&o.data&&o.data.length>0?u(o.data,y):y.innerHTML='<p style="text-align:center;width:100%;">暂无武将</p>'}catch(o){console.error(o)}}function u(o,c){c.innerHTML=o.map(r=>{const y=m.map(b=>{const e=d.find(v=>v.generalId===r.id&&v.slot===b.key);return`
              <div class="equip-slot ${e?"equipped":""}" data-slot="${b.key}" data-id="${r.id}">
                 <div class="slot-icon">${e?b.icon:"+"}</div>
                 <div class="slot-label">${b.label}</div>
              </div>
            `}).join("");return`
          <div class="general-detail-card ${r.activated?"":"inactive"}">
            <div class="gd-top">
               <div class="gd-avatar">🎖️</div>
               <div class="gd-info">
                 <div class="gd-name">${r.name||"武将"} #${r.templateId}</div>
                 <div class="gd-meta">
                   <span>Lv.${r.level}</span> · <span>阶 ${r.tier}</span> · <span>${r.activated?"已激活":"未激活"}</span>
                 </div>
               </div>
            </div>
            
            <div class="stats-row">
               <div class="stat-item"><span class="stat-val">${r.currentHp}/${r.maxHp}</span><span class="stat-lbl">HP</span></div>
               <div class="stat-item"><span class="stat-val">${r.atk||0}</span><span class="stat-lbl">攻击</span></div>
               <div class="stat-item"><span class="stat-val">${r.speed||0}</span><span class="stat-lbl">速度</span></div>
               <div class="stat-item"><span class="stat-val">${r.capacity||0}</span><span class="stat-lbl">统率</span></div>
            </div>

            <div class="skill-section">
               <div class="skill-title">💡 技能: ${r.skillName||"无"}</div>
               <div class="skill-desc">${r.skillDesc||"暂无效果"}</div>
            </div>

            <div class="equip-slots">
                ${r.activated?y:'<div style="grid-column:1/-1;text-align:center;font-size:0.8rem;padding:10px;">需激活后才可穿戴装备</div>'}
            </div>

            <div class="gd-actions">
               ${!r.activated&&r.unlocked?`<button class="btn btn-primary action-btn" data-action="activate" data-id="${r.id}">激活</button>`:""}
               ${r.activated?`<button class="btn btn-primary action-btn" data-action="upgrade" data-id="${r.id}">升级</button>`:""}
               ${r.activated?`<button class="btn btn-secondary action-btn" data-action="ascend" data-id="${r.id}">升阶</button>`:""}
            </div>
          </div>
        `}).join(""),c.querySelectorAll(".action-btn").forEach(r=>r.addEventListener("click",()=>a(r.dataset.action,r.dataset.id))),c.querySelectorAll(".equip-slot").forEach(r=>r.addEventListener("click",()=>l(r.dataset.id,r.dataset.slot)))}function l(o,c){g=parseInt(o);const r=document.getElementById("equip-list"),y=d.filter(e=>e.slot===c&&(!e.generalId||e.generalId===g)),b=y.length?y.map(e=>`
        <div class="equip-item" data-id="${e.id}">
           <div style="font-size:1.5rem">${m.find(v=>v.key===c).icon}</div>
           <div>
              <div style="font-weight:bold">${e.name} +${e.enhanceLevel}</div>
              <div style="font-size:0.8rem;color:#888;">${e.generalId===g?"当前装备":e.generalId?"他人装备":"闲置"}</div>
           </div>
        </div>
      `).join(""):'<p style="text-align:center;color:#888;">暂无可用装备</p>';r.innerHTML=b,r.querySelectorAll(".equip-item").forEach(e=>e.addEventListener("click",()=>p(e.dataset.id))),document.getElementById("equip-modal").style.display="flex"}async function p(o){try{const c=await $.equipGeneral(n,g,o);c.code===200?(s("装备成功"),document.getElementById("equip-modal").style.display="none",k()):s(c.message,"error")}catch(c){s(c.message,"error")}}async function a(o,c){try{let r;o==="activate"&&(r=$.activateGeneral(n,c)),o==="upgrade"&&(r=$.upgradeGeneral(n,c)),o==="ascend"&&(r=$.ascendGeneral(n,c));const y=await r;y.code===200?(s("操作成功"),k()):s(y.message,"error")}catch(r){s(r.message,"error")}}document.getElementById("close-modal").addEventListener("click",()=>document.getElementById("equip-modal").style.display="none"),document.getElementById("back-btn").addEventListener("click",()=>h.navigate("/hall"));function s(o,c="success"){const r=document.getElementById("toast");r.textContent=o,r.className=`toast ${c} show`,setTimeout(()=>{r.className="toast"},2e3)}}function J(i){var l;const n=localStorage.getItem("userId");if(!n){h.navigate("/login");return}i.innerHTML=`
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
  `;const d=document.createElement("style");d.id="stage-page-style",(l=document.getElementById("stage-page-style"))==null||l.remove(),d.textContent=`
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
  `,document.head.appendChild(d);let g="CN",m=[];f();async function f(){document.querySelectorAll(".civ-tab").forEach(p=>{p.addEventListener("click",()=>{document.querySelectorAll(".civ-tab").forEach(a=>a.classList.remove("active")),p.classList.add("active"),g=p.dataset.civ,k()})}),document.getElementById("back-btn").addEventListener("click",()=>{h.navigate("/hall")});try{const[p,a]=await Promise.all([$.getProgress(n),T.getInfo(n)]);a.code===200&&a.data&&(document.getElementById("gold-display").textContent=(a.data.gold||0).toLocaleString()),p.code===200&&p.data&&(m=p.data,k())}catch(p){console.error("加载失败",p),document.getElementById("stage-grid").innerHTML="<p>加载失败，请重试</p>"}}function k(){const p=document.getElementById("stage-grid");p.innerHTML="";const a=m.find(o=>o.civ===g);if(!a||!a.unlocked){p.innerHTML=`
        <div style="grid-column:1/-1; text-align:center; padding: 40px;">
          <h2>🔒 该势力尚未解锁</h2>
          <p style="color:var(--text-secondary)">请先通关前置势力的主线关卡</p>
        </div>
      `;return}const s=10,t=a.maxStageCleared||0;for(let o=1;o<=s;o++){const c=o<=t,r=o<=t+1,y=document.createElement("div");y.className=`stage-card ${c?"cleared":""} ${r?"unlocked":"locked"}`;let b="普通";(o===5||o===9)&&(b="🏰 攻城"),o===10&&(b="👹 BOSS"),y.innerHTML=`
        ${r?"":'<div class="lock-icon">🔒</div>'}
        <div class="stage-name">第 ${o} 关</div>
        <div class="stage-desc">${b}</div>
        <div class="stage-num">${o}</div>
      `,r&&y.addEventListener("click",()=>{u(g,o)}),p.appendChild(y)}}function u(p,a){h.navigate("/battle/prepare",{civ:p,stageNo:a})}}function U(i){var u;const n=localStorage.getItem("userId");if(!n){h.navigate("/login");return}i.innerHTML=`
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
  `;const d=`
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
  `,g=document.createElement("style");g.id="equipment-page-style",(u=document.getElementById("equipment-page-style"))==null||u.remove(),g.textContent=d,document.head.appendChild(g),m();async function m(){try{const[l,p]=await Promise.all([$.getEquipments(n),T.getInfo(n)]);p.code===200&&p.data&&(document.getElementById("gold-display").textContent=(p.data.gold||0).toLocaleString());const a=document.getElementById("equip-list");l.code===200&&l.data&&l.data.length>0?(a.innerHTML=l.data.map(s=>{const t=s.enhanceLevel||0,o=(t+1)*100,c=s.name||`装备#${s.templateId}`,r=W(s.slot),y=F(s.slot);return`
           <div class="equip-card">
             <div class="ec-top">
               <div class="ec-icon">${r}</div>
               <div class="ec-info">
                 <div class="ec-name">${c} <span class="ec-lv">+${t}</span></div>
                 <div class="ec-stat">${y} · ${s.generalId?"已穿戴":"闲置"}</div>
               </div>
             </div>
             <div class="ec-stat">基础属性: ATK ${s.baseAtk||0} / HP ${s.baseHp||0} / SPD ${s.baseSpd||0}</div>
             <div class="ec-stat">消耗: ${o}金币</div>
             <button class="btn btn-primary enhance-btn" data-id="${s.id}" data-cost="${o}">强化</button>
           </div>
           `}).join(""),a.querySelectorAll(".enhance-btn").forEach(s=>{s.addEventListener("click",()=>f(s.dataset.id,s.dataset.cost))})):a.innerHTML='<p style="text-align:center;grid-column:1/-1;">暂无装备</p>'}catch(l){console.error(l)}}async function f(l,p){if(confirm(`确认消耗 ${p} 金币强化装备吗？
(+3以上可能会失败掉级)`))try{const a=await $.enhanceEquipment(n,l);if(a.code===200){const s=a.data,t=s.includes("失败");k(s,t?"error":"success"),m()}else k(a.message||"强化失败","error")}catch(a){k(a.message||"操作失败","error")}}function k(l,p="success"){const a=document.getElementById("toast");a.textContent=l,a.className=`toast ${p} show`,setTimeout(()=>{a.className="toast"},2e3)}document.getElementById("back-btn").addEventListener("click",()=>h.navigate("/hall"))}function Y(i){var s;const n=localStorage.getItem("userId");if(!n){h.navigate("/login");return}let d=[];i.innerHTML=`
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
  `;const g=document.createElement("style");g.id="gem-page-style",(s=document.getElementById("gem-page-style"))==null||s.remove(),g.textContent=`
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
  `,document.head.appendChild(g),m();async function m(){try{const[t,o,c]=await Promise.all([$.getGems(n),$.getEquipments(n),T.getInfo(n)]);c.code===200&&c.data&&(document.getElementById("gold-display").textContent=(c.data.gold||0).toLocaleString()),t.code===200&&(d=t.data||[]);const r=document.getElementById("gem-list"),y={};d.forEach(e=>{if(e.isUsed)return;const v=`${e.gemType}-${e.gemLevel}`;y[v]||(y[v]={type:e.gemType,level:e.gemLevel,count:0,ids:[]}),y[v].count++,y[v].ids.push(e.id)}),Object.keys(y).length===0?r.innerHTML='<p style="text-align:center;color:#888">暂无闲置宝石</p>':(r.innerHTML=Object.values(y).map(e=>`
          <div class="gem-item">
            <div class="gem-info">
              <div class="gem-icon">💎</div>
              <div>
                <div>${e.type} Lv.${e.level}</div>
                <div style="font-size:0.8rem;color:#888">拥有: ${e.count}</div>
              </div>
            </div>
            ${e.count>=5?`<button class="btn btn-primary btn-sm combine-btn" data-type="${e.type}" data-level="${e.level}">合成</button>`:""}
          </div>
        `).join(""),r.querySelectorAll(".combine-btn").forEach(e=>{e.addEventListener("click",()=>p(e.dataset.type,e.dataset.level))}));const b=document.getElementById("equip-list");o.code===200&&o.data&&(b.innerHTML=o.data.map(e=>`
          <div class="equip-item" data-id="${e.id}">
             <div style="font-weight:bold">${e.name||"装备#"+e.templateId} +${e.enhanceLevel||0}</div>
             <div style="font-size:0.8rem;color:#888; margin-top:5px;">
               <span class="socket-span" data-eid="${e.id}" data-idx="1" style="background:${e.socket1GemId?"rgba(46,204,113,0.2)":"rgba(255,255,255,0.1)"};padding:2px 6px;border-radius:4px;cursor:pointer;">
                 孔1: ${e.socket1GemId?"已镶嵌":"+ 空"}
               </span>
               <span class="socket-span" data-eid="${e.id}" data-idx="2" style="background:${e.socket2GemId?"rgba(46,204,113,0.2)":"rgba(255,255,255,0.1)"};padding:2px 6px;border-radius:4px;cursor:pointer;margin-left:5px;">
                 孔2: ${e.socket2GemId?"已镶嵌":"+ 空"}
               </span>
             </div>
          </div>
        `).join(""),b.querySelectorAll(".socket-span").forEach(e=>{e.addEventListener("click",v=>{v.stopPropagation(),e.textContent.includes("已镶嵌")?a("由于时间限制，暂不支持拆卸宝石","error"):u(e.dataset.eid,e.dataset.idx)})}))}catch(t){console.error(t)}}let f=null,k=null;function u(t,o){f=t,k=o;const c=d.filter(y=>!y.isUsed),r=document.getElementById("select-gem-list");c.length===0?r.innerHTML='<p style="text-align:center;color:#888">没有闲置宝石</p>':(r.innerHTML=c.map(y=>`
        <div class="select-item" data-gid="${y.id}">
          <div class="gem-icon" style="width:24px;height:24px;font-size:0.8rem;">💎</div>
          <div>${y.gemType} Lv.${y.gemLevel}</div>
          <div style="margin-left:auto;font-size:0.8rem;color:#aaa;">+${y.statValue}</div>
        </div>
      `).join(""),r.querySelectorAll(".select-item").forEach(y=>{y.addEventListener("click",()=>{l(y.dataset.gid)})})),document.getElementById("gem-select-modal").style.display="flex"}async function l(t){try{const o=await $.inlayGem(n,f,parseInt(k),t);o.code===200?(a("镶嵌成功！"),document.getElementById("gem-select-modal").style.display="none",m()):a(o.message||"镶嵌失败","error")}catch(o){a(o.message,"error")}}document.getElementById("close-modal").addEventListener("click",()=>{document.getElementById("gem-select-modal").style.display="none"});async function p(t,o){try{const c=await $.combineGem(n,t,parseInt(o));c.code===200?(a("合成成功！"),m()):a(c.message,"error")}catch(c){a(c.message,"error")}}function a(t,o="success"){const c=document.getElementById("toast");c.textContent=t,c.className=`toast ${o} show`,setTimeout(()=>{c.className="toast"},2e3)}document.getElementById("back-btn").addEventListener("click",()=>h.navigate("/hall"))}function X(i){const n=localStorage.getItem("userId");if(!n){h.navigate("/login");return}i.innerHTML=`
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
  `;const d=`
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
  `,g=document.createElement("style");g.textContent=d,document.head.appendChild(g);let m=null,f=[],k=[];u();async function u(){try{const[t,o,c]=await Promise.all([$.getGenerals(n),$.getItems(n),T.getInfo(n)]);c.code===200&&c.data&&(document.getElementById("gold-display").textContent=(c.data.gold||0).toLocaleString()),f=t.code===200?t.data:[],k=o.code===200?o.data:[],l(),p()}catch(t){console.error(t)}}function l(){const t=document.getElementById("general-list");f.length===0?t.innerHTML="<p>暂无武将</p>":(t.innerHTML=f.map(o=>`
        <div class="g-card ${m==o.id?"selected":""}" data-id="${o.id}">
          <div class="g-av">🎖️</div>
          <div style="font-size:0.9rem;font-weight:bold">武将#${o.templateId}</div>
          <div style="font-size:0.8rem;color:#ccc">Lv.${o.level}</div>
        </div>
      `).join(""),t.querySelectorAll(".g-card").forEach(o=>{o.addEventListener("click",()=>{m=o.dataset.id,l(),p()})}))}function p(){const t=document.getElementById("book-list");if(!m){t.innerHTML='<p style="text-align:center;color:#888;width:100%">请先选择武将</p>';return}const o=k.filter(c=>c.count>0&&c.itemId>=300);o.length===0?t.innerHTML='<p style="text-align:center;color:#888;width:100%">背包中没有技能书</p>':(t.innerHTML=o.map(c=>{var r;return`
        <div class="book-card" data-iid="${c.itemId}">
          <div class="book-icon">📚</div>
          <div>
            <div style="font-weight:bold">${((r=_[c.itemId])==null?void 0:r.name)||"技能书 #"+c.itemId}</div>
            <div style="font-size:0.8rem;color:#888">拥有: ${c.count}</div>
          </div>
          <button class="btn btn-primary btn-sm learn-btn" data-iid="${c.itemId}">学习</button>
        </div>
      `}).join(""),t.querySelectorAll(".learn-btn").forEach(c=>{c.addEventListener("click",()=>a(c.dataset.iid))}))}async function a(t){if(confirm("确认消耗一本技能书让该武将学习技能吗？(旧技能将被覆盖)"))try{const o=await $.learnSkill(n,m,parseInt(t));o.code===200?(s("学习成功！","success"),u()):s(o.message||"学习失败","error")}catch(o){s(o.message,"error")}}function s(t,o="success"){const c=document.getElementById("toast");c.textContent=t,c.className=`toast ${o} show`,setTimeout(()=>{c.className="toast"},2e3)}document.getElementById("back-btn").addEventListener("click",()=>h.navigate("/hall"))}function Q(i,n){var b;const d=localStorage.getItem("userId");if(!d){h.navigate("/login");return}n.civ;let g=null,m=!1;i.innerHTML=`
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
  `;const f=document.createElement("style");f.id="battle-page-style",(b=document.getElementById("battle-page-style"))==null||b.remove(),f.textContent=`
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
  `,document.head.appendChild(f);let k="";i.querySelectorAll('input[name="tactics"]').forEach(e=>{e.addEventListener("change",v=>k=v.target.value)}),u();async function u(){try{const e=await q.getBattleState(d);e.code===200?(g=e.data,l(g),t()):h.navigate("/hall")}catch{h.navigate("/hall")}}function l(e){e&&(p("a",e.sideA.hero),a("a",e.sideA.troops),p("b",e.sideB.hero),a("b",e.sideB.troops),document.getElementById("turn-display").textContent=`Turn ${e.turnNo}`)}function p(e,v){const x=document.getElementById(`hero-${e}`),I=x.querySelector(".hp-fill"),E=x.querySelector(".hp-text"),L=Math.max(0,Math.min(100,v.hp/v.maxHp*100));I.style.width=L+"%",E.textContent=`${v.hp}/${v.maxHp}`,x.querySelector(".hero-name").textContent=v.name,v.hp<=0&&(x.style.opacity="0.5")}function a(e,v){const x=document.getElementById(`troops-${e}`);x.innerHTML=v.map(I=>`
        <div class="troop-unit ${I.count<=0?"troop-dead":""}">
           <span>${s(I.type)} ${I.name||I.type}</span>
           <span>x${I.count}</span>
        </div>
      `).join("")}function s(e){return e==="INF"?"🛡️":e==="ARC"?"🏹":e==="CAV"?"🐎":"⚔️"}function t(){if(!g)return;const e=document.getElementById("phase-display"),v=g.phase||"HERO_SOLO";if(e){const B=v==="TROOP_WAR"?"🔥 全军出击":"⚔️ 武将单挑";e.textContent=B,e.className=v==="TROOP_WAR"?"phase-badge war":"phase-badge solo"}if(g.finished){y(g.win);return}const x=document.getElementById("action-status"),I=document.getElementById("action-buttons"),E=document.getElementById("btn-attack"),L=document.getElementById("btn-skill");x.style.display="none",I.style.display="flex";const C=g.sideA.hero;E.textContent=`⚔️ ${g.currentTurn+1}回合: 开始`,v==="TROOP_WAR"?(L.disabled=!0,L.textContent="阶段2不可用"):(C.skillCd||0)>0?(L.disabled=!0,L.textContent=`技能冷却 (${C.skillCd})`):(L.disabled=!1,L.textContent="✨ 释放技能"),E.onclick=()=>r(!1),L.onclick=()=>r(!0)}function o(e){if(!e)return;const v=document.getElementById("logs-content");e.forEach(x=>{const I=document.createElement("div");I.className="log-entry";let E="";switch(x.type){case"TURN_START":E=`<span class="log-highlight">=== 第 ${x.turn} 回合 ===</span>`;break;case"PHASE_CHANGE":E=`<span class="log-highlight" style="font-size:1.1em">⚠️ 阶段切换: ${x.fromPhase||"HERO_SOLO"} -> ${x.toPhase||"TROOP_WAR"}</span>`;break;case"HERO_ATTACK":E=`[${x.actorSide==="my"?"我方":"敌方"}] 主将普攻`;break;case"HERO_SKILL":E=`[${x.actorSide==="my"?"我方":"敌方"}] 释放技能! (Dmg: ${x.value})`;break;case"TROOP_ATTACK":const L=x.rollToHero||0;E=`[${x.actorSide==="my"?"我方":"敌方"}] ${c(x.attackerTroopType)} 进攻! <span style="color:#ffd700">🎲Roll: ${L}</span> (${L}% 打主将, ${100-L}% 打兵)`;break;case"HERO_HP_CHANGE":const C=x.value;E=`> [${x.side==="my"?"我方":"敌方"}] 主将 `+(C<0?`<span class="log-dmg">HP ${C}</span>`:`<span class="log-heal">HP +${C}</span>`);break;case"TROOP_STACK_CHANGE":E=`> [${x.side==="my"?"我方":"敌方"}] ${c(x.troopType)}: <span class="log-dmg">损失 ${x.killed} 单位</span> (Remaining: ${x.countAfter})`;break;default:x.desc?E=x.desc:E=JSON.stringify(x)}I.innerHTML=E,v.prepend(I)})}function c(e){return A(e)}async function r(e){if(!m){m=!0;try{const v=await q.processTurn(d,e,g.turnNo+1,k);if(v.code===200){const x=v.data;x.lastEvents&&o(x.lastEvents),g=x,l(x),m=!1,t()}else console.error(v.message),m=!1}catch(v){console.error(v),m=!1}}}function y(e){const v=document.getElementById("result-modal"),x=document.getElementById("result-title"),I=document.getElementById("result-desc");v.style.display="flex",x.textContent=e?"VICTORY!":"DEFEAT",x.style.color=e?"var(--success-color)":"var(--danger-color)",I.textContent=e?"战斗胜利！获得金币与战利品。":"战斗失败，请强化武将后再试。",document.getElementById("result-ok").onclick=()=>{h.navigate("/hall")}}document.getElementById("flee-btn").addEventListener("click",()=>{confirm("确定要撤退吗？(视为战败)")&&h.navigate("/hall")})}function Z(i,n){var y;const d=localStorage.getItem("userId");if(!d){h.navigate("/login");return}const{civ:g,stageNo:m}=n;if(!g||!m){h.navigate("/hall");return}let f=[],k=null,u=[],l={};i.innerHTML=`
      <div class="prepare-page">
        <div class="page-header">
           <button class="btn btn-sm btn-secondary" id="back-btn">← 放弃出征</button>
           <h2>⚔️ 战前整备 - ${g} 第${m}关</h2>
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
    `;const p=document.createElement("style");p.id="prepare-page-style",(y=document.getElementById("prepare-page-style"))==null||y.remove(),p.textContent=`
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
    `,document.head.appendChild(p),a();async function a(){try{const[b,e,v]=await Promise.all([$.getGenerals(d),T.getInfo(d),S.getCodex(d)]);b.code===200&&(f=b.data.filter(I=>I.activated),s());const x=new Set;v.code===200&&v.data&&v.data.forEach(I=>{I.status>=2&&I.civ===g&&x.add(I.troopId)}),e.code===200&&e.data.troops&&(u=(e.data.troops||[]).filter(I=>x.has(I.troopId)&&(I.count||0)>0).sort((I,E)=>(I.troopId||0)-(E.troopId||0)))}catch(b){console.error(b)}}function s(){const b=document.getElementById("generals-list");if(f.length===0){b.innerHTML='<div class="empty-tip">没有激活的武将，请先去大厅激活</div>';return}b.innerHTML=f.map(e=>`
           <div class="general-card ${k===e.id?"selected":""}" data-id="${e.id}">
              <div class="g-avatar">🤴</div>
              <div class="g-info">
                 <div class="g-name">${e.name} <span class="g-lv">Lv.${e.level}</span></div>
                 <div class="g-stats">
                    <span style="color:var(--accent-color)">CAP: ${e.capacity||0}</span>
                 </div>
              </div>
           </div>
        `).join(""),b.querySelectorAll(".general-card").forEach(e=>{e.addEventListener("click",()=>t(parseInt(e.dataset.id)))}),!k&&f.length>0&&t(f[0].id)}function t(b){k=b,document.querySelectorAll(".general-card").forEach(e=>{e.classList.toggle("selected",parseInt(e.dataset.id)===b)}),o(),r()}function o(){if(!k)return;const b=document.getElementById("troops-list");if(u.length===0){b.innerHTML='<div class="empty-tip">没有兵力，请先去招募</div>';return}b.innerHTML=u.map(e=>{const v=z(e.troopId),x=l[e.troopId]||0,I=e.count||0;return`
            <div class="troop-row">
               <div class="t-name">${v.icon} ${v.name}${v.isElite?" [特种]":""} (余:${I})</div>
               <div class="t-control">
                  <button class="btn-tiny btn-minus" data-id="${e.troopId}">-10</button>
                  <input type="number" class="troop-input" id="input-${e.troopId}" value="${x}" max="${I}" min="0">
                  <button class="btn-tiny btn-plus" data-id="${e.troopId}">+10</button>
               </div>
            </div>
            `}).join(""),u.forEach(e=>{const v=document.getElementById("input-"+e.troopId);v&&(v.onchange=x=>{let I=parseInt(x.target.value)||0;c(e.troopId,I)})}),b.querySelectorAll(".btn-minus").forEach(e=>{e.onclick=()=>{const v=parseInt(e.dataset.id);c(v,(l[v]||0)-10)}}),b.querySelectorAll(".btn-plus").forEach(e=>{e.onclick=()=>{const v=parseInt(e.dataset.id);c(v,(l[v]||0)+10)}}),r()}function c(b,e){const v=u.find(E=>E.troopId===b);if(!v)return;const x=v.count||0;e=Math.max(0,Math.min(e,x)),l[b]=e;const I=document.getElementById("input-"+b);I&&(I.value=e),r()}function r(){const b=f.find(C=>C.id===k);if(!b)return;const e=Object.values(l).reduce((C,B)=>C+B,0),v=Object.entries(l).reduce((C,[B,H])=>{const G=z(parseInt(B,10));return C+(H||0)*(G.capCost||1)},0),x=b.capacity||0,I=document.getElementById("capacity-display");I.textContent=`(统率占用: ${v}/${x}，总兵数: ${e})`;const E=document.getElementById("start-btn"),L=e>0&&v<=x;E.disabled=!L,v>x?(I.style.color="var(--danger-color)",E.textContent="兵力超过统率上限"):e===0?(I.style.color="#aaa",E.textContent="请分配兵力"):(I.style.color="var(--success-color)",E.textContent="出征！")}document.getElementById("start-btn").onclick=async()=>{const b=document.getElementById("start-btn");b.disabled=!0,b.textContent="出征中...";try{const e=await q.startStoryBattle(d,g,m,k,l);e.code===200?h.navigate("/battle"):(alert(e.message||"出征失败"),b.disabled=!1,b.textContent="出征！")}catch(e){console.error(e),alert("请求失败"),b.disabled=!1,b.textContent="出征！"}},document.getElementById("back-btn").onclick=()=>h.navigate("/stages")}function ee(i){var p;const n=localStorage.getItem("userId");if(!n){h.navigate("/login");return}i.innerHTML=`
    <div class="codex-page">
      <nav class="page-nav">
        <button class="btn btn-secondary btn-sm" id="back-btn">← 返回大厅</button>
        <h1 class="page-title">兵种图鉴</h1>
      </nav>

      <div class="codex-content">
        <div class="filters">
            <button class="filter-btn active" data-filter="all">全部</button>
            <button class="filter-btn" data-filter="CN">汉</button>
            <button class="filter-btn" data-filter="JP">和</button>
            <button class="filter-btn" data-filter="KR">鲜</button>
            <button class="filter-btn" data-filter="GB">英</button>
        </div>
        <div id="codex-list" class="codex-grid">
          <div class="spinner"></div>
        </div>
      </div>
      
      <div class="toast" id="toast"></div>
    </div>
  `;const d=document.createElement("style");d.id="codex-page-style",(p=document.getElementById("codex-page-style"))==null||p.remove(),d.textContent=`
    .codex-page { min-height: 100vh; background: linear-gradient(135deg, var(--bg-dark) 0%, var(--bg-medium) 100%); }
    .page-nav { background: rgba(0,0,0,0.3); backdrop-filter: blur(10px); padding: var(--spacing-md) var(--spacing-lg); display:flex; align-items:center; gap: var(--spacing-lg); border-bottom: 2px solid rgba(255,255,255,0.1); position: sticky; top: 0; z-index: 100; }
    .page-title { flex: 1; font-size: 1.3rem; background: linear-gradient(135deg, var(--primary-color), var(--accent-color)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
    
    .codex-content { max-width: 1000px; margin: 0 auto; padding: var(--spacing-xl); display: flex; flex-direction: column; gap: var(--spacing-lg); }
    
    .filters { display: flex; gap: 10px; justify-content: center; margin-bottom: 20px; }
    .filter-btn { background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); color: #ccc; padding: 6px 16px; border-radius: 20px; cursor: pointer; transition: all 0.2s; }
    .filter-btn.active, .filter-btn:hover { background: var(--primary-color); color: #fff; border-color: var(--primary-color); }
    
    .codex-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
    
    .codex-card { background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; padding: 16px; display: flex; flex-direction: column; gap: 12px; position: relative; overflow: hidden; transition: transform 0.2s; }
    .codex-card:hover { transform: translateY(-2px); background: rgba(255,255,255,0.08); }
    .codex-card.locked { opacity: 0.7; filter: grayscale(0.8); }
    
    .card-header { display: flex; align-items: center; gap: 12px; }
    .avatar { width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.5rem; background: #333; position: relative; }
    .civ-badge { position: absolute; bottom: -2px; right: -2px; font-size: 0.7rem; background: #000; padding: 2px 4px; border-radius: 4px; border: 1px solid #555; }
    
    .card-info h3 { margin: 0; font-size: 1.1rem; color: #fff; }
    .card-info p { margin: 2px 0 0; font-size: 0.85rem; color: #aaa; }
    
    .card-stats { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 0.9rem; color: #ddd; background: rgba(0,0,0,0.2); padding: 8px; border-radius: 6px; }
    .stat-row { display: flex; justify-content: space-between; }
    
    .evolution-section { border-top: 1px solid rgba(255,255,255,0.1); padding-top: 10px; margin-top: auto; }
    .evo-status { font-size: 0.85rem; color: #aaa; display: flex; justify-content: space-between; align-items: center; }
    .evo-btn { padding: 4px 12px; font-size: 0.85rem; }
    
    .locked-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; flex-direction: column; z-index: 10; backdrop-filter: blur(2px); text-align: center; padding: 20px; }
    .lock-icon { font-size: 2rem; margin-bottom: 10px; }
    .lock-text { color: #ef4444; font-weight: bold; font-size: 0.9rem; }
    
    .toast { position: fixed; bottom: 40px; left: 50%; transform: translateX(-50%); padding: 12px 28px; border-radius: var(--radius-lg); font-weight: bold; opacity: 0; pointer-events: none; transition: opacity .3s ease, transform .3s ease; z-index: 999; }
    .toast.show { opacity: 1; transform: translateX(-50%) translateY(-10px); }
    .toast.success { background: var(--success-color); color:#fff; }
    .toast.error { background: var(--danger-color); color:#fff; }
  `,document.head.appendChild(d);let g=[],m="all";f();async function f(){try{const a=await S.getCodex(n);a.code===200&&a.data&&(g=a.data,k())}catch(a){console.error(a),document.getElementById("codex-list").innerHTML='<div style="text-align:center;width:100%">加载失败</div>'}}function k(){const a=document.getElementById("codex-list");let s=g;m!=="all"&&(s=g.filter(t=>t.civ===m)),s.sort((t,o)=>t.status!==o.status?o.status-t.status:t.troopId-o.troopId),a.innerHTML=s.map(t=>{const o=z(t.troopId),c=t.status<2,r=t.status===0;return`
            <div class="codex-card ${c?"locked":""}">
                <div class="card-header">
                    <div class="avatar" style="background:${o.color}">
                        ${r?"?":o.icon}
                        <span class="civ-badge">${t.civ}</span>
                    </div>
                    <div class="card-info">
                        <h3>${t.name}</h3>
                        <p>${A(t.type)} ${t.isElite?"· 特种":""}</p>
                    </div>
                </div>
                
                <div class="card-stats">
                    <div class="stat-row"><span>攻击</span> <strong>${t.baseAtk}</strong></div>
                    <div class="stat-row"><span>生命</span> <strong>${t.baseHp}</strong></div>
                    <div class="stat-row"><span>统率</span> <strong>${t.cost}</strong></div>
                    <div class="stat-row"><span>射程</span> <strong>${t.type==="ARC"?"远":"近"}</strong></div>
                </div>
                
                <div class="evolution-section">
                    <div class="evo-status">
                        <span>进化阶段: ${t.evolutionTier||0}阶</span>
                        ${!c&&t.evolutionUnlocked?`<button class="btn btn-primary btn-sm evo-btn" data-id="${t.troopId}">进化</button>`:'<span style="font-size:0.8em;color:#777">进化未解锁</span>'}
                    </div>
                </div>
                
                ${c?`
                    <div class="locked-overlay">
                        <div class="lock-icon">🔒</div>
                        <div class="lock-text">
                            ${t.status===1?"已发现":"未解锁"}<br>
                            <span style="font-size:0.8em;color:#aaa">${t.unlockHint||`需通关 ${t.civ} 关卡`}</span>
                        </div>
                    </div>
                `:""}
            </div>
        `}).join(""),document.querySelectorAll(".evo-btn").forEach(t=>{t.onclick=()=>u(t.dataset.id)})}async function u(a){if(confirm("确定要进化该兵种吗？需要消耗金币并满足关卡条件。"))try{const s=await S.evolve(n,parseInt(a));s.code===200?(l("进化成功！","success"),f()):l(s.message||"进化失败","error")}catch(s){l(s.message||"请求失败","error")}}document.querySelectorAll(".filter-btn").forEach(a=>{a.onclick=()=>{document.querySelectorAll(".filter-btn").forEach(s=>s.classList.remove("active")),a.classList.add("active"),m=a.dataset.filter,k()}}),document.getElementById("back-btn").addEventListener("click",()=>h.navigate("/hall"));function l(a,s="success"){const t=document.getElementById("toast");t.textContent=a,t.className=`toast ${s} show`,setTimeout(()=>{t.className="toast"},2e3)}}h.register("/login",N);h.register("/hall",K);h.register("/recruit",V);h.register("/generals",D);h.register("/stages",J);h.register("/equipment",U);h.register("/gem",Y);h.register("/skill",X);h.register("/battle",Q);h.register("/battle/prepare",Z);h.register("/codex",ee);function te(){localStorage.getItem("userId")?h.navigate("/hall"):h.navigate("/login")}te();
//# sourceMappingURL=index-xM08aWFt.js.map
