package de.raidcraft.quests.util;

import com.google.common.base.Strings;
import de.raidcraft.api.conversations.conversation.DefaultConversation;
import de.raidcraft.quests.api.objective.PlayerObjective;
import de.raidcraft.quests.api.objective.PlayerTask;
import de.raidcraft.quests.api.quest.Quest;
import de.raidcraft.util.fanciful.FancyMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * @author Silthus
 */
public class QuestUtil {

    /**
     * Creates a new quest tooltip and appends it to the given message.
     *
     * @param msg to append tooltip to
     * @param quest to create tooltip from
     * @return quest tooltip with name
     */
    public static FancyMessage getQuestTooltip(FancyMessage msg, Quest quest) {

        return msg.append(getQuestTag(quest)).then().style(org.bukkit.ChatColor.RESET);
    }

    /**
     * Gets a quest tooltip not wrapped in another text. Just the tooltip.
     *
     * @param quest to get tooltip for
     * @return quest tooltip
     */
    public static FancyMessage getQuestTooltip(Quest quest) {
        FancyMessage tooltip = new FancyMessage();

        tooltip.then(quest.getTemplate().getFriendlyName()).color(ChatColor.GOLD)
                .newLine();

        if (!Objects.isNull(quest.getDescription())) {
            tooltip.then(quest.getDescription()).style(ChatColor.ITALIC).color(ChatColor.GOLD).newLine();
        }

        tooltip.newLine();

        for (PlayerObjective objective : quest.getObjectives()) {
            if (objective.isHidden()) continue;
            tooltip.then("  * ").color(ChatColor.GOLD).style(ChatColor.RESET)
                    .then(objective.getObjectiveTemplate().getFriendlyName())
                    .style(objective.isCompleted() ? ChatColor.STRIKETHROUGH : ChatColor.RESET)
                    .color(objective.isActive() ? ChatColor.AQUA : ChatColor.GRAY)
                    .newLine();
        }

        return tooltip;
    }

    public static FancyMessage getQuestTag(Quest quest) {
        return new FancyMessage("[").color(org.bukkit.ChatColor.DARK_GRAY)
                .text(quest.getFriendlyName()).color(org.bukkit.ChatColor.GREEN)
                .formattedTooltip(QuestUtil.getQuestTooltip(quest))
                .text("]").color(org.bukkit.ChatColor.DARK_GRAY).then().style(org.bukkit.ChatColor.RESET);
    }

    public static FancyMessage getQuestObjectiveTag(PlayerObjective objective) {

        FancyMessage message = new FancyMessage(objective.getObjectiveTemplate().getFriendlyName());
        if (objective.isCompleted()) {
            message.color(org.bukkit.ChatColor.GRAY).style(org.bukkit.ChatColor.STRIKETHROUGH);
        } else if (objective.isActive()) {
            message.color(org.bukkit.ChatColor.AQUA).style(org.bukkit.ChatColor.UNDERLINE);
        } else {
            message.color(org.bukkit.ChatColor.AQUA);
        }

        String description = objective.getObjectiveTemplate().getDescription();
        if (!Strings.isNullOrEmpty(description)) {
            message.formattedTooltip(new FancyMessage(description).color(org.bukkit.ChatColor.GRAY));
        }

        return message.then().style(org.bukkit.ChatColor.RESET);
    }

    public static FancyMessage getQuestTaskTag(PlayerTask task) {

        FancyMessage message = new FancyMessage(task.getTaskTemplate().getFriendlyName());
        if (task.isCompleted()) {
            message.color(ChatColor.GRAY).style(org.bukkit.ChatColor.STRIKETHROUGH);
        } else if (task.isActive()) {
            message.color(ChatColor.BLUE).style(org.bukkit.ChatColor.UNDERLINE);
        } else {
            message.color(ChatColor.BLUE);
        }

        String description = task.getTaskTemplate().getDescription();
        if (!Strings.isNullOrEmpty(description)) {
            message.formattedTooltip(new FancyMessage(description).color(org.bukkit.ChatColor.GRAY));
        }

        return message.then().style(org.bukkit.ChatColor.RESET);
    }

    public static Map<Quest.Phase, Collection<DefaultConversation>> loadDefaultConversations(ConfigurationSection data) {
        HashMap<Quest.Phase, Collection<DefaultConversation>> conversations = new HashMap<>();
        Arrays.stream(Quest.Phase.values()).forEach(phase -> conversations.put(phase, new ArrayList<>()));
        if (data == null) return conversations;

        for (Quest.Phase phase : Quest.Phase.values()) {
            conversations.get(phase).addAll(DefaultConversation.fromConfig(data.getStringList(phase.getConfigName())));
        }

        return conversations;
    }

    public static Map<Quest.Phase, Boolean> loadDefaultConversationsClearingMap(ConfigurationSection section) {
        HashMap<Quest.Phase, Boolean> clearingMap = new HashMap<>();
        Arrays.stream(Quest.Phase.values()).forEach(phase -> clearingMap.put(phase, true));
        if (section == null) return clearingMap;

        for (Quest.Phase phase : Quest.Phase.values()) {
            ConfigurationSection phaseSection = section.getConfigurationSection(phase.getConfigName() + ".convs");
            if (phaseSection != null) {
                clearingMap.put(phase, phaseSection.getBoolean("clear", true));
            }
        }

        return clearingMap;
    }
}
