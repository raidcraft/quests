package de.raidcraft.quests.requirements;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.quests.QuestManager;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.quest.Quest;
import de.raidcraft.util.ConfigUtil;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Optional;

@Data
public class QuestActiveRequirement implements Requirement<Player> {

    private final QuestManager questManager;

    @Information(
            value = "quest.active",
            desc = "Checks if the given quest is currently active.",
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
            RaidCraft.LOGGER.warning("Invalid quest " + config.getString("quest") + " in " + ConfigUtil.getFileName(config));
            return false;
        }
        return quest.get().isActive();
    }
}
