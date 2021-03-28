package de.raidcraft.quests.actions;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.language.Translator;
import de.raidcraft.api.quests.QuestException;
import de.raidcraft.quests.QuestManager;
import de.raidcraft.quests.QuestPlugin;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.quest.Quest;
import de.raidcraft.quests.api.quest.QuestTemplate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author mdoering
 */
public class AbortQuestAction implements Action<Player> {

    @Override
    @Information(
            value = "quest.abort",
            desc = "Aborts the given quest. Will only work if it is valid to abort the quest.",
            conf = {
                    "quest: <id>"
            }
    )
    public void accept(Player player, ConfigurationSection config) {

        RaidCraft.LOGGER.info("Abort Quest Action was triggered for " + player.getName() + " : " + config.getString("quest"));
        if (!player.hasPermission("rcquests.quest.abort")) {
            Translator.msg(QuestPlugin.class, player, "action.quest.start.no-permission", "Du hast nicht das Recht Quests zu starten!");
            return;
        }
        try {
            QuestManager component = RaidCraft.getComponent(QuestManager.class);
            QuestTemplate questTemplate = component.getQuestTemplate(config.getString("quest"));
            if (questTemplate == null) {
                player.sendMessage(ChatColor.RED + "Wrong config! Unknown quest given: " + config.getString("quest"));
                return;
            }
            QuestHolder questHolder = component.getQuestHolder(player);
            if (questHolder == null) return;
            Optional<Quest> quest = questHolder.getQuest(questTemplate);
            if (!quest.isPresent()) {
                player.sendMessage(ChatColor.RED + "Quest is not active and cannot be aborted!");
                return;
            }
            quest.get().abort();
            quest.get().delete();
            component.clearCache(player);
        } catch (QuestException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            player.sendMessage(ChatColor.RED + e.getMessage());
        }
    }
}
