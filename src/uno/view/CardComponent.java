package uno.view;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import javax.swing.JButton;
import uno.model.Card;
import uno.util.Color;

/** Scalable card artwork shared by the hand, discard pile, and animations. */
public class CardComponent extends JButton {
    private final Card card;
    private boolean playable;

    public CardComponent(Card card) {
        this.card = card;
        setPreferredSize(new Dimension(90, 130));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setToolTipText(card.toString());
        getAccessibleContext().setAccessibleName(card.toString());
    }

    public Card getCard() { return card; }

    public void setPlayable(boolean playable) {
        this.playable = playable;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.scale(getWidth() / 90.0, getHeight() / 130.0);
        g.setColor(playable && isEnabled() ? new java.awt.Color(0xFFCA55) : java.awt.Color.WHITE);
        g.fillRoundRect(0, 0, 90, 130, 12, 12);
        g.setColor(toAwtColor(card.getColor()));
        g.fillRoundRect(4, 4, 82, 122, 9, 9);
        Shape oval = AffineTransform.getRotateInstance(Math.toRadians(28), 45, 65)
                .createTransformedShape(new Ellipse2D.Double(14, 19, 62, 92));
        if (card.getColor() == Color.SPECIAL) {
            Shape oldClip = g.getClip();
            g.clip(oval);
            Color[] colors = {Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN};
            for (int i = 0; i < 4; i++) {
                g.setColor(toAwtColor(colors[i]));
                g.fillRect((i % 2) * 45, (i / 2) * 65, 45, 65);
            }
            g.setClip(oldClip);
        }
        g.setStroke(new BasicStroke(2));
        g.setColor(new java.awt.Color(255, 255, 255, 210));
        g.draw(oval);
        String symbol = symbol();
        float size = card.getValue() <= 9 ? 64 : symbol.length() > 2 ? 23 : 43;
        Font font = new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, (int) size);
        GlyphVector glyphs = font.createGlyphVector(g.getFontRenderContext(), symbol);
        java.awt.geom.Rectangle2D bounds = glyphs.getVisualBounds();
        Shape number = glyphs.getOutline((float) (45 - bounds.getCenterX()), (float) (66 - bounds.getCenterY()));
        g.setColor(new java.awt.Color(0, 0, 0, 200));
        g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(number);
        g.setColor(java.awt.Color.WHITE);
        g.fill(number);
        paintCorner(g, symbol);
        g.rotate(Math.PI, 45, 65);
        paintCorner(g, symbol);
        g.dispose();
    }

    private void paintCorner(Graphics2D g, String symbol) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, symbol.length() > 2 ? 9 : 16));
        g.setColor(java.awt.Color.WHITE);
        g.drawString(symbol, 8, 21);
    }

    private String symbol() {
        switch (card.getValue()) {
            case 10: return "⊘";
            case 11: return "⇄";
            case 12: return "+2";
            case 13: return "WILD";
            case 14: return "+4";
            default: return String.valueOf(card.getValue());
        }
    }

    public static java.awt.Color toAwtColor(Color color) {
        switch (color) {
            case RED: return new java.awt.Color(0xDC3439);
            case BLUE: return new java.awt.Color(0x1672C4);
            case YELLOW: return new java.awt.Color(0xEABF16);
            case GREEN: return new java.awt.Color(0x289447);
            default: return new java.awt.Color(0x202530);
        }
    }
}
