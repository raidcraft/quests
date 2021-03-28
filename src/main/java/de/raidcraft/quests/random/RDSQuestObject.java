package de.raidcraft.quests.random;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.quests.QuestException;
import de.raidcraft.api.random.GenericRDSValue;
import de.raidcraft.api.random.RDSObject;
import de.raidcraft.api.random.RDSObjectFactory;
import de.raidcraft.quests.QuestManager;
import de.raidcraft.quests.api.quest.QuestTemplate;
import de.raidcraft.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author mdoering
 */
public class RDSQuestObject extends GenericRDSValue<QuestTemplate> {

    public static final String RDS_NAME = "quest";

    @RDSObjectFactory.Name(RDS_NAME)
    public static class RDSQuestFactory implements RDSObjectFactory {

        @Override
        public RDSObject createInstance(ConfigurationSection config) {

            try {
                return new RDSQuestObject(config.getString("quest"));
            } catch (QuestException e) {
                RaidCraft.LOGGER.warning(e.getMessage() + " in " + ConfigUtil.getFileName(config));
            }
            return null;
        }
    }

    public RDSQuestObject(QuestTemplate quest) {

        super(quest);
    }

    public RDSQuestObject(String quest) throws QuestException {

        this(RaidCraft.getComponent(QuestManager.class).getQuestTemplate(quest));
    }
}
