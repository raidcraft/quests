package de.raidcraft.quests.api.objective;

import de.raidcraft.api.action.trigger.TriggerListener;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.quest.Quest;
import org.bukkit.entity.Player;

import java.sql.Timestamp;

public interface PlayerTask extends TriggerListener<Player>, Comparable<PlayerTask> {

    @Override
    default Class<Player> getTriggerEntityType() {

        return Player.class;
    }

    /**
     * The database id of the task.
     *
     * @return database id of the task
     */
    int getId();

    /**
     * The quest associated with this task.
     *
     * @return quest of the task
     */
    default Quest getQuest() {
        return getObjective().getQuest();
    }

    /**
     * Gets the parent objective of the task.
     *
     * @return parent objective
     */
    PlayerObjective getObjective();

    /**
     * Gets the config template of the task.
     *
     * @return config template of the task
     */
    TaskTemplate getTaskTemplate();

    default QuestHolder getQuestHolder() {
        return getObjective().getQuestHolder();
    }

    Timestamp getCompletionTime();

    Timestamp getAbortionTime();

    boolean isActive();

    boolean isCompleted();

    boolean isAborted();

    boolean isHidden();

    void complete();

    void abort();

    void updateListeners();

    void unregisterListeners();

    void save();
}
