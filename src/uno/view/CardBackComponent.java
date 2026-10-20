package uno.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

/** Shared face-down artwork, optionally displayed as a clickable draw stack. */
public class CardBackComponent extends JButton {
    private final boolean drawPile;
    private int remaining;

    public CardBackComponent(boolean drawPile) {
        super(drawPile ? "Draw" : "UNO");
        this.drawPile = drawPile;
        setPreferredSize(drawPile ? new Dimension(108, 162) : new Dimension(90, 130));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setFocusable(drawPile);
        setCursor(Cursor.getPredefinedCursor(drawPile ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        setToolTipText(drawPile ? "Draw a card" : "Face-down card");
        getAccessibleContext().setAccessibleName(drawPile ? "Draw a card" : "Face-down card");
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
        setToolTipText(remaining == 0 ? "Draw — recycle the discard pile if available"
                : "Draw a card — " + remaining + " remaining");
        getAccessibleContext().setAccessibleName("Draw a card, " + remaining + " in the draw pile");
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.scale(getWidth() / (drawPile ? 108.0 : 90.0), getHeight() / (drawPile ? 162.0 : 130.0));
        if (drawPile) {
            for (int offset = 10; offset >= 4; offset -= 3) {
                g.setColor(new Color(0, 0, 0, 35));
                g.fillRoundRect(offset + 2, offset + 2, 90, 130, 12, 12);
                g.setColor(new Color(0xE1E5EA));
                g.fillRoundRect(offset, offset, 90, 130, 12, 12);
                g.setColor(new Color(0x9098A3));
                g.drawRoundRect(offset, offset, 90, 130, 12, 12);
            }
        }
        g.setColor(drawPile && isEnabled() && (getModel().isRollover() || hasFocus())
                ? new Color(0xFFCA55) : Color.WHITE);
        g.fillRoundRect(0, 0, 90, 130, 12, 12);
        g.setPaint(new GradientPaint(0, 0, new Color(0x303D50), 90, 130, new Color(0x101723)));
        g.fillRoundRect(4, 4, 82, 122, 9, 9);
        Graphics2D emblem = (Graphics2D) g.create();
        emblem.rotate(Math.toRadians(25), 45, 65);
        emblem.setPaint(new GradientPaint(10, 20, new Color(0xF34A43), 80, 110, new Color(0xB9192F)));
        emblem.fillOval(12, 17, 66, 96);
        emblem.setColor(new Color(255, 255, 255, 160));
        emblem.setStroke(new BasicStroke(1.5f));
        emblem.drawOval(12, 17, 66, 96);
        emblem.dispose();
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 28));
        int labelX = (90 - g.getFontMetrics().stringWidth("UNO")) / 2;
        g.setColor(new Color(0x641325));
        g.drawString("UNO", labelX + 2, 77);
        g.setColor(new Color(0xFFE066));
        g.drawString("UNO", labelX, 74);
        if (drawPile) {
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            g.setColor(isEnabled() ? new Color(0xF5EEDB) : new Color(0xBECBBE));
            String label = "DRAW · " + remaining;
            g.drawString(label, (108 - g.getFontMetrics().stringWidth(label)) / 2, 157);
        }
        g.dispose();
    }
}
