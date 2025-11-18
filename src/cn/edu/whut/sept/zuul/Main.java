/**
 * 该类是"World-of-Zuul"应用程序的入口点。
 * 它包含main方法，用于启动游戏程序。
 * 
 * @author  Michael Kölling and David J. Barnes
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

public class Main {

    /**
     * 程序的主入口方法。
     * 创建Game对象并调用play方法开始游戏。
     * 
     * @param args 命令行参数（本程序中未使用）
     */
    public static void main(String[] args) {
        Game game = new Game();
        game.play();
    }
}
