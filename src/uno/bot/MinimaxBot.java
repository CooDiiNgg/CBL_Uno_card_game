package uno.bot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import uno.logic.GameEngine;
import uno.logic.GameListener;
import uno.logic.RuleValidator;
import uno.model.Card;
import uno.model.Deck;
import uno.model.GameState;
import uno.model.Hand;
import uno.model.Player;
import uno.util.Color;

public class MinimaxBot implements BotStrategy, GameListener {
    private static final String WEIGHTS_FILE = "resources/bot/minimax-weights.txt";
    private static final double[] DEFAULT_WEIGHTS = {0.0, -2.5, 2.5, 0.4, 0.6, 0.6, -0.3, 0.3, 0.3, -0.8, 1.0, -1.0};
    private static final double LEARNING_RATE = 0.02;
    private static final int DEFAULT_SAMPLES = 24;
    private static final int DEFAULT_DEPTH = 5;
    private static final int TRAINING_SAMPLES = 8;
    private static final int TRAINING_DEPTH = 3;

    private int samples;
    private int depth;
    private boolean learning;
    private double[] weights;
    private int gamesLearned;
    private Random random;
    private Color chosenColor;
    private GameState currentGame;
    private List<double[]> positions;
    private List<Player> positionOwners;

    public MinimaxBot() {
        this(DEFAULT_SAMPLES, DEFAULT_DEPTH, true);
    }

    public MinimaxBot(int samples, int depth, boolean learning) {
        if (samples < 1 || depth < 1) {
            throw new IllegalArgumentException("Samples and depth must be at least 1");
        }
        this.samples = samples;
        this.depth = depth;
        this.learning = learning;
        this.random = new Random();
        this.positions = new ArrayList<>();
        this.positionOwners = new ArrayList<>();
        loadWeights();
    }

    @Override
    public Card chooseCard(GameState gameState, Player player) {
        if (gameState == null || player == null) {
            throw new IllegalArgumentException("GameState and player cannot be null");
        }
        chosenColor = null;
        Card topCard = gameState.getDiscardPile().peekTopCard();
        List<Card> validMoves = RuleValidator.getValidMoves(player.getHand(), topCard, gameState.getDrawStackCount());
        if (validMoves.isEmpty()) {
            return null;
        }
        if (player.getHand().size() == 1) {
            chosenColor = bestColor(player.getHand());
            return validMoves.get(0);
        }

        List<Move> candidates = rootMoves(validMoves, gameState.getDrawStackCount() > 0);
        if (candidates.size() == 1) {
            return pick(candidates.get(0), player);
        }

        int meIndex = indexOf(gameState, player);
        List<GameState> worlds = new ArrayList<>();
        for (int i = 0; i < samples; i++) {
            worlds.add(determinize(gameState, meIndex));
        }

        Move bestMove = candidates.get(0);
        double bestScore = -1;
        for (Move move : candidates) {
            double total = 0;
            for (GameState world : worlds) {
                GameState child = copyState(world);
                applyMove(child, move);
                total += minimax(child, depth - 1, meIndex, -1, 2);
            }
            double score = total / worlds.size();
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return pick(bestMove, player);
    }

    @Override
    public Color chooseColor(GameState gameState, Player player) {
        if (chosenColor != null) {
            return chosenColor;
        }
        return bestColor(player.getHand());
    }

    private Card pick(Move move, Player player) {
        chosenColor = move.color != null ? move.color : bestColor(player.getHand());
        return move.card;
    }

    private double minimax(GameState state, int remainingDepth, int meIndex, double alpha, double beta) {
        Player[] players = state.getPlayers();
        if (RuleValidator.isGameOver(players)) {
            return players[meIndex].getHand().isEmpty() ? 1.0 : 0.0;
        }
        if (remainingDepth == 0) {
            return evaluate(state, meIndex);
        }
        boolean maximizing = state.getCurrentPlayerIndex() == meIndex;
        double best = maximizing ? -1 : 2;
        for (Move move : innerMoves(state)) {
            GameState child = copyState(state);
            applyMove(child, move);
            double value = minimax(child, remainingDepth - 1, meIndex, alpha, beta);
            if (maximizing) {
                best = Math.max(best, value);
                alpha = Math.max(alpha, best);
            } else {
                best = Math.min(best, value);
                beta = Math.min(beta, best);
            }
            if (beta <= alpha) {
                break;
            }
        }
        return best;
    }

    private List<Move> rootMoves(List<Card> validMoves, boolean stackPending) {
        List<Move> moves = new ArrayList<>();
        for (Card card : uniqueCards(validMoves)) {
            if (card.getColor() == Color.SPECIAL) {
                for (Color color : Color.getAllColors()) {
                    moves.add(new Move(card, color));
                }
            } else {
                moves.add(new Move(card, null));
            }
        }
        if (stackPending) {
            moves.add(new Move(null, null));
        }
        return moves;
    }

    private List<Move> innerMoves(GameState state) {
        Player player = state.getCurrentPlayer();
        List<Card> validMoves = RuleValidator.getValidMoves(player.getHand(),
                state.getDiscardPile().peekTopCard(), state.getDrawStackCount());
        List<Move> moves = new ArrayList<>();
        for (Card card : uniqueCards(validMoves)) {
            moves.add(new Move(card, card.getColor() == Color.SPECIAL ? bestColor(player.getHand()) : null));
        }
        if (moves.isEmpty() || state.getDrawStackCount() > 0) {
            moves.add(new Move(null, null));
        }
        return moves;
    }

    private List<Card> uniqueCards(List<Card> cards) {
        List<Card> unique = new ArrayList<>();
        for (Card card : cards) {
            boolean seen = false;
            for (Card other : unique) {
                if (other.getColor() == card.getColor() && other.getValue() == card.getValue()) {
                    seen = true;
                    break;
                }
            }
            if (!seen) {
                unique.add(card);
            }
        }
        return unique;
    }

    private void applyMove(GameState state, Move move) {
        GameEngine engine = new GameEngine(state);
        if (move.card == null) {
            engine.drawTurn();
        } else {
            engine.playTurn(move.card, move.color);
        }
    }

    private GameState determinize(GameState gameState, int meIndex) {
        Player[] players = gameState.getPlayers();
        List<Card> unknownCards = new ArrayList<>(gameState.getDeck().getCards());
        for (int i = 0; i < players.length; i++) {
            if (i != meIndex) {
                unknownCards.addAll(players[i].getHand().getCards());
            }
        }
        Collections.shuffle(unknownCards, random);

        Player[] sampledPlayers = new Player[players.length];
        for (int i = 0; i < players.length; i++) {
            Hand hand = new Hand();
            if (i == meIndex) {
                for (Card card : players[i].getHand().getCards()) {
                    hand.addCard(card);
                }
            } else {
                for (int j = 0; j < players[i].getHand().size(); j++) {
                    hand.addCard(unknownCards.remove(unknownCards.size() - 1));
                }
            }
            sampledPlayers[i] = new Player(players[i].getName(), hand, players[i].isBot());
        }
        Deck deck = new Deck(unknownCards);
        return buildState(gameState, deck, new Deck(gameState.getDiscardPile().getCards()), sampledPlayers);
    }

    private GameState copyState(GameState gameState) {
        Player[] players = gameState.getPlayers();
        Player[] copiedPlayers = new Player[players.length];
        for (int i = 0; i < players.length; i++) {
            Hand hand = new Hand();
            for (Card card : players[i].getHand().getCards()) {
                hand.addCard(card);
            }
            copiedPlayers[i] = new Player(players[i].getName(), hand, players[i].isBot());
        }
        return buildState(gameState, new Deck(gameState.getDeck().getCards()),
                new Deck(gameState.getDiscardPile().getCards()), copiedPlayers);
    }

    private GameState buildState(GameState original, Deck deck, Deck discardPile, Player[] players) {
        GameState state = new GameState(deck, discardPile, players);
        state.setCurrentPlayerIndex(original.getCurrentPlayerIndex());
        state.setClockwise(original.isClockwise());
        state.setDrawStackCount(original.getDrawStackCount());
        return state;
    }

    private double evaluate(GameState state, int meIndex) {
        return sigmoid(dot(weights, features(state, meIndex)));
    }

    private double[] features(GameState state, int meIndex) {
        Player[] players = state.getPlayers();
        Hand hand = players[meIndex].getHand();
        int opponentCards = Integer.MAX_VALUE;
        for (int i = 0; i < players.length; i++) {
            if (i != meIndex) {
                opponentCards = Math.min(opponentCards, players[i].getHand().size());
            }
        }

        Color topColor = state.getDiscardPile().peekTopCard().getColor();
        int actions = 0;
        int wilds = 0;
        int drawFours = 0;
        int matching = 0;
        boolean[] colorsHeld = new boolean[16];
        for (Card card : hand.getCards()) {
            if (card.getValue() >= 10 && card.getValue() <= 12) {
                actions++;
            } else if (card.getValue() == 13) {
                wilds++;
            } else if (card.getValue() == 14) {
                drawFours++;
            }
            if (card.getColor() != Color.SPECIAL) {
                colorsHeld[card.getColor().getColor()] = true;
                if (card.getColor() == topColor) {
                    matching++;
                }
            }
        }
        int distinctColors = 0;
        for (Color color : Color.getAllColors()) {
            if (colorsHeld[color.getColor()]) {
                distinctColors++;
            }
        }
        boolean isMyTurn = state.getCurrentPlayerIndex() == meIndex;
        int stackPressure = isMyTurn ? state.getDrawStackCount() : -state.getDrawStackCount();

        return new double[]{
                1.0,
                hand.size() / 10.0,
                opponentCards / 10.0,
                actions / 5.0,
                wilds / 4.0,
                drawFours / 4.0,
                distinctColors / 4.0,
                matching / 5.0,
                isMyTurn ? 1.0 : 0.0,
                stackPressure / 4.0,
                hand.size() == 1 ? 1.0 : 0.0,
                opponentCards == 1 ? 1.0 : 0.0
        };
    }

    private Color bestColor(Hand hand) {
        Color[] colors = Color.getAllColors();
        int[] counts = new int[colors.length];
        for (Card card : hand.getCards()) {
            for (int i = 0; i < colors.length; i++) {
                if (card.getColor() == colors[i]) {
                    counts[i]++;
                }
            }
        }
        int bestIndex = 0;
        for (int i = 1; i < colors.length; i++) {
            if (counts[i] > counts[bestIndex]) {
                bestIndex = i;
            }
        }
        return colors[bestIndex];
    }

    private int indexOf(GameState gameState, Player player) {
        Player[] players = gameState.getPlayers();
        for (int i = 0; i < players.length; i++) {
            if (players[i] == player) {
                return i;
            }
        }
        throw new IllegalArgumentException("Player is not part of this game");
    }

    private static double dot(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    private static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private void recordPositions() {
        if (!learning || currentGame == null) {
            return;
        }
        Player[] players = currentGame.getPlayers();
        for (int i = 0; i < players.length; i++) {
            if (players[i].isBot()) {
                positions.add(features(currentGame, i));
                positionOwners.add(players[i]);
            }
        }
    }

    private void learn(Player winner) {
        for (int i = 0; i < positions.size(); i++) {
            double[] position = positions.get(i);
            double target = positionOwners.get(i) == winner ? 1.0 : 0.0;
            double error = target - sigmoid(dot(weights, position));
            for (int j = 0; j < weights.length; j++) {
                weights[j] += LEARNING_RATE * error * position[j];
            }
        }
        positions.clear();
        positionOwners.clear();
        gamesLearned++;
    }

    public double[] getWeights() {
        return weights.clone();
    }

    public int getGamesLearned() {
        return gamesLearned;
    }

    public void setLearning(boolean learning) {
        this.learning = learning;
    }

    private void loadWeights() {
        weights = DEFAULT_WEIGHTS.clone();
        gamesLearned = 0;
        File file = new File(WEIGHTS_FILE);
        if (!file.isFile()) {
            return;
        }
        try {
            double[] loaded = DEFAULT_WEIGHTS.clone();
            int games = 0;
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                String[] parts = line.split("=");
                if (parts.length != 2) {
                    continue;
                }
                if (parts[0].equals("games")) {
                    games = Integer.parseInt(parts[1].trim());
                } else if (parts[0].startsWith("w")) {
                    int index = Integer.parseInt(parts[0].substring(1));
                    if (index >= 0 && index < loaded.length) {
                        loaded[index] = Double.parseDouble(parts[1].trim());
                    }
                }
            }
            weights = loaded;
            gamesLearned = games;
        } catch (IOException | NumberFormatException e) {
            weights = DEFAULT_WEIGHTS.clone();
            gamesLearned = 0;
        }
    }

    public void saveWeights() {
        StringBuilder builder = new StringBuilder();
        builder.append("games=").append(gamesLearned).append('\n');
        for (int i = 0; i < weights.length; i++) {
            builder.append('w').append(i).append('=').append(weights[i]).append('\n');
        }
        File file = new File(WEIGHTS_FILE);
        try {
            file.getParentFile().mkdirs();
            Files.write(file.toPath(), builder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Could not save bot weights: " + e.getMessage());
        }
    }

    @Override
    public void onGameStarted(GameState gameState) {
        currentGame = gameState;
        positions.clear();
        positionOwners.clear();
        recordPositions();
    }

    @Override
    public void onCardPlayed(Player player, Card card) {
    }

    @Override
    public void onCardsDrawn(Player player, int amount) {
    }

    @Override
    public void onTurnChanged(Player player) {
        recordPositions();
    }

    @Override
    public void onUno(Player player) {
    }

    @Override
    public void onGameOver(Player winner) {
        if (!learning || currentGame == null) {
            return;
        }
        learn(winner);
        saveWeights();
        currentGame = null;
    }

    private static Player playGame(BotStrategy first, BotStrategy second) {
        Player[] players = new Player[]{new Player("First", new Hand(), true), new Player("Second", new Hand(), true)};
        GameState gameState = GameEngine.createGame(players);
        GameEngine engine = new GameEngine(gameState);
        BotStrategy[] bots = new BotStrategy[]{first, second};
        for (int i = 0; i < bots.length; i++) {
            if (bots[i] instanceof MinimaxBot && (i == 0 || bots[i] != bots[0])) {
                engine.addListener((MinimaxBot) bots[i]);
                ((MinimaxBot) bots[i]).onGameStarted(gameState);
            }
        }
        int turns = 0;
        while (!engine.isGameOver() && turns < 2000) {
            int index = gameState.getCurrentPlayerIndex();
            Player player = gameState.getCurrentPlayer();
            Card card = bots[index].chooseCard(gameState, player);
            if (card == null || !engine.canPlay(card)) {
                engine.drawTurn();
            } else {
                Color color = card.getColor() == Color.SPECIAL ? bots[index].chooseColor(gameState, player) : null;
                engine.playTurn(card, color);
            }
            turns++;
        }
        return engine.getWinner();
    }

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "test";
        int games = args.length > 1 ? Integer.parseInt(args[1]) : 200;

        if (mode.equals("train")) {
            MinimaxBot bot = new MinimaxBot(TRAINING_SAMPLES, TRAINING_DEPTH, true);
            for (int i = 1; i <= games; i++) {
                playGame(bot, bot);
                if (i % 500 == 0 || i == games) {
                    System.out.println("Trained " + i + "/" + games + " games (" + bot.getGamesLearned() + " in total)");
                }
            }
            System.out.println("Weights saved to " + WEIGHTS_FILE);
            return;
        }

        MinimaxBot bot = new MinimaxBot(DEFAULT_SAMPLES, DEFAULT_DEPTH, false);
        BasicBot basicBot = new BasicBot();
        int wins = 0;
        for (int i = 0; i < games; i++) {
            boolean minimaxFirst = i % 2 == 0;
            Player winner = minimaxFirst ? playGame(bot, basicBot) : playGame(basicBot, bot);
            if (winner != null && winner.getName().equals(minimaxFirst ? "First" : "Second")) {
                wins++;
            }
        }
        System.out.println("MinimaxBot won " + wins + "/" + games + " games against BasicBot ("
                + Math.round(100.0 * wins / games) + "%)");
    }

    private static class Move {
        private Card card;
        private Color color;

        private Move(Card card, Color color) {
            this.card = card;
            this.color = color;
        }
    }
}
