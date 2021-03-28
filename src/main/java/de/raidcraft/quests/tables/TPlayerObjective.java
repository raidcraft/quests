package de.raidcraft.quests.tables;

import io.ebean.annotation.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rc_quests_player_objectives")
@Getter
@Setter
public class TPlayerObjective {

    @Id
    private int id;
    @NotNull
    @ManyToOne
    private TPlayerQuest quest;
    @NotNull
    private int objectiveId;
    private Timestamp completionTime;
    private Timestamp abortionTime;
    private Timestamp startTime;
    @JoinColumn(name = "objective_id")
    @OneToMany(cascade = CascadeType.REMOVE)
    private List<TPlayerTask> tasks;
}
