package uno.logic;

import java.util.ArrayList;
import java.util.List;
import uno.model.Card;
import uno.model.Deck;
import uno.model.GameState;
import uno.model.Player;
import uno.util.Color;
import uno.util.Constants;

public class GameEngine {
    GameState gameState;
    private List<GameListener> listeners;

    public GameEngine(GameState gameState) {
        if (gameState == null) {
            throw new IllegalArgumentException("GameState cannot be null");
        }
        this.gameState = gameState;
        this.listeners = new ArrayList<>();
    }

    public static GameState createGame(Player[] players) {
        if (players == null || players.length < 2) {
            throw new IllegalArgumentException("At least two players are needed");
        }
        Deck deck = new Deck();
        for (int i = 0; i < Constants.STARTING_HAND_SIZE; i++) {
            for (Player player : players) {
                player.drawCard(deck.drawCard());
            }
        }
        Card startingCard = deck.drawCard();
        while (startingCard.getColor() == Color.SPECIAL) {
            Deck returnedCards = new Deck(new ArrayList<>());
            returnedCards.addCard(startingCard);
            deck.mergeDeck(returnedCards);
            startingCard = deck.drawCard();
        }
        Deck discardPile = new Deck(new ArrayList<>());
        discardPile.addCard(startingCard);
        return new GameState(deck, discardPile, players);
    }

    public void addListener(GameListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners.add(listener);
    }

    public void removeListener(GameListener listener) {
        listeners.remove(listener);
    }

    public GameState getGameState() {
        return gameState;
    }

    public Card getTopCard() {
        return gameState.getDiscardPile().peekTopCard();
    }

    public boolean canPlay(Card card) {
        return RuleValidator.isValidMove(card, getTopCard(), gameState.getDrawStackCount());
    }

    public List<Card> getValidMoves(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        return RuleValidator.getValidMoves(player.getHand(), getTopCard(), gameState.getDrawStackCount());
    }

    public boolean isGameOver() {
        return RuleValidator.isGameOver(gameState.getPlayers());
    }

    public Player getWinner() {
        for (Player player : gameState.getPlayers()) {
            if (player.getHand().isEmpty()) {
                return player;
            }
        }
        return null;
    }

    public void playTurn(Card playedCard, Color chosenColor) {
        if (isGameOver()) {
            throw new IllegalStateException("The game is already over");
        }
        Player currentPlayer = gameState.getCurrentPlayer();
        if (playedCard == null || !currentPlayer.getHand().getCards().contains(playedCard)) {
            throw new IllegalArgumentException("Card is not in the current player's hand");
        }
        Card topCard = getTopCard();
        if (!RuleValidator.isValidMove(playedCard, topCard, gameState.getDrawStackCount())) {
            throw new IllegalArgumentException("Invalid move");
        }
        if (playedCard.getColor() == Color.SPECIAL && (chosenColor == null || chosenColor == Color.SPECIAL)) {
            throw new IllegalArgumentException("A color must be chosen for a wild card");
        }

        Player nextPlayer = gameState.nextPlayer();
        int nextPlayerHandSize = nextPlayer.getHand().size();
        currentPlayer.getHand().removeCard(playedCard);

        Card resolvedCard = ActionResolver.resolveAction(chosenColor, playedCard, gameState, false);
        if (resolvedCard != null) {
            gameState.getDiscardPile().addCard(resolvedCard);
        }
        notifyCardPlayed(currentPlayer, resolvedCard);

        int drawnCards = nextPlayer.getHand().size() - nextPlayerHandSize;
        if (drawnCards > 0) {
            notifyCardsDrawn(nextPlayer, drawnCards);
        }

        if (currentPlayer.getHand().isEmpty()) {
            notifyGameOver(currentPlayer);
            return;
        }
        if (currentPlayer.getHand().size() == 1) {
            notifyUno(currentPlayer);
        }

        gameState.nextTurn();
        notifyTurnChanged(gameState.getCurrentPlayer());
    }

    public Card drawTurn() {
        if (isGameOver()) {
            throw new IllegalStateException("The game is already over");
        }
        Player currentPlayer = gameState.getCurrentPlayer();
        if (gameState.getDrawStackCount() > 0) {
            int handSize = currentPlayer.getHand().size();
            ActionResolver.resolveAction(null, null, gameState, true);
            notifyCardsDrawn(currentPlayer, currentPlayer.getHand().size() - handSize);
            notifyTurnChanged(gameState.getCurrentPlayer());
            return null;
        }

        Card drawnCard = ActionResolver.drawFromDeck(gameState);
        if (drawnCard != null) {
            currentPlayer.drawCard(drawnCard);
        }
        notifyCardsDrawn(currentPlayer, drawnCard == null ? 0 : 1);

        gameState.nextTurn();
        notifyTurnChanged(gameState.getCurrentPlayer());
        return drawnCard;
    }

    private void notifyCardPlayed(Player player, Card card) {
        for (GameListener listener : listeners) {
            listener.onCardPlayed(player, card);
        }
    }

    private void notifyCardsDrawn(Player player, int amount) {
        for (GameListener listener : listeners) {
            listener.onCardsDrawn(player, amount);
        }
    }

    private void notifyTurnChanged(Player player) {
        for (GameListener listener : listeners) {
            listener.onTurnChanged(player);
        }
    }

    private void notifyUno(Player player) {
        for (GameListener listener : listeners) {
            listener.onUno(player);
        }
    }

    private void notifyGameOver(Player winner) {
        for (GameListener listener : listeners) {
            listener.onGameOver(winner);
        }
    }
}
