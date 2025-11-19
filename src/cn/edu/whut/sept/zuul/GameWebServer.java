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
    private static final int PORT = 8080;
    private static final String WEB_ROOT = "web";
    private GameController gameController;
    private boolean running;
    
    /**
     * 创建Web服务器
     */
    public GameWebServer() {
        gameController = new GameController();
        running = false;
    }
    
    /**
     * 启动服务器
     */
    public void start() {
        running = true;
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("========================================");
            System.out.println("World of Zuul Web服务器已启动");
            System.out.println("访问地址: http://localhost:" + PORT);
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
        }
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
            
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                sendErrorResponse(out, 400, "Bad Request");
                clientSocket.close();
                return;
            }
            
            String method = parts[0];
            String path = parts[1];
            
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
        
        if (method.equals("OPTIONS")) {
            out.flush();
            return;
        }
        
        if (path.equals("/api/command") && method.equals("POST")) {
            // 解析JSON（简单解析）
            Map<String, String> request = JsonUtil.parseSimpleJson(requestBody);
            String command = request != null ? request.get("command") : null;
            
            if (command == null || command.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "缺少命令参数");
                out.println(JsonUtil.toJson(error));
            } else {
                Map<String, Object> response = gameController.executeCommand(command);
                out.println(JsonUtil.toJson(response));
            }
        } else if (path.equals("/api/status") && method.equals("GET")) {
            Map<String, Object> status = gameController.getGameStatus();
            out.println(JsonUtil.toJson(status));
        } else {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "未知的API端点");
            out.println(JsonUtil.toJson(error));
        }
        
        out.flush();
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
}

