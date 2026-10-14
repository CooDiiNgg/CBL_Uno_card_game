package uno.controller;

import java.util.ArrayList;
import java.util.List;
import uno.bot.BasicBot;
import uno.bot.BotStrategy;
import uno.logic.GameEngine;
import uno.logic.GameListener;
import uno.model.Card;
import uno.model.GameState;
import uno.model.Hand;
import uno.model.Player;
import uno.persistence.SaveException;
import uno.persistence.SaveManager;
import uno.util.Color;
import uno.util.Constants;

public class GameController {
    private GameEngine gameEngine;
    private BotStrategy botStrategy;
    private List<GameListener> listeners;

    public GameController() {
        this(new BasicBot());
    }

    public GameController(BotStrategy botStrategy) {
        if (botStrategy == null) {
            throw new IllegalArgumentException("Bot strategy cannot be null");
        }
        this.botStrategy = botStrategy;
        this.listeners = new ArrayList<>();
    }

    public void addListener(GameListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners.add(listener);
        if (gameEngine != null) {
            gameEngine.addListener(listener);
        }
    }

    public void setBotStrategy(BotStrategy botStrategy) {
        if (botStrategy == null) {
            throw new IllegalArgumentException("Bot strategy cannot be null");
        }
        this.botStrategy = botStrategy;
    }

    public void startNewGame(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = Constants.DEFAULT_PLAYER_NAME;
        }
        Player[] players = new Player[]{
                new Player(playerName.trim(), new Hand(), false),
                new Player(Constants.BOT_NAME, new Hand(), true)
        };
        startGame(GameEngine.createGame(players));
    }

    public void loadGame(String saveName) throws SaveException {
        startGame(SaveManager.load(saveName));
    }

    public String saveGame(String saveName) throws SaveException {
        if (gameEngine == null) {
            throw new SaveException("There is no game to save");
        }
        if (gameEngine.isGameOver()) {
            throw new SaveException("The game is already over");
        }
        return SaveManager.save(gameEngine.getGameState(), saveName);
    }

    public String[] getSaveNames() {
        return SaveManager.listSaves();
    }

    public boolean playCard(int handIndex, Color chosenColor) {
        if (!isHumanTurn()) {
            return false;
        }
        Player currentPlayer = getGameState().getCurrentPlayer();
        if (handIndex < 0 || handIndex >= currentPlayer.getHand().size()) {
            return false;
        }
        Card card = currentPlayer.getHand().getCard(handIndex);
        if (!gameEngine.canPlay(card)) {
            return false;
        }
        if (card.getColor() == Color.SPECIAL && (chosenColor == null || chosenColor == Color.SPECIAL)) {
            return false;
        }
        gameEngine.playTurn(card, chosenColor);
        return true;
    }

    public Card drawCard() {
        if (!isHumanTurn()) {
            return null;
        }
        return gameEngine.drawTurn();
    }

    public void playBotTurn() {
        if (!isBotTurn()) {
            return;
        }
        GameState gameState = getGameState();
        Player botPlayer = gameState.getCurrentPlayer();
        Card card = botStrategy.chooseCard(gameState, botPlayer);
        if (card == null || !gameEngine.canPlay(card)) {
            gameEngine.drawTurn();
            return;
        }
        Color chosenColor = null;
        if (card.getColor() == Color.SPECIAL) {
            chosenColor = botStrategy.chooseColor(gameState, botPlayer);
        }
        gameEngine.playTurn(card, chosenColor);
    }

    public boolean hasGame() {
        return gameEngine != null;
    }

    public GameState getGameState() {
        if (gameEngine == null) {
            return null;
        }
        return gameEngine.getGameState();
    }

    public Card getTopCard() {
        return gameEngine.getTopCard();
    }

    public List<Card> getValidMoves() {
        return gameEngine.getValidMoves(getGameState().getCurrentPlayer());
    }

    public Player getHumanPlayer() {
        for (Player player : getGameState().getPlayers()) {
            if (!player.isBot()) {
                return player;
            }
        }
        return null;
    }

    public boolean isHumanTurn() {
        return hasGame() && !isGameOver() && !getGameState().getCurrentPlayer().isBot();
    }

    public boolean isBotTurn() {
        return hasGame() && !isGameOver() && getGameState().getCurrentPlayer().isBot();
    }

    public boolean isGameOver() {
        return hasGame() && gameEngine.isGameOver();
    }

    public Player getWinner() {
        if (gameEngine == null) {
            return null;
        }
        return gameEngine.getWinner();
    }

    private void startGame(GameState gameState) {
        gameEngine = new GameEngine(gameState);
        for (GameListener listener : listeners) {
            gameEngine.addListener(listener);
        }
        for (GameListener listener : listeners) {
            listener.onGameStarted(gameState);
        }
    }
}
