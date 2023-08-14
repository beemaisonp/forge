package forge.gamemodes.quest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import forge.deck.CardPool;
import forge.deck.CommanderDeckGenerator;
import forge.deck.Deck;
import forge.deck.DeckFormat;
import forge.deck.DeckProxy;
import forge.gamemodes.quest.data.QuestPreferences;
import forge.item.PaperCard;
import forge.model.FModel;
import forge.util.MyRandom;

/**
 * Manages the creation of random Commander duels for a Commander variant quest. Random generation is handled via
 * the CommanderDeckGenerator class.
 * Auth. Forge & Imakuni#8015
 */
public class QuestEventCommanderDuelManager implements QuestEventDuelManagerInterface {
    /**
     * The list of all possible Commander variant duels.
     */
    private ArrayList<QuestEventDuel> commanderDuels = new ArrayList<>();

    /**
     * Contains the expert deck lists for the commanders.
     */
    private List<DeckProxy> expertCommanderDecks;

    /**
     * Immediately calls assembleDuels() to setup the commanderDuels variable.
     */
    public QuestEventCommanderDuelManager(){
        assembleDuels();
    }

    /**
     * Assembles the list of all possible Commander duels via CommanderDeckGenerator. Should be done within constructor.
     */
    private void assembleDuels() {
        //isCardGen = true seemed to make slightly more difficult decks based purely on experience with a very small sample size.
        //Gotta work on this more, its making pretty average decks after further testing.
        expertCommanderDecks = CommanderDeckGenerator.getCommanderDecks(DeckFormat.Commander, true, true);

        List<DeckProxy> generatedDuels = CommanderDeckGenerator.getCommanderDecks(DeckFormat.Commander, true, false);

        for(DeckProxy dp : generatedDuels){
            QuestEventCommanderDuel duel = new QuestEventCommanderDuel();

            duel.setDescription("Randomly generated " + dp.getName() + " commander deck.");
            duel.setName(dp.getName());
            duel.setTitle(dp.getName());
            duel.setOpponentName(dp.getName());
            duel.setDifficulty(QuestEventDifficulty.EASY);
            duel.setDeckProxy(dp);

            //Setting a blank deck avoids a null pointer exception. The deck is generated in generateDuels() to avoid long load times.
            duel.setEventDeck(new Deck());

            commanderDuels.add(duel);
        }
    }

    /**
     * Retrieve list of all possible Commander duels.
     * @return ArrayList containing all possible Commander duels.
     */
    public Iterable<QuestEventDuel> getAllDuels() {
        return commanderDuels;
    }

    /**
     * Retrieve list of all possible Commander duels.
     * @param difficulty Currently unused
     * @return ArrayList containing all possible Commander duels.
     */
    public Iterable<QuestEventDuel> getDuels(QuestEventDifficulty difficulty){
        return commanderDuels;
    }

    public void generateCommanderDuel(final List<QuestEventDuel> duelOpponents, int enemies) {
        if (enemies < 0 || enemies > 10) {
            enemies = 1;
        }

        QuestEventCommanderDuel duelQuartet = (QuestEventCommanderDuel)commanderDuels.get(((int) (commanderDuels.size() * MyRandom.getRandom().nextDouble())));
        duelOpponents.add(duelQuartet);
        duelQuartet.setEventDeck(duelQuartet.getDeckProxy().getDeck());
        modifyDuelForDifficulty(duelQuartet);

        String description = "Randomized match against: " + duelQuartet.getName();

        //generalize this, just for testin
        for (int i = 0; i < enemies-1; i++) {
            QuestEventCommanderDuel duelExtras = (QuestEventCommanderDuel) commanderDuels.get(((int) (commanderDuels.size() * MyRandom.getRandom().nextDouble())));
            duelExtras.setEventDeck(duelExtras.getDeckProxy().getDeck());
            modifyDuelForDifficulty(duelExtras);
            description += "; " + duelExtras.getName();
            duelQuartet.getExtraOpponents().add(duelExtras); // error here, figure out how lists work
        }
        duelQuartet.setDescription(description);
        if (enemies > 1) {
            duelQuartet.setTitle("" + (enemies+1) + "-Way Duel");
        }
    }

    /**
     * Composes an ArrayList containing 4 QuestEventDuels composed with Commander variant decks. One duel will have its
     * title replaced as Random.
     * @return ArrayList of QuestEventDuels containing 4 duels.
     */
    public List<QuestEventDuel> generateDuels() {
        final List<QuestEventDuel> duelOpponents = new ArrayList<>();

        /*
        //generate testing multiopponent duel

        QuestEventCommanderDuel duelQuartet = (QuestEventCommanderDuel)commanderDuels.get(((int) (commanderDuels.size() * MyRandom.getRandom().nextDouble())));
        duelOpponents.add(duelQuartet);
        duelQuartet.setEventDeck(duelQuartet.getDeckProxy().getDeck());
        duelQuartet.setDescription("Ey Im testing here, should be 2 dudes");
        modifyDuelForDifficulty(duelQuartet);

        //generalize this, just for testin
        for (i = 0; i < 3; i++) {
            QuestEventCommanderDuel duel2 = (QuestEventCommanderDuel)commanderDuels.get(((int) (commanderDuels.size() * MyRandom.getRandom().nextDouble())));
            duel2.setEventDeck(duel2.getDeckProxy().getDeck());
            modifyDuelForDifficulty(duel2);
            duelQuartet.getExtraOpponents().add(duel2); // error here, figure out how lists work
        }
        */

        generateCommanderDuel(duelOpponents, 3);
        generateCommanderDuel(duelOpponents, 2);

        //While there are less than 4 duels chosen
        while (duelOpponents.size() < 6) {
            //Get a random duel from the possible duels list
            QuestEventCommanderDuel duel = (QuestEventCommanderDuel)commanderDuels.get(((int) (commanderDuels.size() * MyRandom.getRandom().nextDouble())));

            //If the chosen duels list already contains this duel, get a different duel to prevent duplicate duels
            if(duelOpponents.contains(duel)) continue;

            //Add the randomly chosen duel to the duel list
            duelOpponents.add(duel);

            //Here the actual deck for this commander is generated by calling .getDeck() on the saved DeckProxy
            duel.setEventDeck(duel.getDeckProxy().getDeck());

            //Modify deck for difficulty
            modifyDuelForDifficulty(duel);
        }

        //Modify the stats of the final duel to hide the opponent, creating a "random" duel.
        //We make a copy of the final duel and overwrite it in the duelOpponents to avoid changing the variables in
        //the original duel, which gets reused.
        QuestEventCommanderDuel duel = (QuestEventCommanderDuel)duelOpponents.get(duelOpponents.size() - 1);
        QuestEventCommanderDuel randomDuel = new QuestEventCommanderDuel();

        randomDuel.setName(duel.getName());
        randomDuel.setOpponentName(duel.getName());
        randomDuel.setDeckProxy(duel.getDeckProxy());
        randomDuel.setTitle("Random Opponent");
        randomDuel.setShowDifficulty(false);
        randomDuel.setDescription("Fight a random generated commander opponent.");
        randomDuel.setIsRandomMatch(true);
        randomDuel.setEventDeck(duel.getEventDeck());

        //Replace the final duel with this newly modified "random" duel
        duelOpponents.set(duelOpponents.size()-1, randomDuel);

        return duelOpponents;
    }

    /**
     * Retrieves the expert level deck generation of a deck with the same commander as the provided DeckProxy.
     * @param dp The easy generation commander deck
     * @return The same commander's expert generation DeckProxy
     */
    private Deck getExpertGenDeck(DeckProxy dp) {
        for(QuestEventDuel qed : commanderDuels){
            QuestEventCommanderDuel cmdQED = (QuestEventCommanderDuel)qed;
            if(cmdQED.getDeckProxy().getName().equals(dp.getName())){
                return cmdQED.getDeckProxy().getDeck();
            }
        }
        return null;
    }

    /**
     * Modifies a given duel by replacing a percentage of the deck with random cards from the more difficult generated version
     * of the same commander's deck. Medium replaces 30%, Hard replaces 60%, Expert replaces 100%.
     * @param duel The QuestEventCommanderDuel to modify
     */
    private void modifyDuelForDifficulty(QuestEventCommanderDuel duel) {
        final QuestPreferences questPreferences = FModel.getQuestPreferences();
        final int index = FModel.getQuest().getAchievements().getDifficulty();
        final int numberOfWins = FModel.getQuest().getAchievements().getWin();
        Deck expertDeck = getExpertGenDeck(duel.getDeckProxy());

        int difficultyReplacementPercent = 0;

        //Note: The code is ordered to make the least number of comparisons I could think of at the time for speed reasons.
        //In reality, it shouldn't really make much difference, but why not?
        if (numberOfWins >= questPreferences.getPrefInt(QuestPreferences.DifficultyPrefs.WINS_EXPERTAI, index)) {
            //At expert, the deck is replaced with the entire expert deck, and we can return immediately
            duel.setEventDeck(expertDeck);
            duel.setDifficulty(QuestEventDifficulty.EXPERT);
            return;
        }

        if (numberOfWins >= questPreferences.getPrefInt(QuestPreferences.DifficultyPrefs.WINS_MEDIUMAI, index)) {
            difficultyReplacementPercent += 30;
            duel.setDifficulty(QuestEventDifficulty.MEDIUM);
        } else return; //return early here since it would be an easy opponent with no changes

        if (numberOfWins >= questPreferences.getPrefInt(QuestPreferences.DifficultyPrefs.WINS_HARDAI, index)) {
            difficultyReplacementPercent += 30;
            duel.setDifficulty(QuestEventDifficulty.HARD);
        }

        CardPool easyMain = duel.getEventDeck().getMain();
        CardPool expertMain = expertDeck.getMain();

        List<PaperCard> easyList = easyMain.toFlatList();
        List<PaperCard> expertList = expertMain.toFlatList();

        //Replace cards in the easy deck with cards from the expert deck up to the difficulty replacement percent
        for (int i = 0; i < difficultyReplacementPercent; i++) {
            if (!easyMain.contains(expertList.get(i))) { //ensure that the card being copied over isn't already in the deck
                easyMain.remove(easyList.get(i));
                easyMain.add(expertList.get(i));
            } else {
                expertList.remove(expertList.get(i));
                i--;
                if (expertList.size() == 0) break; //break if there are no more cards to copy over
            }
        }
    }

    /**
     * Randomizes the list of Commander Duels.
     */
    public void randomizeOpponents(){
        Collections.shuffle(commanderDuels);
    }
}
