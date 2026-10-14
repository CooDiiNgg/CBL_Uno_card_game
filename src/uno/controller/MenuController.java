package uno.controller;

import javax.swing.JOptionPane;
import uno.persistence.SaveException;
import uno.util.Constants;
import uno.view.MainFrame;
import uno.view.MenuPanel;

public class MenuController {
    private MainFrame mainFrame;
    private GameController gameController;

    public MenuController(MainFrame mainFrame, GameController gameController) {
        if (mainFrame == null || gameController == null) {
            throw new IllegalArgumentException("MainFrame and GameController cannot be null");
        }
        this.mainFrame = mainFrame;
        this.gameController = gameController;

        MenuPanel menuPanel = mainFrame.getMenuPanel();
        menuPanel.getStartButton().addActionListener(e -> startGame());
        menuPanel.getLoadButton().addActionListener(e -> loadGame());
        menuPanel.getExitButton().addActionListener(e -> mainFrame.dispose());
    }

    private void startGame() {
        String playerName = JOptionPane.showInputDialog(mainFrame, "Your name:", Constants.DEFAULT_PLAYER_NAME);
        if (playerName == null) {
            return;
        }
        mainFrame.showGame();
        gameController.startNewGame(playerName);
    }

    private void loadGame() {
        String[] saveNames = gameController.getSaveNames();
        if (saveNames.length == 0) {
            JOptionPane.showMessageDialog(mainFrame, "No saved games found");
            return;
        }
        Object choice = JOptionPane.showInputDialog(mainFrame, "Choose a save:", "Load Game",
                JOptionPane.PLAIN_MESSAGE, null, saveNames, saveNames[0]);
        if (choice == null) {
            return;
        }
        try {
            mainFrame.showGame();
            gameController.loadGame((String) choice);
        } catch (SaveException e) {
            mainFrame.showMenu();
            JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Load failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
