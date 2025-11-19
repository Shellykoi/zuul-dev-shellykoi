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
    isLoading: false
};

/**
 * 初始化游戏
 */
function initGame() {
    console.log('初始化游戏...');
    
    // 绑定事件监听器
    setupEventListeners();
    
    // 加载游戏初始状态
    loadGameState();
    
    // 添加欢迎消息（已在HTML中显示，这里不需要重复）
}

/**
 * 设置事件监听器
 */
function setupEventListeners() {
    // 命令输入框
    const commandInput = document.getElementById('command-input');
    const submitBtn = document.getElementById('submit-btn');
    
    // 回车提交命令
    commandInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !gameState.isLoading) {
            executeCommand();
        }
    });
    
    // 提交按钮
    submitBtn.addEventListener('click', () => {
        if (!gameState.isLoading) {
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
            const command = btn.getAttribute('data-command');
            executeCommand(command);
        });
    });
    
    // 动作按钮
    document.querySelectorAll('.btn-action').forEach(btn => {
        btn.addEventListener('click', () => {
            const command = btn.getAttribute('data-command');
            executeCommand(command);
        });
    });
    
    // 物品操作按钮
    document.getElementById('btn-take').addEventListener('click', () => {
        showItemSelection('take');
    });
    
    document.getElementById('btn-drop').addEventListener('click', () => {
        showItemSelection('drop');
    });
    
    document.querySelectorAll('.btn-item[data-command]').forEach(btn => {
        if (!btn.id || btn.id === 'btn-take' || btn.id === 'btn-drop') return;
        btn.addEventListener('click', () => {
            const command = btn.getAttribute('data-command');
            executeCommand(command);
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
 * 执行游戏命令
 */
async function executeCommand(command = null) {
    if (gameState.isLoading) {
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
        // 调用API执行命令
        const response = await fetch(`${API_BASE_URL}/command`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ command: command })
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
        addOutputMessage(data.message, messageType);
    }
    
    // 如果是退出命令
    if (data.quit) {
        setTimeout(() => {
            alert('感谢游玩！再见！');
            window.close();
        }, 500);
    }
}

/**
 * 加载游戏状态
 */
async function loadGameState() {
    try {
        const response = await fetch(`${API_BASE_URL}/status`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
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
        // 房间描述已经是中文，直接使用
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
        document.getElementById('btn-north').disabled = !exits.north;
        document.getElementById('btn-south').disabled = !exits.south;
        document.getElementById('btn-east').disabled = !exits.east;
        document.getElementById('btn-west').disabled = !exits.west;
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
    addOutputMessage('日志已清空。', 'game-response');
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
        submitBtn.disabled = false;
        submitBtn.textContent = '执行';
        commandInput.disabled = false;
        commandInput.focus();
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
            // 获取房间物品
            if (gameState.currentRoom && gameState.currentRoom.items) {
                items = gameState.currentRoom.items;
                title = '选择要拾取的物品';
            } else {
                addOutputMessage('当前房间没有物品！', 'error');
                return;
            }
        } else if (action === 'drop') {
            // 获取玩家物品
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

// 定期更新游戏状态（每5秒）
setInterval(() => {
    if (!gameState.isLoading) {
        loadGameState();
    }
}, 5000);

