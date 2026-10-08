package uno.logic;

import uno.model.Card;
import uno.model.Player;

public class RuleValidator {
    public static boolean isValidMove(Card playedCard, Card topCard) {
        if (playedCard == null || topCard == null) {
            throw new IllegalArgumentException("Cards cannot be null");
        }
        if (isDrawCard(topCard)) {
            return resolveDrawRule(playedCard, topCard);
        }
        return playedCard.getColor().matches(topCard.getColor()) || playedCard.getValue() == topCard.getValue();
    }
    
    private static boolean isDrawCard(Card card) {
        return card.getValue() == 12 || card.getValue() == 14;
    }

    private static boolean resolveDrawRule(Card playedCard, Card topCard) {
        if (topCard.getValue() == 12){
            return isDrawCard(playedCard);
        }
        if (topCard.getValue() == 14){
            return playedCard.getValue() == 14;
        }
        return false;
    }

    public static boolean isGameOver(Player[] players) {
        for (Player player : players) {
            if (player.getHand().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
