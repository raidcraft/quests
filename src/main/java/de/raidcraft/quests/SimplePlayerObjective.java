package de.raidcraft.quests;

import de.raidcraft.RaidCraft;
import de.raidcraft.quests.api.objective.AbstractPlayerObjective;
import de.raidcraft.quests.api.objective.ObjectiveTemplate;
import de.raidcraft.quests.api.objective.PlayerTask;
import de.raidcraft.quests.api.objective.TaskTemplate;
import de.raidcraft.quests.api.quest.Quest;
import de.raidcraft.quests.tables.TPlayerObjective;
import de.raidcraft.quests.tables.TPlayerTask;
import io.ebean.EbeanServer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Silthus
 */
public class SimplePlayerObjective extends AbstractPlayerObjective {

    public SimplePlayerObjective(TPlayerObjective tableEntry, Quest quest, ObjectiveTemplate objectiveTemplate) {

        super(tableEntry.getId(), quest, objectiveTemplate);
        setCompletionTime(tableEntry.getCompletionTime());
        setAbortionTime(tableEntry.getAbortionTime());
        setStartTime(tableEntry.getStartTime());
    }

    @Override
    public String getListenerId() {
        return getQuest().getListenerId() + "." + getObjectiveTemplate().getId() + "." + getId();
    }

    @Override
    protected List<PlayerTask> loadTasks() {
        List<PlayerTask> tasks = new ArrayList<>();
        EbeanServer database = RaidCraft.getDatabase(QuestPlugin.class);
        for (TaskTemplate taskTemplate : getObjectiveTemplate().getTasks()) {
            TPlayerTask entry = database.find(TPlayerTask.class).where()
                    .eq("objective_id", getId())
                    .eq("task_id", taskTemplate.getId()).findOne();
            // create a new db entry if none exists
            if (entry == null) {
                entry = new TPlayerTask();
                entry.setTaskId(taskTemplate.getId());
                entry.setObjective(database.find(TPlayerObjective.class, getId()));
                database.save(entry);
            }
            tasks.add(new SimplePlayerTask(entry, this, taskTemplate));
        }
        return tasks;
    }

    @Override
    public void save() {

        EbeanServer database = RaidCraft.getDatabase(QuestPlugin.class);
        TPlayerObjective objective = database.find(TPlayerObjective.class, getId());
        objective.setCompletionTime(getCompletionTime());
        objective.setAbortionTime(getAbortionTime());
        objective.setStartTime(getStartTime());
        database.save(objective);
    }
}
