package uno.view;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import uno.model.Card;

/** A spaced overlapping hand with a gentle arc and raised selection. */
public class FanHandPanel extends JPanel {
    private static final int CARD_SPACING = 65;

    public FanHandPanel() {
        setLayout(null);
    }

    public void addCard(CardComponent face) {
        // Swing paints index zero last, so the rightmost card sits on top.
        add(new FanCard(face), 0);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(170 + Math.max(0, getComponentCount() - 1) * CARD_SPACING, 205);
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    @Override
    public void doLayout() {
        int count = getComponentCount();
        double gap = count < 2 ? 0 : CARD_SPACING;
        double total = 160 + Math.max(0, count - 1) * gap;
        double left = (getWidth() - total) / 2;
        for (int z = 0; z < count; z++) {
            int index = count - 1 - z;
            double fraction = count < 2 ? 0 : (2.0 * index / (count - 1)) - 1;
            FanCard card = (FanCard) getComponent(z);
            card.angle = Math.toRadians(fraction * 4);
            card.setBounds((int) (left + index * gap), (int) (Math.abs(fraction) * 5), 160, 200);
        }
    }

    static class FanCard extends JComponent {
        private final CardComponent face;
        private double angle;
        private boolean hovered;

        FanCard(CardComponent face) {
            this.face = face;
            face.setSize(90, 130);
            setEnabled(face.isEnabled());
            setFocusable(true);
            setToolTipText(face.getToolTipText());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaintHand(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaintHand(); }
                @Override public void mouseClicked(MouseEvent e) {
                    if (isEnabled() && e.getButton() == MouseEvent.BUTTON1) {
                        requestFocusInWindow();
                        face.doClick(0);
                    }
                }
            });
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("SPACE"), "play");
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "play");
            getActionMap().put("play", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (isEnabled()) { face.doClick(0); }
                }
            });
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) { repaintHand(); }
                @Override public void focusLost(java.awt.event.FocusEvent e) { repaintHand(); }
            });
        }

        Card getCard() { return face.getCard(); }

        private void repaintHand() {
            if (getParent() != null) { getParent().repaint(); }
        }

        private AffineTransform cardTransform(boolean lifted) {
            AffineTransform transform = new AffineTransform();
            transform.translate(80, 108 - (lifted ? 18 : 0));
            transform.rotate(angle);
            transform.translate(-45, -65);
            return transform;
        }

        @Override
        public boolean contains(int x, int y) {
            try {
                // Include the resting footprint to avoid hover flicker when lifted.
                for (boolean lifted : new boolean[]{false, true}) {
                    if (lifted && !(hovered || hasFocus()) ) { continue; }
                    Point2D point = cardTransform(lifted).inverseTransform(new Point2D.Double(x, y), null);
                    if (new RoundRectangle2D.Double(0, 0, 90, 130, 12, 12).contains(point)) { return true; }
                }
            } catch (NoninvertibleTransformException e) {
                return false;
            }
            return false;
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (face != null) { face.setEnabled(enabled); }
            if (!enabled) { hovered = false; }
            repaintHand();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.transform(cardTransform(isEnabled() && (hovered || hasFocus())));
            g.setColor(new java.awt.Color(0, 0, 0, 45));
            g.fillRoundRect(3, 4, 90, 130, 12, 12);
            g.clip(new RoundRectangle2D.Double(0, 0, 90, 130, 12, 12));
            face.printAll(g);
            g.dispose();
        }
    }
}
