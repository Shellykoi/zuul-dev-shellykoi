/**
 * Web版本游戏启动类
 * 启动HTTP服务器，提供Web前端访问
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

public class WebMain {
    /**
     * 主方法，启动Web服务器
     * 
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        GameWebServer server = new GameWebServer();
        server.start();
    }
}

