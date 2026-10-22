package uno;

import javax.swing.SwingUtilities;
import uno.controller.BoardController;
import uno.controller.GameController;
import uno.controller.MenuController;
import uno.bot.MinimaxBot;
import uno.view.MainFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            MinimaxBot minimaxBot = new MinimaxBot();
            GameController gameController = new GameController(minimaxBot);
            gameController.addListener(minimaxBot);
            new MenuController(mainFrame, gameController);
            new BoardController(mainFrame, gameController);
            mainFrame.setVisible(true);
        });
        return;
    }
}
