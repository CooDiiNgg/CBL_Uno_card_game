package uno.logic;

import uno.model.Card;
import uno.model.GameState;
import uno.model.Player;

public interface GameListener {
    void onGameStarted(GameState gameState);

    void onCardPlayed(Player player, Card card);

    void onCardsDrawn(Player player, int amount);

    void onTurnChanged(Player player);

    void onUno(Player player);

    void onGameOver(Player winner);
}
