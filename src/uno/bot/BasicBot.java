package uno.bot;

import java.util.List;
import uno.logic.RuleValidator;
import uno.model.Card;
import uno.model.GameState;
import uno.model.Player;
import uno.util.Color;

public class BasicBot implements BotStrategy {
    @Override
    public Card chooseCard(GameState gameState, Player player) {
        if (gameState == null || player == null) {
            throw new IllegalArgumentException("GameState and player cannot be null");
        }
        Card topCard = gameState.getDiscardPile().peekTopCard();
        List<Card> validMoves = RuleValidator.getValidMoves(player.getHand(), topCard, gameState.getDrawStackCount());
        if (validMoves.isEmpty()) {
            return null;
        }
        return validMoves.get(0);
    }

    @Override
    public Color chooseColor(GameState gameState, Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        Color[] colors = Color.getAllColors();
        int[] counts = new int[colors.length];
        for (Card card : player.getHand().getCards()) {
            for (int i = 0; i < colors.length; i++) {
                if (card.getColor() == colors[i]) {
                    counts[i]++;
                }
            }
        }
        int bestIndex = 0;
        for (int i = 1; i < colors.length; i++) {
            if (counts[i] > counts[bestIndex]) {
                bestIndex = i;
            }
        }
        return colors[bestIndex];
    }
}
