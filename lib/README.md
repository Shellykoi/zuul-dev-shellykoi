# MySQL JDBC 驱动说明

## 下载MySQL驱动

请将MySQL JDBC驱动JAR文件放在此目录中。

### 方法1：从官网下载（推荐）

1. 访问MySQL官方下载页面：
   https://dev.mysql.com/downloads/connector/j/

2. 选择：
   - **Platform**: Platform Independent
   - **版本**: 推荐 8.0.33 或更高版本

3. 下载文件：
   - 选择 "mysql-connector-j-8.0.33.zip" 或类似版本
   - 解压ZIP文件
   - 找到 `mysql-connector-j-8.0.33.jar` 文件

4. 将JAR文件复制到此目录（`lib/`）：
   ```
   lib/
     └── mysql-connector-j-8.0.33.jar
   ```

### 方法2：使用Maven下载（如果有Maven）

```bash
mvn dependency:copy -Dartifact=com.mysql:mysql-connector-j:8.0.33 -DoutputDirectory=lib
```

### 方法3：使用Gradle下载（如果有Gradle）

在 `build.gradle` 中添加：
```gradle
dependencies {
    implementation 'com.mysql:mysql-connector-j:8.0.33'
}
```

然后运行：
```bash
gradle copyDependencies
```

## 验证

下载完成后，确保 `lib` 目录中有类似以下名称的文件：
- `mysql-connector-j-8.0.33.jar` 或
- `mysql-connector-java-8.0.33.jar`

## 注意事项

- 文件名可能因版本而异，如果使用不同版本，请相应修改编译和运行命令中的JAR文件名
- 确保下载的是JAR文件，不是ZIP压缩包
- 如果使用Windows系统，classpath分隔符使用分号（`;`）而不是冒号（`:`）

