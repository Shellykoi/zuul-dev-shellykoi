/**
 * 游戏Web服务器
 * 提供HTTP服务和REST API，支持前端访问
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class GameWebServer {
    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_PORT_ATTEMPTS = 100; // 最多尝试100个端口
    private static final String WEB_ROOT = "web";
    private GameController gameController;
    private boolean running;
    private int actualPort; // 实际使用的端口
    
    /**
     * 创建Web服务器
     */
    public GameWebServer() {
        gameController = new GameController();
        running = false;
        actualPort = DEFAULT_PORT;
    }
    
    /**
     * 启动服务器
     */
    public void start() {
        running = true;
        
        // 尝试从默认端口开始查找可用端口并直接创建 ServerSocket
        ServerSocket serverSocket = null;
        int port = DEFAULT_PORT;
        
        for (int attempt = 0; attempt < MAX_PORT_ATTEMPTS; attempt++) {
            try {
                serverSocket = new ServerSocket(port);
                // 成功创建，跳出循环
                break;
            } catch (IOException e) {
                // 端口被占用，尝试下一个端口
                port++;
                if (attempt == MAX_PORT_ATTEMPTS - 1) {
                    System.err.println("错误: 无法找到可用端口（已尝试 " + MAX_PORT_ATTEMPTS + " 个端口，从 " + DEFAULT_PORT + " 到 " + port + "）");
                    return;
                }
            }
        }
        
        if (serverSocket == null) {
            System.err.println("错误: 无法创建服务器套接字");
            return;
        }
        
        actualPort = port;
        
        // 如果使用的不是默认端口，提示用户
        if (port != DEFAULT_PORT) {
            System.out.println("提示: 端口 " + DEFAULT_PORT + " 已被占用，自动切换到端口 " + port);
        }
        
        try {
            System.out.println("========================================");
            System.out.println("World of Zuul Web服务器已启动");
            System.out.println("访问地址: http://localhost:" + port);
            System.out.println("========================================");
            System.out.println("按 Ctrl+C 停止服务器");
            System.out.println();
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleRequest(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("服务器启动失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 确保关闭 ServerSocket
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("关闭服务器套接字时出错: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 获取实际使用的端口号
     * @return 端口号
     */
    public int getPort() {
        return actualPort;
    }
    
    /**
     * 处理HTTP请求
     */
    private void handleRequest(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();
            PrintWriter out = new PrintWriter(
                new OutputStreamWriter(outputStream, "UTF-8"), true);
            
            // 读取请求行
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                clientSocket.close();
                return;
            }
            
            // 调试：输出原始请求行
            System.out.println("原始请求行: [" + requestLine + "]");
            
            String[] parts = requestLine.split("\\s+", 3); // 使用正则表达式分割，最多3部分
            if (parts.length < 2) {
                sendErrorResponse(out, 400, "Bad Request");
                clientSocket.close();
                return;
            }
            
            String method = parts[0].trim();
            String path = parts[1].trim();
            
            // 调试：输出解析后的方法和路径
            System.out.println("解析后 - 方法: [" + method + "], 路径: [" + path + "]");
            
            // 读取请求头
            Map<String, String> headers = new HashMap<>();
            String headerLine;
            int contentLength = 0;
            while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                int colonIndex = headerLine.indexOf(':');
                if (colonIndex > 0) {
                    String key = headerLine.substring(0, colonIndex).trim().toLowerCase();
                    String value = headerLine.substring(colonIndex + 1).trim();
                    headers.put(key, value);
                    if (key.equals("content-length")) {
                        try {
                            contentLength = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            // 忽略
                        }
                    }
                }
            }
            
            // 读取请求体（如果有）
            String requestBody = "";
            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                int read = in.read(bodyChars, 0, contentLength);
                if (read > 0) {
                    requestBody = new String(bodyChars, 0, read);
                }
            }
            
            // 处理API请求
            if (path.startsWith("/api/")) {
                handleApiRequest(method, path, requestBody, out);
            } else {
                // 处理静态文件请求
                handleStaticFile(path, out, outputStream);
            }
            
            in.close();
            out.close();
            clientSocket.close();
            
        } catch (IOException e) {
            System.err.println("处理请求时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理API请求
     */
    private void handleApiRequest(String method, String path, String requestBody,
                                   PrintWriter out) throws IOException {
        // 设置CORS头
        out.println("HTTP/1.1 200 OK");
        out.println("Access-Control-Allow-Origin: *");
        out.println("Access-Control-Allow-Methods: GET, POST, OPTIONS");
        out.println("Access-Control-Allow-Headers: Content-Type");
        out.println("Content-Type: application/json; charset=UTF-8");
        out.println();
        
        // 规范化HTTP方法（转大写并去除空格）
        String normalizedMethod = method != null ? method.toUpperCase().trim() : "";
        
        if (normalizedMethod.equals("OPTIONS")) {
            out.flush();
            return;
        }
        
        // 提取路径部分（去掉查询参数）
        String pathOnly = path;
        int queryIndex = path.indexOf('?');
        if (queryIndex != -1) {
            pathOnly = path.substring(0, queryIndex);
        }
        
        // 规范化路径：去除多余的斜杠，确保以/api/开头
        final String normalizedPath = normalizePath(pathOnly);
        
        // 调试日志：输出请求信息
        System.out.println("=== API请求 ===");
        System.out.println("原始方法: " + method);
        System.out.println("规范化方法: " + normalizedMethod);
        System.out.println("原始路径: " + path);
        System.out.println("去除查询参数后: " + pathOnly);
        System.out.println("规范化路径: " + normalizedPath);
        System.out.println("请求体: " + (requestBody != null && !requestBody.isEmpty() ? requestBody : "(空)"));
        System.out.println("请求体长度: " + (requestBody != null ? requestBody.length() : 0));
        
        // 详细路径匹配调试
        System.out.println("路径匹配检查:");
        System.out.println("  normalizedPath: [" + normalizedPath + "] (长度: " + normalizedPath.length() + ")");
        System.out.println("  normalizedPath.equals(\"/api/register\"): " + normalizedPath.equals("/api/register"));
        System.out.println("  normalizedPath.equals(\"/api/login\"): " + normalizedPath.equals("/api/login"));
        System.out.println("  normalizedPath.equals(\"/api/command\"): " + normalizedPath.equals("/api/command"));
        System.out.println("  normalizedPath.equals(\"/api/status\"): " + normalizedPath.equals("/api/status"));
        System.out.println("  normalizedPath.equals(\"/api/gamerecord\"): " + normalizedPath.equals("/api/gamerecord"));
        System.out.println("  normalizedPath.equals(\"/api/gamerecords\"): " + normalizedPath.equals("/api/gamerecords"));
        System.out.println("  normalizedPath.equals(\"/api/logout\"): " + normalizedPath.equals("/api/logout"));
        System.out.println("  normalizedMethod: [" + normalizedMethod + "]");
        System.out.println("  normalizedMethod.equals(\"POST\"): " + normalizedMethod.equals("POST"));
        System.out.println("  normalizedMethod.equals(\"GET\"): " + normalizedMethod.equals("GET"));
        
        try {
            if (normalizedPath.equals("/api/register") && normalizedMethod.equals("POST")) {
                // 注册新用户
                Map<String, String> request = JsonUtil.parseSimpleJson(requestBody);
                String username = request != null ? request.get("username") : null;
                String password = request != null ? request.get("password") : null;
                
                Map<String, Object> response = gameController.register(username, password);
                out.println(JsonUtil.toJson(response));
            } else if (normalizedPath.equals("/api/login") && normalizedMethod.equals("POST")) {
                // 用户登录
                System.out.println("处理登录请求...");
                Map<String, String> request = JsonUtil.parseSimpleJson(requestBody);
                String username = request != null ? request.get("username") : null;
                String password = request != null ? request.get("password") : null;
                
                System.out.println("解析后的用户名: " + (username != null ? username : "(null)"));
                System.out.println("解析后的密码长度: " + (password != null ? password.length() : 0));
                
                Map<String, Object> response = gameController.login(username, password);
                System.out.println("登录响应: success=" + response.get("success"));
                out.println(JsonUtil.toJson(response));
            } else if (normalizedPath.equals("/api/command") && normalizedMethod.equals("POST")) {
                // 执行游戏命令
                Map<String, String> request = JsonUtil.parseSimpleJson(requestBody);
                String command = request != null ? request.get("command") : null;
                String sessionId = request != null ? request.get("sessionId") : null;
                
                if (command == null || command.isEmpty()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "缺少命令参数");
                    out.println(JsonUtil.toJson(error));
                } else {
                    Map<String, Object> response;
                    if (sessionId != null && !sessionId.isEmpty()) {
                        response = gameController.executeCommand(command, sessionId);
                    } else {
                        response = gameController.executeCommand(command);
                    }
                    out.println(JsonUtil.toJson(response));
                }
            } else if (normalizedPath.equals("/api/status") && normalizedMethod.equals("GET")) {
                // 获取游戏状态
                String sessionId = getQueryParameter(path, "sessionId");
                Map<String, Object> status;
                if (sessionId != null && !sessionId.isEmpty()) {
                    status = gameController.getGameStatus(sessionId);
                } else {
                    status = gameController.getGameStatus();
                }
                out.println(JsonUtil.toJson(status));
            } else if (normalizedPath.equals("/api/save") && normalizedMethod.equals("POST")) {
                // 保存游戏状态
                Map<String, String> request = JsonUtil.parseSimpleJson(requestBody);
                String sessionId = request != null ? request.get("sessionId") : null;
                
                if (sessionId == null || sessionId.isEmpty()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "缺少会话ID");
                    out.println(JsonUtil.toJson(error));
                } else {
                    Map<String, Object> response = gameController.saveGame(sessionId);
                    out.println(JsonUtil.toJson(response));
                }
            } else if (normalizedPath.equals("/api/load") && normalizedMethod.equals("POST")) {
                // 加载游戏状态
                Map<String, String> request = JsonUtil.parseSimpleJson(requestBody);
                String sessionId = request != null ? request.get("sessionId") : null;
                
                if (sessionId == null || sessionId.isEmpty()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "缺少会话ID");
                    out.println(JsonUtil.toJson(error));
                } else {
                    Map<String, Object> response = gameController.loadGame(sessionId);
                    out.println(JsonUtil.toJson(response));
                }
            } else if (normalizedPath.equals("/api/gamerecord") && normalizedMethod.equals("GET")) {
                // 获取游戏记录
                System.out.println("✅ 匹配到 /api/gamerecord 端点");
                String sessionId = getQueryParameter(path, "sessionId");
                System.out.println("提取的 sessionId: " + (sessionId != null ? sessionId : "(null)"));
                if (sessionId == null || sessionId.isEmpty()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "缺少会话ID");
                    out.println(JsonUtil.toJson(error));
                } else {
                    Map<String, Object> response = gameController.getGameRecord(sessionId);
                    out.println(JsonUtil.toJson(response));
                }
            } else if (normalizedPath.equals("/api/gamerecords") && normalizedMethod.equals("GET")) {
                // 获取所有游戏记录
                String sessionId = getQueryParameter(path, "sessionId");
                if (sessionId == null || sessionId.isEmpty()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "缺少会话ID");
                    out.println(JsonUtil.toJson(error));
                } else {
                    Map<String, Object> response = gameController.getAllGameRecords(sessionId);
                    out.println(JsonUtil.toJson(response));
                }
            } else if (normalizedPath.equals("/api/logout") && normalizedMethod.equals("POST")) {
                // 退出登录
                Map<String, String> request = JsonUtil.parseSimpleJson(requestBody);
                String sessionId = request != null ? request.get("sessionId") : null;
                
                if (sessionId == null || sessionId.isEmpty()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "缺少会话ID");
                    out.println(JsonUtil.toJson(error));
                } else {
                    Map<String, Object> response = gameController.logout(sessionId);
                    out.println(JsonUtil.toJson(response));
                }
            } else {
                // 未匹配到任何路由，输出详细调试信息
                System.err.println("❌ 未匹配的API端点:");
                System.err.println("  方法: " + method);
                System.err.println("  规范化方法: " + normalizedMethod);
                System.err.println("  路径: " + path);
                System.err.println("  规范化路径: " + normalizedPath);
                System.err.println("  规范化路径长度: " + normalizedPath.length());
                System.err.println("  规范化路径字节: " + java.util.Arrays.toString(normalizedPath.getBytes()));
                System.err.println("  支持的端点: /api/register, /api/login, /api/command, /api/status, /api/save, /api/load, /api/gamerecord, /api/gamerecords, /api/logout");
                
                final String finalPath = path;
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "未知的API端点: " + normalizedPath + " (方法: " + method + ")");
                Map<String, String> debugInfo = new HashMap<>();
                debugInfo.put("originalPath", finalPath);
                debugInfo.put("normalizedPath", normalizedPath);
                debugInfo.put("method", method);
                error.put("debug", debugInfo);
                out.println(JsonUtil.toJson(error));
            }
        } catch (Exception e) {
            // 捕获所有异常，确保返回有效的JSON响应
            System.err.println("处理API请求时出错: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "服务器内部错误: " + e.getMessage());
            out.println(JsonUtil.toJson(error));
        }
        
        out.flush();
    }
    
    /**
     * 规范化路径：去除多余的斜杠，确保格式正确
     */
    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        
        String originalPath = path;
        
        // 去除首尾空格、制表符和其他空白字符
        path = path.trim();
        
        // 去除不可见字符（如零宽空格等）
        path = path.replaceAll("[\\u200B-\\u200D\\uFEFF]", "");
        
        // 移除查询参数（?之后的部分）和锚点（#之后的部分）
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }
        int fragmentIndex = path.indexOf('#');
        if (fragmentIndex >= 0) {
            path = path.substring(0, fragmentIndex);
        }
        
        // URL解码（只在路径包含编码字符时才解码）
        if (path.contains("%")) {
            try {
                String decodedPath = java.net.URLDecoder.decode(path, "UTF-8");
                // 只有当解码后的路径不同且不包含特殊字符时才使用解码后的路径
                if (!decodedPath.equals(path) && !decodedPath.contains("\0")) {
                    path = decodedPath;
                }
            } catch (Exception e) {
                // 如果解码失败，使用原始路径
                System.err.println("URL解码失败: " + e.getMessage());
            }
        }
        
        // 确保以/开头
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        // 去除多余的斜杠（将多个连续的斜杠替换为单个斜杠）
        path = path.replaceAll("/+", "/");
        
        // 如果路径以/结尾且不是根路径，去除末尾斜杠
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        
        // 调试输出
        if (!originalPath.equals(path)) {
            System.out.println("路径规范化: [" + originalPath + "] -> [" + path + "]");
        }
        
        return path;
    }
    
    /**
     * 处理静态文件请求
     */
    private void handleStaticFile(String path, PrintWriter out, OutputStream outputStream) throws IOException {
        // 默认首页
        if (path.equals("/") || path.equals("/index.html")) {
            path = "/index.html";
        }
        
        // 移除开头的斜杠
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        // 构建文件路径
        Path filePath = Paths.get(WEB_ROOT, path);
        
        // 安全检查：防止路径遍历攻击
        Path webRootPath = Paths.get(WEB_ROOT).toAbsolutePath().normalize();
        Path resolvedPath = filePath.toAbsolutePath().normalize();
        
        if (!resolvedPath.startsWith(webRootPath)) {
            sendErrorResponse(out, 403, "Forbidden");
            return;
        }
        
        // 检查文件是否存在
        if (!Files.exists(resolvedPath) || !Files.isRegularFile(resolvedPath)) {
            sendErrorResponse(out, 404, "Not Found");
            return;
        }
        
        // 确定Content-Type
        String contentType = getContentType(path);
        
        // 读取文件内容
        byte[] fileContent = Files.readAllBytes(resolvedPath);
        
        // 发送响应头
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: " + contentType);
        out.println("Content-Length: " + fileContent.length);
        out.println();
        out.flush();
        
        // 发送文件内容
        outputStream.write(fileContent);
        outputStream.flush();
    }
    
    /**
     * 根据文件扩展名确定Content-Type
     */
    private String getContentType(String path) {
        if (path.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        } else if (path.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        } else if (path.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        } else if (path.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        } else if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            return "application/octet-stream";
        }
    }
    
    /**
     * 从URL中获取查询参数
     */
    private String getQueryParameter(String path, String paramName) {
        if (path == null || paramName == null) {
            return null;
        }
        
        int queryIndex = path.indexOf('?');
        if (queryIndex == -1) {
            return null;
        }
        
        String query = path.substring(queryIndex + 1);
        String[] params = query.split("&");
        for (String param : params) {
            String[] parts = param.split("=", 2);
            if (parts.length == 2 && parts[0].equals(paramName)) {
                try {
                    // URL解码参数值
                    return java.net.URLDecoder.decode(parts[1], "UTF-8");
                } catch (Exception e) {
                    // 如果解码失败，返回原始值
                    return parts[1];
                }
            }
        }
        return null;
    }
    
    /**
     * 发送错误响应
     */
    private void sendErrorResponse(PrintWriter out, int statusCode, String statusMessage) {
        out.println("HTTP/1.1 " + statusCode + " " + statusMessage);
        out.println("Content-Type: text/html; charset=UTF-8");
        out.println();
        out.println("<html><body><h1>" + statusCode + " " + statusMessage + "</h1></body></html>");
        out.flush();
    }
    
    /**
     * 停止服务器
     */
    public void stop() {
        running = false;
    }
    
    /**
     * 主方法，用于直接启动服务器
     * 也可以使用 WebMain 类来启动
     */
    public static void main(String[] args) {
        GameWebServer server = new GameWebServer();
        server.start();
    }
}

