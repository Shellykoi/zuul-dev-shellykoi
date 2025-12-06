/**
 * 简单的日志工具类。
 * 提供可选的调试日志功能，便于开发时追踪程序执行流程。
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

public class Logger
{
    /**
     * 是否启用调试日志。
     * 默认关闭，不影响正常游戏体验。
     */
    private static boolean debugEnabled = false;
    
    /**
     * 启用调试日志。
     */
    public static void enableDebug()
    {
        debugEnabled = true;
    }
    
    /**
     * 禁用调试日志。
     */
    public static void disableDebug()
    {
        debugEnabled = false;
    }
    
    /**
     * 输出调试日志。
     * 仅在调试模式启用时输出。
     * 
     * @param message 日志消息
     */
    public static void debug(String message)
    {
        if (debugEnabled) {
            System.out.println("[DEBUG] " + message);
        }
    }
    
    /**
     * 输出信息日志（始终输出）。
     * 
     * @param message 日志消息
     */
    public static void info(String message)
    {
        System.out.println("[INFO] " + message);
    }
}

