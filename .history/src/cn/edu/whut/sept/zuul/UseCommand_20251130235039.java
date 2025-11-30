/**
 * Use命令执行器类。
 * 
 * <p>实现物品使用功能，允许玩家使用背包中的物品。支持多种物品类型的使用：
 * <ul>
 *   <li><b>钥匙(KEY)</b>：解锁上锁的房间</li>
 *   <li><b>地图(MAP)</b>：显示当前房间的详细信息和所有出口</li>
 *   <li><b>食物(FOOD)</b>：恢复体力或增加负重能力</li>
 *   <li><b>工具(TOOL)</b>：执行特殊操作或解决谜题</li>
 * </ul>
 * 
 * <p>使用方式：
 * <pre>
 *   use key    // 使用钥匙解锁房间
 *   use map    // 使用地图查看信息
 * </pre>
 * 
 * <p>该类采用命令模式，实现了CommandExecutor接口，将物品使用逻辑封装在独立的类中，
 * 提高了代码的可维护性和扩展性。
 * 
 * @author 扩展功能实现
 * @version 2.0
 * @see CommandExecutor
 * @see Item
 * @see LockedRoom
 */
package cn.edu.whut.sept.zuul;

public class UseCommand implements CommandExecutor
{
    /**
     * 执行use命令，使用玩家背包中的物品。
     * 
     * <p>执行流程：
     * <ol>
     *   <li>检查命令参数是否包含物品名称</li>
     *   <li>从玩家背包中查找物品</li>
     *   <li>验证物品是否存在且可以使用</li>
     *   <li>根据物品类型执行相应的使用效果</li>
     *   <li>返回使用结果信息</li>
     * </ol>
     * 
     * @param command 命令对象，包含命令词和第二个参数（物品名称）
     * @param game 游戏对象，用于访问游戏状态和房间信息
     * @return 总是返回false（use命令不会退出游戏）
     */
    @Override
    public boolean execute(Command command, Game game)
    {
        if (!command.hasSecondWord()) {
            System.out.println("使用什么物品？");
            return false;
        }

        String itemName = command.getSecondWord();
        Player player = game.getPlayer();
        Room currentRoom = player.getCurrentRoom();

        // 从玩家背包中获取物品
        Item item = player.getItem(itemName);
        if (item == null) {
            System.out.println("你没有 " + itemName + "！");
            return false;
        }

        // 检查物品是否可以使用
        if (!item.isUsable()) {
            System.out.println(itemName + " 无法使用。");
            return false;
        }

        // 根据物品类型执行不同的使用效果
        String itemType = item.getItemType();
        String result = "";

        switch (itemType.toUpperCase()) {
            case "KEY":
                result = useKey(item, currentRoom, game);
                break;
            case "MAP":
                result = useMap(item, currentRoom);
                break;
            case "FOOD":
                result = useFood(item, player);
                break;
            case "TOOL":
                result = useTool(item, currentRoom);
                break;
            default:
                result = item.getUseEffect();
                if (result == null || result.isEmpty()) {
                    result = "你使用了 " + itemName + "，但似乎没有什么效果。";
                }
        }

        System.out.println(result);
        return false;
    }
    
    /**
     * 使用钥匙解锁房间。
     * 
     * <p>解锁逻辑：
     * <ol>
     *   <li>首先检查当前房间是否为上锁房间，如果是则尝试解锁</li>
     *   <li>如果当前房间已解锁，返回提示信息</li>
     *   <li>如果当前房间不是上锁房间，检查相邻房间是否有上锁的房间</li>
     *   <li>找到匹配的锁后，使用钥匙解锁并返回成功信息</li>
     * </ol>
     * 
     * <p>钥匙类型必须与房间所需的钥匙类型匹配（不区分大小写）。
     * 
     * @param key 钥匙物品对象，包含钥匙名称和类型信息
     * @param currentRoom 玩家当前所在的房间对象
     * @param game 游戏对象，用于访问游戏状态（当前版本未使用，保留用于未来扩展）
     * @return 使用结果描述字符串，包含解锁成功或失败的信息
     */
    private String useKey(Item key, Room currentRoom, Game game)
    {
        // 检查当前房间是否是上锁房间
        if (currentRoom instanceof LockedRoom) {
            LockedRoom lockedRoom = (LockedRoom) currentRoom;
            // 如果房间已解锁，直接返回提示信息
            if (lockedRoom.isUnlocked()) {
                return "这个房间已经解锁了。";
            }
            
            // 尝试使用钥匙解锁当前房间
            if (lockedRoom.unlock(key.getName())) {
                return "你使用 " + key.getName() + " 成功解锁了房间！\n" +
                       currentRoom.getLongDescription();
            } else {
                // 钥匙类型不匹配，返回错误信息
                return "这把钥匙无法解锁这个房间。需要 " + 
                       lockedRoom.getRequiredKeyType() + " 类型的钥匙。";
            }
        }
        
        // 检查相邻房间是否有上锁的房间（支持远程解锁）
        String[] directions = {"north", "south", "east", "west"};
        for (String direction : directions) {
            Room exitRoom = currentRoom.getExit(direction);
            if (exitRoom instanceof LockedRoom) {
                LockedRoom lockedRoom = (LockedRoom) exitRoom;
                // 检查房间是否已解锁且钥匙类型是否匹配
                if (!lockedRoom.isUnlocked() && 
                    lockedRoom.getRequiredKeyType().equalsIgnoreCase(key.getName())) {
                    lockedRoom.unlock(key.getName());
                    return "你使用 " + key.getName() + " 解锁了 " + 
                           translateDirection(direction) + " 方向的房间！";
                }
            }
        }
        
        // 没有找到需要解锁的房间
        return "你使用了 " + key.getName() + "，但这里没有需要解锁的房间。";
    }
    
    /**
     * 使用地图显示当前位置的详细信息。
     * 
     * <p>地图功能：
     * <ul>
     *   <li>显示当前房间的完整描述信息</li>
     *   <li>列出所有可用的出口方向及对应房间</li>
     *   <li>帮助玩家了解周围环境</li>
     * </ul>
     * 
     * <p>输出格式：
     * <pre>
     *   你打开了地图，查看当前位置的详细信息：
     *   [房间详细描述]
     *   
     *   地图显示：
     *   - 当前房间：[房间名称]
     *   - 出口信息：
     *     北 -> [目标房间名称]
     *     南 -> [目标房间名称]
     *   ...
     * </pre>
     * 
     * @param map 地图物品对象（当前版本未直接使用，保留用于未来扩展）
     * @param currentRoom 玩家当前所在的房间对象
     * @return 格式化的地图信息字符串，包含房间描述和出口信息
     */
    private String useMap(Item map, Room currentRoom)
    {
        StringBuilder result = new StringBuilder();
        // 添加地图使用提示和房间详细描述
        result.append("你打开了地图，查看当前位置的详细信息：\n");
        result.append(currentRoom.getLongDescription());
        result.append("\n\n地图显示：");
        result.append("\n- 当前房间：").append(currentRoom.getShortDescription());
        
        // 遍历所有方向，收集并显示出口信息
        String[] directions = {"north", "south", "east", "west"};
        boolean hasExits = false;
        for (String direction : directions) {
            Room exitRoom = currentRoom.getExit(direction);
            if (exitRoom != null) {
                // 第一次找到出口时，添加"出口信息："标题
                if (!hasExits) {
                    result.append("\n- 出口信息：");
                    hasExits = true;
                }
                // 添加出口方向（中文）和目标房间名称
                result.append("\n  ").append(translateDirection(direction))
                      .append(" -> ").append(exitRoom.getShortDescription());
            }
        }
        
        // 如果没有找到任何出口，提示这是一个封闭的房间
        if (!hasExits) {
            result.append("\n- 这是一个封闭的房间，没有出口。");
        }
        
        return result.toString();
    }
    
    /**
     * 使用食物恢复体力或增加负重。
     * 
     * @param food 食物物品
     * @param player 玩家对象
     * @return 使用结果描述
     */
    private String useFood(Item food, Player player)
    {
        // 食物使用后从背包移除
        player.dropItem(food.getName());
        
        // 根据食物类型给予不同效果
        String foodName = food.getName().toLowerCase();
        if (foodName.contains("cookie") || foodName.contains("饼干")) {
            // 魔法饼干已经在eat命令中处理，这里处理普通食物
            player.increaseMaxWeight(2.0);
            return "你吃掉了 " + food.getName() + "，感觉体力恢复了！\n" +
                   "你的最大负重增加了2kg！当前最大负重: " + 
                   String.format("%.1f", player.getMaxWeight()) + "kg";
        } else {
            return "你吃掉了 " + food.getName() + "，感觉体力恢复了！";
        }
    }
    
    /**
     * 使用工具解决谜题或打开宝箱。
     * 
     * @param tool 工具物品
     * @param currentRoom 当前房间
     * @return 使用结果描述
     */
    private String useTool(Item tool, Room currentRoom)
    {
        // 工具使用效果（可以根据具体工具类型扩展）
        return "你使用了 " + tool.getName() + "。\n" + tool.getUseEffect();
    }
    
    /**
     * 将方向翻译为中文。
     * 
     * @param direction 方向（英文）
     * @return 方向（中文）
     */
    private String translateDirection(String direction)
    {
        switch (direction.toLowerCase()) {
            case "north": return "北";
            case "south": return "南";
            case "east": return "东";
            case "west": return "西";
            default: return direction;
        }
    }
}

