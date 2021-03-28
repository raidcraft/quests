package de.raidcraft.quests;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.chat.AutoCompletionProvider;
import de.raidcraft.api.quests.QuestException;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.quest.Quest;
import de.raidcraft.quests.api.quest.QuestTemplate;
import de.raidcraft.quests.util.QuestUtil;
import de.raidcraft.util.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Silthus
 */
public class QuestAutoCompletionProvider extends AutoCompletionProvider {

    public QuestAutoCompletionProvider() {

        super('?', "Du besitzt keine aktiven Quests die du mit ?Tab vervollständigen kannst.");
    }

    @Override
    protected List<String> getAutoCompleteList(Player player, @Nullable String message) {

        QuestHolder questHolder = RaidCraft.getComponent(QuestManager.class).getQuestHolder(player);
        return questHolder.getActiveQuests().stream()
                .filter(q -> message == null || q.getFriendlyName().toLowerCase().startsWith(message.toLowerCase()))
                .map(Quest::getFriendlyName)
                .collect(Collectors.toList());
    }

    @Override
    public FancyMessage autoComplete(Player player, FancyMessage fancyMessage, String item) {

        try {
            QuestManager questManager = RaidCraft.getComponent(QuestManager.class);
            QuestHolder questHolder = questManager.getQuestHolder(player);
            QuestTemplate questTemplate = questManager.getQuestTemplate(item);
            Optional<Quest> quest = questHolder.getQuest(questTemplate);
            if (quest.isPresent()) {
                return QuestUtil.getQuestTooltip(fancyMessage, quest.get());
            }
        } catch (QuestException e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
        }
        return fancyMessage;
    }
}
