package uno.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import uno.model.Card;
import uno.model.GameState;
import uno.model.Player;

public class GamePanel extends JPanel {
    private static final int FRAME_MS = 1000 / 60;
    private static final int PLAY_MS = 430;
    private static final int DRAW_MS = 400;
    private static final int DRAW_STAGGER_MS = 120;
    private static final int CUE_WIDTH = 240;
    private static final int CUE_HEIGHT = 38;

    private final TableBackground tableBackground = new TableBackground();
    private final Queue<Runnable> animationQueue = new ArrayDeque<>();
    private final List<Flight> flights = new ArrayList<>();
    private final List<Flight> parkedFlights = new ArrayList<>();
    private final Map<Player, OpponentSeat> seats = new LinkedHashMap<>();
    private Map<Player, String> seatDetails = new HashMap<>();
    private final Timer animationTimer;
    private Runnable animationCompletion;
    private boolean animating;
    private Player viewer;
    private JLabel directionLabel;
    private JLabel turnLabel;
    private JLabel statusLabel;
    private JLabel penaltyLabel;
    private JPanel seatPanel;
    private JPanel tablePanel;
    private FanHandPanel handPanel;
    private CardBackComponent drawButton;
    private CardComponent discardComponent;
    private Timer turnCueTimer;
    private float turnCueProgress = -1;
    private Rectangle turnCueBounds;
    private JButton saveButton;
    private JButton menuButton;
    private ActionListener cardActionListener;

    public GamePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(27, 30, 23, 30));

        directionLabel = new JLabel(" ");
        turnLabel = new JLabel(" ");
        styleTableLabel(directionLabel);
        styleTableLabel(turnLabel);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(directionLabel, BorderLayout.WEST);
        topPanel.add(turnLabel, BorderLayout.EAST);
        seatPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        seatPanel.setPreferredSize(new Dimension(0, OpponentSeat.SEAT_HEIGHT));
        JPanel northPanel = new JPanel(new BorderLayout(0, 6));
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(seatPanel, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        drawButton = new CardBackComponent(true);
        penaltyLabel = new JLabel(" ");
        styleTableLabel(penaltyLabel);
        penaltyLabel.setVisible(false);
        tablePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 4));
        tablePanel.add(drawButton);
        tablePanel.add(penaltyLabel);
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
        for (JPanel panel : new JPanel[]{topPanel, northPanel, seatPanel,
                tablePanel, handPanel, buttonPanel, statusPanel, bottomPanel}) {
            panel.setOpaque(false);
        }
        handScrollPane.setOpaque(false);
        handScrollPane.getViewport().setOpaque(false);
        handScrollPane.setBorder(BorderFactory.createEmptyBorder());
        handScrollPane.setViewportBorder(null);
        handScrollPane.getViewport().setScrollMode(javax.swing.JViewport.SIMPLE_SCROLL_MODE);
        for (JButton button : new JButton[]{saveButton, menuButton}) {
            button.setBackground(new java.awt.Color(0x173C32));
            button.setForeground(new java.awt.Color(0xF4EBD2));
            button.setOpaque(true);
            button.setFocusPainted(true);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new java.awt.Color(0x8F956C)),
                    BorderFactory.createEmptyBorder(6, 17, 6, 17)));
        }

        animationTimer = new Timer(FRAME_MS, event -> stepFlights());
        animationTimer.setCoalesce(true);
    }

    private void styleTableLabel(JLabel label) {
        label.setForeground(new java.awt.Color(0xF5EEDB));
        label.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 12));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        tableBackground.paint((Graphics2D) graphics, getWidth(), getHeight());
    }

    public void setSeatDetails(Map<Player, String> seatDetails) {
        this.seatDetails = seatDetails == null ? new HashMap<>() : new HashMap<>(seatDetails);
    }

    public void refresh(GameState gameState, Player viewer, List<Card> validMoves, boolean isViewerTurn) {
        this.viewer = viewer;
        if (!isViewerTurn) {
            hideTurnCue();
        }
        parkedFlights.clear();
        drawButton.setRemaining(gameState.getDeck().size());
        updateSeats(gameState, viewer);
        directionLabel.setText("Direction: " + (gameState.isClockwise() ? "clockwise" : "counter-clockwise"));
        turnLabel.setText("Turn: " + gameState.getCurrentPlayer().getName());

        Card topCard = gameState.getDiscardPile().peekTopCard();
        if (discardComponent == null) {
            discardComponent = new CardComponent(topCard);
            tablePanel.add(discardComponent, 1);
        } else {
            discardComponent.setCard(topCard);
        }
        penaltyLabel.setText("Draw stack: " + gameState.getDrawStackCount());
        penaltyLabel.setVisible(gameState.getDrawStackCount() > 0);
        drawButton.setEnabled(isViewerTurn);

        handPanel.clearCards();
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

    private void updateSeats(GameState gameState, Player viewer) {
        Player[] players = gameState.getPlayers();
        boolean rebuild = seats.size() != players.length - 1;
        for (Player player : players) {
            if (player != viewer && !seats.containsKey(player)) {
                rebuild = true;
            }
        }
        if (rebuild) {
            seats.clear();
            seatPanel.removeAll();
            int viewerIndex = 0;
            for (int i = 0; i < players.length; i++) {
                if (players[i] == viewer) {
                    viewerIndex = i;
                }
            }
            uno.util.Color[] accents = uno.util.Color.getAllColors();
            for (int offset = 1; offset < players.length; offset++) {
                int index = (viewerIndex + offset) % players.length;
                OpponentSeat seat = new OpponentSeat(players[index], CardComponent.toAwtColor(accents[index % accents.length]));
                seats.put(players[index], seat);
                seatPanel.add(seat);
            }
            seatPanel.revalidate();
        }
        for (OpponentSeat seat : seats.values()) {
            seat.setCount(seat.getPlayer().getHand().size());
            seat.setActive(seat.getPlayer() == gameState.getCurrentPlayer());
            seat.setDetail(seatDetails.get(seat.getPlayer()));
        }
    }

    public void showYourTurn() {
        hideTurnCue();
        turnCueProgress = 0;
        long started = System.nanoTime();
        turnCueTimer = new Timer(FRAME_MS, event -> {
            turnCueProgress = Math.min(1f, (System.nanoTime() - started) / 1_250_000_000f);
            repaintTurnCue();
            if (turnCueProgress >= 1) {
                hideTurnCue();
            }
        });
        turnCueTimer.setCoalesce(true);
        turnCueTimer.start();
        repaintTurnCue();
    }

    private void hideTurnCue() {
        if (turnCueTimer != null) {
            turnCueTimer.stop();
            turnCueTimer = null;
        }
        if (turnCueProgress >= 0) {
            turnCueProgress = -1;
            repaintTurnCue();
        }
    }

    private void repaintTurnCue() {
        Rectangle bounds = turnCueBounds();
        if (turnCueBounds != null) {
            repaint(turnCueBounds);
        }
        repaint(bounds);
        turnCueBounds = bounds;
    }

    private Rectangle turnCueBounds() {
        int x = (getWidth() - CUE_WIDTH) / 2;
        int y = SwingUtilities.convertPoint(handPanel, 0, 0, this).y + 3 - 10;
        return new Rectangle(x - 3, y - 3, CUE_WIDTH + 7, CUE_HEIGHT + 17);
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
        enqueue(() -> {
            Flight flight;
            Point2D end = centerOf(discardComponent, 45, 65);
            if (player == viewer) {
                double[] pose = handPanel.detachCard(card);
                Point2D start = pose == null ? centerOf(handPanel, handPanel.getWidth() / 2.0, 108)
                        : centerOf(handPanel, pose[0], pose[1]);
                boolean wild = card.getValue() >= 13;
                Card handFace = wild ? new Card(uno.util.Color.SPECIAL, card.getValue()) : card;
                flight = new Flight(handFace, card, wild, start, pose == null ? 0 : pose[2], 1.0, end, 0, 1.0, PLAY_MS, 0);
            } else {
                OpponentSeat seat = seats.get(player);
                Point2D start = end;
                if (seat != null) {
                    seat.setCount(seat.getCount() - 1);
                    start = centerOf(seat, seat.getCardCenter().getX(), seat.getCardCenter().getY());
                }
                flight = new Flight(null, card, true, start, 0, OpponentSeat.CARD_SCALE, end, 0, 1.0, PLAY_MS, 0);
            }
            flight.onLand = () -> discardComponent.setCard(card);
            List<Flight> step = new ArrayList<>();
            step.add(flight);
            startStep(step);
        });
    }

    public void animateDraw(Player player, int amount) {
        hideTurnCue();
        if (amount <= 0) {
            return;
        }
        List<Card> handCards = player.getHand().getCards();
        List<Card> drawnCards = new ArrayList<>(handCards.subList(Math.max(0, handCards.size() - amount), handCards.size()));
        enqueue(() -> {
            List<Flight> step = new ArrayList<>();
            Point2D start = centerOf(drawButton, 45 * drawButton.getWidth() / 108.0, 65 * drawButton.getHeight() / 162.0);
            if (player == viewer) {
                int firstSlot = handPanel.reserveSlots(drawnCards.size());
                for (int i = 0; i < drawnCards.size(); i++) {
                    double[] slot = handPanel.slotPose(firstSlot + i);
                    Flight flight = new Flight(null, drawnCards.get(i), true, start, 0, 1.0,
                            centerOf(handPanel, slot[0], slot[1]), slot[2], 1.0, DRAW_MS, i * DRAW_STAGGER_MS);
                    flight.parkAfterLanding = true;
                    flight.onStart = () -> drawButton.setRemaining(Math.max(0, drawButton.getRemaining() - 1));
                    step.add(flight);
                }
            } else {
                OpponentSeat seat = seats.get(player);
                Point2D end = seat == null ? start
                        : centerOf(seat, seat.getCardCenter().getX(), seat.getCardCenter().getY());
                for (int i = 0; i < amount; i++) {
                    Flight flight = new Flight(null, null, false, start, 0, 1.0, end, 0,
                            OpponentSeat.CARD_SCALE, DRAW_MS, i * DRAW_STAGGER_MS);
                    flight.onStart = () -> drawButton.setRemaining(Math.max(0, drawButton.getRemaining() - 1));
                    if (seat != null) {
                        flight.onLand = () -> seat.setCount(seat.getCount() + 1);
                    }
                    step.add(flight);
                }
            }
            startStep(step);
        });
    }

    private Point2D centerOf(Component component, double x, double y) {
        Point2D.Double point = new Point2D.Double(x, y);
        Component current = component;
        while (current != null && current != this) {
            point.x += current.getX();
            point.y += current.getY();
            current = current.getParent();
        }
        point.x = Math.max(45, Math.min(getWidth() - 45, point.x));
        point.y = Math.max(65, Math.min(getHeight() - 65, point.y));
        return point;
    }

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

    private void startStep(List<Flight> step) {
        if (step.isEmpty()) {
            finishStep();
            return;
        }
        long now = System.nanoTime();
        for (Flight flight : step) {
            flight.beginNanos = now + flight.delayMs * 1_000_000L;
        }
        flights.addAll(step);
        stepFlights();
        if (!flights.isEmpty() && !animationTimer.isRunning()) {
            animationTimer.start();
        }
    }

    private void stepFlights() {
        long now = System.nanoTime();
        Rectangle dirty = null;
        boolean finished = true;
        for (Flight flight : flights) {
            if (flight.landed) {
                continue;
            }
            if (now < flight.beginNanos) {
                finished = false;
                continue;
            }
            if (!flight.started) {
                flight.started = true;
                if (flight.onStart != null) {
                    flight.onStart.run();
                }
            }
            flight.progress = Math.min(1.0, (now - flight.beginNanos) / (flight.durationMs * 1_000_000.0));
            Rectangle bounds = flight.bounds();
            dirty = union(union(dirty, flight.lastBounds), bounds);
            flight.lastBounds = bounds;
            if (flight.progress >= 1) {
                flight.landed = true;
                if (flight.parkAfterLanding) {
                    parkedFlights.add(flight);
                }
                if (flight.onLand != null) {
                    flight.onLand.run();
                }
            } else {
                finished = false;
            }
        }
        if (dirty != null) {
            repaint(dirty);
        }
        if (finished) {
            flights.clear();
            animationTimer.stop();
            finishStep();
        }
    }

    private static Rectangle union(Rectangle first, Rectangle second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.union(second);
    }

    private void finishStep() {
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
    }

    public void cancelAnimations() {
        hideTurnCue();
        animationTimer.stop();
        animationQueue.clear();
        flights.clear();
        parkedFlights.clear();
        animationCompletion = null;
        animating = false;
        saveButton.setEnabled(true);
        repaint();
    }

    @Override
    protected void paintChildren(Graphics graphics) {
        super.paintChildren(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Flight flight : parkedFlights) {
            flight.paint(g);
        }
        for (Flight flight : flights) {
            if (flight.started && !flight.landed) {
                flight.paint(g);
            }
        }
        if (turnCueProgress >= 0) {
            paintTurnCue(g);
        }
        g.dispose();
        if (!flights.isEmpty() || turnCueProgress >= 0 || handPanel.isMoving()) {
            Toolkit.getDefaultToolkit().sync();
        }
    }

    private void paintTurnCue(Graphics2D graphics) {
        Graphics2D cue = (Graphics2D) graphics.create();
        cue.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        float alpha = Math.min(1f, Math.min(turnCueProgress / 0.15f, (1 - turnCueProgress) / 0.25f));
        cue.setComposite(java.awt.AlphaComposite.SrcOver.derive(Math.max(0, alpha)));
        int x = (getWidth() - CUE_WIDTH) / 2;
        int y = SwingUtilities.convertPoint(handPanel, 0, 0, this).y + 3
                - (int) (10 * (1 - Math.min(1, turnCueProgress / 0.2f)));
        cue.setColor(new java.awt.Color(0x1B2B42));
        cue.fillRoundRect(x, y, CUE_WIDTH, CUE_HEIGHT, 24, 24);
        cue.setColor(new java.awt.Color(0xFFCC58));
        cue.setStroke(new java.awt.BasicStroke(2));
        cue.drawRoundRect(x, y, CUE_WIDTH, CUE_HEIGHT, 24, 24);
        cue.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 17));
        String message = "YOUR TURN";
        cue.drawString(message, (getWidth() - cue.getFontMetrics().stringWidth(message)) / 2, y + 25);
        cue.dispose();
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

    private static class Flight {
        private final Card firstFace;
        private final Card secondFace;
        private final boolean flips;
        private final double startX;
        private final double startY;
        private final double startAngle;
        private final double startScale;
        private final double endX;
        private final double endY;
        private final double endAngle;
        private final double endScale;
        private final double arcX;
        private final double arcY;
        private final int durationMs;
        private final int delayMs;
        private long beginNanos;
        private double progress;
        private boolean started;
        private boolean landed;
        private boolean parkAfterLanding;
        private Rectangle lastBounds;
        private Runnable onStart;
        private Runnable onLand;

        private Flight(Card firstFace, Card secondFace, boolean flips, Point2D start, double startAngle,
                       double startScale, Point2D end, double endAngle, double endScale, int durationMs, int delayMs) {
            this.firstFace = firstFace;
            this.secondFace = secondFace;
            this.flips = flips;
            this.startX = start.getX();
            this.startY = start.getY();
            this.startAngle = startAngle;
            this.startScale = startScale;
            this.endX = end.getX();
            this.endY = end.getY();
            this.endAngle = endAngle;
            this.endScale = endScale;
            this.durationMs = durationMs;
            this.delayMs = delayMs;
            double distance = Math.hypot(endX - startX, endY - startY);
            this.arcX = (startX + endX) / 2;
            this.arcY = Math.min(startY, endY) - Math.min(70, distance * 0.22);
        }

        private double eased() {
            double inverse = 1 - progress;
            return 1 - inverse * inverse * inverse;
        }

        private AffineTransform transform(double flipWidth) {
            double t = eased();
            double inverse = 1 - t;
            double x = inverse * inverse * startX + 2 * inverse * t * arcX + t * t * endX;
            double y = inverse * inverse * startY + 2 * inverse * t * arcY + t * t * endY;
            double tilt = Math.sin(Math.PI * t) * 0.16 * Math.signum(endX - startX);
            double angle = startAngle + (endAngle - startAngle) * t + tilt;
            double scale = (startScale + (endScale - startScale) * t) * (1 + 0.07 * Math.sin(Math.PI * t));
            AffineTransform transform = new AffineTransform();
            transform.translate(x, y);
            transform.rotate(angle);
            transform.scale(scale * flipWidth, scale);
            transform.translate(-45, -65);
            return transform;
        }

        private double flipWidth() {
            if (!flips) {
                return 1;
            }
            return Math.max(0.02, Math.abs(Math.cos(Math.PI * eased())));
        }

        private Card visibleFace() {
            if (!flips) {
                return secondFace;
            }
            return eased() < 0.5 ? firstFace : secondFace;
        }

        private double height() {
            return Math.sin(Math.PI * eased());
        }

        private Rectangle bounds() {
            Rectangle area = transform(flipWidth()).createTransformedShape(
                    new Rectangle(-2, -2, 94, 134)).getBounds();
            int shadow = 4 + (int) Math.ceil(12 * height());
            return new Rectangle(area.x - 3, area.y - 3, area.width + shadow + 6, area.height + shadow + 6);
        }

        private void paint(Graphics2D graphics) {
            double flipWidth = flipWidth();
            AffineTransform transform = transform(flipWidth);
            double lift = height();
            Graphics2D shadow = (Graphics2D) graphics.create();
            shadow.translate(3 + 9 * lift, 4 + 12 * lift);
            shadow.transform(transform);
            shadow.setColor(new java.awt.Color(0, 0, 0, 60 - (int) (20 * lift)));
            shadow.fill(new RoundRectangle2D.Double(0, 0, 90, 130, 12, 12));
            shadow.dispose();
            Graphics2D face = (Graphics2D) graphics.create();
            face.transform(transform);
            Card visible = visibleFace();
            if (visible != null) {
                CardComponent.paintCard(face, visible, false);
            } else {
                CardBackComponent.paintBack(face, false);
            }
            face.dispose();
        }
    }
}
