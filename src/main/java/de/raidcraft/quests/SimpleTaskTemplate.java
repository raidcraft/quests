package de.raidcraft.quests;

import de.raidcraft.quests.api.objective.ObjectiveTemplate;
import de.raidcraft.quests.api.objective.TaskTemplate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;

@Data
@EqualsAndHashCode(of = {"objectiveTemplate"}, callSuper = true)
public class SimpleTaskTemplate extends SimpleObjectiveTemplate implements TaskTemplate {

    private final ObjectiveTemplate objectiveTemplate;

    public SimpleTaskTemplate(int id, ObjectiveTemplate objectiveTemplate, ConfigurationSection data) {
        super(id, objectiveTemplate.getQuestTemplate(), data);
        this.objectiveTemplate = objectiveTemplate;
    }

    @Override
    protected Collection<TaskTemplate> loadTasks(ConfigurationSection data) {
        return new ArrayList<>();
    }
}
