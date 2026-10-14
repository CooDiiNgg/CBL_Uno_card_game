package uno.persistence;

import java.nio.charset.StandardCharsets;
import uno.model.Card;
import uno.util.Color;
import uno.util.Value;

public class GameSerializer {
    private static final int VERSION = 1;

    public static byte[] serialize(SaveState saveState) {
        if (saveState == null) {
            throw new IllegalArgumentException("SaveState cannot be null");
        }
        String[] playerNames = saveState.getPlayerNames();
        boolean[] playerBots = saveState.getPlayerBots();
        Card[][] playerHands = saveState.getPlayerHands();

        StringBuilder builder = new StringBuilder();
        writeLine(builder, "version", String.valueOf(VERSION));
        writeLine(builder, "currentPlayer", String.valueOf(saveState.getCurrentPlayerIndex()));
        writeLine(builder, "clockwise", String.valueOf(saveState.isClockwise()));
        writeLine(builder, "drawStack", String.valueOf(saveState.getDrawStackCount()));
        writeLine(builder, "players", String.valueOf(playerNames.length));
        for (int i = 0; i < playerNames.length; i++) {
            writeLine(builder, "name", playerNames[i].replace('\n', ' ').replace('\r', ' '));
            writeLine(builder, "bot", String.valueOf(playerBots[i]));
            writeLine(builder, "hand", writeCards(playerHands[i]));
        }
        writeLine(builder, "deck", writeCards(saveState.getDeckCards()));
        writeLine(builder, "discard", writeCards(saveState.getDiscardCards()));
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static SaveState deserialize(byte[] data) throws SaveException {
        if (data == null) {
            throw new SaveException("There is no data to deserialize");
        }
        String[] lines = new String(data, StandardCharsets.UTF_8).split("\n", -1);
        try {
            int line = 0;
            int version = Integer.parseInt(readValue(lines, line++, "version"));
            if (version != VERSION) {
                throw new SaveException("Unsupported save version: " + version);
            }
            int currentPlayerIndex = Integer.parseInt(readValue(lines, line++, "currentPlayer"));
            boolean clockwise = readBoolean(readValue(lines, line++, "clockwise"));
            int drawStackCount = Integer.parseInt(readValue(lines, line++, "drawStack"));
            int playerCount = Integer.parseInt(readValue(lines, line++, "players"));
            if (playerCount < 2 || playerCount > 10) {
                throw new SaveException("Invalid number of players: " + playerCount);
            }

            String[] playerNames = new String[playerCount];
            boolean[] playerBots = new boolean[playerCount];
            Card[][] playerHands = new Card[playerCount][];
            for (int i = 0; i < playerCount; i++) {
                playerNames[i] = readValue(lines, line++, "name");
                playerBots[i] = readBoolean(readValue(lines, line++, "bot"));
                playerHands[i] = readCards(readValue(lines, line++, "hand"));
            }
            Card[] deckCards = readCards(readValue(lines, line++, "deck"));
            Card[] discardCards = readCards(readValue(lines, line++, "discard"));

            return new SaveState(playerNames, playerBots, playerHands, deckCards, discardCards,
                    currentPlayerIndex, clockwise, drawStackCount);
        } catch (IllegalArgumentException e) {
            throw new SaveException("Save data is invalid: " + e.getMessage(), e);
        }
    }

    private static void writeLine(StringBuilder builder, String key, String value) {
        builder.append(key).append('=').append(value).append('\n');
    }

    private static String writeCards(Card[] cards) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cards.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(cards[i].getColor().name()).append(':').append(Value.fromValue(cards[i].getValue()).name());
        }
        return builder.toString();
    }

    private static String readValue(String[] lines, int index, String key) throws SaveException {
        if (index >= lines.length) {
            throw new SaveException("Save data ended too early, expected " + key);
        }
        String prefix = key + "=";
        if (!lines[index].startsWith(prefix)) {
            throw new SaveException("Expected " + key + " on line " + (index + 1));
        }
        return lines[index].substring(prefix.length());
    }

    private static boolean readBoolean(String value) throws SaveException {
        if (value.equals("true")) {
            return true;
        }
        if (value.equals("false")) {
            return false;
        }
        throw new SaveException("Expected true or false but found " + value);
    }

    private static Card[] readCards(String value) throws SaveException {
        if (value.isEmpty()) {
            return new Card[0];
        }
        String[] tokens = value.split(",");
        Card[] cards = new Card[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String[] parts = tokens[i].split(":");
            if (parts.length != 2) {
                throw new SaveException("Invalid card: " + tokens[i]);
            }
            cards[i] = new Card(Color.valueOf(parts[0]), Value.valueOf(parts[1]).getValue());
        }
        return cards;
    }
}
