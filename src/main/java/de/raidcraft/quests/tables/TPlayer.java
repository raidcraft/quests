package de.raidcraft.quests.tables;

import io.ebean.annotation.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rc_quests_players")
@Getter
@Setter
public class TPlayer {

    @Id
    private int id;
    @NotNull
    @Column(unique = true)
    private String player;
    private UUID playerId;
    @JoinColumn(name = "player_id")
    @OneToMany(cascade = CascadeType.REMOVE)
    private List<TPlayerQuest> quests = new ArrayList<>();
    @JoinColumn(name = "player_id")
    @OneToMany(cascade = CascadeType.REMOVE)
    private List<TPlayerQuestPool> questPools = new ArrayList<>();
}

