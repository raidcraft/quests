package de.raidcraft.quests.api.quest;

import de.raidcraft.api.action.trigger.TriggerListener;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.objective.PlayerObjective;
import io.ebean.annotation.EnumValue;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Silthus
 */
public interface Quest extends TriggerListener<Player> {

    enum Phase {

        @EnumValue("NOT_STARTED")
        NOT_STARTED,
        @EnumValue("IN_PROGRESS")
        ACTIVE,
        @EnumValue("OBJECTIVES_COMPLETED")
        OJECTIVES_COMPLETED,
        @EnumValue("COMPLETE")
        COMPLETED,
        @EnumValue("ABORTED")
        ABORTED;

        public String getConfigName() {
            return this.name().toLowerCase().replace("_", "-");
        }
    }

    int getId();


    @Override
    default Class<Player> getTriggerEntityType() {

        return Player.class;
    }

    default String getName() {

        return getTemplate().getName();
    }

    default String getFullName() {

        return getTemplate().getId();
    }

    default String getFriendlyName() {

        return getTemplate().getFriendlyName();
    }

    default List<String> getAuthors() {

        return getTemplate().getAuthors();
    }

    default String getDescription() {

        return getTemplate().getDescription();
    }

    default Player getPlayer() {

        return getHolder().getPlayer();
    }

    default List<PlayerObjective> getUncompletedObjectives() {

        return getObjectives().stream()
                .filter(playerObjective -> !playerObjective.isCompleted())
                .filter(playerObjective -> !playerObjective.isAborted())
                .sorted()
                .collect(Collectors.toList());
    }

    List<PlayerObjective> getObjectives();

    QuestTemplate getTemplate();

    QuestHolder getHolder();

    Phase getPhase();

    boolean isCompleted();

    boolean isAborted();

    boolean isObjectiveCompleted(int id);

    boolean isTaskCompleted(int objectiveId, int taskId);

    boolean hasCompletedAllObjectives();

    void onObjectCompletion(PlayerObjective objective);

    boolean isActive();

    Timestamp getStartTime();

    Timestamp getCompletionTime();

    Timestamp getAbortionTime();

    void updateObjectiveListeners();

    boolean start();

    boolean complete();

    /**
     * Tries to abort the quest.
     *
     * @return true if the quest was aborted; false if not
     */
    boolean abort();

    void delete();

    void save();
}
