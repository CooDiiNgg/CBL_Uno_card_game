package uno.view;

import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainFrame extends JFrame {
    private static final String MENU_SCREEN = "menu";
    private static final String SETUP_SCREEN = "setup";
    private static final String GAME_SCREEN = "game";

    private CardLayout cardLayout;
    private JPanel screens;
    private MenuPanel menuPanel;
    private GameSetupPanel setupPanel;
    private GamePanel gamePanel;

    public MainFrame() {
        super("UNO");
        cardLayout = new CardLayout();
        screens = new JPanel(cardLayout);
        menuPanel = new MenuPanel();
        setupPanel = new GameSetupPanel();
        gamePanel = new GamePanel();
        screens.add(menuPanel, MENU_SCREEN);
        screens.add(setupPanel, SETUP_SCREEN);
        screens.add(gamePanel, GAME_SCREEN);
        setContentPane(screens);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
    }

    public void showMenu() {
        cardLayout.show(screens, MENU_SCREEN);
    }

    public void showSetup() {
        cardLayout.show(screens, SETUP_SCREEN);
        setupPanel.focusName();
    }

    public void showGame() {
        cardLayout.show(screens, GAME_SCREEN);
    }

    public MenuPanel getMenuPanel() {
        return menuPanel;
    }

    public GameSetupPanel getSetupPanel() {
        return setupPanel;
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }
}
