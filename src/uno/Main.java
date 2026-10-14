package uno;

import javax.swing.SwingUtilities;
import uno.controller.BoardController;
import uno.controller.GameController;
import uno.controller.MenuController;
import uno.view.MainFrame;
import uno.view.TuiView;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("gui")) {
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame();
                GameController gameController = new GameController();
                new MenuController(mainFrame, gameController);
                new BoardController(mainFrame, gameController);
                mainFrame.setVisible(true);
            });
            return;
        }
        new TuiView(new GameController()).start();
    }
}
