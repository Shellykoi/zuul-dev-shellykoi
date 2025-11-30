/**
 * 该类用于验证和存储游戏中所有有效的命令词。
 * 它维护一个有效命令的列表，并提供检查字符串是否为有效命令的方法。
 * 
 * @author  Michael Kölling and David J. Barnes
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

public class CommandWords
{
    /**
     * 游戏中所有有效命令的数组。
     */
    private static final String[] validCommands = {
            "go", "quit", "help", "look", "back", "take", "drop", "items", "eat", "status", "use", "save", "load"
    };

    /**
     * 创建CommandWords对象并初始化有效命令列表。
     */
    public CommandWords()
    {
        // nothing to do at the moment...
    }

    /**
     * 检查给定的字符串是否为有效的命令词。
     * 
     * @param aString 待检查的字符串
     * @return 如果字符串是有效命令返回true，否则返回false
     */
    public boolean isCommand(String aString)
    {
        if (aString == null) {
            return false;
        }
        for(int i = 0; i < validCommands.length; i++) {
            if(validCommands[i].equals(aString))
                return true;
        }
        return false;
    }

    /**
     * 打印所有有效命令的列表到控制台。
     * 命令之间用空格分隔。
     */
    public void showAll()
    {
        for(String command: validCommands) {
            System.out.print(command + "  ");
        }
        System.out.println();
    }
}
