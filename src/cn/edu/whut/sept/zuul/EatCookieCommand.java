/**
 * EatCookie命令执行器。
 * 玩家吃掉魔法饼干以增加负重能力。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

public class EatCookieCommand implements CommandExecutor
{
    /**
     * 执行eat cookie命令，吃掉魔法饼干。
     * 
     * @param command 命令对象
     * @param game 游戏对象
     * @return 总是返回false（eat cookie命令不会退出游戏）
     */
    @Override
    public boolean execute(Command command, Game game)
    {
        Player player = game.getPlayer();
        
        // 检查命令参数是否为"cookie"
        if (!command.hasSecondWord() || !command.getSecondWord().equals("cookie")) {
            System.out.println("吃什么？（试试 'eat cookie'）");
            return false;
        }

        // 检查玩家是否携带魔法饼干
        Item cookie = player.getItem("cookie");
        if (cookie == null) {
            System.out.println("你没有魔法饼干！");
            System.out.println("在房间里找找魔法饼干，它可能藏在某个地方...");
            return false;
        }

        // 吃掉饼干，增加负重
        player.dropItem("cookie");
        double weightIncrease = 5.0;  // 增加5kg负重
        player.increaseMaxWeight(weightIncrease);
        System.out.println("你吃掉了魔法饼干！");
        System.out.println("你的最大负重增加了 " + weightIncrease + "kg！");
        System.out.println("新的最大负重: " + String.format("%.2f", player.getMaxWeight()) + "kg");
        return false;
    }
}

