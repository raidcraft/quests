package de.raidcraft.quests.api.objective;

import de.raidcraft.api.action.trigger.TriggerListener;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.quest.Quest;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * @author Silthus
 */
public interface PlayerObjective extends TriggerListener<Player>, Comparable<PlayerObjective> {

    @Override
    default Class<Player> getTriggerEntityType() {

        return Player.class;
    }

    int getId();

    Quest getQuest();

    List<PlayerTask> getTasks();

    Optional<PlayerTask> getTask(int id);

    void onTaskComplete(PlayerTask task);

    ObjectiveTemplate getObjectiveTemplate();

    QuestHolder getQuestHolder();

    Timestamp getStartTime();

    Timestamp getCompletionTime();

    Timestamp getAbortionTime();

    Quest.Phase getPhase();

    boolean isActive();

    boolean isStarted();

    boolean isCompleted();

    boolean isAborted();

    /**
     * True if the objective should be hidden from the player.
     * Depends on the current state of the objective and the values
     * {@link ObjectiveTemplate#isHidden()} and {@link ObjectiveTemplate#isSecret()}.
     *
     * @return true if the objective should be hidden from the player
     */
    boolean isHidden();

    void complete();

    void abort();

    void updateListeners();

    void unregisterListeners();

    void save();
}
