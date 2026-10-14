package uno.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
    private JLabel opponentLabel;
    private JLabel turnLabel;
    private JLabel statusLabel;
    private JPanel tablePanel;
    private JPanel handPanel;
    private JButton drawButton;
    private JButton saveButton;
    private JButton menuButton;
    private ActionListener cardActionListener;

    public GamePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        opponentLabel = new JLabel(" ");
        turnLabel = new JLabel(" ");
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(opponentLabel, BorderLayout.WEST);
        topPanel.add(turnLabel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        drawButton = new JButton("Draw");
        drawButton.setPreferredSize(new Dimension(90, 130));
        tablePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 60));
        add(tablePanel, BorderLayout.CENTER);

        handPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JScrollPane handScrollPane = new JScrollPane(handPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        handScrollPane.setPreferredSize(new Dimension(0, 165));

        statusLabel = new JLabel(" ");
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
    }

    public void refresh(GameState gameState, Player viewer, List<Card> validMoves, boolean isViewerTurn) {
        StringBuilder opponents = new StringBuilder();
        for (Player player : gameState.getPlayers()) {
            if (player != viewer) {
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
        tablePanel.add(new CardComponent(gameState.getDiscardPile().peekTopCard()));
        if (gameState.getDrawStackCount() > 0) {
            tablePanel.add(new JLabel("Draw stack: " + gameState.getDrawStackCount()));
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
            handPanel.add(cardComponent);
        }

        revalidate();
        repaint();
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
