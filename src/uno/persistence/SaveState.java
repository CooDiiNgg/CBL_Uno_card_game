package uno.persistence;

import java.util.Arrays;
import uno.model.Card;
import uno.model.Deck;
import uno.model.GameState;
import uno.model.Hand;
import uno.model.Player;

public class SaveState {
    private String[] playerNames;
    private boolean[] playerBots;
    private Card[][] playerHands;
    private Card[] deckCards;
    private Card[] discardCards;
    private int currentPlayerIndex;
    private boolean clockwise;
    private int drawStackCount;

    public SaveState(String[] playerNames, boolean[] playerBots, Card[][] playerHands, Card[] deckCards,
                     Card[] discardCards, int currentPlayerIndex, boolean clockwise, int drawStackCount) {
        if (playerNames == null || playerBots == null || playerHands == null || deckCards == null || discardCards == null) {
            throw new IllegalArgumentException("Save state parts cannot be null");
        }
        if (playerNames.length < 2 || playerBots.length != playerNames.length || playerHands.length != playerNames.length) {
            throw new IllegalArgumentException("Save state needs at least two complete players");
        }
        if (currentPlayerIndex < 0 || currentPlayerIndex >= playerNames.length) {
            throw new IllegalArgumentException("Current player index out of bounds");
        }
        if (discardCards.length == 0) {
            throw new IllegalArgumentException("Discard pile cannot be empty");
        }
        if (drawStackCount < 0) {
            throw new IllegalArgumentException("Draw stack count cannot be negative");
        }
        this.playerNames = playerNames;
        this.playerBots = playerBots;
        this.playerHands = playerHands;
        this.deckCards = deckCards;
        this.discardCards = discardCards;
        this.currentPlayerIndex = currentPlayerIndex;
        this.clockwise = clockwise;
        this.drawStackCount = drawStackCount;
    }

    public static SaveState fromGameState(GameState gameState) {
        if (gameState == null) {
            throw new IllegalArgumentException("GameState cannot be null");
        }
        Player[] players = gameState.getPlayers();
        String[] playerNames = new String[players.length];
        boolean[] playerBots = new boolean[players.length];
        Card[][] playerHands = new Card[players.length][];
        for (int i = 0; i < players.length; i++) {
            playerNames[i] = players[i].getName();
            playerBots[i] = players[i].isBot();
            playerHands[i] = players[i].getHand().getCards().toArray(new Card[0]);
        }
        Card[] deckCards = gameState.getDeck().getCards().toArray(new Card[0]);
        Card[] discardCards = gameState.getDiscardPile().getCards().toArray(new Card[0]);
        return new SaveState(playerNames, playerBots, playerHands, deckCards, discardCards,
                gameState.getCurrentPlayerIndex(), gameState.isClockwise(), gameState.getDrawStackCount());
    }

    public GameState toGameState() {
        Player[] players = new Player[playerNames.length];
        for (int i = 0; i < players.length; i++) {
            Hand hand = new Hand();
            for (Card card : playerHands[i]) {
                hand.addCard(card);
            }
            players[i] = new Player(playerNames[i], hand, playerBots[i]);
        }
        Deck deck = new Deck(Arrays.asList(deckCards));
        Deck discardPile = new Deck(Arrays.asList(discardCards));
        GameState gameState = new GameState(deck, discardPile, players);
        gameState.setCurrentPlayerIndex(currentPlayerIndex);
        gameState.setClockwise(clockwise);
        gameState.setDrawStackCount(drawStackCount);
        return gameState;
    }

    public String[] getPlayerNames() {
        return playerNames.clone();
    }

    public boolean[] getPlayerBots() {
        return playerBots.clone();
    }

    public Card[][] getPlayerHands() {
        return playerHands.clone();
    }

    public Card[] getDeckCards() {
        return deckCards.clone();
    }

    public Card[] getDiscardCards() {
        return discardCards.clone();
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public boolean isClockwise() {
        return clockwise;
    }

    public int getDrawStackCount() {
        return drawStackCount;
    }
}
