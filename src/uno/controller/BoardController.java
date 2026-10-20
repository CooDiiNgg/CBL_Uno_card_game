package uno.controller;

import javax.swing.JOptionPane;
import uno.logic.GameListener;
import uno.model.Card;
import uno.model.GameState;
import uno.model.Player;
import uno.persistence.SaveException;
import uno.util.Color;
import uno.view.GamePanel;
import uno.view.MainFrame;
import uno.view.dialogs.ColorPickerDialog;

public class BoardController implements GameListener {
    private MainFrame mainFrame;
    private GamePanel gamePanel;
    private GameController gameController;
    private TurnScheduler turnScheduler;

    public BoardController(MainFrame mainFrame, GameController gameController) {
        if (mainFrame == null || gameController == null) {
            throw new IllegalArgumentException("MainFrame and GameController cannot be null");
        }
        this.mainFrame = mainFrame;
        this.gamePanel = mainFrame.getGamePanel();
        this.gameController = gameController;
        this.turnScheduler = new TurnScheduler(gameController);

        gamePanel.setCardActionListener(e -> playCard(Integer.parseInt(e.getActionCommand())));
        gamePanel.getDrawButton().addActionListener(e -> {
            if (!gamePanel.isAnimating()) {
                gameController.drawCard();
            }
        });
        gamePanel.getSaveButton().addActionListener(e -> saveGame());
        gamePanel.getMenuButton().addActionListener(e -> {
            turnScheduler.stop();
            gamePanel.cancelAnimations();
            mainFrame.showMenu();
        });
        gameController.addListener(this);
    }

    private void playCard(int handIndex) {
        if (gamePanel.isAnimating() || !gameController.isHumanTurn()) {
            return;
        }
        Card card = gameController.getHumanPlayer().getHand().getCard(handIndex);
        Color chosenColor = null;
        if (card.getColor() == Color.SPECIAL && gameController.getValidMoves().contains(card)) {
            chosenColor = ColorPickerDialog.choose(mainFrame);
            if (chosenColor == null) {
                return;
            }
        }
        if (!gameController.playCard(handIndex, chosenColor)) {
            gamePanel.setStatus("You can't play " + card + " on " + gameController.getTopCard());
        }
    }

    private void saveGame() {
        if (gamePanel.isAnimating()) {
            return;
        }
        String saveName = JOptionPane.showInputDialog(mainFrame, "Save name:");
        if (saveName == null) {
            return;
        }
        try {
            gamePanel.setStatus("Saved to " + gameController.saveGame(saveName.trim()));
        } catch (SaveException e) {
            JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refresh() {
        gamePanel.refresh(gameController.getGameState(), gameController.getHumanPlayer(),
                gameController.getValidMoves(), gameController.isHumanTurn());
    }

    private void scheduleBotTurn() {
        if (gameController.isBotTurn()) {
            turnScheduler.schedule();
        }
    }

    @Override
    public void onGameStarted(GameState gameState) {
        gamePanel.cancelAnimations();
        refresh();
        gamePanel.setStatus("It's " + gameState.getCurrentPlayer().getName() + "'s turn");
        if (gameController.isHumanTurn()) {
            gamePanel.showYourTurn();
        }
        scheduleBotTurn();
    }

    @Override
    public void onCardPlayed(Player player, Card card) {
        gamePanel.setStatus(player.getName() + " played " + card);
        gamePanel.animatePlay(player, card);
    }

    @Override
    public void onCardsDrawn(Player player, int amount) {
        gamePanel.animateDraw(player, amount);
        gamePanel.setStatus(player.getName() + " drew " + amount + (amount == 1 ? " card" : " cards"));
    }

    @Override
    public void onTurnChanged(Player player) {
        gamePanel.afterAnimations(() -> {
            refresh();
            if (gameController.isHumanTurn()) {
                gamePanel.showYourTurn();
            }
            scheduleBotTurn();
        });
    }

    @Override
    public void onUno(Player player) {
        gamePanel.setStatus(player.getName() + ": UNO!");
    }

    @Override
    public void onGameOver(Player winner) {
        turnScheduler.stop();
        gamePanel.afterAnimations(() -> {
            refresh();
            JOptionPane.showMessageDialog(mainFrame, winner.getName() + " wins!", "Game over", JOptionPane.INFORMATION_MESSAGE);
        });
    }
}
