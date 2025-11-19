/**
 * Save命令执行器。
 * 保存当前游戏状态到数据库。
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

public class SaveCommand implements CommandExecutor {
    /**
     * 执行save命令，保存游戏状态。
     * 
     * @param command 命令对象
     * @param game 游戏对象
     * @return 总是返回false（save命令不会退出游戏）
     */
    @Override
    public boolean execute(Command command, Game game) {
        Player player = game.getPlayer();
        
        if (player.getUserId() == null) {
            System.out.println("保存失败：请先登录！");
            return false;
        }
        
        GameStateManager stateManager = new GameStateManager(game);
        boolean success = stateManager.saveGameState();
        
        if (success) {
            System.out.println("游戏状态已保存！");
        } else {
            System.out.println("保存失败，请稍后重试。");
        }
        
        return false;
    }
}

