package de.raidcraft.quests.requirements;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.quests.QuestManager;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.quest.Quest;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Optional;

@Data
public class QuestCompletedRequirement implements Requirement<Player> {

    private final QuestManager questManager;

    @Information(
            value = "quest.completed",
            aliases = {"quest.complete"},
            desc = "Checks if the given quest was completed.",
            conf = {
                    "quest: <id>"
            }
    )
    @Override
    public boolean test(Player player, ConfigurationSection config) {
        QuestHolder holder = getQuestManager().getQuestHolder(player);
        if (holder == null) return false;
        Optional<Quest> quest = holder.getQuest(config.getString("quest"));
        if (!quest.isPresent()) {
            RaidCraft.LOGGER.warning("Could not check quest.completed requirement. Quest " + config.getString("quest") + " not found!");
        }
        return quest.map(activeQuest -> activeQuest.isActive() || activeQuest.isCompleted()).orElse(false);
    }
}
