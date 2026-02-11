(function(){const t=document.createElement("link").relList;if(t&&t.supports&&t.supports("modulepreload"))return;for(const v of document.querySelectorAll('link[rel="modulepreload"]'))g(v);new MutationObserver(v=>{for(const f of v)if(f.type==="childList")for(const k of f.addedNodes)k.tagName==="LINK"&&k.rel==="modulepreload"&&g(k)}).observe(document,{childList:!0,subtree:!0});function r(v){const f={};return v.integrity&&(f.integrity=v.integrity),v.referrerPolicy&&(f.referrerPolicy=v.referrerPolicy),v.crossOrigin==="use-credentials"?f.credentials="include":v.crossOrigin==="anonymous"?f.credentials="omit":f.credentials="same-origin",f}function g(v){if(v.ep)return;v.ep=!0;const f=r(v);fetch(v.href,f)}})();class R{constructor(){this.routes={},this.currentRoute=null}register(t,r){this.routes[t]=r}navigate(t,r={}){const g=this.routes[t];if(!g){console.error(`Route not found: ${t}`);return}this.currentRoute=t;const v=document.getElementById("app");v.innerHTML="",g(v,r)}back(){window.history.back()}}const h=new R,H="/api";class w{static async request(t,r={}){const g=`${H}${t}`,v={headers:{"Content-Type":"application/json",...r.headers},...r};try{const f=await fetch(g,v),k=f.headers.get("content-type")||"";let m;if(k.includes("application/json")?m=await f.json():m=await f.text(),!f.ok){const d=typeof m=="object"&&m.message?m.message:m;throw new Error(d||`请求失败 (${f.status})`)}return m}catch(f){throw console.error(`API Error [${t}]:`,f),f}}static get(t,r){const g=r?"?"+new URLSearchParams(r).toString():"";return this.request(t+g,{method:"GET"})}static post(t,r){return this.request(t,{method:"POST",body:r?JSON.stringify(r):void 0})}static postWithParams(t,r,g){const v=r?"?"+new URLSearchParams(r).toString():"";return this.request(t+v,{method:"POST",body:g?JSON.stringify(g):void 0})}}const q={register(o,t,r,g){return w.post("/auth/register",{username:o,password:t,nickname:r,initialCiv:g})},login(o,t){return w.post("/auth/login",{username:o,password:t})}},B={getInfo(o){return w.get("/player/info",{userId:o})},getProgress(o){return w.get("/player/progress",{userId:o})}},$={getGenerals(o){return w.get("/hall/generals",{userId:o})},getProgress(o){return w.get("/hall/progress",{userId:o})},upgradeGeneral(o,t){return w.postWithParams("/hall/general/upgrade",{userId:o,generalId:t})},activateGeneral(o,t){return w.postWithParams("/hall/general/activate",{userId:o,generalId:t})},ascendGeneral(o,t){return w.postWithParams("/hall/general/ascend",{userId:o,generalId:t})},recruit(o,t,r){return w.postWithParams("/hall/recruit",{userId:o},{troopId:t,count:r})},equipGeneral(o,t,r){return w.postWithParams("/hall/general/equip",{userId:o},{generalId:t,equipmentId:r})},enhanceEquipment(o,t){return w.postWithParams("/hall/equipment/enhance",{userId:o,equipmentId:t})},learnSkill(o,t,r){return w.postWithParams("/hall/skill/learn",{userId:o,generalId:t,bookItemId:r})},inlayGem(o,t,r,g){return w.postWithParams("/hall/gem/inlay",{userId:o},{equipmentId:t,socketIndex:r,gemId:g})},combineGem(o,t,r){return w.postWithParams("/hall/gem/combine",{userId:o},{gemType:t,level:r})},getEquipments(o){return w.get("/hall/equipments",{userId:o})},getGems(o){return w.get("/hall/gems",{userId:o})},getItems(o){return w.get("/hall/items",{userId:o})}},z={startStoryBattle(o,t,r,g,v){return w.postWithParams("/battle/story/start",{userId:o},{civ:t,stageNo:r,generalId:g,troopConfig:v})},processTurn:(o,t,r,g)=>w.postWithParams("/battle/turn",{userId:o},{castSkill:t,clientTurnNo:r,tactics:g}),getBattleState(o){return w.get("/battle/state",{userId:o})}};function M(o){var s;o.innerHTML=`
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
  `;const t=document.createElement("style");t.id="login-page-style",(s=document.getElementById("login-page-style"))==null||s.remove(),t.textContent=`
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
  `,document.head.appendChild(t);let r=!1,g="CN";const v=document.getElementById("username"),f=document.getElementById("password"),k=document.getElementById("nickname"),m=document.getElementById("register-fields"),d=document.getElementById("login-btn"),p=document.getElementById("register-btn"),c=document.getElementById("message"),i=document.querySelectorAll(".civ-option");i.forEach(n=>{n.addEventListener("click",()=>{i.forEach(x=>x.classList.remove("selected")),n.classList.add("selected"),g=n.dataset.civ})});function l(n,x="error"){c.className=`message ${x}`,c.textContent=n}function a(n){localStorage.setItem("userId",n.id),localStorage.setItem("username",n.nickname),l("登录成功！正在进入游戏...","success"),setTimeout(()=>h.navigate("/hall"),600)}p.addEventListener("click",()=>{r=!r,m.style.display=r?"flex":"none",d.textContent=r?"确认注册":"登录",p.textContent=r?"返回登录":"注册",c.textContent=""}),d.addEventListener("click",async()=>{const n=v.value.trim(),x=f.value.trim();if(!n||!x){l("请输入账号和密码");return}d.disabled=!0,p.disabled=!0;try{if(r){const b=k.value.trim();if(!b){l("请输入昵称");return}d.textContent="注册中...";const e=await q.register(n,x,b,g);console.log("注册结果:",e),e.code===200&&e.data?a(e.data):l(e.message||"注册失败")}else{d.textContent="登录中...";const b=await q.login(n,x);console.log("登录结果:",b),b.code===200&&b.data?a(b.data):l(b.message||"登录失败")}}catch(b){l(b.message||"操作失败，请重试")}finally{d.disabled=!1,p.disabled=!1,d.textContent=r?"确认注册":"登录"}}),o.addEventListener("keypress",n=>{n.key==="Enter"&&d.click()})}const P={2001:{troopId:2001,civ:"CN",type:"INF",name:"步兵",icon:"🛡️",color:"var(--inf-color)",recruitGold:20,capCost:2,isElite:!1},2002:{troopId:2002,civ:"CN",type:"ARC",name:"弓兵",icon:"🏹",color:"var(--arc-color)",recruitGold:20,capCost:2,isElite:!1},2003:{troopId:2003,civ:"CN",type:"CAV",name:"骑兵",icon:"🐎",color:"var(--cav-color)",recruitGold:40,capCost:3,isElite:!1},2101:{troopId:2101,civ:"JP",type:"INF",name:"足轻",icon:"🛡️",color:"var(--inf-color)",recruitGold:20,capCost:2,isElite:!1},2102:{troopId:2102,civ:"JP",type:"ARC",name:"弓足轻",icon:"🏹",color:"var(--arc-color)",recruitGold:20,capCost:2,isElite:!1},2103:{troopId:2103,civ:"JP",type:"CAV",name:"骑马武者",icon:"🐎",color:"var(--cav-color)",recruitGold:40,capCost:3,isElite:!1},2201:{troopId:2201,civ:"KR",type:"INF",name:"步卒",icon:"🛡️",color:"var(--inf-color)",recruitGold:20,capCost:2,isElite:!1},2202:{troopId:2202,civ:"KR",type:"ARC",name:"弓手",icon:"🏹",color:"var(--arc-color)",recruitGold:20,capCost:2,isElite:!1},2203:{troopId:2203,civ:"KR",type:"CAV",name:"骑卒",icon:"🐎",color:"var(--cav-color)",recruitGold:40,capCost:3,isElite:!1},2301:{troopId:2301,civ:"GB",type:"INF",name:"Footman",icon:"🛡️",color:"var(--inf-color)",recruitGold:20,capCost:2,isElite:!1},2302:{troopId:2302,civ:"GB",type:"ARC",name:"Archer",icon:"🏹",color:"var(--arc-color)",recruitGold:20,capCost:2,isElite:!1},2303:{troopId:2303,civ:"GB",type:"CAV",name:"Cavalry",icon:"🐎",color:"var(--cav-color)",recruitGold:40,capCost:3,isElite:!1},3001:{troopId:3001,civ:"CN",type:"ARC",name:"青囊医官",icon:"💊",color:"#10b981",recruitGold:120,capCost:3,isElite:!0},3002:{troopId:3002,civ:"JP",type:"ARC",name:"爆裂火筒队",icon:"🔥",color:"#f97316",recruitGold:130,capCost:3,isElite:!0},3003:{troopId:3003,civ:"KR",type:"INF",name:"军乐旗卫",icon:"🎺",color:"#06b6d4",recruitGold:130,capCost:3,isElite:!0},3004:{troopId:3004,civ:"GB",type:"INF",name:"破甲工兵",icon:"🪓",color:"#a3a3a3",recruitGold:140,capCost:3,isElite:!0}},N={weapon:"武器",armor1:"防具",helm:"头盔",boots:"鞋子",mount:"坐骑",accessory:"饰品"},j={301:{name:"鼓舞技能书"},302:{name:"乱舞技能书"},303:{name:"毒箭技能书"},304:{name:"威压技能书"},305:{name:"铁壁技能书"},306:{name:"伏兵技能书"}};function S(o){return P[o]||{troopId:o,civ:"",type:"INF",name:`兵种#${o}`,icon:"⚔️",color:"var(--text-secondary)",recruitGold:20,capCost:1,isElite:!1}}function O(o){return{INF:"步兵",ARC:"弓兵",CAV:"骑兵"}[o]||o}function _(o){return N[o]||"装备"}function F(o){return o==="weapon"?"⚔️":o==="armor1"?"🛡️":o==="helm"?"🪖":o==="boots"?"👢":o==="mount"?"🐎":o==="accessory"?"💍":"📦"}function K(o){const t=localStorage.getItem("userId");if(!t){h.navigate("/login");return}o.innerHTML=`
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
            <button class="btn btn-secondary" id="tower-btn">爬塔挑战</button>
          </div>
        </div>
      </div>
    </div>
  `;const r=document.createElement("style");r.textContent=`
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
  `,document.head.appendChild(r),g(),v(),f();async function g(){try{const m=await B.getInfo(t);console.log("玩家数据:",m),m.code===200&&m.data?(document.getElementById("player-name").textContent=m.data.nickname,document.getElementById("gold-count").textContent=(m.data.gold||0).toLocaleString(),document.getElementById("diamond-count").textContent=(m.data.diamond||0).toLocaleString(),k(m.data.troops||[],m.data.initialCiv||"CN")):console.warn("加载玩家数据: code不为200",m)}catch(m){console.error("加载玩家数据失败:",m)}}async function v(){try{const m=await $.getGenerals(t);console.log("武将数据:",m);const d=document.getElementById("generals-grid");m.code===200&&m.data&&m.data.length>0?d.innerHTML=m.data.slice(0,4).map(p=>`
          <div class="general-card" data-id="${p.id}">
            <div class="general-avatar">🎖️</div>
            <div class="general-name">武将#${p.templateId}</div>
            <div class="general-level">Lv.${p.level}</div>
            ${p.activated?'<div class="badge badge-inf">已激活</div>':'<div class="badge" style="background:rgba(255,255,255,0.1)">未激活</div>'}
          </div>
        `).join(""):d.innerHTML='<p style="grid-column: 1/-1; text-align: center;">暂无武将</p>'}catch(m){console.error("加载武将失败:",m),document.getElementById("generals-grid").innerHTML='<p style="grid-column: 1/-1; text-align: center; color: var(--danger-color);">加载失败</p>'}}async function f(){try{const m=await $.getProgress(t);console.log("进度数据:",m);const d=document.getElementById("progress-grid");if(m.code===200&&m.data&&m.data.length>0){const p={CN:"🇨🇳 中国",JP:"🇯🇵 日本",KR:"🇰🇷 韩国",GB:"🇬🇧 英国"};d.innerHTML=m.data.map(c=>`
          <div class="progress-item">
            <div class="progress-civ">
              <span class="badge badge-${c.civ.toLowerCase()}">${p[c.civ]||c.civ}</span>
            </div>
            <div class="progress-stage">
              当前进度: ${c.maxStageCleared||0}/10 关
              ${c.unlocked?"":'<br/><span style="color: var(--danger-color);">🔒 未解锁</span>'}
            </div>
          </div>
        `).join("")}else d.innerHTML='<p style="grid-column: 1/-1; text-align: center;">暂无进度</p>'}catch(m){console.error("加载进度失败:",m)}}function k(m,d){const p=document.getElementById("troops-grid");if(!p)return;const c=(m||[]).map(i=>{const l=S(i.troopId);return{troopId:i.troopId,count:i.count||0,civ:l.civ,type:l.type,icon:l.icon,name:l.name,color:l.color,isElite:!!l.isElite}}).filter(i=>i.civ===d).sort((i,l)=>{if(i.isElite!==l.isElite)return i.isElite?1:-1;const a={INF:1,ARC:2,CAV:3};return(a[i.type]||9)-(a[l.type]||9)});if(c.length===0){p.innerHTML='<p style="grid-column: 1/-1; text-align: center;">暂无兵力</p>';return}p.innerHTML=c.map(i=>`
      <div class="troop-item">
        <div class="troop-icon" style="background: ${i.color};">${i.icon}</div>
        <div class="troop-info">
          <span class="troop-name">${i.name}${i.isElite?" [特种]":""}</span>
          <span class="troop-count">${i.count.toLocaleString()}</span>
        </div>
      </div>
    `).join("")}document.getElementById("logout-btn").addEventListener("click",()=>{localStorage.clear(),h.navigate("/login")}),document.getElementById("select-stage-btn").addEventListener("click",()=>{h.navigate("/stages")}),document.getElementById("manage-generals-btn").addEventListener("click",()=>{h.navigate("/generals")}),document.getElementById("recruit-btn").addEventListener("click",()=>{h.navigate("/recruit")}),document.getElementById("equip-btn").addEventListener("click",()=>{h.navigate("/equipment")}),document.getElementById("gem-btn").addEventListener("click",()=>{h.navigate("/gem")}),document.getElementById("skill-btn").addEventListener("click",()=>{h.navigate("/skill")}),document.getElementById("tower-btn").addEventListener("click",()=>{alert("爬塔功能敬请期待")})}function W(o){var m;const t=localStorage.getItem("userId");if(!t){h.navigate("/login");return}o.innerHTML=`
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
  `;const r=document.createElement("style");r.id="recruit-page-style",(m=document.getElementById("recruit-page-style"))==null||m.remove(),r.textContent=`
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
  `,document.head.appendChild(r),g();async function g(){try{const[d,p]=await Promise.all([B.getInfo(t),$.getProgress(t)]),c={};d.code===200&&d.data&&(document.getElementById("gold-display").textContent=(d.data.gold||0).toLocaleString(),(d.data.troops||[]).forEach(a=>{c[a.troopId]=a.count||0}));let i=["CN"];p.code===200&&p.data&&(i=p.data.filter(a=>a.unlocked).map(a=>a.civ));const l=Object.values(P).filter(a=>i.includes(a.civ)).sort((a,s)=>a.troopId-s.troopId);v(l,c)}catch(d){console.error("加载数据失败:",d),document.getElementById("recruit-list").innerHTML='<p style="text-align:center">加载失败</p>'}}function v(d,p){const c=document.getElementById("recruit-list");c.innerHTML=d.map(i=>`
      <div class="recruit-card card" data-troop-id="${i.troopId}">
        <div class="troop-header">
          <div class="troop-avatar" style="background:${i.color};">${i.icon}</div>
          <div>
            <h3>${i.name} ${i.isElite?'<span style="font-size:.8em;color:#ffd166">[特种]</span>':""}</h3>
            <p class="troop-desc">${i.type} · ${i.civ}</p>
          </div>
        </div>
        <div class="troop-stats">
          <span>当前拥有: <strong>${(p[i.troopId]||0).toLocaleString()}</strong></span>
          <span>单价: <strong>${i.recruitGold}</strong> 金</span>
        </div>
        <div class="recruit-controls">
          <button class="btn btn-sm qty-btn" data-delta="-10">-10</button>
          <button class="btn btn-sm qty-btn" data-delta="-1">-1</button>
          <input type="number" class="recruit-input" id="qty-${i.troopId}" value="10" min="1" />
          <button class="btn btn-sm qty-btn" data-delta="1">+1</button>
          <button class="btn btn-sm qty-btn" data-delta="10">+10</button>
        </div>
        <button class="btn btn-primary recruit-btn" data-troop-id="${i.troopId}">招募${i.name}</button>
      </div>
    `).join(""),f()}function f(){document.querySelectorAll(".qty-btn").forEach(d=>{d.onclick=()=>{const p=d.parentElement.querySelector(".recruit-input"),c=parseInt(d.dataset.delta||"0",10),i=Math.max(1,(parseInt(p.value||"1",10)||1)+c);p.value=String(i)}}),document.querySelectorAll(".recruit-btn").forEach(d=>{d.onclick=async()=>{const p=parseInt(d.dataset.troopId||"0",10),c=document.getElementById(`qty-${p}`),i=parseInt((c==null?void 0:c.value)||"0",10);if(!(i<=0)){d.disabled=!0;try{const l=await $.recruit(t,p,i);l.code===200?(k("招募成功","success"),g()):k(l.message||"招募失败","error")}catch(l){k(l.message||"请求失败","error")}finally{d.disabled=!1}}}})}document.getElementById("back-btn").addEventListener("click",()=>h.navigate("/hall"));function k(d,p="success"){const c=document.getElementById("toast");c.textContent=d,c.className=`toast ${p} show`,setTimeout(()=>{c.className="toast"},2e3)}}function V(o){var l;const t=localStorage.getItem("userId");if(!t){h.navigate("/login");return}let r=[],g=null;const v=[{key:"weapon",label:"武器",icon:"⚔️"},{key:"armor1",label:"防具",icon:"🛡️"},{key:"helm",label:"头盔",icon:"🪖"},{key:"boots",label:"鞋子",icon:"👢"},{key:"mount",label:"坐骑",icon:"🐎"},{key:"accessory",label:"饰品",icon:"💍"}];o.innerHTML=`
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
  `;const f=document.createElement("style");f.id="generals-page-style",(l=document.getElementById("generals-page-style"))==null||l.remove(),f.textContent=`
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
  `,document.head.appendChild(f),k();async function k(){try{const[a,s,n]=await Promise.all([$.getGenerals(t),$.getEquipments(t),B.getInfo(t)]);n.code===200&&n.data&&(document.getElementById("gold-display").textContent=(n.data.gold||0).toLocaleString()),s.code===200&&(r=s.data||[]);const x=document.getElementById("generals-content");a.code===200&&a.data&&a.data.length>0?m(a.data,x):x.innerHTML='<p style="text-align:center;width:100%;">暂无武将</p>'}catch(a){console.error(a)}}function m(a,s){s.innerHTML=a.map(n=>{const x=v.map(b=>{const e=r.find(u=>u.generalId===n.id&&u.slot===b.key);return`
              <div class="equip-slot ${e?"equipped":""}" data-slot="${b.key}" data-id="${n.id}">
                 <div class="slot-icon">${e?b.icon:"+"}</div>
                 <div class="slot-label">${b.label}</div>
              </div>
            `}).join("");return`
          <div class="general-detail-card ${n.activated?"":"inactive"}">
            <div class="gd-top">
               <div class="gd-avatar">🎖️</div>
               <div class="gd-info">
                 <div class="gd-name">${n.name||"武将"} #${n.templateId}</div>
                 <div class="gd-meta">
                   <span>Lv.${n.level}</span> · <span>阶 ${n.tier}</span> · <span>${n.activated?"已激活":"未激活"}</span>
                 </div>
               </div>
            </div>
            
            <div class="stats-row">
               <div class="stat-item"><span class="stat-val">${n.currentHp}/${n.maxHp}</span><span class="stat-lbl">HP</span></div>
               <div class="stat-item"><span class="stat-val">${n.atk||0}</span><span class="stat-lbl">攻击</span></div>
               <div class="stat-item"><span class="stat-val">${n.speed||0}</span><span class="stat-lbl">速度</span></div>
               <div class="stat-item"><span class="stat-val">${n.capacity||0}</span><span class="stat-lbl">统率</span></div>
            </div>

            <div class="skill-section">
               <div class="skill-title">💡 技能: ${n.skillName||"无"}</div>
               <div class="skill-desc">${n.skillDesc||"暂无效果"}</div>
            </div>

            <div class="equip-slots">
                ${n.activated?x:'<div style="grid-column:1/-1;text-align:center;font-size:0.8rem;padding:10px;">需激活后才可穿戴装备</div>'}
            </div>

            <div class="gd-actions">
               ${!n.activated&&n.unlocked?`<button class="btn btn-primary action-btn" data-action="activate" data-id="${n.id}">激活</button>`:""}
               ${n.activated?`<button class="btn btn-primary action-btn" data-action="upgrade" data-id="${n.id}">升级</button>`:""}
               ${n.activated?`<button class="btn btn-secondary action-btn" data-action="ascend" data-id="${n.id}">升阶</button>`:""}
            </div>
          </div>
        `}).join(""),s.querySelectorAll(".action-btn").forEach(n=>n.addEventListener("click",()=>c(n.dataset.action,n.dataset.id))),s.querySelectorAll(".equip-slot").forEach(n=>n.addEventListener("click",()=>d(n.dataset.id,n.dataset.slot)))}function d(a,s){g=parseInt(a);const n=document.getElementById("equip-list"),x=r.filter(e=>e.slot===s&&(!e.generalId||e.generalId===g)),b=x.length?x.map(e=>`
        <div class="equip-item" data-id="${e.id}">
           <div style="font-size:1.5rem">${v.find(u=>u.key===s).icon}</div>
           <div>
              <div style="font-weight:bold">${e.name} +${e.enhanceLevel}</div>
              <div style="font-size:0.8rem;color:#888;">${e.generalId===g?"当前装备":e.generalId?"他人装备":"闲置"}</div>
           </div>
        </div>
      `).join(""):'<p style="text-align:center;color:#888;">暂无可用装备</p>';n.innerHTML=b,n.querySelectorAll(".equip-item").forEach(e=>e.addEventListener("click",()=>p(e.dataset.id))),document.getElementById("equip-modal").style.display="flex"}async function p(a){try{const s=await $.equipGeneral(t,g,a);s.code===200?(i("装备成功"),document.getElementById("equip-modal").style.display="none",k()):i(s.message,"error")}catch(s){i(s.message,"error")}}async function c(a,s){try{let n;a==="activate"&&(n=$.activateGeneral(t,s)),a==="upgrade"&&(n=$.upgradeGeneral(t,s)),a==="ascend"&&(n=$.ascendGeneral(t,s));const x=await n;x.code===200?(i("操作成功"),k()):i(x.message,"error")}catch(n){i(n.message,"error")}}document.getElementById("close-modal").addEventListener("click",()=>document.getElementById("equip-modal").style.display="none"),document.getElementById("back-btn").addEventListener("click",()=>h.navigate("/hall"));function i(a,s="success"){const n=document.getElementById("toast");n.textContent=a,n.className=`toast ${s} show`,setTimeout(()=>{n.className="toast"},2e3)}}function J(o){var d;const t=localStorage.getItem("userId");if(!t){h.navigate("/login");return}o.innerHTML=`
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
  `;const r=document.createElement("style");r.id="stage-page-style",(d=document.getElementById("stage-page-style"))==null||d.remove(),r.textContent=`
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
  `,document.head.appendChild(r);let g="CN",v=[];f();async function f(){document.querySelectorAll(".civ-tab").forEach(p=>{p.addEventListener("click",()=>{document.querySelectorAll(".civ-tab").forEach(c=>c.classList.remove("active")),p.classList.add("active"),g=p.dataset.civ,k()})}),document.getElementById("back-btn").addEventListener("click",()=>{h.navigate("/hall")});try{const[p,c]=await Promise.all([$.getProgress(t),B.getInfo(t)]);c.code===200&&c.data&&(document.getElementById("gold-display").textContent=(c.data.gold||0).toLocaleString()),p.code===200&&p.data&&(v=p.data,k())}catch(p){console.error("加载失败",p),document.getElementById("stage-grid").innerHTML="<p>加载失败，请重试</p>"}}function k(){const p=document.getElementById("stage-grid");p.innerHTML="";const c=v.find(a=>a.civ===g);if(!c||!c.unlocked){p.innerHTML=`
        <div style="grid-column:1/-1; text-align:center; padding: 40px;">
          <h2>🔒 该势力尚未解锁</h2>
          <p style="color:var(--text-secondary)">请先通关前置势力的主线关卡</p>
        </div>
      `;return}const i=10,l=c.maxStageCleared||0;for(let a=1;a<=i;a++){const s=a<=l,n=a<=l+1,x=document.createElement("div");x.className=`stage-card ${s?"cleared":""} ${n?"unlocked":"locked"}`;let b="普通";(a===5||a===9)&&(b="🏰 攻城"),a===10&&(b="👹 BOSS"),x.innerHTML=`
        ${n?"":'<div class="lock-icon">🔒</div>'}
        <div class="stage-name">第 ${a} 关</div>
        <div class="stage-desc">${b}</div>
        <div class="stage-num">${a}</div>
      `,n&&x.addEventListener("click",()=>{m(g,a)}),p.appendChild(x)}}function m(p,c){h.navigate("/battle/prepare",{civ:p,stageNo:c})}}function D(o){var m;const t=localStorage.getItem("userId");if(!t){h.navigate("/login");return}o.innerHTML=`
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
  `;const r=`
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
  `,g=document.createElement("style");g.id="equipment-page-style",(m=document.getElementById("equipment-page-style"))==null||m.remove(),g.textContent=r,document.head.appendChild(g),v();async function v(){try{const[d,p]=await Promise.all([$.getEquipments(t),B.getInfo(t)]);p.code===200&&p.data&&(document.getElementById("gold-display").textContent=(p.data.gold||0).toLocaleString());const c=document.getElementById("equip-list");d.code===200&&d.data&&d.data.length>0?(c.innerHTML=d.data.map(i=>{const l=i.enhanceLevel||0,a=(l+1)*100,s=i.name||`装备#${i.templateId}`,n=F(i.slot),x=_(i.slot);return`
           <div class="equip-card">
             <div class="ec-top">
               <div class="ec-icon">${n}</div>
               <div class="ec-info">
                 <div class="ec-name">${s} <span class="ec-lv">+${l}</span></div>
                 <div class="ec-stat">${x} · ${i.generalId?"已穿戴":"闲置"}</div>
               </div>
             </div>
             <div class="ec-stat">基础属性: ATK ${i.baseAtk||0} / HP ${i.baseHp||0} / SPD ${i.baseSpd||0}</div>
             <div class="ec-stat">消耗: ${a}金币</div>
             <button class="btn btn-primary enhance-btn" data-id="${i.id}" data-cost="${a}">强化</button>
           </div>
           `}).join(""),c.querySelectorAll(".enhance-btn").forEach(i=>{i.addEventListener("click",()=>f(i.dataset.id,i.dataset.cost))})):c.innerHTML='<p style="text-align:center;grid-column:1/-1;">暂无装备</p>'}catch(d){console.error(d)}}async function f(d,p){if(confirm(`确认消耗 ${p} 金币强化装备吗？
(+3以上可能会失败掉级)`))try{const c=await $.enhanceEquipment(t,d);if(c.code===200){const i=c.data,l=i.includes("失败");k(i,l?"error":"success"),v()}else k(c.message||"强化失败","error")}catch(c){k(c.message||"操作失败","error")}}function k(d,p="success"){const c=document.getElementById("toast");c.textContent=d,c.className=`toast ${p} show`,setTimeout(()=>{c.className="toast"},2e3)}document.getElementById("back-btn").addEventListener("click",()=>h.navigate("/hall"))}function U(o){var i;const t=localStorage.getItem("userId");if(!t){h.navigate("/login");return}let r=[];o.innerHTML=`
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
  `;const g=document.createElement("style");g.id="gem-page-style",(i=document.getElementById("gem-page-style"))==null||i.remove(),g.textContent=`
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
  `,document.head.appendChild(g),v();async function v(){try{const[l,a,s]=await Promise.all([$.getGems(t),$.getEquipments(t),B.getInfo(t)]);s.code===200&&s.data&&(document.getElementById("gold-display").textContent=(s.data.gold||0).toLocaleString()),l.code===200&&(r=l.data||[]);const n=document.getElementById("gem-list"),x={};r.forEach(e=>{if(e.isUsed)return;const u=`${e.gemType}-${e.gemLevel}`;x[u]||(x[u]={type:e.gemType,level:e.gemLevel,count:0,ids:[]}),x[u].count++,x[u].ids.push(e.id)}),Object.keys(x).length===0?n.innerHTML='<p style="text-align:center;color:#888">暂无闲置宝石</p>':(n.innerHTML=Object.values(x).map(e=>`
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
        `).join(""),n.querySelectorAll(".combine-btn").forEach(e=>{e.addEventListener("click",()=>p(e.dataset.type,e.dataset.level))}));const b=document.getElementById("equip-list");a.code===200&&a.data&&(b.innerHTML=a.data.map(e=>`
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
        `).join(""),b.querySelectorAll(".socket-span").forEach(e=>{e.addEventListener("click",u=>{u.stopPropagation(),e.textContent.includes("已镶嵌")?c("由于时间限制，暂不支持拆卸宝石","error"):m(e.dataset.eid,e.dataset.idx)})}))}catch(l){console.error(l)}}let f=null,k=null;function m(l,a){f=l,k=a;const s=r.filter(x=>!x.isUsed),n=document.getElementById("select-gem-list");s.length===0?n.innerHTML='<p style="text-align:center;color:#888">没有闲置宝石</p>':(n.innerHTML=s.map(x=>`
        <div class="select-item" data-gid="${x.id}">
          <div class="gem-icon" style="width:24px;height:24px;font-size:0.8rem;">💎</div>
          <div>${x.gemType} Lv.${x.gemLevel}</div>
          <div style="margin-left:auto;font-size:0.8rem;color:#aaa;">+${x.statValue}</div>
        </div>
      `).join(""),n.querySelectorAll(".select-item").forEach(x=>{x.addEventListener("click",()=>{d(x.dataset.gid)})})),document.getElementById("gem-select-modal").style.display="flex"}async function d(l){try{const a=await $.inlayGem(t,f,parseInt(k),l);a.code===200?(c("镶嵌成功！"),document.getElementById("gem-select-modal").style.display="none",v()):c(a.message||"镶嵌失败","error")}catch(a){c(a.message,"error")}}document.getElementById("close-modal").addEventListener("click",()=>{document.getElementById("gem-select-modal").style.display="none"});async function p(l,a){try{const s=await $.combineGem(t,l,parseInt(a));s.code===200?(c("合成成功！"),v()):c(s.message,"error")}catch(s){c(s.message,"error")}}function c(l,a="success"){const s=document.getElementById("toast");s.textContent=l,s.className=`toast ${a} show`,setTimeout(()=>{s.className="toast"},2e3)}document.getElementById("back-btn").addEventListener("click",()=>h.navigate("/hall"))}function Y(o){const t=localStorage.getItem("userId");if(!t){h.navigate("/login");return}o.innerHTML=`
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
  `;const r=`
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
  `,g=document.createElement("style");g.textContent=r,document.head.appendChild(g);let v=null,f=[],k=[];m();async function m(){try{const[l,a,s]=await Promise.all([$.getGenerals(t),$.getItems(t),B.getInfo(t)]);s.code===200&&s.data&&(document.getElementById("gold-display").textContent=(s.data.gold||0).toLocaleString()),f=l.code===200?l.data:[],k=a.code===200?a.data:[],d(),p()}catch(l){console.error(l)}}function d(){const l=document.getElementById("general-list");f.length===0?l.innerHTML="<p>暂无武将</p>":(l.innerHTML=f.map(a=>`
        <div class="g-card ${v==a.id?"selected":""}" data-id="${a.id}">
          <div class="g-av">🎖️</div>
          <div style="font-size:0.9rem;font-weight:bold">武将#${a.templateId}</div>
          <div style="font-size:0.8rem;color:#ccc">Lv.${a.level}</div>
        </div>
      `).join(""),l.querySelectorAll(".g-card").forEach(a=>{a.addEventListener("click",()=>{v=a.dataset.id,d(),p()})}))}function p(){const l=document.getElementById("book-list");if(!v){l.innerHTML='<p style="text-align:center;color:#888;width:100%">请先选择武将</p>';return}const a=k.filter(s=>s.count>0&&s.itemId>=300);a.length===0?l.innerHTML='<p style="text-align:center;color:#888;width:100%">背包中没有技能书</p>':(l.innerHTML=a.map(s=>{var n;return`
        <div class="book-card" data-iid="${s.itemId}">
          <div class="book-icon">📚</div>
          <div>
            <div style="font-weight:bold">${((n=j[s.itemId])==null?void 0:n.name)||"技能书 #"+s.itemId}</div>
            <div style="font-size:0.8rem;color:#888">拥有: ${s.count}</div>
          </div>
          <button class="btn btn-primary btn-sm learn-btn" data-iid="${s.itemId}">学习</button>
        </div>
      `}).join(""),l.querySelectorAll(".learn-btn").forEach(s=>{s.addEventListener("click",()=>c(s.dataset.iid))}))}async function c(l){if(confirm("确认消耗一本技能书让该武将学习技能吗？(旧技能将被覆盖)"))try{const a=await $.learnSkill(t,v,parseInt(l));a.code===200?(i("学习成功！","success"),m()):i(a.message||"学习失败","error")}catch(a){i(a.message,"error")}}function i(l,a="success"){const s=document.getElementById("toast");s.textContent=l,s.className=`toast ${a} show`,setTimeout(()=>{s.className="toast"},2e3)}document.getElementById("back-btn").addEventListener("click",()=>h.navigate("/hall"))}function X(o,t){var b;const r=localStorage.getItem("userId");if(!r){h.navigate("/login");return}t.civ;let g=null,v=!1;o.innerHTML=`
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
  `,document.head.appendChild(f);let k="";o.querySelectorAll('input[name="tactics"]').forEach(e=>{e.addEventListener("change",u=>k=u.target.value)}),m();async function m(){try{const e=await z.getBattleState(r);e.code===200?(g=e.data,d(g),l()):h.navigate("/hall")}catch{h.navigate("/hall")}}function d(e){e&&(p("a",e.sideA.hero),c("a",e.sideA.troops),p("b",e.sideB.hero),c("b",e.sideB.troops),document.getElementById("turn-display").textContent=`Turn ${e.turnNo}`)}function p(e,u){const y=document.getElementById(`hero-${e}`),I=y.querySelector(".hp-fill"),E=y.querySelector(".hp-text"),C=Math.max(0,Math.min(100,u.hp/u.maxHp*100));I.style.width=C+"%",E.textContent=`${u.hp}/${u.maxHp}`,y.querySelector(".hero-name").textContent=u.name,u.hp<=0&&(y.style.opacity="0.5")}function c(e,u){const y=document.getElementById(`troops-${e}`);y.innerHTML=u.map(I=>`
        <div class="troop-unit ${I.count<=0?"troop-dead":""}">
           <span>${i(I.type)} ${I.name||I.type}</span>
           <span>x${I.count}</span>
        </div>
      `).join("")}function i(e){return e==="INF"?"🛡️":e==="ARC"?"🏹":e==="CAV"?"🐎":"⚔️"}function l(){if(!g)return;const e=document.getElementById("phase-display"),u=g.phase||"HERO_SOLO";if(e){const T=u==="TROOP_WAR"?"🔥 全军出击":"⚔️ 武将单挑";e.textContent=T,e.className=u==="TROOP_WAR"?"phase-badge war":"phase-badge solo"}if(g.finished){x(g.win);return}const y=document.getElementById("action-status"),I=document.getElementById("action-buttons"),E=document.getElementById("btn-attack"),C=document.getElementById("btn-skill");y.style.display="none",I.style.display="flex";const L=g.sideA.hero;E.textContent=`⚔️ ${g.currentTurn+1}回合: 开始`,u==="TROOP_WAR"?(C.disabled=!0,C.textContent="阶段2不可用"):(L.skillCd||0)>0?(C.disabled=!0,C.textContent=`技能冷却 (${L.skillCd})`):(C.disabled=!1,C.textContent="✨ 释放技能"),E.onclick=()=>n(!1),C.onclick=()=>n(!0)}function a(e){if(!e)return;const u=document.getElementById("logs-content");e.forEach(y=>{const I=document.createElement("div");I.className="log-entry";let E="";switch(y.type){case"TURN_START":E=`<span class="log-highlight">=== 第 ${y.turn} 回合 ===</span>`;break;case"PHASE_CHANGE":E=`<span class="log-highlight" style="font-size:1.1em">⚠️ 阶段切换: ${y.fromPhase||"HERO_SOLO"} -> ${y.toPhase||"TROOP_WAR"}</span>`;break;case"HERO_ATTACK":E=`[${y.actorSide==="my"?"我方":"敌方"}] 主将普攻`;break;case"HERO_SKILL":E=`[${y.actorSide==="my"?"我方":"敌方"}] 释放技能! (Dmg: ${y.value})`;break;case"TROOP_ATTACK":const C=y.rollToHero||0;E=`[${y.actorSide==="my"?"我方":"敌方"}] ${s(y.attackerTroopType)} 进攻! <span style="color:#ffd700">🎲Roll: ${C}</span> (${C}% 打主将, ${100-C}% 打兵)`;break;case"HERO_HP_CHANGE":const L=y.value;E=`> [${y.side==="my"?"我方":"敌方"}] 主将 `+(L<0?`<span class="log-dmg">HP ${L}</span>`:`<span class="log-heal">HP +${L}</span>`);break;case"TROOP_STACK_CHANGE":E=`> [${y.side==="my"?"我方":"敌方"}] ${s(y.troopType)}: <span class="log-dmg">损失 ${y.killed} 单位</span> (Remaining: ${y.countAfter})`;break;default:y.desc?E=y.desc:E=JSON.stringify(y)}I.innerHTML=E,u.prepend(I)})}function s(e){return O(e)}async function n(e){if(!v){v=!0;try{const u=await z.processTurn(r,e,g.turnNo+1,k);if(u.code===200){const y=u.data;y.lastEvents&&a(y.lastEvents),g=y,d(y),v=!1,l()}else console.error(u.message),v=!1}catch(u){console.error(u),v=!1}}}function x(e){const u=document.getElementById("result-modal"),y=document.getElementById("result-title"),I=document.getElementById("result-desc");u.style.display="flex",y.textContent=e?"VICTORY!":"DEFEAT",y.style.color=e?"var(--success-color)":"var(--danger-color)",I.textContent=e?"战斗胜利！获得金币与战利品。":"战斗失败，请强化武将后再试。",document.getElementById("result-ok").onclick=()=>{h.navigate("/hall")}}document.getElementById("flee-btn").addEventListener("click",()=>{confirm("确定要撤退吗？(视为战败)")&&h.navigate("/hall")})}function Q(o,t){var x;const r=localStorage.getItem("userId");if(!r){h.navigate("/login");return}const{civ:g,stageNo:v}=t;if(!g||!v){h.navigate("/hall");return}let f=[],k=null,m=[],d={};o.innerHTML=`
      <div class="prepare-page">
        <div class="page-header">
           <button class="btn btn-sm btn-secondary" id="back-btn">← 放弃出征</button>
           <h2>⚔️ 战前整备 - ${g} 第${v}关</h2>
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
    `;const p=document.createElement("style");p.id="prepare-page-style",(x=document.getElementById("prepare-page-style"))==null||x.remove(),p.textContent=`
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
    `,document.head.appendChild(p),c();async function c(){try{const[b,e]=await Promise.all([$.getGenerals(r),B.getInfo(r)]);if(b.code===200&&(f=b.data.filter(u=>u.activated),i()),e.code===200&&e.data.troops){const y={CN:2e3,JP:2100,KR:2200,GB:2300}[g]||2e3,I={CN:3001,JP:3002,KR:3003,GB:3004},E=new Set([y+1,y+2,y+3,I[g]]);m=(e.data.troops||[]).filter(C=>E.has(C.troopId)&&(C.count||0)>0).sort((C,L)=>(C.troopId||0)-(L.troopId||0))}}catch(b){console.error(b)}}function i(){const b=document.getElementById("generals-list");if(f.length===0){b.innerHTML='<div class="empty-tip">没有激活的武将，请先去大厅激活</div>';return}b.innerHTML=f.map(e=>`
           <div class="general-card ${k===e.id?"selected":""}" data-id="${e.id}">
              <div class="g-avatar">🤴</div>
              <div class="g-info">
                 <div class="g-name">${e.name} <span class="g-lv">Lv.${e.level}</span></div>
                 <div class="g-stats">
                    <span style="color:var(--accent-color)">CAP: ${e.capacity||0}</span>
                 </div>
              </div>
           </div>
        `).join(""),b.querySelectorAll(".general-card").forEach(e=>{e.addEventListener("click",()=>l(parseInt(e.dataset.id)))}),!k&&f.length>0&&l(f[0].id)}function l(b){k=b,document.querySelectorAll(".general-card").forEach(e=>{e.classList.toggle("selected",parseInt(e.dataset.id)===b)}),a(),n()}function a(){if(!k)return;const b=document.getElementById("troops-list");if(m.length===0){b.innerHTML='<div class="empty-tip">没有兵力，请先去招募</div>';return}b.innerHTML=m.map(e=>{const u=S(e.troopId),y=d[e.troopId]||0,I=e.count||0;return`
            <div class="troop-row">
               <div class="t-name">${u.icon} ${u.name}${u.isElite?" [特种]":""} (余:${I})</div>
               <div class="t-control">
                  <button class="btn-tiny btn-minus" data-id="${e.troopId}">-10</button>
                  <input type="number" class="troop-input" id="input-${e.troopId}" value="${y}" max="${I}" min="0">
                  <button class="btn-tiny btn-plus" data-id="${e.troopId}">+10</button>
               </div>
            </div>
            `}).join(""),m.forEach(e=>{const u=document.getElementById("input-"+e.troopId);u&&(u.onchange=y=>{let I=parseInt(y.target.value)||0;s(e.troopId,I)})}),b.querySelectorAll(".btn-minus").forEach(e=>{e.onclick=()=>{const u=parseInt(e.dataset.id);s(u,(d[u]||0)-10)}}),b.querySelectorAll(".btn-plus").forEach(e=>{e.onclick=()=>{const u=parseInt(e.dataset.id);s(u,(d[u]||0)+10)}}),n()}function s(b,e){const u=m.find(E=>E.troopId===b);if(!u)return;const y=u.count||0;e=Math.max(0,Math.min(e,y)),d[b]=e;const I=document.getElementById("input-"+b);I&&(I.value=e),n()}function n(){const b=f.find(L=>L.id===k);if(!b)return;const e=Object.values(d).reduce((L,T)=>L+T,0),u=Object.entries(d).reduce((L,[T,A])=>{const G=S(parseInt(T,10));return L+(A||0)*(G.capCost||1)},0),y=b.capacity||0,I=document.getElementById("capacity-display");I.textContent=`(统率占用: ${u}/${y}，总兵数: ${e})`;const E=document.getElementById("start-btn"),C=e>0&&u<=y;E.disabled=!C,u>y?(I.style.color="var(--danger-color)",E.textContent="兵力超过统率上限"):e===0?(I.style.color="#aaa",E.textContent="请分配兵力"):(I.style.color="var(--success-color)",E.textContent="出征！")}document.getElementById("start-btn").onclick=async()=>{const b=document.getElementById("start-btn");b.disabled=!0,b.textContent="出征中...";try{const e=await z.startStoryBattle(r,g,v,k,d);e.code===200?h.navigate("/battle"):(alert(e.message||"出征失败"),b.disabled=!1,b.textContent="出征！")}catch(e){console.error(e),alert("请求失败"),b.disabled=!1,b.textContent="出征！"}},document.getElementById("back-btn").onclick=()=>h.navigate("/stages")}h.register("/login",M);h.register("/hall",K);h.register("/recruit",W);h.register("/generals",V);h.register("/stages",J);h.register("/equipment",D);h.register("/gem",U);h.register("/skill",Y);h.register("/battle",X);h.register("/battle/prepare",Q);function Z(){localStorage.getItem("userId")?h.navigate("/hall"):h.navigate("/login")}Z();
//# sourceMappingURL=index-DxJPccCz.js.map
