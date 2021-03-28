package de.raidcraft.quests.actions;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.language.Translator;
import de.raidcraft.quests.QuestManager;
import de.raidcraft.quests.QuestPlugin;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.quest.Quest;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author mdoering
 */
public class CompleteQuestAction implements Action<Player> {

    @Override
    @Information(
            value = "quest.complete",
            desc = "Manually completes the given quest. Can be executed even if not all objectives are completed.",
            conf = {
                    "quest: <id>"
            }
    )
    public void accept(Player player, ConfigurationSection config) {

        if (!player.hasPermission("rcquests.quest.complete")) {
            Translator.msg(QuestPlugin.class, player, "action.quest.complete.no-permission", "Du hast nicht das Recht Quests zu starten!");
            return;
        }
        QuestManager component = RaidCraft.getComponent(QuestManager.class);
        QuestHolder questHolder = component.getQuestHolder(player);
        if (questHolder == null) return;
        Optional<Quest> quest = questHolder.getQuest(config.getString("quest"));
        if (quest.isPresent()) {
            if (quest.get().isCompleted()) return;
            quest.get().complete();
        } else {
            String msg = questHolder.getPlayer().getName() + " does not have the quest: " + config.getString("quest");
            RaidCraft.LOGGER.warning(msg);
            player.sendMessage(ChatColor.RED + msg);
        }
    }
}
