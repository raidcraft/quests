package de.raidcraft.quests.tables;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

/**
 * @author mdoering
 */
@Entity
@Table(name = "rc_quests_player_quest_items")
@Getter
@Setter
public class TQuestItem {

    @Id
    private int id;
    private UUID player;
    private int slot;
    private int inventoryId;
    private int objectStorageId;
}
