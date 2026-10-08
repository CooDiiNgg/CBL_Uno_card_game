package uno.model;

public class GameState {
    
    private Deck deck;
    private Deck discardPile;
    private Player[] players;
    private int currentPlayerIndex;
    private boolean isClockwise;
    private int drawStackCount;

    public GameState(Deck deck, Deck discardPile, Player[] players) {
        this.deck = deck;
        this.discardPile = discardPile;
        this.players = players;
        this.currentPlayerIndex = 0;
        this.isClockwise = true;
    }

    public Deck getDeck() {
        return deck;
    }

    public Deck getDiscardPile() {
        return discardPile;
    }

    public Player[] getPlayers() {
        return players;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        if (currentPlayerIndex < 0 || currentPlayerIndex >= players.length) {
            throw new IndexOutOfBoundsException("Current player index out of bounds");
        }
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public Player getCurrentPlayer() {
        return players[currentPlayerIndex];
    }

    public boolean isClockwise() {
        return isClockwise;
    }

    public void reverseDirection() {
        isClockwise = !isClockwise;
    }

    public void nextTurn(){
        currentPlayerIndex = (currentPlayerIndex + (isClockwise ? 1 : -1) + players.length) % players.length;
    }

    public Player nextPlayer() {
        return players[(currentPlayerIndex + (isClockwise ? 1 : -1) + players.length) % players.length];
    }

    public Player previousPlayer() {
        return players[(currentPlayerIndex + (isClockwise ? -1 : 1) + players.length) % players.length];
    }

    public void setPlayers(Player[] players) {
        if (players == null || players.length == 0) {
            throw new IllegalArgumentException("Players array cannot be null or empty");
        }
        this.players = players;
    }

    public void setDeck(Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }
        this.deck = deck;
    }

    public void setDiscardPile(Deck discardPile) {
        if (discardPile == null) {
            throw new IllegalArgumentException("Discard pile cannot be null");
        }
        this.discardPile = discardPile;
    }

    public int getDrawStackCount() {
        return drawStackCount;
    }

    public void setDrawStackCount(int drawStackCount) {
        if (drawStackCount < 0) {
            throw new IllegalArgumentException("Draw stack count cannot be negative");
        }
        this.drawStackCount = drawStackCount;
    }
}
