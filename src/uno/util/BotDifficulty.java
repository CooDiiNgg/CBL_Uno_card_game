package uno.util;

public enum BotDifficulty {
    EASY("Easy", "Plays the first card that fits"),
    HARD("Hard", "Minimax search that learns from every game");

    private final String displayName;
    private final String description;

    BotDifficulty(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
