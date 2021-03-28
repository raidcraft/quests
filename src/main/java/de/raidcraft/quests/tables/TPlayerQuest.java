package de.raidcraft.quests.tables;

import de.raidcraft.quests.api.quest.Quest;
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
@Table(name = "rc_quests_player_quests")
@Getter
@Setter
public class TPlayerQuest {

    @Id
    private int id;
    @NotNull
    @ManyToOne
    private TPlayer player;
    private String quest;
    private Quest.Phase phase;
    private Timestamp startTime;
    private Timestamp completionTime;
    private Timestamp abortionTime;
    @ManyToOne
    private TPlayerQuestPool questPool;
    @JoinColumn(name = "quest_id")
    @OneToMany(cascade = CascadeType.REMOVE)
    private List<TPlayerObjective> objectives;
}
