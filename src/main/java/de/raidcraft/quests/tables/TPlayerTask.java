package de.raidcraft.quests.tables;

import io.ebean.annotation.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "rc_quests_player_tasks")
@Getter
@Setter
public class TPlayerTask {

    @Id
    private int id;
    @NotNull
    @ManyToOne
    private TPlayerObjective objective;
    @NotNull
    private int taskId;
    private Timestamp completionTime;
    private Timestamp abortionTime;
}
