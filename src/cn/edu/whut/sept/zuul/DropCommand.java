/**
 * Drop命令执行器。
 * 玩家丢弃身上携带的物品。
 * 
 * @author 扩展功能实现
 * @version 2.0
 */
package cn.edu.whut.sept.zuul;

public class DropCommand implements CommandExecutor
{
    /**
     * 执行drop命令，丢弃物品。
     * 
     * @param command 命令对象
     * @param game 游戏对象
     * @return 总是返回false（drop命令不会退出游戏）
     */
    @Override
    public boolean execute(Command command, Game game)
    {
        if (!command.hasSecondWord()) {
            System.out.println("丢弃什么？");
            return false;
        }

        String itemName = command.getSecondWord();
        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();

        // 检查玩家是否携带该物品
        Item item = player.getItem(itemName);
        if (item == null) {
            System.out.println("你没有 " + itemName + "！");
            return false;
        }

        // 丢弃物品到房间
        player.dropItem(itemName);
        currentRoom.addItem(item);
        System.out.println("你丢弃了 " + itemName + "。");
        return false;
    }
}

