package de.raidcraft.quests.api.objective;

public interface TaskTemplate extends ObjectiveTemplate {

    /**
     * Gets the {@link ObjectiveTemplate} this task is part of.
     *
     * @return parent objective template
     */
    ObjectiveTemplate getObjectiveTemplate();
}
