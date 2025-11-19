# 数据库设置说明

## 数据库配置

游戏使用MySQL数据库存储用户信息、游戏状态和游戏记录。

### 数据库连接信息

- **数据库类型**: MySQL
- **主机**: localhost
- **端口**: 3306
- **数据库名**: zuul_game（自动创建）
- **用户名**: shellykoi
- **密码**: 123456koiii

### 数据库表结构

#### 1. users（用户表）
- `user_id`: 用户ID（主键，自增）
- `username`: 用户名（唯一）
- `password`: 密码
- `created_at`: 创建时间
- `last_login`: 最后登录时间

#### 2. game_records（游戏记录表）
- `record_id`: 记录ID（主键，自增）
- `user_id`: 用户ID（外键）
- `start_time`: 游戏开始时间
- `end_time`: 游戏结束时间
- `is_completed`: 是否通关
- `rooms_explored`: 探索的房间数量
- `items_collected`: 收集的物品数量
- `cookie_eaten`: 是否吃掉魔法饼干

#### 3. player_states（玩家状态表）
- `state_id`: 状态ID（主键，自增）
- `user_id`: 用户ID（外键，唯一）
- `current_room`: 当前所在房间
- `max_weight`: 最大负重
- `inventory`: 物品清单（逗号分隔）
- `rooms_visited`: 已访问的房间（逗号分隔）
- `items_collected_list`: 已收集的物品（逗号分隔）
- `cookie_eaten`: 是否吃掉魔法饼干
- `saved_at`: 保存时间

## 使用前准备

### 1. 安装MySQL

确保已安装MySQL数据库服务器。

### 2. 创建数据库用户

使用MySQL root用户登录，创建数据库用户：

```sql
CREATE USER 'shellykoi'@'localhost' IDENTIFIED BY '123456koiii';
GRANT ALL PRIVILEGES ON *.* TO 'shellykoi'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 添加MySQL JDBC驱动

将MySQL JDBC驱动（mysql-connector-java）添加到项目的classpath中。

**方法1：下载JAR文件（推荐）**
- 下载地址：https://dev.mysql.com/downloads/connector/j/
- 选择 "Platform Independent" 版本
- 下载并解压，找到 `mysql-connector-j-8.0.33.jar` 文件
- 将JAR文件放到项目的`lib`目录
- 详细步骤请参考 `MYSQL_DRIVER_SETUP.md` 文件

**方法2：使用Maven/Gradle**
如果项目使用Maven或Gradle，添加依赖：

Maven (`pom.xml`):
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
</dependency>
```

### 4. 运行游戏

**Windows系统命令**：
```powershell
# 编译（修改代码后执行）
javac -d bin -encoding UTF-8 -cp "lib\mysql-connector-j-8.0.33.jar" src\cn\edu\whut\sept\zuul\*.java

# 运行（每次启动程序）
java -cp "bin;lib\mysql-connector-j-8.0.33.jar" cn.edu.whut.sept.zuul.WebMain
```

**Linux/Mac系统命令**：
```bash
# 编译（修改代码后执行）
javac -d bin -encoding UTF-8 -cp "lib/mysql-connector-j-8.0.33.jar" src/cn/edu/whut/sept/zuul/*.java

# 运行（每次启动程序）
java -cp "bin:lib/mysql-connector-j-8.0.33.jar" cn.edu.whut.sept.zuul.WebMain
```

**注意**：
- Windows使用分号（`;`）作为classpath分隔符
- Linux/Mac使用冒号（`:`）作为classpath分隔符
- 编译命令：代码修改后需要重新编译
- 运行命令：每次启动程序都需要执行

首次运行游戏时，`DatabaseManager`会自动：
1. 连接到MySQL服务器
2. 创建`zuul_game`数据库（如果不存在）
3. 创建所有必需的表（如果不存在）

## API端点

### 注册新用户
- **URL**: `/api/register`
- **方法**: POST
- **请求体**: `{"username": "用户名", "password": "密码"}`
- **响应**: `{"success": true, "message": "注册成功！", "sessionId": "...", "username": "..."}`

### 用户登录
- **URL**: `/api/login`
- **方法**: POST
- **请求体**: `{"username": "用户名", "password": "密码"}`
- **响应**: `{"success": true, "message": "登录成功！", "sessionId": "...", "username": "..."}`

### 执行游戏命令
- **URL**: `/api/command`
- **方法**: POST
- **请求体**: `{"command": "命令", "sessionId": "会话ID"}`
- **响应**: `{"success": true, "message": "输出", "completed": false, "progress": {...}}`

### 保存游戏状态
- **URL**: `/api/save`
- **方法**: POST
- **请求体**: `{"sessionId": "会话ID"}`
- **响应**: `{"success": true, "message": "游戏状态已保存！"}`

### 加载游戏状态
- **URL**: `/api/load`
- **方法**: POST
- **请求体**: `{"sessionId": "会话ID"}`
- **响应**: `{"success": true, "message": "游戏状态已加载！", "gameStatus": {...}}`

### 获取游戏状态
- **URL**: `/api/status?sessionId=会话ID`
- **方法**: GET
- **响应**: `{"currentRoom": {...}, "player": {...}, "completion": {...}}`

## 注意事项

1. **密码安全**: 当前实现中密码以明文存储，生产环境应使用加密（如BCrypt）
2. **会话管理**: 会话存储在内存中，服务器重启后会丢失，需要重新登录
3. **数据库连接**: 确保MySQL服务正在运行，否则游戏无法使用数据库功能
4. **字符编码**: 数据库使用UTF-8编码，支持中文

## 故障排除

### 连接失败
- 检查MySQL服务是否运行
- 检查用户名和密码是否正确
- 检查MySQL是否允许本地连接

### 表创建失败
- 检查用户是否有创建数据库的权限
- 检查MySQL版本是否支持（建议5.7+或8.0+）

### 驱动未找到
- 确保MySQL JDBC驱动在classpath中
- 检查驱动版本是否兼容

