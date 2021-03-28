package de.raidcraft.quests;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.RevertableAction;
import de.raidcraft.quests.api.events.QuestPoolQuestAbortedEvent;
import de.raidcraft.quests.api.events.QuestPoolQuestCompletedEvent;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.objective.ObjectiveTemplate;
import de.raidcraft.quests.api.objective.PlayerObjective;
import de.raidcraft.quests.api.quest.AbstractQuest;
import de.raidcraft.quests.api.quest.QuestTemplate;
import de.raidcraft.quests.tables.TPlayerObjective;
import de.raidcraft.quests.tables.TPlayerQuest;
import de.raidcraft.quests.tables.TPlayerQuestPool;
import io.ebean.EbeanServer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Silthus
 */
public class SimpleQuest extends AbstractQuest {

    protected SimpleQuest(TPlayerQuest quest, QuestTemplate template, QuestHolder holder) {

        super(quest.getId(), template, holder);

        setPhase(quest.getPhase());
        setStartTime(quest.getStartTime());
        setCompletionTime(quest.getCompletionTime());
        setAbortionTime(quest.getAbortionTime());
    }

    @Override
    protected List<PlayerObjective> loadObjectives() {

        List<PlayerObjective> objectives = new ArrayList<>();
        EbeanServer database = RaidCraft.getDatabase(QuestPlugin.class);
        for (ObjectiveTemplate objectiveTemplate : getTemplate().getObjectiveTemplates()) {
            TPlayerObjective entry = database.find(TPlayerObjective.class).where()
                    .eq("quest_id", getId())
                    .eq("objective_id", objectiveTemplate.getId()).findOne();
            // create a new db entry if none exists
            if (entry == null) {
                entry = new TPlayerObjective();
                entry.setObjectiveId(objectiveTemplate.getId());
                entry.setQuest(database.find(TPlayerQuest.class, getId()));
                database.save(entry);
            }
            objectives.add(new SimplePlayerObjective(entry, this, objectiveTemplate));
        }
        return objectives;
    }

    @Override
    public boolean complete() {

        boolean complete = super.complete();
        if (complete) {
            EbeanServer database = RaidCraft.getDatabase(QuestPlugin.class);
            TPlayerQuest quest = database.find(TPlayerQuest.class, getId());
            if (quest != null && quest.getQuestPool() != null) {
                TPlayerQuestPool questPool = quest.getQuestPool();
                questPool.setLastCompletion(quest.getCompletionTime());
                questPool.setSuccessiveQuestCounter(questPool.getSuccessiveQuestCounter() + 1);
                database.update(questPool);
                RaidCraft.callEvent(new QuestPoolQuestCompletedEvent(this, questPool));
                return true;
            }
        }
        return complete;
    }

    @Override
    public boolean abort() {

        boolean abort = super.abort();
        if (abort) {
            EbeanServer database = RaidCraft.getDatabase(QuestPlugin.class);
            TPlayerQuest quest = database.find(TPlayerQuest.class, getId());
            if (quest != null && quest.getQuestPool() != null) {
                TPlayerQuestPool questPool = quest.getQuestPool();
                questPool.setSuccessiveQuestCounter(0);
                database.update(questPool);
                RaidCraft.callEvent(new QuestPoolQuestAbortedEvent(this, questPool));
                return true;
            }
        }
        return abort;
    }

    @Override
    public void delete() {

        EbeanServer database = RaidCraft.getDatabase(QuestPlugin.class);
        TPlayerQuest quest = database.find(TPlayerQuest.class, getId());
        if (quest != null) {
            abort();
            database.delete(quest);
            // also delete all requirements
            getTemplate().getStartRequirements().forEach(requirement -> requirement.delete(getPlayer()));
            getObjectives().forEach(objective -> {
                objective.getObjectiveTemplate().getRequirements()
                        .forEach(requirement -> requirement.delete(getPlayer()));
                objective.getObjectiveTemplate().getActions().stream()
                        .filter(action -> action instanceof RevertableAction)
                        .forEach(action -> ((RevertableAction<Player>) action).revert(getPlayer()));
            });
            getTemplate().getCompletionActions().stream()
                    .filter(action -> action instanceof RevertableAction)
                    .forEach(action -> ((RevertableAction<Player>) action).revert(getPlayer()));
        }
    }

    public void save() {

        EbeanServer database = RaidCraft.getDatabase(QuestPlugin.class);
        TPlayerQuest quest = database.find(TPlayerQuest.class, getId());
        if (quest == null) return;
        quest.setPhase(getPhase());
        quest.setStartTime(getStartTime());
        quest.setCompletionTime(getCompletionTime());
        quest.setAbortionTime(getAbortionTime());
        database.save(quest);
        // also save all quest objectives
        getObjectives().forEach(PlayerObjective::save);
    }
}
