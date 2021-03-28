package de.raidcraft.quests.actions;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.quests.QuestManager;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.util.ConfigUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author mdoering
 */
public class AddQuestItemAction implements Action<Player> {

    @Override
    public void accept(Player player, ConfigurationSection config) {

        try {
            QuestHolder questHolder = RaidCraft.getComponent(QuestManager.class).getQuestHolder(player);
            String itemString = config.getString("item");
            ItemStack item = RaidCraft.getSafeItem(itemString);
            if (item == null) {
                RaidCraft.LOGGER.warning("Invalid item id in " + ConfigUtil.getFileName(config) + " for " + itemString);
                return;
            }
            int amount = config.getInt("amount", 1);
            while (amount > item.getMaxStackSize()) {
                questHolder.getQuestInventory().addItem(RaidCraft.getSafeItem(itemString, item.getMaxStackSize()));
                amount -= item.getMaxStackSize();
            }
            questHolder.getQuestInventory().addItem(item);
        } catch (CustomItemException e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
            e.printStackTrace();
        }
    }
}
