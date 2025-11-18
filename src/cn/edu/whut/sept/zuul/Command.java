/**
 * 该类表示用户在游戏中输入的命令。
 * 命令对象包含一个命令词（如"go"、"quit"等）和可选的第二个词（如方向"north"等）。
 * 
 * @author  Michael Kölling and David J. Barnes
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

public class Command
{
    private String commandWord;
    private String secondWord;

    /**
     * 创建一个命令对象。第一个和第二个命令词必须提供，但如果命令无效，第一个词可以为null。
     * 
     * @param firstWord 命令的第一个词，如果命令无效则为null
     * @param secondWord 命令的第二个词，如果命令没有第二个词则为null
     */
    public Command(String firstWord, String secondWord)
    {
        commandWord = firstWord;
        this.secondWord = secondWord;
    }

    /**
     * 返回命令的第一个词（命令词）。
     * 
     * @return 命令的第一个词，如果命令无效则返回null
     */
    public String getCommandWord()
    {
        return commandWord;
    }

    /**
     * 返回命令的第二个词。
     * 
     * @return 命令的第二个词，如果命令没有第二个词则返回null
     */
    public String getSecondWord()
    {
        return secondWord;
    }

    /**
     * 检查命令是否有效（即命令词不为null）。
     * 
     * @return 如果命令无效（命令词为null）返回true，否则返回false
     */
    public boolean isUnknown()
    {
        return (commandWord == null);
    }

    /**
     * 检查命令是否有第二个词。
     * 
     * @return 如果命令有第二个词返回true，否则返回false
     */
    public boolean hasSecondWord()
    {
        return (secondWord != null);
    }
}
