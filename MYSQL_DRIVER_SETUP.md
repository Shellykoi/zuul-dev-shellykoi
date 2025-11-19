# MySQL驱动安装指南

## 📋 概述

项目需要MySQL JDBC驱动来连接数据库。**我还没有下载驱动文件**，需要您手动下载并放置到项目中。

## 🔽 下载步骤

### 步骤1：访问MySQL官网

打开浏览器，访问：
```
https://dev.mysql.com/downloads/connector/j/
```

### 步骤2：选择下载

1. 在页面中找到 "Platform Independent" 选项
2. 选择版本（推荐 **8.0.33** 或更高版本）
3. 点击 "Download" 按钮
4. 在下载页面，**不需要登录**，直接点击 "No thanks, just start my download" 链接

### 步骤3：解压文件

1. 下载的文件通常是ZIP格式，例如：`mysql-connector-j-8.0.33.zip`
2. 解压ZIP文件
3. 在解压后的文件夹中找到 `mysql-connector-j-8.0.33.jar` 文件

### 步骤4：放置到项目

1. 将JAR文件复制到项目的 `lib` 目录中：
   ```
   se23-sept1-Shellykoi/
     └── lib/
         └── mysql-connector-j-8.0.33.jar
   ```

2. 确保文件名正确（注意版本号）

## ✅ 验证安装

检查 `lib` 目录中是否有JAR文件：
```powershell
# 在PowerShell中执行
dir lib\*.jar
```

应该能看到类似 `mysql-connector-j-8.0.33.jar` 的文件。

## 🚀 编译和运行命令说明

### 快速方式：使用批处理脚本（推荐）

项目提供了两个Windows批处理脚本，可以简化操作：

#### 1. `compile_and_run.bat` - 编译并运行
- **用途**：修改代码后使用，会自动编译并启动服务器
- **使用方法**：双击运行或在命令行执行 `compile_and_run.bat`
- **适用场景**：修改了代码，需要重新编译

#### 2. `run_only.bat` - 仅运行（不编译）
- **用途**：代码未修改，只需重启服务器
- **使用方法**：双击运行或在命令行执行 `run_only.bat`
- **适用场景**：代码未修改，只是重启程序

### 手动方式：使用命令行

### 关于命令的使用频率

**编译命令（javac）**：
- **每次修改代码后都需要重新编译**
- 如果代码没有修改，可以跳过编译直接运行

**运行命令（java）**：
- **每次启动程序都需要执行**
- 这是启动Web服务器的命令

### Windows系统命令（PowerShell）

由于您使用的是Windows系统，classpath分隔符应该使用**分号（`;`）**而不是冒号（`:`）：

#### 编译命令（修改代码后执行）
```powershell
javac -d bin -encoding UTF-8 -cp "lib\mysql-connector-j-8.0.33.jar" src\cn\edu\whut\sept\zuul\*.java
```

#### 运行命令（每次启动程序）
```powershell
java -cp "bin;lib\mysql-connector-j-8.0.33.jar" cn.edu.whut.sept.zuul.WebMain
```

### Linux/Mac系统命令

```bash
# 编译
javac -d bin -encoding UTF-8 -cp "lib/mysql-connector-j-8.0.33.jar" src/cn/edu/whut/sept/zuul/*.java

# 运行
java -cp "bin:lib/mysql-connector-j-8.0.33.jar" cn.edu.whut.sept.zuul.WebMain
```

## 📝 快速参考

### 首次设置（只需一次）
1. ✅ 下载MySQL驱动JAR文件
2. ✅ 放置到 `lib/` 目录
3. ✅ 确保MySQL服务运行
4. ✅ 确保数据库用户已创建

### 日常开发流程

#### 方式1：使用批处理脚本（推荐）

**场景1：修改了代码**
```powershell
# 双击运行 compile_and_run.bat
# 或在命令行执行：
.\compile_and_run.bat
```

**场景2：代码未修改，只是重启程序**
```powershell
# 双击运行 run_only.bat
# 或在命令行执行：
.\run_only.bat
```

#### 方式2：手动执行命令

**场景1：修改了代码**
```powershell
# 1. 重新编译
javac -d bin -encoding UTF-8 -cp "lib\mysql-connector-j-8.0.33.jar" src\cn\edu\whut\sept\zuul\*.java

# 2. 运行程序
java -cp "bin;lib\mysql-connector-j-8.0.33.jar" cn.edu.whut.sept.zuul.WebMain
```

**场景2：代码未修改，只是重启程序**
```powershell
# 直接运行（不需要重新编译）
java -cp "bin;lib\mysql-connector-j-8.0.33.jar" cn.edu.whut.sept.zuul.WebMain
```

## 🔧 常见问题

### Q1: 找不到JAR文件
**A**: 检查文件路径是否正确，确保JAR文件在 `lib` 目录中，文件名拼写正确。

### Q2: ClassNotFoundException
**A**: 
- 检查classpath是否正确包含JAR文件路径
- Windows使用分号（`;`），Linux/Mac使用冒号（`:`）
- 确保JAR文件名与命令中的一致

### Q3: 版本不匹配
**A**: 如果下载的版本不是8.0.33，请修改命令中的JAR文件名。

### Q4: 可以直接使用Maven/Gradle吗？
**A**: 可以！如果项目使用Maven或Gradle，可以添加依赖而不是手动下载JAR文件。

## 📌 重要提示

1. **驱动文件只需下载一次**，之后一直使用
2. **编译命令**：代码修改后需要执行
3. **运行命令**：每次启动程序都需要执行
4. **Windows系统**：classpath使用分号（`;`）分隔
5. **Linux/Mac系统**：classpath使用冒号（`:`）分隔

