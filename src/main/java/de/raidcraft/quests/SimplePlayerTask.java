package de.raidcraft.quests;

import de.raidcraft.RaidCraft;
import de.raidcraft.quests.api.objective.AbstractPlayerTask;
import de.raidcraft.quests.api.objective.PlayerObjective;
import de.raidcraft.quests.api.objective.TaskTemplate;
import de.raidcraft.quests.tables.TPlayerTask;
import io.ebean.EbeanServer;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SimplePlayerTask extends AbstractPlayerTask {

    public SimplePlayerTask(TPlayerTask tableEntry, PlayerObjective objective, TaskTemplate taskTemplate) {
        super(tableEntry.getId(), objective, taskTemplate);
        setCompletionTime(tableEntry.getCompletionTime());
        setAbortionTime(tableEntry.getAbortionTime());
    }

    @Override
    public void save() {

        EbeanServer database = RaidCraft.getDatabase(QuestPlugin.class);
        TPlayerTask task = database.find(TPlayerTask.class, getId());
        task.setCompletionTime(getCompletionTime());
        task.setAbortionTime(getAbortionTime());
        database.save(task);
    }
}
