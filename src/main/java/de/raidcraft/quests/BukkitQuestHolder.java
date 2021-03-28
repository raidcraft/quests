package de.raidcraft.quests;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.quests.QuestException;
import de.raidcraft.quests.api.holder.AbstractQuestHolder;
import de.raidcraft.quests.api.quest.Quest;
import de.raidcraft.quests.api.quest.QuestTemplate;

import java.util.List;
import java.util.UUID;

/**
 * @author Silthus
 */
public class BukkitQuestHolder extends AbstractQuestHolder {

    public BukkitQuestHolder(int id, UUID playerId) {

        super(id, playerId);
        loadExistingQuests();
    }

    private void loadExistingQuests() {

        QuestManager component = RaidCraft.getComponent(QuestManager.class);
        component.getAllQuests(this).stream()
                .filter(Quest::isActive)
                .forEach(quest -> {
                    quest.updateObjectiveListeners();
                    addQuest(quest);
                });
    }

    @Override
    public List<Quest> getAllQuests() {

        QuestManager component = RaidCraft.getComponent(QuestManager.class);
        return component.getAllQuests(this);
    }

    @Override
    public Quest startQuest(QuestTemplate template) throws QuestException {

        super.startQuest(template);
        Quest quest = RaidCraft.getComponent(QuestManager.class).createQuest(this, template);
        if (!quest.isActive() && !quest.isCompleted()) {
            quest.start();
        }
        addQuest(quest);
        quest.updateObjectiveListeners();
        return quest;
    }

    @Override
    public void save() {

        // also save all quests the player has
        getAllQuests().forEach(Quest::save);
        getQuestInventory().save();
    }
}
