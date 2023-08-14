package forge.gamemodes.quest;

import forge.deck.DeckProxy;

import java.util.ArrayList;

/**
 * A QuestEventDuel with a CommanderDeckGenerator used exclusively within QuestEventCommanderDuelManager for the
 * creation of randomly generated Commander decks in a Commander variant quest.
 * Auth. Imakuni & Forge
 */
public class QuestEventCommanderDuel extends QuestEventDuel{
    /**
     * The CommanderDeckGenerator for this duel.
     */
    private DeckProxy deckProxy;

    public DeckProxy getDeckProxy() {return deckProxy;}

    public void setDeckProxy(DeckProxy dp) {deckProxy = dp;}

    private ArrayList<QuestEventCommanderDuel> extraOpponents = new ArrayList<QuestEventCommanderDuel>(3);

    public ArrayList<QuestEventCommanderDuel> getExtraOpponents() {
        return extraOpponents;
    }
}
