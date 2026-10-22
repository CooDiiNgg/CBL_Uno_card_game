package uno.view;

import java.awt.Component;
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
import java.util.IdentityHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import uno.model.Card;

public class FanHandPanel extends JPanel {
    private static final int CARD_SPACING = 65;
    private static final int SLOT_WIDTH = 160;
    private static final int SLOT_HEIGHT = 200;
    private static final double GLIDE_TIME_MS = 55;
    private static final double LIFT_TIME_MS = 35;
    private static final double LIFT_HEIGHT = 18;

    private final Map<Card, double[]> previousPoses = new IdentityHashMap<>();
    private final Timer motionTimer;
    private long lastFrame;
    private int reservedSlots;

    public FanHandPanel() {
        setLayout(null);
        motionTimer = new Timer(1000 / 60, event -> stepMotion());
        motionTimer.setCoalesce(true);
    }

    public void addCard(CardComponent face) {
        FanCard card = new FanCard(face);
        double[] pose = previousPoses.remove(face.getCard());
        if (pose != null) {
            card.x = pose[0];
            card.y = pose[1];
            card.angle = pose[2];
            card.lift = pose[3];
            card.placed = true;
        }
        add(card, 0);
    }

    public void clearCards() {
        previousPoses.clear();
        for (Component component : getComponents()) {
            FanCard card = (FanCard) component;
            if (card.placed) {
                previousPoses.put(card.getCard(), new double[]{card.x, card.y, card.angle, card.lift});
            }
        }
        removeAll();
        reservedSlots = 0;
    }

    public int reserveSlots(int extra) {
        int firstSlot = getComponentCount() + reservedSlots;
        reservedSlots += Math.max(0, extra);
        revalidate();
        doLayout();
        return firstSlot;
    }

    public double[] detachCard(Card card) {
        for (Component component : getComponents()) {
            FanCard fanCard = (FanCard) component;
            Card original = fanCard.getCard();
            if (original == card || (original.getColor() == uno.util.Color.SPECIAL
                    && original.getValue() == card.getValue())) {
                double[] pose = fanCard.centerPose();
                remove(fanCard);
                revalidate();
                doLayout();
                repaint();
                return pose;
            }
        }
        return null;
    }

    public double[] slotPose(int index) {
        int count = getComponentCount() + reservedSlots;
        double gap = count < 2 ? 0 : CARD_SPACING;
        double total = SLOT_WIDTH + Math.max(0, count - 1) * gap;
        double left = (getWidth() - total) / 2;
        double fraction = count < 2 ? 0 : (2.0 * index / (count - 1)) - 1;
        return new double[]{left + index * gap + 80, Math.abs(fraction) * 5 + 108, Math.toRadians(fraction * 4)};
    }

    public boolean isMoving() {
        return motionTimer.isRunning();
    }

    @Override
    public Dimension getPreferredSize() {
        int count = getComponentCount() + reservedSlots;
        return new Dimension(170 + Math.max(0, count - 1) * CARD_SPACING, 205);
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    @Override
    public void doLayout() {
        int count = getComponentCount();
        boolean moving = false;
        for (int z = 0; z < count; z++) {
            FanCard card = (FanCard) getComponent(z);
            double[] slot = slotPose(count - 1 - z);
            card.targetX = slot[0] - 80;
            card.targetY = slot[1] - 108;
            card.targetAngle = slot[2];
            if (!card.placed) {
                card.x = card.targetX;
                card.y = card.targetY;
                card.angle = card.targetAngle;
                card.placed = true;
            }
            card.applyBounds();
            moving |= !card.isSettled();
        }
        if (moving) {
            startMotion();
        }
    }

    private void startMotion() {
        if (!motionTimer.isRunning()) {
            lastFrame = System.nanoTime();
            motionTimer.start();
        }
    }

    private void stepMotion() {
        long now = System.nanoTime();
        double elapsed = Math.min(50, (now - lastFrame) / 1_000_000.0);
        lastFrame = now;
        double glide = 1 - Math.exp(-elapsed / GLIDE_TIME_MS);
        double rise = 1 - Math.exp(-elapsed / LIFT_TIME_MS);
        boolean moving = false;
        for (Component component : getComponents()) {
            FanCard card = (FanCard) component;
            card.x = approach(card.x, card.targetX, glide, 0.2);
            card.y = approach(card.y, card.targetY, glide, 0.2);
            card.angle = approach(card.angle, card.targetAngle, glide, 0.0005);
            card.lift = approach(card.lift, card.wantsLift() ? 1 : 0, rise, 0.01);
            card.applyBounds();
            moving |= !card.isSettled();
        }
        repaint();
        if (!moving) {
            motionTimer.stop();
        }
    }

    private static double approach(double value, double target, double amount, double snap) {
        double next = value + (target - value) * amount;
        return Math.abs(target - next) < snap ? target : next;
    }

    static class FanCard extends JComponent {
        private final CardComponent face;
        private double x;
        private double y;
        private double angle;
        private double lift;
        private double targetX;
        private double targetY;
        private double targetAngle;
        private boolean placed;
        private boolean hovered;

        FanCard(CardComponent face) {
            this.face = face;
            face.setSize(90, 130);
            setEnabled(face.isEnabled());
            setFocusable(true);
            setToolTipText(face.getToolTipText());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; animate(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; animate(); }
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
                @Override public void focusGained(java.awt.event.FocusEvent e) { animate(); }
                @Override public void focusLost(java.awt.event.FocusEvent e) { animate(); }
            });
        }

        Card getCard() { return face.getCard(); }

        private boolean wantsLift() {
            return isEnabled() && (hovered || hasFocus());
        }

        private boolean isSettled() {
            return x == targetX && y == targetY && angle == targetAngle && lift == (wantsLift() ? 1 : 0);
        }

        private void animate() {
            if (getParent() instanceof FanHandPanel) {
                ((FanHandPanel) getParent()).startMotion();
            }
        }

        private void applyBounds() {
            setBounds((int) Math.floor(x), (int) Math.floor(y), SLOT_WIDTH, SLOT_HEIGHT);
        }

        private double[] centerPose() {
            return new double[]{x + 80, y + 108 - LIFT_HEIGHT * lift, angle};
        }

        private AffineTransform cardTransform(double liftAmount) {
            AffineTransform transform = new AffineTransform();
            transform.translate(80 + (x - Math.floor(x)), 108 + (y - Math.floor(y)) - LIFT_HEIGHT * liftAmount);
            transform.rotate(angle);
            transform.translate(-45, -65);
            return transform;
        }

        @Override
        public boolean contains(int px, int py) {
            try {
                for (double liftAmount : new double[]{0, lift}) {
                    Point2D point = cardTransform(liftAmount).inverseTransform(new Point2D.Double(px, py), null);
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
            animate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.transform(cardTransform(lift));
            g.setColor(new java.awt.Color(0, 0, 0, 45 + (int) (25 * lift)));
            g.fillRoundRect(3, 4 + (int) Math.round(4 * lift), 90, 130, 12, 12);
            CardComponent.paintCard(g, face.getCard(), face.isHighlighted());
            g.dispose();
        }
    }
}
