package uno.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Queue;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import uno.model.Card;
import uno.model.GameState;
import uno.model.Player;

public class GamePanel extends JPanel {
    private final TableBackground tableBackground = new TableBackground();
    private JPanel opponentHandPanel;
    private CardComponent discardComponent;
    private final Queue<Runnable> animationQueue = new ArrayDeque<>();
    private Timer animationTimer;
    private Runnable animationCompletion;
    private BufferedImage movingCard;
    private Point movingPosition;
    private boolean animating;
    private Player viewer;
    private static final int ANIMATION_MS = 320;
    private JLabel opponentLabel;
    private JLabel turnLabel;
    private JLabel statusLabel;
    private JPanel tablePanel;
    private FanHandPanel handPanel;
    private CardBackComponent drawButton;
    private Timer turnCueTimer;
    private float turnCueProgress = -1;
    private JButton saveButton;
    private JButton menuButton;
    private ActionListener cardActionListener;

    public GamePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(27, 30, 23, 30));

        opponentLabel = new JLabel(" ");
        turnLabel = new JLabel(" ");
        styleTableLabel(opponentLabel);
        styleTableLabel(turnLabel);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(opponentLabel, BorderLayout.WEST);
        topPanel.add(turnLabel, BorderLayout.EAST);
        opponentHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        JScrollPane opponentScroll = new JScrollPane(opponentHandPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        opponentScroll.setPreferredSize(new Dimension(0, 100));
        JPanel northPanel = new JPanel(new BorderLayout(0, 8));
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(opponentScroll, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        drawButton = new CardBackComponent(true);
        tablePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 4));
        add(tablePanel, BorderLayout.CENTER);

        handPanel = new FanHandPanel();
        JScrollPane handScrollPane = new JScrollPane(handPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        handScrollPane.setPreferredSize(new Dimension(0, 205));

        statusLabel = new JLabel(" ");
        styleTableLabel(statusLabel);
        saveButton = new JButton("Save");
        menuButton = new JButton("Menu");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(menuButton);
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(handScrollPane, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
        for (JPanel panel : new JPanel[]{topPanel, northPanel, opponentHandPanel,
                tablePanel, handPanel, buttonPanel, statusPanel, bottomPanel}) {
            panel.setOpaque(false);
        }
        for (JScrollPane scroll : new JScrollPane[]{opponentScroll, handScrollPane}) {
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.setViewportBorder(null);
            // Transparent viewports must repaint the felt instead of copying old pixels.
            scroll.getViewport().setScrollMode(javax.swing.JViewport.SIMPLE_SCROLL_MODE);
        }
        for (JButton button : new JButton[]{saveButton, menuButton}) {
            button.setBackground(new java.awt.Color(0x173C32));
            button.setForeground(new java.awt.Color(0xF4EBD2));
            button.setOpaque(true);
            button.setFocusPainted(true);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new java.awt.Color(0x8F956C)),
                    BorderFactory.createEmptyBorder(6, 17, 6, 17)));
        }
    }

    private void styleTableLabel(JLabel label) {
        label.setForeground(new java.awt.Color(0xF5EEDB));
        label.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 12));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        tableBackground.paint((java.awt.Graphics2D) graphics, getWidth(), getHeight());
    }

    public void refresh(GameState gameState, Player viewer, List<Card> validMoves, boolean isViewerTurn) {
        this.viewer = viewer;
        if (!isViewerTurn) {
            hideTurnCue();
        }
        drawButton.setRemaining(gameState.getDeck().size());
        opponentHandPanel.removeAll();
        StringBuilder opponents = new StringBuilder();
        for (Player player : gameState.getPlayers()) {
            if (player != viewer) {
                for (int i = 0; i < player.getHand().size(); i++) {
                    JButton back = createCardBack();
                    back.setPreferredSize(new Dimension(60, 85));
                    opponentHandPanel.add(back);
                }
                if (opponents.length() > 0) {
                    opponents.append("    ");
                }
                opponents.append(player.getName()).append(": ").append(player.getHand().size()).append(" cards");
            }
        }
        opponentLabel.setText(opponents.toString());
        turnLabel.setText("Turn: " + gameState.getCurrentPlayer().getName());

        tablePanel.removeAll();
        tablePanel.add(drawButton);
        discardComponent = new CardComponent(gameState.getDiscardPile().peekTopCard());
        tablePanel.add(discardComponent);
        if (gameState.getDrawStackCount() > 0) {
            JLabel penalty = new JLabel("Draw stack: " + gameState.getDrawStackCount());
            styleTableLabel(penalty);
            tablePanel.add(penalty);
        }
        drawButton.setEnabled(isViewerTurn);

        handPanel.removeAll();
        List<Card> cards = viewer.getHand().getCards();
        for (int i = 0; i < cards.size(); i++) {
            CardComponent cardComponent = new CardComponent(cards.get(i));
            cardComponent.setActionCommand(String.valueOf(i));
            cardComponent.setEnabled(isViewerTurn);
            cardComponent.setPlayable(isViewerTurn && validMoves.contains(cards.get(i)));
            if (cardActionListener != null) {
                cardComponent.addActionListener(cardActionListener);
            }
            handPanel.addCard(cardComponent);
        }

        revalidate();
        repaint();
    }

    private JButton createCardBack() {
        return new CardBackComponent(false);
    }

    /** A non-blocking cue: the player can act as soon as the hand is ready. */
    public void showYourTurn() {
        hideTurnCue();
        turnCueProgress = 0;
        long started = System.nanoTime();
        turnCueTimer = new Timer(16, event -> {
            turnCueProgress = Math.min(1f, (System.nanoTime() - started) / 1_250_000_000f);
            if (turnCueProgress >= 1) {
                hideTurnCue();
            }
            repaint();
        });
        turnCueTimer.start();
        repaint();
    }

    private void hideTurnCue() {
        if (turnCueTimer != null) {
            turnCueTimer.stop();
        }
        turnCueProgress = -1;
        repaint();
    }

    public boolean isAnimating() {
        return animating;
    }

    private void lockInput() {
        drawButton.setEnabled(false);
        saveButton.setEnabled(false);
        for (Component card : handPanel.getComponents()) {
            card.setEnabled(false);
        }
    }

    public void animatePlay(Player player, Card card) {
        Component source = opponentHandPanel;
        if (player == viewer) {
            source = handPanel;
            for (Component component : handPanel.getComponents()) {
                if (component instanceof FanHandPanel.FanCard) {
                    Card original = ((FanHandPanel.FanCard) component).getCard();
                    if (original == card || (original.getColor() == uno.util.Color.SPECIAL
                            && original.getValue() == card.getValue())) {
                        source = component;
                        break;
                    }
                }
            }
        }
        final Component origin = source;
        enqueue(() -> moveCard(new CardComponent(card), origin, discardComponent));
    }

    public void animateDraw(Player player, int amount) {
        hideTurnCue();
        Component destination = player == viewer ? handPanel : opponentHandPanel;
        for (int i = 0; i < amount; i++) {
            enqueue(() -> moveCard(createCardBack(), drawButton, destination));
        }
    }

    // Queue visual changes so a penalty draw follows the card that caused it.
    private void enqueue(Runnable animation) {
        hideTurnCue();
        animationQueue.add(animation);
        lockInput();
        if (!animating) {
            animating = true;
            animationQueue.remove().run();
        }
    }

    public void afterAnimations(Runnable action) {
        if (animating) {
            animationCompletion = action;
        } else {
            action.run();
        }
    }

    private Point cardPosition(Component component) {
        Point center = SwingUtilities.convertPoint(component,
                component.getWidth() / 2, component.getHeight() / 2, this);
        // Keep destinations visible even when a large hand is horizontally scrolled.
        center.x = Math.max(45, Math.min(getWidth() - 45, center.x));
        center.y = Math.max(65, Math.min(getHeight() - 65, center.y));
        return new Point(center.x - 45, center.y - 65);
    }

    private void moveCard(JButton face, Component source, Component destination) {
        Point start = cardPosition(source);
        Point end = cardPosition(destination);
        face.setSize(90, 130);
        movingCard = new BufferedImage(90, 130, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = movingCard.getGraphics();
        face.printAll(graphics);
        graphics.dispose();
        movingPosition = start;
        long started = System.nanoTime();
        animationTimer = new Timer(16, event -> {
            double progress = Math.min(1.0, (System.nanoTime() - started) / (ANIMATION_MS * 1_000_000.0));
            double eased = progress * progress * (3 - 2 * progress);
            movingPosition = new Point((int) (start.x + (end.x - start.x) * eased),
                    (int) (start.y + (end.y - start.y) * eased));
            repaint();
            if (progress >= 1) {
                animationTimer.stop();
                movingCard = null;
                finishAnimation();
            }
        });
        animationTimer.start();
        repaint();
    }

    private void finishAnimation() {
        if (!animationQueue.isEmpty()) {
            animationQueue.remove().run();
        } else {
            animating = false;
            saveButton.setEnabled(true);
            Runnable completion = animationCompletion;
            animationCompletion = null;
            if (completion != null) {
                completion.run();
            }
        }
        repaint();
    }

    public void cancelAnimations() {
        hideTurnCue();
        if (animationTimer != null) {
            animationTimer.stop();
        }
        animationQueue.clear();
        animationCompletion = null;
        movingCard = null;
        animating = false;
        saveButton.setEnabled(true);
        repaint();
    }

    @Override
    protected void paintChildren(Graphics graphics) {
        super.paintChildren(graphics);
        if (turnCueProgress >= 0) {
            java.awt.Graphics2D cue = (java.awt.Graphics2D) graphics.create();
            cue.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            float alpha = Math.min(1f, Math.min(turnCueProgress / 0.15f, (1 - turnCueProgress) / 0.25f));
            cue.setComposite(java.awt.AlphaComposite.SrcOver.derive(Math.max(0, alpha)));
            int x = (getWidth() - 240) / 2;
            int y = SwingUtilities.convertPoint(handPanel, 0, 0, this).y + 3
                    - (int) (10 * (1 - Math.min(1, turnCueProgress / 0.2f)));
            cue.setColor(new java.awt.Color(0x1B2B42));
            cue.fillRoundRect(x, y, 240, 38, 24, 24);
            cue.setColor(new java.awt.Color(0xFFCC58));
            cue.setStroke(new java.awt.BasicStroke(2));
            cue.drawRoundRect(x, y, 240, 38, 24, 24);
            cue.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 17));
            String message = "YOUR TURN";
            cue.drawString(message, (getWidth() - cue.getFontMetrics().stringWidth(message)) / 2, y + 25);
            cue.dispose();
        }
        if (movingCard != null && movingPosition != null) {
            graphics.drawImage(movingCard, movingPosition.x, movingPosition.y, null);
        }
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public void setCardActionListener(ActionListener cardActionListener) {
        this.cardActionListener = cardActionListener;
    }

    public JButton getDrawButton() {
        return drawButton;
    }

    public JButton getSaveButton() {
        return saveButton;
    }

    public JButton getMenuButton() {
        return menuButton;
    }
}
