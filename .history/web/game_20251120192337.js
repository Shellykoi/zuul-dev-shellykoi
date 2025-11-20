/**
 * World of Zuul 游戏前端逻辑
 * 处理用户交互、API调用和界面更新
 */

// API基础URL（可变）。会按候选端口自动探测后端，以应对端口自动切换
// 注意：这里不包含/api，因为buildApiUrl会自动添加
let API_BASE_URL = window.location.origin;
let apiBaseUrlResolved = false;
let apiBaseUrlPromise = null;

async function resolveApiBaseUrl() {
    if (apiBaseUrlResolved && apiBaseUrlPromise) return apiBaseUrlPromise;
    if (apiBaseUrlPromise) return apiBaseUrlPromise;

    const origin = window.location.origin;
    const defaultHost = window.location.hostname || 'localhost';
    const currentPort = window.location.port ? parseInt(window.location.port) : (window.location.protocol === 'https:' ? 443 : 80);
    
    // 构建端口列表：优先当前端口，然后按顺序尝试其他常见端口
    const portsToTry = [];
    
    // 1. 首先尝试当前访问的端口
    if (currentPort && currentPort !== 443 && currentPort !== 80) {
        portsToTry.push(`http://${defaultHost}:${currentPort}`);
    }
    
    // 2. 然后尝试常见端口（8080-8084）
    [8080, 8081, 8082, 8083, 8084].forEach(p => {
        const candidate = `http://${defaultHost}:${p}`;
        if (!portsToTry.includes(candidate)) {
            portsToTry.push(candidate);
        }
    });
    
    // 3. 如果当前origin不在列表中，也添加它
    if (!portsToTry.includes(origin) && origin.startsWith('http://')) {
        portsToTry.unshift(origin); // 放在最前面优先尝试
    }

    apiBaseUrlPromise = (async () => {
        console.log('开始探测API服务器端口，候选端口:', portsToTry);
        
        for (const base of portsToTry) {
            try {
                const controller = new AbortController();
                const timeout = setTimeout(() => controller.abort(), 2000); // 增加超时时间到2秒
                
                const statusUrl = `${base}/api/status`;
                console.log(`🔍 尝试连接: ${statusUrl}`);
                const resp = await fetch(statusUrl, { 
                    method: 'GET', 
                    signal: controller.signal,
                    headers: {
                        'Accept': 'application/json'
                    }
                });
                clearTimeout(timeout);
                
                // 验证响应：必须是200状态码，且Content-Type是JSON
                if (resp.ok) {
                    const contentType = resp.headers.get('content-type');
                    if (contentType && contentType.includes('application/json')) {
                        // 进一步验证：尝试解析JSON，确保是我们的API
                        try {
                            const data = await resp.json();
                            // 检查响应是否包含我们API的特征字段（如success, message等）
                            if (data && (data.hasOwnProperty('success') || data.hasOwnProperty('message') || data.hasOwnProperty('currentRoom'))) {
                                // 返回base URL（不包含/api），因为buildApiUrl会自动添加
                                API_BASE_URL = base;
                                apiBaseUrlResolved = true;
                                console.log('✅ API 基础地址已确定为:', API_BASE_URL);
                                return API_BASE_URL;
                            }
                        } catch (e) {
                            console.warn(`端口 ${base} 返回了JSON，但格式不正确:`, e);
                            // 继续尝试下一个端口
                        }
                    } else {
                        console.warn(`端口 ${base} 返回了非JSON响应，Content-Type:`, contentType);
                    }
                } else {
                    console.warn(`端口 ${base} 返回了非200状态码:`, resp.status);
                }
            } catch (e) {
                // 网络错误或超时，继续尝试下一个端口
                if (e.name !== 'AbortError') {
                    console.warn(`端口 ${base} 连接失败:`, e.message);
                }
            }
        }
        
        console.error('❌ 未能探测到可用的后端服务器！');
        console.warn('使用默认地址:', API_BASE_URL);
        console.warn('请确保服务器正在运行，并且可以访问以下端口之一:', portsToTry.map(b => b.replace('http://', '')).join(', '));
        return API_BASE_URL;
    })();

    return apiBaseUrlPromise;
}

/**
 * 构建完整的API URL
 * @param {string} endpoint - API端点（如 'login', 'register', 'command'）
 * @returns {Promise<string>} 完整的API URL
 */
async function buildApiUrl(endpoint) {
    const base = await resolveApiBaseUrl();
    console.log('🔗 构建API URL，base:', base, 'endpoint:', endpoint);
    
    if (!base) {
        throw new Error('无法确定API服务器地址');
    }
    
    // 规范化base URL：去除末尾斜杠，并确保不包含/api
    let normalizedBase = base.trim();
    if (normalizedBase.endsWith('/')) {
        normalizedBase = normalizedBase.substring(0, normalizedBase.length - 1);
    }
    // 如果base已经包含/api，移除它（因为resolveApiBaseUrl应该返回不包含/api的base）
    if (normalizedBase.endsWith('/api')) {
        normalizedBase = normalizedBase.substring(0, normalizedBase.length - 4);
    }
    
    // 确保endpoint不以斜杠开头
    const cleanEndpoint = endpoint.startsWith('/') ? endpoint.substring(1) : endpoint;
    
    // 构建最终URL：base + /api/ + endpoint
    const finalUrl = `${normalizedBase}/api/${cleanEndpoint}`;
    
    console.log('✅ 构建的API URL:', finalUrl);
    console.log('   规范化base:', normalizedBase);
    console.log('   清理后的endpoint:', cleanEndpoint);
    console.log('   完整URL结构:', `${normalizedBase} + /api/ + ${cleanEndpoint}`);
    
    return finalUrl;
}

// 游戏状态
let gameState = {
    currentRoom: null,
    player: null,
    isLoading: false,
    sessionId: null,
    username: null,
    isLoggedIn: false,
    completion: null,
    lastProgress: null
};

/**
 * 初始化游戏
 */
function initGame() {
    console.log('初始化游戏...');
    
    // 绑定事件监听器
    setupEventListeners();
    
    // 禁用游戏界面直到登录
    setGameEnabled(false);
    
    // 显示登录界面
    showLoginModal();
}

/**
 * 设置事件监听器
 */
function setupEventListeners() {
    console.log('设置事件监听器...');
    
    try {
        // 登录/注册相关
        const loginTab = document.getElementById('login-tab');
        const registerTab = document.getElementById('register-tab');
        const loginBtn = document.getElementById('login-btn');
        const registerBtn = document.getElementById('register-btn');
        const guestBtn = document.getElementById('guest-btn');
        
        if (!loginTab || !registerTab || !loginBtn || !registerBtn || !guestBtn) {
            console.error('无法找到必要的DOM元素:', {
                loginTab: !!loginTab,
                registerTab: !!registerTab,
                loginBtn: !!loginBtn,
                registerBtn: !!registerBtn,
                guestBtn: !!guestBtn
            });
            return;
        }
        
        loginTab.addEventListener('click', () => switchTab('login'));
        registerTab.addEventListener('click', () => switchTab('register'));
        loginBtn.addEventListener('click', (e) => {
            console.log('登录按钮被点击', e);
            e.preventDefault();
            e.stopPropagation();
            handleLogin();
        });
        registerBtn.addEventListener('click', (e) => {
            console.log('注册按钮被点击', e);
            e.preventDefault();
            e.stopPropagation();
            handleRegister();
        });
        
        // 测试按钮绑定
        console.log('注册按钮元素:', registerBtn);
        console.log('注册按钮是否可点击:', registerBtn.style.pointerEvents !== 'none');
        
        guestBtn.addEventListener('click', handleGuestLogin);
        
        console.log('事件监听器设置完成');
        
        // 延迟测试，确保DOM完全加载
        setTimeout(() => {
            const testBtn = document.getElementById('register-btn');
            if (testBtn) {
                console.log('延迟测试：注册按钮存在，可以绑定事件');
                // 添加一个测试点击事件
                testBtn.addEventListener('click', () => {
                    console.log('测试：注册按钮点击事件触发');
                }, { once: true });
            } else {
                console.error('延迟测试：注册按钮不存在！');
            }
        }, 100);
        
        // 登录表单回车提交
        const loginUsername = document.getElementById('login-username');
        const loginPassword = document.getElementById('login-password');
        if (loginUsername) {
            loginUsername.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') handleLogin();
            });
        }
        if (loginPassword) {
            loginPassword.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') handleLogin();
            });
        }
        
        // 注册表单回车提交
        const registerUsername = document.getElementById('register-username');
        const registerPassword = document.getElementById('register-password');
        const registerPasswordConfirm = document.getElementById('register-password-confirm');
        if (registerUsername) {
            registerUsername.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') handleRegister();
            });
        }
        if (registerPassword) {
            registerPassword.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') handleRegister();
            });
        }
        if (registerPasswordConfirm) {
            registerPasswordConfirm.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') handleRegister();
            });
        }
        
        // 命令输入框
        const commandInput = document.getElementById('command-input');
        const submitBtn = document.getElementById('submit-btn');
        
        // 回车提交命令
        if (commandInput) {
            commandInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter' && !gameState.isLoading && gameState.isLoggedIn) {
                    executeCommand();
                }
            });
        }
        
        // 提交按钮
        if (submitBtn) {
            submitBtn.addEventListener('click', () => {
                if (!gameState.isLoading && gameState.isLoggedIn) {
                    executeCommand();
                }
            });
        }
        
        // 键盘快捷键（WASD方向键）
        document.addEventListener('keydown', (e) => {
            // 如果焦点在输入框，不处理方向键
            if (document.activeElement === commandInput) {
                return;
            }
            
            if (!gameState.isLoggedIn || gameState.isLoading) return;
            
            const keyMap = {
                'w': 'go north',
                'W': 'go north',
                's': 'go south',
                'S': 'go south',
                'a': 'go west',
                'A': 'go west',
                'd': 'go east',
                'D': 'go east'
            };
            
            const command = keyMap[e.key];
            if (command) {
                e.preventDefault();
                executeCommand(command);
            }
        });
        
        // 退出按钮
        const quitBtn = document.getElementById('quit-btn');
        if (quitBtn) {
            quitBtn.addEventListener('click', () => {
                if (confirm('确定要退出游戏吗？')) {
                    executeCommand('quit');
                }
            });
        }
        
        // 清空日志按钮
        const clearBtn = document.getElementById('clear-btn');
        if (clearBtn) {
            clearBtn.addEventListener('click', () => {
                clearOutput();
            });
        }
        
        // 帮助按钮
        const helpBtn = document.getElementById('help-btn-header');
        const helpModalClose = document.getElementById('help-modal-close');
        const helpModal = document.getElementById('help-modal');
        
        if (helpBtn) {
            helpBtn.addEventListener('click', () => {
                showHelpModal();
            });
        }
        
        if (helpModalClose) {
            helpModalClose.addEventListener('click', () => {
                closeHelpModal();
            });
        }
        
        if (helpModal) {
            helpModal.addEventListener('click', (e) => {
                if (e.target.id === 'help-modal') {
                    closeHelpModal();
                }
            });
        }
        
        // 方向罗盘按钮（在初始化时绑定一次）
        const compassButtons = {
            'north': document.getElementById('compass-north'),
            'south': document.getElementById('compass-south'),
            'east': document.getElementById('compass-east'),
            'west': document.getElementById('compass-west')
        };
        
        Object.keys(compassButtons).forEach(dir => {
            const btn = compassButtons[dir];
            if (btn && btn.dataset.direction !== 'center') {
                btn.addEventListener('click', () => {
                    if (gameState.isLoggedIn && !btn.disabled) {
                        executeCommand(`go ${dir}`);
                    }
                });
            }
        });
        
        // 模态框关闭
        const modalClose = document.getElementById('modal-close');
        if (modalClose) {
            modalClose.addEventListener('click', () => closeModal());
        } else {
            console.warn('未找到 modal-close 元素');
        }

        // 点击模态框外部关闭
        const itemModal = document.getElementById('item-modal');
        if (itemModal) {
            itemModal.addEventListener('click', (e) => {
                if (e.target.id === 'item-modal') {
                    closeModal();
                }
            });
        } else {
            console.warn('未找到 item-modal 元素');
        }
    
    } catch (error) {
        console.error('设置事件监听器时出错:', error);
        console.error('错误堆栈:', error.stack);
    }
}

/**
 * 切换登录/注册标签页
 */
function switchTab(tab) {
    console.log('切换标签页:', tab);
    const loginTab = document.getElementById('login-tab');
    const registerTab = document.getElementById('register-tab');
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    
    if (tab === 'login') {
        loginTab.classList.add('active');
        registerTab.classList.remove('active');
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
    } else {
        registerTab.classList.add('active');
        loginTab.classList.remove('active');
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
        
        // 确保注册按钮可见且可点击
        const registerBtn = document.getElementById('register-btn');
        if (registerBtn) {
            console.log('注册表单显示，检查注册按钮:', {
                display: window.getComputedStyle(registerBtn).display,
                visibility: window.getComputedStyle(registerBtn).visibility,
                pointerEvents: window.getComputedStyle(registerBtn).pointerEvents,
                zIndex: window.getComputedStyle(registerBtn).zIndex
            });
        }
    }
    
    // 清空消息
    document.getElementById('login-message').textContent = '';
    document.getElementById('register-message').textContent = '';
}

/**
 * 显示登录页面（完整页面）
 */
function showLoginModal() {
    const loginPage = document.getElementById('login-page');
    const gamePage = document.getElementById('game-page');
    if (loginPage) loginPage.style.display = 'flex';
    if (gamePage) gamePage.style.display = 'none';
}

/**
 * 隐藏登录页面，显示游戏界面
 */
function hideLoginModal() {
    const loginPage = document.getElementById('login-page');
    const gamePage = document.getElementById('game-page');
    if (loginPage) loginPage.style.display = 'none';
    if (gamePage) gamePage.style.display = 'flex';
}

/**
 * 处理登录
 */
async function handleLogin() {
    console.log('=== handleLogin 被调用 ===');
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value.trim();
    const loadSaved = document.getElementById('load-saved-game').checked;
    const messageEl = document.getElementById('login-message');
    
    console.log('登录信息:', { username, passwordLength: password.length, loadSaved });
    
    if (!username || !password) {
        console.log('验证失败: 用户名或密码为空');
        showMessage(messageEl, '请输入用户名和密码', 'error');
        return;
    }
    
    try {
        const loginUrl = await buildApiUrl('login');
        console.log('✅ 验证通过，准备发送登录请求到:', loginUrl);
        
        const requestBody = JSON.stringify({ username, password });
        console.log('请求体:', requestBody);
        
        const response = await fetch(loginUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: requestBody
        });
        
        console.log('收到响应:', { status: response.status, statusText: response.statusText, ok: response.ok });
        console.log('响应头:', {
            contentType: response.headers.get('content-type'),
            contentLength: response.headers.get('content-length')
        });
        
        if (!response.ok) {
            let errorText = '';
            try {
                errorText = await response.text();
                // 尝试解析为JSON以获取更详细的错误信息
                try {
                    const errorData = JSON.parse(errorText);
                    if (errorData.message) {
                        errorText = errorData.message;
                    }
                } catch (e) {
                    // 不是JSON，使用原始文本
                }
            } catch (e) {
                console.error('读取错误响应文本失败:', e);
                errorText = '无法读取错误信息';
            }
            console.error('HTTP错误响应:', { status: response.status, statusText: response.statusText, body: errorText });
            
            // 根据状态码提供更友好的错误提示
            if (response.status === 0 || response.status >= 500) {
                throw new Error(`服务器错误 (${response.status})。请检查服务器是否正在运行，当前使用的API地址: ${base}`);
            } else if (response.status === 404) {
                throw new Error(`API端点未找到。请检查服务器配置，当前使用的API地址: ${base}`);
            } else {
                throw new Error(errorText || `登录失败 (${response.status})`);
            }
        }
        
        // 读取并解析响应
        console.log('准备读取响应...');
        let data;
        try {
            // 先尝试直接解析JSON
            console.log('开始调用 response.json()...');
            data = await response.json();
            console.log('✅ 成功解析JSON响应');
            console.log('解析后的数据:', data);
            console.log('数据字段:', { 
                success: data.success, 
                sessionId: data.sessionId, 
                username: data.username,
                message: data.message 
            });
        } catch (jsonError) {
            console.error('❌ JSON解析失败，尝试读取文本:', jsonError);
            // 如果JSON解析失败，尝试读取文本
            try {
                const responseText = await response.text();
                console.log('响应文本:', responseText);
                console.log('响应文本长度:', responseText.length);
                
                if (!responseText || responseText.trim() === '') {
                    console.error('服务器返回空响应');
                    throw new Error('服务器返回空响应');
                }
                
                // 尝试手动解析JSON
                try {
                    data = JSON.parse(responseText);
                    console.log('✅ 手动JSON解析成功');
                } catch (parseError) {
                    console.error('❌ 手动JSON解析也失败:', parseError);
                    console.error('响应内容:', responseText);
                    throw new Error('服务器返回了无效的JSON响应: ' + parseError.message);
                }
            } catch (textError) {
                console.error('❌ 读取响应文本也失败:', textError);
                throw new Error('无法读取服务器响应: ' + textError.message);
            }
        }
        
        console.log('检查登录结果，data.success =', data.success);
        if (data.success) {
            console.log('✅ 登录成功，数据:', data);
            console.log('准备更新游戏状态...');
            gameState.sessionId = data.sessionId;
            gameState.username = data.username;
            gameState.isLoggedIn = true;
            
            console.log('更新后的游戏状态:', gameState);
            console.log('准备显示成功消息...');
            showMessage(messageEl, '登录成功！', 'success');
            
            // 如果选择加载保存的游戏
            if (loadSaved) {
                setTimeout(async () => {
                    await loadSavedGame();
                    hideLoginModal();
                    setGameEnabled(true);
                    await loadGameState();
                    addOutputMessage(`欢迎回来，${username}！`, 'game-response');
                }, 500);
            } else {
                // 开始新游戏（游戏状态会在后端自动重置）
                setTimeout(() => {
                    hideLoginModal();
                    setGameEnabled(true);
                    loadGameState();
                    addOutputMessage(`欢迎，${username}！开始新游戏。`, 'game-response');
                    addOutputMessage('输入 \'help\' 查看所有可用命令。', 'game-response');
                }, 500);
            }
        } else {
            showMessage(messageEl, data.message || '登录失败', 'error');
        }
    } catch (error) {
        console.error('❌ 登录时出错:', error);
        console.error('错误详情:', {
            name: error.name,
            message: error.message,
            stack: error.stack
        });
        
        // 根据错误类型提供更详细的提示
        let errorMessage = error.message || '无法连接到服务器';
        
        // 如果是网络错误（如连接被拒绝、超时等）
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            errorMessage = `无法连接到服务器。当前尝试的API地址: ${base}\n` +
                          `请检查：\n` +
                          `1. 服务器是否正在运行\n` +
                          `2. 服务器运行的端口是否正确（常见端口: 8080-8084）\n` +
                          `3. 如果服务器在其他端口运行，请访问 http://localhost:端口号`;
        } else if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            errorMessage = `网络连接失败。请检查服务器是否在运行，当前使用的API地址: ${base}`;
        }
        
        showMessage(messageEl, errorMessage, 'error');
    }
}

/**
 * 处理注册
 */
async function handleRegister() {
    console.log('=== handleRegister 被调用 ===');
    console.trace('调用堆栈');
    
    const usernameEl = document.getElementById('register-username');
    const passwordEl = document.getElementById('register-password');
    const passwordConfirmEl = document.getElementById('register-password-confirm');
    const messageEl = document.getElementById('register-message');
    
    console.log('表单元素检查:', {
        usernameEl: !!usernameEl,
        passwordEl: !!passwordEl,
        passwordConfirmEl: !!passwordConfirmEl,
        messageEl: !!messageEl
    });
    
    if (!usernameEl || !passwordEl || !passwordConfirmEl || !messageEl) {
        console.error('无法找到注册表单元素');
        alert('页面元素加载错误，请刷新页面重试');
        return;
    }
    
    const username = usernameEl.value.trim();
    const password = passwordEl.value.trim();
    const passwordConfirm = passwordConfirmEl.value.trim();
    
    console.log('注册信息:', { 
        username, 
        passwordLength: password.length, 
        passwordConfirmLength: passwordConfirm.length,
        usernameEmpty: !username,
        passwordEmpty: !password,
        passwordsMatch: password === passwordConfirm,
        passwordLengthValid: password.length >= 3
    });
    
    if (!username || !password) {
        console.log('验证失败: 用户名或密码为空', { username: !!username, password: !!password });
        showMessage(messageEl, '请输入用户名和密码', 'error');
        return;
    }
    
    console.log('检查密码一致性...', { password, passwordConfirm, match: password === passwordConfirm });
    if (password !== passwordConfirm) {
        console.log('验证失败: 两次密码不一致', { password, passwordConfirm });
        showMessage(messageEl, '两次输入的密码不一致', 'error');
        return;
    }
    
    console.log('检查密码长度...', { length: password.length, minLength: 3 });
    if (password.length < 3) {
        console.log('验证失败: 密码长度不足', { length: password.length });
        showMessage(messageEl, '密码长度至少为3个字符', 'error');
        return;
    }
    
    try {
        const registerUrl = await buildApiUrl('register');
        console.log('✅ 所有验证通过，准备发送注册请求到:', registerUrl);
        
        const requestBody = JSON.stringify({ username, password });
        console.log('请求体:', requestBody);
        
        const response = await fetch(registerUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: requestBody
        });
        
        console.log('收到响应:', { status: response.status, statusText: response.statusText, ok: response.ok });
        console.log('响应头:', {
            contentType: response.headers.get('content-type'),
            contentLength: response.headers.get('content-length')
        });
        
        if (!response.ok) {
            let errorText = '';
            try {
                errorText = await response.text();
            } catch (e) {
                console.error('读取错误响应文本失败:', e);
                errorText = '无法读取错误信息';
            }
            console.error('HTTP错误响应:', { status: response.status, statusText: response.statusText, body: errorText });
            throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
        }
        
        // 读取响应文本
        let responseText = '';
        try {
            responseText = await response.text();
            console.log('✅ 成功读取响应文本');
            console.log('响应文本:', responseText);
            console.log('响应文本长度:', responseText.length);
            console.log('响应文本是否为空:', !responseText || responseText.trim() === '');
        } catch (e) {
            console.error('❌ 读取响应文本失败:', e);
            throw new Error('无法读取服务器响应: ' + e.message);
        }
        
        if (!responseText || responseText.trim() === '') {
            console.error('服务器返回空响应');
            throw new Error('服务器返回空响应');
        }
        
        let data;
        try {
            data = JSON.parse(responseText);
            console.log('✅ JSON解析成功，解析后的数据:', data);
            console.log('数据字段:', { 
                success: data.success, 
                sessionId: data.sessionId, 
                username: data.username,
                message: data.message 
            });
        } catch (parseError) {
            console.error('❌ JSON解析错误:', parseError);
            console.error('响应内容:', responseText);
            throw new Error('服务器返回了无效的JSON响应');
        }
        
        if (data.success) {
            console.log('✅ 注册成功，数据:', data);
            gameState.sessionId = data.sessionId;
            gameState.username = data.username;
            gameState.isLoggedIn = true;
            
            console.log('更新后的游戏状态:', gameState);
            showMessage(messageEl, '注册成功！正在登录...', 'success');
            
            console.log('准备隐藏登录模态框并启用游戏...');
            setTimeout(() => {
                console.log('执行登录后操作...');
                hideLoginModal();
                setGameEnabled(true);
                loadGameState();
                addOutputMessage(`欢迎，${username}！开始新游戏。`, 'game-response');
                addOutputMessage('输入 \'help\' 查看所有可用命令。', 'game-response');
                console.log('登录后操作完成');
            }, 500);
        } else {
            console.log('❌ 注册失败，服务器返回:', data);
            showMessage(messageEl, data.message || '注册失败', 'error');
        }
    } catch (error) {
        console.error('注册时出错:', error);
        console.error('错误堆栈:', error.stack);
        showMessage(messageEl, error.message || '无法连接到服务器，请确保服务器正在运行', 'error');
    }
}

/**
 * 处理游客登录
 */
async function handleGuestLogin() {
    // 创建匿名会话
    gameState.sessionId = null; // 后端会自动创建匿名会话
    gameState.username = 'Guest';
    gameState.isLoggedIn = true;
    
    hideLoginModal();
    setGameEnabled(true);
    loadGameState();
    addOutputMessage('欢迎，游客！开始新游戏（游戏进度不会被保存）。', 'game-response');
    addOutputMessage('输入 \'help\' 查看所有可用命令。', 'game-response');
}

/**
 * 加载保存的游戏
 */
async function loadSavedGame() {
    if (!gameState.sessionId) {
        return;
    }
    
    try {
        const loadUrl = await buildApiUrl('load');
        const response = await fetch(loadUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ sessionId: gameState.sessionId })
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        if (data.success) {
            addOutputMessage('游戏状态已加载！', 'game-response');
        } else {
            addOutputMessage('没有找到保存的游戏，开始新游戏。', 'game-response');
        }
    } catch (error) {
        console.error('加载游戏时出错:', error);
        addOutputMessage('加载失败，开始新游戏。', 'error');
    }
}

/**
 * 显示消息
 */
function showMessage(element, message, type) {
    element.textContent = message;
    element.className = `form-message ${type}`;
}

/**
 * 设置游戏界面启用/禁用
 */
function setGameEnabled(enabled) {
    gameState.isLoggedIn = enabled;
    const commandInput = document.getElementById('command-input');
    const submitBtn = document.getElementById('submit-btn');
    
    if (commandInput) {
        commandInput.disabled = !enabled;
        if (enabled) {
            commandInput.focus();
        }
    }
    if (submitBtn) {
        submitBtn.disabled = !enabled;
    }
    
    // 更新所有交互元素的禁用状态
    document.querySelectorAll('.exit-chip, .compass-btn, .room-item-action').forEach(btn => {
        if (btn) btn.disabled = !enabled;
    });
}

/**
 * 执行游戏命令
 */
async function executeCommand(command = null) {
    if (gameState.isLoading || !gameState.isLoggedIn) {
        return;
    }
    
    // 获取命令
    if (!command) {
        const commandInput = document.getElementById('command-input');
        command = commandInput.value.trim();
        if (!command) {
            return;
        }
        commandInput.value = '';
    }
    
    // 显示用户输入的命令
    addOutputMessage(`> ${command}`, 'user-command');
    
    // 设置加载状态
    setLoadingState(true);
    
    try {
        // 构建请求体
        const requestBody = { command: command };
        if (gameState.sessionId) {
            requestBody.sessionId = gameState.sessionId;
        }
        
        // 调用API执行命令
        const commandUrl = await buildApiUrl('command');
        const response = await fetch(commandUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody)
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        // 处理响应
        handleCommandResponse(data);
        
        // 更新游戏状态
        await loadGameState();
        
    } catch (error) {
        console.error('执行命令时出错:', error);
        addOutputMessage('错误: 无法连接到游戏服务器。请确保服务器正在运行。', 'error');
    } finally {
        setLoadingState(false);
    }
}

/**
 * 处理命令响应
 */
function handleCommandResponse(data) {
    if (data.success === false) {
        addOutputMessage(data.message || '命令执行失败', 'error');
        return;
    }
    
    // 显示游戏响应消息
    if (data.message) {
        const messageType = data.success ? 'game-response' : 'error';
        const lines = data.message.split('\n');
        lines.forEach(line => {
            const trimmed = line.trim();
            if (!trimmed) return;
            // 后端偶尔会返回“当前房间没有物品！”的提示，但实际 items 列表不为空时跳过这个误报
            if (trimmed === '当前房间没有物品！' && gameState.currentRoom?.items?.length) {
                return;
            }
            addOutputMessage(trimmed, messageType);
        });
    }
    
    // 如果是退出命令
    if (data.quit) {
        setTimeout(() => {
            gameState.isLoggedIn = false;
            setGameEnabled(false);
            showLoginModal();
            clearOutput();
            // 不显示退出消息，因为已经切换到登录页面
        }, 500);
        return;
    }

    // 通关提示和进度信息
    if (data.progress) {
        const progress = data.progress;
        
        // 每次命令后都显示进度信息
        const progressMsg = `进度：房间 ${progress.roomsExplored}/${progress.totalRooms} | ` +
                           `物品 ${progress.itemsCollected}/${progress.totalItems} | ` +
                           `饼干: ${progress.cookieEaten ? '已吃' : '未吃'} | ` +
                           `位置: ${progress.atStartRoom ? '起始房间' : '其他房间'}`;
        addOutputMessage(progressMsg, 'game-response');
        
        // 如果通关，显示醒目的通关提示
        if (data.completed || progress.completed) {
            addOutputMessage('', 'game-response'); // 空行分隔
            addOutputMessage('🎉🎉🎉 恭喜！你已完成所有任务，游戏通关！🎉🎉🎉', 'success');
            addOutputMessage('', 'game-response'); // 空行分隔
            addOutputMessage('通关详情：', 'game-response');
            addOutputMessage(`✓ 已探索所有房间: ${progress.roomsExplored}/${progress.totalRooms}`, 'success');
            addOutputMessage(`✓ 已收集所有物品: ${progress.itemsCollected}/${progress.totalItems}`, 'success');
            addOutputMessage(`✓ 魔法饼干: ${progress.cookieEaten ? '已吃掉 ✓' : '未吃掉 ✗'}`, progress.cookieEaten ? 'success' : 'error');
            addOutputMessage(`✓ 当前位置: ${progress.atStartRoom ? '起始房间 ✓' : '其他房间 ✗'}`, progress.atStartRoom ? 'success' : 'error');
            addOutputMessage('', 'game-response'); // 空行分隔
        }
        
        // 保存当前进度用于比较
        gameState.lastProgress = progress;
    }
}

/**
 * 加载游戏状态
 */
async function loadGameState() {
    if (!gameState.isLoggedIn) {
        return;
    }
    
    try {
        let url = await buildApiUrl('status');
        if (gameState.sessionId) {
            url += `?sessionId=${encodeURIComponent(gameState.sessionId)}`;
        }
        
        const response = await fetch(url);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        if (data.error) {
            console.error('获取游戏状态错误:', data.error);
            return;
        }
        
        // 调试：输出后端返回的原始数据
        console.log('📥 后端返回的游戏状态数据:', data);
        console.log('📥 当前房间数据:', data.currentRoom);
        console.log('📥 房间物品数组:', data.currentRoom?.items);
        console.log('📥 房间物品数组类型:', Array.isArray(data.currentRoom?.items) ? '数组' : typeof data.currentRoom?.items);
        console.log('📥 房间物品数组长度:', data.currentRoom?.items?.length);
        
        // 更新游戏状态
        gameState.currentRoom = data.currentRoom;
        gameState.player = data.player;
        gameState.completion = data.completion;
        
        // 调试：输出更新后的游戏状态
        console.log('📝 更新后的游戏状态 - 当前房间:', gameState.currentRoom);
        console.log('📝 更新后的游戏状态 - 房间物品数组:', gameState.currentRoom?.items);
        console.log('📝 更新后的游戏状态 - 通关信息:', gameState.completion);
        
        // 更新界面
        updateUI();
        
    } catch (error) {
        console.error('加载游戏状态时出错:', error);
        // 不显示错误，因为可能是服务器未启动
    }
}

/**
 * 更新用户界面
 */
function updateUI() {
    // 更新房间信息
    if (gameState.currentRoom) {
        const shortDesc = gameState.currentRoom.shortDescription || '未知房间';
        const longDesc = gameState.currentRoom.longDescription || '正在加载...';
        
        // 更新房间标题和描述
        const roomTitle = document.getElementById('room-title');
        const roomDesc = document.getElementById('room-description');
        const roomNameHeader = document.getElementById('room-name-header');
        
        if (roomTitle) roomTitle.textContent = shortDesc;
        if (roomDesc) roomDesc.textContent = longDesc;
        if (roomNameHeader) roomNameHeader.textContent = shortDesc;
        
        // 更新出口列表
        updateExitsList();
        
        // 更新方向罗盘
        updateCompass();
        
        // 更新房间物品列表
        updateRoomItems();
    }
    
    // 更新玩家信息
    if (gameState.player) {
        const totalWeight = gameState.player.totalWeight || 0;
        const maxWeight = gameState.player.maxWeight || 10;
        const itemCount = gameState.player.inventory?.length || 0;
        
        // 从completion数据获取已访问房间数，如果没有则从player.visitedRooms获取
        const visitedCount = gameState.completion?.roomsExplored || 
                            gameState.player.visitedRooms?.length || 0;
        const totalRooms = gameState.completion?.totalRooms || 6;
        
        // 更新顶部状态栏
        const weightTextHeader = document.getElementById('weight-text-header');
        const weightProgressFill = document.getElementById('weight-progress-fill');
        const weightProgressFillFull = document.getElementById('weight-progress-fill-full');
        const weightTextFull = document.getElementById('weight-text-full');
        const itemCountHeader = document.getElementById('item-count-header');
        const visitedRooms = document.getElementById('visited-rooms');
        
        if (weightTextHeader) {
            weightTextHeader.textContent = `${totalWeight.toFixed(1)}/${maxWeight.toFixed(1)} kg`;
        }
        if (weightProgressFill) {
            const percentage = Math.min((totalWeight / maxWeight) * 100, 100);
            weightProgressFill.style.width = `${percentage}%`;
            weightProgressFill.className = 'weight-progress-fill' + 
                (percentage >= 100 ? ' danger' : percentage >= 80 ? ' warning' : '');
        }
        if (weightProgressFillFull) {
            const percentage = Math.min((totalWeight / maxWeight) * 100, 100);
            weightProgressFillFull.style.width = `${percentage}%`;
            weightProgressFillFull.className = 'weight-progress-fill-full' + 
                (percentage >= 100 ? ' danger' : percentage >= 80 ? ' warning' : '');
        }
        if (weightTextFull) {
            weightTextFull.textContent = `${totalWeight.toFixed(1)} / ${maxWeight.toFixed(1)} kg`;
        }
        if (itemCountHeader) {
            itemCountHeader.textContent = `物品: ${itemCount}`;
        }
        if (visitedRooms) {
            visitedRooms.textContent = `已访问: ${visitedCount}/${totalRooms}`;
        }
        
        // 更新背包列表
        updateInventory();
    }
    
    // 更新推荐命令
    updateCommandChips();
}

/**
 * 更新出口列表
 */
function updateExitsList() {
    const exitsList = document.getElementById('exits-list');
    if (!exitsList || !gameState.currentRoom || !gameState.currentRoom.exits) return;
    
    exitsList.innerHTML = '';
    const exits = gameState.currentRoom.exits;
    const directions = [
        { key: 'north', label: '北', icon: '↑' },
        { key: 'south', label: '南', icon: '↓' },
        { key: 'east', label: '东', icon: '→' },
        { key: 'west', label: '西', icon: '←' }
    ];
    
    directions.forEach(dir => {
        if (exits[dir.key]) {
            const chip = document.createElement('button');
            chip.className = 'exit-chip';
            chip.textContent = `${dir.icon} ${dir.label}`;
            chip.title = `前往${dir.label} (${dir.key})`;
            chip.addEventListener('click', () => {
                if (gameState.isLoggedIn) {
                    executeCommand(`go ${dir.key}`);
                }
            });
            exitsList.appendChild(chip);
        }
    });
}

/**
 * 更新方向罗盘
 */
function updateCompass() {
    if (!gameState.currentRoom || !gameState.currentRoom.exits) return;
    
    const exits = gameState.currentRoom.exits;
    const compassButtons = {
        'north': document.getElementById('compass-north'),
        'south': document.getElementById('compass-south'),
        'east': document.getElementById('compass-east'),
        'west': document.getElementById('compass-west')
    };
    
    Object.keys(compassButtons).forEach(dir => {
        const btn = compassButtons[dir];
        if (btn) {
            btn.disabled = !exits[dir] || !gameState.isLoggedIn;
        }
    });
}

/**
 * 更新房间物品列表
 */
function updateRoomItems() {
    const roomItemsList = document.getElementById('room-items-list');
    if (!roomItemsList) return;
    
    roomItemsList.innerHTML = '';
    
    if (!gameState.currentRoom || !gameState.currentRoom.items || gameState.currentRoom.items.length === 0) {
        roomItemsList.innerHTML = '<p class="empty-state">暂无物品</p>';
        return;
    }
    
    gameState.currentRoom.items.forEach(item => {
        const itemDiv = document.createElement('div');
        itemDiv.className = 'room-item';
        
        const itemInfo = document.createElement('div');
        itemInfo.className = 'room-item-info';
        itemInfo.innerHTML = `
            <span class="room-item-icon">📦</span>
            <div>
                <div class="room-item-name">${item.name}</div>
                <div class="room-item-weight">${item.weight.toFixed(2)} kg</div>
            </div>
        `;
        
        const itemAction = document.createElement('button');
        itemAction.className = 'room-item-action';
        itemAction.textContent = '拾取';
        itemAction.disabled = !gameState.isLoggedIn;
        itemAction.addEventListener('click', () => {
            if (gameState.isLoggedIn) {
                executeCommand(`take ${item.name}`);
            }
        });
        
        itemDiv.appendChild(itemInfo);
        itemDiv.appendChild(itemAction);
        roomItemsList.appendChild(itemDiv);
    });
}

/**
 * 更新背包列表
 */
function updateInventory() {
    // 更新背包悬浮按钮的徽章
    const inventoryBadge = document.getElementById('inventory-badge');
    const inventoryFab = document.getElementById('inventory-fab');
    const inventoryGrid = document.getElementById('inventory-grid');
    
    if (!inventoryGrid) return;
    
    inventoryGrid.innerHTML = '';
    
    if (!gameState.player || !gameState.player.inventory || gameState.player.inventory.length === 0) {
        inventoryGrid.innerHTML = `
            <div class="inventory-empty">
                <div class="inventory-empty-icon">🎒</div>
                <div class="inventory-empty-text">背包为空</div>
            </div>
        `;
        if (inventoryBadge) {
            inventoryBadge.style.display = 'none';
        }
        return;
    }
    
    // 更新徽章显示物品数量
    if (inventoryBadge) {
        inventoryBadge.textContent = gameState.player.inventory.length;
        inventoryBadge.style.display = 'flex';
    }
    
    // 获取物品图标映射
    const itemIcons = {
        'cookie': '🍪',
        'key': '🔑',
        'potion': '🧪',
        'scroll': '📜',
        'book': '📖',
        'default': '📦'
    };
    
    gameState.player.inventory.forEach((item, index) => {
        const itemCard = document.createElement('div');
        itemCard.className = 'inventory-item-card';
        
        // 获取物品图标
        const itemNameLower = item.name.toLowerCase();
        let icon = itemIcons.default;
        for (const [key, value] of Object.entries(itemIcons)) {
            if (itemNameLower.includes(key)) {
                icon = value;
                break;
            }
        }
        
        itemCard.innerHTML = `
            <div class="inventory-item-icon">${icon}</div>
            <div class="inventory-item-name">${item.name}</div>
            <div class="inventory-item-weight">${item.weight.toFixed(2)} kg</div>
        `;
        
        // 添加菜单按钮
        const itemMenu = document.createElement('div');
        itemMenu.className = 'inventory-item-menu';
        
        const toggleBtn = document.createElement('button');
        toggleBtn.className = 'inventory-item-toggle';
        toggleBtn.textContent = '⋮';
        toggleBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            const dropdown = itemMenu.querySelector('.inventory-item-dropdown');
            if (dropdown) {
                // 关闭其他下拉菜单
                document.querySelectorAll('.inventory-item-dropdown').forEach(d => {
                    if (d !== dropdown) d.classList.remove('show');
                });
                dropdown.classList.toggle('show');
            }
        });
        
        const dropdown = document.createElement('div');
        dropdown.className = 'inventory-item-dropdown';
        dropdown.innerHTML = `
            <button class="dropdown-item" data-action="drop">丢弃</button>
            <button class="dropdown-item" data-action="look">查看</button>
        `;
        
        dropdown.querySelectorAll('.dropdown-item').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const action = btn.getAttribute('data-action');
                if (action === 'drop') {
                    executeCommand(`drop ${item.name}`);
                    closeInventoryModal();
                } else if (action === 'look') {
                    executeCommand(`look ${item.name}`);
                }
                dropdown.classList.remove('show');
            });
        });
        
        itemMenu.appendChild(toggleBtn);
        itemMenu.appendChild(dropdown);
        itemCard.appendChild(itemMenu);
        
        // 点击卡片也可以查看物品
        itemCard.addEventListener('click', (e) => {
            if (!e.target.closest('.inventory-item-menu')) {
                executeCommand(`look ${item.name}`);
            }
        });
        
        inventoryGrid.appendChild(itemCard);
    });
    
    // 点击外部关闭下拉菜单
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.inventory-item-menu')) {
            document.querySelectorAll('.inventory-item-dropdown').forEach(d => {
                d.classList.remove('show');
            });
        }
    });
}

/**
 * 更新推荐命令chips
 */
function updateCommandChips() {
    const commandChips = document.getElementById('command-chips');
    if (!commandChips) return;
    
    commandChips.innerHTML = '';
    
    if (!gameState.isLoggedIn) return;
    
    const suggestions = [];
    
    // 根据当前状态推荐命令
    if (gameState.currentRoom) {
        const exits = gameState.currentRoom.exits || {};
        if (exits.north) suggestions.push({ cmd: 'go north', label: '前往北方' });
        if (exits.south) suggestions.push({ cmd: 'go south', label: '前往南方' });
        if (exits.east) suggestions.push({ cmd: 'go east', label: '前往东方' });
        if (exits.west) suggestions.push({ cmd: 'go west', label: '前往西方' });
        
        if (gameState.currentRoom.items && gameState.currentRoom.items.length > 0) {
            suggestions.push({ cmd: 'look', label: '查看房间' });
        }
    }
    
    if (gameState.player && gameState.player.inventory && gameState.player.inventory.length > 0) {
        suggestions.push({ cmd: 'items', label: '查看物品' });
    }
    
    // 添加常用命令
    suggestions.push({ cmd: 'help', label: '帮助' });
    
    // 限制显示数量
    suggestions.slice(0, 6).forEach(suggestion => {
        const chip = document.createElement('button');
        chip.className = 'command-chip';
        chip.textContent = suggestion.label;
        chip.title = suggestion.cmd;
        chip.addEventListener('click', () => {
            executeCommand(suggestion.cmd);
        });
        commandChips.appendChild(chip);
    });
}

/**
 * 添加输出消息（聊天式气泡）
 */
function addOutputMessage(message, type = 'game-response') {
    const outputArea = document.getElementById('output-area');
    
    // 清除欢迎消息（如果存在）
    const welcomeMsg = outputArea.querySelector('.welcome-message');
    if (welcomeMsg) {
        welcomeMsg.remove();
    }
    
    const messageContainer = document.createElement('div');
    messageContainer.className = 'log-message';
    
    const bubble = document.createElement('div');
    
    // 确定消息类型和样式
    let bubbleClass = 'message-bubble ';
    if (type === 'user-command') {
        bubbleClass += 'message-user message-command';
    } else if (type === 'error') {
        bubbleClass += 'message-system message-error';
    } else if (type === 'success') {
        bubbleClass += 'message-system message-success';
    } else {
        bubbleClass += 'message-system';
    }
    
    bubble.className = bubbleClass;
    bubble.textContent = message;
    
    messageContainer.appendChild(bubble);
    outputArea.appendChild(messageContainer);
    
    // 自动滚动到底部
    outputArea.scrollTop = outputArea.scrollHeight;
}

/**
 * 清空输出区域
 */
function clearOutput() {
    const outputArea = document.getElementById('output-area');
    if (outputArea) {
        outputArea.innerHTML = '';
    }
}

/**
 * 设置加载状态
 */
function setLoadingState(loading) {
    gameState.isLoading = loading;
    const submitBtn = document.getElementById('submit-btn');
    const commandInput = document.getElementById('command-input');
    
    if (loading) {
        submitBtn.disabled = true;
        submitBtn.textContent = '执行中...';
        commandInput.disabled = true;
    } else {
        submitBtn.disabled = !gameState.isLoggedIn;
        submitBtn.textContent = '执行';
        commandInput.disabled = !gameState.isLoggedIn;
        if (gameState.isLoggedIn) {
            commandInput.focus();
        }
    }
}

/**
 * 显示物品选择对话框
 */
async function showItemSelection(action) {
    try {
        // 确保使用最新的房间/背包数据
        await loadGameState();

        // 获取当前房间或玩家物品
        let items = [];
        let title = '';
        
        if (action === 'take') {
            // 详细的调试日志
            console.log('=== 拾取物品按钮点击 ===');
            console.log('当前房间对象:', gameState.currentRoom);
            console.log('房间物品数组:', gameState.currentRoom?.items);
            console.log('物品数组类型:', Array.isArray(gameState.currentRoom?.items) ? '数组' : typeof gameState.currentRoom?.items);
            console.log('物品数组长度:', gameState.currentRoom?.items?.length);
            console.log('房间描述:', gameState.currentRoom?.longDescription);
            
            // 检查房间是否有物品
            if (gameState.currentRoom) {
                // 如果 items 数组存在且不为空，直接使用
                if (gameState.currentRoom.items && Array.isArray(gameState.currentRoom.items) && gameState.currentRoom.items.length > 0) {
                    console.log('✅ 找到物品数组，包含', gameState.currentRoom.items.length, '个物品');
                    items = gameState.currentRoom.items;
                    title = '选择要拾取的物品';
                } 
                // 如果 items 数组为空或不存在，但房间描述中包含"物品:"关键字，说明数据可能不同步
                // 尝试重新加载一次状态
                else if (gameState.currentRoom.longDescription && gameState.currentRoom.longDescription.includes('物品:')) {
                    console.warn('⚠️ 检测到房间描述包含"物品:"，但 items 数组为空或不存在，尝试重新加载状态...');
                    // 重新加载状态
                    await loadGameState();
                    console.log('重新加载后的房间物品数组:', gameState.currentRoom?.items);
                    console.log('重新加载后的物品数组长度:', gameState.currentRoom?.items?.length);
                    // 再次检查
                    if (gameState.currentRoom && gameState.currentRoom.items && 
                        Array.isArray(gameState.currentRoom.items) && gameState.currentRoom.items.length > 0) {
                        console.log('✅ 重新加载后找到物品数组');
                        items = gameState.currentRoom.items;
                        title = '选择要拾取的物品';
                    } else {
                        console.error('❌ 重新加载后仍然没有找到物品数组');
                        console.error('房间对象:', JSON.stringify(gameState.currentRoom, null, 2));
                        addOutputMessage('当前房间没有物品！', 'error');
                        return;
                    }
                } else {
                    console.warn('⚠️ 房间没有物品数组，且描述中也不包含"物品:"关键字');
                    addOutputMessage('当前房间没有物品！', 'error');
                    return;
                }
            } else {
                console.error('❌ 当前房间对象不存在');
                addOutputMessage('当前房间没有物品！', 'error');
                return;
            }
        } else if (action === 'drop') {
            if (gameState.player && gameState.player.inventory) {
                items = gameState.player.inventory;
                title = '选择要丢弃的物品';
            } else {
                addOutputMessage('你没有携带任何物品！', 'error');
                return;
            }
        }
        
        if (items.length === 0) {
            console.warn('⚠️ 最终物品数组为空');
            addOutputMessage(action === 'take' ? '当前房间没有物品！' : '你没有携带任何物品！', 'error');
            return;
        }
        
        // 显示模态框
        const modal = document.getElementById('item-modal');
        const modalTitle = document.getElementById('modal-title');
        const modalBody = document.getElementById('modal-body');
        
        modalTitle.textContent = title;
        modalBody.innerHTML = '<ul class="item-list"></ul>';
        
        const itemList = modalBody.querySelector('.item-list');
        
        items.forEach(item => {
            const listItem = document.createElement('li');
            listItem.className = 'item-list-item';
            listItem.innerHTML = `
                <strong>${item.name}</strong>
                <div class="item-desc">${item.description}</div>
                <div class="item-desc">重量: ${item.weight.toFixed(2)} 千克</div>
            `;
            listItem.addEventListener('click', () => {
                executeCommand(`${action} ${item.name}`);
                closeModal();
            });
            itemList.appendChild(listItem);
        });
        
        modal.classList.add('show');
        
    } catch (error) {
        console.error('显示物品选择时出错:', error);
        addOutputMessage('无法加载物品列表', 'error');
    }
}

/**
 * 关闭模态框
 */
function closeModal() {
    const modal = document.getElementById('item-modal');
    if (modal) modal.classList.remove('show');
}

/**
 * 显示帮助对话框
 */
function showHelpModal() {
    const helpModal = document.getElementById('help-modal');
    const helpContent = document.getElementById('help-content');
    
    if (!helpModal || !helpContent) return;
    
    helpContent.innerHTML = `
        <h4>基本命令</h4>
        <p><code>go [方向]</code> - 移动（north/south/east/west 或 北/南/东/西）</p>
        <p><code>look</code> - 查看当前房间</p>
        <p><code>take [物品名]</code> - 拾取物品</p>
        <p><code>drop [物品名]</code> - 丢弃物品</p>
        <p><code>items</code> - 查看背包</p>
        <p><code>eat [物品名]</code> - 吃掉物品（如魔法饼干可增加负重上限）</p>
        <p><code>quit</code> - 退出游戏</p>
        
        <h4>键盘快捷键</h4>
        <p><code>W</code> - 向北移动</p>
        <p><code>S</code> - 向南移动</p>
        <p><code>A</code> - 向西移动</p>
        <p><code>D</code> - 向东移动</p>
        <p><code>Enter</code> - 执行命令</p>
        
        <h4>游戏提示</h4>
        <p>• 点击房间物品列表中的"拾取"按钮可以直接拾取物品</p>
        <p>• 点击背包物品旁的"⋮"菜单可以丢弃或查看物品</p>
        <p>• 注意负重限制，超过上限将无法拾取新物品</p>
        <p>• 吃掉魔法饼干可以增加负重上限</p>
        <p>• 探索所有6个房间以完成游戏</p>
    `;
    
    helpModal.classList.add('show');
}

/**
 * 关闭帮助对话框
 */
function closeHelpModal() {
    const helpModal = document.getElementById('help-modal');
    if (helpModal) helpModal.classList.remove('show');
}

// 页面加载完成后初始化游戏
document.addEventListener('DOMContentLoaded', () => {
    initGame();
});
