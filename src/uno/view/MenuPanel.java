package uno.view;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MenuPanel extends JPanel {
    private JButton startButton;
    private JButton loadButton;
    private JButton exitButton;

    public MenuPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.insets = new Insets(8, 0, 8, 0);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("UNO", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        startButton = createButton("Start Game");
        loadButton = createButton("Load Game");
        exitButton = createButton("Exit");

        add(titleLabel, constraints);
        add(startButton, constraints);
        add(loadButton, constraints);
        add(exitButton, constraints);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(220, 44));
        return button;
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JButton getLoadButton() {
        return loadButton;
    }

    public JButton getExitButton() {
        return exitButton;
    }
}
