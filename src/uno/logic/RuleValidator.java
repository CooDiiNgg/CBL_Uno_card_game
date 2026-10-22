package uno.logic;

import java.util.ArrayList;
import java.util.List;
import uno.model.Card;
import uno.model.Hand;
import uno.model.Player;

public class RuleValidator {
    public static boolean isValidMove(Card playedCard, Card topCard, int drawStackCount) {
        if (playedCard == null || topCard == null) {
            throw new IllegalArgumentException("Cards cannot be null");
        }
        if (drawStackCount > 0 && isDrawCard(topCard)) {
            return resolveDrawRule(playedCard, topCard);
        }
        return playedCard.getColor().matches(topCard.getColor()) || playedCard.getValue() == topCard.getValue();
    }

    public static List<Card> getValidMoves(Hand hand, Card topCard, int drawStackCount) {
        if (hand == null) {
            throw new IllegalArgumentException("Hand cannot be null");
        }
        List<Card> validMoves = new ArrayList<>();
        for (Card card : hand.getCards()) {
            if (isValidMove(card, topCard, drawStackCount)) {
                validMoves.add(card);
            }
        }
        return validMoves;
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
