package de.raidcraft.quests.tables;

import io.ebean.annotation.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mdoering
 */
@Entity
@Table(name = "rc_quests_player_pools")
@Getter
@Setter
public class TPlayerQuestPool {

    @Id
    private int id;
    @NotNull
    @ManyToOne
    private TPlayer player;
    private String questPool;
    private Timestamp lastStart;
    private Timestamp lastCompletion;
    private Timestamp lastReset;
    private int successiveQuestCounter = 0;
    @JoinColumn(name = "quest_pool_id")
    @OneToMany(cascade = CascadeType.REMOVE)
    private List<TPlayerQuest> quests = new ArrayList<>();
}
