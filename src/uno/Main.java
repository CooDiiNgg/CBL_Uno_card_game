package uno;

import javax.swing.SwingUtilities;
import uno.controller.BoardController;
import uno.controller.GameController;
import uno.controller.MenuController;
import uno.bot.MinimaxBot;
import uno.view.MainFrame;
import uno.view.TuiView;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("gui")) {
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
        new TuiView(new GameController()).start();
    }
}
