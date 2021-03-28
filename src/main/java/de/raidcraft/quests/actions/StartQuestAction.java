package de.raidcraft.quests.actions;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.language.Translator;
import de.raidcraft.quests.QuestManager;
import de.raidcraft.quests.QuestPlugin;
import de.raidcraft.api.quests.QuestException;
import de.raidcraft.quests.api.quest.QuestTemplate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * @author mdoering
 */
public class StartQuestAction implements Action<Player> {

    @Override
    @Information(
            value = "quest.start",
            desc = "Starts the given quest. Will only work if it is valid to start the quest.",
            conf = {
                    "quest: <id>"
            }
    )
    public void accept(Player player, ConfigurationSection config) {

        RaidCraft.LOGGER.info("Start Quest Action was triggered for " + player.getName() + " : " + config.getString("quest"));
        if (!player.hasPermission("rcquests.quest.start")) {
            Translator.msg(QuestPlugin.class, player, "action.quest.start.no-permission", "Du hast nicht das Recht Quests zu starten!");
            return;
        }
        try {
            QuestManager component = RaidCraft.getComponent(QuestManager.class);
            QuestTemplate quest = component.getQuestTemplate(config.getString("quest"));
            if (quest == null) {
                player.sendMessage(ChatColor.RED + "Wrong config! Unknown quest given: " + config.getString("quest"));
                return;
            }
            component.getQuestHolder(player).startQuest(quest);
        } catch (QuestException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            player.sendMessage(ChatColor.RED + e.getMessage());
        }
    }
}
