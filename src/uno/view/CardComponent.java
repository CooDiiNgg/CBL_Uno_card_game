package uno.view;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import uno.model.Card;
import uno.util.Color;
import uno.util.Value;

public class CardComponent extends JButton {
    private Card card;

    public CardComponent(Card card) {
        super("<html><center>" + Value.fromValue(card.getValue()).name().replace('_', ' ') + "</center></html>");
        this.card = card;
        setBackground(toAwtColor(card.getColor()));
        setForeground(card.getColor() == Color.YELLOW ? java.awt.Color.BLACK : java.awt.Color.WHITE);
        setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        setPreferredSize(new Dimension(90, 130));
        setFocusPainted(false);
        setOpaque(true);
        setPlayable(false);
        setToolTipText(card.toString());
    }

    public Card getCard() {
        return card;
    }

    public void setPlayable(boolean playable) {
        if (playable) {
            setBorder(BorderFactory.createLineBorder(java.awt.Color.ORANGE, 4));
        } else {
            setBorder(BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY, 1));
        }
    }

    private static java.awt.Color toAwtColor(Color color) {
        switch (color) {
            case RED:
                return new java.awt.Color(0xD72600);
            case BLUE:
                return new java.awt.Color(0x0956BF);
            case YELLOW:
                return new java.awt.Color(0xECD407);
            case GREEN:
                return new java.awt.Color(0x379711);
            default:
                return java.awt.Color.BLACK;
        }
    }
}
