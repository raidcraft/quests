package de.raidcraft.quests.actions;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.quests.QuestManager;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.objective.PlayerObjective;
import de.raidcraft.quests.api.quest.Quest;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author mdoering
 */
public class CompleteObjectiveAction implements Action<Player> {

    @Override
    @Information(
            value = "objective.complete",
            desc = "Manually completes the given objective.",
            conf = {
                    "quest: <id>",
                    "objective: <int:id>"
            }
    )
    public void accept(Player player, ConfigurationSection config) {

        QuestManager component = RaidCraft.getComponent(QuestManager.class);
        QuestHolder questHolder = component.getQuestHolder(player);
        if (questHolder == null) return;
        Optional<Quest> optionalQuest = questHolder.getQuest(config.getString("quest"));
        if (!optionalQuest.isPresent()) {
            String msg = questHolder.getPlayer().getName() + " does not have the quest: " + config.getString("quest");
            RaidCraft.LOGGER.warning(msg);
            player.sendMessage(ChatColor.RED + msg);
            return;
        }
        Quest quest = optionalQuest.get();
        if (quest.isCompleted()) return;
        Optional<PlayerObjective> objective = quest.getUncompletedObjectives().stream()
                .filter(obj -> obj.getObjectiveTemplate().getId() == config.getInt("objective"))
                .findFirst();
        if (objective.isPresent()) {
            objective.get().complete();
        } else {
            player.sendMessage(ChatColor.RED + "Invalid objective given for action in quest: " + quest.getFullName());
        }
    }
}
