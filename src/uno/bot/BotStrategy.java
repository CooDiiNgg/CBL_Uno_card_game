package uno.bot;

import uno.model.Card;
import uno.model.GameState;
import uno.model.Player;
import uno.util.Color;

public interface BotStrategy {
    Card chooseCard(GameState gameState, Player player);

    Color chooseColor(GameState gameState, Player player);
}
