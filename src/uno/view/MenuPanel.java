package uno.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import uno.model.Card;

public class MenuPanel extends JPanel {
    private final TableBackground background = new TableBackground();
    private final CardComponent[] decoration = {
        new CardComponent(new Card(uno.util.Color.RED, 7)),
        new CardComponent(new Card(uno.util.Color.YELLOW, 2)),
        new CardComponent(new Card(uno.util.Color.GREEN, 8)),
        new CardComponent(new Card(uno.util.Color.BLUE, 1))
    };
    private final JButton startButton;
    private final JButton loadButton;
    private final JButton exitButton;

    public MenuPanel() {
        setLayout(new GridBagLayout());
        JPanel menu = new JPanel(new GridBagLayout()) {
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
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(24, 42, 24, 42));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(6, 0, 6, 0);

        JLabel title = new JLabel("UNO", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 86));
        title.setForeground(new Color(0xFFE078));
        menu.add(title, constraints);
        JLabel subtitle = label("Take a seat. Play your hand.", 15, new Color(0xF5EFDF));
        menu.add(subtitle, constraints);

        JPanel colours = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 6, 6));
        colours.setOpaque(false);
        for (uno.util.Color colour : uno.util.Color.getAllColors()) {
            JPanel mark = new JPanel();
            mark.setBackground(CardComponent.toAwtColor(colour));
            mark.setPreferredSize(new Dimension(32, 4));
            colours.add(mark);
        }
        menu.add(colours, constraints);
        startButton = new MenuButton("Start Game", true);
        loadButton = new MenuButton("Load Game", false);
        exitButton = new MenuButton("Exit", false);
        menu.add(startButton, constraints);
        menu.add(loadButton, constraints);
        menu.add(exitButton, constraints);
        constraints.insets = new Insets(15, 0, 0, 0);
        menu.add(label("2 to 4 players · Easy or Hard computer opponents", 12, new Color(0xB8CEC2)), constraints);
        add(menu);
    }

    private JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        background.paint(g, getWidth(), getHeight());
        if (getWidth() >= 760) {
            for (int i = 0; i < decoration.length; i++) {
                Graphics2D card = (Graphics2D) g.create();
                card.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean left = i < 2;
                int offset = i % 2;
                card.translate(getWidth() / 2.0 + (left ? -290 : 290) + offset * 30,
                        getHeight() / 2.0 - 20 + offset * 36);
                card.rotate(Math.toRadians(left ? -20 + offset * 12 : 10 + offset * 12));
                card.translate(-54, -78);
                card.setColor(new Color(0, 0, 0, 45));
                card.fillRoundRect(5, 7, 108, 156, 14, 14);
                decoration[i].setSize(108, 156);
                decoration[i].printAll(card);
                card.dispose();
            }
        }
        g.dispose();
    }

    static class MenuButton extends JButton {
        private final boolean primary;

        MenuButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setPreferredSize(new Dimension(260, 48));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color base = primary ? new Color(0xE4B94E) : new Color(0x254E40);
            if (getModel().isRollover() || hasFocus()) { base = base.brighter(); }
            if (getModel().isPressed()) { base = base.darker(); }
            g.setPaint(new GradientPaint(0, 0, base, 0, getHeight(), base.darker()));
            g.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 14, 14);
            g.setColor(hasFocus() ? Color.WHITE : new Color(255, 255, 255, 65));
            g.setStroke(new BasicStroke(hasFocus() ? 2 : 1));
            g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 14, 14);
            g.setColor(primary ? new Color(0x302710) : new Color(0xF5EFDF));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            java.awt.FontMetrics metrics = g.getFontMetrics();
            g.drawString(getText(), (getWidth() - metrics.stringWidth(getText())) / 2,
                    (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
            g.dispose();
        }
    }

    public JButton getStartButton() { return startButton; }
    public JButton getLoadButton() { return loadButton; }
    public JButton getExitButton() { return exitButton; }
}
