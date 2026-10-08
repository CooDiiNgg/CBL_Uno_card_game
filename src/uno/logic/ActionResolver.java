package uno.logic;

import uno.model.Card;
import uno.util.Color;
import uno.model.GameState;
import uno.model.Player;

public class ActionResolver {
    public static Card resolveAction(Color color, Card card, GameState gameState, boolean isSkippingTurn) {
        if (gameState == null) {
            throw new IllegalArgumentException("GameState cannot be null");
        }
        if (isSkippingTurn) {
            drawCardsHelper(gameState, gameState.nextPlayer());
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

    private static void handleSkipAction(GameState gameState) {
        gameState.nextTurn();
    }

    private static void handleReverseAction(GameState gameState) {
        gameState.reverseDirection();
    }

    private static Card handleWildAction(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null for a wild card");
        }
        return new Card(color, 13);
    }

    private static void drawCardsHelper(GameState gameState, Player nextPlayer) {
        int drawStackCount = gameState.getDrawStackCount();
        for (int i = 0; i < drawStackCount; i++) {
            if (!gameState.getDeck().isEmpty()) {
                nextPlayer.drawCard(gameState.getDeck().drawCard());
            } else {
                gameState.getDeck().mergeDeck(gameState.getDiscardPile());
                if (!gameState.getDeck().isEmpty()) {
                    nextPlayer.drawCard(gameState.getDeck().drawCard());
                } else {
                    throw new IllegalStateException("No cards left to draw");
                }
            }
        }
        gameState.setDrawStackCount(0);
        gameState.nextTurn();
    }

    private static void handleDrawTwoAction(GameState gameState, Card card) {
        Player nextPlayer = gameState.nextPlayer();
        for (Card nextPlayerCard : nextPlayer.getHand().getCards()) {
            if (RuleValidator.isValidMove(nextPlayerCard, card)) {
                return;
            }
        }
        drawCardsHelper(gameState, nextPlayer);
    }

    private static Card handleWildDrawFourAction(GameState gameState, Card card, Color color) {
        handleDrawTwoAction(gameState, card);
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null for a wild draw four card");
        }
        return new Card(color, 14);
    }

}
