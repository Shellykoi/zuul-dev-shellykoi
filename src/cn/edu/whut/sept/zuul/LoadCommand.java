/**
 * Load命令执行器。
 * 从数据库加载上次保存的游戏状态。
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

public class LoadCommand implements CommandExecutor {
    /**
     * 执行load命令，加载游戏状态。
     * 
     * @param command 命令对象
     * @param game 游戏对象
     * @return 总是返回false（load命令不会退出游戏）
     */
    @Override
    public boolean execute(Command command, Game game) {
        Player player = game.getPlayer();
        
        if (player.getUserId() == null) {
            System.out.println("加载失败：请先登录！");
            return false;
        }
        
        GameStateManager stateManager = new GameStateManager(game);
        boolean success = stateManager.loadGameState();
        
        if (success) {
            System.out.println("游戏状态已加载！");
            System.out.println(player.getCurrentRoom().getLongDescription());
        } else {
            System.out.println("没有找到保存的游戏状态，或加载失败。");
        }
        
        return false;
    }
}

