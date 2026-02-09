import { authAPI } from '../api/index.js';
import { router } from '../utils/router.js';

export function LoginPage(container) {
  container.innerHTML = `
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
  `;

  // 添加样式
  const style = document.createElement('style');
  style.id = 'login-page-style';
  document.getElementById('login-page-style')?.remove();
  style.textContent = `
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
  `;
  document.head.appendChild(style);

  // === 状态 ===
  let isRegisterMode = false;
  let selectedCiv = 'CN';

  // === DOM 引用 ===
  const usernameInput = document.getElementById('username');
  const passwordInput = document.getElementById('password');
  const nicknameInput = document.getElementById('nickname');
  const registerFields = document.getElementById('register-fields');
  const loginBtn = document.getElementById('login-btn');
  const registerBtn = document.getElementById('register-btn');
  const messageEl = document.getElementById('message');
  const civOptions = document.querySelectorAll('.civ-option');

  // === 阵营选择 ===
  civOptions.forEach(opt => {
    opt.addEventListener('click', () => {
      civOptions.forEach(o => o.classList.remove('selected'));
      opt.classList.add('selected');
      selectedCiv = opt.dataset.civ;
    });
  });

  // === 显示消息 ===
  function showMessage(text, type = 'error') {
    messageEl.className = `message ${type}`;
    messageEl.textContent = text;
  }

  // === 登录成功处理 ===
  function handleLoginSuccess(user) {
    localStorage.setItem('userId', user.id);
    localStorage.setItem('username', user.nickname);
    showMessage('登录成功！正在进入游戏...', 'success');
    setTimeout(() => router.navigate('/hall'), 600);
  }

  // === 切换注册/登录 ===
  registerBtn.addEventListener('click', () => {
    isRegisterMode = !isRegisterMode;
    registerFields.style.display = isRegisterMode ? 'flex' : 'none';
    loginBtn.textContent = isRegisterMode ? '确认注册' : '登录';
    registerBtn.textContent = isRegisterMode ? '返回登录' : '注册';
    messageEl.textContent = '';
  });

  // === 主操作（登录 / 注册） ===
  loginBtn.addEventListener('click', async () => {
    const username = usernameInput.value.trim();
    const password = passwordInput.value.trim();

    if (!username || !password) {
      showMessage('请输入账号和密码');
      return;
    }

    loginBtn.disabled = true;
    registerBtn.disabled = true;

    try {
      if (isRegisterMode) {
        // --- 注册 ---
        const nickname = nicknameInput.value.trim();
        if (!nickname) {
          showMessage('请输入昵称');
          return;
        }

        loginBtn.textContent = '注册中...';
        const result = await authAPI.register(username, password, nickname, selectedCiv);
        console.log('注册结果:', result);

        // 后端现在返回 Result<UserTbl>: {code: 200, data: {id, nickname, ...}}
        if (result.code === 200 && result.data) {
          handleLoginSuccess(result.data);
        } else {
          showMessage(result.message || '注册失败');
        }
      } else {
        // --- 登录 ---
        loginBtn.textContent = '登录中...';
        const result = await authAPI.login(username, password);
        console.log('登录结果:', result);

        // 后端现在返回 Result<UserTbl>: {code: 200, data: {id, nickname, ...}}
        if (result.code === 200 && result.data) {
          handleLoginSuccess(result.data);
        } else {
          showMessage(result.message || '登录失败');
        }
      }
    } catch (error) {
      showMessage(error.message || '操作失败，请重试');
    } finally {
      loginBtn.disabled = false;
      registerBtn.disabled = false;
      loginBtn.textContent = isRegisterMode ? '确认注册' : '登录';
    }
  });

  // === 回车提交 ===
  container.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') loginBtn.click();
  });
}
