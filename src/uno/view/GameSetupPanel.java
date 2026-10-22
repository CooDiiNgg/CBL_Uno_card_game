package uno.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import uno.util.BotDifficulty;
import uno.util.Constants;

public class GameSetupPanel extends JPanel {
    private static final int MAX_NAME_LENGTH = 16;
    private static final Color CREAM = new Color(0xF5EFDF);
    private static final Color MUTED = new Color(0xB8CEC2);
    private static final Color GOLD = new Color(0xFFE078);

    private final TableBackground background = new TableBackground();
    private final List<BotDifficulty> botDifficulties = new ArrayList<>();
    private final JTextField nameField;
    private final JPanel playerList;
    private final JLabel playerCountLabel;
    private final JButton startButton;
    private final JButton backButton;

    public GameSetupPanel() {
        setLayout(new GridBagLayout());
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(6, 30, 27, 215));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
                g.setColor(new Color(222, 196, 120, 100));
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 32, 32);
                g.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(22, 38, 22, 38));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(4, 0, 4, 0);

        JLabel title = new JLabel("New Game", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 44));
        title.setForeground(GOLD);
        card.add(title, constraints);
        card.add(label("Pick your opponents and how hard they play", 14, CREAM, SwingConstants.CENTER), constraints);

        JPanel colours = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        colours.setOpaque(false);
        for (uno.util.Color colour : uno.util.Color.getAllColors()) {
            JPanel mark = new JPanel();
            mark.setBackground(CardComponent.toAwtColor(colour));
            mark.setPreferredSize(new Dimension(32, 4));
            colours.add(mark);
        }
        card.add(colours, constraints);

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(label("PLAYERS", 11, MUTED, SwingConstants.LEFT), BorderLayout.WEST);
        playerCountLabel = label(" ", 11, MUTED, SwingConstants.RIGHT);
        heading.add(playerCountLabel, BorderLayout.EAST);
        constraints.insets = new Insets(10, 0, 2, 0);
        card.add(heading, constraints);

        nameField = new JTextField(new LimitedDocument(), Constants.DEFAULT_PLAYER_NAME, 14);
        nameField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        nameField.setForeground(CREAM);
        nameField.setCaretColor(GOLD);
        nameField.setOpaque(false);
        nameField.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        nameField.setToolTipText("Your name");

        playerList = new JPanel(new GridLayout(0, 1, 0, 8));
        playerList.setOpaque(false);
        constraints.insets = new Insets(2, 0, 6, 0);
        card.add(playerList, constraints);

        startButton = new MenuPanel.MenuButton("Start Game", true);
        backButton = new MenuPanel.MenuButton("Back", false);
        startButton.setPreferredSize(new Dimension(200, 48));
        backButton.setPreferredSize(new Dimension(130, 48));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actions.setOpaque(false);
        actions.add(backButton);
        actions.add(startButton);
        constraints.insets = new Insets(12, 0, 4, 0);
        card.add(actions, constraints);
        constraints.insets = new Insets(8, 0, 0, 0);
        card.add(label("Easy plays the first card that fits · Hard uses Minimax and learns", 12, MUTED,
                SwingConstants.CENTER), constraints);
        add(card);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "start");
        getActionMap().put("start", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { startButton.doClick(); }
        });
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "back");
        getActionMap().put("back", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { backButton.doClick(); }
        });

        botDifficulties.add(BotDifficulty.HARD);
        rebuildPlayers();
    }

    public String getPlayerName() {
        return nameField.getText().trim();
    }

    public BotDifficulty[] getBotDifficulties() {
        return botDifficulties.toArray(new BotDifficulty[0]);
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public void focusName() {
        nameField.requestFocusInWindow();
        nameField.selectAll();
    }

    private void rebuildPlayers() {
        playerList.removeAll();
        PlayerRow humanRow = new PlayerRow(0);
        JLabel you = label("YOU", 10, GOLD, SwingConstants.LEFT);
        humanRow.add(you, BorderLayout.WEST);
        humanRow.add(nameField, BorderLayout.CENTER);
        playerList.add(humanRow);

        for (int i = 0; i < botDifficulties.size(); i++) {
            final int index = i;
            PlayerRow row = new PlayerRow(i + 1);
            String name = botDifficulties.size() == 1 ? Constants.BOT_NAME : Constants.BOT_NAME + " " + (i + 1);
            row.add(label(name, 14, CREAM, SwingConstants.LEFT), BorderLayout.CENTER);
            JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            controls.setOpaque(false);
            PillButton difficulty = new PillButton(botDifficulties.get(i).getDisplayName() + "  ▾", true);
            difficulty.setToolTipText("Choose how " + name + " plays");
            difficulty.addActionListener(event -> showDifficultyMenu(difficulty, index));
            controls.add(difficulty);
            PillButton remove = new PillButton("✕", false);
            remove.setToolTipText("Remove " + name);
            remove.setEnabled(botDifficulties.size() > Constants.MIN_PLAYERS - 1);
            remove.addActionListener(event -> {
                botDifficulties.remove(index);
                rebuildPlayers();
            });
            controls.add(remove);
            row.add(controls, BorderLayout.EAST);
            playerList.add(row);
        }

        if (botDifficulties.size() < Constants.MAX_PLAYERS - 1) {
            PillButton add = new PillButton("+  Add a computer player", false);
            add.setPreferredSize(new Dimension(0, 44));
            add.addActionListener(event -> {
                botDifficulties.add(BotDifficulty.HARD);
                rebuildPlayers();
            });
            playerList.add(add);
        }
        playerCountLabel.setText((botDifficulties.size() + 1) + " / " + Constants.MAX_PLAYERS);
        playerList.revalidate();
        playerList.repaint();
    }

    private void showDifficultyMenu(JComponent anchor, int index) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createLineBorder(new Color(222, 196, 120, 140)));
        menu.setBackground(new Color(0x10261F));
        for (BotDifficulty difficulty : BotDifficulty.values()) {
            DifficultyItem item = new DifficultyItem(difficulty, difficulty == botDifficulties.get(index));
            item.addActionListener(event -> {
                menu.setVisible(false);
                botDifficulties.set(index, difficulty);
                rebuildPlayers();
            });
            menu.add(item);
        }
        menu.show(anchor, 0, anchor.getHeight() + 4);
    }

    private JLabel label(String text, int size, Color color, int alignment) {
        JLabel label = new JLabel(text, alignment);
        label.setFont(new Font(Font.SANS_SERIF, size <= 11 ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        background.paint((Graphics2D) graphics, getWidth(), getHeight());
    }

    private static class LimitedDocument extends PlainDocument {
        @Override
        public void insertString(int offset, String text, AttributeSet attributes) throws BadLocationException {
            if (text == null) {
                return;
            }
            int room = MAX_NAME_LENGTH - getLength();
            if (room > 0) {
                super.insertString(offset, text.length() > room ? text.substring(0, room) : text, attributes);
            }
        }
    }

    private static class PlayerRow extends JPanel {
        private final Color accent;

        PlayerRow(int seat) {
            super(new BorderLayout(10, 0));
            uno.util.Color[] colours = uno.util.Color.getAllColors();
            accent = CardComponent.toAwtColor(colours[seat % colours.length]);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 32, 6, 8));
            setPreferredSize(new Dimension(380, 46));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(255, 255, 255, 18));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g.setColor(new Color(222, 196, 120, 60));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g.setColor(accent);
            g.fillOval(12, getHeight() / 2 - 5, 10, 10);
            g.dispose();
        }
    }

    private static class PillButton extends JButton {
        private final boolean primary;

        PillButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            FontMetrics metrics = getFontMetrics(getFont());
            setPreferredSize(new Dimension(Math.max(34, metrics.stringWidth(text) + 26), 32));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Color base = primary ? new Color(0xE4B94E) : new Color(0x254E40);
            if (!isEnabled()) {
                base = new Color(0x1A332B);
            } else if (getModel().isRollover() || hasFocus()) {
                base = base.brighter();
            }
            if (getModel().isPressed()) {
                base = base.darker();
            }
            g.setColor(base);
            g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, getHeight() - 2, getHeight() - 2);
            g.setColor(hasFocus() ? Color.WHITE : new Color(255, 255, 255, isEnabled() ? 60 : 25));
            g.setStroke(new BasicStroke(hasFocus() ? 2 : 1));
            g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, getHeight() - 3, getHeight() - 3);
            g.setColor(!isEnabled() ? new Color(0x6E8079) : primary ? new Color(0x302710) : CREAM);
            g.setFont(getFont());
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(getText(), (getWidth() - metrics.stringWidth(getText())) / 2,
                    (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
            g.dispose();
        }
    }

    private static class DifficultyItem extends JMenuItem {
        private final BotDifficulty difficulty;
        private final boolean selected;

        DifficultyItem(BotDifficulty difficulty, boolean selected) {
            super(difficulty.getDisplayName());
            this.difficulty = difficulty;
            this.selected = selected;
            setPreferredSize(new Dimension(300, 52));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            boolean armed = getModel().isArmed() || getModel().isSelected();
            g.setColor(armed ? new Color(0x1F4B3C) : new Color(0x10261F));
            g.fillRect(0, 0, getWidth(), getHeight());
            if (armed || selected) {
                g.setColor(new Color(0xE4B94E));
                g.fillRect(0, 6, 3, getHeight() - 12);
            }
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            g.setColor(selected ? GOLD : CREAM);
            g.drawString(difficulty.getDisplayName() + (selected ? "  ✓" : ""), 16, 22);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(MUTED);
            g.drawString(difficulty.getDescription(), 16, 40);
            g.dispose();
        }
    }
}
