/**
 * 该类负责从终端读取用户输入并解析为命令对象。
 * 解析器读取用户输入的一行文本，将其分解为单词，并检查第一个单词是否为有效命令。
 * 如果有效，则创建一个Command对象；如果无效，则创建一个未知命令。
 * 
 * @author  Michael Kölling and David J. Barnes
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

import java.util.Scanner;

public class Parser
{
    /**
     * 用于验证命令词的对象。
     */
    private CommandWords commands;
    
    /**
     * 用于读取用户输入的扫描器。
     */
    private Scanner reader;

    /**
     * 创建解析器对象，初始化命令词汇表和输入扫描器。
     */
    public Parser()
    {
        commands = new CommandWords();
        reader = new Scanner(System.in);
    }

    /**
     * 从终端读取一行用户输入，解析为命令对象并返回。
     * 输入行被分解为最多两个单词：第一个单词作为命令词，第二个单词作为命令参数。
     * 如果第一个单词不是有效命令，则返回一个未知命令。
     * 特殊处理"eat cookie"命令，将"cookie"作为第二个词。
     * 
     * @return 解析后的Command对象，包含命令词和可选的第二个词
     */
    public Command getCommand()
    {
        String inputLine;
        String word1 = null;
        String word2 = null;

        System.out.print("> ");

        inputLine = reader.nextLine().trim();

        Scanner tokenizer = new Scanner(inputLine);
        if(tokenizer.hasNext()) {
            word1 = tokenizer.next().toLowerCase();   // 读取第一个单词并转为小写
            
            // 处理"eat cookie"这样的多词命令
            if (word1.equals("eat")) {
                // 读取剩余部分作为第二个词
                if (tokenizer.hasNext()) {
                    word2 = tokenizer.nextLine().trim().toLowerCase();
                }
            } else {
                // 其他命令只读取第二个单词
                if (tokenizer.hasNext()) {
                    word2 = tokenizer.next().toLowerCase();
                }
            }
        }

        // 检查第一个单词是否为有效命令
        if(commands.isCommand(word1)) {
            return new Command(word1, word2);
        }
        else {
            return new Command(null, word2);
        }
    }

    /**
     * 在控制台打印所有有效命令的列表。
     */
    public void showCommands()
    {
        commands.showAll();
    }
}
