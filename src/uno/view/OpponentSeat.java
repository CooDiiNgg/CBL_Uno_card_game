package uno.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import javax.swing.JComponent;
import uno.model.Player;

public class OpponentSeat extends JComponent {
    static final int SEAT_WIDTH = 236;
    static final int SEAT_HEIGHT = 120;
    static final double CARD_SCALE = 0.5;
    private static final int FAN_TOP = 46;

    private final Player player;
    private final Color accent;
    private String detail;
    private int count;
    private boolean active;

    public OpponentSeat(Player player, Color accent) {
        this.player = player;
        this.accent = accent;
        setOpaque(false);
        setPreferredSize(new Dimension(SEAT_WIDTH, SEAT_HEIGHT));
    }

    public Player getPlayer() {
        return player;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        int clamped = Math.max(0, count);
        if (clamped != this.count) {
            this.count = clamped;
            setToolTipText(player.getName() + " has " + clamped + (clamped == 1 ? " card" : " cards"));
            repaint();
        }
    }

    public void setDetail(String detail) {
        this.detail = detail;
        repaint();
    }

    public void setActive(boolean active) {
        if (active != this.active) {
            this.active = active;
            repaint();
        }
    }

    public Point2D getCardCenter() {
        return new Point2D.Double(getWidth() / 2.0, FAN_TOP + 65 * CARD_SCALE);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        if (active) {
            g.setColor(new Color(255, 204, 88, 45));
            g.fillRoundRect(0, 0, width, height, 26, 26);
        }
        g.setColor(new Color(6, 30, 27, 195));
        g.fillRoundRect(3, 3, width - 6, height - 6, 22, 22);
        g.setStroke(new BasicStroke(active ? 2f : 1f));
        g.setColor(active ? new Color(0xFFCC58) : new Color(222, 196, 120, 80));
        g.drawRoundRect(3, 3, width - 7, height - 7, 22, 22);

        g.setColor(accent);
        g.fillOval(16, 15, 10, 10);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g.setColor(new Color(0xF5EEDB));
        g.drawString(player.getName(), 33, 25);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(new Color(0xB8CEC2));
        g.drawString(count + (count == 1 ? " card" : " cards"), 33, 40);

        if (detail != null) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
            FontMetrics metrics = g.getFontMetrics();
            String text = detail.toUpperCase();
            int pillWidth = metrics.stringWidth(text) + 16;
            int pillX = width - pillWidth - 14;
            g.setColor(new Color(255, 255, 255, 28));
            g.fillRoundRect(pillX, 12, pillWidth, 18, 18, 18);
            g.setColor(new Color(0xFFE078));
            g.drawString(text, pillX + 8, 25);
        }

        double cardWidth = 90 * CARD_SCALE;
        double cardHeight = 130 * CARD_SCALE;
        double available = width - 40 - cardWidth;
        double spacing = count < 2 ? 0 : Math.min(17, available / (count - 1));
        double total = cardWidth + spacing * Math.max(0, count - 1);
        double left = (width - total) / 2;
        double spread = count < 2 ? 0 : Math.min(3, 24.0 / count);
        for (int i = 0; i < count; i++) {
            Graphics2D card = (Graphics2D) g.create();
            card.translate(left + i * spacing + cardWidth / 2, FAN_TOP + cardHeight / 2);
            card.rotate(Math.toRadians((i - (count - 1) / 2.0) * spread));
            card.scale(CARD_SCALE, CARD_SCALE);
            card.translate(-45, -65);
            card.setColor(new Color(0, 0, 0, 50));
            card.fillRoundRect(3, 4, 90, 130, 12, 12);
            CardBackComponent.paintBack(card, false);
            card.dispose();
        }
        g.dispose();
    }
}
