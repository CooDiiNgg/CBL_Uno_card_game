package uno.view;

import java.util.List;
import java.util.Scanner;
import uno.controller.GameController;
import uno.logic.GameListener;
import uno.model.Card;
import uno.model.GameState;
import uno.model.Player;
import uno.persistence.SaveException;
import uno.util.Color;

public class TuiView implements GameListener {
    private GameController gameController;
    private Scanner scanner;

    public TuiView(GameController gameController) {
        if (gameController == null) {
            throw new IllegalArgumentException("GameController cannot be null");
        }
        this.gameController = gameController;
        this.scanner = new Scanner(System.in);
        gameController.addListener(this);
    }

    public void start() {
        System.out.println("=== UNO ===");
        while (true) {
            System.out.println();
            System.out.println("1) New game");
            System.out.println("2) Load game");
            System.out.println("3) Exit");
            String input = readLine("> ");
            if (input == null || input.equals("3")) {
                System.out.println("Bye!");
                return;
            }
            if (input.equals("1")) {
                String playerName = readLine("Your name: ");
                if (playerName == null) {
                    return;
                }
                gameController.startNewGame(playerName);
                playGame();
            } else if (input.equals("2")) {
                if (loadGame(null)) {
                    playGame();
                }
            } else {
                System.out.println("Unknown option");
            }
        }
    }

    private void playGame() {
        while (!gameController.isGameOver()) {
            if (gameController.isBotTurn()) {
                gameController.playBotTurn();
                continue;
            }
            printBoard();
            String input = readLine("> ");
            if (input == null || !handleCommand(input)) {
                return;
            }
        }
    }

    private boolean handleCommand(String input) {
        if (input.isEmpty()) {
            return true;
        }
        String[] parts = input.split("\\s+");
        switch (parts[0].toLowerCase()) {
            case "d":
            case "draw":
                drawCard();
                return true;
            case "s":
            case "save":
                saveGame(parts.length > 1 ? parts[1] : null);
                return true;
            case "l":
            case "load":
                loadGame(parts.length > 1 ? parts[1] : null);
                return true;
            case "h":
            case "help":
                printHelp();
                return true;
            case "q":
            case "quit":
                return false;
            default:
                playCard(parts[0]);
                return true;
        }
    }

    private void playCard(String input) {
        int handIndex;
        try {
            handIndex = Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Unknown command, type h for help");
            return;
        }
        List<Card> cards = gameController.getGameState().getCurrentPlayer().getHand().getCards();
        if (handIndex < 0 || handIndex >= cards.size()) {
            System.out.println("You don't have a card with number " + input);
            return;
        }
        Card card = cards.get(handIndex);
        Color chosenColor = null;
        if (card.getColor() == Color.SPECIAL && gameController.getValidMoves().contains(card)) {
            chosenColor = askColor();
            if (chosenColor == null) {
                return;
            }
        }
        if (!gameController.playCard(handIndex, chosenColor)) {
            System.out.println("You can't play " + card + " on " + gameController.getTopCard());
        }
    }

    private void drawCard() {
        Card drawnCard = gameController.drawCard();
        if (drawnCard != null) {
            System.out.println("You drew " + drawnCard);
        }
    }

    private void saveGame(String saveName) {
        if (saveName == null) {
            saveName = readLine("Save name: ");
            if (saveName == null) {
                return;
            }
        }
        try {
            System.out.println("Saved to " + gameController.saveGame(saveName));
        } catch (SaveException e) {
            System.out.println("Could not save: " + e.getMessage());
        }
    }

    private boolean loadGame(String saveName) {
        if (saveName == null) {
            String[] saveNames = gameController.getSaveNames();
            if (saveNames.length == 0) {
                System.out.println("No saved games found");
                return false;
            }
            System.out.println("Saved games: " + String.join(", ", saveNames));
            saveName = readLine("Save name: ");
            if (saveName == null) {
                return false;
            }
        }
        try {
            gameController.loadGame(saveName);
            return true;
        } catch (SaveException e) {
            System.out.println("Could not load: " + e.getMessage());
            return false;
        }
    }

    private Color askColor() {
        while (true) {
            String input = readLine("Choose a color (r)ed (b)lue (y)ellow (g)reen: ");
            if (input == null) {
                return null;
            }
            switch (input.toLowerCase()) {
                case "r":
                case "red":
                    return Color.RED;
                case "b":
                case "blue":
                    return Color.BLUE;
                case "y":
                case "yellow":
                    return Color.YELLOW;
                case "g":
                case "green":
                    return Color.GREEN;
                default:
                    System.out.println("Unknown color");
            }
        }
    }

    private void printBoard() {
        GameState gameState = gameController.getGameState();
        Player currentPlayer = gameState.getCurrentPlayer();
        List<Card> validMoves = gameController.getValidMoves();

        System.out.println();
        System.out.println("----------------------------------------");
        for (Player player : gameState.getPlayers()) {
            if (player != currentPlayer) {
                System.out.println(player.getName() + ": " + player.getHand().size() + " cards");
            }
        }
        System.out.println("Deck: " + gameState.getDeck().size() + " | Discard: " + gameState.getDiscardPile().size()
                + " | Direction: " + (gameState.isClockwise() ? "clockwise" : "counter-clockwise"));
        System.out.println("Top card: " + gameController.getTopCard());
        if (gameState.getDrawStackCount() > 0) {
            System.out.println("Draw stack: " + gameState.getDrawStackCount() + " (stack a draw card or 'd' to take them)");
        }
        System.out.println(currentPlayer.getName() + "'s hand:");
        List<Card> cards = currentPlayer.getHand().getCards();
        for (int i = 0; i < cards.size(); i++) {
            String marker = validMoves.contains(cards.get(i)) ? "  *" : "";
            System.out.println("  " + (i + 1) + ") " + cards.get(i) + marker);
        }
        System.out.println("(* = playable, h = help)");
    }

    private void printHelp() {
        System.out.println("<number>    play that card from your hand");
        System.out.println("d           draw a card (or take the whole draw stack)");
        System.out.println("s [name]    save the game");
        System.out.println("l [name]    load a saved game");
        System.out.println("q           back to the main menu");
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println();
            return null;
        }
        return scanner.nextLine().trim();
    }

    @Override
    public void onGameStarted(GameState gameState) {
        StringBuilder names = new StringBuilder();
        for (Player player : gameState.getPlayers()) {
            if (names.length() > 0) {
                names.append(" vs ");
            }
            names.append(player.getName());
        }
        System.out.println("Game started: " + names);
        System.out.println("It's " + gameState.getCurrentPlayer().getName() + "'s turn");
    }

    @Override
    public void onCardPlayed(Player player, Card card) {
        System.out.println(player.getName() + " played " + card);
    }

    @Override
    public void onCardsDrawn(Player player, int amount) {
        if (amount == 0) {
            System.out.println(player.getName() + " could not draw, there are no cards left");
            return;
        }
        System.out.println(player.getName() + " drew " + amount + (amount == 1 ? " card" : " cards"));
    }

    @Override
    public void onTurnChanged(Player player) {
        System.out.println("-> " + player.getName() + "'s turn");
    }

    @Override
    public void onUno(Player player) {
        System.out.println(player.getName() + ": UNO!");
    }

    @Override
    public void onGameOver(Player winner) {
        System.out.println();
        System.out.println("*** " + winner.getName() + " wins! ***");
    }
}
