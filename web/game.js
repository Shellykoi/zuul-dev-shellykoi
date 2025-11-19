/**
 * World of Zuul 游戏前端逻辑
 * 处理用户交互、API调用和界面更新
 */

// API基础URL - 根据实际部署情况修改
const API_BASE_URL = 'http://localhost:8080/api';

// 游戏状态
let gameState = {
    currentRoom: null,
    player: null,
    isLoading: false,
    sessionId: null,
    username: null,
    isLoggedIn: false
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
    // 登录/注册相关
    document.getElementById('login-tab').addEventListener('click', () => switchTab('login'));
    document.getElementById('register-tab').addEventListener('click', () => switchTab('register'));
    document.getElementById('login-btn').addEventListener('click', handleLogin);
    document.getElementById('register-btn').addEventListener('click', handleRegister);
    document.getElementById('guest-btn').addEventListener('click', handleGuestLogin);
    
    // 登录表单回车提交
    document.getElementById('login-username').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') handleLogin();
    });
    document.getElementById('login-password').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') handleLogin();
    });
    
    // 注册表单回车提交
    document.getElementById('register-username').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') handleRegister();
    });
    document.getElementById('register-password').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') handleRegister();
    });
    document.getElementById('register-password-confirm').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') handleRegister();
    });
    
    // 命令输入框
    const commandInput = document.getElementById('command-input');
    const submitBtn = document.getElementById('submit-btn');
    
    // 回车提交命令
    commandInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !gameState.isLoading && gameState.isLoggedIn) {
            executeCommand();
        }
    });
    
    // 提交按钮
    submitBtn.addEventListener('click', () => {
        if (!gameState.isLoading && gameState.isLoggedIn) {
            executeCommand();
        }
    });
    
    // 退出按钮
    document.getElementById('quit-btn').addEventListener('click', () => {
        if (confirm('确定要退出游戏吗？')) {
            executeCommand('quit');
        }
    });
    
    // 清空日志按钮
    document.getElementById('clear-btn').addEventListener('click', () => {
        clearOutput();
    });
    
    // 方向按钮
    document.querySelectorAll('.btn-direction').forEach(btn => {
        btn.addEventListener('click', () => {
            if (gameState.isLoggedIn) {
                const command = btn.getAttribute('data-command');
                executeCommand(command);
            }
        });
    });
    
    // 动作按钮
    document.querySelectorAll('.btn-action').forEach(btn => {
        btn.addEventListener('click', () => {
            if (gameState.isLoggedIn) {
                const command = btn.getAttribute('data-command');
                executeCommand(command);
            }
        });
    });
    
    // 物品操作按钮
    document.getElementById('btn-take').addEventListener('click', () => {
        if (gameState.isLoggedIn) {
            showItemSelection('take');
        }
    });
    
    document.getElementById('btn-drop').addEventListener('click', () => {
        if (gameState.isLoggedIn) {
            showItemSelection('drop');
        }
    });
    
    document.querySelectorAll('.btn-item[data-command]').forEach(btn => {
        if (!btn.id || btn.id === 'btn-take' || btn.id === 'btn-drop') return;
        btn.addEventListener('click', () => {
            if (gameState.isLoggedIn) {
                const command = btn.getAttribute('data-command');
                executeCommand(command);
            }
        });
    });
    
    // 模态框关闭
    document.getElementById('modal-close').addEventListener('click', () => {
        closeModal();
    });
    
    // 点击模态框外部关闭
    document.getElementById('item-modal').addEventListener('click', (e) => {
        if (e.target.id === 'item-modal') {
            closeModal();
        }
    });
}

/**
 * 切换登录/注册标签页
 */
function switchTab(tab) {
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
    }
    
    // 清空消息
    document.getElementById('login-message').textContent = '';
    document.getElementById('register-message').textContent = '';
}

/**
 * 显示登录模态框
 */
function showLoginModal() {
    const modal = document.getElementById('login-modal');
    modal.classList.add('show');
}

/**
 * 隐藏登录模态框
 */
function hideLoginModal() {
    const modal = document.getElementById('login-modal');
    modal.classList.remove('show');
}

/**
 * 处理登录
 */
async function handleLogin() {
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value.trim();
    const loadSaved = document.getElementById('load-saved-game').checked;
    const messageEl = document.getElementById('login-message');
    
    if (!username || !password) {
        showMessage(messageEl, '请输入用户名和密码', 'error');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password })
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        if (data.success) {
            gameState.sessionId = data.sessionId;
            gameState.username = data.username;
            gameState.isLoggedIn = true;
            
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
        console.error('登录时出错:', error);
        showMessage(messageEl, '无法连接到服务器，请确保服务器正在运行', 'error');
    }
}

/**
 * 处理注册
 */
async function handleRegister() {
    const username = document.getElementById('register-username').value.trim();
    const password = document.getElementById('register-password').value.trim();
    const passwordConfirm = document.getElementById('register-password-confirm').value.trim();
    const messageEl = document.getElementById('register-message');
    
    if (!username || !password) {
        showMessage(messageEl, '请输入用户名和密码', 'error');
        return;
    }
    
    if (password !== passwordConfirm) {
        showMessage(messageEl, '两次输入的密码不一致', 'error');
        return;
    }
    
    if (password.length < 3) {
        showMessage(messageEl, '密码长度至少为3个字符', 'error');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password })
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        if (data.success) {
            gameState.sessionId = data.sessionId;
            gameState.username = data.username;
            gameState.isLoggedIn = true;
            
            showMessage(messageEl, '注册成功！正在登录...', 'success');
            
            setTimeout(() => {
                hideLoginModal();
                setGameEnabled(true);
                loadGameState();
                addOutputMessage(`欢迎，${username}！开始新游戏。`, 'game-response');
                addOutputMessage('输入 \'help\' 查看所有可用命令。', 'game-response');
            }, 500);
        } else {
            showMessage(messageEl, data.message || '注册失败', 'error');
        }
    } catch (error) {
        console.error('注册时出错:', error);
        showMessage(messageEl, '无法连接到服务器，请确保服务器正在运行', 'error');
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
        const response = await fetch(`${API_BASE_URL}/load`, {
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
    const commandInput = document.getElementById('command-input');
    const submitBtn = document.getElementById('submit-btn');
    const gameContainer = document.querySelector('.game-container');
    
    commandInput.disabled = !enabled;
    submitBtn.disabled = !enabled;
    
    // 禁用所有按钮
    document.querySelectorAll('.btn-direction, .btn-action, .btn-item').forEach(btn => {
        btn.disabled = !enabled;
    });
    
    if (enabled) {
        gameContainer.style.opacity = '1';
        commandInput.focus();
    } else {
        gameContainer.style.opacity = '0.5';
    }
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
        const response = await fetch(`${API_BASE_URL}/command`, {
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
        // 处理多行消息
        const lines = data.message.split('\n');
        lines.forEach(line => {
            if (line.trim()) {
                addOutputMessage(line, messageType);
            }
        });
    }
    
    // 如果是退出命令
    if (data.quit) {
        setTimeout(() => {
            gameState.isLoggedIn = false;
            setGameEnabled(false);
            showLoginModal();
            clearOutput();
            addOutputMessage('已退出游戏。', 'game-response');
        }, 500);
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
        let url = `${API_BASE_URL}/status`;
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
        
        // 更新游戏状态
        gameState.currentRoom = data.currentRoom;
        gameState.player = data.player;
        
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
    // 更新顶部状态栏
    if (gameState.currentRoom) {
        const shortDesc = gameState.currentRoom.shortDescription || '未知房间';
        document.getElementById('room-name').textContent = shortDesc;
    }
    
    if (gameState.player) {
        const totalWeight = gameState.player.totalWeight || 0;
        const maxWeight = gameState.player.maxWeight || 10;
        const itemCount = gameState.player.inventory?.length || 0;
        
        document.getElementById('weight-info').textContent = 
            `负重: ${totalWeight.toFixed(1)}/${maxWeight.toFixed(1)} 千克`;
        document.getElementById('item-count').textContent = 
            `物品: ${itemCount}`;
    }
    
    // 更新方向按钮状态
    if (gameState.currentRoom && gameState.currentRoom.exits) {
        const exits = gameState.currentRoom.exits;
        document.getElementById('btn-north').disabled = !exits.north || !gameState.isLoggedIn;
        document.getElementById('btn-south').disabled = !exits.south || !gameState.isLoggedIn;
        document.getElementById('btn-east').disabled = !exits.east || !gameState.isLoggedIn;
        document.getElementById('btn-west').disabled = !exits.west || !gameState.isLoggedIn;
    }
}

/**
 * 添加输出消息
 */
function addOutputMessage(message, type = 'game-response') {
    const outputArea = document.getElementById('output-area');
    const messageDiv = document.createElement('div');
    messageDiv.className = `output-message ${type}`;
    messageDiv.textContent = message;
    
    outputArea.appendChild(messageDiv);
    
    // 自动滚动到底部
    outputArea.scrollTop = outputArea.scrollHeight;
}

/**
 * 清空输出区域
 */
function clearOutput() {
    const outputArea = document.getElementById('output-area');
    outputArea.innerHTML = '';
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
        // 获取当前房间或玩家物品
        let items = [];
        let title = '';
        
        if (action === 'take') {
            if (gameState.currentRoom && gameState.currentRoom.items) {
                items = gameState.currentRoom.items;
                title = '选择要拾取的物品';
            } else {
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
    modal.classList.remove('show');
}

// 页面加载完成后初始化游戏
document.addEventListener('DOMContentLoaded', () => {
    initGame();
});
