package uno.logic;

import uno.model.Card;
import uno.model.Deck;
import uno.util.Color;
import uno.model.GameState;
import uno.model.Player;

public class ActionResolver {
    public static Card resolveAction(Color color, Card card, GameState gameState, boolean isSkippingTurn) {
        if (gameState == null) {
            throw new IllegalArgumentException("GameState cannot be null");
        }
        if (isSkippingTurn) {
            drawCardsHelper(gameState, gameState.getCurrentPlayer());
            return null;
        }
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        switch (card.getValue()) {
            case 10:
                handleSkipAction(gameState);
                break;
            case 11:
                handleReverseAction(gameState);
                break;
            case 12:
                gameState.setDrawStackCount(gameState.getDrawStackCount() + 2);
                handleDrawTwoAction(gameState, card);
                break;
            case 13:
                return handleWildAction(color);
            case 14:
                return handleWildDrawFourAction(gameState, card, color);
            default:
                break;
        }
        return card;
    }

    public static Card drawFromDeck(GameState gameState) {
        if (gameState == null) {
            throw new IllegalArgumentException("GameState cannot be null");
        }
        Deck deck = gameState.getDeck();
        if (deck.isEmpty()) {
            refillDeck(gameState);
        }
        if (deck.isEmpty()) {
            return null;
        }
        return deck.drawCard();
    }

    private static void refillDeck(GameState gameState) {
        Deck discardPile = gameState.getDiscardPile();
        if (discardPile.size() <= 1) {
            return;
        }
        Card topCard = discardPile.drawCard();
        gameState.getDeck().mergeDeck(discardPile);
        discardPile.addCard(topCard);
    }

    private static void handleSkipAction(GameState gameState) {
        gameState.nextTurn();
    }

    private static void handleReverseAction(GameState gameState) {
        gameState.reverseDirection();
        if (gameState.getPlayers().length == 2) {
            gameState.nextTurn();
        }
    }

    private static Card handleWildAction(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null for a wild card");
        }
        return new Card(color, 13);
    }

    private static void drawCardsHelper(GameState gameState, Player player) {
        int drawStackCount = gameState.getDrawStackCount();
        for (int i = 0; i < drawStackCount; i++) {
            Card card = drawFromDeck(gameState);
            if (card == null) {
                break;
            }
            player.drawCard(card);
        }
        gameState.setDrawStackCount(0);
        gameState.nextTurn();
    }

    private static void handleDrawTwoAction(GameState gameState, Card card) {
        Player nextPlayer = gameState.nextPlayer();
        for (Card nextPlayerCard : nextPlayer.getHand().getCards()) {
            if (RuleValidator.isValidMove(nextPlayerCard, card, gameState.getDrawStackCount())) {
                return;
            }
        }
        drawCardsHelper(gameState, nextPlayer);
    }

    private static Card handleWildDrawFourAction(GameState gameState, Card card, Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null for a wild draw four card");
        }
        gameState.setDrawStackCount(gameState.getDrawStackCount() + 4);
        handleDrawTwoAction(gameState, card);
        return new Card(color, 14);
    }

}
