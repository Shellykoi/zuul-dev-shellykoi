# Git 中文提交正确流程指南

## 问题原因

在 Windows 环境下，如果 Git 提交消息包含中文时出现乱码，通常是因为：
1. **PowerShell 默认编码**：Windows PowerShell 默认使用 GB2312/GBK 编码（代码页 936），而不是 UTF-8
2. **Git 编辑器编码**：默认编辑器可能没有正确设置 UTF-8 编码
3. **终端输出编码**：显示时编码不匹配

## 解决方案

### 方案一：使用 `-m` 参数直接提交（推荐）

**最简单可靠的方法**，直接在命令行指定提交消息：

```powershell
# 单行提交消息
git commit -m "完成World of Zuul游戏功能扩展：物品系统、玩家系统、back命令、魔法饼干、传送房间，并重构为命令模式"

# 多行提交消息
git commit -m "完成World of Zuul游戏功能扩展" -m "- 物品系统" -m "- 玩家系统" -m "- back命令" -m "- 魔法饼干" -m "- 传送房间" -m "- 重构为命令模式"
```

**优点**：
- ✅ 简单直接，不需要配置编辑器
- ✅ 避免编码问题
- ✅ 适合大多数情况

**缺点**：
- ❌ 对于超长提交消息不够方便

---

### 方案二：配置 Git 和 PowerShell 编码（一劳永逸）

#### 步骤 1：配置 Git 编码（已配置，可跳过）

```powershell
# 设置提交消息编码为 UTF-8
git config --global i18n.commitencoding utf-8

# 设置日志输出编码为 UTF-8
git config --global i18n.logoutputencoding utf-8

# 设置路径引用不转义（避免中文路径问题）
git config --global core.quotepath false
```

#### 步骤 2：配置 PowerShell 编码

**方法 A：临时设置（每次 PowerShell 会话）**

在 PowerShell 中执行：
```powershell
# 设置控制台输出编码为 UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$env:LANG = "zh_CN.UTF-8"
chcp 65001
```

**方法 B：永久设置（推荐）**

创建或编辑 PowerShell 配置文件：
```powershell
# 查看配置文件路径
$PROFILE

# 如果文件不存在，创建它
if (!(Test-Path $PROFILE)) {
    New-Item -ItemType File -Path $PROFILE -Force
}

# 编辑配置文件，添加以下内容
notepad $PROFILE
```

在配置文件中添加：
```powershell
# 设置 UTF-8 编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$env:LANG = "zh_CN.UTF-8"
chcp 65001 | Out-Null
```

#### 步骤 3：配置 Git 编辑器

**使用 VS Code 作为编辑器（推荐）**：
```powershell
git config --global core.editor "code --wait"
```

**使用 Notepad++ 作为编辑器**：
```powershell
git config --global core.editor "'C:/Program Files/Notepad++/notepad++.exe' -multiInst -notabbar -nosession -noPlugin"
```

**使用系统默认编辑器**：
```powershell
git config --global core.editor notepad
```

#### 步骤 4：验证配置

```powershell
# 检查 Git 配置
git config --list | Select-String -Pattern "i18n|core.editor"

# 测试提交（使用 -m 参数）
git commit -m "测试中文提交：你好世界"
git log -1 --pretty=format:"%s"
```

---

### 方案三：使用 UTF-8 编码的提交消息文件

如果需要编写较长的提交消息：

#### 步骤 1：创建 UTF-8 编码的临时文件

```powershell
# 方法 A：使用 PowerShell（推荐）
$commitMsg = @"
完成World of Zuul游戏功能扩展

主要功能：
- 物品系统：支持物品的拾取、丢弃和查看
- 玩家系统：管理玩家状态和物品
- back命令：返回上一个房间
- 魔法饼干：特殊物品，可以增加负重
- 传送房间：随机传送到其他房间
- 重构为命令模式：提高代码可维护性
"@

# 保存为 UTF-8 编码文件
[System.IO.File]::WriteAllText("commit_msg.txt", $commitMsg, [System.Text.Encoding]::UTF8)
```

#### 步骤 2：使用文件提交

```powershell
git commit -F commit_msg.txt

# 提交后删除临时文件
Remove-Item commit_msg.txt
```

---

## 推荐工作流程

### 日常提交（推荐）

```powershell
# 1. 查看更改
git status
git diff

# 2. 添加文件
git add .

# 3. 直接提交（使用 -m 参数）
git commit -m "完成功能：添加物品系统"

# 4. 推送到远程
git push
```

### 复杂提交消息

```powershell
# 1. 创建提交消息文件（UTF-8编码）
$msg = @"
完成World of Zuul游戏功能扩展

详细说明：
- 实现了完整的物品系统
- 添加了玩家状态管理
- 实现了back命令功能
- 添加了魔法饼干特殊物品
- 实现了传送房间功能
- 重构代码为命令模式，提高可维护性

相关文件：
- src/cn/edu/whut/sept/zuul/Item.java
- src/cn/edu/whut/sept/zuul/Player.java
- src/cn/edu/whut/sept/zuul/BackCommand.java
"@

[System.IO.File]::WriteAllText("commit_msg.txt", $msg, [System.Text.Encoding]::UTF8)

# 2. 使用文件提交
git commit -F commit_msg.txt

# 3. 清理临时文件
Remove-Item commit_msg.txt
```

---

## 修复已存在的乱码提交

如果已经提交了乱码消息，需要修改：

### 修改最近一次提交

```powershell
git commit --amend -m "正确的中文提交消息"
```

### 修改历史提交（需要 rebase）

```powershell
# 1. 交互式 rebase（修改最近 3 次提交）
git rebase -i HEAD~3

# 2. 在编辑器中，将要修改的提交前的 'pick' 改为 'reword' 或 'r'

# 3. 保存并关闭编辑器

# 4. Git 会逐个打开提交消息编辑器，修改为正确的中文

# 5. 如果使用 -m 参数，可以在 rebase 过程中使用：
git commit --amend -m "正确的中文提交消息"
git rebase --continue

# 6. 强制推送到远程（谨慎使用）
git push --force-with-lease
```

---

## 验证中文提交是否正常

### 检查提交消息

```powershell
# 查看最近 5 次提交
git log --oneline -5

# 查看详细提交信息
git log -1 --pretty=format:"%h - %an, %ar : %s"

# 查看完整提交消息
git log -1
```

### 检查文件内容

```powershell
# 查看文件内容（确保中文正常显示）
git show HEAD:README.md
```

---

## 常见问题排查

### 问题 1：提交消息显示为乱码

**检查**：
```powershell
git config i18n.logoutputencoding
# 应该输出：utf-8
```

**解决**：
```powershell
git config --global i18n.logoutputencoding utf-8
```

### 问题 2：PowerShell 中中文显示乱码

**解决**：
```powershell
# 临时解决
chcp 65001
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# 永久解决：添加到 PowerShell 配置文件
```

### 问题 3：Git 编辑器打开后中文乱码

**解决**：
- 使用支持 UTF-8 的编辑器（VS Code、Notepad++）
- 配置编辑器默认保存为 UTF-8

### 问题 4：GitHub 上显示乱码

**检查**：
- 确保本地 `git log` 显示正常
- 如果本地正常但 GitHub 乱码，可能是推送时编码问题

**解决**：
```powershell
# 重新推送（如果已修复本地提交）
git push --force-with-lease
```

---

## 最佳实践总结

1. **优先使用 `-m` 参数**：最简单可靠
2. **配置 Git 编码**：设置 `i18n.commitencoding` 和 `i18n.logoutputencoding` 为 `utf-8`
3. **配置 PowerShell 编码**：在配置文件中设置 UTF-8
4. **使用 UTF-8 编辑器**：VS Code 或 Notepad++
5. **验证提交**：提交后立即检查 `git log` 确保中文正常显示
6. **避免在提交消息中混用编码**：统一使用 UTF-8

---

## 快速参考命令

```powershell
# 配置 Git 中文支持（一次性设置）
git config --global i18n.commitencoding utf-8
git config --global i18n.logoutputencoding utf-8
git config --global core.quotepath false

# 配置 PowerShell UTF-8（添加到 $PROFILE）
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

# 日常提交（推荐）
git commit -m "中文提交消息"

# 复杂提交消息
git commit -F commit_msg.txt  # commit_msg.txt 必须是 UTF-8 编码

# 修改最近一次提交
git commit --amend -m "正确的中文消息"
```

---

## 注意事项

⚠️ **重要提醒**：
- 修改已推送的提交历史需要使用 `git push --force-with-lease`，请谨慎操作
- 如果多人协作，修改历史前需要通知团队成员
- 建议在个人分支上测试后再合并到主分支

