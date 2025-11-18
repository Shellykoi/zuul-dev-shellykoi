# World of Zuul 功能扩展与改进记录

本文档记录对 World of Zuul 游戏项目的所有功能扩展和改进步骤。

---

## 项目初始状态分析

### 代码结构
项目包含以下核心类：
- `Main.java`: 程序入口
- `Game.java`: 游戏主控制类
- `Room.java`: 房间类
- `Command.java`: 命令类
- `CommandWords.java`: 命令词验证类
- `Parser.java`: 命令解析器

### 设计缺陷分析

1. **命令处理设计缺陷**
   - `Game.processCommand()` 方法使用大量 if-else 语句处理命令
   - 添加新命令需要修改核心方法，违反开闭原则
   - 命令处理逻辑耦合在 Game 类中

2. **缺少物品系统**
   - 房间无法存储物品
   - 玩家无法携带物品
   - 没有物品重量限制机制

3. **缺少玩家类**
   - 玩家信息直接存储在 Game 类中
   - 无法管理玩家状态和属性

4. **缺少历史记录**
   - 无法实现 back 命令
   - 没有房间访问历史追踪

5. **功能单一**
   - 只有基本的移动和退出功能
   - 缺少游戏趣味性

---

## 改进计划

### 第一阶段：基础架构改进
1. 重构命令处理系统（使用命令模式）
2. 创建物品系统（Item 类）
3. 创建玩家类（Player 类）
4. 实现房间物品存储功能

### 第二阶段：核心功能扩展
1. 实现 look 命令（查看房间和物品）
2. 实现 back 命令（返回上一个房间）
3. 实现 take/drop 命令（拾取/丢弃物品）
4. 实现 items 命令（查看物品列表）

### 第三阶段：高级功能扩展
1. 实现魔法饼干系统
2. 实现传输房间功能
3. 优化 back 命令（支持多级回退）

---

## 详细改进记录

### 步骤 1: 创建 Item 类（物品系统基础）
**时间**: 开始改进
**描述**: 创建 Item 类，用于表示游戏中的物品，包含名称、描述和重量属性。

**实现内容**:
- 创建 `Item.java` 类
- 属性：name（名称）、description（描述）、weight（重量）
- 提供 getter 方法和 toString 方法

**状态**: ✅ 已完成

**实现细节**:
- 文件位置：`src/cn/edu/whut/sept/zuul/Item.java`
- 实现了完整的物品属性管理
- 提供了友好的字符串表示方法

---

### 步骤 2: 扩展 Room 类（支持物品存储）
**时间**: 步骤1完成后
**描述**: 在 Room 类中添加物品存储功能，使房间可以存放多个物品。

**实现内容**:
- 在 Room 类中添加 `HashMap<String, Item> items` 属性
- 添加 `addItem()`, `removeItem()`, `getItem()`, `getItems()` 方法
- 修改 `getLongDescription()` 方法，包含物品信息
- 添加 `getItemsString()` 和 `getTotalWeight()` 方法

**状态**: ✅ 已完成

**实现细节**:
- 文件位置：`src/cn/edu/whut/sept/zuul/Room.java`
- 物品存储使用HashMap，键为物品名称（小写），便于查找
- 房间描述自动包含物品信息

---

### 步骤 3: 创建 Player 类（玩家系统）
**时间**: 步骤2完成后
**描述**: 创建独立的 Player 类来管理玩家信息、位置和物品。

**实现内容**:
- 创建 `Player.java` 类
- 属性：name（姓名）、currentRoom（当前房间）、inventory（物品清单）、maxWeight（最大负重）
- 方法：`takeItem()`, `dropItem()`, `getTotalWeight()`, `canCarry()`, `getInventory()`, `increaseMaxWeight()`

**状态**: ✅ 已完成

**实现细节**:
- 文件位置：`src/cn/edu/whut/sept/zuul/Player.java`
- 实现了完整的负重管理系统
- 支持动态增加最大负重（用于魔法饼干功能）

---

### 步骤 4: 重构命令处理系统（命令模式）
**时间**: 步骤3完成后
**描述**: 使用命令模式重构命令处理，使系统更易扩展。

**实现内容**:
- 创建 `CommandExecutor` 接口
- 为每个命令创建独立的执行类（`GoCommand`, `LookCommand`, `TakeCommand`, `DropCommand`, `BackCommand`, `ItemsCommand`, `HelpCommand`, `QuitCommand`, `EatCookieCommand`）
- 在 Game 类中使用 HashMap 存储命令映射
- 修改 `processCommand()` 方法使用命令模式

**状态**: ✅ 已完成

**实现细节**:
- 文件位置：`src/cn/edu/whut/sept/zuul/CommandExecutor.java` 及各个命令类
- 完全消除了 if-else 链，符合开闭原则
- 新增命令只需创建新的 CommandExecutor 实现类并注册即可

---

### 步骤 5: 实现 look 命令
**时间**: 步骤4完成后
**描述**: 实现 look 命令，查看当前房间信息和房间内所有物品。

**实现内容**:
- 创建 `LookCommand` 类
- 在 CommandWords 中添加 "look" 命令
- 显示房间描述、出口和物品列表

**状态**: ✅ 已完成

**实现细节**:
- 文件位置：`src/cn/edu/whut/sept/zuul/LookCommand.java`
- 利用 Room 类的 `getLongDescription()` 方法自动显示物品信息

---

### 步骤 6: 实现 back 命令（多级回退）
**时间**: 步骤5完成后
**描述**: 实现 back 命令，支持逐层回退到起点。

**实现内容**:
- 在 Game 类中添加房间历史栈（`Stack<Room> roomHistory`）
- 创建 `BackCommand` 类
- 在 go 命令中记录房间历史
- 支持多次 back 命令，直到回到起点

**状态**: ✅ 已完成

**实现细节**:
- 文件位置：`src/cn/edu/whut/sept/zuul/BackCommand.java`
- 使用栈结构实现多级回退
- 当历史为空时提示已到达起点

---

### 步骤 7: 实现 take 和 drop 命令
**时间**: 步骤6完成后
**描述**: 实现物品拾取和丢弃功能。

**实现内容**:
- 创建 `TakeCommand` 类
- 创建 `DropCommand` 类
- 实现重量检查逻辑
- 在 CommandWords 中添加新命令

**状态**: ✅ 已完成

**实现细节**:
- 文件位置：`src/cn/edu/whut/sept/zuul/TakeCommand.java`, `src/cn/edu/whut/sept/zuul/DropCommand.java`
- take 命令会检查玩家负重限制
- 超过负重时给出友好提示

---

### 步骤 8: 实现 items 命令
**时间**: 步骤7完成后
**描述**: 实现 items 命令，显示房间和玩家物品列表。

**实现内容**:
- 创建 `ItemsCommand` 类
- 显示房间物品及总重量
- 显示玩家物品及总重量

**状态**: ✅ 已完成

**实现细节**:
- 文件位置：`src/cn/edu/whut/sept/zuul/ItemsCommand.java`
- 同时显示房间和玩家物品，便于对比

---

### 步骤 9: 实现魔法饼干系统
**时间**: 步骤8完成后
**描述**: 添加魔法饼干物品和 eat cookie 命令。

**实现内容**:
- 在部分房间随机放置魔法饼干
- 创建 `EatCookieCommand` 类
- 实现增加玩家负重的功能（增加5kg）
- 修改 Parser 以支持 "eat cookie" 多词命令

**状态**: ✅ 已完成

**实现细节**:
- 文件位置：`src/cn/edu/whut/sept/zuul/EatCookieCommand.java`
- 魔法饼干在游戏初始化时随机放置在某个房间
- 吃掉饼干后增加5kg最大负重

---

### 步骤 10: 实现传输房间功能
**时间**: 步骤9完成后
**描述**: 创建具有随机传输功能的特殊房间。

**实现内容**:
- 创建 `TransporterRoom` 类（继承 Room）
- 重写 `getExit()` 方法实现随机传输
- 在游戏中添加传输房间
- 修改 `GoCommand` 以处理传输房间的自动传输

**状态**: ✅ 已完成

**实现细节**:
- 文件位置：`src/cn/edu/whut/sept/zuul/TransporterRoom.java`
- 传输房间从起始房间的北面可以进入
- 进入传输房间后会自动随机传送到其他房间
- 避免传送到传输房间本身

---

### 步骤 11: 优化 back 命令（多级回退）
**时间**: 步骤10完成后
**描述**: 改进 back 命令，支持逐层回退到起点。

**实现内容**:
- 使用栈结构存储房间历史
- 支持多次 back 命令
- 显示回退信息

**状态**: ✅ 已完成（已在步骤6中实现）

---

## 当前进度

**已完成**: 11/11 步骤 ✅
**进行中**: 功能测试和优化
**下一步**: 测试所有功能，确保游戏正常运行

---

## 已实现功能总结

### ✅ 已完成的功能扩展（共5项主要功能）

1. **物品系统**
   - ✅ 房间可以存储任意数量的物品
   - ✅ 每个物品有名称、描述和重量
   - ✅ look 命令可以查看房间内的所有物品

2. **玩家系统**
   - ✅ 独立的 Player 类管理玩家信息
   - ✅ 玩家可以携带物品，有负重限制（初始10kg）
   - ✅ take 和 drop 命令实现物品拾取和丢弃
   - ✅ items 命令显示房间和玩家物品列表

3. **back 命令（多级回退）**
   - ✅ 实现 back 命令返回上一个房间
   - ✅ 支持多次使用，逐层回退到起点
   - ✅ 使用栈结构记录房间历史

4. **魔法饼干系统**
   - ✅ 在随机房间放置魔法饼干
   - ✅ eat cookie 命令吃掉饼干
   - ✅ 吃掉饼干后增加5kg最大负重

5. **传输房间功能**
   - ✅ 创建 TransporterRoom 特殊房间
   - ✅ 进入传输房间后随机传送到其他房间
   - ✅ 增加游戏趣味性和挑战性

### ✅ 架构改进

1. **命令模式重构**
   - ✅ 使用命令模式替代 if-else 链
   - ✅ 每个命令独立成类，易于扩展
   - ✅ 符合开闭原则，新增命令无需修改核心代码

2. **代码质量提升**
   - ✅ 所有类都有完整的 JavaDoc 注释
   - ✅ 代码结构清晰，职责分离
   - ✅ 无编译错误，通过代码检查

---

## 备注

- 所有代码修改都会同步更新到本文件
- 每个步骤完成后会更新状态标记
- 如有设计变更会在此记录

